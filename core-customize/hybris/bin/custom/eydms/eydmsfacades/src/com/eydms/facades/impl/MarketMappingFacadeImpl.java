package com.eydms.facades.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.PotentialCustomerStage;
import com.eydms.core.model.BrandWiseSaleModel;
import com.eydms.core.model.CustomerSubAreaMappingModel;
import com.eydms.core.model.LeadMasterModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.services.DJPVisitService;
import com.eydms.core.services.MarketMappingService;
import com.eydms.core.services.NetworkService;
import com.eydms.facades.MarketMappingFacade;
import com.eydms.facades.djp.data.CounterMappingData;
import com.eydms.facades.prosdealer.data.BrandWiseSaleData;
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
	private Populator<CounterMappingData,EyDmsCustomerModel> counterMappingReversePopulator;
	
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
		EyDmsCustomerModel eydmsCustomer = modelService.create(EyDmsCustomerModel.class);
		counterMappingReversePopulator.populate(counterData, eydmsCustomer);
		String type = counterData.getCustomerType();
		marketMappingService.saveCounter(eydmsCustomer, type, routeId,counterData.getBrand());
		
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

		 customerAccountService.saveAddressEntry(eydmsCustomer, newAddress);
		eydmsCustomer.setPotentialCustomerStage(PotentialCustomerStage.CONTACTED);
		eydmsCustomer.setDateOfJoining(new Date());
		eydmsCustomer.setDefaultShipmentAddress(newAddress);
        eydmsCustomer.setLatitude(addressData.getLatitude());
        eydmsCustomer.setLongitude(addressData.getLongitude());
		eydmsCustomer.setContactPersonName(counterData.getContactPersonName());
		eydmsCustomer.setCreatedBy((B2BCustomerModel) userService.getCurrentUser());//SO
        if(CollectionUtils.isNotEmpty(counterData.getBusinessInfo())){
			eydmsCustomer.setBrandWiseSales(brandWiseSaleReverseConverter.convertAll(counterData.getBusinessInfo()));
		}
		if(counterData.getOtherBrands()!=null && !counterData.getOtherBrands().isEmpty()) {
			List<String> brands = new ArrayList<>(counterData.getOtherBrands());
			LOG.info("brands:"+brands);
			eydmsCustomer.setOtherBrands(brands);
		}
		modelService.save(eydmsCustomer);


		createCounterMappingEntry(eydmsCustomer,newAddress,counterData.getBrand(),routeId);
		counterData.setId(eydmsCustomer.getUid());
		counterData.setAddress(addressData);
		counterData.setEnableFormCompletion(false);

 
		
		return counterData;
	}

	private void createCounterMappingEntry(EyDmsCustomerModel eydmsCustomer, AddressModel addressModel, String baseSite, String route) {

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
		if(eydmsCustomer.getRoute()!=null) {
			routeId = eydmsCustomer.getRoute().getRouteId();
			routeName = eydmsCustomer.getRoute().getRouteName();
		}



		EyDmsUserModel employee = (EyDmsUserModel) userService.getCurrentUser();

		if(employee!=null && employee.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID)) && StringUtils.isNotBlank(baseSite))
		{
			brand = baseSiteService.getBaseSiteForUID(baseSite);
		}
		else {
			brand = baseSiteService.getCurrentBaseSite();
		}

		if(StringUtils.isNotBlank(route)) {
			djpVisitService.createCounterRouteMapping(state, district, taluka, brand, eydmsCustomer.getUid(), employee, routeId, routeName);
		}

		CustomerSubAreaMappingModel customerSubAreaMapping = djpVisitService.createCustomerSubAreaMapping(state, district, taluka, eydmsCustomer, (CMSSiteModel) brand);
		modelService.save(customerSubAreaMapping);
	}


}
