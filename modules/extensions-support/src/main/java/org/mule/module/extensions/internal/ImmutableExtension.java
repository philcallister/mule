/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.mule.module.extensions.internal.MuleExtensionUtils.checkNullOrRepeatedNames;
import static org.mule.module.extensions.internal.MuleExtensionUtils.toClassMap;
import static org.mule.module.extensions.internal.MuleExtensionUtils.toMap;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.NoSuchConfigurationException;
import org.mule.extensions.introspection.api.NoSuchOperationException;

import com.google.common.base.Objects;
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
final class ImmutableExtension extends AbstractImmutableDescribed implements Extension
{

    private final String version;
    private final String minMuleVersion;
    private final Map<String, ExtensionConfiguration> configurations;
    private final Map<String, ExtensionOperation> operations;
    private Map<Class<?>, ?> capabilities;

    protected ImmutableExtension(String name,
                                 String description,
                                 String version,
                                 String minMuleVersion,
                                 List<ExtensionConfiguration> configurations,
                                 List<ExtensionOperation> operations,
                                 Set<Object> capabilities)
    {
        super(name, description);

        checkNullOrRepeatedNames(configurations, "configurations");
        checkNullOrRepeatedNames(operations, "operations");

        checkArgument(!StringUtils.isBlank(version), "version cannot be blank");
        this.version = version;

        checkArgument(!StringUtils.isBlank(minMuleVersion), "minMuleVersion cannot be blank");
        this.minMuleVersion = minMuleVersion;

        this.configurations = toMap(configurations);
        this.operations = toMap(operations);
        this.capabilities = toClassMap(capabilities);
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
    public ExtensionOperation getOperation(String name) throws NoSuchOperationException
    {
        ExtensionOperation extensionOperation = operations.get(name);
        if (extensionOperation == null)
        {
            throw new NoSuchOperationException(this, name);
        }

        return extensionOperation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getCapability(Class<T> capabilityType)
    {
        return (T) capabilities.get(capabilityType);
    }


    /**
     * Defines equality by matching the extension's {@link #getName()} and
     * {@link #getVersion()}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Extension)
        {
            Extension other = (Extension) obj;
            return Objects.equal(getName(), other.getName()) && Objects.equal(getVersion(), other.getVersion());
        }

        return false;
    }

    /**
     * Returns a hash code based on the extension's {@link #getName()} and
     * {@link #getVersion()}
     */
    @Override
    public int hashCode()
    {
        return Objects.hashCode(getName(), getVersion());
    }
}
