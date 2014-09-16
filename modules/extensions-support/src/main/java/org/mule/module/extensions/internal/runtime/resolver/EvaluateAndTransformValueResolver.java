/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.module.extensions.internal.util.MuleExtensionUtils.containsExpression;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.isSimpleExpression;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.Initialisable;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.MessageTransformer;
import org.mule.api.transformer.Transformer;
import org.mule.extensions.introspection.api.DataType;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.TemplateParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EvaluateAndTransformValueResolver implements ValueResolver, MuleContextAware, Initialisable
{

    private static final TemplateParser PARSER = TemplateParser.createMuleStyleParser();

    private final Object source;
    private final DataType expectedType;
    private ValueResolver delegate;
    private MuleContext muleContext;

    public EvaluateAndTransformValueResolver(Object source, DataType expectedType)
    {
        this.source = source;
        this.expectedType = expectedType;
    }

    @Override
    public void initialise() throws InitialisationException
    {
        if (source instanceof String)
        {
            String expression = (String) source;
            if (isSimpleExpression(expression, PARSER))
            {
                delegate = new ExpressionLanguageValueResolver(expression, muleContext.getExpressionLanguage());
            }
            else if (containsExpression(expression, PARSER))
            {
                delegate = new ExpressionTemplateValueResolver(expression, muleContext.getExpressionManager());
            }
        }

        if (delegate == null)
        {
            delegate = new StaticValueResolver(source);
        }
    }


    @Override
    public Object resolve(MuleEvent event) throws Exception
    {
        Object evaluated = delegate.resolve(event);
        return evaluated != null ? transform(evaluated, event) : null;
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
        Transformer transformer = muleContext.getRegistry().lookupTransformer(sourceDataType, targetDataType);

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
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
