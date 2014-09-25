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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.extensions.api.annotation.Extension.DEFAULT_CONFIG_NAME;
import static org.mule.extensions.api.annotation.Extension.MIN_MULE_VERSION;
import static org.mule.extensions.introspection.api.DataQualifier.ENUM;
import static org.mule.extensions.introspection.api.DataQualifier.POJO;
import static org.mule.extensions.introspection.api.DataQualifier.BOOLEAN;
import static org.mule.extensions.introspection.api.DataQualifier.DATE;
import static org.mule.extensions.introspection.api.DataQualifier.DATE_TIME;
import static org.mule.extensions.introspection.api.DataQualifier.DECIMAL;
import static org.mule.extensions.introspection.api.DataQualifier.INTEGER;
import static org.mule.extensions.introspection.api.DataQualifier.LIST;
import static org.mule.extensions.introspection.api.DataQualifier.MAP;
import static org.mule.extensions.introspection.api.DataQualifier.OPERATION;
import static org.mule.extensions.introspection.api.DataQualifier.STRING;
import static org.mule.module.extensions.HeisenbergExtension.AGE;
import static org.mule.module.extensions.HeisenbergExtension.EXTENSION_DESCRIPTION;
import static org.mule.module.extensions.HeisenbergExtension.EXTENSION_NAME;
import static org.mule.module.extensions.HeisenbergExtension.EXTENSION_VERSION;
import static org.mule.module.extensions.HeisenbergExtension.HEISENBERG;
import static org.mule.module.extensions.HeisenbergExtension.NAMESPACE;
import static org.mule.module.extensions.HeisenbergExtension.SCHEMA_LOCATION;
import static org.mule.module.extensions.HeisenbergExtension.SCHEMA_VERSION;
import org.mule.api.config.ServiceRegistry;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Configuration;
import org.mule.extensions.api.annotation.Configurations;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionDescriber;
import org.mule.extensions.introspection.api.ExtensionDescribingContext;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.extensions.introspection.spi.ExtensionDescriberPostProcessor;
import org.mule.module.extensions.Door;
import org.mule.module.extensions.HealthStatus;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.introspection.DefaultExtensionDescriber;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultExtensionDescriberTestCase extends AbstractMuleTestCase
{

    private static final String EXTENDED_CONFIG_NAME = "extendedConfig";
    private static final String EXTENDED_CONFIG_DESCRIPTION = "extendedDescription";

    private static final String SAY_MY_NAME_OPERATION = "sayMyName";
    private static final String GET_ENEMY_OPERATION = "getEnemy";
    private static final String KILL_OPERATION = "kill";
    private static final String KILL_CUSTOM_OPERATION = "killWithCustomMessage";
    private static final String HIDE_METH_IN_EVENT_OPERATION = "hideMethInEvent";
    private static final String HIDE_METH_IN_MESSAGE_OPERATION = "hideMethInMessage";

    @Mock
    private ServiceRegistry serviceRegistry;

    private ExtensionBuilder builder;
    private ExtensionDescriber describer;
    private ExtensionDescribingContext describingContext;

    @Before
    public void setUp()
    {
        builder = DefaultExtensionBuilder.newBuilder();


        describer = new DefaultExtensionDescriber();
        describer.setServiceRegistry(serviceRegistry);

        Iterator<ExtensionDescriberPostProcessor> emptyIterator = Collections.emptyIterator();
        when(serviceRegistry.lookupProviders(same(ExtensionDescriberPostProcessor.class))).thenReturn(emptyIterator);
        when(serviceRegistry.lookupProviders(same(ExtensionDescriberPostProcessor.class), any(ClassLoader.class))).thenReturn(emptyIterator);
        describingContext = new ImmutableExtensionDescribingContext(HeisenbergExtension.class, builder);
    }

    @Test
    public void describeTestModule() throws Exception
    {
        describer.describe(describingContext);

        Extension extension = builder.build();
        assertExtensionProperties(extension);

        assertTestModuleConfiguration(extension);
        assertTestModuleOperations(extension);

        assertCapabilities(extension);

        verify(serviceRegistry).lookupProviders(any(Class.class), any(ClassLoader.class));
    }

    @Test
    public void postProcessorsInvoked() throws Exception
    {
        ExtensionDescriberPostProcessor postProcessor1 = mock(ExtensionDescriberPostProcessor.class);
        ExtensionDescriberPostProcessor postProcessor2 = mock(ExtensionDescriberPostProcessor.class);

        Iterator<ExtensionDescriberPostProcessor> it = Arrays.asList(postProcessor1, postProcessor2).iterator();

        when(serviceRegistry.lookupProviders(same(ExtensionDescriberPostProcessor.class), any(ClassLoader.class)))
                .thenReturn(it);

        describeTestModule();

        verify(postProcessor1).postProcess(describingContext);
        verify(postProcessor1).postProcess(describingContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullServiceRegistry()
    {
        describer.setServiceRegistry(null);
    }

    @Test
    public void heisengergPointer() throws Exception
    {
        describingContext = new ImmutableExtensionDescribingContext(HeisenbergPointer.class, builder);
        describeTestModule();
    }

    @Test
    public void heisengergPointerPlusExternalConfig() throws Exception
    {
        describingContext = new ImmutableExtensionDescribingContext(HeisengergPointerPlusExternalConfig.class, builder);
        describer.describe(describingContext);

        Extension extension = builder.build();
        assertExtensionProperties(extension);

        assertThat(extension.getConfigurations().size(), equalTo(2));
        ExtensionConfiguration configuration = extension.getConfiguration(EXTENDED_CONFIG_NAME);
        assertThat(configuration, notNullValue());
        assertThat(configuration.getParameters().size(), equalTo(1));
        assertParameter(configuration.getParameters().get(0), "extendedProperty", "", String.class, STRING, true, true, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void heisengergPointerPlusUnnamedExternalConfig() throws Exception
    {
        describingContext = new ImmutableExtensionDescribingContext(HeisengergPointerPlusUnnamedExternalConfig.class, builder);
        describer.describe(describingContext);

        describingContext.getExtensionBuilder().build();
    }


    private void assertTestModuleConfiguration(Extension extension) throws Exception
    {
        assertEquals(1, extension.getConfigurations().size());
        ExtensionConfiguration conf = extension.getConfigurations().get(0);
        assertSame(conf, extension.getConfiguration(DEFAULT_CONFIG_NAME));

        List<ExtensionParameter> parameters = conf.getParameters();
        assertEquals(13, parameters.size());

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
        assertParameter(parameters.get(9), "nextDoor", "", Door.class, POJO, false, true, null);
        assertParameter(parameters.get(10), "candidateDoors", "", Map.class, MAP, false, true, null);
        assertParameter(parameters.get(11), "initialHealth", "", HealthStatus.class, ENUM, true, true, null);
        assertParameter(parameters.get(12), "finalHealth", "", HealthStatus.class, ENUM, true, true, null);

    }

    private void assertExtensionProperties(Extension extension)
    {
        assertNotNull(extension);

        assertEquals(EXTENSION_NAME, extension.getName());
        assertEquals(EXTENSION_DESCRIPTION, extension.getDescription());
        assertEquals(EXTENSION_VERSION, extension.getVersion());
        assertEquals(MIN_MULE_VERSION, extension.getMinMuleVersion());
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

    @org.mule.extensions.api.annotation.Extension(name = EXTENSION_NAME, description = EXTENSION_DESCRIPTION, version = EXTENSION_VERSION)
    @Xml(schemaLocation = SCHEMA_LOCATION, namespace = NAMESPACE, schemaVersion = SCHEMA_VERSION)
    @Configurations(HeisenbergExtension.class)
    public static class HeisenbergPointer extends HeisenbergExtension
    {
    }

    @org.mule.extensions.api.annotation.Extension(name = EXTENSION_NAME, description = EXTENSION_DESCRIPTION, version = EXTENSION_VERSION)
    @Xml(schemaLocation = SCHEMA_LOCATION, namespace = NAMESPACE, schemaVersion = SCHEMA_VERSION)
    @Configurations({HeisenbergExtension.class, NamedHeisenbergAlternateConfig.class})
    public static class HeisengergPointerPlusExternalConfig
    {

    }

    @org.mule.extensions.api.annotation.Extension(name = EXTENSION_NAME, description = EXTENSION_DESCRIPTION, version = EXTENSION_VERSION)
    @Xml(schemaLocation = SCHEMA_LOCATION, namespace = NAMESPACE, schemaVersion = SCHEMA_VERSION)
    @Configurations({HeisenbergExtension.class, HeisenbergAlternateConfig.class})
    public static class HeisengergPointerPlusUnnamedExternalConfig
    {

    }

    @Configuration(name = EXTENDED_CONFIG_NAME, description = EXTENDED_CONFIG_DESCRIPTION)
    public static class NamedHeisenbergAlternateConfig extends HeisenbergAlternateConfig
    {

    }

    public static class HeisenbergAlternateConfig
    {

        @Configurable
        private String extendedProperty;

        public String getExtendedProperty()
        {
            return extendedProperty;
        }

        public void setExtendedProperty(String extendedProperty)
        {
            this.extendedProperty = extendedProperty;
        }
    }

}
