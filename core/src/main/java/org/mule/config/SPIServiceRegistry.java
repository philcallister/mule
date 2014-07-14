/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config;

import org.mule.api.config.ServiceRegistry;

import java.util.Iterator;

/**
 * Implementation of {@link org.mule.api.config.ServiceRegistry}
 * that uses standard {@link java.util.ServiceLoader} to get
 * the providers
 *
 * @since 3.6
 */
public class SPIServiceRegistry implements ServiceRegistry
{

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> lookupProviders(Class<T> providerClass, ClassLoader loader)
    {
        return javax.imageio.spi.ServiceRegistry.lookupProviders(providerClass, loader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Iterator<T> lookupProviders(Class<T> providerClass)
    {
        return javax.imageio.spi.ServiceRegistry.lookupProviders(providerClass);
    }
}
