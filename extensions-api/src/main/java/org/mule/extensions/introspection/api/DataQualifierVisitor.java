/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.introspection.api;

public interface DataQualifierVisitor
{

    void onVoid();

    void onBoolean();

    void onInteger();

    void onDouble();

    void onDecimal();

    void onString();

    void onShort();

    void onLong();

    void onByte();

    void onStream();

    void onEnum();

    void onDate();

    void onDateTime();

    void onBean();

    void onList();

    void onMap();

    void onOperation();

}
