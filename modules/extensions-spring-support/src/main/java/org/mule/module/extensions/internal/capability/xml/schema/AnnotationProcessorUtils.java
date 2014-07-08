/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml.schema;

import com.google.common.collect.ImmutableMap;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.apache.commons.lang.StringUtils;

final class AnnotationProcessorUtils
{

    private AnnotationProcessorUtils()
    {
    }

    static Map<String, VariableElement> getFieldsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotation)
    {
        ImmutableMap.Builder<String, VariableElement> fields = ImmutableMap.builder();

        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements()))
        {
            if (variableElement.getAnnotation(annotation) != null)
            {
                fields.put(variableElement.getSimpleName().toString(), variableElement);
            }
        }

        return fields.build();
    }

    static Map<String, ExecutableElement> getMethodsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotation)
    {
        ImmutableMap.Builder<String, ExecutableElement> fields = ImmutableMap.builder();

        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements()))
        {
            if (executableElement.getAnnotation(annotation) != null)
            {
                fields.put(executableElement.getSimpleName().toString(), executableElement);
            }
        }

        return fields.build();
    }


    static String getJavaDocSummary(ProcessingEnvironment processingEnv, Element element)
    {
        String comment = processingEnv.getElementUtils().getDocComment(element);

        if (StringUtils.isBlank(comment))
        {
            return StringUtils.EMPTY;
        }

        comment = comment.trim();

        String parsedComment = "";
        boolean tagsBegan = false;
        StringTokenizer st = new StringTokenizer(comment, "\n\r");
        while (st.hasMoreTokens())
        {
            String nextToken = st.nextToken().trim();
            if (nextToken.startsWith("@"))
            {
                tagsBegan = true;
            }
            if (!tagsBegan)
            {
                parsedComment = parsedComment + nextToken + "\n";
            }
        }

        String strippedComments = "";
        boolean insideTag = false;
        for (int i = 0; i < parsedComment.length(); i++)
        {
            if (parsedComment.charAt(i) == '{' &&
                parsedComment.charAt(i + 1) == '@')
            {
                insideTag = true;
                i++; //skip
                continue;
            }
            else if (parsedComment.charAt(i) == '}' && insideTag)
            {
                insideTag = false;
                continue;
            }

            strippedComments += parsedComment.charAt(i);
        }

        strippedComments = strippedComments.trim();
        while (strippedComments.length() > 0 &&
               strippedComments.charAt(strippedComments.length() - 1) == '\n')
        {
            strippedComments = StringUtils.chomp(strippedComments);
        }

        return strippedComments;
    }

    static Map<String, Element> asMap(Collection<? extends Element> elements)
    {
        ImmutableMap.Builder<String, Element> map = ImmutableMap.builder();
        for (Element element : elements)
        {
            map.put(element.getSimpleName().toString(), element);
        }

        return map.build();
    }
}
