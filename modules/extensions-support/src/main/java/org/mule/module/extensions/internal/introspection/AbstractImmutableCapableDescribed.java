/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.Capable;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Abstract implementation for a class that implements both
 * the {@link org.mule.extensions.introspection.api.Described} and
 * {@link org.mule.extensions.introspection.api.Capable} contracts
 *
 * @since 1.0
 */
abstract class AbstractImmutableCapableDescribed extends AbstractImmutableDescribed implements Capable
{

    private Set<Object> capabilities;

    AbstractImmutableCapableDescribed(String name, String description, Set<Object> capabilities) {
        super(name, description);
        this.capabilities = capabilities != null ? ImmutableSet.copyOf(capabilities) : ImmutableSet.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> getCapabilities(Class<T> capabilityType)
    {
        ImmutableSet.Builder<T> matches = ImmutableSet.builder();
        for (Object capability : capabilities)
        {
            if (capabilityType.isInstance(capability))
            {
                matches.add((T) capability);
            }
        }

        return matches.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCapableOf(Class<?> capabilityType)
    {
        return !getCapabilities(capabilityType).isEmpty();
    }
}
