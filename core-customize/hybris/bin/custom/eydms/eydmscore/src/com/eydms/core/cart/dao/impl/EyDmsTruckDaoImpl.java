package com.eydms.core.cart.dao.impl;

import java.util.Collections;
import java.util.List;

import com.eydms.core.cart.dao.EyDmsTruckDao;
import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.core.model.TruckModelMasterModel;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class EyDmsTruckDaoImpl extends DefaultGenericDao<TruckModelMasterModel> implements EyDmsTruckDao{

	public EyDmsTruckDaoImpl() {
		super(TruckModelMasterModel._TYPECODE);
	}

	@Override
	public List<TruckModelMasterModel> findAllTrucks() {
		return this.find();
	}

	@Override
	public TruckModelMasterModel findTruckModelByModelNo(String truckModelNo) {
		validateParameterNotNullStandardMessage("truckModelNo", truckModelNo);
		final List<TruckModelMasterModel> truckModelList = this.find(Collections.singletonMap(TruckModelMasterModel.TRUCKMODEL, truckModelNo));
		if (truckModelList.size() > 1)
		{
			throw new AmbiguousIdentifierException(
					String.format("Found %d truck model with the truckModelNo value: '%s', which should be unique", truckModelList.size(),
							truckModelNo));
		}
		else
		{
			return truckModelList.isEmpty() ? null : truckModelList.get(0);
		}
	}

}
