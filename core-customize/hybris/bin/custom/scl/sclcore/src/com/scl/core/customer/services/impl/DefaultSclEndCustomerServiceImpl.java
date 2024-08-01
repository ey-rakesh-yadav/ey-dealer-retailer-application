package com.scl.core.customer.services.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclEndCustomerDao;
import com.scl.core.customer.services.SclEndCustomerService;
import com.scl.core.dao.NetworkDao;
import com.scl.core.dao.SlctCrmIntegrationDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.CounterType;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.utility.SclDateUtility;
import com.scl.core.services.SmsOtpService;
import com.scl.facades.data.SCLAddressData;
import com.scl.facades.data.SclEndCustomerData;

import com.scl.core.services.impl.SmsOtpServiceImpl;
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

public class DefaultSclEndCustomerServiceImpl extends DefaultB2BCustomerService implements SclEndCustomerService {

    @Autowired
    ModelService modelService;

    @Autowired
    SclEndCustomerDao sclEndCustomerDao;
    
    @Resource
    private Converter<SCLAddressData, AddressModel> sclAddressReverseConverter;
    
    @Autowired
	Populator<SCLAddressData, AddressModel> sclAddressReversePopulator;
    
    @Autowired
	CustomerAccountService customerAccountService;
    
    @Resource
    Converter<AddressModel, SCLAddressData> sclAddressConverter;
    
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
    
    private static final Logger LOGGER = Logger.getLogger(DefaultSclEndCustomerServiceImpl.class);
    
	@Override
	public String saveEndCustomerData(SclEndCustomerData sclEndCustomerData) {
		LOGGER.info("End Customer- save called---" + sclEndCustomerData);
		SclCustomerModel endCustomerModel = modelService.create(SclCustomerModel.class);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String currentDate = dateFormat.format(new Date());
		try {
		//Set the values from Data to Model class and save it
		endCustomerModel.setUid(customCodeGenerator.generate().toString());
		endCustomerModel.setLoginDisabled(Boolean.FALSE);
		endCustomerModel.setActive(Boolean.TRUE);
		endCustomerModel.setIsTermCondition(sclEndCustomerData.getIsTermsAndCondition());
		endCustomerModel.setEmail(sclEndCustomerData.getEmailId());
		if (sclEndCustomerData.getDoj()!=null) {
			endCustomerModel.setDateOfJoining(dateFormat.parse(sclEndCustomerData.getDoj()));
		} else {
			endCustomerModel.setDateOfJoining(dateFormat.parse(currentDate));
		}
		endCustomerModel.setMobileNumber(sclEndCustomerData.getContactNumber());
		endCustomerModel.setName(sclEndCustomerData.getName());
		
		SCLAddressData sclEndCustomerAddress = sclEndCustomerData.getAddress();
		AddressModel newAddress = modelService.create(AddressModel.class);
		
        if(sclEndCustomerAddress != null) {
            sclAddressReversePopulator.populate(sclEndCustomerAddress, newAddress);
            newAddress.setBillingAddress(true);
            newAddress.setDuplicate(true);
            newAddress.setShippingAddress(false);
            newAddress.setIsPrimaryAddress(false);
            newAddress.setVisibleInAddressBook(false);
        }
        endCustomerModel.setState(sclEndCustomerAddress.getState());
        endCustomerModel.setDistrict(sclEndCustomerAddress.getDistrict());
        endCustomerModel.setTaluka(sclEndCustomerAddress.getTaluka());
        
//        endCustomerModel.setAddresses(addressModels);
        Set<PrincipalGroupModel> groups = new HashSet<>();
        PrincipalGroupModel b2bCustomerGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("b2bcustomergroup");
		groups.add(b2bCustomerGroupModel);
		endCustomerModel.setCounterType(CounterType.SITE);
        PrincipalGroupModel principalGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("SclEndCustomerGroup");
		groups.add(principalGroupModel);
		principalGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("SclCustomerGroup");
		groups.add(principalGroupModel);
		endCustomerModel.setGroups(groups);
		
		endCustomerModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("SclEndCustomerUnit"));
		
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
	public SclEndCustomerData getRegisteredEndCustomer() {
		SclEndCustomerData sclEndCustomerRegisered = new SclEndCustomerData();
		SclCustomerModel registeredEndCustomerModel = (SclCustomerModel) userService.getCurrentUser();
		if (null != registeredEndCustomerModel) {
			sclEndCustomerRegisered = getEndCustomerListData(registeredEndCustomerModel);
		}
		return sclEndCustomerRegisered;
	}
	
	private SclEndCustomerData getEndCustomerListData(SclCustomerModel customer) {
		SclEndCustomerData sclEndCustomer = new SclEndCustomerData();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		sclEndCustomer.setName(customer.getName());
		sclEndCustomer.setDoj(dateFormat.format(customer.getDateOfJoining()));
		sclEndCustomer.setEmailId(customer.getEmail());
		
		var registeredAdrs= customer.getAddresses().stream().filter(AddressModel::getBillingAddress).findFirst().orElse(null);
        if(null!=registeredAdrs) {
            var registeredAdr = sclAddressConverter.convert(registeredAdrs);
            sclEndCustomer.setAddress(registeredAdr);
        }
		
		sclEndCustomer.setContactNumber(customer.getMobileNumber());
		sclEndCustomer.setIsTermsAndCondition(customer.getIsTermCondition());
		sclEndCustomer.setLatitude(customer.getLatitude());
		sclEndCustomer.setLongitude(customer.getLongitude());
		return sclEndCustomer;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SearchPageData<SclCustomerModel> getDealersList(SearchPageData searchPageData, String brand, String state, String district, String city, String pincode, String influencerType, String counterType){
		return (SearchPageData<SclCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody()
	    {
	        @Override
	        public SearchPageData<SclCustomerModel> execute(){
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
	public SearchPageData<SclCustomerModel> getInfluencersList(SearchPageData searchPageData, String InfluencerType, String brand, String state,String district,String city,String pincode){
		Date timestamp=SclDateUtility.getFirstDayOfFinancialYear();
		return (SearchPageData<SclCustomerModel>) sessionService.executeInLocalView(new SessionExecutionBody()
	    {
	        @Override
	        public SearchPageData<SclCustomerModel> execute(){
	            try {
	            	searchRestrictionService.disableSearchRestrictions();
	            	return sclEndCustomerDao.getAllInfluncersForStateDistrict(searchPageData, InfluencerType, brand, state, district, city,pincode);
	            }
	            finally {
	                searchRestrictionService.enableSearchRestrictions();
	            }
	        }
	    });
	}
	
	@Override
	public List<ProductModel> getProductsForBrand(String brand){
		return sclEndCustomerDao.getProductsForBrand(brand);
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
