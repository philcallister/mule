/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.resources;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.config.ServiceRegistry;
import org.mule.config.SPIServiceRegistry;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.resources.api.GenerableResource;
import org.mule.extensions.resources.api.ResourcesGenerator;
import org.mule.extensions.resources.spi.GenerableResourceContributor;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Base implementation of {@link org.mule.extensions.resources.api.ResourcesGenerator}
 * that takes care of the basic contract except for actually writing the resources to
 * a persistent store. Implementations are only required to provide that piece of logic
 * by using the {@link #write(org.mule.extensions.resources.api.GenerableResource)}
 * template method
 *
 * @since 3.7.0
 */
public abstract class AbstractResourcesGenerator implements ResourcesGenerator
{

    private Map<String, GenerableResource> resources = new HashMap<>();
    private ServiceRegistry serviceRegistry;

    public AbstractResourcesGenerator()
    {
        setServiceRegistry(new SPIServiceRegistry());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenerableResource getOrCreateResource(String filepath)
    {
        GenerableResource resource = resources.get(filepath);

        if (resource == null)
        {
            resource = new ImmutableGenerableResource(filepath);
            resources.put(filepath, resource);
        }

        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateFor(Extension extension)
    {
        Iterator<GenerableResourceContributor> contributors = serviceRegistry.lookupProviders(GenerableResourceContributor.class, getClass().getClassLoader());

        while (contributors.hasNext())
        {
            contributors.next().contribute(extension, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GenerableResource> dumpAll()
    {
        ImmutableList.Builder<GenerableResource> generatedResources = ImmutableList.builder();
        for (GenerableResource resource : resources.values())
        {
            generatedResources.add(resource);
            write(resource);
        }

        return generatedResources.build();
    }

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        checkArgument(serviceRegistry != null, "serviceRegistry cannot be null");
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Template method to actually write the given
     * {@code resource} to a persistent store
     *
     * @param resource a non null {@link org.mule.extensions.resources.api.GenerableResource}
     */
    protected abstract void write(GenerableResource resource);
}
