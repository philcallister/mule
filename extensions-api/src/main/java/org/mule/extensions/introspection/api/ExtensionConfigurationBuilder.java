/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

public interface ExtensionConfigurationBuilder extends Builder<ExtensionConfiguration>
{

    ExtensionConfigurationBuilder setName(String name);

    ExtensionConfigurationBuilder setDescription(String description);

    ExtensionConfigurationBuilder setDeclaringClass(Class<?> declaringClass);

    ExtensionConfigurationBuilder addParameter(ExtensionParameterBuilder parameter);

}
