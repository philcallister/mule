/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.mule.module.extensions.HeisenbergModule;
import org.mule.tck.junit4.FunctionalTestCase;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

public class ExtensionsDefinitionParserTestCase extends FunctionalTestCase
{

    private static final String HEISENBERG_NAME = "heisenberg";

    @Override
    protected String getConfigFile()
    {
        return "heisenberg-config.xml";
    }

    @Test
    public void heisenbergConfig()
    {
        HeisenbergModule heisenberg = muleContext.getRegistry().lookupObject(HEISENBERG_NAME);
        assertNotNull(heisenberg);
        assertEquals(HeisenbergModule.HEISENBERG, heisenberg.getMyName());
        assertEquals(Integer.valueOf(HeisenbergModule.AGE), heisenberg.getAge());

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
