/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.validation;

import org.mule.api.DefaultMuleException;
import org.mule.config.i18n.MessageFactory;
import org.mule.extension.validation.exception.ValidationException;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Extension;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.util.ClassUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Validations module
 */
@Extension(name = "validation", version = "3.6")
@Xml(namespace = "validation")
public class ValidationExtension
{

    /**
     * The canonical name of the exception class to be thrown each time a validation fails.
     * This is optional in case that you want to customize the exception type. If not provided
     * it will default to {@link org.mule.extension.validation.exception.ValidationException}
     */
    @Configurable
    @Optional(defaultValue = "org.mule.extension.validation.exception.ValidationException")
    private String defaultExceptionClass;

    /**
     * Validates that the given {@code value} is {@code true}
     *
     * @param value the boolean to test
     * @param message an optional custom message
     * @throws Exception if the value is not {@code true}
     */
    @Operation
    public void validateTrue(final boolean value, @Optional String message) throws Exception
    {
        validateWith(message, new Validator()
        {
            @Override
            public boolean isValid()
            {
                return value;
            }

            @Override
            public String getErrorMessage()
            {
                return "value was false";
            }
        });
    }

    private void validateWith(String customMessage, Validator validator) throws Exception
    {
        if (!validator.isValid())
        {
            String message = StringUtils.isBlank(customMessage) ? validator.getErrorMessage() : customMessage;
            throw buildException(message);
        }
    }

    private Exception buildException(String message)
    {
        try
        {
            return ClassUtils.instanciateClass(this.getExceptionClass(), new Object[] {message});
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            return new DefaultMuleException(
                    MessageFactory.createStaticMessage("Failed to create validation exception"), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Exception> getExceptionClass()
    {
        if (StringUtils.isBlank(defaultExceptionClass))
        {
            return ValidationException.class;
        }

        Class<? extends Exception> exceptionClass;
        try
        {
            exceptionClass = ClassUtils.getClass(defaultExceptionClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Could not find exception class " + defaultExceptionClass);
        }

        if (!Exception.class.isAssignableFrom(exceptionClass))
        {
            throw new IllegalArgumentException(String.format(
                    "Was expecting an exception type, %s found instead", exceptionClass.getCanonicalName()));
        }

        if (ClassUtils.getConstructor(exceptionClass, new Class[] {String.class}) == null)
        {
            throw new IllegalArgumentException(
                    String.format(
                            "Exception class must contain a constructor with a single String argument. %s doesn't have a matching constructor",
                            exceptionClass.getCanonicalName()));
        }

        return exceptionClass;
    }

    public String getDefaultExceptionClass()
    {
        return defaultExceptionClass;
    }

    public void setDefaultExceptionClass(String defaultExceptionClass)
    {
        this.defaultExceptionClass = defaultExceptionClass;
    }
}
