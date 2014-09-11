/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.util.ArrayUtils;
import org.mule.util.Preconditions;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable implementation of {@link org.mule.extensions.introspection.api.DataType}
 *
 * @since 3.7.0
 */
public final class ImmutableDataType implements DataType
{

    private final Class<?> type;
    private final DataType[] genericTypes;
    private final DataQualifier qualifier;

    /**
     * Returns a new {@link org.mule.extensions.introspection.api.DataType} that
     * represents the given type. The returned instance will return an empty array
     * when queried for {@link #getGenericTypes()}
     *
     * @param clazz a not {@code null} {@link java.lang.Class}
     * @return a new {@link ImmutableDataType}
     * @throws java.lang.IllegalArgumentException if the argument is null
     */
    public static DataType of(Class<?> clazz)
    {
        return of(clazz, (DataType[]) null);
    }

    /**
     * Returns a new {@link org.mule.extensions.introspection.api.DataType} that
     * represents the given type with the optional generic types.
     *
     * @param clazz        a not {@code null} {@link java.lang.Class}
     * @param genericTypes an optional array of generic types accessible through {@link #getGenericTypes()}
     * @return a new {@link ImmutableDataType}
     * @throws java.lang.IllegalArgumentException if the argument is null
     */
    public static DataType of(Class<?> clazz, Class<?>... genericTypes)
    {
        DataType[] types;
        if (ArrayUtils.isEmpty(genericTypes))
        {
            types = new DataType[] {};
        }
        else
        {
            types = new DataType[genericTypes.length];
            for (int i = 0; i < genericTypes.length; i++)
            {
                types[i] = of(genericTypes[i]);
            }
        }

        return of(clazz, types);
    }

    /**
     * Returns a new {@link org.mule.extensions.introspection.api.DataType} that
     * represents the given class and has the already provided {@link org.mule.extensions.introspection.api.DataType}s
     * as {@link #getGenericTypes()}
     *
     * @param clazz        a not {@code null} {@link java.lang.Class}
     * @param genericTypes an optional array of {@link org.mule.extensions.introspection.api.DataType} types accessible through {@link #getGenericTypes()}
     * @return a new {@link ImmutableDataType}
     * @throws java.lang.IllegalArgumentException if the argument is null
     */
    public static DataType of(Class<?> clazz, DataType... genericTypes)
    {
        if (genericTypes == null)
        {
            genericTypes = new DataType[] {};
        }

        return new ImmutableDataType(clazz, genericTypes, DataQualifierFactory.getQualifier(clazz));
    }

    private ImmutableDataType(Class<?> type, DataType[] genericTypes, DataQualifier qualifier)
    {
        Preconditions.checkArgument(type != null, "Can't build a DataType for a null type");
        this.type = type;
        this.genericTypes = genericTypes;
        this.qualifier = qualifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return type.getName();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAssignableFrom(DataType dataType)
    {
        return type.isAssignableFrom(dataType.getRawType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getRawType()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType[] getGenericTypes()
    {
        return genericTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataQualifier getQualifier()
    {
        return qualifier;
    }

    @Override
    public DataType getSuperclass()
    {
        if (Object.class.equals(type) || Object.class.equals(type.getSuperclass())) {
            return null;
        }

        return ImmutableDataType.of(type.getSuperclass());
    }

    /**
     * Defines equality by checking that the given object is a
     * {@link org.mule.extensions.introspection.api.DataType} with matching
     * {@link #getRawType()} and {@link #getQualifier()}, which also
     * returns a {@link #getGenericTypes()} which every element (if any) also matches
     * the one in this instance
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataType)
        {
            DataType other = (DataType) obj;
            return type.equals(other.getRawType()) &&
                   Arrays.equals(genericTypes, other.getGenericTypes()) &&
                   qualifier.equals(other.getQualifier());
        }

        return false;
    }

    /**
     * Calculates this instance's hash code by considering
     * the {@link #getRawType()}, {@link #getQualifier()} and the individual
     * hashCode of each element in {@link #getGenericTypes()}. If the generic types
     * array is empty, then it's not considered.
     */
    @Override
    public int hashCode()
    {
        int genericTypesHash = Arrays.hashCode(genericTypes);

        if (genericTypesHash == 0)
        {
            genericTypesHash = 1; //neutralize factor
        }
        return genericTypesHash * Objects.hash(type, qualifier) * 31;
    }
}
