package com.scl.core.strategies.impl;

import java.util.Objects;

import javax.annotation.Nonnull;

import de.hybris.platform.commerceservices.order.EntryMergeFilter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;

public class EntryMergeFilterDeliveryWindow implements EntryMergeFilter
{
	@Override
	public Boolean apply(@Nonnull final AbstractOrderEntryModel candidate, @Nonnull final AbstractOrderEntryModel target)
	{
		return Boolean.valueOf(Objects.equals(candidate.getSequence(), target.getSequence()));
	}
}