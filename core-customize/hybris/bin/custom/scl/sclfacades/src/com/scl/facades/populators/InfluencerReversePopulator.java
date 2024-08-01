package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclEndCustomerDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.CustomerOnboardingStatus;
import com.scl.core.enums.InfluencerType;
import com.scl.core.enums.NotificationCategory;
import com.scl.core.model.*;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.NetworkService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.InfluencerData;
import com.scl.facades.data.SCLAddressData;

import com.scl.facades.populators.complaints.EndCustomerComplaintReversePopulator;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class InfluencerReversePopulator implements Populator<InfluencerData, SclCustomerModel> {
    @Resource
    private Converter<SCLAddressData, AddressModel> sclAddressReverseConverter;
    @Resource
    private ModelService modelService;
    @Resource
    private KeyGenerator customCodeGenerator;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private UserService userService;
    @Resource
    private DJPVisitService djpVisitService;

    @Autowired
    SclNotificationService sclNotificationService;
    @Resource
    private KeyGenerator applicationNumberGenerator;
    
    @Resource
    B2BUnitService b2bUnitService;

    @Autowired
    NetworkService networkService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    SclEndCustomerDao sclEndCustomerDao;

    private static final Logger LOG = Logger.getLogger(InfluencerReversePopulator.class);
    
    @Override
    public void populate(InfluencerData source, SclCustomerModel target) throws ConversionException {
        target.setUid(customCodeGenerator.generate().toString());
		
        if( userService.getCurrentUser()!=null)
		{
			if(userService.getCurrentUser() instanceof B2BCustomerModel)
			{
				target.setOnboardingPlacedBy((B2BCustomerModel) userService.getCurrentUser());
				target.setOnboardingPartner((B2BCustomerModel) userService.getCurrentUser());
			}
		}
        
        target.setInfluencerType(InfluencerType.valueOf(source.getInfluencerType()));
        
        CompanyModel b2bUnit = null;
        
        if(source.getBrand().equals(SclCoreConstants.SITE.SCL_SITE))
        {
        	 b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
        }

        if(null != b2bUnit){
			target.setDefaultB2BUnit((B2BUnitModel) b2bUnit);
		}
        
        Set<PrincipalGroupModel> ugSet=new HashSet<>();
        ugSet.add( userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID));
        target.setCounterType(CounterType.PROSPECTIVEINFLUENCER);
        target.setGroups(ugSet);
        target.setName(source.getName());
        target.setEmail(source.getAddress().getEmail());
        target.setApplicationNo(applicationNumberGenerator.generate().toString());
        target.setApplicationDate(new Date());
        Collection<AddressModel> addressModels=new ArrayList<>();
        if(null!=target.getAddresses()){
          addressModels.addAll(target.getAddresses());
        }
        var newAdrs=sclAddressReverseConverter.convert(source.getAddress());
        if(null!=newAdrs) {
            CustomerSubAreaMappingModel customersubarea = djpVisitService.createCustomerSubAreaMapping(newAdrs.getState(), newAdrs.getDistrict(), newAdrs.getTaluka(), target, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
            newAdrs.setOwner(target);
            newAdrs.setBillingAddress(Boolean.TRUE);
            newAdrs.setShippingAddress(Boolean.TRUE);
            newAdrs.setVisibleInAddressBook(Boolean.TRUE);
            newAdrs.setIsPrimaryAddress(Boolean.TRUE);
            modelService.save(newAdrs);
            addressModels.add(newAdrs);
            modelService.save(customersubarea);
        }
        target.setAddresses(addressModels);
        target.setEmail(source.getAddress().getEmail());
        target.setCustomerOnboardingStatus(CustomerOnboardingStatus.FORM_PENDING);
        
        if(source.getAddress()!=null)
        {
            if(source.getAddress().getContactNumber()!=null && !source.getAddress().getContactNumber().isEmpty()) {
                SclCustomerModel sclCustomerModel = sclEndCustomerDao.getRegisteredEndCustomer(source.getAddress().getContactNumber());
                if(Objects.isNull(sclCustomerModel)) {
                    target.setMobileNumber(source.getAddress().getContactNumber());
                }
                else {
                    throw new ConversionException("A user with the provided mobile number already exists. Please try with a different mobile number");
                }
            }

        }

        if(source.getLeadId()!=null) {
            LeadMasterModel leadMaster = networkService.findItemByUidParam(source.getLeadId());
            target.setLeadMaster(leadMaster);
            leadMaster.setOnboardedCustomer(target);
            modelService.saveAll(leadMaster,target);
        }
        else
            modelService.save(target);
    }
}
