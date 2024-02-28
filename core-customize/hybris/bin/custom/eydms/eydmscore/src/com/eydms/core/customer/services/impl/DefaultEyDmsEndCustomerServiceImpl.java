package com.eydms.core.customer.services.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsEndCustomerDao;
import com.eydms.core.customer.services.EyDmsEndCustomerService;
import com.eydms.core.dao.NetworkDao;
import com.eydms.core.dao.SlctCrmIntegrationDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.core.services.SmsOtpService;
import com.eydms.facades.data.EYDMSAddressData;
import com.eydms.facades.data.EyDmsEndCustomerData;

import com.eydms.core.services.impl.SmsOtpServiceImpl;
import de.hybris.platform.b2b.services.impl.DefaultB2BUnitService;
import de.hybris.platform.b2b.services.impl.DefaultB2BCustomerService;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.b2b.services.impl.DefaultB2BUnitService;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;

public class DefaultEyDmsEndCustomerServiceImpl extends DefaultB2BCustomerService implements EyDmsEndCustomerService {

    @Autowired
    ModelService modelService;

    @Autowired
    EyDmsEndCustomerDao eydmsEndCustomerDao;
    
    @Resource
    private Converter<EYDMSAddressData, AddressModel> eydmsAddressReverseConverter;
    
    @Autowired
	Populator<EYDMSAddressData, AddressModel> eydmsAddressReversePopulator;
    
    @Autowired
	CustomerAccountService customerAccountService;
    
    @Resource
    Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
    
    @Resource
    private KeyGenerator customCodeGenerator;
    
    @Resource
    private KeyGenerator applicationNumberGenerator;
    
    @Resource
    private BaseSiteService baseSiteService;
    
    @Resource
    private NetworkDao networkDao;
    
    @Autowired
	TerritoryManagementDao territoryManagementDao;
    
    @Autowired
	SlctCrmIntegrationDao slctCrmIntegrationDao;
    
    @Resource
	DefaultB2BUnitService defaultB2BUnitService;
    
    @Autowired
    SmsOtpService smsOtpService;
    
    private Converter<B2BCustomerModel, CustomerData> customerBasicConverter;
    
    @Autowired 
	PasswordEncoder passwordEncoder;
    
    @Resource
    FlexibleSearchService flexibleSearchService;
    
    @Autowired
    SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;
    
    @Autowired
    UserService userService;
    
    private static final Logger LOGGER = Logger.getLogger(DefaultEyDmsEndCustomerServiceImpl.class);
    
	@Override
	public String saveEndCustomerData(EyDmsEndCustomerData eydmsEndCustomerData) {
		LOGGER.info("End Customer- save called---" + eydmsEndCustomerData);
		EyDmsCustomerModel endCustomerModel = modelService.create(EyDmsCustomerModel.class);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String currentDate = dateFormat.format(new Date());
		try {
		//Set the values from Data to Model class and save it
		endCustomerModel.setUid(customCodeGenerator.generate().toString());
		endCustomerModel.setLoginDisabled(Boolean.FALSE);
		endCustomerModel.setActive(Boolean.TRUE);
		endCustomerModel.setIsTermCondition(eydmsEndCustomerData.getIsTermsAndCondition());
		endCustomerModel.setEmail(eydmsEndCustomerData.getEmailId());
		if (eydmsEndCustomerData.getDoj()!=null) {
			endCustomerModel.setDateOfJoining(dateFormat.parse(eydmsEndCustomerData.getDoj()));
		} else {
			endCustomerModel.setDateOfJoining(dateFormat.parse(currentDate));
		}
		endCustomerModel.setMobileNumber(eydmsEndCustomerData.getContactNumber());
		endCustomerModel.setName(eydmsEndCustomerData.getName());
		
		EYDMSAddressData eydmsEndCustomerAddress = eydmsEndCustomerData.getAddress();
		AddressModel newAddress = modelService.create(AddressModel.class);
		
        if(eydmsEndCustomerAddress != null) {
            eydmsAddressReversePopulator.populate(eydmsEndCustomerAddress, newAddress);
            newAddress.setBillingAddress(true);
            newAddress.setDuplicate(true);
            newAddress.setShippingAddress(false);
            newAddress.setIsPrimaryAddress(false);
            newAddress.setVisibleInAddressBook(false);
        }
        endCustomerModel.setState(eydmsEndCustomerAddress.getState());
        endCustomerModel.setDistrict(eydmsEndCustomerAddress.getDistrict());
        endCustomerModel.setTaluka(eydmsEndCustomerAddress.getTaluka());
        
//        endCustomerModel.setAddresses(addressModels);
        Set<PrincipalGroupModel> groups = new HashSet<>();
        PrincipalGroupModel b2bCustomerGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("b2bcustomergroup");
		groups.add(b2bCustomerGroupModel);
		endCustomerModel.setCounterType(CounterType.SITE);
        PrincipalGroupModel principalGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("EyDmsEndCustomerGroup");
		groups.add(principalGroupModel);
		principalGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("EyDmsCustomerGroup");
		groups.add(principalGroupModel);
		endCustomerModel.setGroups(groups);
		
		endCustomerModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsEndCustomerUnit"));
		
        modelService.save(endCustomerModel);
        customerAccountService.saveAddressEntry(endCustomerModel, newAddress);
		}
		catch(Exception exp){
			exp.printStackTrace();
			LOGGER.error("End Customer -->> Error Message:" + exp.getMessage());
		}
		return endCustomerModel.getUid();
	}
	
		
	@Override
	public EyDmsEndCustomerData getRegisteredEndCustomer() {
		EyDmsEndCustomerData eydmsEndCustomerRegisered = new EyDmsEndCustomerData();
		EyDmsCustomerModel registeredEndCustomerModel = (EyDmsCustomerModel) userService.getCurrentUser();
		if (null != registeredEndCustomerModel) {
			eydmsEndCustomerRegisered = getEndCustomerListData(registeredEndCustomerModel);
		}
		return eydmsEndCustomerRegisered;
	}
	
	private EyDmsEndCustomerData getEndCustomerListData(EyDmsCustomerModel customer) {
		EyDmsEndCustomerData eydmsEndCustomer = new EyDmsEndCustomerData();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		eydmsEndCustomer.setName(customer.getName());
		eydmsEndCustomer.setDoj(dateFormat.format(customer.getDateOfJoining()));
		eydmsEndCustomer.setEmailId(customer.getEmail());
		
		var registeredAdrs= customer.getAddresses().stream().filter(AddressModel::getBillingAddress).findFirst().orElse(null);
        if(null!=registeredAdrs) {
            var registeredAdr = eydmsAddressConverter.convert(registeredAdrs);
            eydmsEndCustomer.setAddress(registeredAdr);
        }
		
		eydmsEndCustomer.setContactNumber(customer.getMobileNumber());
		eydmsEndCustomer.setIsTermsAndCondition(customer.getIsTermCondition());
		eydmsEndCustomer.setLatitude(customer.getLatitude());
		eydmsEndCustomer.setLongitude(customer.getLongitude());
		return eydmsEndCustomer;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SearchPageData<EyDmsCustomerModel> getDealersList(SearchPageData searchPageData, String brand, String state, String district, String city, String pincode, String influencerType, String counterType){
		return (SearchPageData<EyDmsCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody()
	    {
	        @Override
	        public SearchPageData<EyDmsCustomerModel> execute(){
	            try {
	            	searchRestrictionService.disableSearchRestrictions();
	            	return territoryManagementDao.getAllCustomerForStateDistrict(searchPageData, brand, state, district, city,pincode, influencerType, counterType);
	            }
	            finally {
	                searchRestrictionService.enableSearchRestrictions();
	            }
	        }
	    });
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SearchPageData<EyDmsCustomerModel> getInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode){
		Date timestamp=EyDmsDateUtility.getFirstDayOfFinancialYear();
		return (SearchPageData<EyDmsCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody()
	    {
	        @Override
	        public SearchPageData<EyDmsCustomerModel> execute(){
	            try {
	            	searchRestrictionService.disableSearchRestrictions();
	            	return eydmsEndCustomerDao.getAllInfluncersForStateDistrict(searchPageData, InfluencerType, brand, state, district, city,pincode);
	            }
	            finally {
	                searchRestrictionService.enableSearchRestrictions();
	            }
	        }
	    });
	}
	
	@Override
	public List<ProductModel> getProductsForBrand(String brand){
		return eydmsEndCustomerDao.getProductsForBrand(brand);
	}
	
	@Override
	public CustomerData getCustomerByContactNo(String mobileNumber, String smsLoginOtp) {
		CustomerData customerData = new CustomerData();
		B2BCustomerModel b2bCustomer = new B2BCustomerModel();
		Boolean isSuccess = false;
		b2bCustomer.setMobileNumber(mobileNumber);

        B2BCustomerModel customerModel = new B2BCustomerModel();

        try {
            customerModel = flexibleSearchService.getModelByExample(b2bCustomer);
        } catch (ModelNotFoundException e) {
            LOGGER.warn("Mobile number not found");
        } catch (Exception exp) {
        	LOGGER.error(exp);
        }
		
		if(mobileNumber != null)
		{
			SmsOtpServiceImpl smsOtpServiceImpl = new SmsOtpServiceImpl();
			String serverOtp = smsOtpServiceImpl.getOtp(mobileNumber);
			 
			 if(passwordEncoder.matches(smsLoginOtp, serverOtp))
			 {
				 smsOtpServiceImpl.clearOTP(mobileNumber);
				 isSuccess = true;
			 }
			 
		}
		if (isSuccess) {
			return getCustomerBasicConverter().convert(customerModel);
		} else {
			return customerData;
		}
	}

	public Converter<B2BCustomerModel, CustomerData> getCustomerBasicConverter() {
		return customerBasicConverter;
	}

	public void setCustomerBasicConverter(Converter<B2BCustomerModel, CustomerData> customerBasicConverter) {
		this.customerBasicConverter = customerBasicConverter;
	}
}
