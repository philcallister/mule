/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import org.mule.api.callback.HttpCallback;
import org.mule.module.extensions.internal.capability.xml.schema.model.ExplicitGroup;
import org.mule.module.extensions.internal.capability.xml.schema.model.FormChoice;
import org.mule.module.extensions.internal.capability.xml.schema.model.Import;
import org.mule.module.extensions.internal.capability.xml.schema.model.LocalSimpleType;
import org.mule.module.extensions.internal.capability.xml.schema.model.NumFacet;
import org.mule.module.extensions.internal.capability.xml.schema.model.ObjectFactory;
import org.mule.module.extensions.internal.capability.xml.schema.model.Pattern;
import org.mule.module.extensions.internal.capability.xml.schema.model.Restriction;
import org.mule.module.extensions.internal.capability.xml.schema.model.Schema;
import org.mule.module.extensions.internal.capability.xml.schema.model.SchemaConstants;
import org.mule.module.extensions.internal.capability.xml.schema.model.SimpleType;
import org.mule.module.extensions.internal.capability.xml.schema.model.TopLevelSimpleType;
import org.mule.module.extensions.internal.capability.xml.schema.model.Union;
import org.mule.security.oauth.OnNoTokenPolicy;
import org.mule.util.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.type.TypeMirror;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class SchemaBuilder
{

    private Set<EnumType> registeredEnums;
    private Set<ComplexTypeHolder> registeredComplexTypesHolders;
    private Set<String> registeredMethods;
    private Schema schema;
    private ObjectFactory objectFactory;


    private SchemaBuilder()
    {
        registeredEnums = new HashSet<>();
        registeredMethods = new HashSet<>();
        objectFactory = new ObjectFactory();
        registeredComplexTypesHolders = new HashSet<>();
    }

    public static SchemaBuilder newSchema(String targetNamespace)
    {
        SchemaBuilder builder = new SchemaBuilder();
        builder.schema = new Schema();
        builder.schema.setTargetNamespace(targetNamespace);
        builder.schema.setElementFormDefault(FormChoice.QUALIFIED);
        builder.schema.setAttributeFormDefault(FormChoice.UNQUALIFIED);
        return builder;
    }

    public Schema getSchema()
    {
        return schema;
    }

    public SchemaBuilder importXmlNamespace()
    {
        Import xmlImport = new Import();
        xmlImport.setNamespace(SchemaConstants.XML_NAMESPACE);
        schema.getIncludeOrImportOrRedefine().add(xmlImport);
        return this;
    }

    public SchemaBuilder importSpringFrameworkNamespace()
    {
        Import springFrameworkImport = new Import();
        springFrameworkImport.setNamespace(SchemaConstants.SPRING_FRAMEWORK_NAMESPACE);
        springFrameworkImport.setSchemaLocation(SchemaConstants.SPRING_FRAMEWORK_SCHEMA_LOCATION);
        schema.getIncludeOrImportOrRedefine().add(springFrameworkImport);
        return this;
    }

    public SchemaBuilder importMuleNamespace()
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

    public SchemaBuilder registerConfigElement(Module module, GeneratedClass moduleClass, Context ctx)
    {
        Map<QName, String> otherAttributes = new HashMap<QName, String>();
        otherAttributes.put(SchemaConstants.MULE_DEVKIT_JAVA_CLASS_TYPE, moduleClass.fullName());
        ExtensionType config = registerExtension(module.getConfigElementName(), otherAttributes);
        Attribute nameAttribute = createAttribute(SchemaConstants.ATTRIBUTE_NAME_NAME, true, SchemaConstants.STRING, SchemaConstants.ATTRIBUTE_NAME_NAME_DESCRIPTION);
        config.getAttributeOrAttributeGroup().add(nameAttribute);

        ExplicitGroup all = new ExplicitGroup();
        config.setSequence(all);

        for (Field variable : module.getConfigurableFields())
        {
            if (variable.asType().isCollection())
            {
                generateCollectionElement(all, variable, false);
            }
            else if (variable.asType().isComplexType() && !variable.isRefOnly())
            {
                registerComplexTypeChildElement(all, variable, true);
            }
            else
            {
                config.getAttributeOrAttributeGroup().add(createAttribute(variable, false));
            }
        }

        for (Field variable : module.getInjectFields())
        {
            if (variable.asTypeMirror().toString().equals("org.mule.api.store.ObjectStore"))
            {
                config.getAttributeOrAttributeGroup().add(createAttribute("objectStore-ref", true, SchemaConstants.STRING, variable.getJavaDocSummary()));
            }
        }

        if (module instanceof ManagedConnectionModule)
        {
            // add a configurable argument for each connectivity variable
            for (Parameter parameter : ((ManagedConnectionModule) module).getConnectMethod().getParameters())
            {
                if (parameter.asType().isCollection())
                {
                    generateCollectionElement(all, parameter, true);
                }
                else
                {
                    config.getAttributeOrAttributeGroup().add(createParameterAttribute(parameter, true));
                }
            }

            TopLevelElement poolingProfile = new TopLevelElement();
            poolingProfile.setName(SchemaConstants.CONNECTION_POOLING_PROFILE);
            poolingProfile.setType(SchemaConstants.MULE_POOLING_PROFILE_TYPE);
            poolingProfile.setMinOccurs(BigInteger.ZERO);
            poolingProfile.setAnnotation(createDocAnnotation(SchemaConstants.CONNECTION_POOLING_PROFILE_ELEMENT_DESCRIPTION));

            all.getParticle().add(objectFactory.createElement(poolingProfile));

            TopLevelElement abstractReconnectStrategy = new TopLevelElement();
            abstractReconnectStrategy.setRef(SchemaConstants.MULE_ABSTRACT_RECONNECTION_STRATEGY);
            abstractReconnectStrategy.setMinOccurs(BigInteger.ZERO);
            abstractReconnectStrategy.setAnnotation(createDocAnnotation("Reconnection strategy that defines how Mule should handle a connection failure."));

            all.getParticle().add(objectFactory.createElement(abstractReconnectStrategy));
        }

        // add oauth callback configuration
        if (module instanceof OAuthModule)
        {
            OAuthModule oAuthModule = (OAuthModule) module;
            generateHttpCallbackElement(SchemaConstants.OAUTH_CALLBACK_CONFIG_ELEMENT_NAME, all, oAuthModule);

            if (oAuthModule.getOAuthVersion().equals(OAuthVersion.V10A))
            {
                generateOauthAccessTokenElement(SchemaConstants.OAUTH_SAVE_ACCESS_TOKEN_ELEMENT, SchemaConstants.OAUTH_SAVE_ACCESS_TOKEN_ELEMENT_DESCRIPTION, all);
                generateOauthAccessTokenElement(SchemaConstants.OAUTH_RESTORE_ACCESS_TOKEN_ELEMENT, SchemaConstants.OAUTH_RESTORE_ACCESS_TOKEN_ELEMENT_DESCRIPTION, all);
            }
            else
            {
                generateOAuthStoreConfigElement(all);
            }

            Attribute accessTokenUrlAttribute = createAttribute("accessTokenUrl", true, SchemaConstants.STRING, "The URL defined by the Service Provider to obtain an access token", ((OAuthModule) module).getAccessTokenUrl());
            config.getAttributeOrAttributeGroup().add(accessTokenUrlAttribute);


            Attribute onNoTokenAttribute = createAttribute("onNoToken", true, SchemaConstants.ON_NO_TOKEN_TYPE, "The URL defined by the Service Provider to obtain an access token");
            config.getAttributeOrAttributeGroup().add(onNoTokenAttribute);
            onNoTokenAttribute.setDefault("EXCEPTION");
            onNoTokenAttribute.setType(new QName(schema.getTargetNamespace(), SchemaConstants.ON_NO_TOKEN + SchemaConstants.ENUM_TYPE_SUFFIX));


            Attribute authorizationUrlAttribute = createAttribute("authorizationUrl", true, SchemaConstants.STRING, "The URL defined by the Service Provider where the resource owner will be redirected to grant authorization to the connector", ((OAuthModule) module).getAuthorizationUrl());
            config.getAttributeOrAttributeGroup().add(authorizationUrlAttribute);

            if (((OAuthModule) module).getOAuthVersion() == OAuthVersion.V10A)
            {
                Attribute requestTokenUrlAttribute = createAttribute("requestTokenUrl", true, SchemaConstants.STRING, "The URL defined by the Service Provider used to obtain an un-authorized request token", ((OAuthModule) module).getRequestTokenUrl());
                config.getAttributeOrAttributeGroup().add(requestTokenUrlAttribute);
            }
        }
        if (module.hasProcessorMethodWithParameter(HttpCallback.class))
        {
            generateHttpCallbackElement(SchemaConstants.HTTP_CALLBACK_CONFIG_ELEMENT_NAME, all, null);
        }
        config.setAnnotation(createDocAnnotation(module.getJavaDocSummary()));

        if (all.getParticle().size() == 0)
        {
            config.setSequence(null);
        }
        return this;

    }

    public SchemaBuilder registerProcessorsAndSourcesAndFilters(Module module)
    {
        if (module instanceof OAuthModule)
        {
            // generate an MP to start the OAuth process
            registerProcessorElement(false, "authorize", "AuthorizeType", "Starts OAuth authorization process. It must be called from a flow with an http:inbound-endpoint.");
            registerProcessorElement(false, "unauthorize", "UnauthorizeType", "Unauthorizes the connector, forcing to re-use authorize again before accessing any protected message processor.");
            registerAuthorizeType(module);
            registerUnauthorizeType((OAuthModule) module);
        }

        for (FilterMethod method : module.getFilterMethods())
        {
            String name = method.getName();
            Filter filter = method.getAnnotation(Filter.class);
            if (filter.name().length() > 0)
            {
                name = filter.name();
            }

            if (!registeredMethods.contains(name))
            {
                registeredMethods.add(name);

                String typeName = StringUtils.capitalize(name) + SchemaConstants.TYPE_SUFFIX;
                registerFilterElement(name, typeName, method.getJavaDocSummary());
                registerExtendedType(SchemaConstants.MULE_ABSTRACT_FILTER_TYPE, typeName, method);
            }
        }

        for (ProcessorMethod method : module.getProcessorMethods())
        {
            String name = method.getName();
            Processor processor = method.getAnnotation(Processor.class);
            if (processor.name().length() > 0)
            {
                name = processor.name();
            }

            if (!registeredMethods.contains(name))
            {
                registeredMethods.add(name);

                String typeName = StringUtils.capitalize(name) + SchemaConstants.TYPE_SUFFIX;
                registerProcessorElement(method.isIntercepting(), name, typeName, method.getJavaDocSummary());
                registerProcessorType(method.isIntercepting(), typeName, method);
            }
        }

        for (Method method : module.getSourceMethods())
        {
            String name = method.getName();
            Source source = method.getAnnotation(Source.class);
            if (source.name().length() > 0)
            {
                name = source.name();
            }

            if (!registeredMethods.contains(name))
            {
                registeredMethods.add(name);
                String typeName = StringUtils.capitalize(name) + SchemaConstants.TYPE_SUFFIX;
                registerSourceElement(name, typeName, method);
                registerExtendedType(SchemaConstants.MULE_ABSTRACT_INBOUND_ENDPOINT_TYPE, typeName, method);
            }
        }
        return this;
    }

    private LocalSimpleType createOnNoTokenPolicyType()
    {
        LocalSimpleType enumValues = new LocalSimpleType();
        Restriction restriction = new Restriction();
        enumValues.setRestriction(restriction);
        restriction.setBase(SchemaConstants.STRING);


        NoFixedFacet exceptionPolicy = objectFactory.createNoFixedFacet();
        exceptionPolicy.setValue(OnNoTokenPolicy.EXCEPTION.toString());
        JAXBElement<NoFixedFacet> exceptionEnum = objectFactory.createEnumeration(exceptionPolicy);
        NoFixedFacet noStopPolicy = objectFactory.createNoFixedFacet();
        noStopPolicy.setValue(OnNoTokenPolicy.STOP_FLOW.toString());
        JAXBElement<NoFixedFacet> noStopEnum = objectFactory.createEnumeration(noStopPolicy);


        enumValues.getRestriction().getFacets().add(exceptionEnum);
        enumValues.getRestriction().getFacets().add(noStopEnum);

        return enumValues;
    }

    public SchemaBuilder registerTransformers(Module module)
    {
        for (Method method : module.getTransformerMethods())
        {
            Element transformerElement = new TopLevelElement();
            transformerElement.setName(NameUtils.uncamel(method.getName()));
            transformerElement.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_TRANSFORMER);
            transformerElement.setType(SchemaConstants.MULE_ABSTRACT_TRANSFORMER_TYPE);
            schema.getSimpleTypeOrComplexTypeOrGroup().add(transformerElement);
        }
        return this;
    }

    /**
     * Registers one type creating its complex type and assign it an unique name
     *
     * @param type
     * @return the reference name of the complexType
     */
    private String registerComplexType(Type type)
    {
        TopLevelComplexType complexType = new TopLevelComplexType();
        //TODO: remove all this crap and do it with some nice ood
        //check if the type is already registered
        for (ComplexTypeHolder typeHolder : registeredComplexTypesHolders)
        {
            if (areEquals(type, typeHolder.getType()))
            {
                return typeHolder.getComplexType().getName();
            }
        }

        //check if simple name is available otherwise full name is used
        String registeredName = checkSimpleName(type.getName()) ? type.getName() + SchemaConstants.OBJECT_TYPE_SUFFIX : type.getPackageName() + "." + type.getName() + SchemaConstants.OBJECT_TYPE_SUFFIX;
        complexType.setName(registeredName);
        registeredComplexTypesHolders.add(new ComplexTypeHolder(complexType, type));

        ExplicitGroup all = new ExplicitGroup();
        //complexType.setSequence(all);


        if (type.hasSuperClass())
        {
            String superClassName = registerComplexType(type.getSuperClass());
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

        for (Field field : type.getFields())
        {
            if (skipField(field))
            {
                continue;
            }
            if (field.asType().isCollection())
            {
                generateCollectionElement(all, field, true);
            }
            else if (generateNestedProcessor(field))
            {
                generateNestedProcessorElement(all, field);
            }
            else if (field.isText())
            {
                createParameterElement(all, field);
            }
            else if (field.asType().isComplexType())
            {
                registerComplexTypeChildElement(all, field, true);
            }
            else
            {
                if (type.hasSuperClass())
                {
                    complexType.getComplexContent().getExtension().getAttributeOrAttributeGroup().add(createAttribute(field, true));
                }
                else
                {
                    complexType.getAttributeOrAttributeGroup().add(createAttribute(field, true));
                }
            }
        }

        Attribute ref = createAttribute(SchemaConstants.ATTRIBUTE_NAME_REF, true, SchemaConstants.STRING, "The reference object for this parameter");
        if (type.hasSuperClass())
        {
            //complexType.getComplexContent().getExtension().getAttributeOrAttributeGroup().add(ref);
        }
        else
        {
            complexType.getAttributeOrAttributeGroup().add(ref);
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);

        return registeredName;
    }

    private boolean checkSimpleName(String name)
    {
        assert name != null;
        for (ComplexTypeHolder typeHolder : registeredComplexTypesHolders)
        {
            if (name.equals(typeHolder.getType().getName()))
            {
                return false;
            }
        }
        return true;
    }

    private boolean areEquals(Type type, Type typeHolderType)
    {
        return type.getClassName().equals(typeHolderType.getClassName()) && type.getPackage().equals(typeHolderType.getPackage());
    }


    public SchemaBuilder registerEnums()
    {
        for (EnumType enumToBeRegistered : registeredEnums)
        {
            registerEnum(schema, enumToBeRegistered);
        }
        registerOnNoTokenPolicyType();

        return this;
    }

    private void registerOnNoTokenPolicyType()
    {
        TopLevelSimpleType enumSimpleType = new TopLevelSimpleType();
        enumSimpleType.setName(SchemaConstants.ON_NO_TOKEN + SchemaConstants.ENUM_TYPE_SUFFIX);

        Union union = new Union();
        union.getSimpleType().add(createOnNoTokenPolicyType());
        union.getSimpleType().add(createExpressionAndPropertyPlaceHolderSimpleType());
        enumSimpleType.setUnion(union);

        schema.getSimpleTypeOrComplexTypeOrGroup().add(enumSimpleType);
    }

    private void registerEnum(Schema schema, EnumType enumType)
    {
        TopLevelSimpleType enumSimpleType = new TopLevelSimpleType();
        enumSimpleType.setName(enumType.getName() + SchemaConstants.ENUM_TYPE_SUFFIX);
        enumSimpleType.setAnnotation(createDocAnnotation(enumType.getJavaDocSummary()));

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

    private LocalSimpleType createEnumSimpleType(EnumType enumType)
    {
        LocalSimpleType enumValues = new LocalSimpleType();
        Restriction restriction = new Restriction();
        enumValues.setRestriction(restriction);
        restriction.setBase(SchemaConstants.STRING);

        for (Identifiable identifiable : enumType.getEnumConstants())
        {
            NoFixedFacet noFixedFacet = objectFactory.createNoFixedFacet();
            noFixedFacet.setValue(identifiable.getName());
            noFixedFacet.setAnnotation(createDocAnnotation(identifiable.getJavaDocSummary()));
            JAXBElement<NoFixedFacet> enumeration = objectFactory.createEnumeration(noFixedFacet);
            enumValues.getRestriction().getFacets().add(enumeration);
        }

        return enumValues;
    }


    private void registerComplexTypeChildElement(ExplicitGroup all, Variable variable, boolean forceOptional)
    {


        LocalComplexType objectComplexType = new LocalComplexType();
        objectComplexType.setComplexContent(new ComplexContent());
        objectComplexType.getComplexContent().setExtension(new ExtensionType());
        objectComplexType.getComplexContent().getExtension().setBase(
                new QName(schema.getTargetNamespace(), registerComplexType(variable.asType()))
        ); // base to the element type

        TopLevelElement objectElement = new TopLevelElement();
        objectElement.setName(NameUtils.uncamel(variable.getName()));
        objectElement.setMinOccurs((!forceOptional && !variable.isOptional()) ? BigInteger.ONE : BigInteger.ZERO);
        objectElement.setMaxOccurs("1");
        objectElement.setComplexType(objectComplexType);
        objectElement.setAnnotation(createDocAnnotation(variable.parent().getJavaDocParameterSummary(variable.getName())));
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

    private Attribute createAttribute(Variable variable, boolean forceOptional)
    {
        return createAttribute(variable, forceOptional, false);
    }

    private Attribute createParameterAttribute(Variable variable, boolean forceOptional)
    {
        return createAttribute(variable, forceOptional, true);
    }

    private Attribute createAttribute(Variable variable, boolean forceOptional, boolean isParameter)
    {
        Named named = variable.getAnnotation(Named.class);

        String name = variable.getName();
        if (named != null && named.value().length() > 0)
        {
            name = named.value();
        }
        Attribute attribute = new Attribute();
        String optional = SchemaConstants.USE_OPTIONAL;
        if (!forceOptional && !variable.isOptional())
        {
            optional = SchemaConstants.USE_REQUIRED;
        }
        attribute.setUse(optional);

        if (isParameter && variable.asType().isString() && (variable.hasSizeLimit() || variable.hasPattern() || variable.hasEmailPattern()))
        {
            if (variable.hasPattern() || variable.hasEmailPattern())
            {
                registerType(schema, variable.getName() + SchemaConstants.SIZED_TYPE_SUFFIX, SchemaConstants.STRING,
                             variable.getMinSizeLimit(), variable.getMaxSizeLimit(), variable.getPattern());
            }
            else
            {
                registerType(schema, variable.getName() + SchemaConstants.SIZED_TYPE_SUFFIX, SchemaConstants.STRING,
                             variable.getMinSizeLimit(), variable.getMaxSizeLimit());
            }
            attribute.setName(name);
            attribute.setType(new QName(schema.getTargetNamespace(), variable.getName() + SchemaConstants.SIZED_TYPE_SUFFIX));
        }
        else if (isTypeSupported(variable.asTypeMirror()))
        {
            attribute.setName(name);
            attribute.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), variable.asTypeMirror().toString()));
        }
        else if (variable.asType().isEnum())
        {
            attribute.setName(name);
            attribute.setType(new QName(schema.getTargetNamespace(), variable.asType().getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
            registeredEnums.add((EnumType) variable.asType());
        }
        else if (isParameter && variable.asType().isHttpCallback())
        {
            attribute.setName(NameUtils.uncamel(name) + SchemaConstants.FLOW_REF_SUFFIX);
            attribute.setType(SchemaConstants.STRING);
        }
        else
        {
            // non-supported types will get "-ref" so beans can be injected
            attribute.setName(name + SchemaConstants.REF_SUFFIX);
            attribute.setType(SchemaConstants.STRING);

            // we need to create a sub element here as well
        }

        String doc = isParameter ? variable.parent().getJavaDocParameterSummary(variable.getName()) : variable.getJavaDocSummary();
        attribute.setAnnotation(createDocAnnotation(doc));
        // add default value
        if (variable.hasDefaultValue())
        {
            attribute.setDefault(variable.getDefaultValue());
        }
        return attribute;
    }

    private void generateCollectionElement(ExplicitGroup all, Variable variable, boolean forceOptional)
    {
        String name = NameUtils.uncamel(variable.getName());
        BigInteger minOccurs = BigInteger.ZERO;

        if (!forceOptional && !variable.isOptional())
        {
            minOccurs = BigInteger.ONE;
        }
        String collectionName = NameUtils.uncamel(NameUtils.singularize(name));
        LocalComplexType collectionComplexType = generateCollectionComplexType(collectionName, variable);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(name);
        collectionElement.setMinOccurs(minOccurs);
        collectionElement.setMaxOccurs("1");
        collectionElement.setAnnotation(createDocAnnotation(variable.parent().getJavaDocParameterSummary(variable.getName())));
        all.getParticle().add(objectFactory.createElement(collectionElement));

        collectionElement.setComplexType(collectionComplexType);
    }

    private LocalComplexType generateCollectionComplexType(String name, Identifiable type)
    {
        LocalComplexType collectionComplexType = new LocalComplexType();
        ExplicitGroup sequence = new ExplicitGroup();
        ExplicitGroup choice = new ExplicitGroup();

        if (type.asType().isMap())
        {
            collectionComplexType.setChoice(choice);
            choice.getParticle().add(objectFactory.createSequence(sequence));

            Any any = new Any();
            any.setProcessContents(SchemaConstants.LAX);
            any.setMinOccurs(new BigInteger("0"));
            any.setMaxOccurs(SchemaConstants.UNBOUNDED);

            ExplicitGroup anySequence = new ExplicitGroup();
            anySequence.getParticle().add(any);
            choice.getParticle().add(objectFactory.createSequence(anySequence));
        }
        else if (type.asType().isArrayOrList() || type.asType().isSet())
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

    private LocalComplexType generateComplexType(String name, Identifiable typeMirror)
    {
        if (typeMirror.asType().isArrayOrList() || typeMirror.asType().isSet())
        {
            java.util.List<Type> variableTypeParameters = typeMirror.getTypeArguments();
            if (variableTypeParameters.size() != 0 && variableTypeParameters.get(0) != null)
            {
                Type genericType = variableTypeParameters.get(0);
                if (isTypeSupported(genericType.asTypeMirror()))
                {
                    return generateComplexTypeWithRef(genericType);
                }
                else if (genericType.isArrayOrList() ||
                         genericType.isMap() || genericType.isSet())
                {
                    return generateCollectionComplexType(SchemaConstants.INNER_PREFIX + name, genericType);
                }
                else if (genericType.isEnum())
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
        else if (typeMirror.asType().isMap())
        {
            java.util.List<Type> variableTypeParameters = typeMirror.getTypeArguments();

            LocalComplexType mapComplexType = new LocalComplexType();
            Attribute keyAttribute = new Attribute();
            if (variableTypeParameters.size() > 0 && isTypeSupported(variableTypeParameters.get(0).asTypeMirror()))
            {
                keyAttribute.setName(SchemaConstants.ATTRIBUTE_NAME_KEY);
                keyAttribute.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), variableTypeParameters.get(0).asTypeMirror().toString()));
            }
            else if (variableTypeParameters.size() > 0 && variableTypeParameters.get(0).isEnum())
            {
                keyAttribute.setName(SchemaConstants.ATTRIBUTE_NAME_KEY);
                keyAttribute.setType(new QName(schema.getTargetNamespace(), variableTypeParameters.get(0).getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
                registeredEnums.add((EnumType) variableTypeParameters.get(0).asType());
            }
            else
            {
                keyAttribute.setUse(SchemaConstants.USE_REQUIRED);
                keyAttribute.setName(SchemaConstants.ATTRIBUTE_NAME_KEY_REF);
                keyAttribute.setType(SchemaConstants.STRING);
            }

            QName baseType;
            if (variableTypeParameters.size() > 1 && isTypeSupported(variableTypeParameters.get(1).asTypeMirror()))
            {
                baseType = SchemaTypeConversion.convertType(schema.getTargetNamespace(), variableTypeParameters.get(1).asTypeMirror().toString());
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


            return mapComplexType;
        }

        return null;
    }

    private LocalComplexType generateEnumComplexType(Identifiable genericType)
    {
        LocalComplexType complexType = new LocalComplexType();
        SimpleContent simpleContent = new SimpleContent();
        complexType.setSimpleContent(simpleContent);
        SimpleExtensionType simpleContentExtension = new SimpleExtensionType();
        simpleContentExtension.setBase(new QName(schema.getTargetNamespace(), genericType.getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
        simpleContent.setExtension(simpleContentExtension);
        registeredEnums.add((EnumType) genericType.asType());
        return complexType;
    }

    private LocalComplexType generateComplexTypeWithRef(Identifiable genericType)
    {
        LocalComplexType complexType = new LocalComplexType();
        SimpleContent simpleContent = new SimpleContent();
        complexType.setSimpleContent(simpleContent);
        SimpleExtensionType simpleContentExtension = new SimpleExtensionType();
        QName extensionBase = SchemaTypeConversion.convertType(schema.getTargetNamespace(), genericType.asTypeMirror().toString());
        simpleContentExtension.setBase(extensionBase);
        simpleContent.setExtension(simpleContentExtension);

        Attribute refAttribute = createAttribute(SchemaConstants.ATTRIBUTE_NAME_VALUE_REF, true, SchemaConstants.STRING, null);
        simpleContentExtension.getAttributeOrAttributeGroup().add(refAttribute);
        return complexType;
    }

    private LocalComplexType generateExtendedRefComplexType(Type type, String name)
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

    private void generateHttpCallbackElement(String elementName, ExplicitGroup all, OAuthModule module)
    {
        Attribute domainAttribute = createAttribute(SchemaConstants.DOMAIN_ATTRIBUTE_NAME, true, SchemaConstants.STRING, null);
        Attribute localPortAttribute = createAttribute(SchemaConstants.LOCAL_PORT_ATTRIBUTE_NAME, true, SchemaConstants.STRING, null);
        Attribute remotePortAttribute = createAttribute(SchemaConstants.REMOTE_PORT_ATTRIBUTE_NAME, true, SchemaConstants.STRING, null);
        Attribute asyncAttribute = createAttribute(SchemaConstants.ASYNC_ATTRIBUTE_NAME, true, SchemaConstants.BOOLEAN, null);
        asyncAttribute.setDefault(SchemaConstants.ASYNC_DEFAULT_VALUE.toString());
        Attribute pathAttribute = createAttribute(SchemaConstants.PATH_ATTRIBUTE_NAME, true, SchemaConstants.STRING, null);
        Attribute connectorRefAttribute = createAttribute("connector-ref", true, SchemaConstants.STRING, null);

        TopLevelElement httpCallbackConfig = new TopLevelElement();
        httpCallbackConfig.setName(elementName);
        httpCallbackConfig.setMinOccurs(BigInteger.ONE);
        httpCallbackConfig.setMaxOccurs("1");
        httpCallbackConfig.setAnnotation(createDocAnnotation("Config for http callbacks."));

        ExtensionType extensionType = new ExtensionType();
        extensionType.setBase(SchemaConstants.MULE_ABSTRACT_EXTENSION_TYPE);
        extensionType.getAttributeOrAttributeGroup().add(localPortAttribute);
        extensionType.getAttributeOrAttributeGroup().add(remotePortAttribute);
        extensionType.getAttributeOrAttributeGroup().add(domainAttribute);
        extensionType.getAttributeOrAttributeGroup().add(asyncAttribute);
        extensionType.getAttributeOrAttributeGroup().add(pathAttribute);
        extensionType.getAttributeOrAttributeGroup().add(connectorRefAttribute);

        if (module != null && module.getOAuthVersion() == OAuthVersion.V2)
        {
            Attribute defaultAccessTokenId = createAttribute("defaultAccessTokenId", true, SchemaConstants.STRING, "A Mule Expression to use as access token id");
            extensionType.getAttributeOrAttributeGroup().add(defaultAccessTokenId);
        }

        ComplexContent complexContent = new ComplexContent();
        complexContent.setExtension(extensionType);
        LocalComplexType localComplexType = new LocalComplexType();
        localComplexType.setComplexContent(complexContent);
        httpCallbackConfig.setComplexType(localComplexType);
        all.getParticle().add(objectFactory.createElement(httpCallbackConfig));
    }


    private LocalComplexType generateRefComplexType(String name)
    {
        LocalComplexType itemComplexType = new LocalComplexType();
        Attribute refAttribute = createAttribute(name, false, SchemaConstants.STRING, null);
        itemComplexType.getAttributeOrAttributeGroup().add(refAttribute);
        return itemComplexType;
    }

    private void generateOAuthStoreConfigElement(ExplicitGroup all)
    {
        Attribute objectStoreRefAttribute = new Attribute();
        objectStoreRefAttribute.setUse(SchemaConstants.USE_REQUIRED);
        objectStoreRefAttribute.setName(SchemaConstants.OBJECT_STORE_REF_ATTRIBUTE_NAME);
        objectStoreRefAttribute.setType(SchemaConstants.STRING);

        ExtensionType extensionType = new ExtensionType();
        extensionType.setBase(SchemaConstants.MULE_ABSTRACT_EXTENSION_TYPE);
        extensionType.getAttributeOrAttributeGroup().add(objectStoreRefAttribute);

        ComplexContent complexContent = new ComplexContent();
        complexContent.setExtension(extensionType);

        LocalComplexType localComplexType = new LocalComplexType();
        localComplexType.setComplexContent(complexContent);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(SchemaConstants.OAUTH_STORE_CONFIG_ELEMENT);
        collectionElement.setMinOccurs(BigInteger.ZERO);
        collectionElement.setMaxOccurs("1");
        collectionElement.setComplexType(localComplexType);
        collectionElement.setAnnotation(createDocAnnotation(SchemaConstants.OAUTH_STORE_CONFIG_ELEMENT_DESCRIPTION));
        all.getParticle().add(objectFactory.createElement(collectionElement));
    }

    private void generateOauthAccessTokenElement(String name, String docDescription, ExplicitGroup all)
    {
        LocalComplexType collectionComplexType = new LocalComplexType();
        GroupRef group = generateNestedProcessorGroup();
        collectionComplexType.setGroup(group);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(name);
        collectionElement.setMinOccurs(BigInteger.ZERO);
        collectionElement.setMaxOccurs("1");
        collectionElement.setComplexType(collectionComplexType);
        collectionElement.setAnnotation(createDocAnnotation(docDescription));
        all.getParticle().add(objectFactory.createElement(collectionElement));
    }


    private void registerProcessorElement(boolean intercepting, String name, String typeName, String docText)
    {

        Element element = new TopLevelElement();
        element.setName(NameUtils.uncamel(name));
        element.setType(new QName(schema.getTargetNamespace(), typeName));
        element.setAnnotation(createDocAnnotation(docText));
        element.setSubstitutionGroup(intercepting ? SchemaConstants.MULE_ABSTRACT_INTERCEPTING_MESSAGE_PROCESSOR : SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR);
        schema.getSimpleTypeOrComplexTypeOrGroup().add(element);
    }

    private void registerFilterElement(String name, String typeName, String docText)
    {

        Element element = new TopLevelElement();
        element.setName(NameUtils.uncamel(name));
        element.setType(new QName(schema.getTargetNamespace(), typeName));
        element.setAnnotation(createDocAnnotation(docText));
        element.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_FILTER);
        schema.getSimpleTypeOrComplexTypeOrGroup().add(element);
    }

    private void registerSourceElement(String name, String typeName, Method executableElement)
    {
        Element element = new TopLevelElement();
        element.setName(NameUtils.uncamel(name));
        element.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_INBOUND_ENDPOINT);
        element.setType(new QName(schema.getTargetNamespace(), typeName));
        element.setAnnotation(createDocAnnotation(executableElement.getJavaDocSummary()));
        schema.getSimpleTypeOrComplexTypeOrGroup().add(element);
    }

    private void registerAuthorizeType(Module module)
    {
        TopLevelComplexType complexType = new TopLevelComplexType();
        complexType.setName("AuthorizeType");

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE);
        complexContent.setExtension(complexContentExtension);

        Attribute configRefAttr = createAttribute(SchemaConstants.ATTRIBUTE_NAME_CONFIG_REF, true, SchemaConstants.STRING, "Specify which configuration to use for this invocation.");
        complexContentExtension.getAttributeOrAttributeGroup().add(configRefAttr);

        ExplicitGroup all = new ExplicitGroup();
        complexContentExtension.setSequence(all);

        if (module instanceof OAuthModule && ((OAuthModule) module).getOAuthVersion() == OAuthVersion.V2)
        {
            complexContentExtension.getAttributeOrAttributeGroup().add(createAttribute("state", true,
                                                                                       new QName(SchemaConstants.XSD_NAMESPACE, "string", "xs"), "Any value that you wish to be sent with the callback"));
        }

        if (((OAuthModule) module).getAuthorizationParameters() != null)
        {
            for (OAuthAuthorizationParameter parameter : ((OAuthModule) module).getAuthorizationParameters())
            {
                if (isTypeSupported(parameter.getType().asTypeMirror()) || parameter.getType().isEnum())
                {
                    complexContentExtension.getAttributeOrAttributeGroup().add(
                            createTypeAttribute(parameter.getName(), parameter.getType(), parameter.isOptional(), parameter.getDefaultValue())
                    );
                }
            }
        }

        Attribute accessTokenUrlAttribute = createAttribute("accessTokenUrl", true, SchemaConstants.STRING, "The URL defined by the Service Provider to obtain an access token");
        complexContentExtension.getAttributeOrAttributeGroup().add(accessTokenUrlAttribute);

        Attribute accessTokenIdAttribute = createAttribute("accessTokenId", true, SchemaConstants.STRING, "The Id with which the obtained access token will be stored. If not provided, then it will be the config name");
        complexContentExtension.getAttributeOrAttributeGroup().add(accessTokenIdAttribute);

        Attribute authorizationUrlAttribute = createAttribute("authorizationUrl", true, SchemaConstants.STRING, "The URL defined by the Service Provider where the resource owner will be redirected to grant authorization to the connector");
        complexContentExtension.getAttributeOrAttributeGroup().add(authorizationUrlAttribute);

        if (((OAuthModule) module).getOAuthVersion() == OAuthVersion.V10A)
        {
            Attribute requestTokenUrlAttribute = createAttribute("requestTokenUrl", true, SchemaConstants.STRING, "The URL defined by the Service Provider used to obtain an un-authorized request token");
            complexContentExtension.getAttributeOrAttributeGroup().add(requestTokenUrlAttribute);
        }

        if (all.getParticle().size() == 0)
        {
            complexContentExtension.setSequence(null);
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);

    }

    private Attribute createTypeAttribute(String name, Type type, boolean isOptional, String defaultValue)
    {
        Attribute attribute = new Attribute();
        attribute.setUse(isOptional ? SchemaConstants.USE_OPTIONAL : SchemaConstants.USE_REQUIRED);
        if (isTypeSupported(type.asTypeMirror()))
        {
            attribute.setName(name);
            attribute.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), type.asTypeMirror().toString()));
        }
        else if (type.isEnum())
        {
            attribute.setName(name);
            attribute.setType(new QName(schema.getTargetNamespace(), type.asType().getName() + SchemaConstants.ENUM_TYPE_SUFFIX));
            registeredEnums.add((EnumType) type.asType());
        }
        attribute.setAnnotation(createDocAnnotation(type.getJavaDocSummary()));

        if (StringUtils.isNotBlank(defaultValue))
        {
            attribute.setDefault(defaultValue);
        }
        return attribute;
    }

    private void registerProcessorType(boolean intercepting, String name, Method element)
    {
        if (intercepting)
        {
            registerExtendedType(SchemaConstants.MULE_ABSTRACT_INTERCEPTING_MESSAGE_PROCESSOR_TYPE, name, element);
        }
        else
        {
            registerExtendedType(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE, name, element);
        }
    }

    private void registerExtendedType(QName base, String name, Method<? extends Type> element)
    {
        TopLevelComplexType complexType = new TopLevelComplexType();
        complexType.setName(name);

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(base);
        complexContent.setExtension(complexContentExtension);

        boolean optionalConfig = true;
        if (!(element.parent() instanceof Module))
        {
            optionalConfig = false;
        }

        Attribute configRefAttr = createAttribute(SchemaConstants.ATTRIBUTE_NAME_CONFIG_REF, optionalConfig, SchemaConstants.STRING, "Specify which configuration to use for this invocation.");
        complexContentExtension.getAttributeOrAttributeGroup().add(configRefAttr);

        ExplicitGroup all = new ExplicitGroup();
        complexContentExtension.setSequence(all);

        if (element != null)
        {
            int requiredChildElements = 0;
            for (Parameter variable : element.getParameters())
            {
                if (variable.shouldBeIgnored())
                {
                    continue;
                }
                if (variable.asType().isNestedProcessor() ||
                    (variable.asType().isArrayOrList() &&
                     variable.getTypeArguments().size() > 0 &&
                     variable.getTypeArguments().get(0).isNestedProcessor()))
                {
                    requiredChildElements++;
                }
                else if (variable.asType().isCollection())
                {
                    requiredChildElements++;
                }
            }
            for (Parameter variable : element.getParameters())
            {
                if (variable.shouldBeIgnored())
                {
                    continue;
                }
                if (variable.asType().isNestedProcessor() ||
                    (variable.asType().isArrayOrList() &&
                     variable.getTypeArguments().size() > 0 &&
                     variable.getTypeArguments().get(0).isNestedProcessor()))
                {
                    if (requiredChildElements == 1)
                    {
                        GroupRef groupRef = generateNestedProcessorGroup();
                        complexContentExtension.setGroup(groupRef);
                        complexContentExtension.setAll(null);
                    }
                    else
                    {
                        generateNestedProcessorElement(all, variable);
                    }
                }
                else if (variable.isQuery() && variable.asType().isDsqlQueryObject())
                {
                    generateQueryElement(all, variable, complexContentExtension);

                }
                else if (variable.asType().isCollection())
                {
                    generateCollectionElement(all, variable, false);
                }
                else if (isTypeSupported(variable.asTypeMirror()) || variable.asType().isEnum() || variable.asType().isHttpCallback())
                {
                    if (variable.isText())
                    {
                        createParameterElement(all, variable);
                    }
                    else
                    {
                        complexContentExtension.getAttributeOrAttributeGroup().add(createParameterAttribute(variable, false));
                    }
                }
                else if (variable.asType().isComplexType() && !variable.isRefOnly())
                {
                    registerComplexTypeChildElement(all, variable, false);
                }
                else
                {
                    complexContentExtension.getAttributeOrAttributeGroup().add(createParameterAttribute(variable, false));
                }
            }


            if (element instanceof ProcessorMethod)
            {
                ProcessorMethod method = (ProcessorMethod) element;


                if (method.canBeUsedInConnectionManagement())
                {
                    ConnectMethod connectMethod = method.getManagedConnectionModule().getConnectMethod();
                    if (connectMethod != null)
                    {
                        for (Parameter parameter : connectMethod.getParameters())
                        {
                            if (parameter.asType().isCollection())
                            {
                                generateCollectionElement(all, parameter, true);
                            }
                            else
                            {
                                complexContentExtension.getAttributeOrAttributeGroup().add(createParameterAttribute(parameter, true));
                            }
                        }
                    }
                }

                if (method.canBeUsedInOAuthManagement())
                {
                    complexContentExtension.getAttributeOrAttributeGroup().add(createAttribute("accessTokenId", true, SchemaTypeConversion.convertType(schema.getTargetNamespace(), "java.lang.String"), "The id of the access token that will be used to authenticate the call"));
                }

                if (method.isPaged())
                {
                    addPagingAttributes(method, complexContentExtension.getAttributeOrAttributeGroup());
                }
            }
        }

        if (all.getParticle().size() == 0)
        {
            complexContentExtension.setSequence(null);
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);

    }

    private void addPagingAttributes(ProcessorMethod method, List<Attribute> attributes)
    {
        Paged cfg = method.getPagingAnnotation();
        attributes.add(createAttribute("fetchSize", true, SchemaTypeConversion.convertType(schema.getTargetNamespace(), "int"), "The amount of items to fetch on each invocation to the data source", new Integer(cfg.defaultFetchSize()).toString()));
    }

    private void generateQueryElement(ExplicitGroup all, Parameter variable, ExtensionType complexContentExtension)
    {
        complexContentExtension.getAttributeOrAttributeGroup().add(createStringAttribute(variable));
    }

    private Attribute createStringAttribute(Parameter variable)
    {
        String name = variable.getName();
        Attribute attribute = new Attribute();
        String optional = SchemaConstants.USE_OPTIONAL;
        if (!variable.isOptional())
        {
            optional = SchemaConstants.USE_REQUIRED;
        }
        attribute.setUse(optional);
        attribute.setName(name);
        attribute.setType(SchemaConstants.STRING);

        // add default value
        if (variable.hasDefaultValue())
        {
            attribute.setDefault(variable.getDefaultValue());
        }
        return attribute;
    }


    private void registerUnauthorizeType(OAuthModule module)
    {
        TopLevelComplexType complexType = new TopLevelComplexType();
        complexType.setName("UnauthorizeType");

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE);
        complexContent.setExtension(complexContentExtension);


        complexContentExtension.getAttributeOrAttributeGroup().add(createAttribute("accessTokenId", true, SchemaTypeConversion.convertType(schema.getTargetNamespace(), "java.lang.String"), "The id of the access token that will be used to authenticate the call"));


        Attribute configRefAttr = createAttribute(SchemaConstants.ATTRIBUTE_NAME_CONFIG_REF, true, SchemaConstants.STRING, "Specify which configuration to use for this invocation.");
        complexContentExtension.getAttributeOrAttributeGroup().add(configRefAttr);

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);
    }

    private void createParameterElement(ExplicitGroup all, Variable variable)
    {
        Named named = variable.getAnnotation(Named.class);
        String name = NameUtils.uncamel(variable.getName());
        if (named != null && named.value().length() > 0)
        {
            name = named.value();
        }

        TopLevelElement textElement = new TopLevelElement();
        textElement.setName(name);
        textElement.setMinOccurs(variable.isOptional() ? BigInteger.ZERO : BigInteger.ONE);
        textElement.setType(SchemaTypeConversion.convertType(schema.getTargetNamespace(), variable.asTypeMirror().toString()));
        textElement.setDefault(variable.getDefaultValue());
        textElement.setAnnotation(createDocAnnotation(variable.parent().getJavaDocParameterSummary(variable.getName())));
        all.getParticle().add(objectFactory.createElement(textElement));
    }

    private void generateNestedProcessorElement(ExplicitGroup all, Variable variable)
    {

        LocalComplexType collectionComplexType = new LocalComplexType();
        GroupRef group = generateNestedProcessorGroup();
        collectionComplexType.setGroup(group);

        TopLevelElement collectionElement = new TopLevelElement();
        collectionElement.setName(NameUtils.uncamel(variable.getName()));
        collectionElement.setMinOccurs(variable.isOptional() ? BigInteger.ZERO : BigInteger.ONE);
        collectionElement.setComplexType(collectionComplexType);
        collectionElement.setAnnotation(createDocAnnotation(variable.parent().getJavaDocParameterSummary(variable.getName())));
        all.getParticle().add(objectFactory.createElement(collectionElement));

        Attribute attribute = createAttribute("text", true, SchemaConstants.STRING, null);
        collectionComplexType.getAttributeOrAttributeGroup().add(attribute);
    }


    private GroupRef generateNestedProcessorGroup()
    {
        GroupRef group = new GroupRef();
        group.generateNestedProcessorGroup(SchemaConstants.MULE_MESSAGE_PROCESSOR_OR_OUTBOUND_ENDPOINT_TYPE);
        group.setMinOccurs(BigInteger.valueOf(0L));
        group.setMaxOccurs("unbounded");
        return group;
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

    private boolean isTypeSupported(TypeMirror typeMirror)
    {
        return SchemaTypeConversion.isSupported(typeMirror.toString());
    }

    private boolean skipField(Field field)
    {
        return !field.hasGetter() || !field.hasSetter() || field.shouldBeIgnored();
    }

    private boolean generateNestedProcessor(Field field)
    {
        return field.asType().isNestedProcessor() || (field.asType().isArrayOrList() && field.getTypeArguments().size() > 0
                                                      && field.getTypeArguments().get(0).isNestedProcessor());
    }


    private class ComplexTypeHolder
    {

        private ComplexType complexType;
        private Type type;

        public ComplexTypeHolder(ComplexType complexType, Type type)
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

        public Type getType()
        {
            return type;
        }

        public void setType(Type type)
        {
            this.type = type;
        }
    }
}
