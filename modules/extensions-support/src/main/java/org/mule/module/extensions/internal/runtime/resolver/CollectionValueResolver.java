/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.module.extensions.internal.util.MuleExtensionUtils;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CollectionValueResolver implements ValueResolver, Lifecycle, MuleContextAware
{

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final List<ValueResolver> resolvers;
    private MuleContext muleContext;

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
        return MuleExtensionUtils.hasAnyDynamic(resolvers);
    }

    protected abstract Collection<Object> instantiateCollection(int resolversCount);

    @Override
    public void initialise() throws InitialisationException
    {
        for (ValueResolver resolver : resolvers)
        {
            if (resolver instanceof MuleContextAware)
            {
                ((MuleContextAware) resolver).setMuleContext(muleContext);
            }
        }

        LifecycleUtils.initialiseIfNeeded(resolvers);
    }

    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.startIfNeeded(resolvers);
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.stopIfNeeded(resolvers);
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeAllIfNeeded(resolvers, LOGGER);
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
