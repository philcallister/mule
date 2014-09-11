/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static junit.framework.Assert.assertEquals;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.tck.testmodels.fruit.Apple;
import org.mule.tck.testmodels.fruit.Banana;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

@SmallTest
public class DataQualifierFactoryTestCase extends AbstractMuleTestCase
{

    @Test
    public void voidQualifier()
    {
        doAssert(DataQualifier.VOID, void.class, Void.class);
    }

    @Test
    public void bool()
    {
        doAssert(DataQualifier.BOOLEAN, boolean.class, Boolean.class);
    }

    @Test
    public void string()
    {
        doAssert(DataQualifier.STRING, String.class, char.class, Character.class);
    }

    @Test
    public void integer()
    {
        doAssert(DataQualifier.INTEGER, int.class, short.class, Integer.class, Short.class);
    }

    @Test
    public void doubleQualifier()
    {
        doAssert(DataQualifier.DOUBLE, double.class, float.class, Double.class, Float.class);
    }

    @Test
    public void longQualifier()
    {
        doAssert(DataQualifier.LONG, long.class, Long.class);
    }

    @Test
    public void decimal()
    {
        doAssert(DataQualifier.DECIMAL, BigDecimal.class, BigInteger.class);
    }

    @Test
    public void byteQualifier()
    {
        doAssert(DataQualifier.BYTE, byte.class, Byte.class);
    }

    @Test
    public void date()
    {
        doAssert(DataQualifier.DATE, Date.class, java.sql.Date.class);
    }

    @Test
    public void dateTime()
    {
        doAssert(DataQualifier.DATE_TIME, java.sql.Time.class, java.sql.Timestamp.class, Calendar.class, XMLGregorianCalendar.class);
    }

    @Test
    public void stream()
    {
        doAssert(DataQualifier.STREAM, InputStream.class, OutputStream.class, Reader.class, Writer.class);
    }

    @Test
    public void enumQualifier()
    {
        doAssert(DataQualifier.ENUM, Enumeration.class, Enum.class, DataQualifier.class);
    }

    @Test
    public void list()
    {
        doAssert(DataQualifier.LIST, List.class, Set.class, Object[].class);
    }

    @Test
    public void map()
    {
        doAssert(DataQualifier.MAP, Map.class);
    }

    @Test
    public void operation()
    {
        doAssert(DataQualifier.OPERATION, ExtensionOperation.class);
    }

    @Test
    public void bean()
    {
        doAssert(DataQualifier.POJO, Apple.class, Banana.class, Object.class);
    }

    private void doAssert(DataQualifier expected, Class<?>... types)
    {
        for (Class<?> type : types)
        {
            DataQualifier evaluated = DataQualifierFactory.getQualifier(type);
            assertEquals(String.format("was expecting %s but type %s returned %s instead", expected, type.getName(), expected, evaluated),
                         expected, evaluated);
        }
    }

}
