/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.MuleExtensionUtils;
import org.mule.util.Preconditions;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * Immutable concrete implementation of {@link org.mule.extensions.introspection.api.ExtensionOperation}
 *
 * @since 1.0
 */
final class ImmutableExtensionOperation extends AbstractImmutableDescribed implements ExtensionOperation
{

    private final List<DataType> inputTypes;
    private final DataType outputType;
    private final List<ExtensionParameter> parameters;

    ImmutableExtensionOperation(String name,
                                String description,
                                List<DataType> inputTypes,
                                DataType outputType,
                                List<ExtensionParameter> parameters)
    {
        super(name, description);

        Preconditions.checkArgument(!CollectionUtils.isEmpty(inputTypes), "Must provide at least one input type");
        Preconditions.checkState(outputType != null, "Must provide an output type");

        this.inputTypes = MuleExtensionUtils.immutableList(inputTypes);
        this.outputType = outputType;
        this.parameters = MuleExtensionUtils.immutableList(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ExtensionParameter> getParameters()
    {
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataType> getInputTypes()
    {
        return inputTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getOutputType()
    {
        return outputType;
    }
}
