package com.scl.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

import de.hybris.platform.core.GenericSearchConstants;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.depot.operations.dao.DepotOperationsDao;
import com.scl.core.model.ISOMasterModel;
import com.scl.facades.data.ISOVisibilityData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class ISOVisibilityPopulator implements Populator<ISOMasterModel,ISOVisibilityData>{

	@Autowired
	DepotOperationsDao depotOperationsDao;

	@Autowired
	ProductService productService;
	
	@Override
	public void populate(ISOMasterModel source, ISOVisibilityData target) throws ConversionException {
		target.setDeliveryId(source.getDeliveryId());
		target.setDeliveryDetailId(source.getDeliveryDetailId());
		final Logger LOGGER = LoggerFactory.getLogger(ISOVisibilityPopulator.class);

		//String prod = depotOperationsDao.getProductForISOVisibility(source.getGrade(), source.getShipmentState(), source.getPackagingMaterial(), source.getBrand());

		try {
			ProductModel productModel = productService.getProductForCode(source.getProductCode());
			if (Objects.nonNull(productModel)) {
				target.setProductName(productModel.getName());
			}
		}catch(Exception e){
			LOGGER.error("Exception in getting Product name:"+e.getMessage()+ " "+ e.getCause());
		}

		target.setQtyDelivered(source.getQtyDelivered());
		target.setQtyShipped(source.getQtyShipped());
		target.setVehicleType(source.getVehicleType());
		target.setVehicleNo(source.getVehicleNo());
		target.setTransporterName(source.getTransporterName());
		String space=" ";
		if((source.getTransporterMobile()==null) || source.getTransporterMobile().equalsIgnoreCase(SclCoreConstants.INVALID_MOBILE_NUMBER)){
			target.setTransporterMobile(space);
		}else {
			target.setTransporterMobile(source.getTransporterMobile());
		}

		if((source.getDriverMobile()==null) || source.getDriverMobile().equalsIgnoreCase(SclCoreConstants.INVALID_MOBILE_NUMBER)){
			target.setDriverMobile(space);
		}else{
			target.setDriverMobile(source.getDriverMobile());
		}
		target.setDriverName(source.getDriverName());
		target.setPackagingType(source.getPackagingType());
		target.setPackagingMaterial(source.getPackagingMaterial());
		target.setDeliveryMode(source.getDeliveryMode());
		target.setCustomerCategory(source.getCustomerCategory());
		target.setDepotCode(source.getDepotCode());
		if(source.getDepotCode()!=null){
			String depotCodeName = depotOperationsDao.getDepotCodeName(source.getDepotCode());
			if(depotCodeName!=null)
				target.setDepotName(depotCodeName);
		}

		target.setPlantCode(source.getPlantCode());
		target.setCustomer(source.getCustomer());
		target.setConsignee(source.getConsignee());
		target.setShipmentState(source.getShipmentState());
		//new field
		target.setTokenNumber(source.getTokenNumber());
		if(target.getTokenNumber()!=null && target.getVehicleNo()!=null){
			target.setIsGpsEnabled(true);
		}


		if(source.getEtaDate()!=null) {
			target.setErpEtaDate(source.getEtaDate());
		}

		//DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy h:mm a");
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy h:mm a");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
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
