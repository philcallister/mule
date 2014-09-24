/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.ClassUtils.instanciateClass;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.repackaged.internal.org.springframework.util.ReflectionUtils;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Method;
import java.util.Map;

public class ResolverSetResult
{

    public static class Builder
    {

        private int hashCode = 1;
        private ImmutableMap.Builder<ExtensionParameter, Object> values = ImmutableMap.builder();

        private Builder()
        {
        }

        public Builder add(ExtensionParameter parameter, Object value)
        {
            values.put(parameter, value);
            hashCode = 31 * hashCode + (value == null ? 0 : value.hashCode());
            return this;
        }

        public ResolverSetResult build()
        {
            return new ResolverSetResult(values.build(), hashCode);
        }

    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    private final Map<ExtensionParameter, Object> evaluationResult;
    private final int hashCode;

    private ResolverSetResult(Map<ExtensionParameter, Object> evaluationResult, int hashCode)
    {
        this.evaluationResult = evaluationResult;
        this.hashCode = hashCode;
    }

    public Object get(ExtensionParameter parameter)
    {
        return evaluationResult.get(parameter);
    }

    public Map<ExtensionParameter, Object> getValues()
    {
        return evaluationResult;
    }

    public <T> T toInstanceOf(Class<T> prototypeClass) throws Exception
    {
        T object = instanciateClass(prototypeClass);

        for (Map.Entry<ExtensionParameter, Object> entry : evaluationResult.entrySet())
        {
            Method setter = IntrospectionUtils.getSetter(prototypeClass, entry.getKey());
            ReflectionUtils.invokeMethod(setter, object, entry.getValue());
        }

        return object;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ResolverSetResult)
        {
            ResolverSetResult other = (ResolverSetResult) obj;
            for (Map.Entry<ExtensionParameter, Object> entry : evaluationResult.entrySet())
            {
                Object otherValue = other.get(entry.getKey());
                if (!Objects.equal(entry.getValue(), otherValue))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }
}
