/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.introspection;

import static org.mule.module.extensions.internal.util.IntrospectionUtils.checkInstantiable;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.checkNamesClashes;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.checkNullOrRepeatedNames;
import static org.mule.module.extensions.internal.util.MuleExtensionUtils.toMap;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.NoSuchConfigurationException;
import org.mule.extensions.introspection.api.NoSuchOperationException;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Immutable implementation of {@link org.mule.extensions.introspection.api.Extension}
 *
 * @since 1.0
 */
final class ImmutableExtension extends AbstractImmutableCapableDescribed implements Extension
{

    private final String version;
    private final String minMuleVersion;
    private final Class<?> declaringClass;
    private final Map<String, ExtensionConfiguration> configurations;
    private final Map<String, ExtensionOperation> operations;

    protected ImmutableExtension(String name,
                                 String description,
                                 String version,
                                 String minMuleVersion,
                                 Class<?> declaringClass,
                                 List<ExtensionConfiguration> configurations,
                                 List<ExtensionOperation> operations,
                                 Set<Object> capabilities)
    {
        super(name, description, capabilities);

        checkArgument(!name.contains(" "), "Extension name cannot contain spaces");
        checkInstantiable(declaringClass);
        this.declaringClass = declaringClass;

        checkNullOrRepeatedNames(configurations, "configurations");
        checkNullOrRepeatedNames(operations, "operations");
        checkNamesClashes(configurations, operations);
        this.configurations = toMap(configurations);
        this.operations = toMap(operations);

        checkArgument(!StringUtils.isBlank(version), "version cannot be blank");
        this.version = version;

        checkArgument(!StringUtils.isBlank(minMuleVersion), "minMuleVersion cannot be blank");
        this.minMuleVersion = minMuleVersion;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<ExtensionConfiguration> getConfigurations()
    {
        return ImmutableList.copyOf(configurations.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfiguration getConfiguration(String name) throws NoSuchConfigurationException
    {
        ExtensionConfiguration extensionConfiguration = configurations.get(name);
        if (extensionConfiguration == null)
        {
            throw new NoSuchConfigurationException(this, name);
        }

        return extensionConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ExtensionOperation> getOperations()
    {
        return ImmutableList.copyOf(operations.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion()
    {
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMinMuleVersion()
    {
        return minMuleVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass()
    {
        return declaringClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionOperation getOperation(String name) throws NoSuchOperationException
    {
        ExtensionOperation extensionOperation = operations.get(name);
        if (extensionOperation == null)
        {
            throw new NoSuchOperationException(this, name);
        }

        return extensionOperation;
    }
}
