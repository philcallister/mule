/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.Preconditions.checkState;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleValueResolver extends ConfigurationValueResolver
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleValueResolver.class);

    private ValueResolver resolver;

    public ModuleValueResolver(String name, Class<?> moduleClass, ResolverSet resolverSet)
    {
        super(name);

        if (resolverSet.isDynamic())
        {
            // TODO: add resolver which caches based on ResolverSetResult instances
        }
        else
        {
            resolver = new ObjectBuilderValueResolver(resolverSet.toObjectBuilderOf(moduleClass));
        }
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        return resolver.resolve(event);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        injectMuleContextIfNeeded(resolver);
        LifecycleUtils.initialiseIfNeeded(resolver);

        if (!resolver.isDynamic())
        {
            resolver = new CachingValueResolverWrapper(resolver);
        }
    }

    @Override
    public boolean isDynamic()
    {
        checkState(resolver != null, "This resolver has yet not been initialised");
        return resolver.isDynamic();
    }

    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.applyPhaseIfNeeded(Startable.PHASE_NAME, resolver);
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.applyPhaseIfNeeded(Stoppable.PHASE_NAME, resolver);
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(LOGGER, resolver);
    }
}