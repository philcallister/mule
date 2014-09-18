/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;
import org.mule.api.expression.ExpressionManager;

public class ExpressionTemplateValueResolver extends AbstractDynamicValueResolver
{

    private final String expression;
    private final ExpressionManager expressionManager;

    public ExpressionTemplateValueResolver(String expression, ExpressionManager expressionManager)
    {
        checkArgument(expression != null, "Expression cannot be null");
        checkArgument(expressionManager != null, "expressionManager cannot be null");

        this.expression = expression;
        this.expressionManager = expressionManager;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        return expressionManager.parse(expression, event);
    }
}
