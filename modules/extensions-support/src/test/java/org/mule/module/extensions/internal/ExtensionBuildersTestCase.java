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
import static org.mule.module.extensions.internal.ImmutableDataType.of;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.extensions.introspection.api.NoSuchConfigurationException;
import org.mule.extensions.introspection.api.NoSuchOperationException;
import org.mule.extensions.introspection.spi.ExtensionBuilder;
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

    private Extension extension;

    private ExtensionBuilder populatedBuilder()
    {
        ExtensionBuilder builder = DefaultExtensionBuilder.newBuilder();
        return builder.setName(WS_CONSUMER)
                .setDescription(WS_CONSUMER_DESCRIPTION)
                .setVersion(VERSION)
                .setMinMuleVersion(MIN_MULE_VERSION)
                .addCapablity(new Date())
                .addConfiguration(
                        builder.newConfiguration()
                                .setName(CONFIG_NAME)
                                .setDescription(CONFIG_DESCRIPTION)
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
        extension = populatedBuilder().build();
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
        populatedBuilder().addCapablity(null);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidMinMuleVersion() throws Exception
    {
        populatedBuilder().setMinMuleVersion("3.5.0").build();
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
        ExtensionBuilder builder = populatedBuilder();
        Extension extension = builder.addOperation(builder.newOperation()
                                                           .setName(operationName)
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
        ExtensionBuilder builder = populatedBuilder();
        builder.addOperation(builder.newOperation()
                                     .setName("operation")
                                     .setDescription("description")
        ).build();
    }

    private void assertConsumeOperation(List<ExtensionOperation> operations) throws NoSuchOperationException
    {
        ExtensionOperation operation = operations.get(0);
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
        ExtensionOperation operation = operations.get(1);
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

}
