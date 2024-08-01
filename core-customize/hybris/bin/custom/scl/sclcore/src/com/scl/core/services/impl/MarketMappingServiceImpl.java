package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.CounterType;
import com.scl.core.model.RouteMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.MarketMappingService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MarketMappingServiceImpl implements MarketMappingService{

	@Resource
	private UserService userService;
	
	@Resource
	private ModelService modelService;
	
	@Resource
	private BaseSiteService baseSiteService;
	
	@Resource
	private B2BUnitService b2bUnitService;
	
	@Resource
	private FlexibleSearchService flexibleSearchService;
	
	final static String e = CounterType.RETAILER.getCode().toString();
	
	@Override
	public void saveCounter(SclCustomerModel sclCustomer, String type, String routeId,String brand) {

		UserGroupModel userGroup=null;
		CounterType counterType = null;
		Set<PrincipalGroupModel> groups = new HashSet<PrincipalGroupModel>();

		switch(type)
		{
		case SclCoreConstants.COUNTER_TYPE.RETAILER:
			userGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
			counterType = CounterType.RETAILER;
			break;
		case SclCoreConstants.COUNTER_TYPE.DEALER :
			userGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
			counterType = CounterType.DEALER;
			break;
		case SclCoreConstants.COUNTER_TYPE.INFLUENCER :
			userGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
			counterType = CounterType.INFLUENCER;
			break;
		case SclCoreConstants.COUNTER_TYPE.SITE :
			userGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
			counterType = CounterType.SITE;
			break;
		}

		groups.add(userGroup);
		sclCustomer.setGroups(groups);
		if(counterType!=null) {
			sclCustomer.setCounterType(counterType);
		}
	//	String site = baseSiteService.getCurrentBaseSite().getUid();
		CompanyModel b2bUnit=null;
		if(brand!=null){
		/*	switch(brand)
			{
				case SclCoreConstants.SITE.SCL_SITE:
					b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
					break;
				case SclCoreConstants.SITE.BANGUR_SITE :
					b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID);
					break;
				case SclCoreConstants.SITE.ROCKSTRONG_SITE:
					b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID);
					break;
				default:
					b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
			}*/
			if(brand.equalsIgnoreCase("scl")) {
				b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
			}
			else {
				b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_OTHER_UNIT_UID);
			}
		}else {
			b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_OTHER_UNIT_UID);
		}

		/*switch(site)
		{
		case SclCoreConstants.SITE.SHREE_SITE :
			b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
			break;
		case SclCoreConstants.SITE.BANGUR_SITE :
			b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID);
			break;
		case SclCoreConstants.SITE.ROCKSTRONG_SITE:
			b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID);
			break;
		}*/

		if(null != b2bUnit){
			sclCustomer.setDefaultB2BUnit((B2BUnitModel) b2bUnit);
		}
		sclCustomer.setSo((SclUserModel) userService.getCurrentUser());
		if(StringUtils.isNotBlank(routeId)) {
			RouteMasterModel routeMaster = new RouteMasterModel();
			routeMaster.setRouteId(routeId);
			RouteMasterModel route = flexibleSearchService.getModelByExample(routeMaster);
			sclCustomer.setRoute(route);
		}
		modelService.save(sclCustomer);

	}

}
