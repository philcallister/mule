/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CollectionValueResolver implements ValueResolver, Lifecycle
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<ValueResolver> resolvers;

    public static CollectionValueResolver of(Class<? extends Collection> collectionType, List<ValueResolver> resolvers)
    {
        return Set.class.isAssignableFrom(collectionType)
               ? new SetValueResolver(resolvers)
               : new ListValueResolver(resolvers);
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic()
    {
        for (ValueResolver resolver : resolvers)
        {
            if (resolver.isDynamic())
            {
                return true;
            }
        }
        return false;
    }

    protected abstract Collection<Object> instantiateCollection(int resolversCount);

    @Override
    public void initialise() throws InitialisationException
    {
        LifecycleUtils.initialiseIfNeeded(resolvers.toArray());
    }

    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.applyPhaseIfNeeded(Startable.PHASE_NAME, resolvers.toArray());
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.applyPhaseIfNeeded(Stoppable.PHASE_NAME, resolvers.toArray());
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(logger, resolvers.toArray());
    }

}
