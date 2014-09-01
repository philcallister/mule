/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionOperationBuilder;

import java.util.List;

public interface NavigableExtensionBuilder extends ExtensionBuilder
{

    String getName();

    String getDescription();

    String getVersion();

    String getMinMuleVersion();

    Class<?> getDeclaringClass();

    List<ExtensionConfigurationBuilder> getConfigurations();

    List<ExtensionOperationBuilder> getOperations();

}
