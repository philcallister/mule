/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.api.context.MuleContextAware;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.Lifecycle;
import org.mule.api.transport.PropertyScope;
import org.mule.extensions.api.annotation.Configurable;
import org.mule.extensions.api.annotation.Extension;
import org.mule.extensions.api.annotation.Operation;
import org.mule.extensions.api.annotation.capability.Xml;
import org.mule.extensions.api.annotation.param.Optional;
import org.mule.extensions.api.annotation.param.Payload;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Extension(name = HeisenbergExtension.EXTENSION_NAME, description = HeisenbergExtension.EXTENSION_DESCRIPTION, version = HeisenbergExtension.EXTENSION_VERSION)
@Xml(schemaLocation = HeisenbergExtension.SCHEMA_LOCATION, namespace = HeisenbergExtension.NAMESPACE, schemaVersion = HeisenbergExtension.SCHEMA_VERSION)
public class HeisenbergExtension implements Lifecycle, MuleContextAware
{

    public static final String SCHEMA_LOCATION = "http://www.mulesoft.org/schema/mule/extension/heisenberg";
    public static final String NAMESPACE = "heisenberg";
    public static final String SCHEMA_VERSION = "1.0-blue";

    public static final String HEISENBERG = "Heisenberg";
    public static final String AGE = "50";
    public static final String EXTENSION_NAME = "heisenberg";
    public static final String EXTENSION_DESCRIPTION = "My Test Extension just to unit test";
    public static final String EXTENSION_VERSION = "1.0";

    private int initialise = 0;
    private int start = 0;
    private int stop;
    private int dispose = 0;

    private MuleContext muleContext;

    @Configurable
    @Optional(defaultValue = HEISENBERG)
    private String myName;

    @Configurable
    @Optional(defaultValue = AGE)
    private Integer age;

    @Configurable
    private List<String> enemies = new LinkedList<>();

    @Configurable
    private BigDecimal money;

    @Configurable
    private boolean cancer;

    @Configurable
    private Date dateOfBirth;

    @Configurable
    private Calendar dateOfDeath;

    @Configurable
    @Optional
    private Map<String, Long> recipe;

    @Configurable
    @Optional
    private Set<Ricin> ricinPacks;

    @Configurable
    @Optional
    private Door nextDoor;

    /**
     * Doors I might knock on but still haven't made up mind about
     */
    @Configurable
    @Optional
    private Map<String, Door> candidateDoors;

    @Configurable
    private HealthStatus initialHealth;

    @Configurable
    private HealthStatus finalHealth;


    @Operation
    public String sayMyName()
    {
        return myName;
    }

    @Operation
    public String getEnemy(int index)
    {
        return enemies.get(index);
    }

    @Operation
    public String kill(@Payload String goodbyeMessage,
                       NestedProcessor enemiesLookup) throws Exception
    {

        return killWithCustomMessage(goodbyeMessage, enemiesLookup);
    }

    @Operation
    public String killWithCustomMessage(@Optional(defaultValue = "#[payload]") String goodbyeMessage,
                                        NestedProcessor enemiesLookup) throws Exception
    {
        List<String> toKill = (List<String>) enemiesLookup.process();
        StringBuilder builder = new StringBuilder();

        for (String kill : toKill)
        {
            builder.append(String.format("%s: %s", goodbyeMessage, kill)).append("\n");
        }

        return builder.toString();
    }

    @Operation
    public void hideMethInEvent(MuleEvent event)
    {
        hideMethInMessage(event.getMessage());
    }

    @Operation
    public void hideMethInMessage(MuleMessage message)
    {
        message.setProperty("secretPackage", "meth", PropertyScope.INVOCATION);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        initialise++;
    }

    @Override
    public void start() throws MuleException
    {
        start++;
    }

    @Override
    public void stop() throws MuleException
    {
        stop++;
    }

    @Override
    public void dispose()
    {
        dispose++;
    }

    public String getMyName()
    {
        return myName;
    }

    public void setMyName(String myName)
    {
        this.myName = myName;
    }

    public List<String> getEnemies()
    {
        return enemies;
    }

    public void setEnemies(List<String> enemies)
    {
        this.enemies = enemies;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public boolean isCancer()
    {
        return cancer;
    }

    public void setCancer(boolean cancer)
    {
        this.cancer = cancer;
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public void setMoney(BigDecimal money)
    {
        this.money = money;
    }

    public Calendar getDateOfDeath()
    {
        return dateOfDeath;
    }

    public void setDateOfDeath(Calendar dateOfDeath)
    {
        this.dateOfDeath = dateOfDeath;
    }

    public Date getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public Map<String, Long> getRecipe()
    {
        return recipe;
    }

    public void setRecipe(Map<String, Long> recipe)
    {
        this.recipe = recipe;
    }

    public Set<Ricin> getRicinPacks()
    {
        return ricinPacks;
    }

    public void setRicinPacks(Set<Ricin> ricinPacks)
    {
        this.ricinPacks = ricinPacks;
    }

    public Door getNextDoor()
    {
        return nextDoor;
    }

    public void setNextDoor(Door nextDoor)
    {
        this.nextDoor = nextDoor;
    }

    public Map<String, Door> getCandidateDoors()
    {
        return candidateDoors;
    }

    public void setCandidateDoors(Map<String, Door> candidateDoors)
    {
        this.candidateDoors = candidateDoors;
    }

    public int getInitialise()
    {
        return initialise;
    }

    public int getStart()
    {
        return start;
    }

    public int getStop()
    {
        return stop;
    }

    public int getDispose()
    {
        return dispose;
    }

    public HealthStatus getInitialHealth()
    {
        return initialHealth;
    }

    public void setInitialHealth(HealthStatus initialHealth)
    {
        this.initialHealth = initialHealth;
    }

    public HealthStatus getFinalHealth()
    {
        return finalHealth;
    }

    public void setFinalHealth(HealthStatus finalHealth)
    {
        this.finalHealth = finalHealth;
    }

    @Override
    public void setMuleContext(MuleContext context)
    {
        muleContext = context;
    }

    public MuleContext getMuleContext()
    {
        return muleContext;
    }
}
