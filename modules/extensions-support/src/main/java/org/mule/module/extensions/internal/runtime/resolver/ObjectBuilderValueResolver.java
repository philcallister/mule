/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleEvent;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;

public class ObjectBuilderValueResolver implements ValueResolver
{

    private final ObjectBuilder builder;

    public ObjectBuilderValueResolver(ObjectBuilder builder)
    {
        this.builder = builder;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        return builder.build(event);
    }
}
