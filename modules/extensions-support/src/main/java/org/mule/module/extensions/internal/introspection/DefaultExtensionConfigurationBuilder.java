/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionParameterBuilder;
import org.mule.module.extensions.internal.util.MuleExtensionUtils;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

final class DefaultExtensionConfigurationBuilder implements NavigableExtensionConfigurationBuilder
{

    private String name;
    private String description;
    private Class<?> declaringClass;
    private List<ExtensionParameterBuilder> parameters = new LinkedList<>();

    DefaultExtensionConfigurationBuilder()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfigurationBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfigurationBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription()
    {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass()
    {
        return declaringClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfigurationBuilder setDeclaringClass(Class<?> declaringClass)
    {
        this.declaringClass = declaringClass;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfigurationBuilder addParameter(ExtensionParameterBuilder parameter)
    {
        parameters.add(parameter);
        return this;
    }

    @Override
    public List<ExtensionParameterBuilder> getParameters()
    {
        return ImmutableList.copyOf(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfiguration build()
    {
        return new ImmutableExtensionConfiguration(name, description, declaringClass, MuleExtensionUtils.build(parameters));
    }

}
