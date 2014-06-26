/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.spi;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.extensions.introspection.spi.CapabilityAwareBuilder;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCapabilitiesResolverTestCase extends AbstractMuleTestCase
{

    private static final String SCHEMA_VERSION = "SCHEMA_VERSION";
    private static final String NAMESPACE = "NAMESPACE";
    private static final String SCHEMA_LOCATION = "SCHEMA_LOCATION";

    private CapabilitiesResolver resolver;

    @Mock
    private CapabilityAwareBuilder<?, ?> builder;

    @Before
    public void before()
    {
        resolver = new CapabilitiesResolver();
    }

    @Test
    public void capabilityAdded()
    {
        ArgumentCaptor<XmlCapability> captor = ArgumentCaptor.forClass(XmlCapability.class);
        resolver.resolveCapabilities(XmlSupport.class, builder);
        verify(builder).addCapablity(captor.capture());

        XmlCapability capability = captor.getValue();
        assertNotNull(capability);
        assertEquals(SCHEMA_VERSION, capability.getSchemaVersion());
        assertEquals(NAMESPACE, capability.getNamespace());
        assertEquals(SCHEMA_LOCATION, capability.getSchemaLocation());
    }

    @Test
    public void noCapability()
    {
        resolver.resolveCapabilities(getClass(), builder);
        verify(builder, never()).addCapablity(any(XmlCapability.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClass()
    {
        resolver.resolveCapabilities(null, builder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullBuilder()
    {
        resolver.resolveCapabilities(getClass(), null);
    }


    @Xml(schemaVersion = SCHEMA_VERSION, namespace = NAMESPACE, schemaLocation = SCHEMA_LOCATION)
    private static class XmlSupport
    {

    }

}
