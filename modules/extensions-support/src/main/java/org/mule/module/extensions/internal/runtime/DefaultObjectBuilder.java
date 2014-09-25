/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime;

import static org.mule.module.extensions.internal.util.IntrospectionUtils.checkInstantiable;
import static org.mule.util.ClassUtils.instanciateClass;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;
import org.mule.module.extensions.internal.util.MuleExtensionUtils;
import org.mule.repackaged.internal.org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ObjectBuilder}
 *
 * @since 3.7.0
 */
public class DefaultObjectBuilder implements ObjectBuilder, Lifecycle, MuleContextAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultObjectBuilder.class);

    private Class<?> prototypeClass;
    private final Map<Method, ValueResolver> properties = new HashMap<>();
    private MuleContext muleContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectBuilder setPrototypeClass(Class<?> prototypeClass)
    {
        checkInstantiable(prototypeClass);
        this.prototypeClass = prototypeClass;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectBuilder addProperty(Method method, ValueResolver resolver)
    {
        checkArgument(method != null, "method cannot be null");
        checkArgument(resolver != null, "resolver cannot be null");

        properties.put(method, resolver);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic()
    {
        return MuleExtensionUtils.hasAnyDynamic(properties.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object build(MuleEvent event) throws Exception
    {
        Object object = instanciateClass(prototypeClass);

        for (Map.Entry<Method, ValueResolver> entry : properties.entrySet())
        {
            ReflectionUtils.invokeMethod(entry.getKey(), object, entry.getValue().resolve(event));
        }

        return object;
    }

    /**
     * For each registered {@link ValueResolver}, it propagates
     * the {link #muleContext} if it implements the {@link MuleContextAware}
     * interface and invokes {@link Initialisable#initialise()} if that
     * interface is also implemented by the resolver
     */
    @Override
    public void initialise() throws InitialisationException
    {
        for (ValueResolver resolver : properties.values())
        {
            if (resolver instanceof MuleContextAware)
            {
                ((MuleContextAware) resolver).setMuleContext(muleContext);
            }
        }

        LifecycleUtils.initialiseIfNeeded(properties.values());
    }

    /**
     * For each registered {@link ValueResolver} it invokes
     * {@link Startable#start()} if the resolver implements that interface
     */
    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.startIfNeeded(properties.values());
    }

    /**
     * For each registered {@link ValueResolver} the
     * {@link Stoppable#stop()} method is invoked
     * if the resolver implements such interface
     */
    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.stopIfNeeded(properties.values());
    }

    /**
     * For each registered {@link ValueResolver} the
     * {@link Disposable#dispose()} method is invoked
     * if the resolver implements such interface
     */
    @Override
    public void dispose()
    {
        LifecycleUtils.disposeAllIfNeeded(properties.values(), LOGGER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
