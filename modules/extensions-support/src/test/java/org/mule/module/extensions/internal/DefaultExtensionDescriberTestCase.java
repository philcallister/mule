/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mule.extensions.api.annotation.Extension.DEFAULT_CONFIG_NAME;
import static org.mule.extensions.api.annotation.Extension.MIN_MULE_VERSION;
import static org.mule.extensions.introspection.api.DataQualifier.BEAN;
import static org.mule.extensions.introspection.api.DataQualifier.BOOLEAN;
import static org.mule.extensions.introspection.api.DataQualifier.DATE;
import static org.mule.extensions.introspection.api.DataQualifier.DATE_TIME;
import static org.mule.extensions.introspection.api.DataQualifier.DECIMAL;
import static org.mule.extensions.introspection.api.DataQualifier.INTEGER;
import static org.mule.extensions.introspection.api.DataQualifier.LIST;
import static org.mule.extensions.introspection.api.DataQualifier.MAP;
import static org.mule.extensions.introspection.api.DataQualifier.OPERATION;
import static org.mule.extensions.introspection.api.DataQualifier.STRING;
import static org.mule.module.extensions.HeisenbergModule.AGE;
import static org.mule.module.extensions.HeisenbergModule.EXTENSION_DESCRIPTION;
import static org.mule.module.extensions.HeisenbergModule.EXTENSION_NAME;
import static org.mule.module.extensions.HeisenbergModule.EXTENSION_VERSION;
import static org.mule.module.extensions.HeisenbergModule.HEISENBERG;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.Door;
import org.mule.module.extensions.HeisenbergModule;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class DefaultExtensionDescriberTestCase extends AbstractMuleTestCase
{

    private static final String SAY_MY_NAME_OPERATION = "sayMyName";
    private static final String GET_ENEMY_OPERATION = "getEnemy";
    private static final String KILL_OPERATION = "kill";
    private static final String KILL_CUSTOM_OPERATION = "killWithCustomMessage";
    private static final String HIDE_METH_IN_EVENT_OPERATION = "hideMethInEvent";
    private static final String HIDE_METH_IN_MESSAGE_OPERATION = "hideMethInMessage";

    private ExtensionBuilder builder;

    @Before
    public void setUp()
    {
        builder = DefaultExtensionBuilder.newBuilder();
    }

    @Test
    public void describeTestModule() throws Exception
    {
        ExtensionDescriber describer = new DefaultExtensionDescriber();
        describer.describe(HeisenbergModule.class, builder);

        Extension extension = builder.build();
        assertNotNull(extension);

        assertEquals(EXTENSION_NAME, extension.getName());
        assertEquals(EXTENSION_DESCRIPTION, extension.getDescription());
        assertEquals(EXTENSION_VERSION, extension.getVersion());
        assertEquals(MIN_MULE_VERSION, extension.getMinMuleVersion());

        assertTestModuleConfiguration(extension);
        assertTestModuleOperations(extension);

        assertCapabilities(extension);
    }

    private void assertTestModuleConfiguration(Extension extension) throws Exception
    {
        assertEquals(1, extension.getConfigurations().size());
        ExtensionConfiguration conf = extension.getConfigurations().get(0);
        assertSame(conf, extension.getConfiguration(DEFAULT_CONFIG_NAME));

        List<ExtensionParameter> parameters = conf.getParameters();
        assertEquals(11, parameters.size());

        assertParameter(parameters.get(0), "myName", "", String.class, STRING, false, true, HEISENBERG);
        assertParameter(parameters.get(1), "age", "", Integer.class, INTEGER, false, true, AGE);
        assertParameter(parameters.get(2), "enemies", "", List.class, LIST, true, true, null);
        assertParameter(parameters.get(3), "money", "", BigDecimal.class, DECIMAL, true, true, null);
        assertParameter(parameters.get(4), "cancer", "", boolean.class, BOOLEAN, true, true, null);
        assertParameter(parameters.get(4), "cancer", "", boolean.class, BOOLEAN, true, true, null);
        assertParameter(parameters.get(5), "dateOfBirth", "", Date.class, DATE, true, true, null);
        assertParameter(parameters.get(6), "dateOfDeath", "", Calendar.class, DATE_TIME, true, true, null);
        assertParameter(parameters.get(7), "recipe", "", Map.class, MAP, false, true, null);
        assertParameter(parameters.get(8), "ricinPacks", "", Set.class, LIST, false, true, null);
        assertParameter(parameters.get(9), "nextDoor", "", Door.class, BEAN, false, true, null);
        assertParameter(parameters.get(10), "candidateDoors", "", Map.class, MAP, false, true, null);
    }

    private void assertTestModuleOperations(Extension extension) throws Exception
    {
        assertEquals(6, extension.getOperations().size());
        assertOperation(extension, SAY_MY_NAME_OPERATION, "", new Class<?>[] {Object.class}, String.class);
        assertOperation(extension, GET_ENEMY_OPERATION, "", new Class<?>[] {Object.class}, String.class);
        assertOperation(extension, KILL_OPERATION, "", new Class<?>[] {Object.class}, String.class);
        assertOperation(extension, KILL_CUSTOM_OPERATION, "", new Class<?>[] {Object.class}, String.class);
        assertOperation(extension, HIDE_METH_IN_EVENT_OPERATION, "", new Class<?>[] {Object.class}, void.class);
        assertOperation(extension, HIDE_METH_IN_MESSAGE_OPERATION, "", new Class<?>[] {Object.class}, void.class);

        ExtensionOperation operation = extension.getOperation(SAY_MY_NAME_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());

        operation = extension.getOperation(GET_ENEMY_OPERATION);
        assertNotNull(operation);
        assertEquals(1, operation.getParameters().size());
        assertParameter(operation.getParameters().get(0), "index", "", int.class, INTEGER, true, true, null);

        operation = extension.getOperation(KILL_OPERATION);
        assertNotNull(operation);
        assertEquals(1, operation.getParameters().size());
        assertParameter(operation.getParameters().get(0), "enemiesLookup", "", ExtensionOperation.class, OPERATION, true, true, null);

        operation = extension.getOperation(KILL_CUSTOM_OPERATION);
        assertNotNull(operation);
        assertEquals(2, operation.getParameters().size());
        assertParameter(operation.getParameters().get(0), "goodbyeMessage", "", String.class, STRING, false, true, "#[payload]");
        assertParameter(operation.getParameters().get(1), "enemiesLookup", "", ExtensionOperation.class, OPERATION, true, true, null);

        operation = extension.getOperation(HIDE_METH_IN_EVENT_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());

        operation = extension.getOperation(HIDE_METH_IN_MESSAGE_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());
    }

    private void assertOperation(Extension extension,
                                 String operationName,
                                 String operationDescription,
                                 Class<?>[] inputTypes,
                                 Class<?> outputType) throws Exception
    {

        ExtensionOperation operation = extension.getOperation(operationName);

        assertEquals(operationName, operation.getName());
        assertEquals(operationDescription, operation.getDescription());
        match(operation.getInputTypes(), inputTypes);
        assertEquals(outputType, operation.getOutputType().getRawType());
    }

    private void assertParameter(ExtensionParameter param,
                                 String name,
                                 String description,
                                 Class<?> type,
                                 DataQualifier qualifier,
                                 boolean required,
                                 boolean dynamic,
                                 Object defaultValue)
    {

        assertEquals(name, param.getName());
        assertEquals(description, param.getDescription());
        assertEquals(type, param.getType().getRawType());
        assertSame(qualifier, param.getType().getQualifier());
        assertEquals(required, param.isRequired());
        assertEquals(dynamic, param.isDynamic());
        assertEquals(defaultValue, param.getDefaultValue());
    }

    protected void assertCapabilities(Extension extension)
    {
        // template method for asserting custom capabilities in modules that define them
    }

    private <T> void match(List<DataType> dataTypes, T[] array)
    {
        assertEquals(dataTypes.size(), array.length);

        for (int i = 0; i < array.length; i++)
        {
            assertEquals(dataTypes.get(i).getRawType(), array[i]);
        }
    }

}
