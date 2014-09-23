/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.runtime.DefaultObjectBuilder;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;
import org.mule.module.extensions.internal.util.IntrospectionUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolverSet implements ValueResolver, Lifecycle, MuleContextAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverSet.class);

    private Map<ExtensionParameter, ValueResolver> resolvers = new LinkedHashMap<>();
    private boolean dynamic = false;
    private MuleContext muleContext;

    public ResolverSet add(ExtensionParameter parameter, ValueResolver resolver)
    {
        resolvers.put(parameter, resolver);

        if (resolver.isDynamic())
        {
            dynamic = true;
        }
        return this;
    }

    @Override
    public boolean isDynamic()
    {
        return dynamic;
    }

    @Override
    public ResolverSetResult resolve(MuleEvent event) throws Exception
    {
        ResolverSetResult.Builder builder = ResolverSetResult.newBuilder();
        for (Map.Entry<ExtensionParameter, ValueResolver> entry : resolvers.entrySet())
        {
            builder.add(entry.getKey(), entry.getValue().resolve(event));
        }

        return builder.build();
    }

    public ObjectBuilder toObjectBuilderOf(Class<?> prototypeClass)
    {
        ObjectBuilder builder = new DefaultObjectBuilder();
        builder.setPrototypeClass(prototypeClass);

        for (Map.Entry<ExtensionParameter, ValueResolver> entry : resolvers.entrySet())
        {
            Method setter = IntrospectionUtils.getSetter(prototypeClass, entry.getKey());
            builder.addProperty(setter, entry.getValue());
        }

        return builder;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        for (ValueResolver resolver : resolvers.values())
        {
            if (resolver instanceof MuleContextAware)
            {
                ((MuleContextAware) resolver).setMuleContext(muleContext);
            }
        }
        LifecycleUtils.initialiseIfNeeded(resolvers.values());
    }

    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.startIfNeeded(resolvers.values());
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.stopIfNeeded(resolvers.values());
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(resolvers.values(), LOGGER);
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
