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
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;

public class ModuleValueResolver extends ConfigurationValueResolver
{

    private final ResolverSet resolverSet;
    private ValueResolver resolver;

    public ModuleValueResolver(String name, Class<?> moduleClass, ResolverSet resolverSet)
    {
        super(name);
        this.resolverSet = resolverSet;

        if (resolverSet.isDynamic())
        {
            resolver = new CachedResolverSetValueResolver(moduleClass, resolverSet);
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
    public boolean isDynamic()
    {
        return resolver.isDynamic();
    }

    @Override
    public void initialise() throws InitialisationException
    {
        injectMuleContextIfNeeded(resolverSet);
        injectMuleContextIfNeeded(resolver);
        resolverSet.initialise();

        if (resolver instanceof Initialisable)
        {
            ((Initialisable) resolver).initialise();
        }
    }

    @Override
    public void start() throws MuleException
    {
        resolverSet.start();

        if (resolver instanceof Startable)
        {
            ((Startable) resolver).start();
        }

        resolver = new InitialLifecycleValueResolver(resolver);

        if (!resolver.isDynamic())
        {
            resolver = new CachingValueResolverWrapper(resolver);
        }
    }

    @Override
    public void stop() throws MuleException
    {
        if (resolver instanceof Stoppable)
        {
            ((Stoppable) resolver).stop();
        }

        resolverSet.stop();
    }

    @Override
    public void dispose()
    {
        if (resolver instanceof Disposable)
        {
            ((Disposable) resolver).dispose();
        }

        resolverSet.dispose();
    }
}