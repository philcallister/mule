/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.extensions.api.annotation.Extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

final class ExtensionDescriptor
{

    private String name;
    private String description;
    private String version;
    private String minMuleVersion;
    private String configElementName;
    private List<Field> configurableFields = new LinkedList<>();
    private List<Method> operationMethods = new LinkedList<>();

    ExtensionDescriptor(Extension extension)
    {
        name = extension.name();
        description = extension.description();
        version = extension.version();
        minMuleVersion = extension.minMuleVersion();
        configElementName = extension.configElementName();
    }

    ExtensionDescriptor addConfigurableFields(Collection<Field> fields)
    {
        configurableFields.addAll(fields);
        return this;
    }

    List<Field> getConfigurableFields()
    {
        return configurableFields;
    }

    ExtensionDescriptor addOperationMethods(Collection<Method> methods)
    {
        operationMethods.addAll(methods);
        return this;
    }

    public List<Method> getOperationMethods()
    {
        return operationMethods;
    }

    String getName()
    {
        return name;
    }

    String getDescription()
    {
        return description;
    }

    String getVersion()
    {
        return version;
    }

    String getMinMuleVersion()
    {
        return minMuleVersion;
    }

    String getConfigElementName()
    {
        return configElementName;
    }
}
