/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.config;

import static org.mule.util.Preconditions.checkState;
import org.mule.config.spring.MuleArtifactContext;
import org.mule.extensions.api.ExtensionsManager;
import org.mule.extensions.introspection.api.Described;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.util.ClassUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Generic implementation of {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * capable of parsing configurations and operations for any given {@link org.mule.extensions.introspection.api.Extension}
 * which supports the given namespace.
 * <p/>
 * For this namespace handler to function, an instance of {@link org.mule.extensions.api.ExtensionsManager}
 * has to be accessible and the {@link org.mule.extensions.api.ExtensionsManager#discoverExtensions(ClassLoader)}
 * needs to have successfully discovered and register extensions.
 *
 * @since 3.7.0
 */
public class ExtensionsNamespaceHandler extends NamespaceHandlerSupport
{

    private ExtensionsManager extensionsManager;
    private Map<String, Extension> handledExtensions = new HashMap<>();

    /**
     * Attempts to get a hold on a {@link org.mule.extensions.api.ExtensionsManager}
     * instance
     *
     * @throws java.lang.IllegalStateException if no extension manager could be found
     */
    @Override
    public void init()
    {
        extensionsManager = MuleArtifactContext.getCurrentMuleContext().get().getExtensionsManager();
        checkState(extensionsManager != null, "Could not obtain handledExtensions manager");
    }

    /**
     * Registers parsers for the given element, provided that an extension can be found which
     * responds to that namespace
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        String namespace = element.getNamespaceURI();
        if (!isHandled(namespace))
        {
            registerExtensionParsers(namespace, element, parserContext);
        }

        return super.parse(element, parserContext);
    }

    private boolean isHandled(String namespace)
    {
        return handledExtensions.containsKey(namespace);
    }

    private void registerExtensionParsers(String namespace, Element element, ParserContext parserContext)
    {
        try
        {
            Extension extension = locateExtensionByNamespace(namespace);

            //TODO: Configuration parser should be determined based on capabilities
            register(extension, extension.getConfigurations(), ModuleConfigurationBeanDefinitionParser.class);

            register(extension, extension.getOperations(), ExtensionOperationBeanDefinitionParser.class);

            handledExtensions.put(namespace, extension);
        }
        catch (Exception e)
        {
            parserContext.getReaderContext().fatal(e.getMessage(), element, e);
        }

    }

    private void register(Extension extension, Collection<? extends Described> objects, Class<? extends BeanDefinitionParser> parserType) throws Exception
    {
        for (Described described : objects)
        {
            registerBeanDefinitionParser(described.getName(), ClassUtils.instanciateClass(parserType, extension, described));
        }
    }

    private Extension locateExtensionByNamespace(String namespace)
    {
        Collection<Extension> capableExtensions = extensionsManager.getExtensionsCapableOf(XmlCapability.class);
        if (CollectionUtils.isEmpty(capableExtensions))
        {
            throw new IllegalArgumentException(
                    String.format("Could not find any handled extensions supporting XML capabilities. Can't process namespace %s", namespace));
        }

        for (Extension extension : capableExtensions)
        {
            XmlCapability capability = extension.getCapabilities(XmlCapability.class).iterator().next();
            if (namespace.equals(capability.getSchemaLocation()))
            {
                return extension;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find extension associated to namespace %s", namespace));
    }

}
