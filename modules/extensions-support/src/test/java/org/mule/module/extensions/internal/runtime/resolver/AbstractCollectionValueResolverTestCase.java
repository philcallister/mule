/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.module.extensions.internal.util.ExtensionsTestUtils.getResolver;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.module.extensions.internal.util.ExtensionsTestUtils;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCollectionValueResolverTestCase extends AbstractMuleTestCase
{

    private CollectionValueResolver resolver;
    private List<ValueResolver> childResolvers;
    private List<Integer> expectedValues;

    @Mock
    private MuleContext muleContext;

    @Mock
    private MuleEvent event;

    @Before
    public void before() throws Exception
    {
        childResolvers = new ArrayList();
        expectedValues = new ArrayList<>();

        for (int i = 0; i < getChildResolversCount(); i++)
        {
            ValueResolver childResolver = getResolver(i, event, false, MuleContextAware.class, Lifecycle.class);
            childResolvers.add(childResolver);
            expectedValues.add(i);
        }

        resolver = createCollectionResolver(childResolvers);
    }

    @Test
    public void resolve() throws Exception
    {
        Collection<Object> resolved = (Collection<Object>) resolver.resolve(event);

        assertThat(resolved, notNullValue());
        assertThat(resolved.size(), equalTo(getChildResolversCount()));
        assertThat(resolved, hasItems(expectedValues.toArray()));
    }

    @Test
    public void resolversAreCopied() throws Exception
    {
        int initialResolversCount = childResolvers.size();
        childResolvers.add(ExtensionsTestUtils.getResolver(-1, event, false));

        Collection<Object> resolved = (Collection<Object>) resolver.resolve(event);
        assertThat(resolved.size(), equalTo(initialResolversCount));
    }

    @Test
    public void emptyList() throws Exception
    {
        childResolvers.clear();
        resolver = createCollectionResolver(childResolvers);

        Collection<Object> resolved = (Collection<Object>) resolver.resolve(mock(MuleEvent.class));
        assertThat(resolved, notNullValue());
        assertThat(resolved.size(), equalTo(0));
    }

    @Test
    public void isNotDynamic()
    {
        assertThat(resolver.isDynamic(), is(false));
    }

    @Test
    public void isDynamic() throws Exception
    {
        childResolvers = new ArrayList();
        childResolvers.add(getResolver(null, event, false));
        childResolvers.add(getResolver(null, event, true));

        resolver = createCollectionResolver(childResolvers);
        assertThat(resolver.isDynamic(), is(true));
    }

    @Test
    public void collectionOfExpectedType() throws Exception
    {
        Collection<Object> resolved = (Collection<Object>) resolver.resolve(mock(MuleEvent.class));
        assertThat(resolved, instanceOf(getExpectedCollectionType()));
    }

    @Test
    public void resolvedCollectionIsMutalbe() throws Exception
    {
        Collection<Object> resolved = (Collection<Object>) resolver.resolve(mock(MuleEvent.class));
        int originalSize = resolved.size();
        resolved.add(-1);

        assertThat(resolved.size(), equalTo(originalSize + 1));
    }

    @Test
    public void initialise() throws Exception
    {
        resolver.setMuleContext(muleContext);
        resolver.initialise();
        ExtensionsTestUtils.verifyAllInitialised(childResolvers, muleContext);
    }

    @Test
    public void start() throws Exception
    {
        resolver.start();
        ExtensionsTestUtils.verifyAllStarted(childResolvers);
    }

    @Test
    public void stop() throws Exception
    {
        resolver.stop();
        ExtensionsTestUtils.verifyAllStopped(childResolvers);
    }

    @Test
    public void dispose() throws Exception
    {
        resolver.dispose();
        ExtensionsTestUtils.verifyAllDisposed(childResolvers);
    }


    protected abstract CollectionValueResolver createCollectionResolver(List<ValueResolver> childResolvers);

    protected abstract Class<? extends Collection> getExpectedCollectionType();

    protected int getChildResolversCount()
    {
        return 10;
    }

    protected void doAssertOf(Class<? extends Collection> collectionType, Class<? extends ValueResolver> expectedResolverType)
    {
        ValueResolver resolver = CollectionValueResolver.of(mock(collectionType).getClass(), new ArrayList<ValueResolver>());
        assertThat(resolver.getClass() == expectedResolverType, is(true));
    }
}
