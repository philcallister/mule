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
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.runtime.DefaultObjectBuilder;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;
import org.mule.module.extensions.internal.util.IntrospectionUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ValueResolver} which is based on associating a set of
 * {@link ExtensionParameter}s to a {@link ValueResolver}. The result
 * of evaluating this resolver is a {@link ResolverSetResult}.public static final
 * <p/>
 * The general purpose of this class is to repeatedly evaluate a set of {@link ValueResolver}s
 * which results are to be used in the construction of an object, so that the structure
 * of such can be described only once (by the set of {@link ExtensionParameter}s and {@link ValueResolver}s
 * but evaluated many times. With this goal in mind is that the return value of this resolver
 * will always be a {@link ResolverSetResult} and that this resolver includes the method
 * {@link #toObjectBuilderOf(Class)}, so that an {@link ObjectBuilder} can easily be derived from
 * this definition.
 * <p/>
 * Instances of this class are to be considered thread safe and reusable
 *
 * @since 3.7.0
 */
public class ResolverSet implements ValueResolver, Lifecycle, MuleContextAware
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverSet.class);

    private Map<ExtensionParameter, ValueResolver> resolvers = new LinkedHashMap<>();
    private boolean dynamic = false;
    private MuleContext muleContext;

    /**
     * Links the given {@link ValueResolver} to the given {@link ExtensionParameter}.
     * If such {@code parameter} was already added, then the associated {@code resolver}
     * is replaced.
     * <p/>
     * Since this class implements {@link Lifecycle} and {@link MuleContextAware},
     * this method works in tandem with the ones defined in that interface.
     * All the lifecycle invocations received by this instances are propagates to the
     * registered resolvers. You should make a best effort then to add all the resolvers
     * <b>before</b> lifecycle is applied. Otherwise, adding more resolvers will be allowed
     * but you'll be in charge of whatever lifecycle phases it has missed
     *
     * @param parameter a not {@code null} {@link ExtensionParameter}
     * @param resolver  a not {@code null} {@link ValueResolver}
     * @return this resolver set to allow chaining
     * @throws IllegalArgumentException is either {@code parameter} or {@code resolver} are {@code null}
     */
    public ResolverSet add(ExtensionParameter parameter, ValueResolver resolver)
    {
        checkArgument(parameter != null, "parameter cannot be null");
        checkArgument(resolver != null, "resolver cannot be null");

        resolvers.put(parameter, resolver);

        if (resolver.isDynamic())
        {
            dynamic = true;
        }
        return this;
    }

    /**
     * Whether at least one of the given {@link ValueResolver} are dynamic
     *
     * @return {@code true} if at least one resolver is dynamic. {@code false} otherwise
     */
    @Override
    public boolean isDynamic()
    {
        return dynamic;
    }

    /**
     * Evaluates all the added {@link ValueResolver}s and returns the results into
     * a {@link ResolverSetResult}
     *
     * @param event a not {@code null} {@link MuleEvent}
     * @return a {@link ResolverSetResult}
     * @throws Exception
     */
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

    /**
     * Returns a new instance of {@link ObjectBuilder} which builds instances
     * of {@code prototypeClass} using the {@link ValueResolver}s and
     * {@link ExtensionParameter}s configured into this set
     *
     * @param prototypeClass the class which instances you want to create
     * @return a new {@link ObjectBuilder}
     */
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

    /**
     * For each registered {@link ValueResolver}, orderly invokes
     * {@link MuleContextAware#setMuleContext(MuleContext)} and
     * {@link Initialisable#initialise()} if each implements either of
     * those interfaces
     *
     * @throws InitialisationException
     */
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

    /**
     * For each registered {@link ValueResolver}, it invokes
     * {@link Startable#start()} if each implements that interface
     *
     * @throws MuleException
     */
    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.startIfNeeded(resolvers.values());
    }

    /**
     * For each registered {@link ValueResolver}, it invokes
     * {@link Stoppable#stop()} if each implements that interface
     *
     * @throws MuleException
     */
    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.stopIfNeeded(resolvers.values());
    }

    /**
     * For each registered {@link ValueResolver}, it invokes
     * {@link Disposable#dispose()} if each implements that interface
     */
    @Override
    public void dispose()
    {
        LifecycleUtils.disposeAllIfNeeded(resolvers.values(), LOGGER);
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
