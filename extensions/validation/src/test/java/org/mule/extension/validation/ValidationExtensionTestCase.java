/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.validation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.mule.tck.junit4.ExtensionsFunctionalTestCase;

import org.junit.Test;

;

public class ValidationExtensionTestCase extends ExtensionsFunctionalTestCase
{

    @Override
    protected String getConfigFile()
    {
        return "validation-test-config.xml";
    }

    @Test
    public void customConfigurationPresent() throws Exception
    {
        assertNotNull(muleContext.getExtensionsManager());
        ValidationExtension extension = muleContext.getRegistry().lookupObject("customValidator");
        assertNotNull(extension);
        assertEquals(RuntimeException.class.getName(), extension.getDefaultExceptionClass());
    }
}
