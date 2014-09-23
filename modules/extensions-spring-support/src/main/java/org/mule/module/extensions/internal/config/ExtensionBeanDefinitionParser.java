/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.config;

import static org.mule.module.extensions.internal.util.MuleExtensionUtils.isExpression;
import org.mule.api.lifecycle.Disposable;
import org.mule.api.lifecycle.Initialisable;
import org.mule.config.spring.MuleHierarchicalBeanDefinitionParserDelegate;
import org.mule.extensions.introspection.api.DataQualifierVisitor;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.BaseDataQualifierVisitor;
import org.mule.module.extensions.internal.capability.xml.schema.model.SchemaConstants;
import org.mule.module.extensions.internal.introspection.ImmutableDataType;
import org.mule.module.extensions.internal.introspection.SimpleTypeDataQualifierVisitor;
import org.mule.module.extensions.internal.runtime.DefaultObjectBuilder;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;
import org.mule.module.extensions.internal.runtime.resolver.CachingValueResolverWrapper;
import org.mule.module.extensions.internal.runtime.resolver.CollectionValueResolver;
import org.mule.module.extensions.internal.runtime.resolver.EvaluateAndTransformValueResolver;
import org.mule.module.extensions.internal.runtime.resolver.ObjectBuilderValueResolver;
import org.mule.module.extensions.internal.runtime.resolver.RegistryLookupValueResolver;
import org.mule.module.extensions.internal.runtime.resolver.StaticValueResolver;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.module.extensions.internal.util.NameUtils;
import org.mule.util.TemplateParser;
import org.mule.util.ValueHolder;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Base implementation of {@link org.springframework.beans.factory.xml.BeanDefinitionParser}
 * for all parsers capable of handling objects described by the extensions introspection API
 *
 * @since 3.7.0
 */
abstract class ExtensionBeanDefinitionParser implements BeanDefinitionParser
{

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss";
    private static final String CALENDAR_FORMAT = "yyyy-MM-dd'T'hh:mm:ssX";

    private TemplateParser parser = TemplateParser.createMuleStyleParser();
    private ConversionService conversionService = new DefaultConversionService();

    protected boolean hasAttribute(Element element, String attributeName)
    {
        String value = element.getAttribute(attributeName);
        return !StringUtils.isBlank(value);
    }

    private ValueResolver parseCollectionAsInnerElement(Element collectionElement,
                                                        String childElementName,
                                                        DataType collectionType)
    {
        final DataType itemsType = collectionType.getGenericTypes().length > 0 ? collectionType.getGenericTypes()[0] : ImmutableDataType.of(Object.class);
        final List<ValueResolver> resolvers = new LinkedList<>();

        for (final Element item : DomUtils.getChildElementsByTagName(collectionElement, childElementName))
        {
            DataQualifierVisitor visitor = new BaseDataQualifierVisitor()
            {
                @Override
                public void onPojo()
                {
                    resolvers.add(new ObjectBuilderValueResolver(recursePojoProperties(itemsType.getRawType(), item)));
                }

                @Override
                protected void defaultOperation()
                {
                    String value = item.getAttribute(SchemaConstants.ATTRIBUTE_NAME_VALUE);
                    resolvers.add(getResolverFromValue(value, itemsType));
                }
            };

            itemsType.getQualifier().accept(visitor);
        }

        return CollectionValueResolver.of((Class<? extends Collection>) collectionType.getRawType(), resolvers);
    }

    private ValueResolver parseCollection(Element element,
                                          String fieldName,
                                          String parentElementName,
                                          String childElementName,
                                          Object defaultValue,
                                          DataType collectionDataType)
    {
        ValueResolver resolver = getResolverFromAttribute(element, fieldName, collectionDataType, defaultValue);
        if (resolver == null)
        {
            Element collectionElement = DomUtils.getChildElementByTagName(element, parentElementName);
            if (collectionElement != null)
            {
                resolver = parseCollectionAsInnerElement(collectionElement, childElementName, collectionDataType);
            }
            else
            {
                resolver = new StaticValueResolver(defaultValue);
            }
        }

        return resolver;
    }

    private ValueResolver getResolverFromAttribute(Element element, String attributeName, DataType expectedDataType, Object defaultValue)
    {
        return getResolverFromValue(getAttributeValue(element, attributeName, defaultValue), expectedDataType);
    }

    private Object getAttributeValue(Element element, String attributeName, Object defaultValue)
    {
        return hasAttribute(element, attributeName)
               ? element.getAttribute(attributeName)
               : defaultValue;
    }

    private ValueResolver getResolverFromValue(final Object value, final DataType expectedDataType)
    {
        if (isExpression(value, parser))
        {
            return new EvaluateAndTransformValueResolver((String) value, expectedDataType);
        }

        if (value != null)
        {
            final ValueHolder<ValueResolver> resolverValueHolder = new ValueHolder<>();
            DataQualifierVisitor visitor = new SimpleTypeDataQualifierVisitor()
            {

                @Override
                protected void onSimpleType()
                {
                    if (conversionService.canConvert(value.getClass(), expectedDataType.getRawType()))
                    {
                        resolverValueHolder.set(new StaticValueResolver(conversionService.convert(value, expectedDataType.getRawType())));
                    }
                    else
                    {
                        defaultOperation();

                    }
                }

                @Override
                protected void defaultOperation()
                {
                    resolverValueHolder.set(new CachingValueResolverWrapper(new RegistryLookupValueResolver(value.toString())));
                }
            };

            expectedDataType.getQualifier().accept(visitor);
            return resolverValueHolder.get();
        }

        return null;
    }

    /**
     * parses a pojo which type is described by {@code pojoType},
     * recursively moving through the pojo's properties.
     *
     * @param element           the XML element which has the bean as a child
     * @param fieldName         the name of the field in which the parsed pojo is going to be assigned
     * @param parentElementName the name of the the bean's top level XML element
     * @param pojoType          a {@link org.mule.extensions.introspection.api.DataType} describing the bean's type
     * @return a {@link org.springframework.beans.factory.config.BeanDefinition} if the bean could be parsed, {@code null}
     * if the bean is not present on the XML definition
     */
    private ValueResolver parsePojo(Element element,
                                    String fieldName,
                                    String parentElementName,
                                    DataType pojoType,
                                    Object defaultValue)
    {
        ValueResolver resolver = getResolverFromAttribute(element, fieldName, pojoType, defaultValue);

        if (resolver != null)
        {
            return resolver;
        }

        element = DomUtils.getChildElementByTagName(element, parentElementName);

        if (element == null)
        {
            return new StaticValueResolver(null);
        }

        return new ObjectBuilderValueResolver(recursePojoProperties(pojoType.getRawType(), element));
    }

    private ObjectBuilder recursePojoProperties(Class<?> declaringClass, Element element)
    {
        ObjectBuilder builder = new DefaultObjectBuilder();
        builder.setPrototypeClass(declaringClass);

        for (Map.Entry<Method, DataType> entry : IntrospectionUtils.getSettersDataTypes(declaringClass).entrySet())
        {
            Method setter = entry.getKey();

            if (IntrospectionUtils.isIgnored(setter))
            {
                continue;
            }

            String parameterName = NameUtils.getFieldNameFromSetter(setter.getName());
            DataType dataType = entry.getValue();

            ValueResolver resolver = getResolverFromAttribute(element, parameterName, dataType, null);

            if (resolver == null)
            {
                parameterName = NameUtils.hyphenize(parameterName);
                Element childElement = DomUtils.getChildElementByTagName(element, parameterName);
                if (childElement != null)
                {
                    ObjectBuilder childBuilder = recursePojoProperties(dataType.getRawType(), childElement);
                    resolver = new ObjectBuilderValueResolver(childBuilder);
                }
            }

            if (resolver != null)
            {
                builder.addProperty(setter, resolver);
            }
        }

        return builder;
    }

    private Date doParseDate(Element element,
                             String attributeName,
                             String parseFormat,
                             Object defaultValue)
    {

        Object value = getAttributeValue(element, attributeName, defaultValue);

        if (value == null)
        {
            return null;
        }

        if (value instanceof String)
        {
            SimpleDateFormat format = new SimpleDateFormat(parseFormat);
            try
            {
                return format.parse((String) value);
            }
            catch (ParseException e)
            {
                throw new IllegalArgumentException(String.format("Could not transform value '%s' into a Date using pattern %s", value, parseFormat));
            }
        }

        if (value instanceof Date)
        {
            return (Date) value;
        }

        throw new IllegalArgumentException(
                String.format("Could not transform value of type '%s' to Date", value != null ? value.getClass().getName() : "null"));
    }

    protected void applyLifecycle(BeanDefinitionBuilder builder)
    {
        Class<?> declaringClass = builder.getBeanDefinition().getBeanClass();
        if (Initialisable.class.isAssignableFrom(declaringClass))
        {
            builder.setInitMethodName(Initialisable.PHASE_NAME);
        }

        if (Disposable.class.isAssignableFrom(declaringClass))
        {
            builder.setDestroyMethodName(Disposable.PHASE_NAME);
        }
    }


    private ValueResolver parseCalendar(Element element, String attributeName, DataType dataType, Object defaultValue)
    {
        Object value = getAttributeValue(element, attributeName, defaultValue);
        if (isExpression(value, parser))
        {
            return new EvaluateAndTransformValueResolver((String) value, dataType);
        }

        Date date = doParseDate(element, attributeName, CALENDAR_FORMAT, defaultValue);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return new StaticValueResolver(calendar);
    }

    private ValueResolver parseDate(Element element, String attributeName, DataType dataType, Object defaultValue)
    {
        Object value = getAttributeValue(element, attributeName, defaultValue);
        if (isExpression(value, parser))
        {
            return new EvaluateAndTransformValueResolver((String) value, dataType);
        }
        else
        {
            return new StaticValueResolver(doParseDate(element, attributeName, DATE_FORMAT, defaultValue));
        }
    }

    protected void setNoRecurseOnDefinition(BeanDefinition definition)
    {
        definition.setAttribute(MuleHierarchicalBeanDefinitionParserDelegate.MULE_NO_RECURSE, Boolean.TRUE);
    }

    protected ValueResolver parseParameter(Element element, ExtensionParameter parameter)
    {
        return parseElement(element, parameter.getName(), parameter.getType(), parameter.getDefaultValue());
    }

    private ValueResolver parseElement(final Element element,
                                       final String fieldName,
                                       final DataType dataType,
                                       final Object defaultValue)
    {
        final String hyphenizedFieldName = NameUtils.hyphenize(fieldName);
        final String singularName = NameUtils.singularize(hyphenizedFieldName);
        final ValueHolder<ValueResolver> resolverReference = new ValueHolder<>();

        DataQualifierVisitor visitor = new BaseDataQualifierVisitor()
        {

            /**
             * An attribute of a supported or unknown type
             */
            @Override
            public void defaultOperation()
            {
                resolverReference.set(getResolverFromValue(getAttributeValue(element, fieldName, defaultValue), dataType));
            }

            /**
             * A collection type. Might be defined in an inner element or referenced
             * from an attribute
             */
            @Override
            public void onList()
            {
                resolverReference.set(parseCollection(element, fieldName, hyphenizedFieldName, singularName, defaultValue, dataType));
            }

            @Override
            public void onPojo()
            {
                resolverReference.set(parsePojo(element, fieldName, hyphenizedFieldName, dataType, defaultValue));
            }

            @Override
            public void onDate()
            {
                resolverReference.set(parseDate(element, fieldName, dataType, defaultValue));
            }

            @Override
            public void onDateTime()
            {
                if (Calendar.class.isAssignableFrom(dataType.getRawType()))
                {
                    resolverReference.set(parseCalendar(element, fieldName, dataType, defaultValue));
                }
                else
                {
                    onDate();
                }
            }
        };

        dataType.getQualifier().accept(visitor);
        return resolverReference.get();
    }
}
