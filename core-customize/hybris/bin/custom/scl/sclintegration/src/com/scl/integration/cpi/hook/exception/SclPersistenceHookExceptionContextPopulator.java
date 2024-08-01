package com.scl.integration.cpi.hook.exception;

import com.google.common.base.Preconditions;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookException;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookNotFoundException;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookType;
import de.hybris.platform.odata2services.odata.errors.PersistenceHookExceptionContextPopulator;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;

import javax.validation.constraints.NotNull;

public class SclPersistenceHookExceptionContextPopulator  extends PersistenceHookExceptionContextPopulator {

    public static final String DUPLICATE_CRM_ORDER_ERROR = "DuplicateCRMOrderError";
    public static final String INVALID_TRANSPORTATION_ZONE_ERROR = "ValidTransportationZoneNotFoundException";
    private static final String HOOK_NOT_FOUND_MSG_TEMPLATE = "%s [%s] does not exist. Payload will not be persisted.";
    private static final String HOOK_EXECUTION_MSG_TEMPLATE = "Exception occurred during the execution of %s: [%s].";
    private static final String ERROR_CODE_PRE = "pre_persist_error";
    private static final String ERROR_CODE_POST = "post_persist_error";
    private static final String ERROR_CODE_NOT_FOUND = "hook_not_found";
    private static final String PRE_PERSIST_HOOK = "PrePersistHook";
    private static final String POST_PERSIST_HOOK = "PostPersistHook";

    @Override
    public void populate(@NotNull final ODataErrorContext context)
    {
        Preconditions.checkArgument(context != null, "ODataErrorContext cannot be null");

        final var contextException = context.getException();

        if (contextException instanceof PersistenceHookException)
        {
            final var ex = (PersistenceHookException) context.getException();
            context.setHttpStatus(HttpStatusCodes.BAD_REQUEST);
            context.setInnerError(getIntegrationKey(ex));
            context.setErrorCode(selectErrorCode(ex));
            if (ex.getMessage().contains(DUPLICATE_CRM_ORDER_ERROR)){
                String[] exMsgs=ex.getMessage().split(":", 2);
                context.setMessage(exMsgs[1]);
                context.setHttpStatus(HttpStatusCodes.NOT_ACCEPTABLE);
                context.setErrorCode(DUPLICATE_CRM_ORDER_ERROR);
            }else if(ex.getMessage().contains(INVALID_TRANSPORTATION_ZONE_ERROR)) {
            	context.setMessage(ex.getMessage());
                context.setHttpStatus(HttpStatusCodes.EXPECTATION_FAILED);
                context.setErrorCode(INVALID_TRANSPORTATION_ZONE_ERROR);
            }
            else {
                context.setMessage(getMessage(ex));
            }
        }
    }

    private String getMessage(final PersistenceHookException ex)
    {
        final String hookType = getHookType(ex);
        return ex instanceof PersistenceHookNotFoundException
                ? String.format(HOOK_NOT_FOUND_MSG_TEMPLATE, hookType, ex.getHookName())
                : String.format(HOOK_EXECUTION_MSG_TEMPLATE, hookType, ex.getHookName());
    }

    private String getHookType(final PersistenceHookException ex)
    {
        if (ex.getPersistenceHookType() == PersistenceHookType.PRE)
        {
            return PRE_PERSIST_HOOK;
        }
        if (ex.getPersistenceHookType() == PersistenceHookType.POST)
        {
            return POST_PERSIST_HOOK;
        }
        return "PersistenceHook";
    }

    private String getIntegrationKey(final PersistenceHookException ex)
    {
        return ex.getPersistenceContext() != null
                ? ex.getPersistenceContext().getIntegrationItem().getIntegrationKey() : null;
    }

    private String selectErrorCode(final PersistenceHookException ex) {
        if (ex instanceof PersistenceHookNotFoundException)
        {
            return ERROR_CODE_NOT_FOUND;
        }
        return ex.getPersistenceHookType() == PersistenceHookType.POST
                ? ERROR_CODE_POST : ERROR_CODE_PRE;
    }

}
