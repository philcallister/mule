/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionDescribingContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable implementation of {@link org.mule.extensions.introspection.api.ExtensionDescribingContext}.
 * The fact that this class's attributes are immutable, doesn't mean that their inner state
 * is in fact immutable also.
 *
 * @since 3.7.0
 */
public final class ImmutableExtensionDescribingContext implements ExtensionDescribingContext
{

    private final Class<?> extensionType;
    private final ExtensionBuilder builder;
    private final Map<String, Object> customParameters = new HashMap<>();

    public ImmutableExtensionDescribingContext(Class<?> extensionType, ExtensionBuilder builder)
    {
        this.extensionType = extensionType;
        this.builder = builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getExtensionType()
    {
        return extensionType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder getExtensionBuilder()
    {
        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getCustomParameters()
    {
        return customParameters;
    }
}
