/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.extensions.HealthStatus;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;
import org.mule.module.extensions.internal.util.ExtensionsTestUtils;
import org.mule.repackaged.internal.org.springframework.util.ReflectionUtils;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultObjectBuilderTestCase extends AbstractMuleTestCase
{

    private static Class<?> PROTOTYPE_CLASS = HeisenbergExtension.class;
    private static final String NAME = "heisenberg";
    private static final int AGE = 50;
    private static HealthStatus HEALTH = HealthStatus.DEAD;

    @Mock
    private MuleEvent event;

    @Mock
    private MuleContext muleContext;

    private DefaultObjectBuilder builder;
    private Method nameSetter;
    private Method ageSetter;
    private Method healthSetter;
    private List<ValueResolver> resolvers = new ArrayList<>();

    @Before
    public void before()
    {
        builder = new DefaultObjectBuilder();
        builder.setPrototypeClass(PROTOTYPE_CLASS);

        nameSetter = ReflectionUtils.findMethod(PROTOTYPE_CLASS, "setMyName", String.class);
        ageSetter = ReflectionUtils.findMethod(PROTOTYPE_CLASS, "setAge", Integer.class);
        healthSetter = ReflectionUtils.findMethod(PROTOTYPE_CLASS, "setInitialHealth", HealthStatus.class);
    }

    @Test
    public void build() throws Exception
    {
        populate(false);
        HeisenbergExtension heisenberg = (HeisenbergExtension) builder.build(event);
        verify(heisenberg);
    }

    @Test
    public void reusable() throws Exception
    {
        populate(false);
        HeisenbergExtension heisenberg1 = (HeisenbergExtension) builder.build(event);
        HeisenbergExtension heisenberg2 = (HeisenbergExtension) builder.build(event);
        HeisenbergExtension heisenberg3 = (HeisenbergExtension) builder.build(event);

        assertThat(heisenberg1, is(not(heisenberg2)));
        assertThat(heisenberg1, is(not(heisenberg3)));
        verify(heisenberg1);
        verify(heisenberg2);
        verify(heisenberg3);
    }

    private void verify(HeisenbergExtension heisenberg)
    {
        assertThat(heisenberg.getMyName(), is(NAME));
        assertThat(heisenberg.getAge(), is(AGE));
        assertThat(heisenberg.getInitialHealth(), is(HEALTH));
    }

    @Test
    public void isStatic() throws Exception
    {
        populate(false);
        assertThat(builder.isDynamic(), is(false));
    }

    @Test
    public void isDynamic() throws Exception
    {
        builder.addProperty(nameSetter, getResolver(NAME, false));
        builder.addProperty(ageSetter, getResolver(AGE, true));

        assertThat(builder.isDynamic(), is(true));
    }

    @Test
    public void initialise() throws Exception
    {
        builder.setMuleContext(muleContext);
        builder.initialise();
        ExtensionsTestUtils.verifyAllInitialised(resolvers, muleContext);
    }

    @Test
    public void start() throws Exception
    {
        builder.start();
        ExtensionsTestUtils.verifyAllStarted(resolvers);
    }

    @Test
    public void stop() throws Exception
    {
        builder.stop();
        ExtensionsTestUtils.verifyAllStopped(resolvers);
    }

    @Test
    public void dispose() throws Exception
    {
        builder.dispose();
        ExtensionsTestUtils.verifyAllDisposed(resolvers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildInterface() throws Exception
    {
        builder.setPrototypeClass(MuleMessage.class);
        builder.build(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void abstractClass() throws Exception
    {
        builder.setPrototypeClass(TestAbstract.class);
        builder.build(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDefaultConstructor() throws Exception
    {
        builder.setPrototypeClass(TestNoDefaultConstructor.class);
        builder.build(event);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noPublicConstructor() throws Exception
    {
        builder.setPrototypeClass(NoPublicConstructor.class);
        builder.build(event);
    }

    private void populate(boolean dynamic) throws Exception
    {
        builder.addProperty(nameSetter, getResolver(NAME, dynamic));
        builder.addProperty(ageSetter, getResolver(AGE, dynamic));
        builder.addProperty(healthSetter, getResolver(HEALTH, dynamic));
    }

    private ValueResolver getResolver(Object value, boolean dynamic) throws Exception
    {
        ValueResolver resolver = ExtensionsTestUtils.getResolver(value, event, dynamic);
        resolvers.add(resolver);

        return resolver;
    }

    private static abstract class TestAbstract
    {

    }

    public static class TestNoDefaultConstructor
    {

        public TestNoDefaultConstructor(String value)
        {
        }
    }

    public static class NoPublicConstructor
    {

        protected NoPublicConstructor()
        {
        }
    }
}