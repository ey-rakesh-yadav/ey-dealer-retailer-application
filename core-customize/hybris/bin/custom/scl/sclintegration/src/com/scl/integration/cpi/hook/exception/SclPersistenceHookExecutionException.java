/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.scl.integration.cpi.hook.exception;

import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookException;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookExecutionException;

/**
 * This exception is thrown when a hook fails to execute during runtime
 */
public class SclPersistenceHookExecutionException extends PersistenceHookException
{
    private static final long serialVersionUID = 40576310898280932L;
    private static final String MESSAGE_TEMPLATE = "Exception occurred during the execution of hook [%s].";

    /**
     * Instantiates this exception
     *
     * @param context  a context for the persistence hook execution
     * @param hookName name of the hook that failed execution
     * @param e        exception thrown by the hook.
     */
    public SclPersistenceHookExecutionException(final PersistenceContext context, final String hookName, final DuplicateOrderRuntimeException e)
    {
        super(e.getMessage(), context, hookName, e);
    }
    
    public SclPersistenceHookExecutionException(final PersistenceContext context, final String hookName, final ValidTransportationZoneNotFoundException e)
    {
        super(e.getMessage(), context, hookName, e);
    }


}
