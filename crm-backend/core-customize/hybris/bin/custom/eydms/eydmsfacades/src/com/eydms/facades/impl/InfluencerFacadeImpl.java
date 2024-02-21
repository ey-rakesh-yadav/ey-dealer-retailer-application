package com.eydms.facades.impl;

import java.time.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.annotation.Resource;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.InfluencerDao;
import com.eydms.core.dao.PointRequisitionDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.LeadStatus;
import com.eydms.core.model.*;

import com.eydms.core.enums.TypeOfVisitMaster;
import com.eydms.core.enums.VisitStatus;
import com.eydms.core.model.*;
import com.eydms.core.services.*;
import com.eydms.facades.DJPVisitFacade;

import com.eydms.core.enums.MeetStatus;
import com.eydms.core.enums.VenueType;
import com.eydms.core.model.EndCustomerComplaintModel;
import com.eydms.core.model.LeadMasterModel;
import com.eydms.core.model.MeetingCompletionFormModel;
import com.eydms.core.model.MeetingScheduleModel;
import com.eydms.core.services.NetworkService;

import com.eydms.facades.data.*;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.services.InfluencerService;
import com.eydms.core.services.SchemesAndDiscountService;
import com.eydms.facades.InfluencerFacade;
import com.eydms.facades.util.GenericMediaUtil;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

public class InfluencerFacadeImpl implements InfluencerFacade {

	private static final Logger LOG = Logger.getLogger(InfluencerFacadeImpl.class);

	@Autowired
	InfluencerService influencerService;
	@Autowired
	InfluencerDao influencerDao;
	@Autowired
	Converter<MeetingCompletionFormModel, MeetingCompletionFormData> meetingCompletionFormConverter;
	@Autowired
	Converter<B2BCustomerModel,EyDmsSiteData> eydmsSiteConverter;

	@Autowired
	Converter<MeetingCompletionFormData, MeetingCompletionFormModel> meetingCompletionFormReverseConverter;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	UserService userService;

	@Autowired
	SchemesAndDiscountService schemesAndDiscountService;

	@Autowired
	Converter<EYDMSAddressData, AddressModel> eydmsAddressReverseConverter;

	@Autowired
	private Converter<EyDmsCustomerModel, InfluencersDetails360WsData> influencerDetails360Converter;

	@Autowired
	ModelService modelService;

	@Resource
	private Converter<MultipartFile, MediaModel> eydmsMediaReverseConverter;

	@Resource
	private GenericMediaUtil genericMediaUtil;

	@Autowired
	Converter<AddressModel, AddressData> eydmsAddressConverter;

	@Autowired
	private Converter<MeetingScheduleModel, ScheduledMeetData> scheduledMeetConverter;

	@Autowired
	private Converter<AddressModel, AddressData> addressConverter;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SearchRestrictionService searchRestrictionService;

	@Autowired
	Populator<EYDMSAddressData, AddressModel> eydmsAddressReversePopulator;

	@Autowired
	CustomerAccountService customerAccountService;
	@Resource
	private NetworkService networkService;

	@Autowired
	DJPVisitService djpVisitService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
	KeyGenerator counterVisitIdGenerator;

	@Resource
	private DJPVisitFacade djpVisitFacade;


	@Autowired
	PointRequisitionDao pointRequisitionDao;


	@Resource
	private Converter<EyDmsCustomerModel, CustomerCardData> customerCardDataConverter;

	@Override
	public OnboardingPartnerData searchOnboardingPartner(List<String> searchKey) throws Exception {
		B2BCustomerModel partner = influencerService.searchOnboardingPartner(searchKey);
		OnboardingPartnerData data = new OnboardingPartnerData();

//		if(partner.getClass().equals(EyDmsUserModel.class))
//		{
//			List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(partner);
//			if(!subAreas.isEmpty())
//			{
//				address.setTaluka(subAreas.get(0).getTaluka());
//				address.setDistrict(subAreas.get(0).getDistrict());
//			}
//		}

		if (!Objects.isNull(partner)) {
			data.setUid(partner.getUid());
			data.setName(partner.getName());
			data.setProfilePic(partner.getProfilePicture() != null ? partner.getProfilePicture().getURL() : null);
			data.setEmail(partner.getEmail());
			data.setMobileNumber(partner.getMobileNumber());

			Collection<AddressModel> list = partner.getAddresses();
			if (CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
				if (billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if (null != billingAddress) {
						data.setAddress((eydmsAddressConverter.convert(billingAddress)));
					}
				}
			}
		}

		return data;
	}

	@Override
	public Boolean assignOnboardingPartner(String influencerUid, String partnerUid) {
		return influencerService.assignOnboardingPartner(influencerUid, partnerUid);
	}

	@Override
	public String getOnboardingStatus(String influencerId) {
		return influencerService.getOnboardingStatus(influencerId);
	}

	@Override
	public InfCockpitSchemeStatusData getGiftPointsStatus() {
		InfCockpitSchemeStatusData infCockpitSchemeStatusData = new InfCockpitSchemeStatusData();

		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
		String influencerType = currentUser.getInfluencerType().getCode();
		String state = currentUser.getState();

		HashMap<String, Double> codePointsMap = new HashMap<String, Double>();
		HashMap<String, String> codeNameMap = new HashMap<String, String>();
		Double totalRedeemablePoints = schemesAndDiscountService.getTotalAvailablePoints(currentUser.getUid());

		Double maxPoints = 0.0;
		Double minPoints = 0.0;
		Double pointsToNextGift = 0.0;
		String maxPointsGift = "";
		String minPointsGift = "";
		boolean isEligibleForNextGift = true;


		List<List<Object>> giftPointsStatusList = influencerService.getGiftSchemeStatusDetails(influencerType, state);
		if(Objects.isNull(giftPointsStatusList)) {
			return null;
		}

		for (List<Object> giftPoints : giftPointsStatusList) {
			String p_giftName = (String) giftPoints.get(0);
			String p_giftCode = (String) giftPoints.get(1);
			String pr_state = (String) giftPoints.get(2);
			String pr_influencerType = (String) giftPoints.get(3);
			Double pr_price = (Double) giftPoints.get(4);

			if (pr_state != null && pr_influencerType != null) {
				if (pr_state.contains(state) && pr_influencerType.contains(influencerType)) {
					codePointsMap.put(p_giftCode, pr_price);
					codeNameMap.put(p_giftCode, p_giftName);
				}
			} else if (pr_state != null) {
				if (pr_state.contains(state)) {
					if (codePointsMap.containsKey(p_giftCode)) {
						continue;
					} else {
						codePointsMap.put(p_giftCode, pr_price);
						codeNameMap.put(p_giftCode, p_giftName);
					}
				}
			} else if (pr_influencerType != null) {
				if (pr_influencerType.contains(influencerType)) {
					if (codePointsMap.containsKey(p_giftCode)) {
						continue;
					} else {
						codePointsMap.put(p_giftCode, pr_price);
						codeNameMap.put(p_giftCode, p_giftName);
					}
				}
			} else {
				if (codePointsMap.containsKey(p_giftCode)) {
					continue;
				} else {
					codePointsMap.put(p_giftCode, pr_price);
					codeNameMap.put(p_giftCode, p_giftName);
				}

			}

		}

		LOG.info("The GiftCode - Points Map is " + codePointsMap);
		LOG.info("The GiftCode - GiftName Map is " + codeNameMap);


		List<Double> codePointsMapList = codePointsMap.values().stream().collect(Collectors.toList());
		LOG.info("The points map list before sorting is " + codePointsMapList);

		Collections.sort(codePointsMapList);
		LOG.info("The points map list before sorting is " + codePointsMapList);

		if (totalRedeemablePoints > codePointsMapList.get(codePointsMapList.size() - 1)) {
			minPoints = codePointsMapList.get(codePointsMapList.size() - 1);
			maxPoints = null;
			isEligibleForNextGift = false;
		} else {
			for (int i = 0; i < codePointsMapList.size(); i++) {
				if (codePointsMapList.get(i) > totalRedeemablePoints) {
					if (i == 0) {
						maxPoints = codePointsMapList.get(i);
						minPoints = 0.0;
						break;
					} else {
						maxPoints = codePointsMapList.get(i);
						minPoints = codePointsMapList.get(i - 1);
						break;
					}
				}
			}
		}

		for (Map.Entry<String, Double> entry : codePointsMap.entrySet()) {
			if (entry.getValue() == minPoints) {
				minPointsGift = entry.getKey();
			}
			if (maxPoints != null && entry.getValue() == maxPoints) {
				maxPointsGift = entry.getKey();
			} else if (maxPoints == null) {
				maxPointsGift = null;
			}
		}

		pointsToNextGift = (maxPoints != null) ? (maxPoints - totalRedeemablePoints) : null;

		LOG.info("Minimum Points: " + minPoints);
		LOG.info("Minimum Points Gift: " + codeNameMap.get(minPointsGift));
		LOG.info("Maximum Points: " + maxPoints);
		if (maxPoints != null)
			LOG.info("Maximum Points Gift: " + codeNameMap.get(maxPointsGift));
		else
			LOG.info("There is no maximum points gift since the available points is greater than the max gift points");
		LOG.info("Points to next gift: " + pointsToNextGift);

		infCockpitSchemeStatusData.setTotalRedeemablePoints(totalRedeemablePoints);
		infCockpitSchemeStatusData.setMinPoints(minPoints);
		infCockpitSchemeStatusData.setMinPointsGift(codeNameMap.get(minPointsGift));
		infCockpitSchemeStatusData.setMaxPoints(maxPoints);
		infCockpitSchemeStatusData.setMaxPointsGift(codeNameMap.get(maxPointsGift));
		infCockpitSchemeStatusData.setPointsToNextGift(pointsToNextGift);
		infCockpitSchemeStatusData.setIsEligibleForNextGift(isEligibleForNextGift);

		return infCockpitSchemeStatusData;
	}

	@Override
	public Boolean editProfile(EyDmsCustomerData data) {
		EyDmsCustomerModel customer = (EyDmsCustomerModel) userService.getUserForUID(data.getUid());
		if (data.getName() != null || !data.getName().isBlank()) {
			customer.setName(data.getName());
		}

		Collection<AddressModel> list = customer.getAddresses();
		if (CollectionUtils.isNotEmpty(list)) {
			List<AddressModel> billingAddressList = list.stream().filter(address -> address.getBillingAddress()).collect(Collectors.toList());
			if (billingAddressList != null && !billingAddressList.isEmpty()) {
				AddressModel billingAddress = billingAddressList.get(0);
				if (null != billingAddress) {
					eydmsAddressReversePopulator.populate(data.getAddress(), billingAddress);
					customerAccountService.saveAddressEntry(customer, billingAddress);
				}
			}
		}

		if (data.getContactNumber() != null || !data.getContactNumber().isBlank()) {
			customer.setMobileNumber(data.getContactNumber());
		}

		if (!Objects.isNull(data.getProfilePic())) {
			customer.setProfilePicture(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(data.getProfilePic().getByteStream(), data.getProfilePic().getFileName())));
		}

		modelService.save(customer);
		return Boolean.TRUE;
	}

	@Override
	public OnboardingPartnerData fetchOnboardingPartnerDetails(String influencerUid) {
		EyDmsCustomerModel customerModel = influencerService.fetchOnboardingPartnerDetails(influencerUid);
		OnboardingPartnerData data = new OnboardingPartnerData();
		B2BCustomerModel partner = customerModel.getOnboardingPartner();
		if (partner != null) {
			data.setUid(partner.getUid());
			data.setName(partner.getName());
			data.setProfilePic(partner.getProfilePicture() != null ? partner.getProfilePicture().getURL() : null);
			data.setEmail(partner.getEmail());
			data.setMobileNumber(partner.getMobileNumber());

			Collection<AddressModel> list = partner.getAddresses();
			if (CollectionUtils.isNotEmpty(list)) {
				List<AddressModel> billingAddressList = list.stream().filter(a -> a.getBillingAddress()).collect(Collectors.toList());
				if (billingAddressList != null && !billingAddressList.isEmpty()) {
					AddressModel billingAddress = billingAddressList.get(0);
					if (null != billingAddress) {
						data.setAddress((addressConverter.convert(billingAddress)));
					}
				}
			}
		}
		return data;
	}

	@Override
	public Boolean submitMeetingCompletionForm(MeetingCompletionFormData data) {
		meetingCompletionFormReverseConverter.convert(data);
		return true;
	}


	@Override
	public InfluencerManagementHomePageData getInfluencerManagementHomePage(String influencerType) {
		InfluencerManagementHomePageData homePageData = new InfluencerManagementHomePageData();
		List<List<Object>> influencerTypeList = influencerService.getInfluencerTypeList(null);
		if (influencerTypeList != null && !influencerTypeList.isEmpty()) {
			influencerTypeList = influencerTypeList.stream().filter(s -> s != null && s.get(0) != null && s.get(1) != null).collect(Collectors.toList());
			Integer influencerTypeTotal = influencerTypeList.stream().map(s -> (Integer) s.get(1)).reduce(0, Integer::sum);
			if (influencerTypeTotal != null && influencerTypeTotal > 0) {
				List<InfluencerTypeData> infTypeList = new ArrayList<>();
				for (List<Object> list : influencerTypeList) {
					String influencerTypeName = (String) list.get(0);
					Integer influencerTypeCount = (Integer) list.get(1);
					InfluencerTypeData influencerTypeData = new InfluencerTypeData();
					influencerTypeData.setInfluencerType(influencerTypeName);
					influencerTypeData.setCount(influencerTypeCount);
					influencerTypeData.setPercentage((influencerTypeCount * 100.0) / influencerTypeTotal);
					infTypeList.add(influencerTypeData);
				}
				homePageData.setInfluencerTypes(infTypeList);
			}
		}

		List<List<Object>> influencerNetworkList = influencerService.getInfluencerNetworkTypeList(null);
		if (influencerNetworkList != null && !influencerNetworkList.isEmpty()) {
			influencerNetworkList = influencerNetworkList.stream().filter(s -> s != null && s.get(0) != null && s.get(1) != null).collect(Collectors.toList());
			Integer influencerNtwTypeTotal = influencerNetworkList.stream().map(s -> (Integer) s.get(1)).reduce(0, Integer::sum);
			if (influencerNtwTypeTotal != null && influencerNtwTypeTotal > 0) {
				List<InfluencerNetworkTypeData> ntwTypeList = new ArrayList<>();
				for (List<Object> list : influencerNetworkList) {
					String influencerNtwTypeName = (String) list.get(0);
					Integer influencerNtwTypeCount = (Integer) list.get(1);
					InfluencerNetworkTypeData influencerNtwTypeData = new InfluencerNetworkTypeData();
					influencerNtwTypeData.setNetworkType(influencerNtwTypeName);
					influencerNtwTypeData.setCount(influencerNtwTypeCount);
					influencerNtwTypeData.setPercentage((influencerNtwTypeCount * 100.0) / influencerNtwTypeTotal);
					ntwTypeList.add(influencerNtwTypeData);
				}
				homePageData.setNetworkTypes(ntwTypeList);
			}
		}

		List<List<Object>> influencerNetworkAdditionList = influencerService.getInfluencerNetworkAddition(influencerType);
		if (influencerNetworkAdditionList != null && !influencerNetworkAdditionList.isEmpty()) {
			int currentMonth = LocalDate.now().getMonthValue();
			int currentYear = LocalDate.now().getYear();
			int lastMonth = LocalDate.now().minusMonths(1).getMonthValue();
			int lastYear = LocalDate.now().minusMonths(1).getYear();
			Integer currentMonthAddition = 0;
			Integer lastMonthAddition = 0;
			Optional<Integer> currentMonthOpt = influencerNetworkAdditionList.stream().filter(s -> s != null && s.size()==3 && s.get(0) != null && s.get(1) != null && s.get(2) != null && ((Integer) s.get(0)) == currentYear && ((Integer) s.get(1)) == currentMonth).map(s -> (Integer) s.get(2)).findAny();
			Optional<Integer> lastMonthOpt = influencerNetworkAdditionList.stream().filter(s -> s != null && s.size()==3 && s.get(0) != null && s.get(1) != null && s.get(2) != null && ((Integer) s.get(0)) == lastYear && ((Integer) s.get(1)) == lastMonth).map(s -> (Integer) s.get(2)).findAny();
			if (currentMonthOpt.isPresent()) {
				currentMonthAddition = currentMonthOpt.get();
			}
			if (lastMonthOpt.isPresent()) {
				lastMonthAddition = lastMonthOpt.get();
			}
			homePageData.setNetworkAdditionCurrentMonth(currentMonthAddition);
			homePageData.setNetworkAdditionLastMonth(lastMonthAddition);
			Double growth = 0.0;
			try {
				if(lastMonthAddition>0) {
					Double growth1 = (100.0*((currentMonthAddition)-(lastMonthAddition)))/(lastMonthAddition);
					BigDecimal bd = new BigDecimal(Double.toString(growth1));
					bd = bd.setScale(2, RoundingMode.HALF_UP);
					growth = bd.doubleValue();
				}
			}
			catch(Exception e) {

			}
			homePageData.setNetworkAdditionGrowth(growth);
		}
		
		Integer leadsPendingForApproval = influencerService.getLeadsPendingForApproval(null);
		homePageData.setLeadPendingForApproval(leadsPendingForApproval);
		
		Integer onboardingsPendingForApproval = influencerService.getOnboardingsPendingForApproval(null);
		homePageData.setOnboardingPendingForApproval(onboardingsPendingForApproval);
		
		return homePageData;
	}

	@Override
	public CustomerCardListData getInfluencerOnboardingList(SearchPageData searchPageData,
															RequestCustomerData customerRequestData) {
		SearchPageData<EyDmsCustomerModel> customerList = influencerService.getInfluencerOnboardingList(searchPageData, customerRequestData);
		CustomerCardListData customerCardListData = new CustomerCardListData();
		if (customerList.getResults() != null && !customerList.getResults().isEmpty()) {
			customerCardListData.setCustomerCards(customerCardDataConverter.convertAll(customerList.getResults()));
		}
		if(customerList!=null && customerList.getPagination()!=null) {
			customerCardListData.setTotalCount(customerList.getPagination().getTotalNumberOfResults());
		}
		return customerCardListData;
	}

	@Override
	public SearchPageData<EyDmsLeadData> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData) {
		return influencerService.getPaginatedLeadList(searchPageData, leadRequestData);
	}

	@Override
	public EyDmsLeadData updateLeadStatus(String leadId, String status, String rejectedComment) {
		EyDmsLeadData leadData = new EyDmsLeadData();

		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		LeadMasterModel leadMasterModel = networkService.findItemByUidParam(leadId);

		if (leadMasterModel != null) {
			if (status.equalsIgnoreCase(LeadStatus.APPROVED.getCode())) {
				leadMasterModel.setApprovedBy(currentUser);
				leadMasterModel.setApprovedDate(new Date());
				leadMasterModel.setStatus(LeadStatus.valueOf(status));
				modelService.save(leadMasterModel);
				leadData.setLeadId(leadId);

			} else if (status.equalsIgnoreCase(LeadStatus.REJECTED.getCode())) {
				leadMasterModel.setRejectedBy(currentUser);
				leadMasterModel.setRejectedDate(new Date());
				leadMasterModel.setRejectedComment(rejectedComment);
				leadMasterModel.setStatus(LeadStatus.valueOf(status));
				modelService.save(leadMasterModel);
				leadData.setLeadId(leadId);
			}
		}
		return leadData;
	}

	@Override
	public EyDmsLeadData viewLeadDetailsById(String leadId) {
		EyDmsLeadData leadData = new EyDmsLeadData();
		LeadMasterModel leadMasterModel = networkService.findItemByUidParam(leadId);
		if (leadMasterModel != null) {
			if (leadMasterModel.getLeadType() != null) {
				leadData.setInfluencerType(leadMasterModel.getLeadType().getCode());
			}
			leadData.setName(leadMasterModel.getName());
			leadData.setMobileNo(leadMasterModel.getContactNo());
			leadData.setEmail(leadMasterModel.getEmail());
			leadData.setNetworkPotential(String.valueOf(leadMasterModel.getPotential()));
			if (leadMasterModel.getAssociatedBrand()!= null) {
				leadData.setBrandAssociatedWith(leadMasterModel.getAssociatedBrand().getIsocode());
			}
			if(Objects.nonNull(leadMasterModel.getAddress())) {
				AddressModel address = leadMasterModel.getAddress();
				leadData.setAddress(addressConverter.convert(address));
			}
			else
			{
				AddressData addressData = new AddressData();
				addressData.setLine1(leadMasterModel.getLine1());
				addressData.setLine2(leadMasterModel.getLine2());
				addressData.setState(leadMasterModel.getState());
				addressData.setDistrict(leadMasterModel.getDistrict());
				addressData.setTaluka(leadMasterModel.getTaluka());
				addressData.setCity(leadMasterModel.getCity());
				addressData.setErpCity(leadMasterModel.getCity());
				addressData.setPostalCode(leadMasterModel.getPostalCode());
				leadData.setAddress(addressData);
			}
		}
		return leadData;
	}

	@Override
	public SearchPageData<ScheduledMeetData> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status) {

		SearchPageData<MeetingScheduleModel> searchResult = influencerService.getPaginatedScheduleMeetList(searchPageData, meetingType, startDate, endDate, searchFilter, status);


		List<ScheduledMeetData> dataList = new ArrayList<>();
/*
		if(searchResult.getResults()!=null && !searchResult.getResults().isEmpty()){
			LOG.info(String.format("Size is  :: %s ", String.valueOf(searchResult.getResults().size())));
			for (MeetingScheduleModel result : searchResult.getResults()) {
				ScheduledMeetData data= new ScheduledMeetData();
				data= scheduledMeetConverter.convert(result);
				dataList.add(data);
			}

		}*/

		if (searchResult != null && searchResult.getResults() != null) {
			LOG.info(String.format("Size is  :: %s ", String.valueOf(searchResult.getResults().size())));
			List<MeetingScheduleModel> list = searchResult.getResults();
			list.forEach(entry -> {
				ScheduledMeetData data = new ScheduledMeetData();
				data = scheduledMeetConverter.convert(entry);
				dataList.add(data);
			});
		}

		final SearchPageData<ScheduledMeetData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(dataList);
		return result;
	}

	private void updateTotalVisitDetails(EyDmsCustomerModel influencer, EyDmsUserModel user) {
		int currentYear = LocalDate.now().getYear();
		int currentMonth = LocalDate.now().getMonthValue();
		if (influencer.getLastVisitTime() != null) {
			Instant instant = influencer.getLastVisitTime().toInstant();
			LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
			int lastVisitYear = lastVisitDate.getYear();
			int lastVisitMonth = lastVisitDate.getMonthValue();

			//Last Visit Last Month
			if ((lastVisitYear == currentYear && lastVisitMonth == currentMonth - 1) || (lastVisitYear == currentYear - 1 && lastVisitMonth == 12 && currentMonth == 1)) {

				//Site Master
				influencer.setLmInfluencerVisit(influencer.getCmInfluencerVisit());
				influencer.setLmInfluencerVisit(1);
			} else if (lastVisitYear == currentYear && lastVisitMonth == currentMonth) {
				//Site Master
				influencer.setCmInfluencerVisit(influencer.getCmInfluencerVisit() != null ? influencer.getCmInfluencerVisit() + 1 : 1);
			} else {
				//Site Master
				influencer.setLmInfluencerVisit(0);
				influencer.setCmInfluencerVisit(1);
			}
		} else {
			//Site Master
			influencer.setLmInfluencerVisit(0);
			influencer.setCmInfluencerVisit(1);
		}

		//EyDms user
		if (user.getLastInfluencerVisitDate() != null) {
			Instant instant = user.getLastInfluencerVisitDate().toInstant();
			LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
			int lastVisitYear = lastVisitDate.getYear();
			int lastVisitMonth = lastVisitDate.getMonthValue();

			if ((lastVisitYear == currentYear && lastVisitMonth == currentMonth - 1) || (lastVisitYear == currentYear - 1 && lastVisitMonth == 12 && currentMonth == 1)) {

				//Site Master
				user.setLmInfluencerVisit(user.getCmInfluencerVisit());
				user.setLmInfluencerVisit(1);
			} else if (lastVisitYear == currentYear && lastVisitMonth == currentMonth) {
				//Site Master
				user.setCmInfluencerVisit(user.getCmInfluencerVisit() != null ? user.getCmInfluencerVisit() + 1 : 1);
			} else {
				//Site Master
				user.setLmInfluencerVisit(0);
				user.setCmInfluencerVisit(1);
			}
		} else {
			user.setLmInfluencerVisit(0);
			user.setCmInfluencerVisit(1);
		}
		user.setLastInfluencerVisitDate(new Date());
		influencer.setLastVisitTime(new Date());
	}

	@Override
	public InfluencerVisitData saveInfluencerVisitForm(InfluencerVisitData influencerVisitData) {

		InfluencerVisitData data =  new InfluencerVisitData();
		EyDmsCustomerModel influencer = (EyDmsCustomerModel) userService.getUserForUID(influencerVisitData.getInfluencerId());
		EyDmsUserModel user  = (EyDmsUserModel) userService.getCurrentUser();

		SubAreaMasterModel subArea=null;
		if(territoryManagementService.getTerritoryForCustWithAllBrands(influencer)!=null && !territoryManagementService.getTerritoryForCustWithAllBrands(influencer).isEmpty()){
			subArea = territoryManagementService.getTerritoryForCustWithAllBrands(influencer).get(0);
		}
		DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
		String planDate = dateFormat.format(new Date());
		VisitMasterModel visit=null;
		if(subArea!=null) {
			visit = djpVisitService.createVisitMasterData(null, null, user, subArea.getPk().toString(), planDate);
			visit.setVisitType(TypeOfVisitMaster.INFLUENCER_VISIT);
			visit.setEndVisitTime(new Date());
			visit.setStatus(VisitStatus.COMPLETED);
			modelService.save(visit);
		}

		InfluencerVisitMasterModel influencerVisitMasterModel = createCounterVisitMasterData(visit,influencer,influencerVisitData);
		updateTotalVisitDetails(influencer,user);
		modelService.saveAll(visit,influencerVisitMasterModel,influencer,user);
		if(influencerVisitData.getLeadMaster()!=null){
			boolean result = djpVisitFacade.submitLeadGeneration(influencerVisitData.getLeadMaster(), influencerVisitMasterModel.getPk().toString());
			modelService.refresh(influencerVisitMasterModel);
			if(result) {
				if(influencerVisitMasterModel.getLeads()!=null && !influencerVisitMasterModel.getLeads().isEmpty()) {
					LeadMasterModel leadMasterModel = influencerVisitMasterModel.getLeads().iterator().next();
					leadMasterModel.setApprovedBy(user);
					leadMasterModel.setApprovedDate(new Date());
					leadMasterModel.setStatus(LeadStatus.APPROVED);
					modelService.save(leadMasterModel);
				}
			}
		}

		data.setInfluencerId(influencer.getUid());
		return data;
	}

	public InfluencerVisitMasterModel createCounterVisitMasterData(VisitMasterModel visit,EyDmsCustomerModel influencer, InfluencerVisitData visitData) {

		InfluencerVisitMasterModel model = modelService.create(InfluencerVisitMasterModel.class);
		model.setId(counterVisitIdGenerator.generate().toString());
		model.setSequence(1);
		model.setEyDmsCustomer(influencer);
		model.setCounterType(CounterType.INFLUENCER);
		model.setStartVisitTime(new Date());
		model.setEndVisitTime(new Date());
		model.setAddressSite(visitData.getSiteAddress());
	//	model.setSiteAddress(visitData.getSiteAddress());
		model.setLatitude(visitData.getLatitude());
		model.setLongitude(visitData.getLongitude());

		model.setNoOfSiteRunning(visitData.getNoOfSiteRunning());
		model.setNoOfShreeSiteRunning(visitData.getNoOfShreeSiteRunning());

		influencer.setNoOfSiteRunning(visitData.getNoOfSiteRunning());
		influencer.setNoOfShreeSiteRunning(visitData.getNoOfShreeSiteRunning());

		/*model.setNoOfSiteRunning(influencer.getNoOfSiteRunning());
		model.setNoOfShreeSiteRunning(influencer.getNoOfShreeSiteRunning());*/

		model.setShreeSalesPerBag(visitData.getShreeSalesPerBag());
		model.setMonthlyPotentialInBags(visitData.getMonthlyPotentialInBags());
		model.setSow(visitData.getSow());
		if(visit!=null) {
			LOG.info("Visit added into influencer");
			model.setVisit(visit);
		}
		model.setAnyLeadsGeneratedByInfluencer(visitData.getAnyLeadsGeneratedByInfluencer());
		if(visitData.getBrandsUsedByInfluencer()!=null && !visitData.getBrandsUsedByInfluencer().isEmpty()) {
			List<String> brands = new ArrayList<>(visitData.getBrandsUsedByInfluencer());
			LOG.info("brands:"+brands);
			model.setBrandsUsedByInfluencer(brands);
		}
		model.setRemarks(visitData.getRemarks());
		return model;
	}

  @Override
	public ScheduledMeetData getMeetingCompletionFormDetail(String scheduleMeetId) {
		ScheduledMeetData data=new ScheduledMeetData();
		MeetingScheduleModel meetingScheduleByCode = influencerService.getMeetingScheduleByCode(scheduleMeetId);
		if(meetingScheduleByCode!=null) {
				data = scheduledMeetConverter.convert(meetingScheduleByCode);
				if (data != null) {
					if (meetingScheduleByCode.getMeetingForm() != null) {
						data.setMeetingForm(meetingCompletionFormConverter.convert(meetingScheduleByCode.getMeetingForm()));
					}
				}
		}
		return data;
	}

    @Override
    public InfluencersDetails360WsData getInfluencerDetailsFor360(String influencerCode) {
		 EyDmsCustomerModel influencer= (EyDmsCustomerModel) userService.getUserForUID(influencerCode);
		InfluencersDetails360WsData data=new InfluencersDetails360WsData();
		if( influencer.getLastLiftingDate()!=null) {
			data.setLastLiftingDate(influencer.getLastLiftingDate());
		}
		if(influencer.getLastLiftingQuantity()!=null){
			data.setLastLiftingQuantity((influencer.getLastLiftingQuantity()));
		}
		data.setName(influencer.getName());
		data.setCode(influencer.getUid());
		if(influencer.getEmail()!=null)
			data.setEmail(influencer.getEmail());
		if(influencer.getMobileNumber()!=null)
			data.setContactNumber(influencer.getMobileNumber());
		data.setNirmanMitraCode(influencer.getNirmanMitraCode());
	    data.setLastVisitTime(influencer.getLastVisitTime());
		data.setNetworkType(influencer.getNetworkType());
		data.setPotential(influencer.getCounterPotential());

		LocalDate salesMTDStartDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		LocalDate salesMTDEndDate = LocalDate.now();

		List<EyDmsCustomerModel> influencerList=new ArrayList<>();
		influencerList.add(influencer);

		List<List<Object>> list = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, salesMTDStartDate.toString(), salesMTDEndDate.toString(),null,null);
		Map<String, Double>  currentMonthSale = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
				.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

		//formula is 100* (current  MTD sales - last year MTD sales till same date)/ (last year MTD sales till same date).
		LocalDate salesMTDLastYearStartDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusYears(1);
		LocalDate salesMTDLastYearEndDate = LocalDate.now().minusYears(1);
		List<List<Object>> list1 = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, salesMTDLastYearStartDate.toString(), salesMTDLastYearEndDate.toString(),null,null);
		Map<String, Double>  currentMonthSaleLastYear = list1.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
				.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
		Double currentSales=0.0;
		Double lastYearSales=0.0;
		Double lastMonth=0.0;
		Double lastYearCurrentMonth=0.0;


		if(currentMonthSale!=null && currentMonthSale.get(influencerCode)!=null) {
			 currentSales = currentMonthSale.get(influencerCode);
		}
		if(currentMonthSaleLastYear!=null && currentMonthSaleLastYear.get(influencerCode)!=null) {
			lastYearSales = currentMonthSaleLastYear.get(influencerCode);
		}
		if (currentSales != null && lastYearSales != null && lastYearSales != 0.0) {
			double growth = ((currentSales - lastYearSales) * 100) / lastYearSales;
			data.setSales(currentSales);
			data.setGrowth(growth);
		} else {
			double growth = 0.0;
			data.setGrowth(growth);
			data.setSales(currentSales);
		}

		LocalDate lastMonthSalesMTDStartDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
		LocalDate lastMonthSalesMTDEndDate = LocalDate.now().minusMonths(1);
		List<List<Object>> lastMonthMTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastMonthSalesMTDStartDate.toString(), lastMonthSalesMTDEndDate.toString(),null,null);
		Map<String, Double>  lastMonthSale = lastMonthMTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
				.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

       if(lastMonthSale!=null && lastMonthSale.get(influencerCode)!=null) {
		   data.setLastMonth(lastMonthSale.get(influencerCode));
	   }

		LocalDate lastYearCurrentMonthSalesStartDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusYears(1);
		LocalDate lastYearCurrentMonthSalesEndDate = LocalDate.now().minusYears(1);
		List<List<Object>> lastYearCurrent = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastYearCurrentMonthSalesStartDate.toString(), lastYearCurrentMonthSalesEndDate.toString(),null,null);
		Map<String, Double>  lastYearCurrentMonthSales = lastYearCurrent.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
				.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

		if(lastYearCurrentMonthSales!=null && lastYearCurrentMonthSales.get(influencerCode)!=null) {
			data.setLastYearCurrentMonth(lastYearCurrentMonthSales.get(influencerCode));
		}

		if(influencer.getEyDmsCustomers()!=null && !influencer.getEyDmsCustomers().isEmpty()) {
			List<B2BCustomerModel> siteList = influencer.getEyDmsCustomers().stream().filter(o -> o.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
			if (siteList != null && !siteList.isEmpty()) {
				List<EyDmsSiteData> dataList = eydmsSiteConverter.convertAll(siteList);
				data.setSites(dataList);
			}
		}
		/*if(influencer.getTaggedPartners()!=null && !influencer.getTaggedPartners().isEmpty()) {
			List<B2BCustomerModel> siteList = influencer.getTaggedPartners().stream().filter(o -> o.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
			if (siteList != null && !siteList.isEmpty()) {
				List<EyDmsSiteData> dataList = eydmsSiteConverter.convertAll(siteList);
				data.setSites(dataList);
			}
		}*/
		data.setNoOfShreeSiteRunning(influencer.getNoOfShreeSiteRunning());
		data.setNoOfSiteRunning(influencer.getNoOfSiteRunning());
		Integer visitCountMTD = influencerDao.getVisitCountMTD(influencer, LocalDate.now().getMonth().getValue(),LocalDate.now().getYear());
		data.setVisits(visitCountMTD);

		data.setTotalAvailablePoints(influencer.getAvailablePoints());
		data.setInfluencerCategory(influencer.getInfluencerType().getCode());
		return data;
    }

	@Override
	public InfluencerVisitHistoryListData getInfluencerVisitHistory(String filter) {
		InfluencerVisitHistoryListData listData=new InfluencerVisitHistoryListData();
		List<InfluencerVisitHistoryData> dataList=new ArrayList<>();
		int influencerVisitHistoryCount=0;
		double sumCount=0.0;
		double avgVisit=0.0;
		if(StringUtils.isNotBlank(filter)){
			int totalDays = 0;
			if(filter.equalsIgnoreCase("Daily")){
				int dayCount=1;
				LocalDate days=LocalDate.now();
				LocalDate firstDay=null,nextday=null;
				LocalDate day=LocalDate.now();
				for(int i=1;i<=days.lengthOfMonth();i++){
					firstDay = LocalDate.of(days.getYear(),days.getMonth(),dayCount);
					nextday = LocalDate.of(days.getYear(),days.getMonth(),days.lengthOfMonth()).plusDays(1);
					influencerVisitHistoryCount = influencerService.getInfluencerVisitHistoryCount(firstDay.toString(), nextday.toString());
					sumCount+=influencerVisitHistoryCount;
				}
				firstDay = day.minusDays(6);
				for(int i=1;i<=7;i++){
					InfluencerVisitHistoryData data=new InfluencerVisitHistoryData();
					influencerVisitHistoryCount = influencerService.getInfluencerVisitHistoryCount(firstDay.toString(), nextday.toString());
					data.setCount(influencerVisitHistoryCount);
					//data.setDailyWeekMonthName(days.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat("-").concat(String.valueOf(i)));
					data.setDailyWeekMonthName(String.valueOf(firstDay.getDayOfMonth()).concat("/").concat(String.valueOf(firstDay.getMonthValue())));
					dataList.add(data);
					firstDay=firstDay.plusDays(1);
				}

				avgVisit=sumCount/days.lengthOfMonth();
			}else if(filter.equalsIgnoreCase("Weekly")){
				LocalDate firstDay=null,lastDay=null;
				List<LocalDate> last4WeeksStartingMonday = getLast4WeeksStartingMonday();
				for (LocalDate date : last4WeeksStartingMonday) {
					InfluencerVisitHistoryData data=new InfluencerVisitHistoryData();
					lastDay=date.plusDays(7);
					influencerVisitHistoryCount = influencerService.getInfluencerVisitHistoryCount(date.toString(), lastDay.toString());
					sumCount+=influencerVisitHistoryCount;
					data.setCount(influencerVisitHistoryCount);
					totalDays +=7;
					data.setDailyWeekMonthName(date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).concat("`").concat(String.valueOf(date.getDayOfMonth())));
					SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
					ZoneId zone = ZoneId.systemDefault();
					ZonedDateTime dateTime = date.atStartOfDay(zone);
					data.setStartOfWeek(formatter.format(Date.from(dateTime.toInstant())));
					ZonedDateTime dateTime1 = lastDay.atStartOfDay(zone);
					data.setEndOfWeek(formatter.format(Date.from(dateTime1.toInstant())));
					dataList.add(data);
				}
				avgVisit=sumCount/totalDays;
			}else if(filter.equalsIgnoreCase("Monthly")){
				LocalDate month=LocalDate.now();
				for(int i=1;i<=4;i++){
					LocalDate firstDay = month.with(TemporalAdjusters.firstDayOfMonth());
					LocalDate lastDay = month.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
					InfluencerVisitHistoryData data=new InfluencerVisitHistoryData();
					influencerVisitHistoryCount = influencerService.getInfluencerVisitHistoryCount(firstDay.toString(), lastDay.toString());
					data.setCount(influencerVisitHistoryCount);
					//currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
					data.setDailyWeekMonthName(String.valueOf(firstDay.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).concat("'").concat(DateTimeFormatter.ofPattern("yy").format(firstDay)));
					dataList.add(data);
					sumCount+=influencerVisitHistoryCount;
					totalDays+=month.lengthOfMonth();
					month = month.minusMonths(1);
				}
				avgVisit=sumCount/totalDays;
			}
		}else{
			if(filter.equalsIgnoreCase("Daily")){
				int dayCount=1;
				LocalDate days=LocalDate.now();
				LocalDate firstDay=null,nextday=null;
				LocalDate day=LocalDate.now();
				for(int i=1;i<=days.lengthOfMonth();i++){
					firstDay = LocalDate.of(days.getYear(),days.getMonth(),dayCount);
					nextday = LocalDate.of(days.getYear(),days.getMonth(),days.lengthOfMonth()).plusDays(1);
					influencerVisitHistoryCount = influencerService.getInfluencerVisitHistoryCount(firstDay.toString(), nextday.toString());
					sumCount+=influencerVisitHistoryCount;
				}

				for(int i=1;i<=7;i++){
					firstDay = day.minusDays(6);
					nextday=day.plusDays(1);
					InfluencerVisitHistoryData data=new InfluencerVisitHistoryData();
					influencerVisitHistoryCount = influencerService.getInfluencerVisitHistoryCount(firstDay.toString(), nextday.toString());
					data.setCount(influencerVisitHistoryCount);
					//data.setDailyWeekMonthName(String.valueOf(firstDay.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).concat("-").concat(String.valueOf(i)));
					data.setDailyWeekMonthName(String.valueOf(days.getDayOfMonth()).concat("/").concat(String.valueOf(days.getMonthValue())));
					dataList.add(data);
				}

				avgVisit=sumCount/days.lengthOfMonth();
			}
		}
		listData.setAverageVisit(avgVisit);
		listData.setVisitHistory(dataList);
		return listData;
	}

	public static List<LocalDate> getLast4WeeksStartingMonday() {
		LocalDate today = LocalDate.now();
		DayOfWeek currentDayOfWeek = today.getDayOfWeek();
		// Calculate the number of days to subtract to get to the previous Monday
		int daysToSubtract = (currentDayOfWeek.getValue() + 6) % 7;
		LocalDate startDate = today.minusDays(daysToSubtract);
		List<LocalDate> weekDates = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			weekDates.add(startDate.minusWeeks(i));
		}
		return weekDates;
	}
}
