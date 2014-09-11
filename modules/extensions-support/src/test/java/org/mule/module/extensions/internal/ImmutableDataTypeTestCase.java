/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.mule.extensions.introspection.api.DataQualifier;
import org.mule.extensions.introspection.api.DataType;
import org.mule.module.extensions.internal.introspection.ImmutableDataType;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.tck.testmodels.fruit.Apple;
import org.mule.tck.testmodels.fruit.Banana;
import org.mule.tck.testmodels.fruit.FruitBasket;
import org.mule.util.ArrayUtils;

import java.util.List;
import java.util.Map;

import org.junit.Test;

@SmallTest
public class ImmutableDataTypeTestCase extends AbstractMuleTestCase
{

    @Test
    public void simpleType()
    {
        DataType type = ImmutableDataType.of(String.class);
        assertEquals(String.class, type.getRawType());
        assertNoGenericTypes(type);
        assertEquals(DataQualifier.STRING, type.getQualifier());
    }

    @Test
    public void typeWithGeneric()
    {
        DataType type = ImmutableDataType.of(Map.class, String.class, Apple.class);
        assertMap(type, String.class, DataQualifier.STRING, Apple.class, DataQualifier.POJO);
        assertNoGenericTypes(type.getGenericTypes()[0]);
        assertNoGenericTypes(type.getGenericTypes()[1]);
    }

    @Test
    public void complexTypeWithManyGenerics()
    {
        DataType fruitMap = ImmutableDataType.of(Map.class, Apple.class, Banana.class);
        DataType basketList = ImmutableDataType.of(List.class, FruitBasket.class);
        DataType type = ImmutableDataType.of(Map.class, fruitMap, basketList);

        assertMap(type, Map.class, DataQualifier.MAP, List.class, DataQualifier.LIST);
        assertMap(type.getGenericTypes()[0], Apple.class, DataQualifier.POJO, Banana.class, DataQualifier.POJO);
        assertNoGenericTypes(type.getGenericTypes()[0].getGenericTypes()[0]);
        assertNoGenericTypes(type.getGenericTypes()[0].getGenericTypes()[1]);

        assertList(type.getGenericTypes()[1], FruitBasket.class, DataQualifier.POJO);
        assertNoGenericTypes(type.getGenericTypes()[1].getGenericTypes()[0]);
    }

    @Test
    public void equalsOfNoGenericType()
    {
        DataType type1 = ImmutableDataType.of(String.class);
        DataType type2 = ImmutableDataType.of(String.class);

        assertEquals(type1, type2);
    }

    @Test
    public void equalsWithGenericType()
    {
        DataType type1 = ImmutableDataType.of(Map.class, String.class, Long.class);
        DataType type2 = ImmutableDataType.of(Map.class, String.class, Long.class);

        assertEquals(type1, type2);
    }

    @Test
    public void notEqualsWithoutGenericTypes()
    {
        DataType type1 = ImmutableDataType.of(Apple.class);
        DataType type2 = ImmutableDataType.of(String.class);

        assertFalse(type1.equals(type2));
    }

    @Test
    public void notEqualsWithGenericTypes()
    {
        DataType type1 = ImmutableDataType.of(Map.class, String.class, Long.class);
        DataType type2 = ImmutableDataType.of(Map.class, String.class, Apple.class);

        assertFalse(type1.equals(type2));
    }

    @Test
    public void hashCodeOnEqualTypes()
    {
        DataType type1 = ImmutableDataType.of(Map.class, String.class, Long.class);
        DataType type2 = ImmutableDataType.of(Map.class, String.class, Long.class);

        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    public void hashCodeOnUnequalTypes()
    {
        DataType type1 = ImmutableDataType.of(Map.class, String.class, Long.class);
        DataType type2 = ImmutableDataType.of(Map.class, String.class, Apple.class);

        assertFalse(type1.hashCode() == type2.hashCode());
    }

    private void assertList(DataType type, Class<?> valueType, DataQualifier valueQualifierType)
    {
        assertEquals(List.class, type.getRawType());
        assertEquals(1, type.getGenericTypes().length);
        assertEquals(DataQualifier.LIST, type.getQualifier());
        assertEquals(valueType, type.getGenericTypes()[0].getRawType());
        assertEquals(valueQualifierType, type.getGenericTypes()[0].getQualifier());
    }


    private void assertMap(DataType type, Class<?> keyType, DataQualifier keyQualifier, Class<?> valueType, DataQualifier valueQualifier)
    {
        assertEquals(Map.class, type.getRawType());
        assertEquals(2, type.getGenericTypes().length);
        assertEquals(DataQualifier.MAP, type.getQualifier());

        assertEquals(keyType, type.getGenericTypes()[0].getRawType());
        assertEquals(keyQualifier, type.getGenericTypes()[0].getQualifier());

        assertEquals(valueType, type.getGenericTypes()[1].getRawType());
        assertEquals(valueQualifier, type.getGenericTypes()[1].getQualifier());
    }

    private void assertNoGenericTypes(DataType type)
    {
        assertNotNull(type.getGenericTypes());
        assertTrue(ArrayUtils.isEmpty(type.getGenericTypes()));
    }
}
