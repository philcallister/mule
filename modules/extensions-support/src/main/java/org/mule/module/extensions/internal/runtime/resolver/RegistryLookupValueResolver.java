/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleEvent;

public class RegistryLookupValueResolver implements ValueResolver
{

    private final String key;

    public RegistryLookupValueResolver(String key)
    {
        this.key = key;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        return event.getMuleContext().getRegistry().get(key);
    }

}
