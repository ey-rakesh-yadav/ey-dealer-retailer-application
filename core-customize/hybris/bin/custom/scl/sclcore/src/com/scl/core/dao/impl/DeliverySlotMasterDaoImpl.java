package com.scl.core.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Collections;
import java.util.List;

import com.scl.core.dao.DeliverySlotMasterDao;
import com.scl.core.enums.DeliverySlots;
import com.scl.core.model.DeliverySlotMasterModel;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class DeliverySlotMasterDaoImpl extends DefaultGenericDao<DeliverySlotMasterModel> implements DeliverySlotMasterDao  {

    public DeliverySlotMasterDaoImpl() {
        super(DeliverySlotMasterModel._TYPECODE);
    }

	@Override
	public List<DeliverySlotMasterModel> findAll() {
		return this.find();
	}

	@Override
	public DeliverySlotMasterModel findByDisplayName(String displayName) {
		validateParameterNotNullStandardMessage("displayName", displayName);
		final List<DeliverySlotMasterModel> slotList = this.find(Collections.singletonMap(DeliverySlotMasterModel.DISPLAYNAME, displayName));
		if (slotList.size() > 1)
		{
			throw new AmbiguousIdentifierException(
					String.format("Found %d delivery slots with the displayName value: '%s', which should be unique", slotList.size(),
							displayName));
		}
		else
		{
			return slotList.isEmpty() ? null : slotList.get(0);
		}
	}

	@Override
	public DeliverySlotMasterModel findByCentreTime(String centreTime) {
		validateParameterNotNullStandardMessage("centreTime", centreTime);
		final List<DeliverySlotMasterModel> slotList = this.find(Collections.singletonMap(DeliverySlotMasterModel.CENTRETIME, centreTime));
		if (slotList.size() > 1)
		{
			throw new AmbiguousIdentifierException(
					String.format("Found %d delivery slots with the centreTime value: '%s', which should be unique", slotList.size(),
							centreTime));
		}
		else
		{
			return slotList.isEmpty() ? null : slotList.get(0);
		}
	}
	
	@Override
	public DeliverySlotMasterModel findByEnum(String enumCode) {
		validateParameterNotNullStandardMessage("enumCode", enumCode);
		DeliverySlots slot = DeliverySlots.valueOf(enumCode);
		final List<DeliverySlotMasterModel> slotList = this.find(Collections.singletonMap(DeliverySlotMasterModel.SLOT, slot));
		if (slotList.size() > 1)
		{
			throw new AmbiguousIdentifierException(
					String.format("Found %d delivery slots with the enumCode value: '%s', which should be unique", slotList.size(),
							enumCode));
		}
		else
		{
			return slotList.isEmpty() ? null : slotList.get(0);
		}
	}
}
