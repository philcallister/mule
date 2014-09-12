/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.apache.commons.lang.StringUtils.EMPTY;
import org.mule.api.expression.InvalidExpressionException;

import org.junit.Test;

public class ExpressionLanguageValueResolverTestCase extends AbstractValueResolverTestCase
{

    @Test
    public void expression() throws Exception
    {
        final String expected = "Hello World!";
        Object evaluated = getResolver("#['Hello ' + payload]").resolve(getTestEvent("World!"));
        assertEvaluation(evaluated, expected);
    }

    @Test(expected = InvalidExpressionException.class)
    public void constant() throws Exception
    {
        getResolver("Hello World!").resolve(getTestEvent(EMPTY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullExpression() throws Exception
    {
        getResolver(null).resolve(getTestEvent(EMPTY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankExpression() throws Exception
    {
        Object evaluated = getResolver(EMPTY).resolve(getTestEvent("I'm blank!"));
        assertEvaluation(evaluated, EMPTY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullExpressionLanguage()
    {
        new ExpressionLanguageValueResolver("#[payload]", null);
    }

    @Override
    protected ValueResolver getResolver(String expression)
    {
        return new ExpressionLanguageValueResolver(expression, muleContext.getExpressionLanguage());
    }
}
