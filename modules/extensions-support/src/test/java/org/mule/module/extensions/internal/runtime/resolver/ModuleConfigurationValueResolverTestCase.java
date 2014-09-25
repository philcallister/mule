/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;
import org.mule.module.extensions.internal.util.ExtensionsTestUtils;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ModuleConfigurationValueResolverTestCase extends AbstractMuleTestCase
{

    private static final String CONFIG_NAME = "myConfig";
    private static final Class<?> MODULE_CLASS = HeisenbergExtension.class;

    @Mock
    private ResolverSet resolverSet;

    @Mock
    private MuleContext muleContext;

    @Mock
    private MuleEvent event;

    @Mock(extraInterfaces = {Lifecycle.class, MuleContextAware.class})
    private ObjectBuilder objectBuilder;

    @Mock
    private HeisenbergExtension config;

    @Mock
    private ResolverSetResult resolverSetResult = mock(ResolverSetResult.class);

    private int instancesBuiltThroughResolverSet;

    private ModuleConfigurationValueResolver resolver;

    @Before
    public void before() throws Exception
    {
        instancesBuiltThroughResolverSet = 0;
    }

    @Test
    public void resolveStaticConfig() throws Exception
    {
        resolver = getStaticConfigResolver();

        for (int i = 0; i < 10; i++)
        {
            assertThat((HeisenbergExtension) resolver.resolve(event), is(config));
        }

        verify(objectBuilder).build(event);
    }

    @Test
    public void resolveDynamicConfig() throws Exception
    {
        resolver = getDynamicConfigResolver();
        final int count = 10;

        for (int i = 0; i < count; i++)
        {
            assertThat((HeisenbergExtension) resolver.resolve(event), is(config));
        }

        assertThat(instancesBuiltThroughResolverSet, is(count));
    }

    @Test
    public void staticConfigIsNotDynamic() throws Exception
    {
        resolver = getStaticConfigResolver();
        assertThat(resolver.isDynamic(), is(false));
    }

    @Test
    public void dynamicConfigIsDynamic() throws Exception
    {
        resolver = getDynamicConfigResolver();
        assertThat(resolver.isDynamic(), is(true));
    }

    @Test
    public void staticConfigName() throws Exception
    {
        resolver = getStaticConfigResolver();
        assertThat(resolver.getName(), is(CONFIG_NAME));
    }

    @Test
    public void dynamicConfigName() throws Exception
    {
        resolver = getDynamicConfigResolver();
        assertThat(resolver.getName(), is(CONFIG_NAME));
    }

    @Test
    public void staticConfigInitialisation() throws Exception
    {
        resolver = getStaticConfigResolver();
        ExtensionsTestUtils.verifyInitialisation(resolverSet, muleContext);
        resolver.resolve(event);

        ExtensionsTestUtils.verifyInitialisation(config, muleContext);
        ExtensionsTestUtils.verifyInitialisation(objectBuilder, muleContext);
    }

    @Test
    public void dynamicConfigInitialisation() throws Exception
    {
        resolver = getDynamicConfigResolver();
        ExtensionsTestUtils.verifyInitialisation(resolverSet, muleContext);
        resolver.resolve(event);

        ExtensionsTestUtils.verifyInitialisation(config, muleContext);
    }

    @Test
    public void staticConfigStart() throws Exception
    {
        resolver = getStaticConfigResolver();
        verify(resolverSet).start();
        verify((Startable) objectBuilder).start();

        resolver.resolve(event);
        verify(config).start();
    }

    @Test
    public void dynamicConfigStart() throws Exception
    {
        resolver = getDynamicConfigResolver();
        verify(resolverSet).start();

        resolver.resolve(event);
        verify(config).start();
    }

    @Test
    public void staticConfigStop() throws Exception
    {
        resolver = getStaticConfigResolver();
        resolver.resolve(event);

        resolver.stop();
        verify(resolverSet).stop();
        verify((Stoppable) objectBuilder).stop();
        verify(config).stop();
    }

    @Test
    public void dynamicConfigStop() throws Exception
    {
        resolver = getDynamicConfigResolver();
        resolver.resolve(event);

        resolver.stop();
        verify(resolverSet).stop();
        verify(config).stop();
    }

    @Test
    public void staticConfigDispose() throws Exception
    {
        resolver = getStaticConfigResolver();
        resolver.resolve(event);

        resolver.dispose();
        verify(resolverSet).dispose();
        verify((Disposable) objectBuilder).dispose();
        verify(config).dispose();
    }

    @Test
    public void dynamicConfigDispose() throws Exception
    {
        resolver = getDynamicConfigResolver();
        resolver.resolve(event);

        resolver.dispose();
        verify(resolverSet).dispose();
        verify(config).dispose();
    }

    private ModuleConfigurationValueResolver getStaticConfigResolver() throws Exception
    {
        when(resolverSet.isDynamic()).thenReturn(false);
        when(resolverSet.toObjectBuilderOf(MODULE_CLASS)).thenReturn(objectBuilder);
        when(objectBuilder.build(event)).thenReturn(config);
        when(objectBuilder.isDynamic()).thenReturn(false);

        return getConfigResolver();
    }

    private ModuleConfigurationValueResolver getDynamicConfigResolver() throws Exception
    {
        when(resolverSet.isDynamic()).thenReturn(true);
        when(resolverSet.resolve(event)).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                ResolverSetResult resolverSetResult = mock(ResolverSetResult.class);
                when(resolverSetResult.toInstanceOf(MODULE_CLASS)).thenAnswer(new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        instancesBuiltThroughResolverSet++;
                        return config;
                    }
                });

                return resolverSetResult;
            }
        });

        return getConfigResolver();
    }

    private ModuleConfigurationValueResolver getConfigResolver() throws Exception
    {
        ModuleConfigurationValueResolver resolver = new ModuleConfigurationValueResolver(CONFIG_NAME, MODULE_CLASS, resolverSet);
        resolver.setMuleContext(muleContext);
        resolver.initialise();
        resolver.start();

        return resolver;
    }

}
