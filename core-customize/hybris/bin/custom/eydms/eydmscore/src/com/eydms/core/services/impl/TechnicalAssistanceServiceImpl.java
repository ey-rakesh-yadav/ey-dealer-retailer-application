package com.eydms.core.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;

import java.util.Map;
import java.util.stream.Collectors;

import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.facades.data.EndCustomerComplaintData;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.NetworkAssitanceData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.services.TechnicalAssistanceService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.TechnicalAssistanceData;
import com.eydms.core.dao.TechnicalAssistanceDao;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

public class TechnicalAssistanceServiceImpl implements TechnicalAssistanceService{

	@Autowired
	ModelService modelService;
	
	@Autowired
	EnumerationService enumerationService;
	
	@Autowired
	UserService userService;

	@Autowired
	EyDmsNotificationService eydmsNotificationService;
	
	@Autowired
    KeyGenerator customCodeGenerator;
	
	@Autowired
	TechnicalAssistanceDao technicalAssistanceDao;
	
	@Autowired
	TerritoryManagementService territoryService;
	
	@Autowired
	I18NService i18NService;

	@Autowired
	private BaseSiteService baseSiteService;

	@Autowired
	private KeyGenerator requestNoGenerator;

	private static final Logger LOG = Logger.getLogger(TechnicalAssistanceServiceImpl.class);
	 
	@Override
	public Boolean saveForm(String userId, TechnicalAssistanceData data) {
		
		TechnicalAssistanceModel model = modelService.create(TechnicalAssistanceModel.class);
		model.setName(data.getName());
		model.setConstructionStage(TACurrentConstructionStage.valueOf(data.getConstructionStage()));
		model.setLine1(data.getLine1());
		model.setLine2(data.getLine2());
		model.setState(data.getState());
		model.setDistrict(data.getDistrict());
		model.setTaluka(data.getTaluka());
		model.setCity(data.getCity());
		model.setPostalCode(data.getPostalCode());
		model.setCellPhone(data.getCellPhone());

		Date supervisionDate=null;
		try {
			supervisionDate = new SimpleDateFormat("dd/MM/yyyy").parse(data.getDateOfSupervisionRequired());
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		
		model.setDateOfSupervisionRequired(supervisionDate);
		model.setDurationOfSupervisionRequired(data.getDurationOfSupervisionRequired());
		model.setExpertiseRequired(TAExpertise.valueOf(data.getExpertiseRequired()));
		model.setRequestNo(customCodeGenerator.generate().toString());
		model.setRaisedBy((EyDmsUserModel) userService.getUserForUID(userId));
		model.setRequestDate(Calendar.getInstance().getTime());
		model.setRequestStatus(TAServiceRequestStatus.REQUEST_RAISED);
		model.setTsoStatus(TATSOStatus.PENDING);
		model.setBrand(baseSiteService.getCurrentBaseSite());
		if(data.getConstructionAdvisory()!=null) {
			//model.setConstructionAdvisory(ConstructionAdvisory.valueOf(data.getConstructionAdvisory()));
			model.setConstructionAdvisorys(data.getConstructionAdvisory());
		}

		SubAreaMasterModel subArea = territoryService.getTerritoryByDistrictAndTaluka(data.getDistrict(),data.getTaluka());
		if(subArea!=null){
			List<EyDmsUserModel> tsoList =  territoryService.getTSOforSubArea(subArea);
        	if(tsoList!=null && !tsoList.isEmpty()) {
        		model.setTsoAssigned(tsoList.get(0));
        		model.setTsoAssignedDate(new Date());
        	}
			model.setSubArea(subArea);
			DistrictMasterModel districtMaster = subArea.getDistrictMaster();
			if(districtMaster!=null){
				model.setDistrictMaster(districtMaster);
				RegionMasterModel regionMaster = districtMaster.getRegion();
				if(regionMaster!=null){
					model.setRegionMaster(regionMaster);
					StateMasterModel stateMaster = regionMaster.getState();
					if(stateMaster!=null){
						model.setStateMaster(stateMaster);
					}
				}
			}
		}
		modelService.save(model);
		try {
			StringBuilder builder = new StringBuilder();
			NotificationCategory category = NotificationCategory.TA_REQUEST_RAISED;
			builder.append("You have a Technical Assistance Request  "+ model.getRequestNo() + " raised by " + model.getRaisedBy().getName() +" " + model.getRaisedBy().getUid() + ". ");
			builder.append("Kindly approve/reject the request ");
			String body = builder.toString();
			StringBuilder builder1 = new StringBuilder();
			builder1.append("New Technical Assistance Request has been raised ");
			String subject = builder1.toString();
			Map<String,String> suggestion = new HashMap<>();
			suggestion.put("TaRequestId",model.getRequestNo());
			eydmsNotificationService.submitDealerNotification((B2BCustomerModel) model.getTsoAssigned(),body,subject,category,suggestion);
		}
		catch(Exception e) {
			LOG.error("Error while sending TA Request notification");
		}
		
		return Boolean.TRUE;
	}

	@Override
	public List<String> getExpertiseListForCurrentConstructionStage(String constructionStage) {
		
		TACurrentConstructionStage constructionStageEnum = enumerationService.getEnumerationValue(TACurrentConstructionStage.class, constructionStage);
		
		List<TAExpertise> enumList = technicalAssistanceDao.getExpertiseListForCurrentConstructionStage(constructionStageEnum);
		
		List<String> expertises = new ArrayList<>();
		
		for(TAExpertise expertise : enumList)
		{
			String expertiseName = null != enumerationService.getEnumerationName(expertise, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(expertise, i18NService.getCurrentLocale()) : expertise.getCode();
			expertises.add(expertiseName);
		}
		
		return expertises;
	}
	
	@Override
	public SearchPageData<NetworkAssistanceModel> getNetworkAssitances(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			,String requestNo, String filters,List<String> status){
		return technicalAssistanceDao.getNetworkAssitances(searchPageData, startDate, endDate, partnerCode, requestNo, filters, status);
	}

	@Override
	public SearchPageData<EndCustomerComplaintModel> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			,String requestNo, String filters,List<String> requestStatuses,Boolean isSiteVisitRequired, Boolean plannedVisitForToday,String subAreaMasterPk){
		return technicalAssistanceDao.getEndCustomerComplaints(searchPageData, startDate, endDate, partnerCode, requestNo, filters,requestStatuses, isSiteVisitRequired,plannedVisitForToday, subAreaMasterPk);
	}

	@Override
	public EndCustomerComplaintModel getEndCustomerComplaintForRequestNo(String requestId) {
		return technicalAssistanceDao.getEndCustomerComplaintForRequestNo(requestId);
	}

	@Override
	public SearchPageData<TechnicalAssistanceModel> getTechnicalAssistances(SearchPageData searchPageData, String startDate, String endDate, String name
			,String requestNo, String filters,List<String> status,List<String> constructionAdvisory) {

		return technicalAssistanceDao.getTechnicalAssistances(searchPageData, startDate, endDate, name, requestNo, filters,status, constructionAdvisory);

	}

	@Override
	public TechnicalAssistanceModel getTechnicalAssistanceRequestDetails(String requestNo) {
		return technicalAssistanceDao.getTechnicalAssistanceForRequestNo(requestNo);
	}
  
  @Override
	public List<EyDmsCustomerModel> getAllRetailersForMaterial(String userId) {
		List<EyDmsCustomerModel> retailersList = new ArrayList<>();
		retailersList.addAll(territoryService.getAllCustomerForSO().stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList()));
		return retailersList;
	}

	@Override
	public List<EyDmsCustomerModel> getAllDealersForMaterial(String userId) {
		List<EyDmsCustomerModel> dealersList = new ArrayList<>();
		dealersList.addAll(territoryService.getAllCustomerForSO().stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList()));
		return dealersList;

	}

	@Override
	public Boolean submitCustomerComplaint(CustomerComplaintModel customerComplaintModel) {
		modelService.save(customerComplaintModel);
		return Boolean.TRUE;
	}

	@Override
	public InvoiceMasterModel getInvoiceMasterByInvoiceNo(String invoiceNo) {
		return technicalAssistanceDao.getInvoiceMasterByInvoiceNo(invoiceNo);
	}

	@Override
	public CustomerComplaintModel getCustomerAssistanceRequestDetails(String requestNo) {
		return technicalAssistanceDao.getCustomerAssistanceForRequestNo(requestNo);
	}

	@Override
	public List<CustomerComplaintModel> getCustomerAssistanceRequestList(String userId, Integer year, Integer month, String requestStatus, String key) {
		EyDmsUserModel user = (EyDmsUserModel) userService.getUserForUID(userId);

		Calendar cal = Calendar.getInstance();

		if(!Objects.isNull(year) && !Objects.isNull(month))
		{
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month-1);
		}

		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date startDate = cal.getTime();

		cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.MONTH));
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);

		Date endDate = cal.getTime();

		TAServiceRequestStatus status = null;

		if(requestStatus!=null)
		{
			status =  enumerationService.getEnumerationValue(TAServiceRequestStatus.class, requestStatus);
		}

		List<CustomerComplaintModel> list = new ArrayList<>();

		if(!Objects.isNull(year) && !Objects.isNull(month))
		{
			list = technicalAssistanceDao.getCustomerAssistanceRequestList(user, startDate, endDate, status, key);
		}
		else
		{
			list = technicalAssistanceDao.getCustomerAssistanceRequestList(user, null, null, status, key);
		}

		return list;
	}

  @Override
	public Boolean saveSOComment(String requestNo, String comment) {
		NetworkAssistanceModel model = technicalAssistanceDao.getNetworkAssistanceForRequestNo(requestNo);
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		if(currentUser.getUserType().getCode().equals("SO")){
//		if(currentUser.getGroups().contains(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID) ){
			model.setCommentBySo(comment);
			model.setCommentSoUser(currentUser);
			model.setCommentSoDate(new Date());
		}
		else if(currentUser.getUserType().getCode().equals("TSM")){
			model.setCommentByTSM(comment);
			model.setCommentTSMUser(currentUser);
			model.setCommentTSMDate(new Date());
		}

		modelService.save(model);
		return Boolean.TRUE;
	}

	@Override
	public Boolean markRequestAsCompleted(String requestNo,String resolvedComment) {
		NetworkAssistanceModel model = technicalAssistanceDao.getNetworkAssistanceForRequestNo(requestNo);
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		model.setResolvedBy(currentUser);
		model.setResolveDate(new Date());
		model.setResolvedComment(resolvedComment);
		model.setStatus(AssitanceStatus.CLOSED);
		modelService.save(model);
		return Boolean.TRUE;
	}
  
	@Override
	public HybrisEnumValue getEnumerationValueForLocalizedName(String enumClass, String localizedName)
	{
		List<HybrisEnumValue> list = enumerationService.getEnumerationValues(enumClass);
		
		Map<String,HybrisEnumValue> map = new HashMap<>();
		
		for(HybrisEnumValue enumValue : list)
		{
			String name = ( null != enumerationService.getEnumerationName(enumValue, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(enumValue, i18NService.getCurrentLocale()) : enumValue.getCode() );
			map.put(name,enumValue);
		}
		
		return map.get(localizedName);
	}

	@Override
	public Boolean newRequestForm(NetworkAssitanceData networkAssitanceData) {
		NetworkAssistanceModel networkAssistanceModel = modelService.create(NetworkAssistanceModel.class);

		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
		networkAssistanceModel.setStatus(AssitanceStatus.OPEN);
		networkAssistanceModel.setTitle(networkAssitanceData.getTitle());
		networkAssistanceModel.setDescription(networkAssitanceData.getDescription());
		networkAssistanceModel.setAssitanceTopic(AssitanceTopic.valueOf(networkAssitanceData.getAssitanceTopic()));
		networkAssistanceModel.setBrand(brand);
		networkAssistanceModel.setRequestDate(new Date());
		networkAssistanceModel.setRaisedBy(currentUser);
		networkAssistanceModel.setRequestNo(String.valueOf(requestNoGenerator.generate()));


		networkAssistanceModel.setLine1(networkAssitanceData.getLine1());
		networkAssistanceModel.setLine2(networkAssitanceData.getLine2());
		networkAssistanceModel.setState(networkAssitanceData.getState());
		networkAssistanceModel.setDistrict(networkAssitanceData.getDistrict());
		networkAssistanceModel.setTaluka(networkAssitanceData.getTaluka());
		networkAssistanceModel.setCity(networkAssitanceData.getCity());

//		List<SubAreaMasterModel> subareas = territoryService.getTerritoriesForCustomer((EyDmsCustomerModel) currentUser);
		SubAreaMasterModel subArea = territoryService.getTerritoryByDistrictAndTaluka(networkAssitanceData.getDistrict(),networkAssitanceData.getTaluka());
		if(subArea!=null){
			networkAssistanceModel.setSubArea(subArea);
			DistrictMasterModel districtMaster = subArea.getDistrictMaster();
			if(districtMaster!=null){
				networkAssistanceModel.setDistrictMaster(districtMaster);
				RegionMasterModel regionMaster = districtMaster.getRegion();
				if(regionMaster!=null){
					networkAssistanceModel.setRegionMaster(regionMaster);
					StateMasterModel stateMaster = regionMaster.getState();
					if(stateMaster!=null){
						networkAssistanceModel.setStateMaster(stateMaster);
					}
				}
			}
		}

		/*CustDepotMasterModel custDepotForCustomer = territoryService.getCustDepotForCustomer((EyDmsCustomerModel) currentUser);
		networkAssistanceModel.setCustDepo(custDepotForCustomer);*/

		modelService.save(networkAssistanceModel);
		return Boolean.TRUE;
	}

	@Override
	public Integer countOfAssignedTicketNumbers() {
		return technicalAssistanceDao.countOfAssignedTicketNumbers();
	}

    @Override
    public DetailsFromSiteModel getDetailesFromSiteById(String siteId) {
		return technicalAssistanceDao.getDetailesFromSiteById(siteId);
    }

    @Override
    public EndCustomerComplaintData submitRequestForAnotherTSODetails(String complaintId, String commentForAnotherTSORequest) {
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		EndCustomerComplaintData data=new EndCustomerComplaintData();
		EndCustomerComplaintModel model = getEndCustomerComplaintForRequestNo(complaintId);
		if(model!=null) {
			model.setRequestForAnotherTSOComment(commentForAnotherTSORequest);
			model.setRequestForAnotherTSORequestRaisedDate(new Date());
			model.setRequestForAnotherTSORequestRaisedBy(currentUser);
			modelService.save(model);
			data.setRequestId(model.getRequestId());
		}
		return data;
    }
    
    @Override
	public TechnicalAssistanceModel rejectTAByTSO(String requestNo, String rejectedReason) {
    	TechnicalAssistanceModel model  =  technicalAssistanceDao.getTechnicalAssistanceForRequestNo(requestNo);
    	model.setRejectedDate(new Date());
    	model.setRejectedBy((B2BCustomerModel) userService.getCurrentUser());
    	model.setRejectedReason(rejectedReason);
    	model.setTsoStatus(TATSOStatus.REJECTED);
    	model.setRequestStatus(TAServiceRequestStatus.REQUEST_REJECTED);
    	modelService.save(model);
    	return model;
	}
    
    @Override
    public TechnicalAssistanceModel acceptTAByTSO(String requestNo, String siteId) {
    	TechnicalAssistanceModel model  =  technicalAssistanceDao.getTechnicalAssistanceForRequestNo(requestNo);
    	model.setAcceptedDate(new Date());
    	model.setAcceptedBy((B2BCustomerModel) userService.getCurrentUser());
    	model.setRequestStatus(TAServiceRequestStatus.SERVICE_ONGOING);
    	if(siteId!=null) {
    		model.setSite((EyDmsSiteMasterModel) userService.getUserForUID(siteId));
    	}
    	modelService.save(model);
    	return model;
    }
    
    @Override
    public TechnicalAssistanceModel closeTAByTSO(String requestNo, String closeComment) {
    	TechnicalAssistanceModel model  =  technicalAssistanceDao.getTechnicalAssistanceForRequestNo(requestNo);
    	if(model.getLastVisitedDate()!=null) {
    		model.setClosedDate(new Date());
    		model.setClosedBy((B2BCustomerModel) userService.getCurrentUser());
    		model.setClosedByComment(closeComment);
    		model.setTsoStatus(TATSOStatus.COMPLETED);
    		model.setRequestStatus(TAServiceRequestStatus.SERVICE_COMPLETED);
    		modelService.save(model);
    		return model;
    	}
    	else {
    		throw new UnknownIdentifierException("TA cannot be closed as the site has not been visited at least once.");
    	}
    }


}

