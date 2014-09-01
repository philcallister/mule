/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.validation;

import org.apache.commons.validator.routines.CodeValidator;
import org.apache.commons.validator.routines.CreditCardValidator;

public enum CreditCardType
{
    VISA(CreditCardValidator.VISA_VALIDATOR),
    MASTERCARD(CreditCardValidator.MASTERCARD_VALIDATOR),
    DINERS(CreditCardValidator.DINERS_VALIDATOR),
    DISCOVER(CreditCardValidator.DISCOVER_VALIDATOR),
    AMEX(CreditCardValidator.AMEX_VALIDATOR);

    private CodeValidator codeValidator;

    private CreditCardType(CodeValidator codeValidator)
    {

    }

    public CodeValidator getCodeValidator()
    {
        return codeValidator;
    }
}
