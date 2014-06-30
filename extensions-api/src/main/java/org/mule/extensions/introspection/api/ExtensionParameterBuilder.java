/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

public interface ExtensionParameterBuilder extends Builder<ExtensionParameter>
{

    ExtensionParameterBuilder setName(String name);

    ExtensionParameterBuilder setDescription(String description);

    ExtensionParameterBuilder setType(DataType type);

    ExtensionParameterBuilder setRequired(boolean required);

    ExtensionParameterBuilder setDynamic(boolean dynamic);

    ExtensionParameterBuilder setDefaultValue(Object defaultValue);

}
