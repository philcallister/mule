/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.api.config;

/**
 * Objects that need a {@link org.mule.api.config.ServiceRegistry} should implement
 * this interface in order to be assigned one by the platform
 *
 * @since 1.0
 */
public interface ServiceRegistryAware
{

    /**
     * sets a {@link org.mule.api.config.ServiceRegistry}
     *
     * @param serviceRegistry a service registry
     * @throws java.lang.IllegalArgumentException if {@code serviceRegistry} is {@code null}
     */
    void setServiceRegistry(ServiceRegistry serviceRegistry);
}
