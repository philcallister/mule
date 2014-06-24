/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.extensions.internal.capability.xml.schema.model;

public class SchemaLocation
{

    private String fileName;
    private String location;
    private Schema schema;
    private String namespaceHandler;
    private String targetNamespace;
    private String className;

    public SchemaLocation(Schema schema, String targetNamespace, String fileName, String location, String namespaceHandler, String className)
    {
        this.fileName = fileName;
        this.location = location;
        this.schema = schema;
        this.namespaceHandler = namespaceHandler;
        this.targetNamespace = targetNamespace;
        this.className = className;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getLocation()
    {
        return location;
    }

    public Schema getSchema()
    {
        return schema;
    }

    public String getNamespaceHandler()
    {
        return namespaceHandler;
    }

    public String getTargetNamespace()
    {
        return targetNamespace;
    }

    public String getClassName()
    {
        return className;
    }
}

