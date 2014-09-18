/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;
import org.mule.api.el.ExpressionLanguage;

import org.apache.commons.lang.StringUtils;

public class ExpressionLanguageValueResolver extends AbstractDynamicValueResolver
{

    private final String expression;
    private final ExpressionLanguage expressionLanguage;

    public ExpressionLanguageValueResolver(String expression, ExpressionLanguage expressionLanguage)
    {
        checkArgument(!StringUtils.isBlank(expression), "expression cannot be blank");
        checkArgument(expressionLanguage != null, "expressionLanguage cannot be null");

        this.expression = expression;
        this.expressionLanguage = expressionLanguage;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        return expressionLanguage.evaluate(expression, event);
    }
}
