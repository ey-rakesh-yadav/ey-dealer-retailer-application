package com.scl.facades.populators.djp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.CounterVisitMasterModel;
import com.scl.facades.data.CounterVisitMasterData;
import com.scl.facades.data.MarketMappingDetailsData;

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
				//Skip the MarketMapping record if the product is inactive
				if(Objects.nonNull(marketMapping.getProduct()) && !(marketMapping.getProduct().getActive())) {
					return;
				}
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
		
		UserGroupModel userGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		if(source.getSclCustomer().getGroups().contains(userGroup))
		{
			target.setIsShreeCounter(source.getIsShreeCounter());
		}
	}

}
	