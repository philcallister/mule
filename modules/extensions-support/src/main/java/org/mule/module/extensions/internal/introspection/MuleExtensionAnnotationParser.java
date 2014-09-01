/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.util.Preconditions.checkState;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Extension;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.api.annotation.param.Payload;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.util.ClassUtils;
import org.mule.util.ParamReader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

final class MuleExtensionAnnotationParser
{

    private static final Set<Class<?>> notParametrizableTypes = ImmutableSet.<Class<?>>builder()
            .add(MuleEvent.class)
            .add(MuleMessage.class)
            .build();

    static Extension getExtension(Class<?> extensionType)
    {
        Extension extension = extensionType.getAnnotation(Extension.class);
        checkState(extension != null, String.format("%s is not a Mule extension since it's not annotated with %s",
                                                                  extensionType.getName(), Extension.class.getName()));

        return extension;
    }

    static Collection<Field> getConfigurableFields(Class<?> extensionType)
    {
        List<Field> fields = ClassUtils.getDeclaredFields(extensionType, true);
        return CollectionUtils.select(fields, new Predicate()
        {
            @Override
            public boolean evaluate(Object object)
            {
                return ((Field) object).getAnnotation(Configurable.class) != null;
            }
        });
    }


    public static List<ParameterDescriptor> parseParameter(Method method)
    {
        String[] paramNames = getParamNames(method);

        if (ArrayUtils.isEmpty(paramNames))
        {
            return ImmutableList.of();
        }

        DataType[] parameterTypes = IntrospectionUtils.getMethodArgumentTypes(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        List<ParameterDescriptor> parameters = new ArrayList<>(paramNames.length);

        for (int i = 0; i < paramNames.length; i++)
        {
            if (!isParametrizable(parameterTypes[i], parameterAnnotations[i]))
            {
                continue;
            }

            DataType dataType = adaptType(parameterTypes[i]);

            ParameterDescriptor parameter = new ParameterDescriptor();
            parameter.setName(paramNames[i]);
            parameter.setType(dataType);

            Map<Class<? extends Annotation>, Annotation> annotations = parseAnnotations(parameterAnnotations[i]);

            Optional optional = (Optional) annotations.get(Optional.class);
            if (optional != null)
            {
                parameter.setRequired(false);
                parameter.setDefaultValue(getDefaultValue(optional, dataType));
            }
            else
            {
                parameter.setRequired(true);
            }

            parameters.add(parameter);
        }

        return parameters;
    }

    protected static Object getDefaultValue(Optional optional, DataType dataType)
    {
        if (optional == null)
        {
            return null;
        }

        String defaultValue = optional.defaultValue();
        if (DataQualifier.STRING.equals(dataType.getQualifier()))
        {
            return defaultValue;
        }
        else
        {
            return StringUtils.isEmpty(defaultValue) ? null : defaultValue;
        }
    }

    private static boolean isParametrizable(DataType type, Annotation[] annotations)
    {
        if (notParametrizableTypes.contains(type.getRawType()))
        {
            return false;
        }

        return !contains(annotations, Payload.class);
    }

    private static DataType adaptType(DataType type)
    {
        if (NestedProcessor.class.equals(type.getRawType()))
        {
            return ImmutableDataType.of(ExtensionOperation.class);
        }

        return type;
    }


    private static <T extends Annotation> boolean contains(Annotation[] annotations, Class<T> annotationType)
    {
        for (Annotation annotation : annotations)
        {
            if (annotationType.isInstance(annotation))
            {
                return true;
            }
        }

        return false;
    }

    private static String[] getParamNames(Method method)
    {
        String[] paramNames;
        try
        {
            paramNames = new ParamReader(method.getDeclaringClass()).getParameterNames(method);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(
                    String.format("Could not read parameter names from method %s of class %s", method.getName(), method.getDeclaringClass().getName())
                    , e);
        }

        return paramNames;
    }

    private static Map<Class<? extends Annotation>, Annotation> parseAnnotations(Annotation[] annotations)
    {

        Map<Class<? extends Annotation>, Annotation> map = new HashMap<>();

        for (Annotation annotation : annotations)
        {
            map.put(resolveAnnotationClass(annotation), annotation);
        }

        return map;
    }

    private static Class<? extends Annotation> resolveAnnotationClass(Annotation annotation)
    {
        if (Proxy.isProxyClass(annotation.getClass()))
        {
            return (Class<Annotation>) annotation.getClass().getInterfaces()[0];
        }
        else
        {
            return annotation.getClass();
        }
    }


}
