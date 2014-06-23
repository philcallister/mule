/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionParameter;

import java.util.List;

/**
 * Immutable implementation of {@link org.mule.extensions.introspection.api.ExtensionConfiguration}
 *
 * @since 1.0
 */
final class ImmutableExtensionConfiguration extends AbstractImmutableDescribed implements ExtensionConfiguration
{

    private final List<ExtensionParameter> parameters;

    protected ImmutableExtensionConfiguration(String name, String description, List<ExtensionParameter> parameters)
    {
        super(name, description);
        MuleExtensionUtils.checkNullOrRepeatedNames(parameters, "parameters");
        this.parameters = MuleExtensionUtils.immutableList(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<ExtensionParameter> getParameters()
    {
        return parameters;
    }

}
