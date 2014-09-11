/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.module.extensions.internal.introspection.MuleExtensionAnnotationParser.getDefaultValue;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.config.ServiceRegistry;
import org.mule.config.SPIServiceRegistry;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Configuration;
import org.mule.extensions.api.annotation.Configurations;
import org.mule.extensions.api.annotation.Extension;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.api.annotation.Operations;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.extensions.introspection.api.ExtensionDescribingContext;
import org.mule.extensions.introspection.api.ExtensionOperationBuilder;
import org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link org.mule.extensions.introspection.api.ExtensionDescriber}
 *
 * @since 3.7.0
 */
public final class DefaultExtensionDescriber implements ExtensionDescriber
{

    private ServiceRegistry serviceRegistry;
    private CapabilitiesResolver capabilitiesResolver = new CapabilitiesResolver();
    private Iterator<ExtensionDescriberPostProcessor> postProcessors;

    public DefaultExtensionDescriber()
    {
        setServiceRegistry(new SPIServiceRegistry());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void describe(ExtensionDescribingContext context)
    {
        checkArgument(context != null, "context cannot be null");
        checkArgument(context.getExtensionType() != null, "Can't describe a null type");
        checkArgument(context.getExtensionBuilder() != null, "Can't describe with a null builder");

        Extension extension = MuleExtensionAnnotationParser.getExtension(context.getExtensionType());
        describeExtension(context, extension);
        describeConfigurations(context);
        describeOperations(context);
        describeCapabilities(context);
        applyPostProcessors(context);
    }

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        checkArgument(serviceRegistry != null, "serviceRegistry cannot be null");
        this.serviceRegistry = serviceRegistry;
    }

    private void describeExtension(ExtensionDescribingContext context, Extension extension)
    {
        context.getExtensionBuilder()
                .setName(extension.name())
                .setDescription(extension.description())
                .setVersion(extension.version())
                .setDeclaringClass(context.getExtensionType())
                .setMinMuleVersion(extension.minMuleVersion());
    }


    private void describeConfigurations(ExtensionDescribingContext context)
    {
        Configurations configurations = context.getExtensionType().getAnnotation(Configurations.class);
        if (configurations != null)
        {
            for (Class<?> declaringClass : configurations.value())
            {
                parseConfiguration(context, declaringClass);
            }
        }
        else
        {
            parseConfiguration(context, context.getExtensionType());
        }
    }

    private void parseConfiguration(ExtensionDescribingContext context, Class<?> declaringClass)
    {
        final ExtensionBuilder builder = context.getExtensionBuilder();
        final ExtensionConfigurationBuilder configuration = context.getExtensionBuilder().newConfiguration();

        configuration.setDeclaringClass(declaringClass);

        Configuration configurationAnnotation = declaringClass.getAnnotation(Configuration.class);
        if (configurationAnnotation != null)
        {
            configuration.setName(configurationAnnotation.name())
                    .setDescription(configurationAnnotation.description());
        }
        else
        {
            configuration.setName(Extension.DEFAULT_CONFIG_NAME)
                    .setDescription(Extension.DEFAULT_CONFIG_DESCRIPTION);
        }

        for (Field field : MuleExtensionAnnotationParser.getConfigurableFields(declaringClass))
        {
            Configurable configurable = field.getAnnotation(Configurable.class);
            Optional optional = field.getAnnotation(Optional.class);
            DataType dataType = IntrospectionUtils.getFieldDataType(field);

            configuration.addParameter(builder.newParameter()
                                               .setName(field.getName())
                                               .setType(dataType)
                                               .setDynamic(configurable.isDynamic())
                                               .setRequired(optional == null)
                                               .setDefaultValue(getDefaultValue(optional, dataType)));
        }

        builder.addConfiguration(configuration);
    }

    private void describeOperations(ExtensionDescribingContext context)
    {
        final Class<?> extensionType = context.getExtensionType();

        Operations operations = extensionType.getAnnotation(Operations.class);
        if (operations != null)
        {
            for (Class<?> declaringClass : operations.value())
            {
                parseOperation(context, declaringClass);
            }
        }
        else
        {
            parseOperation(context, extensionType);
        }
    }

    private void parseOperation(ExtensionDescribingContext context, Class<?> declaringClass)
    {
        final ExtensionBuilder builder = context.getExtensionBuilder();

        for (Method method : ClassUtils.getMethodsAnnotatedWith(declaringClass, Operation.class))
        {
            Operation annotation = method.getAnnotation(Operation.class);
            ExtensionOperationBuilder operation = builder.newOperation();

            operation
                    .setName(resolveOperationName(method, annotation))
                    .setDeclaringClass(declaringClass)
                    .setOutputType(IntrospectionUtils.getMethodReturnType(method));

            resolveOperationInputTypes(annotation, operation);
            parseOperationParameters(method, builder, operation);

            builder.addOperation(operation);
        }
    }

    private void parseOperationParameters(Method method,
                                          ExtensionBuilder builder,
                                          ExtensionOperationBuilder operation)
    {
        List<ParameterDescriptor> descriptors = MuleExtensionAnnotationParser.parseParameter(method);

        for (ParameterDescriptor parameterDescriptor : descriptors)
        {
            operation.addParameter(builder.newParameter()
                                           .setType(parameterDescriptor.getType())
                                           .setDynamic(true) //TODO: Add logic to determine this rather than hardcoding true
                                           .setName(parameterDescriptor.getName())
                                           .setDescription(StringUtils.EMPTY)
                                           .setRequired(parameterDescriptor.isRequired())
                                           .setDefaultValue(parameterDescriptor.getDefaultValue())
            );
        }
    }

    private String resolveOperationName(Method method, Operation operation)
    {
        return StringUtils.isBlank(operation.name()) ? method.getName() : operation.name();
    }

    private void resolveOperationInputTypes(Operation operation, ExtensionOperationBuilder builder)
    {
        Class<?>[] acceptedTypes = operation.acceptedPayloadTypes();
        if (acceptedTypes != null)
        {
            for (Class<?> type : acceptedTypes)
            {
                builder.addInputType(IntrospectionUtils.getClassDataType(type));
            }
        }
    }

    private void applyPostProcessors(ExtensionDescribingContext context)
    {
        Iterator<ExtensionDescriberPostProcessor> postProcessors = getPostProcessors();
        while (postProcessors.hasNext())
        {
            postProcessors.next().postProcess(context);
        }
    }

    private synchronized Iterator<ExtensionDescriberPostProcessor> getPostProcessors()
    {
        if (postProcessors == null)
        {
            postProcessors = serviceRegistry.lookupProviders(ExtensionDescriberPostProcessor.class, getClass().getClassLoader());
        }

        return postProcessors;
    }

    private void describeCapabilities(ExtensionDescribingContext context)
    {
        capabilitiesResolver.resolveCapabilities(context.getExtensionType(), context.getExtensionBuilder());
    }
}
