/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionParameterBuilder;

public interface NavigableExtensionParameterBuilder extends ExtensionParameterBuilder
{

    String getName();

    String getDescription();

    DataType getType();

    boolean isRequired();

    boolean isDynamic();

    Object getDefaultValue();

}
