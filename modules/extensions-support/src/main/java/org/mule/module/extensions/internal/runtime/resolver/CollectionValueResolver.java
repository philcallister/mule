/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

abstract class CollectionValueResolver implements ValueResolver
{

    private final List<ValueResolver> resolvers;

    public CollectionValueResolver(List<ValueResolver> resolvers)
    {
        checkArgument(resolvers != null, "resolvers cannot be null");
        this.resolvers = ImmutableList.copyOf(resolvers);
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        Collection<Object> collection = instantiateCollection(resolvers.size());
        for (ValueResolver resolver : resolvers)
        {
            collection.add(resolver.resolve(event));
        }

        return collection;
    }

    protected abstract Collection<Object> instantiateCollection(int resolversCount);

}
