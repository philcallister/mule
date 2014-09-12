/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListValueResolver extends CollectionValueResolver
{

    public ListValueResolver(List<ValueResolver> resolvers)
    {
        super(resolvers);
    }

    @Override
    protected Collection<Object> instantiateCollection(int resolversCount)
    {
        return new ArrayList<>(resolversCount);
    }
}
