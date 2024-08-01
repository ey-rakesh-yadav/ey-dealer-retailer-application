package com.scl.integration.cpi.hook.service;

import com.scl.integration.cpi.hook.exception.DuplicateOrderRuntimeException;
import com.scl.integration.cpi.hook.exception.SclPersistenceHookExecutionException;
import com.scl.integration.cpi.hook.exception.ValidTransportationZoneNotFoundException;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.inboundservices.persistence.PersistenceContext;
import de.hybris.platform.inboundservices.persistence.hook.PersistenceHookProvider;
import de.hybris.platform.inboundservices.persistence.hook.PostPersistHook;
import de.hybris.platform.inboundservices.persistence.hook.PrePersistHook;
import de.hybris.platform.inboundservices.persistence.hook.impl.DefaultPersistenceHookExecutor;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookExecutionException;
import de.hybris.platform.inboundservices.persistence.hook.impl.PersistenceHookNotFoundException;
import de.hybris.platform.integrationservices.util.Log;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SclPersistenceService  extends DefaultPersistenceHookExecutor {


    private static final Logger LOG = Log.getLogger(DefaultPersistenceHookExecutor.class);

    private final List<PersistenceHookProvider> hookProviders = new ArrayList<>();

    @Override
    public Optional<ItemModel> runPrePersistHook(final ItemModel item,
                                                 final PersistenceContext context)
    {
        final String hookName = context.getPrePersistHook();
        return StringUtils.isBlank(hookName)
                ? Optional.of(item)
                : executePrePersistHook(findPrePersistHooks(context, hookName), item, context);
    }

    private PrePersistHook findPrePersistHooks(final PersistenceContext context, final String hookName)
    {
        return hookProviders.stream()
                .map(provider -> provider.getPrePersistHook(context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new PersistenceHookNotFoundException(context, hookName));
    }


    private Optional<ItemModel> executePrePersistHook(final PrePersistHook hook, final ItemModel item, final PersistenceContext context)
    {
        try
        {
            LOG.info("Executing Pre-Persist-Hook: [{}] with item: [{}]", hook, item);
            return hook.execute(item, context);
        }
        catch (final DuplicateOrderRuntimeException e)
        {
            throw new SclPersistenceHookExecutionException(context, context.getPrePersistHook(), e);
        }
        catch (final ValidTransportationZoneNotFoundException e)
        {
            throw new SclPersistenceHookExecutionException(context, context.getPrePersistHook(), e);
        }
        catch (final RuntimeException e)
        {
            throw new PersistenceHookExecutionException(context, context.getPrePersistHook(), e);
        }
    }

    public void setHookProviders(@NotNull final List<PersistenceHookProvider> providers)
    {
        hookProviders.clear();
        hookProviders.addAll(providers);
    }

    List<PersistenceHookProvider> getHookProviders() {
        return new ArrayList<>(this.hookProviders);
    }


    @Override
    public void runPostPersistHook(final ItemModel item, final PersistenceContext context)
    {
        final String hookName = context.getPostPersistHook();
        if (StringUtils.isNotBlank(hookName))
        {
            final Optional<PostPersistHook> hook = hookProviders.stream()
                    .map(provider -> provider.getPostPersistHook(context))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            hook.ifPresentOrElse(h -> executePostPersistHook(h, item, context), () -> reportHookNotFound(context, hookName));
        }
    }


    private void executePostPersistHook(final PostPersistHook hook, final ItemModel item, final PersistenceContext context)
    {
        try
        {
            LOG.info("Executing Post-Persist-Hook: [{}] with item: [{}]", hook, item);
            hook.execute(item, context);
        } catch (final RuntimeException e)
        {
            throw new PersistenceHookExecutionException(context, context.getPostPersistHook(), e);
        }
    }

    private void reportHookNotFound(final PersistenceContext ctx, final String hookName)
    {
        throw new PersistenceHookNotFoundException(ctx, hookName);
    }

}
