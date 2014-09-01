/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

public interface ExtensionOperationBuilder extends Builder<ExtensionOperation>
{

    ExtensionOperationBuilder setName(String name);

    ExtensionOperationBuilder setDescription(String description);

    ExtensionOperationBuilder setDeclaringClass(Class<?> declaringClass);

    ExtensionOperationBuilder addInputType(DataType... type);

    ExtensionOperationBuilder setOutputType(DataType type);

    ExtensionOperationBuilder addParameter(ExtensionParameterBuilder parameter);

}
