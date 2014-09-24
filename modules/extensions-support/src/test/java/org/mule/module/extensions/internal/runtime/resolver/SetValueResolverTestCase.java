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
import java.util.Set;

import org.junit.Test;

@SmallTest
public class SetValueResolverTestCase extends AbstractCollectionValueResolverTestCase
{

    @Override
    protected CollectionValueResolver createCollectionResolver(List<ValueResolver> childResolvers)
    {
        return new SetValueResolver(childResolvers);
    }

    @Override
    protected Class<? extends Collection> getExpectedCollectionType()
    {
        return Set.class;
    }

    @Test
    public void of()
    {
        doAssertOf(Set.class, SetValueResolver.class);
    }
}
