/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.extensions.internal.capability.xml.schema.model;

import javax.xml.namespace.QName;

public final class SchemaConstants
{

    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final String SPRING_FRAMEWORK_NAMESPACE = "http://www.springframework.org/schema/beans";
    public static final String SPRING_FRAMEWORK_SCHEMA_LOCATION = "http://www.springframework.org/schema/beans/spring-beans-3.0.xsd";
    public static final String MULE_NAMESPACE = "http://www.mulesoft.org/schema/mule/core";
    public static final String MULE_SCHEMA_LOCATION = "http://www.mulesoft.org/schema/mule/core/current/mule.xsd";
    public static final QName MULE_ABSTRACT_EXTENSION = new QName(MULE_NAMESPACE, "abstract-extension", "mule");
    public static final QName MULE_PROPERTY_PLACEHOLDER_TYPE = new QName(MULE_NAMESPACE, "propertyPlaceholderType", "mule");
    public static final QName MULE_ABSTRACT_EXTENSION_TYPE = new QName(MULE_NAMESPACE, "abstractExtensionType", "mule");
    public static final QName MULE_ABSTRACT_MESSAGE_PROCESSOR = new QName(MULE_NAMESPACE, "abstract-message-processor", "mule");
    public static final QName MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE = new QName(MULE_NAMESPACE, "abstractMessageProcessorType", "mule");
    public static final QName MULE_MESSAGE_PROCESSOR_OR_OUTBOUND_ENDPOINT_TYPE = new QName(MULE_NAMESPACE, "messageProcessorOrOutboundEndpoint", "mule");
    public static final String SUBSTITUTABLE_INT = "substitutableInt";
    public static final String SUBSTITUTABLE_LONG = "substitutableLong";
    public static final String SUBSTITUTABLE_BOOLEAN = "substitutableBoolean";
    public static final QName STRING = new QName(XSD_NAMESPACE, "string", "xs");
    public static final QName DECIMAL = new QName(XSD_NAMESPACE, "decimal", "xs");
    public static final QName FLOAT = new QName(XSD_NAMESPACE, "float", "xs");
    public static final QName INTEGER = new QName(MULE_NAMESPACE, SUBSTITUTABLE_INT, "mule");
    public static final QName DOUBLE = new QName(XSD_NAMESPACE, "double", "xs");
    public static final QName DATETIME = new QName(XSD_NAMESPACE, "dateTime", "xs");
    public static final QName LONG = new QName(MULE_NAMESPACE, SUBSTITUTABLE_LONG, "mule");
    public static final QName BYTE = new QName(XSD_NAMESPACE, "byte", "xs");
    public static final QName BOOLEAN = new QName(MULE_NAMESPACE, SUBSTITUTABLE_BOOLEAN, "mule");
    public static final QName ANYURI = new QName(XSD_NAMESPACE, "anyURI", "xs");
    public static final QName EXPRESSION = new QName(MULE_NAMESPACE, "expression", "mule");
    public static final String USE_REQUIRED = "required";
    public static final String USE_OPTIONAL = "optional";
    public static final String INNER_PREFIX = "inner-";
    public static final String ATTRIBUTE_NAME_CONFIG = "config";
    public static final String ATTRIBUTE_DESCRIPTION_CONFIG = "Specify which configuration to use for this invocation.";
    public static final String ATTRIBUTE_NAME_VALUE = "value";
    public static final String ENUM_TYPE_SUFFIX = "EnumType";
    public static final String TYPE_SUFFIX = "Type";
    public static final String UNBOUNDED = "unbounded";
    public static final String LAX = "lax";
    public static final String ATTRIBUTE_NAME_NAME = "name";
    public static final String ATTRIBUTE_NAME_NAME_DESCRIPTION = "Give a name to this configuration so it can be later referenced by config-ref.";
    public static String DEFAULT_PATTERN = "DEFAULT_PATTERN";
    public static final String XSD_EXTENSION = ".xsd";
}
