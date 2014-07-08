/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import static org.mule.util.Preconditions.checkState;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.introspection.DefaultExtensionDescriber;
import org.mule.module.extensions.internal.introspection.NavigableExtensionBuilder;
import org.mule.util.ClassUtils;
import org.mule.util.CollectionUtils;
import org.mule.util.ExceptionUtils;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang.StringUtils;

@SupportedAnnotationTypes(value = {"org.mule.extensions.api.annotation.capability.Xml"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SchemaGeneratorAnnotationProcessor extends AbstractProcessor
{

    public SchemaGeneratorAnnotationProcessor()
    {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        log("Starting Schema generator for XML enabled Extensions");

        try
        {
            List<GeneratedSchemaContext> generatedSchemaContexts = new LinkedList<>();

            for (TypeElement extension : findXmlCapableExtensions(roundEnv))
            {
                generatedSchemaContexts.add(generateSchema(extension));
            }

            writeProducts(generatedSchemaContexts);

            return false;

        }
        catch (Exception e)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                                     String.format("%s\n%s", e.getMessage(), ExceptionUtils.getFullStackTrace(e)));
            throw e;
        }
    }

    private void writeProducts(List<GeneratedSchemaContext> generatedSchemaContexts)
    {
        if (generatedSchemaContexts.isEmpty())
        {
            log("No XML capable extensions were found");
            return;
        }

        for (GeneratedSchemaContext schemaContext : generatedSchemaContexts)
        {
            log(String.format("Writing schema and spring bundle for extension %s", schemaContext.getExtension().getName()));
            writeSchema(schemaContext);
        }
    }

    private void writeSchema(GeneratedSchemaContext schemaContext)
    {
        writeResource(schemaContext.getExtension(),
                      "mule-extension-" + schemaContext.getExtension().getName(),
                      schemaContext.getSchema());
    }

    private void writeResource(Extension extension, String filename, String content)
    {
        final String extensionName = extension.getName();
        FileObject file;
        try
        {
            file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,
                                                           StringUtils.EMPTY,
                                                           filename);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not create schema file for extension " + extensionName, e);
        }

        try (OutputStream out = file.openOutputStream())
        {
            out.write(content.getBytes());
            out.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException("could not write schema to file for extension " + extensionName);
        }
    }

    private GeneratedSchemaContext generateSchema(TypeElement extensionElement)
    {
        Extension extension = parseExtension(extensionElement);
        XmlCapability xmlCapability = getXmlCapability(extension);

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        String schema = schemaGenerator.generate(extension, xmlCapability);

        return new GeneratedSchemaContext(extension, xmlCapability.getSchemaLocation(), schema);
    }

    private Extension parseExtension(TypeElement extensionElement)
    {
        ExtensionBuilder builder = DefaultExtensionBuilder.newBuilder();
        new DefaultExtensionDescriber().describe(getClass(extensionElement), builder);

        new SchemaDocumenter(processingEnv).document((NavigableExtensionBuilder) builder, extensionElement);
        return builder.build();
    }

    private XmlCapability getXmlCapability(Extension extension)
    {
        Set<XmlCapability> capabilities = extension.getCapabilities(XmlCapability.class);
        checkState(!CollectionUtils.isEmpty(capabilities), "Could not find xml capability for extension " + extension.getName());

        return capabilities.iterator().next();
    }


    private List<TypeElement> findXmlCapableExtensions(RoundEnvironment env)
    {
        Set<TypeElement> typeElements = ElementFilter.typesIn(env.getElementsAnnotatedWith(Xml.class));
        ImmutableList.Builder<TypeElement> extensions = ImmutableList.builder();
        for (TypeElement type : typeElements)
        {
            if (type.getAnnotation(org.mule.extensions.api.annotation.Extension.class) != null)
            {
                extensions.add(type);
            }
        }
        return extensions.build();
    }

    private void log(String message)
    {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    private Class<?> getClass(TypeElement element)
    {
        final String classname = element.getQualifiedName().toString();
        try
        {
            ClassUtils.loadClass(classname, getClass());
            return ClassUtils.getClass(getClass().getClassLoader(), classname, true);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(
                    String.format("Could not load class %s while trying to generate XML schema", classname), e);
        }
    }

    private class GeneratedSchemaContext
    {

        private Extension extension;
        private String namespace;
        private String schema;

        private GeneratedSchemaContext(Extension extension, String namespace, String schema)
        {
            this.extension = extension;
            this.namespace = namespace;
            this.schema = schema;
        }

        public Extension getExtension()
        {
            return extension;
        }

        public String getNamespace()
        {
            return namespace;
        }

        public String getSchema()
        {
            return schema;
        }
    }
}
