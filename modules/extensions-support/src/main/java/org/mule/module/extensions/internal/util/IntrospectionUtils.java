/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.util;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.introspection.api.DataType;
import org.mule.module.extensions.internal.ImmutableDataType;
import org.mule.repackaged.internal.org.springframework.core.ResolvableType;
import org.mule.util.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Set of utility operations to get insights about objects and their operations
 *
 * @since 3.6.0
 */
public class IntrospectionUtils
{

    public static DataType getClassDataType(Class<?> clazz)
    {
        return toDataType(ResolvableType.forClass(clazz));
    }

    public static DataType getMethodReturnType(Method method)
    {
        checkArgument(method != null, "Can't introspect a null method");
        return toDataType(ResolvableType.forMethodReturnType(method));
    }

    public static DataType[] getMethodArgumentTypes(Method method)
    {
        checkArgument(method != null, "Can't introspect a null method");
        Class<?>[] parameters = method.getParameterTypes();
        if (ArrayUtils.isEmpty(parameters))
        {
            return new DataType[] {};
        }

        DataType[] types = new DataType[parameters.length];
        for (int i = 0; i < parameters.length; i++)
        {
            ResolvableType type = ResolvableType.forMethodParameter(method, i);
            types[i] = toDataType(type);
        }

        return types;
    }

    public static DataType getFieldDataType(Field field)
    {
        checkArgument(field != null, "Can't introspect a null field");
        return toDataType(ResolvableType.forField(field));
    }

    private static DataType toDataType(ResolvableType type)
    {
        return ImmutableDataType.of(type.getRawClass(), toRawTypes(type.getGenerics()));
    }

    private static Class<?>[] toRawTypes(ResolvableType[] resolvableTypes)
    {
        Class<?>[] types = new Class<?>[resolvableTypes.length];
        for (int i = 0; i < resolvableTypes.length; i++)
        {
            types[i] = resolvableTypes[i].getRawClass();
        }

        return types;
    }
}
