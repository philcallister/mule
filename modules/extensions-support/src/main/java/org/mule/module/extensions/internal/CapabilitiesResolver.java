/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.introspection.api.CapabilityAwareBuilder;
import org.mule.extensions.introspection.spi.CapabilityExtractor;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;

public final class CapabilitiesResolver
{

    private static List<CapabilityExtractor> extractors;

    synchronized static CapabilitiesResolver newInstance()
    {
        if (extractors == null)
        {
            Iterator<CapabilityExtractor> it = ServiceRegistry.lookupProviders(CapabilityExtractor.class);
            extractors = ImmutableList.copyOf(it);
        }

        return new CapabilitiesResolver();
    }

    private CapabilitiesResolver()
    {
    }

    public void resolveCapabilities(Class<?> extensionType, CapabilityAwareBuilder<?, ?> builder)
    {
        checkArgument(extensionType != null, "extensionType cannot be null");
        checkArgument(builder != null, "builder cannot be null");

        for (CapabilityExtractor extractor : extractors)
        {
            Object capability = extractor.extractCapability(extensionType);
            if (capability != null)
            {
                builder.addCapablity(capability);
            }
        }
    }
}
