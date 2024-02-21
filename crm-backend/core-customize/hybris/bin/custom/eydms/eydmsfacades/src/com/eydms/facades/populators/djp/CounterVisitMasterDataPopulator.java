package com.eydms.facades.populators.djp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.facades.data.CounterVisitMasterData;
import com.eydms.facades.data.MarketMappingDetailsData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;

public class CounterVisitMasterDataPopulator implements Populator<CounterVisitMasterModel,CounterVisitMasterData> {

	@Autowired
	UserService userService;
	
	@Override
	public void populate(CounterVisitMasterModel source, CounterVisitMasterData target) throws ConversionException {
		
		try {
			target.setLastVisitDate(source.getLastVisitDate());
		}catch(NullPointerException e)
		{
			target.setLastVisitDate(null);
		}
		
		target.setTotalSale(source.getTotalSale());
		target.setWholeSale(source.getWholeSale());
		target.setCounterSale(source.getCounterSale());
		target.setCounterShare(source.getCounterShare());
		
		List<MarketMappingDetailsData> dataList = new ArrayList<>();
		
		if(source.getMarketMapping()!= null && !source.getMarketMapping().isEmpty()) 
		{
			source.getMarketMapping().forEach(marketMapping -> {
				MarketMappingDetailsData data = new MarketMappingDetailsData();
				data.setBrand(marketMapping.getBrand()!=null?marketMapping.getBrand().getIsocode():null);
				data.setProduct(marketMapping.getProduct()!=null?marketMapping.getProduct().getCode():null);
				data.setWholesalePrice(marketMapping.getWholesalePrice());
				data.setRetailsalePrice(marketMapping.getRetailsalePrice());
				data.setDiscount(marketMapping.getDiscount());
				data.setBilling(marketMapping.getBilling());
				data.setWholeSales(marketMapping.getWholeSales());
				data.setRetailSales(marketMapping.getRetailSales());
				dataList.add(data);
			});
		}
		target.setMarketMappingList(dataList);
		
		UserGroupModel userGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		if(source.getEyDmsCustomer().getGroups().contains(userGroup))
		{
			target.setIsShreeCounter(source.getIsShreeCounter());
		}
	}

}
	