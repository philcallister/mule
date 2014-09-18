/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime.resolver;

import static org.mule.module.extensions.internal.util.MuleExtensionUtils.containsExpression;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.isSimpleExpression;
import static org.mule.util.Preconditions.checkState;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.lifecycle.LifecycleUtils;
import org.mule.api.lifecycle.Startable;
import org.mule.api.lifecycle.Stoppable;
import org.mule.api.transformer.MessageTransformer;
import org.mule.api.transformer.Transformer;
import org.mule.extensions.introspection.api.DataType;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.TemplateParser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluateAndTransformValueResolver implements ValueResolver, MuleContextAware, Lifecycle
{

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluateAndTransformValueResolver.class);
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
    public boolean isDynamic()
    {
        checkState(delegate != null, "This value resolver needs to be initialised before it can perform any operation");
        return delegate.isDynamic();
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

        LifecycleUtils.initialiseIfNeeded(delegate);
    }

    @Override
    public void start() throws MuleException
    {
        LifecycleUtils.applyPhaseIfNeeded(Startable.PHASE_NAME, delegate);
    }

    @Override
    public void stop() throws MuleException
    {
        LifecycleUtils.applyPhaseIfNeeded(Stoppable.PHASE_NAME, delegate);
    }

    @Override
    public void dispose()
    {
        LifecycleUtils.disposeIfNeeded(LOGGER, delegate);
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }
}
