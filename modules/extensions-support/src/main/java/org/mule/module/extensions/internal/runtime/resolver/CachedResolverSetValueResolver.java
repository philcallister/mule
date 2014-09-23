/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Stoppable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedResolverSetValueResolver implements ValueResolver, Stoppable, Disposable
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedResolverSetValueResolver.class);

    private final Class<?> prototypeClass;
    private final ResolverSet resolverSet;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private final LoadingCache<ResolverSetResult, Object> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)  //TODO: externalize this? make configurable?
            .removalListener(new EvictionListener())
            .build(new CacheLoader<ResolverSetResult, Object>()
            {
                @Override
                public Object load(ResolverSetResult key) throws Exception
                {
                    return key.toInstanceOf(prototypeClass);
                }
            });

    public CachedResolverSetValueResolver(Class<?> prototypeClass, ResolverSet resolverSet)
    {
        this.prototypeClass = prototypeClass;
        this.resolverSet = resolverSet;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        if (stopped.get())
        {
            throw new IllegalStateException("Cannot resolve value since this resolver is stopped");
        }

        ResolverSetResult result = resolverSet.resolve(event);
        return cache.getUnchecked(result);
    }

    @Override
    public boolean isDynamic()
    {
        return resolverSet.isDynamic();
    }

    @Override
    public void stop() throws MuleException
    {
        stopped.set(true);
        LifecycleUtils.stopIfNeeded(cache.asMap().values(), LOGGER);
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(cache.asMap().values(), LOGGER);
    }

    private class EvictionListener implements RemovalListener<ResolverSetResult, Object>
    {

        @Override
        public void onRemoval(RemovalNotification<ResolverSetResult, Object> notification)
        {
            Object value = notification.getValue();
            if (value == null || stopped.get())
            {
                return;
            }

            if (value instanceof Stoppable)
            {
                try
                {
                    ((Stoppable) value).stop();
                }
                catch (MuleException e)
                {
                    LOGGER.error("Found exception trying to stop instance of " + value.getClass().getName(), e);
                }
            }

            LifecycleUtils.disposeIfNeeded(value, LOGGER);
        }
    }

}
