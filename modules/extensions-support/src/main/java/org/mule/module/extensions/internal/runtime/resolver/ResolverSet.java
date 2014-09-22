/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleEvent;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.runtime.DefaultObjectBuilder;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResolverSet implements ValueResolver
{

    private Map<ExtensionParameter, ValueResolver> resolvers = new LinkedHashMap<>();
    private boolean dynamic = false;

    public ResolverSet add(ExtensionParameter parameter, ValueResolver resolver)
    {
        resolvers.put(parameter, resolver);

        if (resolver.isDynamic())
        {
            dynamic = true;
        }
        return this;
    }

    @Override
    public boolean isDynamic()
    {
        return dynamic;
    }

    @Override
    public ResolverSetResult resolve(MuleEvent event) throws Exception
    {
        Map<ExtensionParameter, Object> result = new HashMap<>(resolvers.size());
        for (Map.Entry<ExtensionParameter, ValueResolver> entry : resolvers.entrySet())
        {
            result.put(entry.getKey(), entry.getValue().resolve(event));
        }

        return new ResolverSetResult(result);
    }

    public ObjectBuilder toObjectBuilderOf(Class<?> prototypeClass)
    {
        ObjectBuilder builder = new DefaultObjectBuilder();
        builder.setPrototypeClass(prototypeClass);

        for (Map.Entry<ExtensionParameter, ValueResolver> entry : resolvers.entrySet())
        {
            builder.addProperty(entry.getKey(), entry.getValue());
        }

        return builder;
    }

}
