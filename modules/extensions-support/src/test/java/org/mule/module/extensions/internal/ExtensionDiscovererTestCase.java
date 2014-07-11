/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static junit.framework.Assert.assertEquals;
import org.mule.extensions.api.ExtensionsManager;
import org.mule.extensions.introspection.api.Extension;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.module.extensions.internal.introspection.DefaultExtensionDescriber;
import org.mule.module.extensions.internal.introspection.ExtensionDiscoverer;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ExtensionDiscovererTestCase extends AbstractMuleTestCase
{

    @Mock
    private ExtensionsManager extensionsManager;

    private ExtensionDiscoverer discoverer;

    @Before
    public void setUp()
    {
        discoverer = new DefaultExtensionDiscoverer();
    }

    @Test
    public void scan() throws Exception
    {
        List<Extension> extensions = discoverer.discover(getClass().getClassLoader(), new DefaultExtensionDescriber());
        assertEquals(1, extensions.size());

        Extension extension = extensions.get(0);
        assertMuleExtension(extension);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullClassLoader()
    {
        discoverer.discover(null, new DefaultExtensionDescriber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDescriber()
    {
        discoverer.discover(getClass().getClassLoader(), null);
    }

    private void assertMuleExtension(Extension extension)
    {
        assertEquals(HeisenbergExtension.EXTENSION_NAME, extension.getName());
    }
}
