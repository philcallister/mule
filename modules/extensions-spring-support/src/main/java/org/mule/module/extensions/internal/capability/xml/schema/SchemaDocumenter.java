/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.getFieldsAnnotatedWith;
import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.getJavaDocSummary;
import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.getMethodDocumentation;
import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.getMethodsAnnotatedWith;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.introspection.api.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.api.ExtensionOperationBuilder;
import org.mule.extensions.introspection.api.ExtensionParameterBuilder;
import org.mule.module.extensions.internal.introspection.NavigableExtensionBuilder;
import org.mule.module.extensions.internal.introspection.NavigableExtensionConfigurationBuilder;
import org.mule.module.extensions.internal.introspection.NavigableExtensionOperationBuilder;
import org.mule.module.extensions.internal.introspection.NavigableExtensionParameterBuilder;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Utility class that picks a {@link org.mule.module.extensions.internal.introspection.NavigableExtensionBuilder}
 * on which a {@link org.mule.extensions.introspection.api.Extension} has already been described
 * and enriches such description with the javadocs extracted from the extension's acting classes.
 * <p/>
 * This is necessary because such documentation is not available on runtime, thus this class
 * uses the annotation processor's AST access to extract it
 *
 * @since 3.7.0
 */
final class SchemaDocumenter
{

    private ProcessingEnvironment processingEnv;

    SchemaDocumenter(ProcessingEnvironment processingEnv)
    {
        this.processingEnv = processingEnv;
    }

    void document(NavigableExtensionBuilder builder, TypeElement extensionElement)
    {
        builder.setDescription(getJavaDocSummary(processingEnv, extensionElement));
        documentConfigurations(builder, extensionElement);
        documentOperations(builder, extensionElement);
    }

    private void documentOperations(NavigableExtensionBuilder builder, TypeElement extensionElement)
    {
        final Map<String, ExecutableElement> methods = getMethodsAnnotatedWith(extensionElement, Operation.class);

        for (ExtensionOperationBuilder ob : builder.getOperations())
        {
            NavigableExtensionOperationBuilder operationBuilder = navigable(ob);
            if (operationBuilder == null)
            {
                continue;
            }

            ExecutableElement method = methods.get(operationBuilder.getName());

            if (method == null)
            {
                continue;
            }

            MethodDocumentation documentation = getMethodDocumentation(processingEnv, method);
            operationBuilder.setDescription(documentation.getSummary());
            documentOperationParameters(operationBuilder, documentation);
        }
    }

    private void documentOperationParameters(NavigableExtensionOperationBuilder builder, MethodDocumentation documentation)
    {
        for (ExtensionParameterBuilder pb : builder.getParameters())
        {
            NavigableExtensionParameterBuilder parameterBuilder = navigable(pb);
            if (pb == null)
            {
                continue;
            }

            String description = documentation.getParameters().get(parameterBuilder.getName());
            if (description != null)
            {
                parameterBuilder.setDescription(description);
            }
        }
    }

    private void documentConfigurations(NavigableExtensionBuilder builder, TypeElement extensionElement)
    {
        for (ExtensionConfigurationBuilder cb : builder.getConfigurations())
        {
            NavigableExtensionConfigurationBuilder configurationBuilder = navigable(cb);
            if (configurationBuilder == null)
            {
                continue;
            }

            documentConfigurationParameters(configurationBuilder.getParameters(), extensionElement);
        }
    }

    private void documentConfigurationParameters(Collection<ExtensionParameterBuilder> builders, TypeElement element)
    {
        final Map<String, VariableElement> fields = getFieldsAnnotatedWith(element, Configurable.class);
        while (element != null && !Object.class.getName().equals(element.getQualifiedName().toString()))
        {
            for (ExtensionParameterBuilder pb : builders)
            {
                NavigableExtensionParameterBuilder parameterBuilder = navigable(pb);
                if (parameterBuilder == null)
                {
                    continue;
                }

                VariableElement field = fields.get(parameterBuilder.getName());
                if (field != null)
                {
                    parameterBuilder.setDescription(getJavaDocSummary(processingEnv, field));
                }
            }

            element = (TypeElement) processingEnv.getTypeUtils().asElement(element.getSuperclass());
        }
    }

    private NavigableExtensionConfigurationBuilder navigable(ExtensionConfigurationBuilder builder)
    {
        if (builder instanceof NavigableExtensionConfigurationBuilder)
        {
            return (NavigableExtensionConfigurationBuilder) builder;
        }

        return null;
    }

    private NavigableExtensionParameterBuilder navigable(ExtensionParameterBuilder builder)
    {
        if (builder instanceof NavigableExtensionParameterBuilder)
        {
            return (NavigableExtensionParameterBuilder) builder;
        }

        return null;
    }

    private NavigableExtensionOperationBuilder navigable(ExtensionOperationBuilder builder)
    {
        if (builder instanceof NavigableExtensionOperationBuilder)
        {
            return (NavigableExtensionOperationBuilder) builder;
        }

        return null;
    }
}
