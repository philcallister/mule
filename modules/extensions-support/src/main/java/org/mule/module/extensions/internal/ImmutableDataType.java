/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.util.ArrayUtils;
import org.mule.util.Preconditions;

import java.util.Arrays;
import java.util.Objects;

public final class ImmutableDataType implements DataType
{

    private final Class<?> type;
    private final DataType[] genericTypes;
    private final DataQualifier qualifier;

    public static DataType of(Class<?> clazz)
    {
        return of(clazz, (DataType[]) null);
    }

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

    @Override
    public String getName()
    {
        return type.getName();
    }

    public boolean isAssignableFrom(DataType dataType)
    {
        return type.isAssignableFrom(dataType.getType());
    }

    @Override
    public boolean isInstance(Object object)
    {
        return type.isInstance(object);
    }

    @Override
    public Class<?> getType()
    {
        return type;
    }

    @Override
    public DataType[] getGenericTypes()
    {
        return genericTypes;
    }

    @Override
    public DataQualifier getQualifier()
    {
        return qualifier;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataType)
        {
            DataType other = (DataType) obj;
            return type.equals(other.getType()) &&
                   Arrays.equals(genericTypes, other.getGenericTypes()) &&
                   qualifier.equals(other.getQualifier());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, genericTypes, qualifier);
    }
}
