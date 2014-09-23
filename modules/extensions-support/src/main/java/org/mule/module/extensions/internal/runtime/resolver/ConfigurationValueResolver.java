/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.NamedObject;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.Startable;

abstract class ConfigurationValueResolver implements ValueResolver, MuleContextAware, Lifecycle, NamedObject
{

    private final String name;
    protected MuleContext muleContext;

    ConfigurationValueResolver(String name)
    {
        this.name = name;
    }

    protected void injectMuleContextIfNeeded(Object configuration)
    {
        if (configuration instanceof MuleContextAware)
        {
            ((MuleContextAware) configuration).setMuleContext(muleContext);
        }
    }

    protected void applyInitialLifeCycle(Object configuration) throws MuleException
    {
        injectMuleContextIfNeeded(configuration);
        if (configuration instanceof Initialisable)
        {
            ((Initialisable) configuration).initialise();
        }

        if (configuration instanceof Startable)
        {
            ((Startable) configuration).start();
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
