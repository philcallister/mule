/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import org.mule.module.extensions.internal.BaseDataQualifierVisitor;

public abstract class SimpleTypeDataQualifierVisitor extends BaseDataQualifierVisitor
{

    protected abstract void onSimpleType();

    @Override
    public void onBoolean()
    {
        onSimpleType();
    }

    @Override
    public void onInteger()
    {
        onSimpleType();
    }

    @Override
    public void onDouble()
    {
        onSimpleType();
    }

    @Override
    public void onDecimal()
    {
        onSimpleType();
    }

    @Override
    public void onString()
    {
        onSimpleType();
    }

    @Override
    public void onShort()
    {
        onSimpleType();
    }

    @Override
    public void onLong()
    {
        onSimpleType();
    }

    @Override
    public void onByte()
    {
        onSimpleType();
    }

    @Override
    public void onEnum()
    {
        onSimpleType();
    }
}
