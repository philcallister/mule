/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Stoppable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingValueResolverWrapper implements ValueResolver, Stoppable, Disposable
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingValueResolverWrapper.class);

    private final ValueResolver delegate;
    private Object value;

    public CachingValueResolverWrapper(ValueResolver delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        if (value == null)
        {
            value = delegate.resolve(event);
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic()
    {
        return false;
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.stopIfNeeded(value);
        LifecycleUtils.stopIfNeeded(delegate);
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(value, LOGGER);
        LifecycleUtils.disposeIfNeeded(delegate, LOGGER);
    }
}
