package com.eydms.facades.populators;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.web.multipart.MultipartFile;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CustomerOnboardingStatus;
import com.eydms.core.enums.PotentialCustomerStage;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.DJPVisitService;
import com.eydms.facades.data.EYDMSAddressData;
import com.eydms.facades.data.EYDMSDealerData;
import com.eydms.facades.data.EYDMSRetailerData;
import com.eydms.facades.util.GenericMediaUtil;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class EYDMSDealerReversePopulator implements Populator<EYDMSDealerData, EyDmsCustomerModel>{
	
	@Resource
    private Converter<MultipartFile, MediaModel> eydmsMediaReverseConverter;
    @Resource
    private Converter<EYDMSAddressData, AddressModel> eydmsAddressReverseConverter;
    @Resource
    private KeyGenerator customCodeGenerator;
    @Resource
    private KeyGenerator applicationNumberGenerator;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private ModelService modelService;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Resource
    private DJPVisitService djpVisitService;
    @Resource
    private UserService userService;
    
    @Resource
    B2BUnitService b2bUnitService;
    
    @Resource
    TerritoryManagementDao territoryManagementDao;

	@Override
	public void populate(EYDMSDealerData source, EyDmsCustomerModel target) throws ConversionException {
		target.setUid(customCodeGenerator.generate().toString());
        Set<PrincipalGroupModel> ugSet=new HashSet<>();
        ugSet.add(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID));
        
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
        
        if( userService.getCurrentUser()!=null)
		{
			if(userService.getCurrentUser() instanceof B2BCustomerModel)
			{
				target.setOnboardingPlacedBy((B2BCustomerModel) userService.getCurrentUser());
				target.setOnboardingPartner((B2BCustomerModel) userService.getCurrentUser());
			}
		}
        
        target.setGroups(ugSet);
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setMobileNumber(source.getContactNumber());
        target.setPotentialCustomerStage(PotentialCustomerStage.VERIFICATION);
        target.setAadharNo(source.getAadharNo());
        target.setGstIN(source.getGstin());
        target.setApplicationNo(applicationNumberGenerator.generate().toString());
        target.setApplicationDate(new Date());
        if(null!=source.getGstin()) {
            target.setGstinCertificate(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getGstinCertificate().getByteStream(),source.getGstinCertificate().getFileName())));
        }
        target.setRegistrationState(source.getStateOfRegistration());
        List<AddressModel> existingAddresses = new ArrayList<>();
        if (Objects.nonNull(target.getAddresses())) {
            existingAddresses.addAll(target.getAddresses());
        }
        var regAdrs=source.getRegisteredAddress();
        djpVisitService.createCustomerSubAreaMapping(regAdrs.getState(), regAdrs.getDistrict(), regAdrs.getTaluka(), target, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
        var registeredAdr = eydmsAddressReverseConverter.convert(regAdrs);
        if (Objects.nonNull(registeredAdr)) {
            registeredAdr.setOwner(target);
            registeredAdr.setBillingAddress(Boolean.TRUE);
            registeredAdr.setVisibleInAddressBook(Boolean.TRUE);
            registeredAdr.setIsPrimaryAddress(Boolean.TRUE);
            modelService.save(registeredAdr);
            existingAddresses.add(registeredAdr);
        }

        source.getShippingAddress().forEach(adr -> {
            var shippingAddr = eydmsAddressReverseConverter.convert(adr);
            if (Objects.nonNull(shippingAddr)) {
                shippingAddr.setOwner(target);
                shippingAddr.setShippingAddress(Boolean.TRUE);
                shippingAddr.setVisibleInAddressBook(Boolean.TRUE);
                modelService.save(shippingAddr);
                existingAddresses.add(shippingAddr);
            }
        });

        target.setAddresses(existingAddresses);
        if(null!=source.getAadharDoc()) {
            target.setAadharPhoto(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getAadharDoc().getByteStream(), source.getAadharDoc().getFileName())));
        }
        target.setImage(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getImage().getByteStream(), source.getImage().getFileName())));
        target.setCustomerOnboardingStatus(CustomerOnboardingStatus.FORM_PENDING);
        
        if(source.getFormSS()!=null)
        {
        	 List<MediaModel> formImages = new ArrayList<>();
             formImages.add(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getFormSS().getByteStream(),source.getFormSS().getFileName())));
             
             target.setOnboardingFormsImages(formImages);
        }
       
        modelService.save(target);
		
	}

}
