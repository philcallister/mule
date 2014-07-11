/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.spi;

import org.mule.extensions.introspection.api.ExtensionDescribingContext;

/**
 * A post processor that allows doing extra task before a
 * {@link org.mule.extensions.introspection.api.ExtensionDescriber} finishes
 * describing a {@link org.mule.extensions.introspection.api.Extension}
 * <p/>
 * This allows to customize the discovery process by given a hooking point
 * for manipulating the {@link org.mule.extensions.introspection.api.ExtensionBuilder} before
 * it actually generates the final {@link org.mule.extensions.introspection.api.Extension} instance.
 * <p/>
 * Instances are to be discovered by standard SPI mechanism
 *
 * @since 1.0
 */
public interface ExtensionDescriberPostProcessor
{

    /**
     * Optionally applies extra logic into any of the {@code context}'s
     * attributes
     *
     * @param context the current {@link org.mule.extensions.introspection.api.ExtensionDescribingContext}
     */
    void postProcess(ExtensionDescribingContext context);

}
