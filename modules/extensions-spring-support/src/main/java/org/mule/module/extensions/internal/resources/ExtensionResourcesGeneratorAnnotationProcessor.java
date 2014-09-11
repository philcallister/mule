/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.resources;

import static org.mule.util.Preconditions.checkState;
import org.mule.config.SPIServiceRegistry;
import org.mule.extensions.introspection.api.CapabilityAwareBuilder;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.extensions.introspection.api.ExtensionDescribingContext;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.extensions.resources.api.ResourcesGenerator;
import org.mule.module.extensions.internal.ImmutableExtensionDescribingContext;
import org.mule.module.extensions.internal.capability.xml.XmlCapabilityExtractor;
import org.mule.module.extensions.internal.capability.xml.schema.SchemaDocumenterPostProcessor;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.introspection.DefaultExtensionDescriber;
import org.mule.util.ClassUtils;
import org.mule.util.ExceptionUtils;

import com.google.common.collect.ImmutableList;

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

/**
 * Annotation processor that picks up all the extensions annotated with
 * {@link org.mule.extensions.introspection.api.Extension} and use a
 * {@link org.mule.extensions.resources.api.ResourcesGenerator} to generated
 * the required resources.
 * <p/>
 * This annotation processor will automatically generate and package into the output jar
 * the XSD schema, spring bundles and extension registration files
 * necessary for mule to work with this extension.
 * <p/>
 * Depending on the capabilities declared by each extension, some of those resources
 * might or might not be generated
 *
 * @since 3.7.0
 */
@SupportedAnnotationTypes(value = {"org.mule.extensions.api.annotation.Extension"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ExtensionResourcesGeneratorAnnotationProcessor extends AbstractProcessor
{

    public ExtensionResourcesGeneratorAnnotationProcessor()
    {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        log("Starting Resources generator for Extensions");

        ResourcesGenerator generator = new AnnotationProcessorResourceGenerator(processingEnv);
        try
        {
            for (TypeElement extensionElement : findExtensions(roundEnv))
            {
                Extension extension = parseExtension(extensionElement);
                generator.generateFor(extension);
            }

            generator.dumpAll();

            return false;
        }
        catch (Exception e)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                                     String.format("%s\n%s", e.getMessage(), ExceptionUtils.getFullStackTrace(e)));
            throw e;
        }
    }

    private Extension parseExtension(TypeElement extensionElement)
    {
        ExtensionBuilder builder = DefaultExtensionBuilder.newBuilder();
        Class<?> extensionClass = getClass(extensionElement);

        ExtensionDescribingContext context = new ImmutableExtensionDescribingContext(extensionClass, builder);
        context.getCustomParameters().put(SchemaDocumenterPostProcessor.EXTENSION_ELEMENT, extensionElement);
        context.getCustomParameters().put(SchemaDocumenterPostProcessor.PROCESSING_ENVIRONMENT, processingEnv);

        buildExtensionDescriber().describe(context);

        extractXmlCapability(extensionClass, builder);

        return builder.build();
    }

    private ExtensionDescriber buildExtensionDescriber()
    {
        ExtensionDescriber describer = new DefaultExtensionDescriber();
        describer.setServiceRegistry(new SPIServiceRegistry());

        return describer;
    }

    private XmlCapability extractXmlCapability(Class<?> extensionClass, CapabilityAwareBuilder<?, ?> builder)
    {
        XmlCapabilityExtractor extractor = new XmlCapabilityExtractor();
        XmlCapability capability = (XmlCapability) extractor.extractCapability(extensionClass, builder);
        checkState(capability != null, "Could not find xml capability for extension " + extensionClass.getName());

        return capability;
    }


    private List<TypeElement> findExtensions(RoundEnvironment env)
    {
        return ImmutableList.copyOf(ElementFilter.typesIn(env.getElementsAnnotatedWith(org.mule.extensions.api.annotation.Extension.class)));
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
}
