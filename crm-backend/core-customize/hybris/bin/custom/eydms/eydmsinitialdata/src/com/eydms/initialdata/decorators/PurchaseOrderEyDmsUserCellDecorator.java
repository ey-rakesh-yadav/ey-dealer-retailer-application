package com.eydms.initialdata.decorators;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.model.EyDmsUserModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.impex.jalo.header.AbstractColumnDescriptor;
import de.hybris.platform.impex.jalo.header.HeaderValidationException;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.CSVCellDecorator;
import de.hybris.platform.core.Registry;

public class PurchaseOrderEyDmsUserCellDecorator implements CSVCellDecorator{

	
	 protected BaseSiteService getBaseSiteService()
	    {
	        return (BaseSiteService) Registry.getApplicationContext().getBean("baseSiteService");
	    }
	 
	 protected TerritoryManagementDao getTerritoryManagementDao()
	    {
	        return (TerritoryManagementDao) Registry.getApplicationContext().getBean("territoryManagementDao");
	    }

	 
	//To be Checked
	@Override
	public String decorate(int position, Map<Integer, String> srcLine) {
		
		String siteId = srcLine.get(position);
		
		BaseSiteModel site = getBaseSiteService().getBaseSiteForUID(siteId);
		
		String taluka = srcLine.get(2);
		
		EyDmsUserModel user = getTerritoryManagementDao().getEyDmsUserForSubArea(taluka,site);
		
		return user.getUid();
	}

}
