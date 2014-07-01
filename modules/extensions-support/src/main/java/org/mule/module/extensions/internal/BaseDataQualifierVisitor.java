/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import org.mule.extensions.introspection.api.DataQualifierVisitor;

public class BaseDataQualifierVisitor implements DataQualifierVisitor
{

    @Override
    public void onVoid()
    {
        defaultOperation();
    }

    @Override
    public void onBoolean()
    {
        defaultOperation();
    }

    @Override
    public void onInteger()
    {
        defaultOperation();
    }

    @Override
    public void onDouble()
    {
        defaultOperation();
    }

    @Override
    public void onDecimal()
    {
        defaultOperation();
    }

    @Override
    public void onString()
    {
        defaultOperation();
    }

    @Override
    public void onShort()
    {
        defaultOperation();
    }

    @Override
    public void onLong()
    {
        defaultOperation();
    }

    @Override
    public void onByte()
    {
        defaultOperation();
    }

    @Override
    public void onStream()
    {
        defaultOperation();
    }

    @Override
    public void onEnum()
    {
        defaultOperation();
    }

    @Override
    public void onDate()
    {
        defaultOperation();
    }

    @Override
    public void onDateTime()
    {
        defaultOperation();
    }

    @Override
    public void onBean()
    {
        defaultOperation();
    }

    @Override
    public void onList()
    {
        defaultOperation();
    }

    @Override
    public void onMap()
    {
        defaultOperation();
    }

    @Override
    public void onOperation()
    {
        defaultOperation();
    }

    protected void defaultOperation()
    {

    }
}
