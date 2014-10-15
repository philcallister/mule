package org.mule.module.json.validation;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.module.json.JsonData;
import org.mule.transformer.types.DataTypeFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.google.common.base.Joiner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefaultJsonParser
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJsonParser.class);
    private static final Class<?>[] TRANSFORMABLE_SUPPORTED_TYPES = new Class<?>[] {JsonData.class, JsonNode.class, String.class};

    DefaultJsonParser()
    {
    }

    public JsonNode asJsonNode(MuleEvent event, Object input) throws IOException
    {
        JsonNode jsonNode = asJsonNode(input);
        if (jsonNode == null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Input type {} was not of any supported type. Attempting with transformer resolution",
                             input.getClass().getName(), Joiner.on(',').join(TRANSFORMABLE_SUPPORTED_TYPES));
            }

            input = transformToSupportedType(event, input, TRANSFORMABLE_SUPPORTED_TYPES);
            jsonNode = asJsonNode(input);

            if (jsonNode == null)
            {
                LOGGER.debug("Could not transform input of type {} to any supported type. Returning null", input.getClass().getName());
            }
        }

        return jsonNode;
    }

    private <T> Object transformToSupportedType(MuleEvent event, T input, Class<?>... supportedTypes)
    {
        final DataType sourceType = DataTypeFactory.create(input.getClass());
        final MuleContext muleContext = event.getMuleContext();

        Object transformedData = null;

        for (Class<?> supportedType : supportedTypes)
        {
            transformedData = attemptTransformation(sourceType, input, supportedType, muleContext);
            if (transformedData != null)
            {
                break;
            }
        }

        return transformedData;
    }

    private <S, R> R attemptTransformation(DataType<S> sourceDataType, S source, Class<R> resultType, MuleContext muleContext)
    {
        DataType<R> resultDataType = DataTypeFactory.create(resultType);

        Transformer transformer;
        try
        {
            transformer = muleContext.getRegistry().lookupTransformer(sourceDataType, resultDataType);
        }
        catch (TransformerException e)
        {
            LOGGER.debug("Could not find a transformer from type {} to {}", sourceDataType.getType().getName(), resultType.getName());
            return null;
        }

        LOGGER.debug("Located transformer {} from type {} to type {}. Attempting transformation...", transformer.getName(), sourceDataType.getType().getName(), resultType.getName());

        try
        {
            return (R) transformer.transform(source);
        }
        catch (TransformerException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        String.format("Transformer %s threw exception while trying to transform an object of type %s into a %s",
                                      transformer.getName(), sourceDataType.getType().getName(), resultType.getName())
                        , e);
            }

            return null;
        }
    }

    private JsonNode asJsonNode(Object input) throws IOException
    {
        if (input instanceof String)
        {
            return JsonLoader.fromString((String) input);
        }
        else if (input instanceof Reader)
        {
            return JsonLoader.fromReader((Reader) input);
        }
        else if (input instanceof InputStream)
        {
            return JsonLoader.fromReader(new InputStreamReader((InputStream) input));
        }
        else if (input instanceof byte[])
        {
            return JsonLoader.fromReader(new InputStreamReader(new ByteArrayInputStream((byte[]) input)));
        }
        else if (input instanceof JsonNode)
        {
            return (JsonNode) input;
        }
        else if (input instanceof JsonData)
        {
            JsonData jsonData = (JsonData) input;
            return JsonLoader.fromReader(new StringReader(jsonData.toString()));
        }

        return null;
    }

}
