/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml;

import static org.apache.commons.lang.StringUtils.isBlank;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.api.CapabilityAwareBuilder;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.extensions.introspection.spi.CapabilityExtractor;
import org.mule.module.extensions.internal.introspection.NavigableExtensionBuilder;

/**
 * Implementation of {@link org.mule.extensions.introspection.spi.CapabilityExtractor}
 * that verifies if the extension is annotated with {@link org.mule.extensions.introspection.api.capability.XmlCapability}
 * and if so, registers into the builder a {@link org.mule.extensions.introspection.api.capability.XmlCapability
 *
 * @since 3.7.0
 */
public class XmlCapabilityExtractor implements CapabilityExtractor
{

    public static final String DEFAULT_SCHEMA_LOCATION_MASK = "http://www.mulesoft.org/schema/mule/extension/%s";

    @Override
    public Object extractCapability(Class<?> extensionType, CapabilityAwareBuilder<?, ?> builder)
    {
        Xml xml = extensionType.getAnnotation(Xml.class);
        if (xml != null)
        {
            XmlCapability capability = processCapability(xml, builder);
            builder.addCapablity(capability);

            return capability;
        }

        return null;
    }

    private XmlCapability processCapability(Xml xml, CapabilityAwareBuilder<?, ?> builder)
    {
        if (builder instanceof NavigableExtensionBuilder)
        {
            return applyRules(xml, (NavigableExtensionBuilder) builder);
        }
        else
        {
            return new ImmutableXmlCapability(xml.schemaVersion(), xml.namespace(), xml.schemaLocation());
        }
    }

    private XmlCapability applyRules(Xml xml, NavigableExtensionBuilder builder)
    {
        String schemaVersion = isBlank(xml.schemaVersion()) ? builder.getVersion() : xml.schemaVersion();
        String schemaLocation = isBlank(xml.schemaLocation()) ? buildDefaultLocation(builder) : xml.schemaLocation();

        return new ImmutableXmlCapability(schemaVersion, xml.namespace(), schemaLocation);
    }

    private String buildDefaultLocation(NavigableExtensionBuilder builder)
    {
        return String.format(DEFAULT_SCHEMA_LOCATION_MASK, builder.getName());
    }

}
