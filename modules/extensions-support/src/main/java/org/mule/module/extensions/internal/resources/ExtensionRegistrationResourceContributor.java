/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.resources;

import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.resources.api.ResourcesGenerator;
import org.mule.extensions.resources.spi.GenerableResourceContributor;

/**
 * Implementation of {@link org.mule.extensions.resources.spi.GenerableResourceContributor}
 * that registers the given extension into a standard SPI service registration file
 * located in the path &quot;extensions/mule.extensions&quot;
 *
 * @since 3.7.0
 */
public class ExtensionRegistrationResourceContributor implements GenerableResourceContributor
{

    @Override
    public void contribute(Extension extension, ResourcesGenerator resourcesGenerator)
    {
        resourcesGenerator.getOrCreateResource("extensions/mule.extensions").getContentBuilder()
                .append(extension.getDeclaringClass().getName())
                .append('\n');
    }
}
