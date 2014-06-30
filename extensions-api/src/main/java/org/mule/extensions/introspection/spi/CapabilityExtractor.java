/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.spi;

/**
 * A component capable of extracting one specific capability
 * out of a {@link java.lang.Class} that composes a {@link org.mule.extensions.introspection.api.Extension}
 * <p/>
 * Because actual capabilities might be defined across several modules (or even extensions!)
 * the actual extractors are fetched through SPI, using the standard {@link java.util.ServiceLoader}
 * contract.
 *
 * @since 1.0
 */
public interface CapabilityExtractor
{

    /**
     * Looks for a specific capability in the given {@code extensionType}.
     * Implementations must take into account that those capabilities might be
     * expressed in a variety of forms, being this method's responsibility to find them
     * all and return them in a consistent way that the rest of the platform can handle.
     * If the capability is not found, then this method should return {@code null}
     *
     * @param extensionType a type maybe holding a capability
     * @return a capability object or {@code null}
     */
    Object extractCapability(Class<?> extensionType);
}
