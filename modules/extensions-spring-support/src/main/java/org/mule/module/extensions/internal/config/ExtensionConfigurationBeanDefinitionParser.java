/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.config;

import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionParameter;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ExtensionConfigurationBeanDefinitionParser extends AbstractExtensionBeanDefinitionParser
{

    private final Extension extension;
    private final ExtensionConfiguration configuration;

    public ExtensionConfigurationBeanDefinitionParser(Extension extension, ExtensionConfiguration configuration)
    {
        this.extension = extension;
        this.configuration = configuration;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        parseConfigName(element);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(extension.getActingClass());
        builder.setScope(BeanDefinition.SCOPE_SINGLETON);

        for (ExtensionParameter parameter : configuration.getParameters())
        {
            parseParameter(parameter, builder, element);
        }

        BeanDefinition definition = builder.getBeanDefinition();
        setNoRecurseOnDefinition(definition);

        return definition;
    }
}
