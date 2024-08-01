package com.scl.facades.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.SiteManagementDao;
import com.scl.core.enums.CustomerComplaintTSOStatus;
import com.scl.core.enums.DetailsOfTaskToBeDone;
import com.scl.core.enums.ModeOfMeeting;
import com.scl.core.enums.SiteRootCause;
import com.scl.core.enums.TAServiceRequestStatus;
import com.scl.core.enums.TATSOStatus;
import com.scl.core.enums.TESuppportRequired;
import com.scl.core.enums.TypeOfCement;
import com.scl.core.jalo.ComplaintRootCauseIdentified;
import com.scl.core.jalo.ComplaintTEMeeting;
import com.scl.core.model.*;
import com.scl.core.services.impl.BrandingServiceImpl;
import com.scl.facades.data.*;
import com.scl.facades.order.data.TrackingData;
import com.scl.facades.prosdealer.data.DealerListData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.services.TechnicalAssistanceService;
import com.scl.facades.TechnicalAssistanceFacade;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.I18NService;

import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;

import static org.apache.fop.fonts.type1.AdobeStandardEncoding.d;
import static org.apache.fop.fonts.type1.AdobeStandardEncoding.m;

public class TechnicalAssistanceFacadeImpl implements TechnicalAssistanceFacade{
	private static final Logger LOG = Logger.getLogger(TechnicalAssistanceFacadeImpl.class);
	private static final String MEDIA_BASE_URL = "media.host.base.url";

	@Autowired
	TechnicalAssistanceService technicalAssistanceService;
	
	@Autowired
	Converter<TechnicalAssistanceModel,TechnicalAssistanceData> technicalAssistanceConverter;

	@Autowired
	Converter<ComplaintRootCauseIdentifiedModel,ComplaintRootCauseIndentifiedData> complaintRootCauseIdentifiedConverter;

	@Autowired
	Converter<ScheduleNextVisitModel,ScheduleNextVisitData> scheduleNextVisitDataConverter;

	@Autowired
	Converter<ComplaintSiteVisitNotRequiredModel,ComplaintSiteVisitNotRequiredData> complaintSiteVisitNotRequiredConverter;

	@Autowired
	Converter<DetailsFromSiteModel,DetailsFromSiteData> detailsFromSiteConverter;

	@Autowired
	Converter<ComplaintTestPerformedModel,ComplaintTestPerformedData> complaintTestperformedConverter;
	@Autowired
	EnumerationService enumerationService;
	
	@Autowired
	I18NService i18NService;
	
	@Autowired
	Populator<AddressModel, SCLAddressData> sclAddressPopulator;

	@Autowired
	ModelService modelService;

	@Autowired
	SiteManagementDao siteManagementDao;
	@Autowired
	Converter<CustomerComplaintModel, CustomerComplaintData> customerAssistanceConverter;

	private Converter<SclCustomerModel, CustomerData> dealerBasicConverter;
	private Converter<CustomerComplaintData, CustomerComplaintModel> customerComplaintReverseConverter;
	@Autowired
	Converter<EndCustomerComplaintData, EndCustomerComplaintModel> endCustomerComplaintReverseConverter;
	private Converter<InvoiceMasterModel, InvoiceMasterData> invoiceMasterDataConverter;

	@Resource
	private Converter<ComplaintDispatchDetailsModel,ComplaintDispatchDetailsData> sclComplaintDispatchDetailsConverter;
	private SclCustomerService sclCustomerService;

	@Autowired
	KeyGenerator complaintmeetIdGenerator;
	@Autowired
	KeyGenerator nextVisitIdGenerator;
	@Autowired
	KeyGenerator detailsFromSiteIdGenerator;
	@Autowired
	KeyGenerator siteVisitNotRequiredIdGenerator;

	@Autowired
	KeyGenerator complaintDispatchedDetailsGenerator;

	@Autowired
	KeyGenerator scheduleNextVisitGenerator;

	@Autowired
	KeyGenerator complaintRootCauseIdentifiedGenerator;

	@Autowired
	KeyGenerator complaintTestPerformedGenerator;

	@Autowired
	MediaService mediaService;
	@Autowired
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	UserService userService;

	@Autowired
	ConfigurationService configurationService;

	@Override
	public Boolean saveForm(String userId, TechnicalAssistanceData data) {
		return technicalAssistanceService.saveForm(userId, data);
	}

	@Override
	public List<String> getExpertiseListForCurrentConstructionStage(String constructionStage) {
		return technicalAssistanceService.getExpertiseListForCurrentConstructionStage(constructionStage);
	}
	
	@Override
	public SearchPageData<NetworkAssitanceData> getNetworkAssitances(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			,String requestNo, String filters,List<String> status){
		List<NetworkAssitanceData> dataList = new ArrayList<NetworkAssitanceData>();

		SearchPageData<NetworkAssistanceModel> searchResult = technicalAssistanceService.getNetworkAssitances(searchPageData, startDate, endDate, partnerCode, requestNo, filters,status);
		if(searchResult!=null && searchResult.getResults()!=null) {
			List<NetworkAssistanceModel> list = searchResult.getResults();
			list.forEach(entry -> {
				NetworkAssitanceData networkAssitance = new NetworkAssitanceData();
				networkAssitance.setTitle(entry.getTitle());
				networkAssitance.setDescription(entry.getDescription());
				networkAssitance.setCommentBySo(entry.getCommentBySo());
				networkAssitance.setCommentByTSM(entry.getCommentByTSM());
				networkAssitance.setCounterType(entry.getCounterType());
				if(entry.getAssitanceTopic()!=null) {
					networkAssitance.setAssitanceTopic(entry.getAssitanceTopic().getCode());
				}
				if(entry.getRaisedBy()!=null) {
					networkAssitance.setRaisedBy(entry.getRaisedBy().getName());
					networkAssitance.setContactNo(entry.getRaisedBy().getMobileNumber());
				}
				networkAssitance.setRequestNo(entry.getRequestNo());
				if(entry.getStatus()!=null) {
					networkAssitance.setStatus(entry.getStatus().getCode());
				}
				if(entry.getRequestDate()!=null) {
					DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
					networkAssitance.setRequestDate(dateFormat.format(entry.getRequestDate()));
				}
				if(entry.getResolveDate()!=null) {
					DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
					networkAssitance.setResolveDate(dateFormat.format(entry.getResolveDate()));
				}
				networkAssitance.setResolvedComment(entry.getResolvedComment());
				if(entry.getResolvedBy()!=null) {
					networkAssitance.setResolvedBy(entry.getResolvedBy().getName());
				}
				dataList.add(networkAssitance);
			});
		}		
		final SearchPageData<NetworkAssitanceData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(dataList);
		return result;
	}

	@Override
	public SearchPageData<EndCustomerComplaintData> getEndCustomerComplaints(SearchPageData searchPageData, String startDate, String endDate, String partnerCode
			,String requestNo, String filters,List<String> requestStatuses,Boolean isSiteVisitRequired,Boolean plannedVisitForToday, String subAreaMasterPk,List<String> talukas){
		List<EndCustomerComplaintData> dataList = new ArrayList<>();

		SearchPageData<EndCustomerComplaintModel> searchResult = technicalAssistanceService.getEndCustomerComplaints(searchPageData, startDate, endDate, partnerCode, requestNo, filters,requestStatuses,isSiteVisitRequired,plannedVisitForToday, subAreaMasterPk,talukas);
		if(searchResult!=null && searchResult.getResults()!=null) {
			List<EndCustomerComplaintModel> list = searchResult.getResults();
			list.forEach(entry -> {
				EndCustomerComplaintData endCustomerComplaintData = new EndCustomerComplaintData();
				endCustomerComplaintData.setName(entry.getCustomerName());
				endCustomerComplaintData.setPhoneNumber(entry.getPhoneNumber());
				endCustomerComplaintData.setCustomerNo(entry.getCustomerNo());
				endCustomerComplaintData.setRequestId(entry.getRequestId());
				//endCustomerComplaintData.setRequestRaisedDate(entry.getRequestRaisedDate().toString());
				if(entry.getNatureOfComplaint()!=null) {
					endCustomerComplaintData.setNatureOfComplaint(String.valueOf(entry.getNatureOfComplaint()));
				}
				if(entry.getSite()!=null) {
					endCustomerComplaintData.setSiteId(entry.getSite().getUid());
					endCustomerComplaintData.setSiteName(entry.getSite().getName());
				}
				if(entry.getRaisedBy()!=null) {
					endCustomerComplaintData.setRaisedBy(entry.getRaisedBy().getName());
				}
				if (entry.getTsoStatus()!=null && userService.getCurrentUser().getGroups()!=null && userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
					endCustomerComplaintData.setStatus(entry.getTsoStatus().getCode());
				}
				else if(entry.getStatus()!=null) {
					endCustomerComplaintData.setStatus(entry.getStatus().getCode());
				}
				if(entry.getRequestRaisedDate()!=null) {
					DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
					endCustomerComplaintData.setRequestRaisedDate(dateFormat.format(entry.getRequestRaisedDate()));
					DateFormat timeFormat = new SimpleDateFormat("hh:mm a"); // Customize the format as needed
					timeFormat.setTimeZone(TimeZone.getTimeZone("IST"));
					String time = timeFormat.format(entry.getRequestRaisedDate());
					endCustomerComplaintData.setRequestRaisedTime(time);
				}
				if(entry.getNextVisitDate()!=null) {
					DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
					endCustomerComplaintData.setNextSiteVisit(dateFormat.format(entry.getNextVisitDate()));
				}

				if(entry.getTsoAssigned()!=null) {
					endCustomerComplaintData.setTsoAssignedName(entry.getTsoAssigned().getName());
					endCustomerComplaintData.setTsoAssignedEmail(entry.getTsoAssigned().getUid());
				}
				if(entry.getTaluka()!=null){
					endCustomerComplaintData.setTaluka(entry.getTaluka());
				}
				if(entry.getState()!=null){
					endCustomerComplaintData.setState(entry.getState());
				}
				if(entry.getDistrict()!=null){
					endCustomerComplaintData.setDistrict(entry.getDistrict());
				}


				dataList.add(endCustomerComplaintData);
			});
		}
		final SearchPageData<EndCustomerComplaintData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(dataList);
		return result;
	}

	@Override
	public SearchPageData<TechnicalAssistanceData> getTechnicalAssistances(SearchPageData searchPageData, String startDate, String endDate, String name
			,String requestNo, String filters, List<String> status, List<String> constructionAdvisory,List<String> taluka) {

		SearchPageData<TechnicalAssistanceModel> searchResult = technicalAssistanceService.getTechnicalAssistances(searchPageData, startDate, endDate, name, requestNo, filters,status, constructionAdvisory,taluka);
		//List<TechnicalAssistanceModel> modelList = technicalAssistanceService.getTechnicalAssistanceRequestList(userId, year, month, requestStatus, key);

		List<TechnicalAssistanceData> dataList = new ArrayList<>();
		if(searchResult.getResults()!=null && !searchResult.getResults().isEmpty()){
			dataList = technicalAssistanceConverter.convertAll(searchResult.getResults());
		}
		/*
		TechnicalAssistanceListData list = new TechnicalAssistanceListData();
		list.setTechnicalAssistance(dataList);*/
		final SearchPageData<TechnicalAssistanceData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(dataList);
		return result;
	}

	@Override
	public TechnicalAssistanceData getTechnicalAssistanceRequestDetails(String requestNo) {
		
		TechnicalAssistanceModel model = technicalAssistanceService.getTechnicalAssistanceRequestDetails(requestNo);
		TechnicalAssistanceData data = new TechnicalAssistanceData();
		
		data.setName(model.getName());
		if(model.getConstructionStage()!=null){
			data.setConstructionStage(String.valueOf(model.getConstructionStage()));
		}
	
		data.setLine1(model.getLine1());
		data.setLine2(model.getLine2());
		data.setState(model.getState());
		data.setDistrict(model.getDistrict());
		data.setTaluka(model.getTaluka());
		data.setCity(model.getCity());
		data.setPostalCode(model.getPostalCode());
		data.setContactNumber(model.getCellPhone());
		if(model.getSite()!=null) {
			data.setSiteId(model.getSite().getUid());
			data.setSiteName(model.getSite().getName());
		}
		if (model.getTsoStatus()!=null && userService.getCurrentUser().getGroups()!=null && userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
			data.setRequestStatus(null != enumerationService.getEnumerationName(model.getTsoStatus(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(model.getTsoStatus(), i18NService.getCurrentLocale()) : model.getTsoStatus().getCode() );
		}
		else if(model.getRequestStatus()!=null){
			data.setRequestStatus(null != enumerationService.getEnumerationName(model.getRequestStatus(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(model.getRequestStatus(), i18NService.getCurrentLocale()) : model.getRequestStatus().getCode());
		}
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
	    if(model.getDateOfSupervisionRequired()!=null)
	    {
	    	data.setDateOfSupervisionRequired(formatter.format(model.getDateOfSupervisionRequired()));
	    }
		
		data.setDurationOfSupervisionRequired(model.getDurationOfSupervisionRequired());
		if(model.getExpertiseRequired()!=null) {
			data.setExpertiseRequired(String.valueOf(model.getExpertiseRequired()));
		}
		if(model.getConstructionAdvisorys()!=null) {
			//data.setConstructionAdvisory(null != enumerationService.getEnumerationName(model.getConstructionAdvisory(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(model.getConstructionAdvisory(), i18NService.getCurrentLocale()) : model.getConstructionAdvisory().getCode());
			data.setConstructionAdvisory(model.getConstructionAdvisorys());		
		}
		List<TrackingData> trackingDataList = new ArrayList<>();

		TrackingData data1 = new TrackingData();
		data1.setName("Request Raised");
		data1.setActualTime(model.getRequestDate());
		data1.setCode(data1.getName());
		data1.setIndex(1);
		if(model.getTsoAssigned()!=null) {
			List<String> comments = new ArrayList<String>();
			comments.add("Tso Assigned: " + model.getTsoAssigned().getUid());
			data1.setComment(comments);
		}
		trackingDataList.add(data1);

		TrackingData data3 = new TrackingData();
		data3.setName("Service Ongoing");
		data3.setActualTime(model.getAcceptedDate());
		data3.setCode(data3.getName());
		data3.setIndex(2);
		trackingDataList.add(data3);

		TrackingData data4 = new TrackingData();
		data4.setName("Service Completed");
		data4.setActualTime(model.getClosedDate());
		data4.setCode(data4.getName());
		data4.setIndex(3);
		trackingDataList.add(data4);

		if(model.getRejectedDate()!=null) {
			TrackingData data5 = new TrackingData();
			data5.setName("Service Rejected");
			data5.setActualTime(model.getRejectedDate());
			data5.setCode(data5.getName());
			data5.setIndex(4);
			trackingDataList.add(data5);
		}

		data.setTrackingDetails(trackingDataList);
		return data;
	}

  @Override
	public DealerListData getAllRetailersForMaterial(String userId) {

		List<SclCustomerModel> retailersList = getTechnicalAssistanceService().getAllRetailersForMaterial(userId);
		List<CustomerData> allData= Optional.of(retailersList.stream()
				.map(b2BCustomer -> getDealerBasicConverter()
						.convert(b2BCustomer)).collect(Collectors.toList())).get();
		DealerListData dataList = new DealerListData();
		dataList.setDealers(allData);
		return dataList;
	}

	@Override
	public DealerListData getAllDealersForMaterial(String userId) {
		List<SclCustomerModel> dealersList = getTechnicalAssistanceService().getAllDealersForMaterial(userId);
		List<CustomerData> allData= Optional.of(dealersList.stream()
				.map(b2BCustomer -> getDealerBasicConverter()
						.convert(b2BCustomer)).collect(Collectors.toList())).get();
		DealerListData dataList = new DealerListData();
		dataList.setDealers(allData);
		return dataList;
	}

	@Override
	public Boolean submitCustomerComplaint(CustomerComplaintData customerComplaintData) {
		CustomerComplaintModel customerComplaintModel = getModelService().create(CustomerComplaintModel.class);
		getCustomerComplaintReverseConverter().convert(customerComplaintData, customerComplaintModel);
		return getTechnicalAssistanceService().submitCustomerComplaint(customerComplaintModel);
	}

	@Override
	public InvoiceMasterData getInvoiceMasterByInvoiceNo(String invoiceNo) {
		InvoiceMasterModel invoiceMasterModel = getTechnicalAssistanceService().getInvoiceMasterByInvoiceNo(invoiceNo);
		return getInvoiceMasterDataConverter().convert(invoiceMasterModel);
	}

	@Override
	public CustomerData getCustomerByCode(String customerCode) {
		if(Objects.nonNull(customerCode)){
			SclCustomerModel sclCustomerModel = getSclCustomerService().getSclCustomerForUid(customerCode);
			CustomerData customerData =  getDealerBasicConverter().convert(sclCustomerModel);
			return customerData;
		}
		return new CustomerData();
	}

	@Override
	public CustomerComplaintData getCustomerAssistanceRequestDetails(String requestNo) {
		CustomerComplaintModel model = technicalAssistanceService.getCustomerAssistanceRequestDetails(requestNo);
		CustomerComplaintData data = new CustomerComplaintData();

		data.setName(model.getName());

		data.setConstructionStage(null != enumerationService.getEnumerationName(model.getConstructionStage(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(model.getConstructionStage(), i18NService.getCurrentLocale()) : model.getConstructionStage().getCode());

		SCLAddressData address = new SCLAddressData();
		sclAddressPopulator.populate(model.getAddress(), address);
		data.setAddress(address);

		if(model.getRequestDate()!=null)
		{
			data.setRequestDate(model.getRequestDate());
		}

		data.setDurationOfSupervisionRequired(model.getDurationOfSupervisionRequired());
		data.setExpertiseRequired(null != enumerationService.getEnumerationName(model.getExpertiseRequired(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(model.getExpertiseRequired(), i18NService.getCurrentLocale()) : model.getExpertiseRequired().getCode());

		return data;
	}

	@Override
	public CustomerAssistanceListData getCustomerAssistanceRequestList(String userId, Integer year, Integer month, String status, String searchKey) {
		List<CustomerComplaintModel> modelList = technicalAssistanceService.getCustomerAssistanceRequestList(userId, year, month, status, searchKey);

		List<CustomerComplaintData> dataList = new ArrayList<>();

		dataList = customerAssistanceConverter.convertAll(modelList);

		CustomerAssistanceListData list = new CustomerAssistanceListData();
		list.setCustomerAssistanceList(dataList);

		return list;
	}

	public TechnicalAssistanceService getTechnicalAssistanceService() {
		return technicalAssistanceService;
	}

	public void setTechnicalAssistanceService(TechnicalAssistanceService technicalAssistanceService) {
		this.technicalAssistanceService = technicalAssistanceService;
	}

	public Converter<SclCustomerModel, CustomerData> getDealerBasicConverter() {
		return dealerBasicConverter;
	}

	public void setDealerBasicConverter(Converter<SclCustomerModel, CustomerData> dealerBasicConverter) {
		this.dealerBasicConverter = dealerBasicConverter;
	}

	public Converter<CustomerComplaintData, CustomerComplaintModel> getCustomerComplaintReverseConverter() {
		return customerComplaintReverseConverter;
	}

	public void setCustomerComplaintReverseConverter(Converter<CustomerComplaintData, CustomerComplaintModel> customerComplaintReverseConverter) {
		this.customerComplaintReverseConverter = customerComplaintReverseConverter;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public Converter<InvoiceMasterModel, InvoiceMasterData> getInvoiceMasterDataConverter() {
		return invoiceMasterDataConverter;
	}

	public void setInvoiceMasterDataConverter(Converter<InvoiceMasterModel, InvoiceMasterData> invoiceMasterDataConverter) {
		this.invoiceMasterDataConverter = invoiceMasterDataConverter;
	}

	public SclCustomerService getSclCustomerService() {
		return sclCustomerService;
	}

	public void setSclCustomerService(SclCustomerService sclCustomerService) {
		this.sclCustomerService = sclCustomerService;
	}
  
	@Override
	public Boolean saveSOComment(String requestNo, String comment) {
		return technicalAssistanceService.saveSOComment(requestNo, comment);
	}

	@Override
	public Boolean markRequestAsCompleted(String requestNo,String resolvedComment) {
		return technicalAssistanceService.markRequestAsCompleted(requestNo,resolvedComment);
	}

	@Override
	public Boolean newRequestForm(NetworkAssitanceData networkAssitanceData) {
		return technicalAssistanceService.newRequestForm(networkAssitanceData);
	}

	@Override
	public String addEndCustomerDetails(EndCustomerComplaintData data) {
		EndCustomerComplaintModel customerComplaintModel = getModelService().create(EndCustomerComplaintModel.class);
		EndCustomerComplaintModel convert = endCustomerComplaintReverseConverter.convert(data, customerComplaintModel);
		if (Objects.nonNull(convert)) {
			return convert.getRequestId();
		}else {
			return null;
		}
	}
	
	@Override
	public EndCustomerComplaintData getEndCustomerDetailsByRequestId(String requestId, boolean isUiRelatedChange) {
		EndCustomerComplaintData data=new EndCustomerComplaintData();
		if(requestId!=null) {
			EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(requestId);
			populateEndCustomerComplaintModel(model, data,isUiRelatedChange);
		}
		return data;
	}
	
	public EndCustomerComplaintData populateEndCustomerComplaintModel(EndCustomerComplaintModel model, EndCustomerComplaintData data, boolean isUIRelatedChange){

		if(model!=null){
					//1st Form Customer Details
						data.setName(model.getCustomerName());
						data.setCustomerNo(model.getCustomerNo());
						data.setSiteAddressLine1(model.getAddressLine1());
						data.setSiteAddressLine2(model.getAddressLine2());
						data.setPhoneNumber(model.getPhoneNumber());
						data.setDistrict(model.getDistrict());
						data.setState(model.getState());
						data.setCity(model.getCity());
						data.setTaluka(model.getTaluka());
						data.setPinCode(model.getPincode());
						data.setLastVisitDate(model.getLastVisitDate());
						data.setNoOfVisitDone(model.getNoOfVisitDone());
					//2nd Form - Material Details
					if(model.getDealer()!=null) {
						data.setDealer(model.getDealer().getName());
					}
					if(model.getRetailer()!=null) {
						data.setRetailer(model.getRetailer().getName());
					}
					if(model.getSite()!=null) {
						data.setSiteId(model.getSite().getUid());
						data.setSiteName(model.getSite().getName());
						data.setLatitude(model.getSite().getLatitude());
						data.setLongitude(model.getSite().getLongitude());
					}
						data.setDateOfPurchase(String.valueOf(model.getDateOfPurchase()));
					if (model.getProduct() != null) {
						data.setProduct(model.getProduct().getCode());
					}
						data.setNumberOfPurchase(model.getNumberOfPurchase());
						data.setBatchNo(model.getBatchNo());
						data.setIsBillAvailable(model.getIsBillAvailable());
						if(model.getUploadInvoice()!=null) {
							data.setUploadInvoice(model.getUploadInvoice().getRealFileName());
							data.setUploadInvoiceUrl(model.getUploadInvoice().getURL());
						}
						data.setDepot(model.getDepot());
					//3rd Form - compaint Details
						data.setDateOfUse(String.valueOf(model.getDateOfUse()));
						if(model.getNatureOfComplaint()!=null) {
							data.setNatureOfComplaint(model.getNatureOfComplaint().getCode());
						}
						data.setProblemPerceivedByCustomer(model.getProblemPerceivedByCustomer());
						data.setProblemReportedByDepot(model.getProblemReportedByDepot());
						data.setAmountOfDamagePerceivedByCustomer(model.getAmountOfDamagePerceivedByCustomer());
						data.setAmountOfDamagePerceivedByDepot(model.getAmountOfDamagePerceivedByDepot());
						data.setAreaOfApplication(model.getAreaOfApplication());
						data.setCommentsAboutSurrounding(model.getCommentsAboutSurrounding());
					   	data.setNoOfBagsConsumed(model.getNoOfBagsConsumed());
						//common Details
					data.setRequestId(model.getRequestId());
					data.setRequestRaisedDate(String.valueOf(model.getRequestRaisedDate()));
					String soTaggedname = null;
					if(model.getSoTaggedName()!=null) {
						soTaggedname = model.getSoTaggedName();
					}
					if(model.getRaisedBy()!=null) {
						data.setRaisedBy(model.getRaisedBy().getName());
						if(soTaggedname==null && model.getRaisedBy().getGroups()!=null && model.getRaisedBy().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
							soTaggedname = model.getRaisedBy().getUid();
						}
					}
					data.setSoTagged(soTaggedname);
					data.setIsSiteVisitRequired(model.getIsSiteVisitRequired());
					data.setPlant(model.getPlant());

					if(isUIRelatedChange) {
						if (model.getTsoStatus()!=null && userService.getCurrentUser().getGroups()!=null && userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
							data.setStatus(model.getTsoStatus().getCode());
						}
						else if(model.getStatus()!=null) {
							data.setStatus(model.getStatus().getCode());
						}
					}
					else {
						if(model.getTsoStatus()!=null) {
							data.setStatus(model.getTsoStatus().getCode());
						}
					}

					TicketClosureRequestData cr = new TicketClosureRequestData();
					cr.setTsoComment(model.getTsoComment());
					cr.setCustomerComment(model.getCustomerComment());
					cr.setTicketClosureRequestDate(model.getTicketClosureRequestDate());
					data.setTicketClosureRequest(cr);


					if(model.getDispatchDetails()!=null && !model.getDispatchDetails().isEmpty()) {
						List<ComplaintDispatchDetailsData> dispatchList = getComplaintDispatchDetails(data, model);
						data.setDispatchDetails(dispatchList);
					}

					if(model.getCurrentRootCause()!=null){
						ComplaintRootCauseIndentifiedData rootCauseData =complaintRootCauseIdentifiedConverter.convert(model.getCurrentRootCause());
						data.setRootCauseIdentified(rootCauseData);
					}

					if(model.getCurrentNextScheduleVisit()!=null){
						ScheduleNextVisitData visitData = scheduleNextVisitDataConverter.convert(model.getCurrentNextScheduleVisit());
						data.setScheduleNextVisit(visitData);
					}

					if(model.getCurrentSiteVisitNotRequired()!=null){
						ComplaintSiteVisitNotRequiredData siteVisitNotRequiredData = complaintSiteVisitNotRequiredConverter.convert(model.getCurrentSiteVisitNotRequired());
						data.setSiteVisitNotRequired(siteVisitNotRequiredData);
					}

					if(model.getCurrentDetailsFromSite()!=null){
						DetailsFromSiteData siteData = detailsFromSiteConverter.convert(model.getCurrentDetailsFromSite());
						data.setDetailsFromSite(siteData);
					}
					if(model.getCurrentTestPerformed()!=null){
						ComplaintTestPerformedData complaintTestPerformedData = complaintTestperformedConverter.convert(model.getCurrentTestPerformed());
						data.setTestPerformed(complaintTestPerformedData);
					}
					if(model.getTsoAssigned()!=null) {
						data.setAssignedTsoEmailId(model.getTsoAssigned().getUid());
						data.setTsoAssignedName(model.getTsoAssigned().getName());
					}
					if(model.getProduct()!=null && model.getProduct().getBrand()!=null) {
						data.setBrand(model.getProduct().getBrand().getIsocode());
					}

				}


		return data;
	}

    @Override
    public EndCustomerComplaintData getEndCustomerComplaintRequestTrackerDetails(String requestId) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(requestId);
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		data.setName(model.getCustomerName());

		data.setSiteAddressLine1(model.getAddressLine1());
		data.setSiteAddressLine2(model.getAddressLine2());
		data.setState(model.getState());
		data.setDistrict(model.getDistrict());
		data.setTaluka(model.getTaluka());
		data.setCity(model.getCity());
		data.setPinCode(model.getPincode());
		data.setPhoneNumber(model.getPhoneNumber());
		if(model.getRaisedBy()!=null) {
			data.setRaisedBy(model.getRaisedBy().getName());
		}

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

		if(model.getDateOfUse()!=null)
		{
			data.setDateOfUse(formatter.format(model.getDateOfUse()));
		}

		if(model.getTsoAssigned()!=null) {
			data.setTsoAssignedName(model.getTsoAssigned().getName());
			data.setAssignedTsoEmailId(model.getTsoAssigned().getUid());
		}

		List<TrackingData> trackingDataList = new ArrayList<>();

		TrackingData data1 = new TrackingData();
		data1.setName("Request Raised");
		data1.setActualTime(model.getRequestRaisedDate());
		data1.setCode(data1.getName());
		data1.setIndex(1);
		if(model.getTsoAssigned()!=null) {
			List<String> comments = new ArrayList<String>();
			comments.add("Tso Assigned: " + model.getTsoAssigned().getUid());
			data1.setComment(comments);
		}
		trackingDataList.add(data1);

//		TrackingData data2 = new TrackingData();
//		data2.setName("TSO Assigned");
//		data2.setActualTime(null);
//		data2.setCode(data2.getName());
//		data2.setIndex(2);
//		trackingDataList.add(data2);

		TrackingData data3 = new TrackingData();
		data3.setName("Service Ongoing");
		data3.setActualTime(model.getServiceOngoingDate());
		data3.setCode(data3.getName());
		data3.setIndex(2);
		trackingDataList.add(data3);

		TrackingData data4 = new TrackingData();
		data4.setName("Service Completed");
		data4.setActualTime(model.getTicketClosureRequestDate());
		data4.setCode(data4.getName());
		data4.setIndex(3);
		trackingDataList.add(data4);

		data.setTrackingDetails(trackingDataList);
		return data;
    }

	@Override
	public EndCustomerComplaintData updateCustomerComplaint(String complaintId, String soTaggedName, Boolean isSiteVisitRequired) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintRequestNumber(complaintId);
		EndCustomerComplaintData data = new EndCustomerComplaintData();
		SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
		if(model!=null){
			if(Objects.nonNull(isSiteVisitRequired)) {
				model.setIsSiteVisitRequired(isSiteVisitRequired);
			}
			if(model.getSoTaggedName()!=null) {
				model.setSoTaggedName(soTaggedName);
			}
			if(model.getServiceOngoingDate()==null)
			{
				model.setServiceOngoingDate(new Date());
				model.setStatus(TAServiceRequestStatus.SERVICE_ONGOING);
			}
      /*  model.setTsoAssigned(currentUser);
        model.setTsoAssignedDate(new Date());
*/
			modelService.save(model);
			data.setRequestId(complaintId);
		}
		else{
			throw new UnknownIdentifierException("No complaint found for the request complaint id. ");
		}
		return data;
	}

	@Override
	public EndCustomerComplaintData bookTechnicalExpert(ComplaintTEMeetingData complaintTEMeetingData) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintTEMeetingData.getComplaintId());
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		if(model!=null){
			ComplaintTEMeetingModel complaintTEMeetingModel = new ComplaintTEMeetingModel();
			complaintTEMeetingModel.setId(String.valueOf(complaintmeetIdGenerator.generate()));
			complaintTEMeetingModel.setAdditionalComment(complaintTEMeetingData.getAdditinonalComment());

			if(complaintTEMeetingData.getMeetingDate()!=null) {
				try {
					Date meetingDate = new SimpleDateFormat("dd/MM/yyyy").parse(complaintTEMeetingData.getMeetingDate());
					complaintTEMeetingModel.setMeetingDate(meetingDate);
				} catch (ParseException e) {

					throw new IllegalArgumentException(String.format("Please provide valid date %s", complaintTEMeetingData.getMeetingDate()));
				}
			}

			complaintTEMeetingModel.setMeetingTime(complaintTEMeetingData.getMeetingTime());
			if(complaintTEMeetingData.getModeOfMeeting()!=null){
				complaintTEMeetingModel.setModeOfMeeting(ModeOfMeeting.valueOf(complaintTEMeetingData.getModeOfMeeting()));
			}
			if(complaintTEMeetingData.getSupportRequired()!=null){
				complaintTEMeetingModel.setSupportRequired(TESuppportRequired.valueOf(complaintTEMeetingData.getSupportRequired()));
			}
			complaintTEMeetingModel.setComplaint(model);
			modelService.save(complaintTEMeetingModel);
			model.setCurrentTEmeeting(complaintTEMeetingModel);
			modelService.save(model);
			data.setRequestId(complaintTEMeetingData.getComplaintId());
		}

		return data;
	}

	@Override

	public EndCustomerComplaintData submitTicketClosureRequest(TicketClosureRequestData ticketClosureRequestData) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(ticketClosureRequestData.getComplaintId());
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		if(model!=null){
			model.setCustomerComment(ticketClosureRequestData.getCustomerComment());
			model.setTsoComment(ticketClosureRequestData.getTsoComment());
			model.setTsoStatus(CustomerComplaintTSOStatus.CLOSURE_REQUEST_SENT);
			model.setStatus(TAServiceRequestStatus.SERVICE_COMPLETED);
			model.setTicketClosureRequestDate(new Date());
			modelService.save(model);
			data.setRequestId(ticketClosureRequestData.getComplaintId());
		}
		return data;
	}

	@Override
	public EndCustomerComplaintData saveSiteVisitNotRequiredForm(ComplaintSiteVisitNotRequiredData complaintSiteVisitNotRequiredData) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintSiteVisitNotRequiredData.getComplaintId());
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		if(model!=null){
			ComplaintSiteVisitNotRequiredModel siteVisitNotRequiredModel = new ComplaintSiteVisitNotRequiredModel();
			siteVisitNotRequiredModel.setId((String) siteVisitNotRequiredIdGenerator.generate());
			siteVisitNotRequiredModel.setVisitNotRequiredReason(complaintSiteVisitNotRequiredData.getVisitNotRequiredReason());
			siteVisitNotRequiredModel.setTeComments(complaintSiteVisitNotRequiredData.getTeComment());
			siteVisitNotRequiredModel.setAdditionalComment(complaintSiteVisitNotRequiredData.getAdditionalComment());
			siteVisitNotRequiredModel.setRootCause(SiteRootCause.valueOf(complaintSiteVisitNotRequiredData.getRootCause()));
			siteVisitNotRequiredModel.setSolution(complaintSiteVisitNotRequiredData.getSolution());
			siteVisitNotRequiredModel.setIsCallHappenedWithTE(complaintSiteVisitNotRequiredData.getIscallHappenedWithTE());
			siteVisitNotRequiredModel.setProblemAsReportedByCustomer(complaintSiteVisitNotRequiredData.getProblemAsReportedByCustomer());
			
			siteVisitNotRequiredModel.setComplaint(model);
			modelService.save(siteVisitNotRequiredModel);
			model.setCurrentSiteVisitNotRequired(siteVisitNotRequiredModel);
			modelService.save(model);

			data.setRequestId(complaintSiteVisitNotRequiredData.getComplaintId());
		}
		return data;
	}

	@Override
	public EndCustomerComplaintData saveComplaintDispatchDetails(ComplaintDispatchDetailsData complaintDispatchDetailsData) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintDispatchDetailsData.getComplaintId());
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		if (model != null) {
			ComplaintDispatchDetailsModel complaintDispatchDetailsModel = new ComplaintDispatchDetailsModel();
			complaintDispatchDetailsModel.setId((String) complaintDispatchedDetailsGenerator.generate());
	//		complaintDispatchDetailsModel.setTypeOfCement(TypeOfCement.valueOf(complaintDispatchDetailsData.getTypeOfCement()));
			complaintDispatchDetailsModel.setDispatchedFrom(complaintDispatchDetailsData.getDispatchFrom());
			complaintDispatchDetailsModel.setQuantity(complaintDispatchDetailsData.getQuantity());
			complaintDispatchDetailsModel.setCahllanNo(complaintDispatchDetailsData.getChallanNo());
			complaintDispatchDetailsModel.setTypeOfPacking(complaintDispatchDetailsData.getTypeOfPacking());
			if(complaintDispatchDetailsData.getTypeOfCement()!=null){
				complaintDispatchDetailsModel.setSiteCementType(siteManagementDao.findSiteCementTypeByCode(complaintDispatchDetailsData.getTypeOfCement()));
			}
			if (complaintDispatchDetailsData.getDateOfDispatch() != null) {
				try {
					Date dispatchDate = new SimpleDateFormat("dd/MM/yyyy").parse(complaintDispatchDetailsData.getDateOfDispatch());
					complaintDispatchDetailsModel.setDateOfDispatch(dispatchDate);
				} catch (ParseException e) {

					throw new IllegalArgumentException(String.format("Please provide valid date %s", complaintDispatchDetailsData.getDateOfDispatch()));
				}
			}
			if (complaintDispatchDetailsData.getInvoiceDate() != null) {
				try {
					Date invoiceDate = new SimpleDateFormat("dd/MM/yyyy").parse(complaintDispatchDetailsData.getInvoiceDate());
					complaintDispatchDetailsModel.setInvoiceDate(invoiceDate);
				} catch (ParseException e) {

					throw new IllegalArgumentException(String.format("Please provide valid date %s", complaintDispatchDetailsData.getInvoiceDate()));
				}
			}
			if (complaintDispatchDetailsData.getManufacturingDate() != null) {
				try {
					Date manufacturingDate = new SimpleDateFormat("dd/MM/yyyy").parse(complaintDispatchDetailsData.getManufacturingDate());
					complaintDispatchDetailsModel.setManufacturingDate(manufacturingDate);
				} catch (ParseException e) {

					throw new IllegalArgumentException(String.format("Please provide valid date %s", complaintDispatchDetailsData.getManufacturingDate()));
				}
			}

			complaintDispatchDetailsModel.setWeekNumber(complaintDispatchDetailsData.getWeekNumber());
			complaintDispatchDetailsModel.setInvoiceNo(complaintDispatchDetailsData.getInvoiceNo());
			complaintDispatchDetailsModel.setTransporters(complaintDispatchDetailsData.getTransporters());
			complaintDispatchDetailsModel.setComplaint(model);
			modelService.save(complaintDispatchDetailsModel);
			model.setCurrentDispatchDetails(complaintDispatchDetailsModel);
			modelService.save(model);

			data.setRequestId(complaintDispatchDetailsData.getComplaintId());
		}
		return data;
	}
    @Override
	public EndCustomerComplaintData submitNextVisitPlanDetailes(ScheduleNextVisitData scheduleNextVisitData) {
		EndCustomerComplaintData data = new EndCustomerComplaintData();
		EndCustomerComplaintModel endCustomerComplaintModel=null;
		ScheduleNextVisitModel scheduleNextVisitModel=modelService.create(ScheduleNextVisitModel.class);
		scheduleNextVisitModel.setId((String) scheduleNextVisitGenerator.generate());
		if(scheduleNextVisitData!=null && scheduleNextVisitData.getComplaintId()!=null) {
			endCustomerComplaintModel = technicalAssistanceService.getEndCustomerComplaintForRequestNo(scheduleNextVisitData.getComplaintId());
		}
		Date visitDate=null;
		if(endCustomerComplaintModel!=null){
			if(scheduleNextVisitData.getNextSiteVisitDate()!=null){
				try {
					 visitDate = new SimpleDateFormat("yyyy-MM-dd").parse(scheduleNextVisitData.getNextSiteVisitDate());
					scheduleNextVisitModel.setNextSiteVisitDate(visitDate);
				} catch (ParseException e) {
					throw new IllegalArgumentException(String.format("Please provide valid date %s", scheduleNextVisitData.getNextSiteVisitDate()));
				}
			}
			if(scheduleNextVisitData.getNextSiteVisitTime()!=null){
				scheduleNextVisitModel.setNextSiteVisitTime(scheduleNextVisitData.getNextSiteVisitTime());
				DateTimeFormatter parser = DateTimeFormatter.ofPattern("h[:mm]a");
				LocalTime localTime = LocalTime.parse(scheduleNextVisitData.getNextSiteVisitTime(), parser);
				if(visitDate!=null) {
					LocalDate local_date = visitDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					LocalDateTime localDateTime = LocalDateTime.of(local_date, localTime);
					Date convertedDatetime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
					scheduleNextVisitModel.setNextSiteVisitDate(convertedDatetime);
				}
			}
			if(scheduleNextVisitData.getDetailsOfTaskToBeDone()!=null) {
				scheduleNextVisitModel.setDetailsOfTaskToBeDone(DetailsOfTaskToBeDone.valueOf(scheduleNextVisitData.getDetailsOfTaskToBeDone()));
			}
			if(scheduleNextVisitData.getSiteId()!=null) {
				scheduleNextVisitModel.setSite((SclSiteMasterModel)userService.getUserForUID(scheduleNextVisitData.getSiteId()));
				endCustomerComplaintModel.setSite(scheduleNextVisitModel.getSite());
			}
			modelService.save(scheduleNextVisitModel);
		}
		//scheduleNextVisitModel.setSiteVisitId(String.valueOf(nextVisitIdGenerator.generate()));
		if(endCustomerComplaintModel!=null) {
			endCustomerComplaintModel.setCurrentNextScheduleVisit(scheduleNextVisitModel);
			endCustomerComplaintModel.setNextVisitDate(visitDate);
			scheduleNextVisitModel.setComplaint(endCustomerComplaintModel);
			modelService.save(endCustomerComplaintModel);
		}
		modelService.save(scheduleNextVisitModel);

		data.setRequestId(scheduleNextVisitData.getComplaintId());
		return data;

	}

	@Override
	public CustomerComplaintHomePageData countOfAssignedTicketNumbers() {
		CustomerComplaintHomePageData data=new CustomerComplaintHomePageData();
		SclUserModel sclUserModel = (SclUserModel) userService.getCurrentUser();
		data.setOpenComplaintsCount(technicalAssistanceService.countOfAssignedTicketNumbers(sclUserModel));
		return  data;
	}

	@Override
	public EndCustomerComplaintData getComplaintDispatchDetails(String complaintId) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintId);
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		List<ComplaintDispatchDetailsData> dataList =  getComplaintDispatchDetails(data, model);
		data.setRequestId(complaintId);
		data.setDispatchDetails(dataList);
		return data;
	}

	private List<ComplaintDispatchDetailsData> getComplaintDispatchDetails(EndCustomerComplaintData data, EndCustomerComplaintModel model) {
		List<ComplaintDispatchDetailsData> dataList = new ArrayList<>();
	//	List<ComplaintDispatchDetailsModel> sortedDispatchDetails = new ArrayList<>();
		if(model!=null){
			Collection<ComplaintDispatchDetailsModel> dispatchDetails = model.getDispatchDetails();
			/*if(!dispatchDetails.isEmpty() && dispatchDetails!=null){
				for(ComplaintDispatchDetailsModel dispatchDetailsModel: dispatchDetails){
					if(dispatchDetailsModel.getDateOfDispatch()!=null){
						sortedDispatchDetails  = dispatchDetails.stream().sorted(Comparator.comparing(ComplaintDispatchDetailsModel::getDateOfDispatch).reversed()).collect(Collectors.toList());
					}
				}
			}*/
			Collection<ComplaintDispatchDetailsModel> sortedDispatchDetails  = dispatchDetails.stream().sorted(Comparator.comparing(ComplaintDispatchDetailsModel::getCreationtime).reversed()).collect(Collectors.toList());

			dataList = sclComplaintDispatchDetailsConverter.convertAll(sortedDispatchDetails);
		}
		return dataList;
	}
	
	@Override
	public ComplaintDispatchDetailsData removeComplaintDispatchDetails(String complaintId, String dispatchDetailsId) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintId);
		ComplaintDispatchDetailsData data = new ComplaintDispatchDetailsData();
		if(model!=null){
			Optional<ComplaintDispatchDetailsModel> optional = model.getDispatchDetails().stream().filter(dispatch->dispatch.getId().equals(dispatchDetailsId)).findAny();
			if(optional.isPresent()){
				ComplaintDispatchDetailsModel dispatchDetail = optional.get();
				//model.getDispatchDetails().remove(dispatchDetail);
				modelService.remove(dispatchDetail);

				data.setId(dispatchDetailsId);
			}

		}
		return data;
	}

	@Override
	public EndCustomerComplaintData saveComplaintRootCauseIdentifiedForm(ComplaintRootCauseIndentifiedData complaintRootCauseIndentifiedData) {
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintRootCauseIndentifiedData.getComplaintId());
		EndCustomerComplaintData data = new EndCustomerComplaintData();

		if(model!=null){
			ComplaintRootCauseIdentifiedModel complaintRootCauseIdentifiedModel = new ComplaintRootCauseIdentifiedModel();

			complaintRootCauseIdentifiedModel.setId((String) complaintRootCauseIdentifiedGenerator.generate());
			List<SiteRootCause> enumerationValues = enumerationService.getEnumerationValues(SiteRootCause.class);
			for (SiteRootCause enumerationValue : enumerationValues) {
				if(enumerationValue.equals(SiteRootCause.valueOf(complaintRootCauseIndentifiedData.getRootCause())))
				{
					complaintRootCauseIdentifiedModel.setRootCause(SiteRootCause.valueOf(complaintRootCauseIndentifiedData.getRootCause()));
				}
			}

			complaintRootCauseIdentifiedModel.setIsRootCauseIdentified(complaintRootCauseIndentifiedData.getIsRootCauseIdentified());
			complaintRootCauseIdentifiedModel.setAccountableDepartment(complaintRootCauseIndentifiedData.getAccountableDepartment());
			complaintRootCauseIdentifiedModel.setEmailId(complaintRootCauseIndentifiedData.getEmailId());

			complaintRootCauseIdentifiedModel.setComplaint(model);
			modelService.save(complaintRootCauseIdentifiedModel);
			model.setCurrentRootCause(complaintRootCauseIdentifiedModel);
			modelService.save(model);

			data.setRequestId(complaintRootCauseIndentifiedData.getComplaintId());
		}
		return data;
	}

	@Override
	public EndCustomerComplaintData saveDetailsFromSite(DetailsFromSiteData detailsFromSiteData) {
		EndCustomerComplaintData data = new EndCustomerComplaintData();
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(detailsFromSiteData.getComplaintId());
		DetailsFromSiteModel detailsFromSiteModel=modelService.create(DetailsFromSiteModel.class);
		if(model!=null){
			detailsFromSiteModel.setAreaOfTheSite(detailsFromSiteData.getAreaOfTheSite());
			detailsFromSiteModel.setIsCallHappenedWithTE(detailsFromSiteData.getIsCallHappenedWithTE());
			detailsFromSiteModel.setAdditionalComments(detailsFromSiteData.getAdditionalComments());
			detailsFromSiteModel.setTeComment(detailsFromSiteData.getTeComment());
			detailsFromSiteModel.setProductSampleAvailable(detailsFromSiteData.getProductSampleAvailable());
			detailsFromSiteModel.setId(String.valueOf(detailsFromSiteIdGenerator.generate()));
			modelService.save(detailsFromSiteModel);
			if(detailsFromSiteData.getUploadSitePicture()!=null && !detailsFromSiteData.getUploadSitePicture().isEmpty())
				detailsFromSiteModel.setUploadSitePictureStatus(submitUploadsitePicture(detailsFromSiteModel.getId(), detailsFromSiteData.getUploadSitePicture()));
			model.setCurrentDetailsFromSite(detailsFromSiteModel);
			detailsFromSiteModel.setComplaint(model);
			modelService.save(model);
			modelService.save(detailsFromSiteModel);
			data.setRequestId(detailsFromSiteData.getComplaintId());
		}
		return data;
	}

    @Override
    public EndCustomerComplaintData submitRequestForAnotherTSODetails(String complaintId, String commentForAnotherTSORequest) {
        return technicalAssistanceService.submitRequestForAnotherTSODetails(complaintId,commentForAnotherTSORequest);
    }


	public boolean submitUploadsitePicture(String siteId,List<String> uploadPhoto)
	{
		List<MediaModel> catalogUnawareMediaModels=new ArrayList<>();
		DetailsFromSiteModel detailsFromSiteModel = technicalAssistanceService.getDetailesFromSiteById(siteId);
		if(uploadPhoto!=null && !uploadPhoto.isEmpty()){
			for (String s : uploadPhoto) {
				MediaModel brandingPicture = getUploadPicture(s);
				catalogUnawareMediaModels.add(brandingPicture);
			}
			detailsFromSiteModel.setUploadSitePicture(catalogUnawareMediaModels);
		}
		modelService.save(detailsFromSiteModel);
		return true;
	}
	private MediaModel getUploadPicture(String data) {
		byte[] bytes = Base64.getDecoder().decode(data);
		MultipartFile multipartFile = getMultipartFile("UploadSitePicture", bytes);
		return createMediaFromFile("UploadSitePicture", "UploadSitePictureDoc", multipartFile);
	}
	public MultipartFile getMultipartFile(String name, byte[] bytes) {

		MultipartFile mfile = null;
		ByteArrayInputStream in = null;
		try {
			in = new ByteArrayInputStream(bytes);
			FileItemFactory factory = new DiskFileItemFactory(16, null);
			//FileItem fileItem = factory.createItem("mainFile", "jpeg", false, name);
			FileItem fileItem = factory.createItem(name, "jpeg", false, name);
			IOUtils.copy(new ByteArrayInputStream(bytes), fileItem.getOutputStream());
			mfile = new CommonsMultipartFile(fileItem);
			in.close();
		}catch (IOException e){
			LOG.error("unexpected error for getting multipart file" + e.getMessage());
		}
		return mfile;
	}

	private CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file) {

		Long currentTimeInMillis = System.currentTimeMillis();
		final String mediaCode = documentType.concat(SclCoreConstants.UNDERSCORE_CHARACTER).concat(uid).concat(String.valueOf(currentTimeInMillis));

		final MediaFolderModel imageMediaFolder = mediaService.getFolder(SclCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
		CatalogUnawareMediaModel documentMedia = null;

		try {
			documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
		} catch (AmbiguousIdentifierException ex) {
			LOG.error("More than one media found with code : " + mediaCode);
			LOG.error("Removing duplicate media : " + mediaCode);
			CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
			duplicateMedia.setCode(mediaCode);
			List<CatalogUnawareMediaModel> duplicateMedias = flexibleSearchService.getModelsByExample(duplicateMedia);
			modelService.removeAll(duplicateMedias);
		} catch (UnknownIdentifierException uie) {
			if (LOG.isDebugEnabled()) {
				LOG.error("No Media found with code : " + mediaCode);
			}
		} finally {
			if (null == documentMedia) {
				documentMedia = modelService.create(CatalogUnawareMediaModel.class);
				documentMedia.setCode(mediaCode);
				documentMedia.setRealFileName(file.getName());
			}
		}
		documentMedia.setFolder(imageMediaFolder);
		documentMedia.setMime(file.getContentType());
		documentMedia.setRealFileName(file.getName());
		modelService.save(documentMedia);
		try {
			mediaService.setStreamForMedia(documentMedia, file.getInputStream());
		} catch (IOException ioe) {
			LOG.error("IO Exception occured while creating: " + documentType + " ,for dealer with uid: " + uid);
		}

		return (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
	}

	@Override
	public EndCustomerComplaintData saveComplaintTestPerformed(ComplaintTestPerformedData complaintTestPerformedData) {
		EndCustomerComplaintData data = new EndCustomerComplaintData();
		EndCustomerComplaintModel model = technicalAssistanceService.getEndCustomerComplaintForRequestNo(complaintTestPerformedData.getComplaintId());

		if(model!=null){
			ComplaintTestPerformedModel complaintTestPerformedModel = modelService.create(ComplaintTestPerformedModel.class);
			complaintTestPerformedModel.setId((String) complaintTestPerformedGenerator.generate());
			if(complaintTestPerformedData.getServiceType()!=null) {
				complaintTestPerformedModel.setServiceType(siteManagementDao.findServiceTypeByCode(complaintTestPerformedData.getServiceType().getCode()));
			}

			List<ServiceTypeTestData> serviceTypeTestDataList = complaintTestPerformedData.getServiceTest();
			if (serviceTypeTestDataList != null && !serviceTypeTestDataList.isEmpty()) {

				List<SiteServiceTestModel> list = serviceTypeTestDataList.stream().map(serviceTypeTest -> siteManagementDao.findServiceTypeTestByCode(serviceTypeTest.getCode())).collect(Collectors.toList());
				complaintTestPerformedModel.setServiceTypeTest(list);
			}

			complaintTestPerformedModel.setComplaint(model);
			modelService.save(complaintTestPerformedModel);
			model.setCurrentTestPerformed(complaintTestPerformedModel);
			modelService.save(model);

			data.setRequestId(complaintTestPerformedData.getComplaintId());

		}
		return data;
	}
	
    @Override
    public TechnicalAssistanceData rejectTAByTSO(String requestNo, String rejectedReason) {
    	TechnicalAssistanceModel model  =  technicalAssistanceService.rejectTAByTSO(requestNo, rejectedReason);
    	TechnicalAssistanceData data = new TechnicalAssistanceData();
    	data.setRequestNo(model.getRequestNo());
    	return data;
    }
    
    @Override
    public TechnicalAssistanceData acceptTAByTSO(String requestNo, String siteId) {
    	TechnicalAssistanceModel model  =  technicalAssistanceService.acceptTAByTSO(requestNo, siteId);
    	TechnicalAssistanceData data = new TechnicalAssistanceData();
    	data.setRequestNo(model.getRequestNo());
    	return data;
    }
    
    @Override
    public TechnicalAssistanceData closeTAByTSO(String requestNo, String closeComment) {
    	TechnicalAssistanceModel model  =  technicalAssistanceService.closeTAByTSO(requestNo, closeComment);
    	TechnicalAssistanceData data = new TechnicalAssistanceData();
    	data.setRequestNo(model.getRequestNo());
    	return data;
    }

	/**
	 * Get TSO
	 * @param taluka
	 * @param district
	 * @param state
	 * @return tsoUserData
	 */
	@Override
	public TSOUserData getTSO(String taluka,String district, String state,String pendingRequest){
		TSOUserData tsoUserData = new TSOUserData();
		List<SclUserModel> sclUserModelList = technicalAssistanceService.getTSO(taluka, district, state);
		List<SclUserData> allSclUserData = new ArrayList<>();
		allSclUserData = getSclUserData(sclUserModelList,pendingRequest);
		List<SclUserData> sortedUserData = allSclUserData.stream().sorted(Comparator.comparing(SclUserData::getName)).collect(Collectors.toList());
		tsoUserData.setSclUserData(sortedUserData);
		return tsoUserData;
	}

	/**
	 * Get SclUser Data
	 * @param sclUserModelList
	 * @param pendingRequest
	 * @return sclUserData
	 */
	private List<SclUserData> getSclUserData(List<SclUserModel> sclUserModelList,String pendingRequest) {
		SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
		List<SclUserData> dataList = new ArrayList<>();
		int pendingReqCount = 0;
		LOG.info(String.format("TSOs mapped for the user %s are %s",currentUser.getUid(),sclUserModelList.toString()));
		for (SclUserModel sclUserModel : sclUserModelList) {
			SclUserData data = new SclUserData();
			data.setEmailId(sclUserModel.getUid());
			data.setContactNumber(sclUserModel.getMobileNumber());
			data.setName(sclUserModel.getName());
			if(StringUtils.isNotBlank(pendingRequest)) {
				if(pendingRequest.equalsIgnoreCase("CC")){
					pendingReqCount = technicalAssistanceService.countOfAssignedTicketNumbers(sclUserModel);
				} else if (pendingRequest.equalsIgnoreCase("TA")) {
					pendingReqCount = technicalAssistanceService.countOfTAAssignedTicketNumbers(sclUserModel);
				}
			}
			data.setPendingRequest(pendingReqCount);
			dataList.add(data);
		}
		return dataList;
	}

	private String createMediaUrl(String url) {
		String baseUrl=configurationService.getConfiguration().getString("media.host.base.url");
		return baseUrl+url;
	}


}
