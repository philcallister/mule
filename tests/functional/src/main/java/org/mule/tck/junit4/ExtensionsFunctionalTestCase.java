/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tck.junit4;

import org.mule.api.MuleContext;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.resources.api.GenerableResource;
import org.mule.extensions.resources.api.ResourcesGenerator;
import org.mule.extensions.resources.spi.GenerableResourceContributor;
import org.mule.module.extensions.internal.ImmutableExtensionDescribingContext;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.introspection.DefaultExtensionDescriber;
import org.mule.module.extensions.internal.resources.AbstractResourcesGenerator;
import org.mule.util.ArrayUtils;
import org.mule.util.IOUtils;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;

/**
 * Base test class for {@link org.mule.tck.junit4.FunctionalTestCase}s
 * that make use of components generated through the extensions API.
 * <p/>
 * The value added by this class in comparison to a traditional
 * {@link org.mule.tck.junit4.FunctionalTestCase} is that before creating
 * the {@link org.mule.api.MuleContext}, it scans certain packages of the
 * classpath and discovers extensions. Once those are discovered and described,
 * a {@link org.mule.extensions.resources.api.ResourcesGenerator} is used to automatically
 * generate any backing resources needed (for example, XSD schemas, spring bundles,
 * service registration files, etc).
 * <p/>
 * In this way, the user experience is greatly simplified when running the test
 * either through an IDE or build tool such as maven or gradle.
 * <p/>
 * By default, the only packaged scanned for extensions is
 * &quot;org.mule.extension&quot; but that can be customized
 * by overriding the {@link #getDiscoverablePackages()} method
 *
 * @since 3.6.0
 */
public abstract class ExtensionsFunctionalTestCase extends FunctionalTestCase
{

    @Override
    protected MuleContext createMuleContext() throws Exception
    {
        discoverExtensions();
        return super.createMuleContext();
    }

    /**
     * Returns an array with the packages that are to be scanned
     */
    protected String[] getDiscoverablePackages()
    {
        return new String[] {"org.mule.extension"};
    }

    /**
     * Returns an array with extension types for which this test should
     * generate resources. This is useful when several extensions exist in the same
     * package but you want to focus the test on only a subset of them.
     * If this method returns {@code null} or an empty array, then all discovered
     * extensions will get resources generated. Otherwise, discovered extensions
     * not listed here will depend exclusively on whatever resources are already
     * present on the classpath.
     * Default implementation of this method returns {@code null}
     */
    protected Class<?>[] getManagedExtensionTypes()
    {
        return null;
    }

    private List<GenerableResourceContributor> getGenerableResourceContributors()
    {
        return ImmutableList.copyOf(ServiceRegistry.lookupProviders(GenerableResourceContributor.class));
    }

    private void discoverExtensions() throws Exception
    {
        Reflections reflections = new Reflections(getDiscoverablePackages());
        List<Extension> extensions = new LinkedList<>();

        Class<?>[] managedExtensionTypes = getManagedExtensionTypes();
        final boolean filtersExtensions = managedExtensionTypes != null && managedExtensionTypes.length > 0;

        for (Class<?> extensionType : reflections.getTypesAnnotatedWith(org.mule.extensions.api.annotation.Extension.class))
        {
            if (filtersExtensions && !ArrayUtils.contains(managedExtensionTypes, extensionType))
            {
                continue;
            }

            ExtensionBuilder builder = DefaultExtensionBuilder.newBuilder();

            new DefaultExtensionDescriber().describe(new ImmutableExtensionDescribingContext(extensionType, builder));

            extensions.add(builder.build());
        }

        File targetDirectory = getGenerationTargetDirectory();

        ResourcesGenerator generator = new ExtensionsTestInfrastructureResourcesGenerator(targetDirectory);

        List<GenerableResourceContributor> resourceContributors = getGenerableResourceContributors();
        for (Extension extension : extensions)
        {
            for (GenerableResourceContributor contributor : resourceContributors)
            {
                contributor.contribute(extension, generator);
            }
        }

        generateResourcesAndAddToClasspath(generator);
    }

    private void generateResourcesAndAddToClasspath(ResourcesGenerator generator) throws Exception
    {
        ClassLoader cl = getClass().getClassLoader();
        Method method = org.springframework.util.ReflectionUtils.findMethod(cl.getClass(), "addURL", URL.class);
        method.setAccessible(true);

        for (GenerableResource resource : generator.dumpAll())
        {
            URL generatedResourceURL = new File(resource.getFilePath()).toURI().toURL();
            method.invoke(cl, generatedResourceURL);
        }
    }

    private File getGenerationTargetDirectory()
    {
        URL url = IOUtils.getResourceAsUrl(getEffectiveConfigFile(), getClass(), true, true);
        File targetDirectory = new File(FileUtils.toFile(url).getParentFile(), "META-INF");

        if (!targetDirectory.exists() && !targetDirectory.mkdir())
        {
            throw new RuntimeException("Could not create target directory " + targetDirectory.getAbsolutePath());
        }

        return targetDirectory;
    }

    private String getEffectiveConfigFile()
    {
        String configFile = getConfigFile();
        if (configFile != null)
        {
            return configFile;
        }

        configFile = getConfigFileFromSplittable(getConfigurationResources());
        if (configFile != null)
        {
            return configFile;
        }

        configFile = getConfigFileFromSplittable(getConfigResources());
        if (configFile != null)
        {
            return configFile;
        }

        String[] configFiles = getConfigFiles();
        if (!ArrayUtils.isEmpty(configFiles))
        {
            return configFiles[0].trim();
        }

        throw new IllegalArgumentException("No valid config file was specified");
    }

    private String getConfigFileFromSplittable(String configFile)
    {
        if (configFile != null)
        {
            return configFile.split(",")[0].trim();
        }

        return null;
    }


    private class ExtensionsTestInfrastructureResourcesGenerator extends AbstractResourcesGenerator
    {

        private File targetDirectory;

        private ExtensionsTestInfrastructureResourcesGenerator(File targetDirectory)
        {
            this.targetDirectory = targetDirectory;
        }

        @Override
        protected void write(GenerableResource resource)
        {
            File targetFile = new File(targetDirectory, resource.getFilePath());
            try
            {
                FileUtils.write(targetFile, resource.getContentBuilder().toString());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
    }
}
