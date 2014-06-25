/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.mule.extensions.introspection.api.DataQualifier.BEAN;
import static org.mule.extensions.introspection.api.DataQualifier.ENUM;
import static org.mule.extensions.introspection.api.DataQualifier.LIST;
import static org.mule.extensions.introspection.api.DataQualifier.MAP;
import static org.mule.extensions.introspection.api.DataQualifier.OPERATION;
import static org.mule.extensions.introspection.api.DataQualifier.STRING;
import org.mule.extensions.api.annotation.param.Ignore;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.capability.xml.schema.model.Annotation;
import org.mule.module.extensions.internal.capability.xml.schema.model.Any;
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
import org.mule.module.extensions.internal.capability.xml.schema.model.NameUtils;
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
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.repackaged.internal.org.springframework.util.ReflectionUtils;
import org.mule.util.ArrayUtils;
import org.mule.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * @since 3.6.0
 */
//TODO: there're A LOT of ifs here... Keeping them just for now since
// they're inherited from devkit. But we should really implement
// visitor or double dispatch on the DatQualifier to avoid this madness
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
        registerType(schema, "decimalType", SchemaConstants.DECIMAL);
        registerType(schema, "floatType", SchemaConstants.FLOAT);
        registerType(schema, "doubleType", SchemaConstants.DOUBLE);
        registerType(schema, "dateTimeType", SchemaConstants.DATETIME);
        registerType(schema, "longType", SchemaConstants.LONG);
        registerType(schema, "byteType", SchemaConstants.BYTE);
        registerType(schema, "booleanType", SchemaConstants.BOOLEAN);
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

    private LocalSimpleType createSimpleType(QName base, int minlen, int maxlen)
    {
        return createSimpleType(base, minlen, maxlen, SchemaConstants.DEFAULT_PATTERN);
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

    public SchemaBuilder registerConfigElement(ExtensionConfiguration configuration)
    {
        Map<QName, String> otherAttributes = new HashMap<>();
        ExtensionType config = registerExtension(configuration.getName(), otherAttributes);
        Attribute nameAttribute = createAttribute(SchemaConstants.ATTRIBUTE_NAME_NAME, true, SchemaConstants.STRING, SchemaConstants.ATTRIBUTE_NAME_NAME_DESCRIPTION);
        config.getAttributeOrAttributeGroup().add(nameAttribute);

        ExplicitGroup all = new ExplicitGroup();
        config.setSequence(all);

        for (ExtensionParameter parameter : configuration.getParameters())
        {
            if (LIST.equals(parameter.getType().getQualifier()))
            {
                generateCollectionElement(all, parameter, false);
            }
            else if (BEAN.equals(parameter.getType().getQualifier()))
            {
                registerComplexTypeChildElement(all,
                                                parameter.getName(),
                                                parameter.getDescription(),
                                                parameter.getType(),
                                                parameter.isRequired());
            }
            else
            {
                config.getAttributeOrAttributeGroup().add(createAttribute(parameter.getName(), parameter.getType(), parameter.isRequired()));
            }
        }

        config.setAnnotation(createDocAnnotation(configuration.getDescription()));

        if (all.getParticle().size() == 0)
        {
            config.setSequence(null);
        }

        return this;
    }

    public SchemaBuilder registerOperation(ExtensionOperation operation)
    {
        String typeName = StringUtils.capitalize(operation.getName()) + SchemaConstants.TYPE_SUFFIX;
        registerProcessorElement(operation.getName(), typeName, operation.getDescription());
        registerProcessorType(typeName, operation);

        return this;
    }

    /**
     * Registers one type creating its complex type and assign it an unique name
     *
     * @param type
     * @return the reference name of the complexType
     */
    private String registerComplexType(DataType type)
    {
        //check if the type is already registered
        if (registeredComplexTypesHolders.containsKey(type))
        {
            return registeredComplexTypesHolders.get(type).getComplexType().getName();
        }

        TopLevelComplexType complexType = new TopLevelComplexType();
        complexType.setName(type.getName());
        registeredComplexTypesHolders.put(type, new ComplexTypeHolder(complexType, type));

        ExplicitGroup all = new ExplicitGroup();

        DataType superclass = type.getSuperclass();
        if (superclass != null)
        {
            String superClassName = registerComplexType(superclass);
            ComplexContent complexContent = new ComplexContent();
            complexType.setComplexContent(complexContent);
            complexType.getComplexContent().setExtension(new ExtensionType());
            complexType.getComplexContent().getExtension().setBase(
                    new QName(schema.getTargetNamespace(), superClassName)
            ); // base to the element type
            complexContent.getExtension().setSequence(all);
        }
        else
        {
            complexType.setSequence(all);
        }

        BeanInfo info;
        try
        {
            info = Introspector.getBeanInfo(type.getRawType());
        }
        catch (IntrospectionException e)
        {
            throw new RuntimeException(String.format("Could not register type for class %s", type.getRawType().getName()), e);
        }

        // use the property descriptors to only get the attributes compliant with the bean contract
        for (PropertyDescriptor property : info.getPropertyDescriptors())
        {
            Field field = ReflectionUtils.findField(type.getRawType(), property.getName());
            if (skipField(field))
            {
                continue;
            }

            DataType fieldType = IntrospectionUtils.getFieldDataType(field);
            DataQualifier fieldTypeQualifier = fieldType.getQualifier();

            if (LIST.equals(fieldTypeQualifier))
            {
                generateCollectionElement(all, field.getName(), EMPTY, fieldType, isRequired(field));
            }
            else if (OPERATION.equals(fieldTypeQualifier))
            {
                generateNestedProcessorElement(all, field.getName(), isRequired(field));
            }
            else if (STRING.equals(fieldType))
            {
                createParameterElement(all,
                                       field.getName(),
                                       EMPTY,
                                       fieldType,
                                       isRequired(field),
                                       EMPTY);
            }
            else if (BEAN.equals(fieldTypeQualifier))
            {
                registerComplexTypeChildElement(all, field.getName(), EMPTY, fieldType, false);
            }
            else
            {
                Attribute attribute = createAttribute(field.getName(), fieldType, false);
                if (fieldType.getSuperclass() != null)
                {
                    complexType.getComplexContent().getExtension().getAttributeOrAttributeGroup().add(attribute);
                }
                else
                {
                    complexType.getAttributeOrAttributeGroup().add(attribute);
                }
            }
        }

        if (type.getSuperclass() == null)
        {
            Attribute ref = createAttribute(SchemaConstants.ATTRIBUTE_NAME_REF, true, SchemaConstants.STRING, "The reference object for this parameter");
            complexType.getAttributeOrAttributeGroup().add(ref);
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);

        return type.getName();
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

    private void registerComplexTypeChildElement(ExplicitGroup all, ExtensionParameter parameter)
    {
        registerComplexTypeChildElement(all,
                                        parameter.getName(),
                                        parameter.getDescription(),
                                        parameter.getType(),
                                        parameter.isRequired());
    }

    private void registerComplexTypeChildElement(ExplicitGroup all,
                                                 String name,
                                                 String description,
                                                 DataType type,
                                                 boolean required)
    {
        LocalComplexType objectComplexType = new LocalComplexType();
        objectComplexType.setComplexContent(new ComplexContent());
        objectComplexType.getComplexContent().setExtension(new ExtensionType());
        objectComplexType.getComplexContent().getExtension().setBase(
                new QName(schema.getTargetNamespace(), registerComplexType(type))
        ); // base to the element type

        TopLevelElement objectElement = new TopLevelElement();
        objectElement.setName(NameUtils.uncamel(name));
        objectElement.setMinOccurs(required ? BigInteger.ONE : BigInteger.ZERO);
        objectElement.setMaxOccurs("1");
        objectElement.setComplexType(objectComplexType);
        objectElement.setAnnotation(createDocAnnotation(description));

        all.getParticle().add(objectFactory.createElement(objectElement));
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

    private Attribute createAttribute(String name, DataType type, boolean required)
    {
        Attribute attribute = new Attribute();
        attribute.setUse(required ? SchemaConstants.USE_REQUIRED : SchemaConstants.USE_OPTIONAL);

        if (isTypeSupported(type))
        {
            attribute.setName(name);
            attribute.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), type.getName()));
        }
        else if (ENUM.equals(type.getQualifier()))
        {
            attribute.setName(name);
            attribute.setType(new QName(schema.getTargetNamespace(), type.getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
            registeredEnums.add(type);
        }
        else
        {
            // non-supported types will get "-ref" so beans can be injected
            attribute.setName(name + SchemaConstants.REF_SUFFIX);
            attribute.setType(SchemaConstants.STRING);
        }

        return attribute;
    }

    private void generateCollectionElement(ExplicitGroup all, ExtensionParameter parameter, boolean forceOptional)
    {
        generateCollectionElement(all,
                                  parameter.getName(),
                                  parameter.getDescription(),
                                  parameter.getType(),
                                  !forceOptional && parameter.isRequired());
    }

    private void generateCollectionElement(ExplicitGroup all, String name, String description, DataType type, boolean required)
    {
        name = NameUtils.uncamel(name);
        BigInteger minOccurs = required ? BigInteger.ONE : BigInteger.ZERO;

        String collectionName = NameUtils.uncamel(NameUtils.singularize(name));
        LocalComplexType collectionComplexType = generateCollectionComplexType(collectionName, type);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(name);
        collectionElement.setMinOccurs(minOccurs);
        collectionElement.setMaxOccurs("1");
        collectionElement.setAnnotation(createDocAnnotation(description));
        all.getParticle().add(objectFactory.createElement(collectionElement));

        collectionElement.setComplexType(collectionComplexType);
    }

    private LocalComplexType generateCollectionComplexType(String name, DataType type)
    {
        LocalComplexType collectionComplexType = new LocalComplexType();
        ExplicitGroup sequence = new ExplicitGroup();
        ExplicitGroup choice = new ExplicitGroup();

        if (MAP.equals(type.getQualifier()))
        {
            collectionComplexType.setChoice(choice);
            choice.getParticle().add(objectFactory.createSequence(sequence));

            Any any = new Any();
            any.setProcessContents(SchemaConstants.LAX);
            any.setMinOccurs(BigInteger.ZERO);
            any.setMaxOccurs(SchemaConstants.UNBOUNDED);

            ExplicitGroup anySequence = new ExplicitGroup();
            anySequence.getParticle().add(any);
            choice.getParticle().add(objectFactory.createSequence(anySequence));
        }
        else if (LIST.equals(type.getQualifier()))
        {
            collectionComplexType.setSequence(sequence);
        }

        TopLevelElement collectionItemElement = new TopLevelElement();
        collectionItemElement.setName(name);
        collectionItemElement.setMinOccurs(BigInteger.ZERO);
        collectionItemElement.setMaxOccurs(SchemaConstants.UNBOUNDED);
        collectionItemElement.setComplexType(generateComplexType(name, type));
        sequence.getParticle().add(objectFactory.createElement(collectionItemElement));

        Attribute ref = createAttribute(SchemaConstants.ATTRIBUTE_NAME_REF, true, SchemaConstants.STRING, "The reference object for this parameter");
        collectionComplexType.getAttributeOrAttributeGroup().add(ref);

        return collectionComplexType;
    }

    private LocalComplexType generateComplexType(String name, DataType type)
    {
        DataQualifier typeQualifier = type.getQualifier();
        if (LIST.equals(typeQualifier))
        {
            if (!ArrayUtils.isEmpty(type.getGenericTypes()))
            {
                DataType genericType = type.getGenericTypes()[0];
                if (isTypeSupported(genericType))
                {
                    return generateComplexTypeWithRef(genericType);
                }
                else if (MAP.equals(genericType.getQualifier()) ||
                         LIST.equals(genericType.getQualifier()))
                {
                    return generateCollectionComplexType(SchemaConstants.INNER_PREFIX + name, genericType);
                }
                else if (ENUM.equals(typeQualifier))
                {
                    return generateEnumComplexType(genericType);
                }
                else
                {
                    return generateExtendedRefComplexType(genericType, SchemaConstants.ATTRIBUTE_NAME_VALUE_REF);
                }
            }
            else
            {
                return generateRefComplexType(SchemaConstants.ATTRIBUTE_NAME_VALUE_REF);
            }
        }
        else if (MAP.equals(typeQualifier))
        {
            DataType[] genericTypes = type.getGenericTypes();

            LocalComplexType mapComplexType = new LocalComplexType();
            Attribute keyAttribute = new Attribute();

            if (!ArrayUtils.isEmpty(genericTypes))
            {
                DataType keyType = genericTypes[0];
                DataQualifier keyQualifier = keyType.getQualifier();

                if (isTypeSupported(keyType))
                {
                    keyAttribute.setName(SchemaConstants.ATTRIBUTE_NAME_KEY);
                    keyAttribute.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), keyType.getName()));
                }
                else if (ENUM.equals(keyQualifier))
                {
                    keyAttribute.setName(SchemaConstants.ATTRIBUTE_NAME_KEY);
                    keyAttribute.setType(new QName(schema.getTargetNamespace(), keyType.getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
                    registeredEnums.add(keyType);
                }
                else
                {
                    keyAttribute.setUse(SchemaConstants.USE_REQUIRED);
                    keyAttribute.setName(SchemaConstants.ATTRIBUTE_NAME_KEY_REF);
                    keyAttribute.setType(SchemaConstants.STRING);
                }

                QName baseType;
                if (genericTypes.length > 1 && isTypeSupported(genericTypes[1]))
                {
                    baseType = SchemaTypeConversion.convertType(schema.getTargetNamespace(), genericTypes[1].getName());
                }
                else
                {
                    baseType = new QName(SchemaConstants.XSD_NAMESPACE, "string", "xs");
                }

                SimpleContent simpleContent = new SimpleContent();
                mapComplexType.setSimpleContent(simpleContent);
                SimpleExtensionType complexContentExtension = new SimpleExtensionType();
                complexContentExtension.setBase(baseType);
                simpleContent.setExtension(complexContentExtension);

                Attribute refAttribute = createAttribute(SchemaConstants.ATTRIBUTE_NAME_VALUE_REF, true, SchemaConstants.STRING, null);
                complexContentExtension.getAttributeOrAttributeGroup().add(refAttribute);
                complexContentExtension.getAttributeOrAttributeGroup().add(keyAttribute);
            }

            return mapComplexType;
        }

        return null;
    }

    private LocalComplexType generateEnumComplexType(DataType type)
    {
        LocalComplexType complexType = new LocalComplexType();
        SimpleContent simpleContent = new SimpleContent();
        complexType.setSimpleContent(simpleContent);
        SimpleExtensionType simpleContentExtension = new SimpleExtensionType();
        simpleContentExtension.setBase(new QName(schema.getTargetNamespace(), type.getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
        simpleContent.setExtension(simpleContentExtension);
        registeredEnums.add(type);

        return complexType;
    }

    private LocalComplexType generateComplexTypeWithRef(DataType type)
    {
        LocalComplexType complexType = new LocalComplexType();
        SimpleContent simpleContent = new SimpleContent();
        complexType.setSimpleContent(simpleContent);
        SimpleExtensionType simpleContentExtension = new SimpleExtensionType();
        QName extensionBase = SchemaTypeConversion.convertType(schema.getTargetNamespace(), type.getName());
        simpleContentExtension.setBase(extensionBase);
        simpleContent.setExtension(simpleContentExtension);

        Attribute refAttribute = createAttribute(SchemaConstants.ATTRIBUTE_NAME_VALUE_REF, true, SchemaConstants.STRING, null);
        simpleContentExtension.getAttributeOrAttributeGroup().add(refAttribute);
        return complexType;
    }

    private LocalComplexType generateExtendedRefComplexType(DataType type, String name)
    {
        LocalComplexType itemComplexType = new LocalComplexType();
        itemComplexType.setComplexContent(new ComplexContent());
        itemComplexType.getComplexContent().setExtension(new ExtensionType());
        itemComplexType.getComplexContent().getExtension().setBase(
                new QName(schema.getTargetNamespace(), registerComplexType(type))
        ); // base to the type type


        Attribute refAttribute = createAttribute(name, true, SchemaConstants.STRING, null);
        itemComplexType.getComplexContent().getExtension().getAttributeOrAttributeGroup().add(refAttribute);
        return itemComplexType;
    }

    private LocalComplexType generateRefComplexType(String name)
    {
        LocalComplexType itemComplexType = new LocalComplexType();
        Attribute refAttribute = createAttribute(name, false, SchemaConstants.STRING, null);
        itemComplexType.getAttributeOrAttributeGroup().add(refAttribute);
        return itemComplexType;
    }

    private void registerProcessorElement(String name, String typeName, String docText)
    {

        Element element = new TopLevelElement();
        element.setName(NameUtils.uncamel(name));
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
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(base);
        complexContent.setExtension(complexContentExtension);

        Attribute configRefAttr = createAttribute(SchemaConstants.ATTRIBUTE_NAME_CONFIG_REF, true, SchemaConstants.STRING, "Specify which configuration to use for this invocation.");
        complexContentExtension.getAttributeOrAttributeGroup().add(configRefAttr);

        ExplicitGroup all = new ExplicitGroup();
        complexContentExtension.setSequence(all);

        int requiredChildElements = countRequiredChildElements(parameters);

        for (ExtensionParameter parameter : parameters)
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
                    generateNestedProcessorElement(all, parameter.getName(), parameter.isRequired());
                }
            }
            else
            {
                if (LIST.equals(parameterQualifier))
                {
                    generateCollectionElement(all, parameter, false);
                }
                else if (isTypeSupported(parameterType) || ENUM.equals(parameterQualifier))
                {
                    if (STRING.equals(parameterQualifier))
                    {
                        createParameterElement(all, parameter);
                    }
                    else
                    {
                        complexContentExtension.getAttributeOrAttributeGroup().add(createParameterAttribute(parameter, false));
                    }
                }
                else if (BEAN.equals(parameterQualifier))
                {
                    registerComplexTypeChildElement(all, parameter);
                }
                else
                {
                    complexContentExtension.getAttributeOrAttributeGroup().add(createParameterAttribute(parameter, false));
                }
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

    private void createParameterElement(ExplicitGroup all, ExtensionParameter parameter)
    {
        createParameterElement(all,
                               parameter.getName(),
                               parameter.getDescription(),
                               parameter.getType(),
                               parameter.isRequired(),
                               parameter.getDefaultValue());
    }

    private void createParameterElement(ExplicitGroup all,
                                        String name,
                                        String description,
                                        DataType type,
                                        boolean required,
                                        Object defaultValue)
    {
        TopLevelElement textElement = new TopLevelElement();
        textElement.setName(name);
        textElement.setMinOccurs(required ? BigInteger.ONE : BigInteger.ZERO);
        textElement.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), type.getName()));
        textElement.setDefault(defaultValue != null ? defaultValue.toString() : EMPTY);
        textElement.setAnnotation(createDocAnnotation(description));
        all.getParticle().add(objectFactory.createElement(textElement));
    }

    private void generateNestedProcessorElement(ExplicitGroup all, String name, boolean required)
    {
        LocalComplexType collectionComplexType = new LocalComplexType();
        GroupRef group = generateNestedProcessorGroup();
        collectionComplexType.setGroup(group);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(NameUtils.uncamel(name));
        collectionElement.setMinOccurs(required ? BigInteger.ONE : BigInteger.ZERO);
        collectionElement.setComplexType(collectionComplexType);
        collectionElement.setAnnotation(createDocAnnotation(EMPTY));
        all.getParticle().add(objectFactory.createElement(collectionElement));

        Attribute attribute = createAttribute("text", true, SchemaConstants.STRING, null);
        collectionComplexType.getAttributeOrAttributeGroup().add(attribute);
    }

    private GroupRef generateNestedProcessorGroup()
    {
        GroupRef group = new GroupRef();
        group.generateNestedProcessorGroup(SchemaConstants.MULE_MESSAGE_PROCESSOR_OR_OUTBOUND_ENDPOINT_TYPE);
        group.setMinOccurs(BigInteger.ZERO);
        group.setMaxOccurs("unbounded");

        return group;
    }

    private Attribute createParameterAttribute(ExtensionParameter parameter, boolean forceOptional)
    {
        return createAttribute(parameter.getName(), parameter.getType(), !forceOptional && parameter.isRequired());
    }

    private Attribute createAttribute(String name, boolean optional, QName type, String description)
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

    private Attribute createAttribute(String name, boolean optional, QName type, String description, String defaultValue)
    {
        Attribute attr = new Attribute();
        attr.setName(name);
        attr.setUse(optional ? SchemaConstants.USE_OPTIONAL : SchemaConstants.USE_REQUIRED);
        attr.setType(type);

        if (description != null)
        {
            attr.setAnnotation(createDocAnnotation(description));
        }

        if (defaultValue != null)
        {
            attr.setDefault(defaultValue);
        }

        return attr;
    }

    private Annotation createDocAnnotation(String content)
    {
        Annotation annotation = new Annotation();
        Documentation doc = new Documentation();
        doc.getContent().add(content);
        annotation.getAppinfoOrDocumentation().add(doc);
        return annotation;
    }

    private boolean isTypeSupported(DataType type)
    {
        return SchemaTypeConversion.isSupported(type.getRawType().getName());
    }

    private boolean skipField(Field field)
    {
        return field == null || field.getAnnotation(Ignore.class) != null;
    }

    private boolean isRequired(Field field)
    {
        return field.getAnnotation(Optional.class) == null;
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
