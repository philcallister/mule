/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.capability.xml;

import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.capability.XmlCapability;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.module.extensions.HeisenbergModule;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.capability.xml.schema.SchemaGenerator;
import org.mule.module.extensions.internal.introspection.DefaultExtensionDescriber;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class SchemaGeneratorTestCase extends AbstractMuleTestCase
{

    private SchemaGenerator generator;

    @Before
    public void before()
    {
        generator = new SchemaGenerator();
    }


    @Test
    public void generate()
    {
        ExtensionBuilder builder = DefaultExtensionBuilder.newBuilder();
        new DefaultExtensionDescriber().describe(HeisenbergModule.class, builder);
        Extension extension = builder.build();
        XmlCapability capability = extension.getCapabilities(XmlCapability.class).iterator().next();

        String schema = generator.generate(extension, capability);

        //TODO: assertions. For practical reasons, the assertions on this test will only be coded
        // once the definition parser is ready
        System.out.println(schema);

    }


}
