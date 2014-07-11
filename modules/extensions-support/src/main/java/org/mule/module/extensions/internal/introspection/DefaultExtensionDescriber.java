/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.module.extensions.internal.introspection.MuleExtensionAnnotationParser.getDefaultValue;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.extensions.introspection.api.ExtensionDescribingContext;
import org.mule.extensions.introspection.api.ExtensionOperationBuilder;
import org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor;
import org.mule.module.extensions.internal.util.IntrospectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;

import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link org.mule.extensions.introspection.api.ExtensionDescriber}
 *
 * @since 3.6.0
 */
public final class DefaultExtensionDescriber implements ExtensionDescriber
{

    private CapabilitiesResolver capabilitiesResolver = new CapabilitiesResolver();
    private Iterator<ExtensionDescriberPostProcessor> postProcessors;

    /**
     * {@inheritDoc}
     */
    @Override
    public void describe(ExtensionDescribingContext context)
    {
        checkArgument(context != null, "context cannot be null");
        checkArgument(context.getExtensionType() != null, "Can't describe a null type");
        checkArgument(context.getExtensionBuilder() != null, "Can't describe with a null builder");

        ExtensionDescriptor descriptor = MuleExtensionAnnotationParser.parseExtensionDescriptor(context.getExtensionType());
        describeExtension(context, descriptor);
        describeConfigurations(context, descriptor);
        describeOperations(context, descriptor);
        describeCapabilities(context);
        applyPostProcessors(context);
    }

    private void describeExtension(ExtensionDescribingContext context, ExtensionDescriptor descriptor)
    {
        context.getExtensionBuilder()
                .setName(descriptor.getName())
                .setDescription(descriptor.getDescription())
                .setVersion(descriptor.getVersion())
                .setMinMuleVersion(descriptor.getMinMuleVersion())
                .setActingClass(context.getExtensionType());
    }


    private void describeConfigurations(ExtensionDescribingContext context, ExtensionDescriptor descriptor)
    {
        ExtensionBuilder builder = context.getExtensionBuilder();

        ExtensionConfigurationBuilder configuration = builder.newConfiguration()
                .setName(descriptor.getConfigElementName())
                .setDescription(descriptor.getDescription());

        builder.addConfiguration(configuration);

        for (Field field : descriptor.getConfigurableFields())
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
    }

    private void describeOperations(ExtensionDescribingContext context, ExtensionDescriptor extension)
    {
        ExtensionBuilder builder = context.getExtensionBuilder();
        for (Method method : extension.getOperationMethods())
        {
            Operation annotation = method.getAnnotation(Operation.class);
            ExtensionOperationBuilder operation = builder.newOperation();
            builder.addOperation(operation);

            operation
                    .setName(resolveOperationName(method, annotation))
                    .setOutputType(IntrospectionUtils.getMethodReturnType(method));

            resolveOperationInputTypes(annotation, operation);
            parseOperationParameters(method, builder, operation, extension);
        }
    }

    private void parseOperationParameters(Method method,
                                          ExtensionBuilder builder,
                                          ExtensionOperationBuilder operation,
                                          ExtensionDescriptor extension)
    {
        List<ParameterDescriptor> descriptors = MuleExtensionAnnotationParser.parseParameter(method, extension);

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
            postProcessors = ServiceRegistry.lookupProviders(ExtensionDescriberPostProcessor.class, getClass().getClassLoader());
        }

        return postProcessors;
    }

    private void describeCapabilities(ExtensionDescribingContext context)
    {
        capabilitiesResolver.resolveCapabilities(context.getExtensionType(), context.getExtensionBuilder());
    }
}
