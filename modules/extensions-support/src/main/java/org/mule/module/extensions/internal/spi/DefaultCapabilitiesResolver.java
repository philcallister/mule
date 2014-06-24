/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.spi;

import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.spi.CapabilityAwareBuilder;
import org.mule.extensions.spi.CapabilitiesResolver;
import org.mule.module.extensions.internal.capability.xml.ImmutableXmlCapability;
import org.mule.util.Preconditions;

import java.util.Arrays;
import java.util.List;

final class DefaultCapabilitiesResolver implements CapabilitiesResolver
{

    private static abstract class CapabilityResolver
    {

        private void resolve(Class<?> extensionType, CapabilityAwareBuilder<?, ?> builder)
        {
            Object capability = extractCapability(extensionType);
            if (capability != null)
            {
                builder.addCapablity(capability);
            }
        }

        abstract Object extractCapability(Class<?> extensionType);

    }

    private static final CapabilityResolver xmlResolver = new CapabilityResolver()
    {
        @Override
        Object extractCapability(Class<?> extensionType)
        {
            Xml xml = extensionType.getAnnotation(Xml.class);
            if (xml != null)
            {
                return new ImmutableXmlCapability(xml.schemaVersion(), xml.namespace(), xml.schemaLocation());
            }

            return null;
        }
    };

    private static List<CapabilityResolver> resolvers = Arrays.asList(
            xmlResolver
    );


    /**
     * {@inheritDoc}
     */
    @Override
    public void resolveCapabilities(Class<?> extensionType, CapabilityAwareBuilder<?, ?> builder)
    {
        Preconditions.checkArgument(extensionType != null, "extensionType cannot be null");
        Preconditions.checkArgument(builder != null, "builder cannot be null");

        for (CapabilityResolver resolver : resolvers)
        {
            resolver.resolve(extensionType, builder);
        }
    }

}
