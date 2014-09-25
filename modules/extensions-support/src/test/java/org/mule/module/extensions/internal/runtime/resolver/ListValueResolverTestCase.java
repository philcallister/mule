/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import org.mule.tck.size.SmallTest;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ListValueResolverTestCase extends AbstractCollectionValueResolverTestCase
{

    @Override
    protected CollectionValueResolver createCollectionResolver(List<ValueResolver> childResolvers)
    {
        return new ListValueResolver(childResolvers);
    }

    @Override
    protected Class<? extends Collection> getExpectedCollectionType()
    {
        return List.class;
    }

    @Test
    public void ofList()
    {
        doAssertOf(List.class, ListValueResolver.class);
    }

    @Test
    public void ofCollection()
    {
        doAssertOf(Collection.class, ListValueResolver.class);
    }
}
