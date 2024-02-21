package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.EyDmsSiteMasterModel;
import com.eydms.facades.data.EyDmsSiteMasterData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EyDmsSiteMasterPopulator implements Populator<EyDmsSiteMasterModel, EyDmsSiteMasterData> {


    private Converter<AddressModel, AddressData> addressConverter;

    @Override
    public void populate(EyDmsSiteMasterModel eydmsSiteMasterModel, EyDmsSiteMasterData eydmsSiteMasterData) throws ConversionException {

    	eydmsSiteMasterData.setCode(eydmsSiteMasterModel.getUid());
    	eydmsSiteMasterData.setName(eydmsSiteMasterModel.getName());
    	eydmsSiteMasterData.setTotalVisits(eydmsSiteMasterModel.getCurrentMonthSiteVisit());
    	eydmsSiteMasterData.setConstructionStage(String.valueOf(eydmsSiteMasterModel.getConstructionStage()));
    	eydmsSiteMasterData.setOrderPlaced(eydmsSiteMasterModel.getCurrentMonthSale());
    	eydmsSiteMasterData.setActive(eydmsSiteMasterModel.getSiteActive());
    	eydmsSiteMasterData.setSiteStatus(String.valueOf(eydmsSiteMasterModel.getSiteStatus()));


    	if (eydmsSiteMasterModel.getLastVisitTime()!= null) {
    		DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
    		String lastVisitDate = dateFormat.format(eydmsSiteMasterModel.getLastVisitTime());
    		eydmsSiteMasterData.setLastVisitDate(lastVisitDate);
    	}

    	if (eydmsSiteMasterModel.getNextSlabCasting()!= null) {
    		DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
    		String nextSlabCasting = dateFormat.format(eydmsSiteMasterModel.getNextSlabCasting());
    		eydmsSiteMasterData.setNextSlabCasting(nextSlabCasting);
    	}

    	if(null != eydmsSiteMasterModel.getAddresses() && !eydmsSiteMasterModel.getAddresses().isEmpty()){
			for (AddressModel address : eydmsSiteMasterModel.getAddresses()) {
				populateDeliveryAddress(address,eydmsSiteMasterData);
				break;
			}
    	}
    }

    private void populateDeliveryAddress(final AddressModel deliveryAddress , final EyDmsSiteMasterData target){
        target.setSiteAddress(getAddressConverter().convert(deliveryAddress));
    }

    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }


}
