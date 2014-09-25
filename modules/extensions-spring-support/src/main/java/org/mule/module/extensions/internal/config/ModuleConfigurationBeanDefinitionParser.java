/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.config;

import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.module.extensions.internal.runtime.resolver.ModuleConfigurationValueResolver;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class ModuleConfigurationBeanDefinitionParser extends ExtensionConfigurationBeanDefinitionParser
{

    public ModuleConfigurationBeanDefinitionParser(Extension extension, ExtensionConfiguration configuration)
    {
        super(extension, configuration);
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder, ParserContext parserContext)
    {
        builder.addConstructorArgValue(configuration.getDeclaringClass());
        builder.addConstructorArgValue(getResolverSet(element));
    }

    @Override
    protected Class<? extends ValueResolver> getResolverClass()
    {
        return ModuleConfigurationValueResolver.class;
    }
}
