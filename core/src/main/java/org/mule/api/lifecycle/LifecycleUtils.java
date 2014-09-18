/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.api.lifecycle;

import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleException;
import org.mule.util.ArrayUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

public abstract class LifecycleUtils
{

    public static void applyPhaseIfNeeded(String phase, Object... objects) throws MuleException
    {
        checkArgument(!StringUtils.isBlank(phase), "phase cannot be blank");
        if (ArrayUtils.isEmpty(objects))
        {
            return;
        }

        for (Object object : objects)
        {
            if (object == null)
            {
                continue;
            }

            if (Initialisable.PHASE_NAME.equals(phase))
            {
                if (object instanceof Initialisable)
                {
                    ((Initialisable) object).initialise();
                }
            }
            else if (Startable.PHASE_NAME.equals(phase))
            {
                if (object instanceof Startable)
                {
                    ((Startable) object).start();
                }
            }
            else if (Stoppable.PHASE_NAME.equals(phase))
            {
                if (object instanceof Stoppable)
                {
                    ((Stoppable) object).stop();
                }
            }
            else if (Disposable.PHASE_NAME.equals(phase))
            {
                if (object instanceof Disposable)
                {
                    ((Disposable) object).dispose();
                }
            }
            else
            {
                throw new IllegalArgumentException("Unknown phase " + phase);
            }

        }
    }

    public static void initialiseIfNeeded(Object... objects) throws InitialisationException
    {
        if (ArrayUtils.isEmpty(objects))
        {
            return;
        }

        for (Object object : objects)
        {
            if (object instanceof Initialisable)
            {
                ((Initialisable) object).initialise();
            }
        }
    }

    public static void disposeIfNeeded(Logger logger, Object... objects)
    {
        try
        {
            applyPhaseIfNeeded(Disposable.PHASE_NAME, objects);
        }
        catch (Exception e)
        {
            logger.error("Found exception trying to dispose. Shutdown will continue", e);
        }
    }
}
