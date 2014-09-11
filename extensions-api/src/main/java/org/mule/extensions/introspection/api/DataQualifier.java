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
 * a {@link #POJO} refers to a pojo implementing the bean contract
 *
 * @since 1.0
 */
public enum DataQualifier
{

    /**
     * A void type. Means no value
     */
    VOID
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onVoid();
                }
            },

    /**
     * A boolean type.
     */
    BOOLEAN
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onBoolean();
                }
            },

    /**
     * A number with no decimal part
     */
    INTEGER
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onInteger();
                }
            },

    /**
     * A double precision number
     */
    DOUBLE
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onDouble();
                }
            },

    /**
     * A floating point number
     */
    DECIMAL
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onDecimal();
                }
            },

    /**
     * A text type
     */
    STRING
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onString();
                }
            },

    /**
     * A short number
     */
    SHORT
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onShort();
                }
            },

    /**
     * A long integer
     */
    LONG
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onLong();
                }
            },

    /**
     * A single byte
     */
    BYTE
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onByte();
                }
            },

    /**
     * A streaming, consumible type
     */
    STREAM
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onStream();
                }
            },

    /**
     * An {@link java.lang.Enum} type
     */
    ENUM
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onEnum();
                }
            },

    /**
     * A date type
     */
    DATE
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onDate();
                }
            },

    /**
     * A date with time
     */
    DATE_TIME
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onDateTime();
                }
            },

    /**
     * A pojo implementing the bean contract
     */
    POJO
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onPojo();
                }
            },

    /**
     * A java {@link java.util.Collection} type
     */
    LIST
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onList();
                }
            },

    /**
     * A java {@link java.util.Map}
     */
    MAP
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onMap();
                }
            },

    /**
     * A reference to another operation which will in turn
     * return another type. Consider this as a level of indirection
     */
    OPERATION
            {
                @Override
                public void accept(DataQualifierVisitor visitor)
                {
                    visitor.onOperation();
                }
            };

    public abstract void accept(DataQualifierVisitor visitor);
}
