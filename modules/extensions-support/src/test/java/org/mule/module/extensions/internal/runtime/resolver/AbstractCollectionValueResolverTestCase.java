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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mule.api.MuleEvent;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractCollectionValueResolverTestCase extends AbstractMuleTestCase
{

    private ValueResolver resolver;
    private List<ValueResolver> childResolvers;
    private List<Integer> expectedValues;

    @Before
    public void before() throws Exception
    {
        childResolvers = new ArrayList();
        expectedValues = new ArrayList<>();

        for (int i = 0; i < getChildResolversCount(); i++)
        {
            ValueResolver childResolver = mock(ValueResolver.class);
            when(childResolver.resolve(any(MuleEvent.class))).thenReturn(i);
            childResolvers.add(childResolver);
            expectedValues.add(i);
        }

        resolver = createCollectionResolver(childResolvers);
    }

    @Test
    public void resolve() throws Exception
    {
        Collection<Object> resolved = (Collection<Object>) resolver.resolve(mock(MuleEvent.class));

        assertThat(resolved, notNullValue());
        assertThat(resolved.size(), equalTo(getChildResolversCount()));
        assertThat(resolved, hasItems(expectedValues.toArray()));
    }

    @Test
    public void resolversAreCopied() throws Exception
    {
        int initialResolversCount = childResolvers.size();

        ValueResolver extra = mock(ValueResolver.class);
        when(extra.resolve(any(MuleEvent.class))).thenReturn(-1);
        childResolvers.add(extra);

        Collection<Object> resolved = (Collection<Object>) resolver.resolve(mock(MuleEvent.class));
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

    protected abstract CollectionValueResolver createCollectionResolver(List<ValueResolver> childResolvers);

    protected abstract Class<? extends Collection> getExpectedCollectionType();

    protected int getChildResolversCount()
    {
        return 10;
    }
}
