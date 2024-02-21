package com.eydms.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.depot.operations.dao.DepotOperationsDao;
import com.eydms.core.model.ISOMasterModel;
import com.eydms.facades.data.ISOVisibilityData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class ISOVisibilityPopulator implements Populator<ISOMasterModel,ISOVisibilityData>{

	@Autowired
	DepotOperationsDao depotOperationsDao;
	
	@Override
	public void populate(ISOMasterModel source, ISOVisibilityData target) throws ConversionException {
		target.setDeliveryId(source.getDeliveryId());
		target.setDeliveryDetailId(source.getDeliveryDetailId());
		
		String prod = depotOperationsDao.getProductForISOVisibility(source.getGrade(), source.getShipmentState(), source.getPackagingMaterial(), source.getBrand());
		target.setProductName(prod);
    
		target.setQtyDelivered(source.getQtyDelivered());
		target.setQtyShipped(source.getQtyShipped());
		target.setVehicleType(source.getVehicleType());
		target.setVehicleNo(source.getVehicleNo());
		target.setTransporterName(source.getTransporterName());
		target.setTransporterMobile(source.getTransporterMobile());
		target.setDriverMobile(source.getDriverMobile());
		target.setDriverName(source.getDriverName());
		target.setPackagingType(source.getPackagingType());
		target.setPackagingMaterial(source.getPackagingMaterial());
		target.setDeliveryMode(source.getDeliveryMode());
		target.setCustomerCategory(source.getCustomerCategory());
		target.setDepotCode(source.getDepotCode());
		target.setPlantCode(source.getPlantCode());
		target.setCustomer(source.getCustomer());
		target.setConsignee(source.getConsignee());
		target.setShipmentState(source.getShipmentState());

		if(source.getEtaDate()!=null) {
			target.setErpEtaDate(source.getEtaDate());
		}
		
		DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy h:mm a");
		if(source.getActualDepartureDate()!=null)
		{
			target.setActualDepartureDate(formatter.format(source.getActualDepartureDate()));
		}
		if(source.getReceiptDate()!=null)
		{
			target.setReceiptDate(formatter.format(source.getReceiptDate()));
		}
		
		target.setFromOrgName(source.getFromOrgName());
		target.setBrand(source.getBrand());
		target.setGrade(source.getGrade());
	}

}
