/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.extensions.internal.capability.xml.schema.model;

import org.mule.extensions.introspection.api.DataType;

import javax.xml.namespace.QName;

public final class SchemaTypeConversion
{

    public static boolean isSupported(DataType type)
    {
        return convertType("", type.getRawType().getName()) != null;
    }

    public static QName convertType(String targetNamespace, String typeName)
    {
        if (typeName.equals("java.lang.String"))
        {
            return new QName(SchemaConstants.XSD_NAMESPACE, "string", "xs");
        }
        else if (typeName.equals("int"))
        {
            return new QName(targetNamespace, "integerType");
        }
        else if (typeName.equals("float"))
        {
            return new QName(targetNamespace, "floatType");
        }
        else if (typeName.equals("long"))
        {
            return new QName(targetNamespace, "longType");
        }
        else if (typeName.equals("byte"))
        {
            return new QName(targetNamespace, "byteType");
        }
        else if (typeName.equals("short"))
        {
            return new QName(targetNamespace, "integerType");
        }
        else if (typeName.equals("double"))
        {
            return new QName(targetNamespace, "doubleType");
        }
        else if (typeName.equals("boolean"))
        {
            return new QName(targetNamespace, "booleanType");
        }
        else if (typeName.equals("char"))
        {
            return new QName(targetNamespace, "charType");
        }
        else if (typeName.equals("java.lang.Integer"))
        {
            return new QName(targetNamespace, "integerType");
        }
        else if (typeName.equals("java.lang.Float"))
        {
            return new QName(targetNamespace, "floatType");
        }
        else if (typeName.equals("java.lang.Long"))
        {
            return new QName(targetNamespace, "longType");
        }
        else if (typeName.equals("java.lang.Byte"))
        {
            return new QName(targetNamespace, "byteType");
        }
        else if (typeName.equals("java.lang.Short"))
        {
            return new QName(targetNamespace, "integerType");
        }
        else if (typeName.equals("java.lang.Double"))
        {
            return new QName(targetNamespace, "doubleType");
        }
        else if (typeName.equals("java.lang.Boolean"))
        {
            return new QName(targetNamespace, "booleanType");
        }
        else if (typeName.equals("java.lang.Character"))
        {
            return new QName(targetNamespace, "charType");
        }
        else if (typeName.equals("java.math.BigDecimal"))
        {
            return new QName(targetNamespace, "doubleType");
        }
        else if (typeName.equals("java.math.BigInteger"))
        {
            return new QName(targetNamespace, "integerType");
        }
        else if (typeName.equals("java.util.Date"))
        {
            return new QName(targetNamespace, "dateTimeType");
        }
        else if (typeName.equals("java.util.Calendar"))
        {
            return new QName(targetNamespace, "dateTimeType");
        }
        else if (typeName.equals("java.lang.Class") ||
                 typeName.startsWith("java.lang.Class<"))
        {
            return new QName(SchemaConstants.XSD_NAMESPACE, "string", "xs");
        }
        else if (typeName.equals("java.net.URL"))
        {
            return new QName(targetNamespace, "anyUriType");
        }
        else if (typeName.equals("java.net.URI"))
        {
            return new QName(targetNamespace, "anyUriType");
        }

        return null;
    }
}
