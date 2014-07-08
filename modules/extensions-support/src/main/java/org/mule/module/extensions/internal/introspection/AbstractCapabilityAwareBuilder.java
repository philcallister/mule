/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.introspection.api.Builder;
import org.mule.extensions.introspection.api.CapabilityAwareBuilder;

import java.util.HashSet;
import java.util.Set;

abstract class AbstractCapabilityAwareBuilder<T, B extends Builder<T>> implements CapabilityAwareBuilder<T, B>
{

    protected Set<Object> capabilities = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Object> B addCapablity(T capability)
    {
        checkArgument(capability != null, "capability cannot be null");
        capabilities.add(capability);

        return (B) this;
    }
}
