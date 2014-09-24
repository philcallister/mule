/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleRuntimeException;
import org.mule.config.i18n.MessageFactory;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.introspection.ExtensionDiscoverer;
import org.mule.util.ClassUtils;
import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

final class DefaultExtensionDiscoverer implements ExtensionDiscoverer
{

    private static final String EXTENSIONS_PROPERTIES = "META-INF/extensions/mule.extensions";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Extension> discover(ClassLoader classLoader, ExtensionDescriber describer)
    {
        checkArgument(classLoader != null, "classloader cannot be null");
        checkArgument(describer != null, "describer cannot be null");

        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(classLoader);
            Enumeration<URL> allExtensions = classLoader.getResources(EXTENSIONS_PROPERTIES);

            ImmutableList.Builder<Extension> extensions = ImmutableList.builder();

            while (allExtensions.hasMoreElements())
            {
                String lines = read(allExtensions.nextElement());
                for (String line : lines.split(System.lineSeparator()))
                {
                    line = line.trim();

                    if (StringUtils.isBlank(line))
                    {
                        continue; // skip empty line
                    }

                    extensions.add(loadExtension(line, classLoader, describer));
                }
            }

            return extensions.build();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to find extensions", e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private Extension loadExtension(String extensionClassname, ClassLoader classLoader, ExtensionDescriber describer)
    {
        try
        {
            Class<?> extensionClass = ClassUtils.loadClass(extensionClassname, classLoader);

            ExtensionBuilder builder = DefaultExtensionBuilder.newBuilder();
            describer.describe(new ImmutableExtensionDescribingContext(extensionClass, builder));

            return builder.build();
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException(String.format("Extension %s was declared but not found in classpath", extensionClassname), e);
        }
    }

    private String read(URL url)
    {
        try (InputStream in = url.openStream())
        {
            return IOUtils.toString(in);
        }
        catch (IOException e)
        {
            throw new MuleRuntimeException(
                    MessageFactory.createStaticMessage("Could not read extension descriptor at " + url.getPath()), e);
        }
    }

}
