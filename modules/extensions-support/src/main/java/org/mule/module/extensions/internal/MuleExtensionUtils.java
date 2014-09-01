/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.introspection.api.Builder;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.Described;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public final class MuleExtensionUtils
{

    public static void checkNullOrRepeatedNames(Collection<? extends Described> describedCollection, String describedEntityName)
    {
        Set<String> repeatedNames = collectRepeatedNames(describedCollection, describedEntityName);

        if (!repeatedNames.isEmpty())
        {
            throw new IllegalArgumentException(
                    String.format("The following %s were declared multiple times: [%s]",
                                  describedEntityName,
                                  Joiner.on(", ").join(repeatedNames))
            );
        }
    }

    /**
     * Verifies that no operation has the same name as a configuration. This
     * method assumes that the configurations and operations provided have
     * already been verified through {@link #checkNullOrRepeatedNames(java.util.Collection, String)}
     * which means that name clashes can only occur against each other and not within the
     * inner elements of each collection
     */
    public static void checkNamesClashes(Collection<ExtensionConfiguration> configurations, Collection<ExtensionOperation> operations)
    {
        List<Described> all = new ArrayList<>(configurations.size() + operations.size());
        all.addAll(configurations);
        all.addAll(operations);

        Set<String> clashes = collectRepeatedNames(all, "operations");
        if (!clashes.isEmpty())
        {
            throw new IllegalArgumentException(
                    String.format("The following operations have the same name as a declared configuration: [%s]",
                                  Joiner.on(", ").join(clashes))
            );
        }
    }

    private static Set<String> collectRepeatedNames(Collection<? extends Described> describedCollection, String describedEntityName)
    {
        if (CollectionUtils.isEmpty(describedCollection))
        {
            return ImmutableSet.of();
        }

        Multiset<String> names = LinkedHashMultiset.create();

        for (Described described : describedCollection)
        {
            checkArgument(described != null, String.format("A null %s was provided", describedEntityName));
            names.add(described.getName());
        }

        names = Multisets.copyHighestCountFirst(names);
        Set<String> repeatedNames = new HashSet<>();
        for (String name : names)
        {
            if (names.count(name) == 1)
            {
                break;
            }

            repeatedNames.add(name);
        }

        return repeatedNames;
    }

    public static <T> List<T> build(Collection<? extends Builder<T>> builders)
    {
        if (CollectionUtils.isEmpty(builders))
        {
            return Collections.emptyList();
        }

        List<T> built = new ArrayList<>(builders.size());
        for (Builder<T> builder : builders)
        {
            built.add(builder.build());
        }

        return built;
    }

    public static <T> List<T> immutableList(Collection<T> collection)
    {
        return collection != null ? ImmutableList.copyOf(collection) : ImmutableList.<T>of();
    }

    public static <T extends Described> Map<String, T> toMap(List<T> objects)
    {
        ImmutableMap.Builder<String, T> map = ImmutableMap.builder();
        for (T object : objects)
        {
            map.put(object.getName(), object);
        }

        return map.build();
    }

    public static Map<Class<?>, Object> toClassMap(Collection<?> objects)
    {
        ImmutableMap.Builder<Class<?>, Object> map = ImmutableMap.builder();
        for (Object object : objects)
        {
            map.put(object.getClass(), object);
        }

        return map.build();
    }

    public static boolean isListOf(DataType type, DataQualifier of)
    {
        return DataQualifier.LIST.equals(type.getQualifier()) &&
               type.getGenericTypes().length > 0 &&
               of.equals(type.getGenericTypes()[0].getQualifier());
    }

    public static void checkDeclaringClass(Class<?> declaringClass)
    {
        checkArgument(declaringClass != null, "declaringClass cannot be null");
    }
}
