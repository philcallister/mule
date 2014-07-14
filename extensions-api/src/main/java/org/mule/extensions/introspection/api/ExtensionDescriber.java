/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;


import org.mule.api.config.ServiceRegistryAware;

/**
 * This component takes a raw representation of a {@link org.mule.extensions.introspection.api.Extension}
 * and describes it into a context so that an actual instance of
 * {@link org.mule.extensions.introspection.api.ExtensionDescriber} can be created.
 * <p/>
 * To allow customization of the describing process, implementations will use standard SPI discovery mechanism
 * to locale registered instances of {@link org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor}
 * which are invoked in order. Discovery of the post processors is not to be done directly but through an implementation
 * of {@link org.mule.api.config.ServiceRegistry}
 *
 * @since 1.0
 */
public interface ExtensionDescriber extends ServiceRegistryAware
{

    /**
     * Performs the describing logic over the given {@code context} and invokes
     * the registered {@link org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor}s
     * afterwards
     *
     * @param context a valid {@link org.mule.extensions.introspection.api.ExtensionDescribingContext context}
     */
    void describe(ExtensionDescribingContext context);

}
