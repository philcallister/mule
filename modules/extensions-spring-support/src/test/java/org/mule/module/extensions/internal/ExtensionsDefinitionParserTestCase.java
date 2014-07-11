/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.mule.module.extensions.Door;
import org.mule.module.extensions.HeisenbergExtension;
import org.mule.module.extensions.Ricin;
import org.mule.tck.junit4.ExtensionsFunctionalTestCase;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ExtensionsDefinitionParserTestCase extends ExtensionsFunctionalTestCase
{

    private static final String HEISENBERG_NAME = "heisenberg";
    private static final String HEISENBERG_BYREF = "heisenbergByRef";

    @Override
    protected String getConfigFile()
    {
        return "heisenberg-config.xml";
    }

    @Override
    protected String[] getDiscoverablePackages()
    {
        return new String[] {"org.mule.module.extensions"};
    }

    @Test
    public void heisenbergConfig()
    {
        HeisenbergExtension heisenberg = muleContext.getRegistry().lookupObject(HEISENBERG_NAME);
        assertHeisenbergConfig(heisenberg);
    }

    @Test
    public void heisenbergByRef() throws Exception
    {
        HeisenbergExtension heisenberg = muleContext.getRegistry().lookupObject(HEISENBERG_BYREF);
        assertHeisenbergConfig(heisenberg);
    }

    private void assertHeisenbergConfig(HeisenbergExtension heisenberg)
    {
        assertNotNull(heisenberg);

        assertSimpleProperties(heisenberg);
        assertRecipe(heisenberg);
        assertDoors(heisenberg);
        assertRicinPacks(heisenberg);
        assertCandidateDoors(heisenberg);
    }

    private void assertRicinPacks(HeisenbergExtension heisenberg)
    {
        Set<Ricin> ricinPacks = heisenberg.getRicinPacks();

        assertNotNull(ricinPacks);
        assertEquals(1, ricinPacks.size());
        Ricin ricin = ricinPacks.iterator().next();
        assertEquals(Long.valueOf(22), ricin.getMicrogramsPerKilo());
        assertDoor(ricin.getDestination(), "Lidia", "Stevia coffe shop");
    }

    private void assertDoors(HeisenbergExtension heisenberg)
    {
        Door door = heisenberg.getNextDoor();
        assertDoor(door, "Gustavo Fring", "pollos hermanos");

        Door previous = door.getPrevious();
        assertDoor(door.getPrevious(), "Krazy-8", "Jesse's");
        assertNull(previous.getPrevious());
    }

    private void assertRecipe(HeisenbergExtension heisenberg)
    {
        Map<String, Long> recipe = heisenberg.getRecipe();
        assertNotNull(recipe);
        assertEquals(3, recipe.size());
        assertEquals(Long.valueOf(75), recipe.get("methylamine"));
        assertEquals(Long.valueOf(0), recipe.get("pseudoephedrine"));
        assertEquals(Long.valueOf(25), recipe.get("P2P"));
    }

    private void assertSimpleProperties(HeisenbergExtension heisenberg)
    {
        assertEquals(HeisenbergExtension.HEISENBERG, heisenberg.getMyName());
        assertEquals(Integer.valueOf(HeisenbergExtension.AGE), heisenberg.getAge());

        List<String> enemies = heisenberg.getEnemies();
        assertEquals(2, enemies.size());
        assertEquals("Gustavo Fring", enemies.get(0));
        assertEquals("Hank", enemies.get(1));

        assertTrue(heisenberg.isCancer());

        Calendar dayOfBirth = Calendar.getInstance();
        dayOfBirth.setTime(heisenberg.getDateOfBirth());

        //only compare year to avoid timezone related flakyness
        assertEquals(getDateOfBirth().get(Calendar.YEAR), dayOfBirth.get(Calendar.YEAR));
        assertEquals(getDateOfDeath().get(Calendar.YEAR), heisenberg.getDateOfDeath().get(Calendar.YEAR));

        assertEquals(new BigDecimal("1000000"), heisenberg.getMoney());
    }

    private void assertCandidateDoors(HeisenbergExtension heisenberg)
    {
        Map<String, Door> candidates = heisenberg.getCandidateDoors();
        assertNotNull(candidates);
        assertEquals(2, candidates.size());

        assertDoor(candidates.get("skyler"), "Skyler", "308 Negra Arroyo Lane");
        assertDoor(candidates.get("saul"), "Saul", "Shopping Mall");
    }

    private void assertDoor(Door door, String victim, String address)
    {
        assertNotNull(door);
        assertEquals(victim, door.getVictim());
        assertEquals(address, door.getAddress());
    }

    private Calendar getDateOfBirth()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1959);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 7);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    private Calendar getDateOfDeath()
    {
        Calendar calendar = getDateOfBirth();
        calendar.set(Calendar.YEAR, 2011);

        return calendar;
    }
}
