/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.module.extensions.internal.util.IntrospectionUtils.checkInstantiable;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.checkNullOrRepeatedNames;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.checkSetters;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.util.MuleExtensionUtils;

import java.util.List;

/**
 * Immutable implementation of {@link org.mule.extensions.introspection.api.ExtensionConfiguration}
 *
 * @since 1.0
 */
final class ImmutableExtensionConfiguration extends AbstractImmutableDescribed implements ExtensionConfiguration
{

    private final List<ExtensionParameter> parameters;
    private final Class<?> declaringClass;

    protected ImmutableExtensionConfiguration(String name,
                                              String description,
                                              Class<?> declaringClass,
                                              List<ExtensionParameter> parameters)
    {
        super(name, description);
        checkInstantiable(declaringClass);
        checkNullOrRepeatedNames(parameters, "parameters");

        this.parameters = MuleExtensionUtils.immutableList(parameters);
        checkSetters(declaringClass, this.parameters);
        this.declaringClass = declaringClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<ExtensionParameter> getParameters()
    {
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass()
    {
        return this.declaringClass;
    }
}
