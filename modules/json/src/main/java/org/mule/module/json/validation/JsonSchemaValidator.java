/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.json.validation;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleRuntimeException;
import org.mule.config.i18n.MessageFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.Dereferencing;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfigurationBuilder;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class JsonSchemaValidator
{

    public static final class Builder
    {

        private String schemaLocation;
        private JsonSchemaDereferencing dereferencing = JsonSchemaDereferencing.CANONICAL;
        private final Map<String, String> schemaRedirects = new HashMap<>();

        private Builder()
        {
        }

        public Builder setSchemaLocation(String schemaLocation)
        {
            checkArgument(!isBlank(schemaLocation), "schemaLocation cannot be null or blank");
            this.schemaLocation = schemaLocation;
            return this;
        }

        public Builder setDereferencing(JsonSchemaDereferencing dereferencing)
        {
            checkArgument(dereferencing != null, "dereferencing cannot be null");
            this.dereferencing = dereferencing;
            return this;
        }

        public Builder addSchemaRedirect(String from, String to)
        {
            checkArgument(!isBlank(from), "from cannot be null or blank");
            checkArgument(!isBlank(to), "to cannot be null or blank");
            schemaRedirects.put(from, to);

            return this;
        }

        public JsonSchemaValidator build()
        {
            final URITranslatorConfigurationBuilder translatorConfigurationBuilder = URITranslatorConfiguration.newBuilder();
            for (Map.Entry<String, String> redirect : schemaRedirects.entrySet())
            {
                translatorConfigurationBuilder.addSchemaRedirect(redirect.getKey(), redirect.getValue());
            }

            final LoadingConfigurationBuilder loadingConfigurationBuilder = LoadingConfiguration.newBuilder()
                    .dereferencing(dereferencing == JsonSchemaDereferencing.CANONICAL
                                   ? Dereferencing.CANONICAL
                                   : Dereferencing.INLINE)
                    .setURITranslatorConfiguration(translatorConfigurationBuilder.freeze());

            JsonSchemaFactory factory = JsonSchemaFactory.newBuilder()
                    .setLoadingConfiguration(loadingConfigurationBuilder.freeze())
                    .freeze();

            try
            {
                return new JsonSchemaValidator(factory.getJsonSchema(schemaLocation));
            }
            catch (ProcessingException e)
            {
                throw new MuleRuntimeException(MessageFactory.createStaticMessage("Could not initialise JsonSchemaValidator"), e);
            }
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    private final JsonSchema schema;
    private final DefaultJsonParser jsonParser = new DefaultJsonParser();

    private JsonSchemaValidator(JsonSchema schema)
    {
        this.schema = schema;
    }

    public void validate(MuleEvent event) throws MuleException
    {
        Object input = event.getMessage().getPayload();
        ProcessingReport report;
        JsonNode jsonNode;

        try
        {
            jsonNode = jsonParser.asJsonNode(event, input);

            if ((input instanceof Reader) || (input instanceof InputStream))
            {
                event.getMessage().setPayload(jsonNode.toString());
            }

            report = schema.validate(jsonNode);
        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(MessageFactory.createStaticMessage("Exception was found while trying to validate json schema"), e);
        }

        if (!report.isSuccess())
        {
            throw new JsonSchemaValidationException(report.toString(), jsonNode.toString());
        }
    }
}
