/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.extensions.introspection.api.ExtensionParameter;

import com.google.common.base.Objects;

import java.util.Map;

public class ResolverSetResult
{

    private Map<ExtensionParameter, Object> evaluationResult;

    public ResolverSetResult(Map<ExtensionParameter, Object> evaluationResult)
    {
        this.evaluationResult = evaluationResult;
    }

    public Object get(ExtensionParameter parameter)
    {
        return evaluationResult.get(parameter);
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
        int result = 1;

        for (Object value : evaluationResult.values())
        {
            result = 31 * result + (value == null ? 0 : value.hashCode());
        }

        return result;
    }
}
