/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Lifecycle;

abstract class ConfigurationValueResolver implements ValueResolver, MuleContextAware, Lifecycle
{

    protected MuleContext muleContext;

    protected void injectMuleContext(Object configuration) {
        if (configuration instanceof MuleContextAware) {
            ((MuleContextAware) configuration).setMuleContext(muleContext);
        }
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
