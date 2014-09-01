/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionParameterBuilder;

import java.util.List;

public interface NavigableExtensionConfigurationBuilder extends ExtensionConfigurationBuilder
{

    String getName();

    String getDescription();

    Class<?> getDeclaringClass();

    List<ExtensionParameterBuilder> getParameters();

}
