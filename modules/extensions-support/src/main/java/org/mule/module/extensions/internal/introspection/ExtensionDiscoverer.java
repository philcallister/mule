/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionDescriber;

import java.util.List;

/**
 * A component capable of searching the classpath for extensions
 * according to the algorithm described in {@link org.mule.extensions.api.ExtensionsManager#discoverExtensions(ClassLoader)}
 *
 * @since 3.7.0
 */
public interface ExtensionDiscoverer
{

    /**
     * Performs a search for extensions
     * according to the algorithm described in {@link org.mule.extensions.api.ExtensionsManager#discoverExtensions(ClassLoader)}
     *
     * @param classLoader the {@link java.lang.ClassLoader} in which path perform the search
     * @param describer   a not {@code null} {@link org.mule.extensions.introspection.api.ExtensionDescriber} to introspect the
     *                    extension types
     * @return a {@link java.util.List} of {@link org.mule.extensions.introspection.api.Extension}. Might be empty
     * but it will never be {@code null}
     */
    List<Extension> discover(ClassLoader classLoader, ExtensionDescriber describer);
}
