/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

/**
 * Provides a high level definition about the &quot;family&quot;
 * a given {@link org.mule.extensions.introspection.api.DataType}
 * belongs to. For example, the {@link #STREAM} qualifier denotes a
 * type used for streaming, no matter if it's an {@link java.io.InputStream},
 * a {@link java.io.Reader} or whatever type used for that purpose.
 * At the same time, a {@link #DECIMAL} referes to a floating point numeric type and
 * a {@link #BEAN} refers to a pojo implementing the bean contract
 *
 * @since 1.0
 */
public enum DataQualifier
{

    /**
     * A void type. Means no value
     */
    VOID,

    /**
     * A boolean type.
     */
    BOOLEAN,

    /**
     * A number with no decimal part
     */
    INTEGER,

    /**
     * A double precision number
     */
    DOUBLE,

    /**
     * A floating point number
     */
    DECIMAL,

    /**
     * A text type
     */
    STRING,

    /**
     * A short number
     */
    SHORT,

    /**
     * A long integer
     */
    LONG,

    /**
     * A single byte
     */
    BYTE,

    /**
     * A streaming, consumible type
     */
    STREAM,

    /**
     * An {@link java.lang.Enum} type
     */
    ENUM,

    /**
     * A date type
     */
    DATE,

    /**
     * A date with time
     */
    DATE_TIME,

    /**
     * A pojo implementing the bean contract
     */
    BEAN,

    /**
     * A java {@link java.util.Collection} type
     */
    LIST,

    /**
     * A java {@link java.util.Map}
     */
    MAP,

    /**
     * A reference to another operation which will in turn
     * return another type. Consider this as a level of indirection
     */
    OPERATION;
}
