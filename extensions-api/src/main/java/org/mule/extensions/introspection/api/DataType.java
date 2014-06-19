/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

public final class DataType
{

    private final Class<?> type;
    private final Class<?>[] genericTypes;
    private final DataQualifier qualifier;

    public static DataType of(Class<?> clazz)
    {
        return of(clazz, null);
    }

    public static DataType of(Class<?> clazz, Class<?>... genericTypes)
    {
        return new DataType(clazz, genericTypes, DataQualifierFactory.getQualifier(clazz));
    }

    private DataType(Class<?> type, Class<?>[] genericTypes, DataQualifier qualifier)
    {
        this.type = type;
        this.genericTypes = genericTypes;
        this.qualifier = qualifier;
    }

    public String getName()
    {
        return type.getName();
    }

    public boolean isAssignableFrom(DataType dataType)
    {
        return type.isAssignableFrom(dataType.getType());
    }

    public boolean isInstance(Object object)
    {
        return type.isInstance(object);
    }

    public Class<?> getType()
    {
        return type;
    }

    public Class<?>[] getGenericTypes()
    {
        return genericTypes;
    }

    public DataQualifier getQualifier()
    {
        return qualifier;
    }
}
