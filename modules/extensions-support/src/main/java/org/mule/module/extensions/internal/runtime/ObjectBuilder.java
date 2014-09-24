/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime;

import org.mule.api.MuleEvent;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;

import java.lang.reflect.Method;

/**
 * A builder capable of creating instances of a
 * given {@link Class}, populating its attributes
 * based on a list of {@link ValueResolver}s.
 * <p/>
 * The built object's class must comply with the following
 * requirements:
 * <p/>
 * <ul>
 * <li>It must be a concrete class
 * <li>It must have a public, default constructor</li>
 * <li>It must have a standard setter for each property that
 * this builder will be populating</li>
 * </ul>
 * <p/>
 * Instances of this class are to be considered thread safe and
 * reusable since the {@link #build(MuleEvent)} method can be invoked
 * several times on the same instance. Each time {@link #build(MuleEvent)}
 * is invoked, the resolvers will be re evaluated with the given event.
 *
 * @since 3.7.0
 */
public interface ObjectBuilder
{

    /**
     * Sets the {@link Class} of the object being built. The class
     * must have a default public constructor and publish
     * standard setters for each property to be populated
     *
     * @param prototypeClass a not {@code null} {@link Class}
     * @return this builder
     * @throws {@link IllegalArgumentException} if the class does not comply
     *                with the requirements
     */
    ObjectBuilder setPrototypeClass(Class<?> prototypeClass);

    /**
     * Adds a property to the builder.
     *
     * @param method   the setter to be used as an accessor
     * @param resolver a {@link ValueResolver} used to provide the actual value
     * @return this builder
     * @throws {@link java.lang.IllegalArgumentException} if method or resolver are {@code null}
     */
    ObjectBuilder addProperty(Method method, ValueResolver resolver);

    /**
     * Whether any of the registered {@link ValueResolver}s are dynamic
     *
     * @return {@code true} if at least one resolver is dynamic. {@code false} otherwise
     */
    boolean isDynamic();

    /**
     * Returns a new instance of the specified class.
     * The given {@link MuleEvent} will be used to obtain a
     * value from each registered {@link ValueResolver}
     *
     * @param event a {@link MuleEvent}
     * @return a new instance
     * @throws Exception
     */
    Object build(MuleEvent event) throws Exception;
}
