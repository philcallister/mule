/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.resources;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.resources.api.ResourcesGenerator;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@SmallTest
public class ExtensionRegistrationResourceContributorTestCase extends AbstractMuleTestCase
{

    @Test
    public void contribute()
    {
        Extension extension = mock(Extension.class);
        when(extension.getDeclaringClass()).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return ExtensionRegistrationResourceContributorTestCase.class;
            }
        });

        StringBuilder contentBuilder = new StringBuilder();
        ResourcesGenerator generator = mock(ResourcesGenerator.class, RETURNS_DEEP_STUBS);
        when(generator.getOrCreateResource("extensions/mule.extensions").getContentBuilder()).thenReturn(contentBuilder);

        new ExtensionRegistrationResourceContributor().contribute(extension, generator);

        assertEquals(getClass().getName() + '\n', contentBuilder.toString());
    }

}
