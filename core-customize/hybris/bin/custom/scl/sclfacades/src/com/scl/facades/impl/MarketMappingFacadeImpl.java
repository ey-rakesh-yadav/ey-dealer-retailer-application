package com.scl.facades.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.PotentialCustomerStage;
import com.scl.core.model.BrandWiseSaleModel;
import com.scl.core.model.CustomerSubAreaMappingModel;
import com.scl.core.model.LeadMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.MarketMappingService;
import com.scl.core.services.NetworkService;
import com.scl.facades.MarketMappingFacade;
import com.scl.facades.djp.data.CounterMappingData;
import com.scl.facades.prosdealer.data.BrandWiseSaleData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MarketMappingFacadeImpl implements MarketMappingFacade {

	private static final Logger LOG = Logger.getLogger(MarketMappingFacadeImpl.class);

	@Resource
	private UserService userService;
	
	@Resource
	private ModelService modelService;
	
	@Resource
	private MarketMappingService marketMappingService;
	
	@Resource
	private CustomerAccountService customerAccountService;
	
	@Resource
	private Populator<CounterMappingData,SclCustomerModel> counterMappingReversePopulator;
	
	@Resource
	private Populator<AddressData, AddressModel> addressReversePopulator;

	@Resource
	private DJPVisitService djpVisitService;

	@Resource
	private BaseSiteService baseSiteService;

	@Resource
	private NetworkService networkService;
	@Resource
	private Converter<BrandWiseSaleData, BrandWiseSaleModel> brandWiseSaleReverseConverter;
	
	@Override
	public CounterMappingData addCounter(CounterMappingData counterData, String routeId,String leadId) {
		SclCustomerModel sclCustomer = modelService.create(SclCustomerModel.class);
		counterMappingReversePopulator.populate(counterData, sclCustomer);
		String type = counterData.getCustomerType();
		marketMappingService.saveCounter(sclCustomer, type, routeId,counterData.getBrand());
		
		AddressModel newAddress = modelService.create(AddressModel.class);
		AddressData addressData = counterData.getAddress();
		addressReversePopulator.populate(addressData, newAddress);
		newAddress.setBillingAddress(true);

		LeadMasterModel lead= null;
		 if(Objects.nonNull(leadId)) {
			lead= networkService.findItemByUidParam(leadId);
			if (Objects.nonNull(lead)) {
				lead.setEnableFormCompletion(false);
				modelService.save(lead);
			}
		}

		 customerAccountService.saveAddressEntry(sclCustomer, newAddress);
		sclCustomer.setPotentialCustomerStage(PotentialCustomerStage.CONTACTED);
		sclCustomer.setDateOfJoining(new Date());
		sclCustomer.setDefaultShipmentAddress(newAddress);
        sclCustomer.setLatitude(addressData.getLatitude());
        sclCustomer.setLongitude(addressData.getLongitude());
		sclCustomer.setContactPersonName(counterData.getContactPersonName());
		sclCustomer.setCreatedBy((B2BCustomerModel) userService.getCurrentUser());//SO
        if(CollectionUtils.isNotEmpty(counterData.getBusinessInfo())){
			sclCustomer.setBrandWiseSales(brandWiseSaleReverseConverter.convertAll(counterData.getBusinessInfo()));
		}
		if(counterData.getOtherBrands()!=null && !counterData.getOtherBrands().isEmpty()) {
			List<String> brands = new ArrayList<>(counterData.getOtherBrands());
			LOG.info("brands:"+brands);
			sclCustomer.setOtherBrands(brands);
		}
		modelService.save(sclCustomer);


		createCounterMappingEntry(sclCustomer,newAddress,counterData.getBrand(),routeId);
		counterData.setId(sclCustomer.getUid());
		counterData.setAddress(addressData);
		counterData.setEnableFormCompletion(false);

 
		
		return counterData;
	}

	private void createCounterMappingEntry(SclCustomerModel sclCustomer, AddressModel addressModel, String baseSite, String route) {

		String taluka = StringUtils.EMPTY;
		String state = StringUtils.EMPTY;
		String district = StringUtils.EMPTY;
		String routeId = StringUtils.EMPTY;
		String routeName = StringUtils.EMPTY;
		BaseSiteModel brand=null;
		if(null!= addressModel){
			taluka = addressModel.getTaluka();
			state = addressModel.getState();
			district = addressModel.getDistrict();
		}
		if(sclCustomer.getRoute()!=null) {
			routeId = sclCustomer.getRoute().getRouteId();
			routeName = sclCustomer.getRoute().getRouteName();
		}



		SclUserModel employee = (SclUserModel) userService.getCurrentUser();

//		if(employee!=null && employee.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID)) && StringUtils.isNotBlank(baseSite))
//		{
//			brand = baseSiteService.getBaseSiteForUID(baseSite);
//		}
//		else {
//			brand = baseSiteService.getCurrentBaseSite();
//		}

		brand = baseSiteService.getCurrentBaseSite();

		if(StringUtils.isNotBlank(route)) {
			djpVisitService.createCounterRouteMapping(state, district, taluka, brand, sclCustomer.getUid(), employee, routeId, routeName);
		}

		CustomerSubAreaMappingModel customerSubAreaMapping = djpVisitService.createCustomerSubAreaMapping(state, district, taluka, sclCustomer, (CMSSiteModel) brand);
		modelService.save(customerSubAreaMapping);
	}


}
