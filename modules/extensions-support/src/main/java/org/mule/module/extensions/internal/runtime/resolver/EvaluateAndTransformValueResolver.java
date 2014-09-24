/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.module.extensions.internal.util.MuleExtensionUtils.isSimpleExpression;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.MessageTransformer;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.extensions.introspection.api.DataType;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.TemplateParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;

public class EvaluateAndTransformValueResolver extends BaseValueResolverWrapper
{

    private static final TemplateParser PARSER = TemplateParser.createMuleStyleParser();

    private final String expression;
    private final DataType expectedType;

    public EvaluateAndTransformValueResolver(String expression, DataType expectedType)
    {
        super(null);

        checkArgument(!StringUtils.isBlank(expression), "Expression cannot be blank or null");
        checkArgument(expectedType != null, "expected type cannot be null");
        this.expression = expression;
        this.expectedType = expectedType;
    }

    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        Object evaluated = delegate.resolve(event);
        return evaluated != null ? transform(evaluated, event) : null;
    }

    @Override
    public boolean isDynamic()
    {
        return true;
    }

    private Object transform(Object object, MuleEvent event) throws Exception
    {
        if (expectedType.getRawType().isInstance(object))
        {
            return object;
        }

        Type expectedClass = expectedType.getRawType();
        if (expectedClass instanceof ParameterizedType)
        {
            expectedClass = ((ParameterizedType) expectedClass).getRawType();
        }

        org.mule.api.transformer.DataType sourceDataType = DataTypeFactory.create(object.getClass());
        org.mule.api.transformer.DataType targetDataType = DataTypeFactory.create((Class) expectedClass);

        Transformer transformer;
        try
        {
            transformer = muleContext.getRegistry().lookupTransformer(sourceDataType, targetDataType);
        }
        catch (TransformerException e)
        {
            // no transformer found. Return the object we have and let's hope for the best
            return object;
        }

        if (transformer != null)
        {
            if (transformer instanceof MessageTransformer)
            {
                return ((MessageTransformer) transformer).transform(object, event);
            }
            else
            {
                return transformer.transform(object);
            }
        }

        return object;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        delegate = isSimpleExpression(expression, PARSER)
                   ? new ExpressionLanguageValueResolver(expression, muleContext.getExpressionLanguage())
                   : new ExpressionTemplateValueResolver(expression, muleContext.getExpressionManager());

        super.initialise();
    }
}
