/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.asMap;
import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.getFieldsAnnotatedWith;
import static org.mule.module.extensions.internal.capability.xml.schema.AnnotationProcessorUtils.getJavaDocSummary;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

final class SchemaDocumenter
{

    private ProcessingEnvironment processingEnv;

    SchemaDocumenter(ProcessingEnvironment processingEnv)
    {
        this.processingEnv = processingEnv;
    }

    void document(NavigableExtensionBuilder builder, TypeElement extensionElement)
    {
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

            operationBuilder.setDescription(getJavaDocSummary(processingEnv, method));
            documentOperationParameters(operationBuilder, method);
        }
    }

    private void documentOperationParameters(NavigableExtensionOperationBuilder builder, ExecutableElement method)
    {
        final Map<String, Element> attributes = asMap(method.getParameters());
        for (ExtensionParameterBuilder pb : builder.getParameters())
        {
            NavigableExtensionParameterBuilder parameterBuilder = navigable(pb);
            if (pb == null)
            {
                continue;
            }

            Element attribute = attributes.get(parameterBuilder.getName());
            if (attribute != null)
            {
                parameterBuilder.setDescription(getJavaDocSummary(processingEnv, attribute));
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
