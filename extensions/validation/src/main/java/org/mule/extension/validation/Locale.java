/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.validation;

public enum Locale
{

    ENGLISH(java.util.Locale.ENGLISH),
    FRENCH(java.util.Locale.FRENCH),
    GERMAN(java.util.Locale.GERMAN),
    ITALIAN(java.util.Locale.ITALIAN),
    JAPANESE(java.util.Locale.JAPANESE),
    KOREAN(java.util.Locale.KOREAN),
    CHINESE(java.util.Locale.CHINESE),
    SIMPLIFIED_CHINESE(java.util.Locale.SIMPLIFIED_CHINESE),
    TRADITIONAL_CHINESE(java.util.Locale.TRADITIONAL_CHINESE),
    FRANCE(java.util.Locale.FRANCE),
    GERMANY(java.util.Locale.GERMANY),
    ITALY(java.util.Locale.ITALY),
    JAPAN(java.util.Locale.JAPAN),
    KOREA(java.util.Locale.KOREA),
    CHINA(java.util.Locale.CHINA),
    PRC(java.util.Locale.PRC),
    TAIWAN(java.util.Locale.TAIWAN),
    UK(java.util.Locale.UK),
    US(java.util.Locale.US),
    CANADA(java.util.Locale.CANADA),
    CANADA_FRENCH(java.util.Locale.CANADA_FRENCH);

    private java.util.Locale javaLocale;

    private Locale(java.util.Locale javaLocale)
    {
        this.javaLocale = javaLocale;
    }

    public java.util.Locale getJavaLocale()
    {
        return javaLocale;
    }
}
