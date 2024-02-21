package com.eydms.core.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.CounterType;
import com.eydms.core.model.RouteMasterModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.services.MarketMappingService;
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
	public void saveCounter(EyDmsCustomerModel eydmsCustomer, String type, String routeId,String brand) {

		UserGroupModel userGroup=null;
		CounterType counterType = null;
		Set<PrincipalGroupModel> groups = new HashSet<PrincipalGroupModel>();

		switch(type)
		{
		case EyDmsCoreConstants.COUNTER_TYPE.RETAILER:
			userGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
			counterType = CounterType.RETAILER;
			break;
		case EyDmsCoreConstants.COUNTER_TYPE.DEALER :
			userGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
			counterType = CounterType.DEALER;
			break;
		case EyDmsCoreConstants.COUNTER_TYPE.INFLUENCER :
			userGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
			counterType = CounterType.INFLUENCER;
			break;
		case EyDmsCoreConstants.COUNTER_TYPE.SITE :
			userGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
			counterType = CounterType.SITE;
			break;
		}

		groups.add(userGroup);
		eydmsCustomer.setGroups(groups);
		if(counterType!=null) {
			eydmsCustomer.setCounterType(counterType);
		}
	//	String site = baseSiteService.getCurrentBaseSite().getUid();
		CompanyModel b2bUnit=null;
		if(brand!=null){
			switch(brand)
			{
				case EyDmsCoreConstants.SITE.SHREE_SITE :
					b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID);
					break;
				case EyDmsCoreConstants.SITE.BANGUR_SITE :
					b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID);
					break;
				case EyDmsCoreConstants.SITE.ROCKSTRONG_SITE:
					b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID);
					break;
				default:
					b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_OTHER_UNIT_UID);
			}
		}else {
			b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_OTHER_UNIT_UID);
		}

		/*switch(site)
		{
		case EyDmsCoreConstants.SITE.SHREE_SITE :
			b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID);
			break;
		case EyDmsCoreConstants.SITE.BANGUR_SITE :
			b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID);
			break;
		case EyDmsCoreConstants.SITE.ROCKSTRONG_SITE:
			b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID);
			break;
		}*/

		if(null != b2bUnit){
			eydmsCustomer.setDefaultB2BUnit((B2BUnitModel) b2bUnit);
		}
		eydmsCustomer.setSo((EyDmsUserModel) userService.getCurrentUser());
		if(StringUtils.isNotBlank(routeId)) {
			RouteMasterModel routeMaster = new RouteMasterModel();
			routeMaster.setRouteId(routeId);
			RouteMasterModel route = flexibleSearchService.getModelByExample(routeMaster);
			eydmsCustomer.setRoute(route);
		}
		modelService.save(eydmsCustomer);

	}

}
