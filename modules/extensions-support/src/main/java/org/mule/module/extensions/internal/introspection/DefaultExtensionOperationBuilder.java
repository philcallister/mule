/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionOperationBuilder;
import org.mule.extensions.introspection.api.ExtensionParameterBuilder;
import org.mule.module.extensions.internal.util.MuleExtensionUtils;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

final class DefaultExtensionOperationBuilder implements NavigableExtensionOperationBuilder
{

    private String name;
    private String description = StringUtils.EMPTY;
    private Class<?> declaringClass;
    private List<DataType> inputTypes = new LinkedList<>();
    private DataType outputType = null;
    private List<ExtensionParameterBuilder> parameters = new LinkedList<>();

    DefaultExtensionOperationBuilder()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionOperationBuilder setName(String name)
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
    public ExtensionOperationBuilder setDescription(String description)
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
    public ExtensionOperationBuilder addInputType(DataType... type)
    {
        inputTypes.addAll(Arrays.asList(type));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataType> getInputDataTypes()
    {
        return ImmutableList.copyOf(inputTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionOperationBuilder setOutputType(DataType type)
    {
        outputType = type;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass()
    {
        return declaringClass;
    }

    @Override
    public ExtensionOperationBuilder setDeclaringClass(Class<?> declaringClass)
    {
        this.declaringClass = declaringClass;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getOutputDataType()
    {
        return outputType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionOperationBuilder addParameter(ExtensionParameterBuilder parameter)
    {
        parameters.add(parameter);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ExtensionParameterBuilder> getParameters()
    {
        return ImmutableList.copyOf(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionOperation build()
    {
        if (inputTypes.isEmpty())
        {
            inputTypes.add(ImmutableDataType.of(Object.class));
        }

        return new ImmutableExtensionOperation(name,
                                               description,
                                               declaringClass,
                                               inputTypes,
                                               outputType,
                                               MuleExtensionUtils.build(parameters));
    }
}
