/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

/**
 * A definition of an abstract data type, which provides information
 * that goes beyond it's actual {@link java.lang.reflect.Type}, but also
 * provides information about its parametrized generic types and
 * {@link org.mule.extensions.introspection.api.DataQualifier}
 *
 * @since 1.0
 */
public interface DataType
{

    /**
     * Returns the type's name
     *
     * @return a not {@code null} {@link java.lang.String}
     */
    String getName();

    /**
     * Returns the {@link java.lang.Class} for the type described by this instance
     *
     * @return a not {@code null} {@link java.lang.Class}
     */
    Class<?> getRawType();

    /**
     * An array of nested {@link org.mule.extensions.introspection.api.DataType}s which represent the
     * parametrized types for the type returned by {@link #getRawType()}
     *
     * @return an array of {@link org.mule.extensions.introspection.api.DataType}. It might be empty but it will not be
     * {@code null}
     */
    DataType[] getGenericTypes();

    /**
     * A qualifier for the represented type
     *
     * @return a {@link org.mule.extensions.introspection.api.DataQualifier}
     */
    DataQualifier getQualifier();

    DataType getSuperclass();
}
