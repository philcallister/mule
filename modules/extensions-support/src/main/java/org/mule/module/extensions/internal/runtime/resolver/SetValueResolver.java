/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SetValueResolver extends CollectionValueResolver
{

    public SetValueResolver(List<ValueResolver> resolvers)
    {
        super(resolvers);
    }

    @Override
    protected Collection<Object> instantiateCollection(int resolversCount)
    {
        return new HashSet<>(resolversCount);
    }
}
