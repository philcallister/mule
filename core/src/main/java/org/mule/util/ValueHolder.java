/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.util;

/**
 * Utility class that holds a given value, allowing to set/retrieve it.
 *
 * @since 3.7.0
 */
public class ValueHolder<T>
{

    private T value;

    public ValueHolder()
    {
    }

    public ValueHolder(T value)
    {
        this();
        set(value);
    }

    public T get()
    {
        return value;
    }

    public void set(T value)
    {
        this.value = value;
    }
}
