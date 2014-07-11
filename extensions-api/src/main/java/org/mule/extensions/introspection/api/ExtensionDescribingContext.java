/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

import java.util.Map;

/**
 * Used for propagating state across a {@link org.mule.extensions.introspection.api.ExtensionDescriber}
 * and its {@link org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor}s
 * <p/>
 * Once the {@link org.mule.extensions.introspection.api.ExtensionDescriber} finishes applying its logic,
 * it will propagate this context through all the found
 * {@link org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor}s, which means that any
 * side effects applied by any of the before mentioned will be visible by the next ones.
 *
 * @since 1.0
 */
public interface ExtensionDescribingContext
{

    /**
     * The {@link java.lang.Class} of the extension type being described
     */
    Class<?> getExtensionType();

    /**
     * The {@link org.mule.extensions.introspection.api.ExtensionBuilder} in which
     * the extension is being described into
     *
     * @return a non {@code null} {@link org.mule.extensions.introspection.api.ExtensionBuilder}
     */
    ExtensionBuilder getExtensionBuilder();

    /**
     * A {@link java.util.Map} to hold custom parameters that implementations of
     * {@link org.mule.extensions.introspection.api.ExtensionDescriber} and
     * {@link org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor} might
     * want to share with each other
     *
     * @return a non {@code null} map. Not be assumed thread-safe
     */
    Map<String, Object> getCustomParameters();
}
