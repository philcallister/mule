/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.spi.ExtensionOperationBuilder;
import org.mule.extensions.introspection.spi.ExtensionParameterBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

final class DefaultExtensionOperationBuilder implements ExtensionOperationBuilder
{

    private String name;
    private String description = StringUtils.EMPTY;
    private List<DataType> inputTypes = new LinkedList<>();
    private DataType outputType = null;
    private List<ExtensionParameterBuilder> parameters = new LinkedList<>();

    DefaultExtensionOperationBuilder()
    {
    }

    @Override
    public ExtensionOperationBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public ExtensionOperationBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public ExtensionOperationBuilder addInputType(DataType... type)
    {
        inputTypes.addAll(Arrays.asList(type));
        return this;
    }

    @Override
    public ExtensionOperationBuilder setOutputType(DataType type)
    {
        outputType = type;
        return this;
    }

    @Override
    public ExtensionOperationBuilder addParameter(ExtensionParameterBuilder parameter)
    {
        parameters.add(parameter);
        return this;
    }

    protected <T> void addAll(List<T> list, T[] elements)
    {
        if (elements != null)
        {
            list.addAll(Arrays.asList(elements));
        }
    }

    @Override
    public ExtensionOperation build()
    {
        if (inputTypes.isEmpty())
        {
            inputTypes.add(ImmutableDataType.of(Object.class));
        }

        return new ImmutableExtensionOperation(name,
                                               description,
                                               inputTypes,
                                               outputType,
                                               MuleExtensionUtils.build(parameters));
    }
}
