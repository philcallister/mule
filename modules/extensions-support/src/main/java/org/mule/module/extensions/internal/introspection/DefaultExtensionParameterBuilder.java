/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.extensions.introspection.api.ExtensionParameterBuilder;

import org.apache.commons.lang.StringUtils;

final class DefaultExtensionParameterBuilder implements NavigableExtensionParameterBuilder
{

    private String name;
    private String description = StringUtils.EMPTY;
    private DataType type;
    private boolean required = false;
    private boolean dynamic = true;
    private Object defaultValue;

    DefaultExtensionParameterBuilder()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameterBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameterBuilder setDescription(String description)
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
    public ExtensionParameterBuilder setType(DataType type)
    {
        this.type = type;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getType()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameterBuilder setRequired(boolean required)
    {
        this.required = required;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequired()
    {
        return required;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameterBuilder setDynamic(boolean dynamic)
    {
        this.dynamic = dynamic;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic()
    {
        return dynamic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameterBuilder setDefaultValue(Object defaultValue)
    {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameter build()
    {
        if (required && defaultValue != null)
        {
            throw new IllegalStateException("If a parameter is required then it cannot have a default value");
        }

        return new ImmutableExtensionParameter(name,
                                               description,
                                               type,
                                               required,
                                               dynamic,
                                               defaultValue);
    }
}
