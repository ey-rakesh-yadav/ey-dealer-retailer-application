package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.SclSiteMasterModel;
import com.scl.facades.data.SclSiteMasterData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SclSiteMasterPopulator implements Populator<SclSiteMasterModel, SclSiteMasterData> {


    private Converter<AddressModel, AddressData> addressConverter;

    @Override
    public void populate(SclSiteMasterModel sclSiteMasterModel, SclSiteMasterData sclSiteMasterData) throws ConversionException {

    	sclSiteMasterData.setCode(sclSiteMasterModel.getUid());
    	sclSiteMasterData.setName(sclSiteMasterModel.getName());
    	sclSiteMasterData.setTotalVisits(sclSiteMasterModel.getCurrentMonthSiteVisit());
    	sclSiteMasterData.setConstructionStage(String.valueOf(sclSiteMasterModel.getConstructionStage()));
    	//sclSiteMasterData.setOrderPlaced(sclSiteMasterModel.getCurrentMonthSale());
		sclSiteMasterData.setOrderPlaced(getTwoDeimalPlaceFormat(sclSiteMasterModel.getNumberOfBagsPurchased()));
    	sclSiteMasterData.setActive(sclSiteMasterModel.getSiteActive());
    	sclSiteMasterData.setSiteStatus(String.valueOf(sclSiteMasterModel.getSiteStatus()));
		sclSiteMasterData.setOrderCount(sclSiteMasterModel.getOrderCount());


    	if (sclSiteMasterModel.getLastVisitTime()!= null) {
    		DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
    		String lastVisitDate = dateFormat.format(sclSiteMasterModel.getLastVisitTime());
    		sclSiteMasterData.setLastVisitDate(lastVisitDate);
    	}

    	if (sclSiteMasterModel.getNextSlabCasting()!= null) {
    		DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
    		String nextSlabCasting = dateFormat.format(sclSiteMasterModel.getNextSlabCasting());
    		sclSiteMasterData.setNextSlabCasting(nextSlabCasting);
    	}

    	if(null != sclSiteMasterModel.getAddresses() && !sclSiteMasterModel.getAddresses().isEmpty()){
			for (AddressModel address : sclSiteMasterModel.getAddresses()) {
				populateDeliveryAddress(address,sclSiteMasterData);
				break;
			}
    	}
    }

    private void populateDeliveryAddress(final AddressModel deliveryAddress , final SclSiteMasterData target){
        target.setSiteAddress(getAddressConverter().convert(deliveryAddress));
    }

    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }
	private double getTwoDeimalPlaceFormat(double value){
		return  new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}


}
