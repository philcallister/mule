/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal;

import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.spi.ExtensionOperationBuilder;
import org.mule.extensions.introspection.spi.ExtensionParameterBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class DefaultExtensionOperationBuilder implements ExtensionOperationBuilder
{

    private String name;
    private String description;
    private Map<Class<?>, Class<?>[]> inputTypes = new LinkedHashMap<>();
    private Class<?> outputType = null;
    private Class<?>[] outputGenericTypes = new Class<?>[] {};
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
    public ExtensionOperationBuilder addInputType(Class<?> type)
    {
        return addInputType(type, new Class<?>[] {});
    }

    @Override
    public ExtensionOperationBuilder addInputType(Class<?> type, Class<?>... genericTypes)
    {
        inputTypes.put(type, genericTypes);
        return this;
    }

    @Override
    public ExtensionOperationBuilder setOutputType(Class<?> type, Class<?>... genericTypes)
    {
        outputType = type;
        outputGenericTypes = genericTypes != null ? genericTypes : new Class<?>[] {};

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
        return new ImmutableExtensionOperation(name,
                                               description,
                                               buildDataTypes(inputTypes),
                                               DataType.of(outputType, outputGenericTypes),
                                               MuleExtensionUtils.build(parameters));
    }

    private List<DataType> buildDataTypes(Map<Class<?>, Class<?>[]> types)
    {
        List<DataType> dataTypes = new ArrayList<>(types.size());
        for (Map.Entry<Class<?>, Class<?>[]> entry : types.entrySet())
        {
            dataTypes.add(DataType.of(entry.getKey(), entry.getValue()));
        }

        return dataTypes;
    }
}
