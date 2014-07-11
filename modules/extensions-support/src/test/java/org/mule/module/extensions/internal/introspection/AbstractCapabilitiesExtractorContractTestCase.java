/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCapabilitiesExtractorContractTestCase extends AbstractMuleTestCase
{
    protected CapabilitiesResolver resolver;

    @Mock
    protected NavigableExtensionBuilder builder;

    @Before
    public void before()
    {
        resolver = new CapabilitiesResolver();
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
}
