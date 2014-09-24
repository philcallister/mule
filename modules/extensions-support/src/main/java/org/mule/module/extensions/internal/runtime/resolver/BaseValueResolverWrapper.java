/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Startable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseValueResolverWrapper implements ValueResolver, Lifecycle, MuleContextAware
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected ValueResolver delegate;
    protected MuleContext muleContext;

    BaseValueResolverWrapper(ValueResolver delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isDynamic()
    {
        return delegate.isDynamic();
    }

    @Override
    public void initialise() throws InitialisationException
    {
        if (delegate instanceof MuleContextAware)
        {
            ((MuleContextAware) delegate).setMuleContext(muleContext);
        }
    }

    @Override
    public void start() throws MuleException
    {
        if (delegate instanceof Startable)
        {
            ((Startable) delegate).start();
        }
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.stopIfNeeded(delegate);
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(delegate, logger);
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
