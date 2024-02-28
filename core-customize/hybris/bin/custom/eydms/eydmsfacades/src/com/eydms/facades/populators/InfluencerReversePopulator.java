package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsEndCustomerDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.CustomerOnboardingStatus;
import com.eydms.core.enums.InfluencerType;
import com.eydms.core.enums.NotificationCategory;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.services.DJPVisitService;
import com.eydms.core.services.NetworkService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.InfluencerData;
import com.eydms.facades.data.EYDMSAddressData;

import com.eydms.facades.populators.complaints.EndCustomerComplaintReversePopulator;
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

public class InfluencerReversePopulator implements Populator<InfluencerData, EyDmsCustomerModel> {
    @Resource
    private Converter<EYDMSAddressData, AddressModel> eydmsAddressReverseConverter;
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
    EyDmsNotificationService eydmsNotificationService;
    @Resource
    private KeyGenerator applicationNumberGenerator;
    
    @Resource
    B2BUnitService b2bUnitService;

    @Autowired
    NetworkService networkService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    EyDmsEndCustomerDao eydmsEndCustomerDao;

    private static final Logger LOG = Logger.getLogger(InfluencerReversePopulator.class);
    
    @Override
    public void populate(InfluencerData source, EyDmsCustomerModel target) throws ConversionException {
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
        
        if(source.getBrand().equals(EyDmsCoreConstants.SITE.SHREE_SITE))
        {
        	 b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID);
        }
        else if(source.getBrand().equals(EyDmsCoreConstants.SITE.BANGUR_SITE))
        {
        	b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID);
        }
        else if(source.getBrand().equals(EyDmsCoreConstants.SITE.ROCKSTRONG_SITE))
        {
        	b2bUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID);
        }
        
        if(null != b2bUnit){
			target.setDefaultB2BUnit((B2BUnitModel) b2bUnit);
		}
        
        Set<PrincipalGroupModel> ugSet=new HashSet<>();
        ugSet.add( userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID));
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
        var newAdrs=eydmsAddressReverseConverter.convert(source.getAddress());
        if(null!=newAdrs) {
            CustomerSubAreaMappingModel customersubarea = djpVisitService.createCustomerSubAreaMapping(newAdrs.getState(), newAdrs.getDistrict(), newAdrs.getTaluka(), target, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
            newAdrs.setOwner(target);
            newAdrs.setBillingAddress(Boolean.TRUE);
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
                EyDmsCustomerModel eydmsCustomerModel = eydmsEndCustomerDao.getRegisteredEndCustomer(source.getAddress().getContactNumber());
                if(Objects.isNull(eydmsCustomerModel)) {
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
