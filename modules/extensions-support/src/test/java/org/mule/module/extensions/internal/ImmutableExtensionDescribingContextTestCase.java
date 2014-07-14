/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionDescribingContext;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ImmutableExtensionDescribingContextTestCase extends AbstractMuleTestCase
{

    private static final Class<?> extensionType = ImmutableExtensionDescribingContextTestCase.class;

    @Mock
    private ExtensionBuilder builder;

    private ExtensionDescribingContext context;

    @Before
    public void before()
    {
        context = new ImmutableExtensionDescribingContext(extensionType, builder);
    }

    @Test
    public void getExtensionType()
    {
        assertSame(extensionType, context.getExtensionType());
    }

    @Test
    public void getExtensionBuilder()
    {
        assertSame(builder, context.getExtensionBuilder());
    }

    @Test
    public void customParameters()
    {
        assertNotNull(context.getCustomParameters());
        assertTrue(context.getCustomParameters().isEmpty());

        final String key = "key";
        final String value = "value";

        context.getCustomParameters().put(key, value);
        assertEquals(1, context.getCustomParameters().size());
        assertEquals(value, context.getCustomParameters().get(key));
    }
}
