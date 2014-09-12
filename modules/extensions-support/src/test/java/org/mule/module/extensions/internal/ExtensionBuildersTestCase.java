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
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mule.extensions.introspection.api.DataQualifier.BOOLEAN;
import static org.mule.extensions.introspection.api.DataQualifier.LIST;
import static org.mule.extensions.introspection.api.DataQualifier.STRING;
import static org.mule.module.extensions.internal.introspection.ImmutableDataType.of;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionBuilder;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.extensions.introspection.api.NoSuchConfigurationException;
import org.mule.extensions.introspection.api.NoSuchOperationException;
import org.mule.module.extensions.internal.introspection.DefaultExtensionBuilder;
import org.mule.module.extensions.internal.introspection.ImmutableDataType;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class ExtensionBuildersTestCase extends AbstractMuleTestCase
{

    private static final DataType STRING_DATA_TYPE = ImmutableDataType.of(String.class);
    private static final Class<?> DECLARING_CLASS = ExtensionBuildersTestCase.class;

    private static final String CONFIG_NAME = "config";
    private static final String CONFIG_DESCRIPTION = "Default description";
    private static final String WS_CONSUMER = "WSConsumer";
    private static final String WS_CONSUMER_DESCRIPTION = "Generic Consumer for SOAP Web Services";
    private static final String VERSION = "3.6.0";
    private static final String MIN_MULE_VERSION = VERSION;
    private static final String WSDL_LOCATION = "wsdlLocation";
    private static final String URI_TO_FIND_THE_WSDL = "URI to find the WSDL";
    private static final String SERVICE = "service";
    private static final String SERVICE_NAME = "Service Name";
    private static final String PORT = "port";
    private static final String SERVICE_PORT = "Service Port";
    private static final String ADDRESS = "address";
    private static final String SERVICE_ADDRESS = "Service address";
    private static final String CONSUMER = "consumer";
    private static final String GO_GET_THEM_TIGER = "Go get them tiger";
    private static final String OPERATION = "operation";
    private static final String THE_OPERATION_TO_USE = "The operation to use";
    private static final String MTOM_ENABLED = "mtomEnabled";
    private static final String MTOM_DESCRIPTION = "Whether or not use MTOM for attachments";
    private static final String BROADCAST = "broadcast";
    private static final String BROADCAST_DESCRIPTION = "consumes many services";
    private static final String CALLBACK = "callback";
    private static final String CALLBACK_DESCRIPTION = "async callback";


    private ExtensionBuilder builder;
    private Extension extension;

    private ExtensionBuilder populatedBuilder()
    {
        builder = DefaultExtensionBuilder.newBuilder();
        return builder.setName(WS_CONSUMER)
                .setDescription(WS_CONSUMER_DESCRIPTION)
                .setVersion(VERSION)
                .setMinMuleVersion(MIN_MULE_VERSION)
                .setDeclaringClass(ExtensionBuildersTestCase.class)
                .addCapablity(new Date())
                .addConfiguration(
                        builder.newConfiguration()
                                .setName(CONFIG_NAME)
                                .setDescription(CONFIG_DESCRIPTION)
                                .setDeclaringClass(WsConsumerConfig.class)
                                .addParameter(builder.newParameter()
                                                      .setName(WSDL_LOCATION)
                                                      .setDescription(URI_TO_FIND_THE_WSDL)
                                                      .setRequired(true)
                                                      .setDynamic(false)
                                                      .setType(of(String.class))
                                )
                                .addParameter(builder.newParameter()
                                                      .setName(SERVICE)
                                                      .setDescription(SERVICE_NAME)
                                                      .setRequired(true)
                                                      .setType(of(String.class))
                                )
                                .addParameter(builder.newParameter()
                                                      .setName(PORT)
                                                      .setDescription(SERVICE_PORT)
                                                      .setRequired(true)
                                                      .setType(of(String.class))
                                )
                                .addParameter(builder.newParameter()
                                                      .setName(ADDRESS)
                                                      .setDescription(SERVICE_ADDRESS)
                                                      .setRequired(true)
                                                      .setType(of(String.class))
                                )
                )
                .addOperation(builder.newOperation()
                                      .setName(CONSUMER)
                                      .setDescription(GO_GET_THEM_TIGER)
                                      .addInputType(of(String.class))
                                      .setOutputType(of(String.class))
                                      .setDeclaringClass(DECLARING_CLASS)
                                      .addParameter(builder.newParameter()
                                                            .setName(OPERATION)
                                                            .setDescription(THE_OPERATION_TO_USE)
                                                            .setRequired(true)
                                                            .setType(of(String.class))
                                      )
                                      .addParameter(builder.newParameter()
                                                            .setName(MTOM_ENABLED)
                                                            .setDescription(MTOM_DESCRIPTION)
                                                            .setRequired(false)
                                                            .setDefaultValue(true)
                                                            .setType(of(Boolean.class))
                                      )
                ).addOperation(builder.newOperation()
                                       .setName(BROADCAST)
                                       .setDescription(BROADCAST_DESCRIPTION)
                                       .addInputType(of(String.class))
                                       .setOutputType(of(List.class, String.class))
                                       .setDeclaringClass(DECLARING_CLASS)
                                       .addParameter(builder.newParameter()
                                                             .setName(OPERATION)
                                                             .setDescription(THE_OPERATION_TO_USE)
                                                             .setRequired(true)
                                                             .setType(of(List.class, String.class))
                                       ).addParameter(builder.newParameter()
                                                              .setName(MTOM_ENABLED)
                                                              .setDescription(MTOM_DESCRIPTION)
                                                              .setRequired(false)
                                                              .setDefaultValue(true)
                                                              .setType(of(Boolean.class))
                                       ).addParameter(builder.newParameter()
                                                              .setName(CALLBACK)
                                                              .setDescription(CALLBACK_DESCRIPTION)
                                                              .setRequired(true)
                                                              .setDynamic(false)
                                                              .setType(of(ExtensionOperation.class))
                                       )
                );
    }

    @Before
    public void buildExtension() throws Exception
    {
        builder = populatedBuilder();
        extension = builder.build();
    }

    @Test
    public void assertExtension()
    {
        assertEquals(WS_CONSUMER, extension.getName());
        assertEquals(WS_CONSUMER_DESCRIPTION, extension.getDescription());
        assertEquals(VERSION, extension.getVersion());
        assertEquals(MIN_MULE_VERSION, extension.getMinMuleVersion());
        assertEquals(1, extension.getConfigurations().size());

        Set<Date> capabilities = extension.getCapabilities(Date.class);
        assertNotNull(capabilities);
        assertEquals(1, capabilities.size());
        Date capability = capabilities.iterator().next();
        assertTrue(capability instanceof Date);
    }

    @Test
    public void defaultConfiguration() throws Exception
    {
        ExtensionConfiguration configuration = extension.getConfiguration(CONFIG_NAME);
        assertNotNull(configuration);
        assertEquals(CONFIG_NAME, configuration.getName());
        assertEquals(CONFIG_DESCRIPTION, configuration.getDescription());

        List<ExtensionParameter> parameters = configuration.getParameters();
        assertEquals(4, parameters.size());
        assertParameter(parameters.get(0), WSDL_LOCATION, URI_TO_FIND_THE_WSDL, false, true, of(String.class), STRING, null);
        assertParameter(parameters.get(1), SERVICE, SERVICE_NAME, true, true, of(String.class), STRING, null);
        assertParameter(parameters.get(2), PORT, SERVICE_PORT, true, true, of(String.class), STRING, null);
        assertParameter(parameters.get(3), ADDRESS, SERVICE_ADDRESS, true, true, of(String.class), STRING, null);
    }

    @Test
    public void onlyOneConfig() throws Exception
    {
        assertEquals(1, extension.getConfigurations().size());
        assertSame(extension.getConfigurations().get(0), extension.getConfiguration(CONFIG_NAME));
    }

    @Test(expected = NoSuchConfigurationException.class)
    public void noSuchConfiguration() throws Exception
    {
        extension.getConfiguration("fake");
    }

    @Test(expected = NoSuchOperationException.class)
    public void noSuchOperation() throws Exception
    {
        extension.getOperation("fake");
    }

    @Test
    public void noSuchCapability()
    {
        Set<String> capabilities = extension.getCapabilities(String.class);
        assertNotNull(capabilities);
        assertTrue(capabilities.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullCapability()
    {
        builder.addCapablity(null);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidMinMuleVersion() throws Exception
    {
        builder.setMinMuleVersion("3.5.0").build();
    }

    @Test
    public void operations() throws Exception
    {
        List<ExtensionOperation> operations = extension.getOperations();
        assertEquals(2, operations.size());
        assertConsumeOperation(operations);
        assertBroadcastOperation(operations);
    }

    @Test
    public void defaultOperationInputType() throws Exception
    {
        final String operationName = "operation";
        extension = builder.addOperation(builder.newOperation()
                                                 .setName(operationName)
                                                 .setDeclaringClass(DECLARING_CLASS)
                                                 .setDescription("description")
                                                 .setOutputType(of(String.class)))
                .build();

        List<DataType> inputTypes = extension.getOperation(operationName).getInputTypes();
        assertEquals(1, inputTypes.size());

        DataType type = inputTypes.get(0);
        assertEquals(Object.class, type.getRawType());
        assertTrue(Arrays.equals(new Class<?>[] {}, type.getGenericTypes()));
    }

    @Test(expected = IllegalStateException.class)
    public void operationWithoutOutputType() throws Exception
    {
        builder.addOperation(builder.newOperation()
                                     .setName("operation")
                                     .setDescription("description")
                                     .setDeclaringClass(DECLARING_CLASS)
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void operationWithoutDeclaringClass() throws Exception
    {
        builder.addOperation(builder.newOperation()
                                     .setName("operation")
                                     .setDescription("description")
                                     .setOutputType(STRING_DATA_TYPE)
        ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void badExtensionVersion()
    {
        builder.setVersion("i'm new").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void badMinMuleVersion()
    {
        builder.setMinMuleVersion("i'm new").build();
    }

    @Test
    public void configurationsOrder()
    {
        final String beta = "beta";
        final String alpha = "alpha";

        Extension extension = builder
                .addConfiguration(builder.newConfiguration()
                                          .setName(beta)
                                          .setDescription(beta)
                                          .setDeclaringClass(DECLARING_CLASS))
                .addConfiguration(builder.newConfiguration()
                                          .setName(alpha)
                                          .setDescription(alpha)
                                          .setDeclaringClass(DECLARING_CLASS))
                .build();

        List<ExtensionConfiguration> configurations = extension.getConfigurations();
        assertEquals(3, configurations.size());
        assertEquals(CONFIG_NAME, configurations.get(0).getName());
        assertEquals(alpha, configurations.get(1).getName());
        assertEquals(beta, configurations.get(2).getName());
    }

    @Test
    public void operationsAlphaSorted()
    {
        assertEquals(2, extension.getOperations().size());
        assertEquals(BROADCAST, extension.getOperations().get(0).getName());
        assertEquals(CONSUMER, extension.getOperations().get(1).getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameClashes()
    {
        builder.addOperation(builder.newOperation()
                                     .setName(CONFIG_NAME)
                                     .setDescription("")
                                     .addInputType(STRING_DATA_TYPE)
                                     .setOutputType(STRING_DATA_TYPE))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void operationWithParameterNamedName()
    {
        builder.addOperation(builder.newOperation()
                                     .setName("invalidOperation")
                                     .setDescription("")
                                     .addInputType(STRING_DATA_TYPE)
                                     .setOutputType(STRING_DATA_TYPE)
                                     .addParameter(builder.newParameter()
                                                           .setName("name")
                                                           .setType(STRING_DATA_TYPE)))
                .build();
    }


    @Test(expected = IllegalArgumentException.class)
    public void nameWithSpaces()
    {
        builder.setName("i have spaces").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void configurationWithFiledWithoutGetter()
    {
        builder.addConfiguration(builder.newConfiguration()
                                         .setName("fail")
                                         .setDescription("fail")
                                         .setDeclaringClass(getClass())
                                         .addParameter(builder.newParameter()
                                                               .setName("notExistent")
                                                               .setType(STRING_DATA_TYPE)
                                                               .setDescription("no setter")))
                .build();
    }


    @Test(expected = IllegalArgumentException.class)
    public void configurationWithoutValidConstructor()
    {
        builder.addConfiguration(builder.newConfiguration()
                                         .setName("fail")
                                         .setDescription("fail")
                                         .setDeclaringClass(InvalidConstructorConfiguration.class))
                .build();
    }

    private void assertConsumeOperation(List<ExtensionOperation> operations) throws NoSuchOperationException
    {
        ExtensionOperation operation = operations.get(1);
        assertSame(operation, extension.getOperation(CONSUMER));

        assertEquals(CONSUMER, operation.getName());
        assertEquals(GO_GET_THEM_TIGER, operation.getDescription());
        strictTypeAssert(operation.getInputTypes(), String.class);
        strictTypeAssert(operation.getOutputType(), String.class);

        List<ExtensionParameter> parameters = operation.getParameters();
        assertEquals(2, parameters.size());
        assertParameter(parameters.get(0), OPERATION, THE_OPERATION_TO_USE, true, true, of(String.class), STRING, null);
        assertParameter(parameters.get(1), MTOM_ENABLED, MTOM_DESCRIPTION, true, false, of(Boolean.class), BOOLEAN, true);
    }

    private void assertBroadcastOperation(List<ExtensionOperation> operations) throws NoSuchOperationException
    {
        ExtensionOperation operation = operations.get(0);
        assertSame(operation, extension.getOperation(BROADCAST));

        assertEquals(BROADCAST, operation.getName());
        assertEquals(BROADCAST_DESCRIPTION, operation.getDescription());
        strictTypeAssert(operation.getInputTypes(), String.class);
        strictTypeAssert(operation.getOutputType(), List.class, new Class[] {String.class});

        List<ExtensionParameter> parameters = operation.getParameters();
        assertEquals(3, parameters.size());
        assertParameter(parameters.get(0), OPERATION, THE_OPERATION_TO_USE, true, true, of(List.class, String.class), LIST, null);
        assertParameter(parameters.get(1), MTOM_ENABLED, MTOM_DESCRIPTION, true, false, of(Boolean.class), BOOLEAN, true);
        assertParameter(parameters.get(2), CALLBACK, CALLBACK_DESCRIPTION, false, true, of(ExtensionOperation.class), DataQualifier.OPERATION, null);
    }

    private void assertParameter(ExtensionParameter parameter,
                                 String name,
                                 String description,
                                 boolean acceptsExpressions,
                                 boolean required,
                                 DataType type,
                                 DataQualifier qualifier,
                                 Object defaultValue)
    {

        assertNotNull(parameter);
        assertEquals(name, parameter.getName());
        assertEquals(description, parameter.getDescription());
        assertEquals(acceptsExpressions, parameter.isDynamic());
        assertEquals(required, parameter.isRequired());
        assertEquals(type, parameter.getType());
        assertSame(qualifier, parameter.getType().getQualifier());

        if (defaultValue != null)
        {
            assertEquals(defaultValue, parameter.getDefaultValue());
        }
        else
        {
            assertNull(parameter.getDefaultValue());
        }
    }

    private void strictTypeAssert(List<DataType> types, Class<?> expected, Class<?>[]... genericTypes)
    {
        assertEquals(1, types.size());
        strictTypeAssert(types.get(0), expected, genericTypes);
    }

    private void strictTypeAssert(DataType type, Class<?> expected, Class<?>[]... genericTypes)
    {
        assertEquals(expected, type.getRawType());
        Arrays.equals(genericTypes, type.getGenericTypes());
    }

    @SuppressWarnings("unused")
    private static class WsConsumerConfig
    {

        public WsConsumerConfig()
        {

        }

        public void setWsdlLocation(String wsdlLocation)
        {

        }

        public void setService(String service)
        {

        }

        public void setPort(String port)
        {

        }

        public void setAddress(String address)
        {

        }
    }

    private static class InvalidConstructorConfiguration
    {

        @SuppressWarnings("unused")
        public InvalidConstructorConfiguration(String value)
        {
        }
    }
}
