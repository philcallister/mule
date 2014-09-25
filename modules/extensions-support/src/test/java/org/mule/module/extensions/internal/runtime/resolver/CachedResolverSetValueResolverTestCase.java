/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mule.api.MuleEvent;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class CachedResolverSetValueResolverTestCase extends AbstractMuleTestCase
{

    private static final Class<?> MODULE_CLASS = HeisenbergExtension.class;
    private static final long EXPIRATION_INTERVAL = 500;
    private static final TimeUnit EXPIRATION_TIME_UNIT = TimeUnit.MILLISECONDS;

    @Mock
    private ResolverSet resolverSet;

    @Mock
    private ResolverSetResult resolverSetResult;

    @Mock
    private HeisenbergExtension config;

    @Mock
    private MuleEvent event;

    private CachedResolverSetValueResolver resolver;

    @Before
    public void before() throws Exception
    {
        when(resolverSet.resolve(event)).thenReturn(resolverSetResult);
        when(resolverSetResult.toInstanceOf(MODULE_CLASS)).thenReturn(config);
        resolver = new CachedResolverSetValueResolver(MODULE_CLASS, resolverSet, EXPIRATION_INTERVAL, EXPIRATION_TIME_UNIT);
    }

    @Test
    public void resolveCached() throws Exception
    {
        final int count = 10;
        for (int i = 0; i < count; i++)
        {
            assertThat((HeisenbergExtension) resolver.resolve(event), is(config));
        }

        verify(resolverSet, times(count)).resolve(event);
        verify(resolverSetResult).toInstanceOf(MODULE_CLASS);
    }

    @Test
    public void resolveDifferentInstances() throws Exception
    {
        HeisenbergExtension instance1 = (HeisenbergExtension) resolver.resolve(event);
        assertThat(instance1, is(config));

        ResolverSetResult alternateResult = mock(ResolverSetResult.class);
        HeisenbergExtension alternateConfig = mock(HeisenbergExtension.class);
        when(resolverSet.resolve(event)).thenReturn(alternateResult);
        when(alternateResult.toInstanceOf(MODULE_CLASS)).thenReturn(alternateConfig);

        HeisenbergExtension instance2 = (HeisenbergExtension) resolver.resolve(event);
        assertThat(instance2, is(alternateConfig));
        assertThat(config, not(sameInstance(instance2)));

        verify(resolverSetResult).toInstanceOf(MODULE_CLASS);
        verify(alternateResult).toInstanceOf(MODULE_CLASS);
    }

    @Test
    public void stop() throws Exception
    {
        resolver.resolve(event);
        resolver.stop();
        verify(config).stop();
    }

    @Test
    public void dispose() throws Exception
    {
        resolver.resolve(event);
        resolver.dispose();
        verify(config).dispose();
    }

    @Test
    public void timeBasedEviction() throws Exception {
        resolver.resolve(event);

        Thread.sleep(EXPIRATION_INTERVAL);
        resolver.cleanUpCache();

        InOrder inOrder = inOrder(config);
        inOrder.verify(config).stop();
        inOrder.verify(config).dispose();
    }

}
