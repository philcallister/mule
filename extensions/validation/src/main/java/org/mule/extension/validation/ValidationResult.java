/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.extension.validation;

import org.mule.api.MuleEvent;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class ValidationResult implements Serializable
{

    private static final long serialVersionUID = -6736205148881613778L;

    private final MuleEvent event;
    private final List<String> messages;

    public ValidationResult(MuleEvent event, List<String> messages)
    {
        this.event = event;
        if (messages != null)
        {
            this.messages = Collections.unmodifiableList(messages);
        }
        else
        {
            this.messages = null;
        }
    }

    public List<String> getMessages()
    {
        return this.messages;
    }

    public MuleEvent getEvent()
    {
        return this.event;
    }

    public boolean hasErros()
    {
        return !CollectionUtils.isEmpty(this.messages);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (this.messages != null)
        {
            for (String message : this.messages)
            {
                if (builder.length() > 0)
                {
                    builder.append("\n");
                }

                builder.append(message);
            }
        }

        return builder.toString();
    }
}
