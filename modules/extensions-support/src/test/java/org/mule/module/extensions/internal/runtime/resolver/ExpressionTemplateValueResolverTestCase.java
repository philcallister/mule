/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.apache.commons.lang.StringUtils.EMPTY;

import org.junit.Test;

public class ExpressionTemplateValueResolverTestCase extends AbstractValueResolverTestCase
{

    @Test
    public void template() throws Exception
    {
        final String expected = "Hello World!";
        Object evaluated = getResolver("Hello #[payload]").resolve(getTestEvent("World!"));
        assertEvaluation(evaluated, expected);
    }

    @Test
    public void constant() throws Exception
    {
        final String expected = "Hello World!";
        Object evaluated = getResolver(expected).resolve(getTestEvent(EMPTY));
        assertEvaluation(evaluated, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullExpression() throws Exception
    {
        getResolver(null).resolve(getTestEvent(EMPTY));
    }

    @Test
    public void blankExpression() throws Exception
    {
        Object evaluated = getResolver(EMPTY).resolve(getTestEvent("I'm blank!"));
        assertEvaluation(evaluated, EMPTY);
    }

    @Override
    protected ValueResolver getResolver(String expression)
    {
        return new ExpressionTemplateValueResolver(expression, muleContext.getExpressionManager());
    }
}
