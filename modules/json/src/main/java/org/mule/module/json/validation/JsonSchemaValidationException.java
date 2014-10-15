/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.json.validation;

import org.mule.api.MuleException;
import org.mule.config.i18n.MessageFactory;

public class JsonSchemaValidationException extends MuleException
{

    private final String invalidJson;

    public JsonSchemaValidationException(String validationError, String invalidJson) {
        super(MessageFactory.createStaticMessage("Json content is not compliant with schema\n" + validationError));
        this.invalidJson = invalidJson;
    }

    public String getInvalidJson()
    {
        return invalidJson;
    }
}
