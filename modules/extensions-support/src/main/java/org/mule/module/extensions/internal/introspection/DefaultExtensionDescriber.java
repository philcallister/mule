/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.module.extensions.internal.introspection.MuleExtensionAnnotationParser.getDefaultValue;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.extensions.introspection.api.ExtensionOperationBuilder;
import org.mule.module.extensions.internal.util.IntrospectionUtils;
import org.mule.util.Preconditions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public final class DefaultExtensionDescriber implements ExtensionDescriber
{

    private CapabilitiesResolver capabilitiesResolver = CapabilitiesResolver.newInstance();

    @Override
    public void describe(Class<?> extensionType, ExtensionBuilder builder)
    {
        Preconditions.checkArgument(extensionType != null, "Can't describe a null type");
        ExtensionDescriptor descriptor = MuleExtensionAnnotationParser.parseExtensionDescriptor(extensionType);
        describeExtension(builder, descriptor, extensionType);
        describeConfigurations(builder, descriptor);
        describeOperations(builder, descriptor);
        describeCapabilities(extensionType, builder);
    }

    private void describeExtension(ExtensionBuilder builder, ExtensionDescriptor descriptor, Class<?> extensionType)
    {
        builder.setName(descriptor.getName())
                .setDescription(descriptor.getDescription())
                .setVersion(descriptor.getVersion())
                .setMinMuleVersion(descriptor.getMinMuleVersion())
                .setActingClass(extensionType);
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
            DataType dataType = IntrospectionUtils.getFieldDataType(field);

            configuration.addParameter(builder.newParameter()
                                               .setName(field.getName())
                                               .setType(dataType)
                                               .setDynamic(configurable.isDynamic())
                                               .setRequired(optional == null)
                                               .setDefaultValue(getDefaultValue(optional, dataType)));
        }
    }

    private void describeOperations(ExtensionBuilder builder, ExtensionDescriptor extension)
    {
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

    private void describeCapabilities(Class<?> extensionType, ExtensionBuilder builder)
    {
        capabilitiesResolver.resolveCapabilities(extensionType, builder);
    }
}
