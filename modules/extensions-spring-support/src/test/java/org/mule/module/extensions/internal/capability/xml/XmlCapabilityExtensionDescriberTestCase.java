/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.module.extensions.internal.DefaultExtensionDescriberTestCase;
import org.mule.tck.size.SmallTest;

import java.util.Set;

@SmallTest
public class XmlCapabilityExtensionDescriberTestCase extends DefaultExtensionDescriberTestCase
{

    @Override
    protected void assertCapabilities(Extension extension)
    {
        assertXmlCapability(extension);
    }

    private void assertXmlCapability(Extension extension)
    {
        Set<XmlCapability> capabilities = extension.getCapabilities(XmlCapability.class);
        assertNotNull(capabilities);
        assertEquals(1, capabilities.size());

        XmlCapability xml = capabilities.iterator().next();
        assertEquals(HeisenbergExtension.SCHEMA_LOCATION, xml.getSchemaLocation());
        assertEquals(HeisenbergExtension.SCHEMA_VERSION, xml.getSchemaVersion());
        assertEquals(HeisenbergExtension.NAMESPACE, xml.getNamespace());
    }
}
