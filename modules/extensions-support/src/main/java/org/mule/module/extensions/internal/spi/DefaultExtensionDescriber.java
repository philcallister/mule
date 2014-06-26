/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.spi;

import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.introspection.spi.ExtensionBuilder;
import org.mule.extensions.introspection.spi.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.spi.ExtensionDescriber;
import org.mule.extensions.introspection.spi.ExtensionOperationBuilder;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.util.Preconditions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public final class DefaultExtensionDescriber implements ExtensionDescriber
{

    private final Class<?> extensionType;
    private CapabilitiesResolver capabilitiesResolver = new CapabilitiesResolver();

    public DefaultExtensionDescriber(Class<?> extensionType)
    {
        Preconditions.checkArgument(extensionType != null, "extensionType cannot be null");
        this.extensionType = extensionType;
    }

    @Override
    public void describe(ExtensionBuilder builder)
    {
        ExtensionDescriptor descriptor = MuleExtensionAnnotationParser.parseExtensionDescriptor(extensionType);
        describeExtension(builder, descriptor);
        describeConfigurations(builder, descriptor);
        describeOperations(builder, descriptor);
        describeCapabilities(builder);
    }

    private void describeExtension(ExtensionBuilder builder, ExtensionDescriptor descriptor)
    {
        builder.setName(descriptor.getName())
                .setDescription(descriptor.getDescription())
                .setVersion(descriptor.getVersion())
                .setMinMuleVersion(descriptor.getMinMuleVersion());
    }


    private void describeConfigurations(ExtensionBuilder builder, ExtensionDescriptor descriptor)
    {
        // TODO: for now we add only one configuration, when we do OAuth or when we resolve the question around config representations this has to change
        ExtensionConfigurationBuilder configuration = builder.newConfiguration()
                .setName(descriptor.getConfigElementName())
                .setDescription(descriptor.getDescription());

        builder.addConfiguration(configuration);

        for (Field field : descriptor.getConfigurableFields())
        {
            Configurable configurable = field.getAnnotation(Configurable.class);
            Optional optional = field.getAnnotation(Optional.class);

            configuration.addParameter(builder.newParameter()
                                               .setName(field.getName())
                                                       //.setDescription(from java doc) TODO: get this from java doc
                                               .setType(IntrospectionUtils.getFieldDataType(field))
                                               .setDynamic(configurable.isDynamic())
                                               .setRequired(optional == null)
                                               .setDefaultValue(optional != null ? optional.defaultValue() : null));
        }
    }

    private void describeOperations(ExtensionBuilder builder, ExtensionDescriptor extension)
    {
        for (Method method : extension.getOperationMethods())
        {
            Operation annotation = method.getAnnotation(Operation.class);
            ExtensionOperationBuilder operation = builder.newOperation();
            builder.addOperation(operation);

            operation.setName(resolveOperationName(method, annotation))
                    //.setDescription() TODO: take this from java doc
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

    private void describeCapabilities(ExtensionBuilder builder)
    {
        capabilitiesResolver.resolveCapabilities(extensionType, builder);
    }
}
