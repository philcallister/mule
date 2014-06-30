/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml;

import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.spi.CapabilityExtractor;

public class XmlCapabilityExtractor implements CapabilityExtractor
{

    @Override
    public Object extractCapability(Class<?> extensionType)
    {
        Xml xml = extensionType.getAnnotation(Xml.class);
        if (xml != null)
        {
            return new ImmutableXmlCapability(xml.schemaVersion(), xml.namespace(), xml.schemaLocation());
        }

        return null;
    }
}
