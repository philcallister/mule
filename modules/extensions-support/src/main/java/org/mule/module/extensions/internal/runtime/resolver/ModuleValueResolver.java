/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;

public class ModuleValueResolver extends ConfigurationValueResolver
{

    private final Object configuration;

    public ModuleValueResolver(Object configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        return configuration;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        if (configuration instanceof Initialisable)
        {
            ((Initialisable) configuration).initialise();
        }
    }

    @Override
    public void start() throws MuleException
    {
        if (configuration instanceof Startable)
        {
            ((Startable) configuration).start();
        }
    }

    @Override
    public void stop() throws MuleException
    {
        if (configuration instanceof Stoppable)
        {
            ((Stoppable) configuration).stop();
        }
    }

    @Override
    public void dispose()
    {
        if (configuration instanceof Disposable)
        {
            ((Disposable) configuration).dispose();
        }
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        super.setMuleContext(context);
        injectMuleContext(configuration);
    }
}
