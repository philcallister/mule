/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal;

import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.spi.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.spi.ExtensionParameterBuilder;

import java.util.LinkedList;
import java.util.List;

final class DefaultExtensionConfigurationBuilder implements ExtensionConfigurationBuilder
{

    private String name;
    private String description;
    private List<ExtensionParameterBuilder> parameters = new LinkedList<>();

    DefaultExtensionConfigurationBuilder()
    {
    }

    @Override
    public ExtensionConfigurationBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public ExtensionConfigurationBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public ExtensionConfigurationBuilder addParameter(ExtensionParameterBuilder parameter)
    {
        parameters.add(parameter);
        return this;
    }

    @Override
    public ExtensionConfiguration build()
    {
        return new ImmutableExtensionConfiguration(name, description, MuleExtensionUtils.build(parameters));
    }

}
