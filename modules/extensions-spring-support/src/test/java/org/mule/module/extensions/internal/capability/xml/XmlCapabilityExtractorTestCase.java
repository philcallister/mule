/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.module.extensions.internal.introspection.AbstractCapabilitiesExtractorContractTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SmallTest
public class XmlCapabilityExtractorTestCase extends AbstractCapabilitiesExtractorContractTestCase
{

    private static final String SCHEMA_VERSION = "SCHEMA_VERSION";
    private static final String NAMESPACE = "NAMESPACE";
    private static final String SCHEMA_LOCATION = "SCHEMA_LOCATION";

    private static final String EXTENSION_NAME = "extension";
    private static final String EXTENSION_VERSION = "3.6";

    private ArgumentCaptor<XmlCapability> captor;

    @Before
    public void before()
    {
        super.before();
        captor = ArgumentCaptor.forClass(XmlCapability.class);
    }

    @Test
    public void capabilityAdded()
    {
        resolver.resolveCapabilities(XmlSupport.class, builder);
        verify(builder).addCapablity(captor.capture());

        XmlCapability capability = captor.getValue();
        assertNotNull(capability);
        assertEquals(SCHEMA_VERSION, capability.getSchemaVersion());
        assertEquals(NAMESPACE, capability.getNamespace());
        assertEquals(SCHEMA_LOCATION, capability.getSchemaLocation());
    }

    @Test
    public void defaultCapabilityValues()
    {
        when(builder.getName()).thenReturn(EXTENSION_NAME);
        when(builder.getVersion()).thenReturn(EXTENSION_VERSION);

        resolver.resolveCapabilities(DefaultXmlExtension.class, builder);
        verify(builder).addCapablity(captor.capture());

        XmlCapability capability = captor.getValue();
        assertNotNull(capability);
        assertEquals(EXTENSION_VERSION, capability.getSchemaVersion());
        assertEquals(NAMESPACE, capability.getNamespace());
        assertEquals(String.format(XmlCapabilityExtractor.DEFAULT_SCHEMA_LOCATION_MASK, EXTENSION_NAME), capability.getSchemaLocation());
    }

    @Xml(schemaVersion = SCHEMA_VERSION, namespace = NAMESPACE, schemaLocation = SCHEMA_LOCATION)
    private static class XmlSupport
    {

    }

    @Xml(namespace = NAMESPACE)
    private static class DefaultXmlExtension
    {

    }
}
