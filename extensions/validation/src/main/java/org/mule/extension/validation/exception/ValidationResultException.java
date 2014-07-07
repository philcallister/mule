/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.validation.exception;

import org.mule.api.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;
import org.mule.extension.validation.ValidationResult;

/**
 * Exception to propagate an instance of {@link ValidationResult} which has errors;
 */
public class ValidationResultException extends MuleException
{

    private static final long serialVersionUID = -6513734614424639483L;

    private final ValidationResult validationResult;

    public ValidationResultException(ValidationResult validationResult)
    {
        this.validationResult = validationResult;
        this.setMessage(this.buildMessage());
    }

    public ValidationResult getValidationResult()
    {
        return this.validationResult;
    }

    private Message buildMessage()
    {
        StringBuilder builder = new StringBuilder();
        for (String message : this.validationResult.getMessages())
        {
            builder.append(message).append("\n");
        }

        return MessageFactory.createStaticMessage(builder.toString());
    }
}
