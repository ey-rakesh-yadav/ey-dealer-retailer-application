package com.eydms.core.territory.impl;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.model.TerritoryUnitModel;
import com.eydms.core.territory.TerritoryUnitService;

import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;

public class DefaultTerritoryUnitService implements TerritoryUnitService {

    private static final Logger LOG = Logger.getLogger(DefaultTerritoryUnitService.class);
    @Resource(name="userService")
    private UserService userService;
    private B2BUnitService b2bUnitService;

    @Override
    public TerritoryUnitModel getUnitForUid(String uid) {
        TerritoryUnitModel unit;
        try
        {
            unit = userService.getUserGroupForUID(uid, TerritoryUnitModel.class);
        }
        catch (final UnknownIdentifierException | ClassMismatchException e)
        {
            unit = null;
            LOG.error("Failed to get unit: {}. Cause: {}" + uid + " " + e.getMessage());
        }
        return unit;
    }
    
    @Override
    public TerritoryUnitModel getTerritotyUnitforUid(String uid) {
    	TerritoryUnitModel unit;
        try
        {
            unit = (TerritoryUnitModel) getB2bUnitService().getUnitForUid(uid);
        }
        catch (final UnknownIdentifierException | ClassMismatchException e)
        {
            unit = null;
            LOG.error("Failed to get unit: {}. Cause: {}" + uid + " " + e.getMessage());
        }
		return unit;
    }

	public B2BUnitService getB2bUnitService() {
		return b2bUnitService;
	}

	public void setB2bUnitService(B2BUnitService b2bUnitService) {
		this.b2bUnitService = b2bUnitService;
	}
    
}