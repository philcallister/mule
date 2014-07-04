/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.config;

import org.mule.extensions.introspection.api.DataType;
import org.mule.module.extensions.internal.BaseDataQualifierVisitor;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Parsing delegate to process elements on unknown types.
 * Implementations of this class are not to be considered
 * thread safe
 *
 * @since 3.6.0
 */
abstract class ParseDelegate
{

    /**
     * Returns a {@link org.mule.module.extensions.internal.config.ParseDelegate}
     * that matches the given type
     */
    static ParseDelegate of(DataType dataType)
    {
        dataType = new DataTypeSelector(dataType).select();

        ParseDelegateResolver resolver = new ParseDelegateResolver();
        dataType.getQualifier().accept(resolver);

        ParseDelegate delegate = resolver.getDelegate();
        delegate.dataType = dataType;

        return delegate;
    }

    protected ExtensionBeanDefinitionParser parser;
    protected DataType dataType;

    private ParseDelegate()
    {
    }

    /**
     * Parses the given element according to the resolved type
     *
     * @param element a {@link org.w3c.dom.Element}
     * @return an nullable object
     */
    abstract Object parse(Element element);

    /**
     * Sets the current {@link org.mule.module.extensions.internal.config.ExtensionBeanDefinitionParser}
     * to be used. Because implementations of this class are not to be considered thread safe
     * be careful with the side effects of setting this
     *
     * @param parser
     */
    void setParser(ExtensionBeanDefinitionParser parser)
    {
        this.parser = parser;
    }

    /**
     * Implementation of {@link org.mule.module.extensions.internal.BaseDataQualifierVisitor}
     * which resolves which concrete instance of {@link org.mule.module.extensions.internal.config.ParseDelegate}
     * should be use to parse a particular type
     *
     * @since 3.6.0
     */
    private static class ParseDelegateResolver extends BaseDataQualifierVisitor
    {

        private ParseDelegate delegate;

        @Override
        protected void defaultOperation()
        {
            delegate = new TextParseDelegate();
        }

        @Override
        public void onBean()
        {
            delegate = new BeanParseDelegate();
        }

        private ParseDelegate getDelegate()
        {
            return delegate;
        }
    }

    /**
     * Implementation of {@link org.mule.module.extensions.internal.BaseDataQualifierVisitor}
     * that selects the {@link org.mule.extensions.introspection.api.DataType} to be used.
     * By default, it returns the same one that was provided in the constructor, but in some
     * cases like in lists, then a generic type might be chosen if available
     *
     * @since 3.6.0
     */
    private static class DataTypeSelector extends BaseDataQualifierVisitor
    {

        private DataType dataType;

        private DataTypeSelector(DataType dataType)
        {
            this.dataType = dataType;
            dataType.getQualifier().accept(this);
        }

        @Override
        public void onList()
        {
            if (dataType.getGenericTypes().length > 0)
            {
                dataType = dataType.getGenericTypes()[0];
            }
        }

        private DataType select()
        {
            return dataType;
        }
    }

    /**
     * Implementation of {@link org.mule.module.extensions.internal.config.ParseDelegate}
     * which returns the text content of the given element
     *
     * @since 3.6.0
     */
    private static class TextParseDelegate extends ParseDelegate
    {

        @Override
        Object parse(Element element)
        {
            return element.getTextContent();
        }
    }

    /**
     * Implementation of {@link org.mule.module.extensions.internal.config.ParseDelegate}
     * which parses the element as a java bean
     */
    private static class BeanParseDelegate extends ParseDelegate
    {

        @Override
        Object parse(Element element)
        {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(dataType.getRawType());

            if (parser.recurseBeanProperties(dataType.getRawType(), builder, element))
            {
                return builder.getBeanDefinition();
            }

            return null;
        }
    }
}
