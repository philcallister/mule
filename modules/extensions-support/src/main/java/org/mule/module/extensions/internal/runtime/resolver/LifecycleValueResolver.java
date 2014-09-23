/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleEvent;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.Startable;

public class LifecycleValueResolver implements ValueResolver
{

    private final ValueResolver delegate;

    public LifecycleValueResolver(ValueResolver delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        Object resolved = delegate.resolve(event);

        if (resolved instanceof MuleContextAware)
        {
            ((MuleContextAware) resolved).setMuleContext(event.getMuleContext());
        }

        if (resolved instanceof Initialisable)
        {
            ((Initialisable) resolved).initialise();
        }

        if (resolved instanceof Startable)
        {
            ((Startable) resolved).start();
        }

        return resolved;
    }

    @Override
    public boolean isDynamic()
    {
        return delegate.isDynamic();
    }
}
