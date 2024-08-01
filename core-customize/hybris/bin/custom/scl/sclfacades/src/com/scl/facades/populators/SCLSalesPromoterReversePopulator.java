package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.CustomerOnboardingStatus;
import com.scl.core.enums.PotentialCustomerStage;
import com.scl.core.jalo.RegionMaster;
import com.scl.core.model.*;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.SCLAddressData;
import com.scl.facades.data.SCLSalesPromoterData;
import com.scl.facades.util.GenericMediaUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

public class SCLSalesPromoterReversePopulator implements Populator<SCLSalesPromoterData, SclCustomerModel> {

    @Resource
    private Converter<MultipartFile, MediaModel> sclMediaReverseConverter;
    @Resource
    private Converter<SCLAddressData, AddressModel> sclAddressReverseConverter;
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

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Resource
    B2BUnitService b2bUnitService;

    @Override
    public void populate(SCLSalesPromoterData source, SclCustomerModel target) throws ConversionException {
        target.setUid(customCodeGenerator.generate().toString());
        Set<PrincipalGroupModel> ugSet=new HashSet<>();
        ugSet.add(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_PROMOTER_ONBOARDING_USER_GROUP_UID));

        CompanyModel b2bUnit = null;

        if(source.getBrand().equals(SclCoreConstants.SITE.SHREE_SITE))
        {
            b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
        }
        else if(source.getBrand().equals(SclCoreConstants.SITE.SCL_SITE))
        {
            b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
        }
        else if(source.getBrand().equals(SclCoreConstants.SITE.BANGUR_SITE))
        {
            b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID);
        }
        else if(source.getBrand().equals(SclCoreConstants.SITE.ROCKSTRONG_SITE))
        {
            b2bUnit = b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID);
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
            target.setGstinCertificate(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getGstinCertificate().getByteStream(),source.getGstinCertificate().getFileName())));
        }
        target.setRegistrationState(source.getStateOfRegistration());
        List<AddressModel> existingAddresses = new ArrayList<>();
        if (Objects.nonNull(target.getAddresses())) {
            existingAddresses.addAll(target.getAddresses());
        }
        var regAdrs=source.getRegisteredAddress();
        djpVisitService.createCustomerSubAreaMapping(regAdrs.getState(), regAdrs.getDistrict(), regAdrs.getTaluka(), target, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
        var registeredAdr = sclAddressReverseConverter.convert(regAdrs);
        if (Objects.nonNull(registeredAdr)) {
            registeredAdr.setOwner(target);
            registeredAdr.setBillingAddress(Boolean.TRUE);
            registeredAdr.setVisibleInAddressBook(Boolean.TRUE);
            registeredAdr.setIsPrimaryAddress(Boolean.TRUE);
            modelService.save(registeredAdr);
            existingAddresses.add(registeredAdr);
        }

        source.getShippingAddress().forEach(adr -> {
            var shippingAddr = sclAddressReverseConverter.convert(adr);
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
            target.setAadharPhoto(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getAadharDoc().getByteStream(), source.getAadharDoc().getFileName())));
        }
        target.setImage(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getImage().getByteStream(), source.getImage().getFileName())));
        target.setCustomerOnboardingStatus(CustomerOnboardingStatus.FORM_PENDING);

        SubAreaMasterModel subArea = territoryManagementService.getTerritoryByDistrictAndTaluka(regAdrs.getDistrict(),regAdrs.getTaluka());
        if(subArea!=null){
            DistrictMasterModel district = subArea.getDistrictMaster();
            target.setDistrictMaster(district);
            if(district!=null){
                RegionMasterModel region = district.getRegion();
                target.setRegionMaster(region);
                if(region!=null){
                    StateMasterModel state = region.getState();
                    target.setStateMaster(state);
                }
            }
            target.setSubAreaMaster(subArea);
        }

        modelService.save(target);
    }
}
