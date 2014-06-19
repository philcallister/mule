/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal;

import static org.mule.util.Preconditions.checkArgument;
import static org.mule.util.Preconditions.checkState;
import org.mule.common.MuleVersion;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.spi.Builder;
import org.mule.extensions.introspection.spi.ExtensionBuilder;
import org.mule.extensions.introspection.spi.ExtensionConfigurationBuilder;
import org.mule.extensions.introspection.spi.ExtensionOperationBuilder;
import org.mule.extensions.introspection.spi.ExtensionParameterBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link org.mule.extensions.introspection.spi.ExtensionBuilder}
 * which builds instances of {@link ImmutableExtension}
 *
 * @since 1.0
 */
public final class DefaultExtensionBuilder implements ExtensionBuilder
{

    private static final String MIN_MULE_VERSION = "3.5.0";
    private static final MuleVersion DEFAULT_MIN_MULE_VERSION = new MuleVersion(MIN_MULE_VERSION);

    private String name;
    private String description;
    private String version;
    private String minMuleVersion;
    private List<ExtensionConfigurationBuilder> configurations = new LinkedList<>();
    private List<Builder<ExtensionOperation>> operations = new LinkedList<>();
    private Set<Object> capabilities = new HashSet<>();

    public static ExtensionBuilder newBuilder()
    {
        return new DefaultExtensionBuilder();
    }

    private DefaultExtensionBuilder()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder setVersion(String version)
    {
        this.version = version;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder setMinMuleVersion(String minMuleVersion)
    {
        this.minMuleVersion = minMuleVersion;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder addConfiguration(ExtensionConfigurationBuilder configuration)
    {
        checkArgument(configuration != null, "cannot add a null configuration builder");
        configurations.add(configuration);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionBuilder addOperation(ExtensionOperationBuilder operation)
    {
        checkArgument(operation != null, "Cannot add a null operation builder");
        operations.add(operation);

        return this;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Object> ExtensionBuilder addCapablity(T capability)
    {
        checkArgument(capability != null, "capability cannot be null");
        capabilities.add(capability);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Extension build()
    {
        validateMuleVersion();
        return new ImmutableExtension(name,
                                          description,
                                          version,
                                          minMuleVersion,
                                          MuleExtensionUtils.build(configurations),
                                          MuleExtensionUtils.build(operations),
                                          capabilities);
    }

    private void validateMuleVersion()
    {
        checkState(!StringUtils.isBlank(minMuleVersion), "minimum Mule version cannot be blank");
        checkState(new MuleVersion(minMuleVersion).atLeast(DEFAULT_MIN_MULE_VERSION),
                   String.format("Minimum Mule version must be at least %s", DEFAULT_MIN_MULE_VERSION.toString()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionConfigurationBuilder newConfiguration()
    {
        return new DefaultExtensionConfigurationBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionOperationBuilder newOperation()
    {
        return new DefaultExtensionOperationBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionParameterBuilder newParameter()
    {
        return new DefaultExtensionParameterBuilder();
    }
}
