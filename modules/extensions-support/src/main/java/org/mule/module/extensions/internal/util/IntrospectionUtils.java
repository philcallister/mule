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
import org.mule.repackaged.internal.org.springframework.util.ReflectionUtils;
import org.mule.util.ArrayUtils;

import com.google.common.collect.ImmutableMap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Set of utility operations to get insights about objects and their operations
 *
 * @since 3.6.0
 */
public class IntrospectionUtils
{

    /**
     * Returns a {@link org.mule.extensions.introspection.api.DataType} representing the
     * given clazz
     */
    public static DataType getClassDataType(Class<?> clazz)
    {
        return toDataType(ResolvableType.forClass(clazz));
    }

    /**
     * Returns a {@link org.mule.extensions.introspection.api.DataType} representing
     * the given {@link java.lang.reflect.Method}'s return type
     *
     * @return a {@link org.mule.extensions.introspection.api.DataType}
     * @throws java.lang.IllegalArgumentException is method is {@code null}
     */
    public static DataType getMethodReturnType(Method method)
    {
        checkArgument(method != null, "Can't introspect a null method");
        return toDataType(ResolvableType.forMethodReturnType(method));
    }

    /**
     * Returns an array of {@link org.mule.extensions.introspection.api.DataType}
     * representing each of the given {@link java.lang.reflect.Method}'s argument
     * types.
     *
     * @param method a not {@code null} {@link java.lang.reflect.Method}
     * @return an array of {@link org.mule.extensions.introspection.api.DataType} matching
     * the method's arguments. If the method doesn't take any, then the array will be empty
     * @throws java.lang.IllegalArgumentException is method is {@code null}
     */
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

    /**
     * Returns a {@link org.mule.extensions.introspection.api.DataType} describing
     * the given {@link java.lang.reflect.Field}'s type
     *
     * @param field a not {@code null} {@link java.lang.reflect.Field}
     * @return a {@link org.mule.extensions.introspection.api.DataType} matching the field's type
     * @throws java.lang.IllegalArgumentException if field is {@code null}
     */
    public static DataType getFieldDataType(Field field)
    {
        checkArgument(field != null, "Can't introspect a null field");
        return toDataType(ResolvableType.forField(field));
    }

    /**
     * Uses {@link java.beans.Introspector} to extract the {@link java.lang.reflect.Field}s
     * from {@code clazz} that have getters and setters. Then returns a {@link java.util.Map}
     * in which those fields are keys and the values are {@link org.mule.extensions.introspection.api.DataType}
     * representing those field's types (using {@link #getFieldDataType(java.lang.reflect.Field)}.
     * <p/>
     * For this to work, {@code clazz} has to be introspector friendly.
     *
     * @param clazz a non {@code null} {@link java.lang.Class}
     * @return a {@link java.util.Map} with {@link java.lang.reflect.Field} as keys and {@link org.mule.extensions.introspection.api.DataType}
     * as values. It might be empty but it will never be {@code null}
     */
    public static Map<Field, DataType> getFieldsDataTypes(Class<?> clazz)
    {
        BeanInfo info;
        try
        {
            info = Introspector.getBeanInfo(clazz);
        }
        catch (IntrospectionException e)
        {
            throw new RuntimeException(String.format("Could not introspect class %s", clazz.getName()), e);
        }

        // use the property descriptors to only get the attributes compliant with the bean contract
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        if (ArrayUtils.isEmpty(descriptors))
        {
            return ImmutableMap.of();
        }

        ImmutableMap.Builder<Field, DataType> map = ImmutableMap.builder();

        // use the property descriptors to only get the attributes compliant with the bean contract
        for (PropertyDescriptor property : info.getPropertyDescriptors())
        {
            Field field = ReflectionUtils.findField(clazz, property.getName());
            if (field == null)
            {
                continue;
            }
            DataType fieldType = IntrospectionUtils.getFieldDataType(field);
            map.put(field, fieldType);
        }

        return map.build();
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
