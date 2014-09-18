/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.mule.extensions.introspection.api.DataQualifier.LIST;
import static org.mule.extensions.introspection.api.DataQualifier.OPERATION;
import static org.mule.module.extensions.internal.util.IntrospectionUtils.isDynamic;
import static org.mule.module.extensions.internal.util.IntrospectionUtils.isIgnored;
import static org.mule.module.extensions.internal.util.IntrospectionUtils.isRequired;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.BaseDataQualifierVisitor;
import org.mule.module.extensions.internal.capability.xml.schema.model.Annotation;
import org.mule.module.extensions.internal.capability.xml.schema.model.Attribute;
import org.mule.module.extensions.internal.capability.xml.schema.model.ComplexContent;
import org.mule.module.extensions.internal.capability.xml.schema.model.ComplexType;
import org.mule.module.extensions.internal.capability.xml.schema.model.Documentation;
import org.mule.module.extensions.internal.capability.xml.schema.model.Element;
import org.mule.module.extensions.internal.capability.xml.schema.model.ExplicitGroup;
import org.mule.module.extensions.internal.capability.xml.schema.model.ExtensionType;
import org.mule.module.extensions.internal.capability.xml.schema.model.FormChoice;
import org.mule.module.extensions.internal.capability.xml.schema.model.GroupRef;
import org.mule.module.extensions.internal.capability.xml.schema.model.Import;
import org.mule.module.extensions.internal.capability.xml.schema.model.LocalComplexType;
import org.mule.module.extensions.internal.capability.xml.schema.model.LocalSimpleType;
import org.mule.module.extensions.internal.capability.xml.schema.model.NoFixedFacet;
import org.mule.module.extensions.internal.capability.xml.schema.model.NumFacet;
import org.mule.module.extensions.internal.capability.xml.schema.model.ObjectFactory;
import org.mule.module.extensions.internal.capability.xml.schema.model.Pattern;
import org.mule.module.extensions.internal.capability.xml.schema.model.Restriction;
import org.mule.module.extensions.internal.capability.xml.schema.model.Schema;
import org.mule.module.extensions.internal.capability.xml.schema.model.SchemaConstants;
import org.mule.module.extensions.internal.capability.xml.schema.model.SchemaTypeConversion;
import org.mule.module.extensions.internal.capability.xml.schema.model.SimpleContent;
import org.mule.module.extensions.internal.capability.xml.schema.model.SimpleExtensionType;
import org.mule.module.extensions.internal.capability.xml.schema.model.SimpleType;
import org.mule.module.extensions.internal.capability.xml.schema.model.TopLevelComplexType;
import org.mule.module.extensions.internal.capability.xml.schema.model.TopLevelElement;
import org.mule.module.extensions.internal.capability.xml.schema.model.TopLevelSimpleType;
import org.mule.module.extensions.internal.capability.xml.schema.model.Union;
import org.mule.module.extensions.internal.introspection.ImmutableDataType;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.module.extensions.internal.util.NameUtils;
import org.mule.util.ArrayUtils;
import org.mule.util.StringUtils;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Builder class to generate a XSD schema that describes a
 * {@link org.mule.extensions.introspection.api.Extension}
 *
 * @since 3.7.0
 */
public class SchemaBuilder
{

    private Set<DataType> registeredEnums;
    private Map<DataType, ComplexTypeHolder> registeredComplexTypesHolders;
    private Schema schema;
    private ObjectFactory objectFactory;


    private SchemaBuilder()
    {
        registeredEnums = new HashSet<>();
        objectFactory = new ObjectFactory();
        registeredComplexTypesHolders = new HashMap<>();
    }

    public static SchemaBuilder newSchema(String targetNamespace)
    {
        SchemaBuilder builder = new SchemaBuilder();
        builder.schema = new Schema();
        builder.schema.setTargetNamespace(targetNamespace);
        builder.schema.setElementFormDefault(FormChoice.QUALIFIED);
        builder.schema.setAttributeFormDefault(FormChoice.UNQUALIFIED);
        builder.importXmlNamespace()
                .importSpringFrameworkNamespace()
                .importMuleNamespace();

        return builder;
    }

    public Schema getSchema()
    {
        return schema;
    }

    private SchemaBuilder importXmlNamespace()
    {
        Import xmlImport = new Import();
        xmlImport.setNamespace(SchemaConstants.XML_NAMESPACE);
        schema.getIncludeOrImportOrRedefine().add(xmlImport);
        return this;
    }

    private SchemaBuilder importSpringFrameworkNamespace()
    {
        Import springFrameworkImport = new Import();
        springFrameworkImport.setNamespace(SchemaConstants.SPRING_FRAMEWORK_NAMESPACE);
        springFrameworkImport.setSchemaLocation(SchemaConstants.SPRING_FRAMEWORK_SCHEMA_LOCATION);
        schema.getIncludeOrImportOrRedefine().add(springFrameworkImport);
        return this;
    }

    private SchemaBuilder importMuleNamespace()
    {
        Import muleSchemaImport = new Import();
        muleSchemaImport.setNamespace(SchemaConstants.MULE_NAMESPACE);
        muleSchemaImport.setSchemaLocation(SchemaConstants.MULE_SCHEMA_LOCATION);
        schema.getIncludeOrImportOrRedefine().add(muleSchemaImport);
        return this;
    }

    public Schema registerSimpleTypes()
    {
        registerType(schema, "integerType", SchemaConstants.INTEGER);
        registerType(schema, "longType", SchemaConstants.LONG);
        registerType(schema, "booleanType", SchemaConstants.BOOLEAN);
        registerType(schema, "decimalType", SchemaConstants.DECIMAL);
        registerType(schema, "floatType", SchemaConstants.FLOAT);
        registerType(schema, "doubleType", SchemaConstants.DOUBLE);
        registerType(schema, "dateTimeType", SchemaConstants.DATETIME);
        registerType(schema, "byteType", SchemaConstants.BYTE);
        registerType(schema, "anyUriType", SchemaConstants.ANYURI);
        registerType(schema, "charType", SchemaConstants.STRING, 1, 1);

        return schema;
    }

    private void registerType(Schema schema, String name, QName base)
    {
        registerType(schema, name, base, -1, -1);
    }

    private void registerType(Schema schema, String name, QName base, int minlen, int maxlen)
    {
        registerType(schema, name, base, minlen, maxlen, SchemaConstants.DEFAULT_PATTERN);
    }

    private void registerType(Schema schema, String name, QName base, int minlen, int maxlen, String pattern)
    {
        SimpleType simpleType = new TopLevelSimpleType();
        simpleType.setName(name);
        Union union = new Union();
        simpleType.setUnion(union);

        union.getSimpleType().add(createSimpleType(base, minlen, maxlen, pattern));
        union.getSimpleType().add(createExpressionAndPropertyPlaceHolderSimpleType());

        schema.getSimpleTypeOrComplexTypeOrGroup().add(simpleType);
    }

    private LocalSimpleType createSimpleType(QName base, int minlen, int maxlen, String pattern)
    {
        LocalSimpleType simpleType = new LocalSimpleType();
        Restriction restriction = new Restriction();
        restriction.setBase(base);

        if (minlen != -1)
        {
            NumFacet minLenFacet = new NumFacet();
            minLenFacet.setValue(Integer.toString(minlen));
            JAXBElement<NumFacet> element = objectFactory.createMinLength(minLenFacet);
            restriction.getFacets().add(element);
        }

        if (maxlen != -1)
        {
            NumFacet maxLenFacet = new NumFacet();
            maxLenFacet.setValue(Integer.toString(maxlen));
            JAXBElement<NumFacet> element = objectFactory.createMaxLength(maxLenFacet);
            restriction.getFacets().add(element);
        }

        if (!SchemaConstants.DEFAULT_PATTERN.equals(pattern))
        {
            Pattern xmlPattern = objectFactory.createPattern();
            xmlPattern.setValue(pattern);
            restriction.getFacets().add(xmlPattern);
        }

        simpleType.setRestriction(restriction);

        return simpleType;
    }

    public SchemaBuilder registerConfigElement(final ExtensionConfiguration configuration)
    {
        Map<QName, String> otherAttributes = new HashMap<>();
        final ExtensionType config = registerExtension(configuration.getName(), otherAttributes);
        config.getAttributeOrAttributeGroup().add(createNameAttribute());

        final ExplicitGroup all = new ExplicitGroup();
        config.setSequence(all);


        for (final ExtensionParameter parameter : configuration.getParameters())
        {
            parameter.getType().getQualifier().accept(new BaseDataQualifierVisitor()
            {

                private boolean forceOptional = false;

                @Override
                public void onList()
                {
                    forceOptional = true;
                    defaultOperation();
                    generateCollectionElement(all, parameter, true);
                }

                @Override
                public void onPojo()
                {
                    boolean describable = forceOptional = IntrospectionUtils.isDescribable(configuration.getDeclaringClass(), parameter);

                    defaultOperation();

                    if (describable)
                    {
                        registerComplexTypeChildElement(all,
                                                        parameter.getName(),
                                                        parameter.getDescription(),
                                                        parameter.getType(),
                                                        isRequired(parameter, forceOptional));
                    }
                }

                @Override
                protected void defaultOperation()
                {
                    config.getAttributeOrAttributeGroup().add(createAttribute(parameter, isRequired(parameter, forceOptional)));
                }
            });
        }

        config.setAnnotation(createDocAnnotation(configuration.getDescription()));

        if (all.getParticle().size() == 0)
        {
            config.setSequence(null);
        }

        return this;
    }

    private Attribute createNameAttribute()
    {
        return createAttribute(SchemaConstants.ATTRIBUTE_NAME_NAME, ImmutableDataType.of(String.class), true, false);
    }

    public SchemaBuilder registerOperation(ExtensionOperation operation)
    {
        String typeName = StringUtils.capitalize(operation.getName()) + SchemaConstants.TYPE_SUFFIX;
        registerProcessorElement(operation.getName(), typeName, operation.getDescription());
        registerProcessorType(typeName, operation);

        return this;
    }

    /**
     * Registers a pojo type creating a base complex type and a substitutable
     * top level type while assigning it a name. This method will not register
     * the same type twice even if requested to
     *
     * @param type        a {@link org.mule.extensions.introspection.api.DataType} referencing a pojo type
     * @param description the type's description
     * @return the reference name of the complexType
     */
    private String registerPojoType(DataType type, String description)
    {
        ComplexTypeHolder alreadyRegisteredType = registeredComplexTypesHolders.get(type);
        if (alreadyRegisteredType != null)
        {
            return alreadyRegisteredType.getComplexType().getName();
        }

        registerBasePojoType(type, description);
        registerPojoGlobalElement(type, description);

        return getBaseTypeName(type);
    }

    private String getBaseTypeName(DataType type)
    {
        return type.getName();
    }

    private TopLevelComplexType registerBasePojoType(DataType type, String description)
    {
        final TopLevelComplexType complexType = new TopLevelComplexType();
        registeredComplexTypesHolders.put(type, new ComplexTypeHolder(complexType, type));

        complexType.setName(type.getName());
        complexType.setAnnotation(createDocAnnotation(description));

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);

        final ExtensionType extension = new ExtensionType();
        extension.setBase(SchemaConstants.MULE_ABSTRACT_EXTENSION_TYPE);
        complexContent.setExtension(extension);

        final ExplicitGroup all = new ExplicitGroup();
        extension.setSequence(all);

        for (Map.Entry<Method, DataType> entry : IntrospectionUtils.getSettersDataTypes(type.getRawType()).entrySet())
        {
            final Method method = entry.getKey();
            if (isIgnored(method))
            {
                continue;
            }

            final String name = NameUtils.getFieldNameFromSetter(method.getName());
            final DataType methodType = entry.getValue();
            final boolean required = isRequired(method);
            final boolean dynamic = isDynamic(method);

            methodType.getQualifier().accept(new BaseDataQualifierVisitor()
            {

                @Override
                public void onList()
                {
                    generateCollectionElement(all, name, EMPTY, methodType, required);
                }

                @Override
                public void onOperation()
                {
                    generateNestedProcessorElement(all, name, EMPTY, required);
                }

                @Override
                public void onPojo()
                {
                    registerComplexTypeChildElement(all, name, EMPTY, methodType, false);
                }

                @Override
                protected void defaultOperation()
                {
                    Attribute attribute = createAttribute(name, methodType, required, dynamic);
                    extension.getAttributeOrAttributeGroup().add(attribute);
                }
            });
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);
        return complexType;
    }

    public SchemaBuilder registerEnums()
    {
        for (DataType enumToBeRegistered : registeredEnums)
        {
            registerEnum(schema, enumToBeRegistered);
        }

        return this;
    }

    private void registerEnum(Schema schema, DataType enumType)
    {
        TopLevelSimpleType enumSimpleType = new TopLevelSimpleType();
        enumSimpleType.setName(enumType.getName() + SchemaConstants.ENUM_TYPE_SUFFIX);

        Union union = new Union();
        union.getSimpleType().add(createEnumSimpleType(enumType));
        union.getSimpleType().add(createExpressionAndPropertyPlaceHolderSimpleType());
        enumSimpleType.setUnion(union);

        schema.getSimpleTypeOrComplexTypeOrGroup().add(enumSimpleType);
    }

    private LocalSimpleType createExpressionAndPropertyPlaceHolderSimpleType()
    {
        LocalSimpleType expression = new LocalSimpleType();
        Restriction restriction = new Restriction();
        expression.setRestriction(restriction);
        restriction.setBase(SchemaConstants.MULE_PROPERTY_PLACEHOLDER_TYPE);

        return expression;
    }

    private LocalSimpleType createEnumSimpleType(DataType enumType)
    {
        LocalSimpleType enumValues = new LocalSimpleType();
        Restriction restriction = new Restriction();
        enumValues.setRestriction(restriction);
        restriction.setBase(SchemaConstants.STRING);


        Class<? extends Enum> enumClass = (Class<? extends Enum>) enumType.getRawType();

        for (Enum value : enumClass.getEnumConstants())
        {
            NoFixedFacet noFixedFacet = objectFactory.createNoFixedFacet();
            noFixedFacet.setValue(value.name());

            JAXBElement<NoFixedFacet> enumeration = objectFactory.createEnumeration(noFixedFacet);
            enumValues.getRestriction().getFacets().add(enumeration);
        }

        return enumValues;
    }

    private void registerComplexTypeChildElement(ExplicitGroup all,
                                                 String name,
                                                 String description,
                                                 DataType type,
                                                 boolean required)
    {
        name = NameUtils.hyphenize(name);

        // this top level element is for declaring the object inside a config or operation
        TopLevelElement objectElement = new TopLevelElement();
        objectElement.setName(name);
        objectElement.setMinOccurs(required ? BigInteger.ONE : BigInteger.ZERO);
        objectElement.setMaxOccurs("1");
        objectElement.setComplexType(newLocalComplexTypeWithBase(type, description));
        objectElement.setAnnotation(createDocAnnotation(description));

        all.getParticle().add(objectFactory.createElement(objectElement));
    }

    private void registerPojoGlobalElement(DataType type, String description)
    {
        TopLevelElement objectElement = new TopLevelElement();
        objectElement.setName(NameUtils.hyphenize(type.getRawType().getSimpleName()));

        LocalComplexType complexContent = newLocalComplexTypeWithBase(type, description);
        complexContent.getComplexContent().getExtension().getAttributeOrAttributeGroup().add(createNameAttribute());
        objectElement.setComplexType(complexContent);

        objectElement.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_EXTENSION);
        objectElement.setAnnotation(createDocAnnotation(description));

        schema.getSimpleTypeOrComplexTypeOrGroup().add(objectElement);
    }

    private LocalComplexType newLocalComplexTypeWithBase(DataType type, String description)
    {
        LocalComplexType objectComplexType = new LocalComplexType();
        objectComplexType.setComplexContent(new ComplexContent());
        objectComplexType.getComplexContent().setExtension(new ExtensionType());
        objectComplexType.getComplexContent().getExtension().setBase(
                new QName(schema.getTargetNamespace(), registerPojoType(type, description))
        ); // base to the pojo type

        return objectComplexType;
    }

    private ExtensionType registerExtension(String name, Map<QName, String> otherAttributes)
    {
        LocalComplexType complexType = new LocalComplexType();

        Element extension = new TopLevelElement();
        extension.setName(name);
        extension.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_EXTENSION);
        extension.setComplexType(complexType);

        extension.getOtherAttributes().putAll(otherAttributes);

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(SchemaConstants.MULE_ABSTRACT_EXTENSION_TYPE);
        complexContent.setExtension(complexContentExtension);

        schema.getSimpleTypeOrComplexTypeOrGroup().add(extension);

        return complexContentExtension;
    }

    private Attribute createAttribute(ExtensionParameter parameter, boolean required)
    {
        return createAttribute(parameter.getName(), parameter.getDescription(), parameter.getType(), required, parameter.isDynamic());
    }

    private Attribute createAttribute(String name, DataType type, boolean required, boolean dynamic)
    {
        return createAttribute(name, EMPTY, type, required, dynamic);
    }


    private Attribute createAttribute(final String name, String description, final DataType type, boolean required, final boolean dynamic)
    {
        final Attribute attribute = new Attribute();
        attribute.setUse(required ? SchemaConstants.USE_REQUIRED : SchemaConstants.USE_OPTIONAL);
        attribute.setAnnotation(createDocAnnotation(description));

        type.getQualifier().accept(new BaseDataQualifierVisitor()
        {

            @Override
            public void onEnum()
            {
                attribute.setName(name);
                attribute.setType(new QName(schema.getTargetNamespace(), type.getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
                registeredEnums.add(type);
            }

            @Override
            protected void defaultOperation()
            {
                attribute.setName(name);

                if (dynamic)
                {
                    attribute.setType(SchemaConstants.EXPRESSION);
                }
                else if (isTypeSupported(type))
                {
                    attribute.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), type.getName()));
                }
                else
                {
                    attribute.setType(SchemaConstants.STRING);
                }
            }
        });

        return attribute;
    }

    private void generateCollectionElement(ExplicitGroup all, ExtensionParameter parameter, boolean forceOptional)
    {
        boolean required = isRequired(parameter, forceOptional);
        generateCollectionElement(all, parameter.getName(), parameter.getDescription(), parameter.getType(), required);
    }

    private void generateCollectionElement(ExplicitGroup all, String name, String description, DataType type, boolean required)
    {
        name = NameUtils.hyphenize(name);

        BigInteger minOccurs = required ? BigInteger.ONE : BigInteger.ZERO;
        String collectionName = NameUtils.hyphenize(NameUtils.singularize(name));
        LocalComplexType collectionComplexType = generateCollectionComplexType(collectionName, description, type);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(name);
        collectionElement.setMinOccurs(minOccurs);
        collectionElement.setMaxOccurs("1");
        collectionElement.setAnnotation(createDocAnnotation(description));
        all.getParticle().add(objectFactory.createElement(collectionElement));

        collectionElement.setComplexType(collectionComplexType);
    }

    private LocalComplexType generateCollectionComplexType(String name, final String description, final DataType type)
    {
        final LocalComplexType collectionComplexType = new LocalComplexType();
        final ExplicitGroup sequence = new ExplicitGroup();
        collectionComplexType.setSequence(sequence);

        final TopLevelElement collectionItemElement = new TopLevelElement();
        collectionItemElement.setName(name);
        collectionItemElement.setMinOccurs(BigInteger.ZERO);
        collectionItemElement.setMaxOccurs(SchemaConstants.UNBOUNDED);

        final DataType genericType = getGenericType(type);
        genericType.getQualifier().accept(new BaseDataQualifierVisitor()
        {

            @Override
            public void onPojo()
            {
                //registerPojoType(genericType, description);
                //ComplexType complexType = registeredComplexTypesHolders.get(genericType).getComplexType();
                //LocalComplexType localComplexType = newLocalComplexTypeWithBase(genericType, EMPTY);
                //
                //localComplexType.setComplexContent(complexType.getComplexContent());
                //localComplexType.setAll(complexType.getAll());
                //localComplexType.setSequence(complexType.getSequence());
                //localComplexType.getAttributeOrAttributeGroup().addAll(complexType.getAttributeOrAttributeGroup());

                collectionItemElement.setComplexType(newLocalComplexTypeWithBase(genericType, description));
            }

            @Override
            protected void defaultOperation()
            {
                collectionItemElement.setComplexType(generateComplexValueType(genericType));
            }
        });

        sequence.getParticle().add(objectFactory.createElement(collectionItemElement));

        return collectionComplexType;
    }

    private LocalComplexType generateComplexValueType(DataType type)
    {
        LocalComplexType complexType = new LocalComplexType();
        SimpleContent simpleContent = new SimpleContent();
        complexType.setSimpleContent(simpleContent);
        SimpleExtensionType simpleContentExtension = new SimpleExtensionType();
        QName extensionBase = SchemaTypeConversion.convertType(schema.getTargetNamespace(), type.getName());
        simpleContentExtension.setBase(extensionBase);
        simpleContent.setExtension(simpleContentExtension);

        Attribute valueAttribute = createAttribute(SchemaConstants.ATTRIBUTE_NAME_VALUE, type, true, true);
        simpleContentExtension.getAttributeOrAttributeGroup().add(valueAttribute);

        return complexType;
    }

    private DataType getGenericType(DataType type)
    {
        return ArrayUtils.isEmpty(type.getGenericTypes()) ? type : type.getGenericTypes()[0];
    }

    private void registerProcessorElement(String name, String typeName, String docText)
    {
        Element element = new TopLevelElement();
        element.setName(NameUtils.hyphenize(name));
        element.setType(new QName(schema.getTargetNamespace(), typeName));
        element.setAnnotation(createDocAnnotation(docText));
        element.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR);
        schema.getSimpleTypeOrComplexTypeOrGroup().add(element);
    }

    private void registerExtendedType(QName base, String name, List<ExtensionParameter> parameters)
    {
        TopLevelComplexType complexType = new TopLevelComplexType();
        complexType.setName(name);

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        final ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(base);
        complexContent.setExtension(complexContentExtension);

        Attribute configAttr = createAttribute(SchemaConstants.ATTRIBUTE_NAME_CONFIG, SchemaConstants.ATTRIBUTE_DESCRIPTION_CONFIG, false, SchemaConstants.EXPRESSION);
        complexContentExtension.getAttributeOrAttributeGroup().add(configAttr);

        final ExplicitGroup all = new ExplicitGroup();
        complexContentExtension.setSequence(all);

        int requiredChildElements = countRequiredChildElements(parameters);

        for (final ExtensionParameter parameter : parameters)
        {
            DataType parameterType = parameter.getType();
            DataQualifier parameterQualifier = parameterType.getQualifier();

            if (requiresChildElements(parameterType))
            {
                if (requiredChildElements == 1)
                {
                    GroupRef groupRef = generateNestedProcessorGroup();
                    complexContentExtension.setGroup(groupRef);
                    complexContentExtension.setAll(null);
                }
                else
                {
                    generateNestedProcessorElement(all, parameter.getName(), EMPTY, parameter.isRequired());
                }
            }
            else
            {
                parameterQualifier.accept(new BaseDataQualifierVisitor()
                {

                    @Override
                    public void onList()
                    {
                        generateCollectionElement(all, parameter, false);
                    }

                    @Override
                    protected void defaultOperation()
                    {
                        complexContentExtension.getAttributeOrAttributeGroup().add(createAttribute(parameter, false));
                    }
                });
            }
        }

        if (all.getParticle().size() == 0)
        {
            complexContentExtension.setSequence(null);
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);
    }

    private int countRequiredChildElements(List<ExtensionParameter> parameters)
    {
        int requiredChildElements = 0;
        for (ExtensionParameter parameter : parameters)
        {
            DataType type = parameter.getType();
            if (requiresChildElements(type))
            {
                requiredChildElements++;
            }
            else if (LIST.equals(type.getQualifier()))
            {
                requiredChildElements++;
            }
        }

        return requiredChildElements;
    }

    private boolean requiresChildElements(DataType type)
    {
        DataType[] genericTypes = type.getGenericTypes();
        DataQualifier qualifier = type.getQualifier();

        return OPERATION.equals(qualifier) ||
               (LIST.equals(qualifier) &&
                !ArrayUtils.isEmpty(genericTypes) &&
                OPERATION.equals(genericTypes[0].getQualifier()));
    }

    private void registerProcessorType(String name, ExtensionOperation operation)
    {
        registerExtendedType(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE, name, operation.getParameters());
    }

    private void generateNestedProcessorElement(ExplicitGroup all, String name, String description, boolean required)
    {
        LocalComplexType collectionComplexType = new LocalComplexType();
        GroupRef group = generateNestedProcessorGroup();
        collectionComplexType.setGroup(group);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(NameUtils.hyphenize(name));
        collectionElement.setMinOccurs(required ? BigInteger.ONE : BigInteger.ZERO);
        collectionElement.setComplexType(collectionComplexType);
        collectionElement.setAnnotation(createDocAnnotation(EMPTY));
        all.getParticle().add(objectFactory.createElement(collectionElement));
    }

    private GroupRef generateNestedProcessorGroup()
    {
        GroupRef group = new GroupRef();
        group.generateNestedProcessorGroup(SchemaConstants.MULE_MESSAGE_PROCESSOR_OR_OUTBOUND_ENDPOINT_TYPE);
        group.setMinOccurs(BigInteger.ZERO);
        group.setMaxOccurs("unbounded");

        return group;
    }

    private Attribute createAttribute(String name, String description, boolean optional, QName type)
    {
        Attribute attr = new Attribute();
        attr.setName(name);
        attr.setUse(optional ? SchemaConstants.USE_OPTIONAL : SchemaConstants.USE_REQUIRED);
        attr.setType(type);

        if (description != null)
        {
            attr.setAnnotation(createDocAnnotation(description));
        }

        return attr;
    }

    private Annotation createDocAnnotation(String content)
    {
        if (StringUtils.isBlank(content))
        {
            return null;
        }

        Annotation annotation = new Annotation();
        Documentation doc = new Documentation();
        doc.getContent().add(content);
        annotation.getAppinfoOrDocumentation().add(doc);
        return annotation;
    }

    private boolean isTypeSupported(DataType type)
    {
        return SchemaTypeConversion.isSupported(type);
    }

    private class ComplexTypeHolder
    {

        private ComplexType complexType;
        private DataType type;

        public ComplexTypeHolder(ComplexType complexType, DataType type)
        {
            this.complexType = complexType;
            this.type = type;
        }

        public ComplexType getComplexType()
        {
            return complexType;
        }

        public void setComplexType(ComplexType complexType)
        {
            this.complexType = complexType;
        }

        public DataType getType()
        {
            return type;
        }

        public void setType(DataType type)
        {
            this.type = type;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof ComplexTypeHolder)
            {
                ComplexTypeHolder other = (ComplexTypeHolder) obj;
                return type.equals(other.getType());
            }

            return false;
        }

        @Override
        public int hashCode()
        {
            return type.hashCode();
        }
    }
}
