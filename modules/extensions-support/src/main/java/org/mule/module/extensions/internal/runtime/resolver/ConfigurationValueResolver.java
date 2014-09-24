/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleContext;
import org.mule.api.NamedObject;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.extensions.introspection.api.ExtensionConfiguration;

/**
 * Base class for {@link ValueResolver} instances which
 * return extension's configurations (not refering to
 * instances of {@link ExtensionConfiguration} but an actual
 * configuration instance
 *
 * @since 3.7.0
 */
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
