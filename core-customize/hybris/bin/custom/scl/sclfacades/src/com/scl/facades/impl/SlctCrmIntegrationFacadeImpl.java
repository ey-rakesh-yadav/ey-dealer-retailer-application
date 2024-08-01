package com.scl.facades.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;


import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.services.SalesPlanningService;
import com.scl.core.services.SiteManagementService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.TechnicalAssistanceFacade;
import com.scl.facades.constants.SclFacadesConstants;
import com.scl.facades.data.*;
import com.scl.facades.order.data.IntegrationOrderEntryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.DeliverySlotMasterDao;
import com.scl.core.dao.SlctCrmIntegrationDao;
import com.scl.core.services.SlctCrmIntegrationService;
import com.scl.facades.SlctCrmIntegrationFacade;
import com.scl.facades.order.data.IntegrationOrderData;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
//import com.scl.facades.populators.var;


public class SlctCrmIntegrationFacadeImpl implements SlctCrmIntegrationFacade {
	private static final Logger LOG = Logger.getLogger(SlctCrmIntegrationFacadeImpl.class);

	@Autowired
	SlctCrmIntegrationService slctCrmIntegrationService;
	@Autowired
	TechnicalAssistanceFacade technicalAssistanceFacade;

	@Autowired
	SlctCrmIntegrationDao slctCrmIntegrationDao;

	@Autowired
	CatalogVersionService catalogVersionService;

	@Autowired
	DeliverySlotMasterDao deliverySlotMasterDao;

	@Autowired
	SalesPlanningService salesPlanningService;

	@Autowired
	ProductService productService;

	@Autowired
	ModelService modelService;

	@Autowired
	UserService userService;

	@Autowired
	SclNotificationService sclNotificationService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	Converter<MarketMappingDetailsModel, MarketMappingDetailsData> visitDataConverter;

	@Autowired
	Converter<MarketMappingDetailsModel, MarketMappingDetailsData> reviewLogsVisitDataConverter;

	@Resource
	private Converter<AddressModel, SCLAddressData> sclAddressConverter;

	@Autowired
	private Converter<AddressModel, AddressData> addressConverter;

	@Autowired
	Converter<MeetingCompletionFormModel, MeetingCompletionFormData> meetingCompletionFormConverter;

	@Autowired
	private Converter<MeetingScheduleModel, ScheduledMeetData> scheduledMeetConverter;

	@Autowired
	TechnicalAssistanceFacadeImpl technicalAssistanceFacadeImpl;

	@Autowired
	I18NService i18NService;

	@Autowired
	EnumerationService enumerationService;

	@Autowired
	SiteManagementService siteManagementService;

	@Autowired
	private Converter<SiteTransactionModel, SiteConversionData> slctSiteConversionConverter;

	@Override
	public MarketMappingListData getAllMarketMappingDetails() {
		MarketMappingListData resultOutput = new MarketMappingListData();
		List<MarketMappingDetailsModel> list = slctCrmIntegrationService.getAllMarketMappingDetails();
		List<MarketMappingDetailsData> result = new ArrayList<MarketMappingDetailsData>();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (MarketMappingDetailsModel marketMapping : list) {

			MarketMappingDetailsData data = new MarketMappingDetailsData();
			data.setId(marketMapping.getId());

			if (marketMapping.getBrand() != null)
				data.setBrand(marketMapping.getBrand().getName());

			if (marketMapping.getProduct() != null) {
				data.setProduct(marketMapping.getProduct().getName());
				data.setGrade(marketMapping.getProduct().getGrade());
				data.setPackaging(marketMapping.getProduct().getPackaging());
				data.setProduct(marketMapping.getProduct().getCode());
			}

			data.setWholesalePrice(marketMapping.getWholesalePrice());
			data.setWholeSales(marketMapping.getWholeSales());
			data.setRetailsalePrice(marketMapping.getRetailsalePrice());
			data.setRetailSales(marketMapping.getRetailSales());
			data.setStock(marketMapping.getStock());
			data.setDiscount(marketMapping.getDiscount());
			data.setBilling(marketMapping.getBilling());

			CounterVisitMasterData counterVisitMasterData = new CounterVisitMasterData();

			if (marketMapping.getCounterVisit() != null) {
				counterVisitMasterData.setId(marketMapping.getCounterVisit().getId());
				if (marketMapping.getCounterVisit().getStartVisitTime() != null) {
					data.setCounterVisitStartTime(dateFormat.format(marketMapping.getCounterVisit().getStartVisitTime()));
				}
				if (marketMapping.getCounterVisit().getVisit() != null) {
					if (marketMapping.getCounterVisit().getVisit().getUser() != null) {
						data.setEmployeeCodeSO(marketMapping.getCounterVisit().getVisit().getUser().getEmployeeCode());
						data.setSoName(marketMapping.getCounterVisit().getVisit().getUser().getName());
						data.setSoUid(marketMapping.getCounterVisit().getVisit().getUser().getUid());
						if (marketMapping.getCounterVisit().getVisit().getUser().getDefaultB2BUnit() != null)
							data.setSoBrand(marketMapping.getCounterVisit().getVisit().getUser().getDefaultB2BUnit().getUid());
						if (marketMapping.getCounterVisit().getVisit().getUser().getUserType() != null) {
							data.setUserType(marketMapping.getCounterVisit().getVisit().getUser().getUserType().getCode());
						}
					}


//					if (marketMapping.getCounterVisit().getVisit().getSubAreaMaster() != null) {
//						data.setTaluka(marketMapping.getCounterVisit().getVisit().getSubAreaMaster().getTaluka());
//						data.setDistrict(marketMapping.getCounterVisit().getVisit().getSubAreaMaster().getDistrict());
//					}
					if (marketMapping.getCounterVisit().getSclCustomer() != null) {
						data.setErpCustomerNo(marketMapping.getCounterVisit().getSclCustomer().getCustomerNo());
						data.setCustomerName(marketMapping.getCounterVisit().getSclCustomer().getName());
						data.setCrmCustomerCode(marketMapping.getCounterVisit().getSclCustomer().getUid());
						data.setLatitude(marketMapping.getCounterVisit().getSclCustomer().getLatitude());
						data.setLongitude(marketMapping.getCounterVisit().getSclCustomer().getLongitude());
						data.setMobileNumber(marketMapping.getCounterVisit().getSclCustomer().getMobileNumber());
						if (marketMapping.getCounterVisit().getSclCustomer().getAddresses() != null) {
							List<AddressModel> primaryAddressList = marketMapping.getCounterVisit().getSclCustomer().getAddresses().stream().toList();
							primaryAddressList = primaryAddressList.stream().filter(address -> ((address.getIsPrimaryAddress() && address.getBillingAddress()) || address.getBillingAddress())).collect(Collectors.toList());
							if (primaryAddressList != null && !primaryAddressList.isEmpty()) {
								AddressModel primaryBillingAddress = primaryAddressList.get(0);
								data.setAddressLine1(primaryBillingAddress.getLine1());
								data.setAddressLine2(primaryBillingAddress.getLine2());
								data.setCity(primaryBillingAddress.getErpCity());
							}
						}

						CustomerSubAreaMappingModel customerSubAreaMappingModel = slctCrmIntegrationDao.getCustomerSubAreaMapByCustomer(marketMapping.getCounterVisit().getSclCustomer());
						if (customerSubAreaMappingModel != null) {
							data.setTaluka(customerSubAreaMappingModel.getSubArea());
							data.setDistrict(customerSubAreaMappingModel.getDistrict());
							data.setState(customerSubAreaMappingModel.getState());
						}
					}


				}
			}
			data.setCounterVisitId(counterVisitMasterData);
			result.add(data);
		}
		resultOutput.setMarketMappingDetailsList(result);
		return resultOutput;
	}

	@Override
	public CRMVisitListData getAllVisit() {
		CRMVisitListData resultOutput = new CRMVisitListData();
		List<VisitMasterModel> list = slctCrmIntegrationService.getAllVisit();
		List<CRMVisitData> result = new ArrayList<CRMVisitData>();
		for (VisitMasterModel visit : list) {
			CRMVisitData data = new CRMVisitData();
			data.setId(visit.getId());
			if (visit.getRoute() != null)
				data.setRoute(visit.getRoute().getRouteId());
			if (visit.getObjective() != null) {
				data.setObjectiveId(visit.getObjective().getObjectiveId());
				data.setObjectiveName(visit.getObjective().getObjectiveName());
			}

			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			if (visit.getStartVisitTime() != null)
				data.setStartVisitTime(dateFormat.format(visit.getStartVisitTime()));

			if (visit.getEndVisitTime() != null)
				data.setEndVisitTime(dateFormat.format(visit.getEndVisitTime()));

			DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
			if (visit.getVisitPlannedDate() != null) {
				data.setVisitPlannedDate(dateFormat1.format(visit.getVisitPlannedDate()));
			}
			if (visit.getStatus() != null) {
				data.setStatus(visit.getStatus().getCode());
			}
			if (visit.getApprovalStatus() != null) {
				data.setApprovalStatus(visit.getApprovalStatus().getCode());
			}
			//New Territory Change
			if (visit.getSubAreaMaster() != null) {
				data.setSubArea(visit.getSubAreaMaster().getTaluka());
				data.setDistrict(visit.getSubAreaMaster().getDistrict());
			}
			//data.setState(visit.getState());

			if (visit.getUser() != null) {
				data.setSoEmail(visit.getUser().getUid());
				data.setSoEmployeeCode(visit.getUser().getEmployeeCode());
				data.setSoName(visit.getUser().getName());
				data.setUid(visit.getUser().getUid());
				data.setSoBrand(visit.getUser().getDefaultB2BUnit().getUid());
				if (visit.getUser().getUserType() != null) {
					data.setUserType(visit.getUser().getUserType().getCode());
				}
			}

			data.setRouteDeviationComment(visit.getRouteDeviationComment());
			data.setRouteDeviationReason(visit.getRouteDeviationReason());
			data.setObjectiveDeviationComment(visit.getObjectiveDeviationComment());
			data.setObjectiveDeviationReason(visit.getObjectiveDeviationReason());

			result.add(data);
		}
		resultOutput.setVisitList(result);
		return resultOutput;
	}


	@Override
	public CRMCounterVisitListData getAllCounterVisit() {
		CRMCounterVisitListData resultOutput = new CRMCounterVisitListData();
		List<CounterVisitMasterModel> list = slctCrmIntegrationService.getAllCounterVisit();
		List<CRMCounterVisitData> result = new ArrayList<CRMCounterVisitData>();
		for (CounterVisitMasterModel counterVisit : list) {
			CRMCounterVisitData counter = new CRMCounterVisitData();

			counter.setId(counterVisit.getId());
			if (counterVisit.getSclCustomer() != null) {
				counter.setCustomerNo(counterVisit.getSclCustomer().getCustomerNo());
				counter.setCustomerName(counterVisit.getSclCustomer().getName());
				counter.setCrmCustomerCode(counterVisit.getSclCustomer().getUid());
				counter.setLatitude(counterVisit.getSclCustomer().getLatitude());
				counter.setLongitude(counterVisit.getSclCustomer().getLongitude());
				counter.setMobileNumber(counterVisit.getSclCustomer().getMobileNumber());
				if (counterVisit.getSclCustomer().getAddresses() != null) {
					List<AddressModel> primaryAddressList = counterVisit.getSclCustomer().getAddresses().stream().toList();
					primaryAddressList = primaryAddressList.stream().filter(address -> ((address.getIsPrimaryAddress() && address.getBillingAddress()) || address.getBillingAddress())).collect(Collectors.toList());
					if (primaryAddressList != null && !primaryAddressList.isEmpty()) {
						AddressModel primaryBillingAddress = primaryAddressList.get(0);
						counter.setAddressLine1(primaryBillingAddress.getLine1());
						counter.setAddressLine2(primaryBillingAddress.getLine2());
						counter.setCity(primaryBillingAddress.getErpCity());
					}
				}
			}
			if (counterVisit.getCounterType() != null) {
				counter.setCounterType(counterVisit.getCounterType().getCode());
			}
			counter.setIsAdoc(counterVisit.getIsAdHoc());
			counter.setDeviationComment(counterVisit.getDeviationComment());
			counter.setDeviationReason(counterVisit.getDeviationReason());
			counter.setSystemRecommended(counterVisit.getCounterScore() != null ? true : false);

			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			if (counterVisit.getStartVisitTime() != null)
				counter.setStartVisitTime(dateFormat.format(counterVisit.getStartVisitTime()));

			if (counterVisit.getEndVisitTime() != null)
				counter.setEndVisitTime(dateFormat.format(counterVisit.getEndVisitTime()));

			CRMVisitData visit = new CRMVisitData();
			if (counterVisit.getVisit() != null) {
				visit.setId(counterVisit.getVisit().getId());
			}
			if (counterVisit.getCounterPotential() != null) {
				counter.setCounterPotential(counterVisit.getCounterPotential());
			}
			if (counterVisit.getTotalSale() != null) {
				counter.setTotalSale(counterVisit.getTotalSale());
			}
			if (counterVisit.getWholeSale() != null) {
				counter.setWholeSale(counterVisit.getWholeSale());
			}
			if (counterVisit.getCounterSale() != null) {
				counter.setCounterSale(counterVisit.getCounterSale());
			}
			counter.setIsShreeCounter(counterVisit.getIsShreeCounter());
			counter.setIsBangurCounter(counterVisit.getIsBangurCounter());
			counter.setIsRockstrongCounter(counterVisit.getIsRockstrongCounter());
			counter.setIsDealerFlag(counterVisit.getIsDealerFlag());
			if (counterVisit.getCounterShare() != null) {
				counter.setCounterShare(counterVisit.getCounterShare());
			}
			if (counterVisit.getFlaggedBy() != null) {
				counter.setFlaggedBy(counterVisit.getFlaggedBy().getEmployeeCode());
			}
			counter.setOrderGenerated(counterVisit.getOrderGenerated());
			counter.setRemarkForFlag(counterVisit.getRemarkForFlag());
			counter.setRemarkForUnflag(counterVisit.getRemarkForUnflag());
			if (counterVisit.getUnflagTime() != null) {
				counter.setUnflagTime(dateFormat.format(counterVisit.getUnflagTime()));
			}
			counter.setNetworkType(counterVisit.getNetworkType());

			if (counterVisit.getVisit() != null && counterVisit.getVisit().getUser() != null && counterVisit.getVisit().getUser().getUserType() != null) {
				counter.setUserType(counterVisit.getVisit().getUser().getUserType().getCode());
			}

			counter.setJourneyVisitId(visit);
			result.add(counter);

		}
		resultOutput.setCrmCounterVisitList(result);
		return resultOutput;
	}

	@Override
	public SlctCrmInfluencerListData getAllInfluencerDetails() {
		SlctCrmInfluencerListData resultOutput = new SlctCrmInfluencerListData();
		List<SlctCrmInfluencerData> result = new ArrayList<>();
		List<SclCustomerModel> influencerList = slctCrmIntegrationService.getAllInfluencerDetails();
		for (SclCustomerModel influencer : influencerList) {
			SlctCrmInfluencerData influencerData = new SlctCrmInfluencerData();

			influencerData.setCrmCustomerCode(influencer.getUid());
			influencerData.setName(influencer.getName());
			influencerData.setInfluencerCreationDate(influencer.getCreationtime());
			if (influencer.getInfluencerType() != null) {
				influencerData.setInfluencerType(influencer.getInfluencerType().getCode());
			}
			influencerData.setMobileNumber(influencer.getMobileNumber());
			if (influencer.getDefaultB2BUnit() != null) {
				if (influencer.getDefaultB2BUnit().getUid().equals("SclOtherUnit")) {
					influencerData.setBrand("NON SCL");
					influencerData.setIsSclInfluencer(false);
				} else {
					influencerData.setBrand("SHREE");
//					if (influencer.getDefaultB2BUnit().getUid().equals("SclShreeUnit")) {

//					} else if (influencer.getDefaultB2BUnit().getUid().equals("SclBangurUnit")) {
//						influencerData.setBrand("BANGUR");
//					} else if (influencer.getDefaultB2BUnit().getUid().equals("SclRockstrongUnit")) {
//						influencerData.setBrand("ROCKSTRONG");
//					}
					influencerData.setIsSclInfluencer(true);
				}
			}

			if (influencer.getAddresses() != null) {
				List<AddressModel> primaryBillingAddressList = influencer.getAddresses().stream().toList();
				primaryBillingAddressList = primaryBillingAddressList.stream().filter(address -> (address.getBillingAddress())).collect(Collectors.toList());
				if (primaryBillingAddressList != null && !primaryBillingAddressList.isEmpty()) {
					AddressModel primaryBillingAddress = primaryBillingAddressList.get(0);
					influencerData.setAddressLine1(primaryBillingAddress.getLine1());
					influencerData.setAddressLine2(primaryBillingAddress.getLine2());
					influencerData.setState(primaryBillingAddress.getState());
					influencerData.setDistrict(primaryBillingAddress.getDistrict());
					influencerData.setCity(primaryBillingAddress.getErpCity());
					influencerData.setTaluka(primaryBillingAddress.getTaluka());
				}
			}
			influencerData.setCounterPotential(influencer.getCounterPotential());
			if (influencer.getOnboardingPartner() != null) {
				influencerData.setOnboardingPartnerName(influencer.getOnboardingPartner().getName());
				influencerData.setOnboardingPartnerUid(influencer.getOnboardingPartner().getUid());
			}

			influencerData.setAadharNo(influencer.getAadharNo());
			influencerData.setPanCardNo(influencer.getPanCard());
			influencerData.setGstIn(influencer.getGstIN());
			influencerData.setBankAccountNo(influencer.getBankAccountNo());
			influencerData.setIfscCode(influencer.getIfscCode());
			influencerData.setApplicationNo(influencer.getApplicationNo());
			influencerData.setApplicationDate(influencer.getApplicationDate());


//			NominationData nomineeData = new NominationData();

//			if (influencer.getNominee() != null) {
//				nomineeData.setNomineeID(influencer.getNominee().getNomineeID());
//				nomineeData.setName(influencer.getNominee().getName());
//				nomineeData.setFathersName(influencer.getNominee().getFathersName());
//				nomineeData.setLine1(influencer.getNominee().getAddressLine1());
//				nomineeData.setLine2(influencer.getNominee().getAddressLine2());
//				nomineeData.setState(influencer.getNominee().getState());
//				nomineeData.setDistrict(influencer.getNominee().getDistrict());
//				nomineeData.setCity(influencer.getNominee().getCity());
//				nomineeData.setTaluka(influencer.getNominee().getTaluka());
//				nomineeData.setPanCard(influencer.getNominee().getPanCard());
//				nomineeData.setAadharCard(influencer.getNominee().getAadharCard());
//
////				List<UIDData> uids = new ArrayList<UIDData>();
////				for(UidMediaModel uidMedia: influencer.getNominee().getUidMedias()) {
////					UIDData uid = new UIDData();
////
////					uid.setUidNumber(uidMedia.getUidNumber());
////					uids.add(uid);
////				}
////				nomineeData.setUids(uids);
//			}
//			influencerData.setNominee(nomineeData);

			result.add(influencerData);
		}

		resultOutput.setInfluencerDetails(result);
		return resultOutput;
	}

	@Override
	public RetailerSalesInfoListData getAllRetailerSalesInfo() {
		RetailerSalesInfoListData resultOutput = new RetailerSalesInfoListData();
		List<List<Object>> retailerSalesList = slctCrmIntegrationService.getAllRetailerSalesInfo();
		List<RetailerSalesInfoData> result = new ArrayList<RetailerSalesInfoData>();

		retailerSalesList.forEach(retailer -> {
			RetailerSalesInfoData retailerData = new RetailerSalesInfoData();
			if (retailer.get(0) != null) {
				retailerData.setCrmOrderNo((String) retailer.get(0));
			}
			if (retailer.get(1) != null) {
				retailerData.setErpOrderNo((String) retailer.get(1));
			}
			if (retailer.get(2) != null) {
				retailerData.setErpDealerNo((String) retailer.get(2));
			}
			if (retailer.get(3) != null) {
				retailerData.setCrmRetailerId((String) retailer.get(3));
			}
			if (retailer.get(4) != null) {
				retailerData.setErpRetailerNo((String) retailer.get(4));
			}

			result.add(retailerData);
		});
		resultOutput.setRetailerSalesInfoData(result);

		return resultOutput;
	}

	@Override
	public boolean deleteLPSourceMasterData(LPSourceMasterListData lpSourceMasterListData) {
		if (lpSourceMasterListData != null) {

				BaseSiteModel brand = baseSiteService.getBaseSiteForUID("scl");
				DeliveryModeModel deliveryMode = null;
				DeliveryModeModel roadDeliveryModel = slctCrmIntegrationService.getTheDeliveryMode("ROAD");
				DeliveryModeModel railDeliveryModel = slctCrmIntegrationService.getTheDeliveryMode("RAIL");
				List<LPSourceMasterData> lpSourceMaster = lpSourceMasterListData.getLpSourceMaster();
				for (LPSourceMasterData lpSource : lpSourceMaster) {
					try{
					if (!(lpSource.getBrand().equals("scl"))) {
						brand = null;
					}
					
					if (lpSource.getDeliveryMode().equals("ROAD")) {
						deliveryMode = roadDeliveryModel;
					} else if (lpSource.getDeliveryMode().equals("RAIL")) {
						deliveryMode = railDeliveryModel;
					}
					 
					List<DestinationSourceMasterModel> destinationSourceMaster = slctCrmIntegrationService.getLpSourceMasterList(brand, deliveryMode, lpSource.getMasterProductCode(), lpSource.getIncoTerm(), lpSource.getDestCityId());
					if (destinationSourceMaster != null && !destinationSourceMaster.isEmpty()) {
						modelService.removeAll(destinationSourceMaster);
					}
					}
					catch(RuntimeException e){
						LOG.info("deleteLPSourceMaster: exception occured  for MasterProductCode: "+ lpSource.getMasterProductCode() + " IncoTerm"+ lpSource.getIncoTerm() + " eMessage: "+ e.getMessage());
					}
				}

			return true;
		}
		return false;
	}

	@Override
	public boolean insertUpdateOutstandingDetails(SlctCrmOutstandingDetailsListData slctCrmOutstandingDetailsListData) {

		List<SlctCrmOutstandingDetailsData> outstandingDetails = slctCrmOutstandingDetailsListData.getSlctCrmOutstandingDetails();
		List<CreditAndOutstandingModel> modelList = new ArrayList<CreditAndOutstandingModel>();
		Map<String, Boolean> creditLimitMap = new HashMap<>();
		Map<String, Boolean> securityDepositMap = new HashMap<>();
		Boolean creditLimitFlag = false;
		Boolean securityDepositFlag = false;
		for (SlctCrmOutstandingDetailsData outstanding : outstandingDetails) {
			try {
				CreditAndOutstandingModel creditAndOutstanding = slctCrmIntegrationService.getCrmOutstandingDetails(outstanding.getCustomerCode());
				if (Objects.isNull(creditAndOutstanding)) {
					creditAndOutstanding = modelService.create(CreditAndOutstandingModel.class);
				}

				if (outstanding.getUpdateType().equals("B") || outstanding.getUpdateType().equals("b")) {
					//Initializing the buckets
					Double bucket1 = getBucketValue(outstanding.getBucket1());
					Double bucket2 = getBucketValue(outstanding.getBucket2());
					Double bucket3 = getBucketValue(outstanding.getBucket3());
					Double bucket4 = getBucketValue(outstanding.getBucket4());
					Double bucket5 = getBucketValue(outstanding.getBucket5());
					Double bucket6 = getBucketValue(outstanding.getBucket6());
					Double bucket7 = getBucketValue(outstanding.getBucket7());
					Double bucket8 = getBucketValue(outstanding.getBucket8());
					Double bucket9 = getBucketValue(outstanding.getBucket9());
					Double bucket10 = getBucketValue(outstanding.getBucket10());

					LOG.info(String.format("Bucket 8 value before summation : %g, Bucket 9 :%g and Bucket 10 :%g for Customer:%s",bucket8, bucket9, bucket10,outstanding.getCustomerCode()));
					//Summing Buckets 8, 9 and 10 and assigning it to the Bucket 8
					bucket8 += bucket9 + bucket10;
					LOG.info(String.format("Bucket 8 value after summation : %g for customer:%s",bucket8, outstanding.getCustomerCode()));

					creditAndOutstanding.setBucket1(bucket1);
					creditAndOutstanding.setBucket2(bucket2);
					creditAndOutstanding.setBucket3(bucket3);
					creditAndOutstanding.setBucket4(bucket4);
					creditAndOutstanding.setBucket5(bucket5);
					creditAndOutstanding.setBucket6(bucket6);
					creditAndOutstanding.setBucket7(bucket7);
					creditAndOutstanding.setBucket8(bucket8);

					//Explicitly Setting the buckets 9 and 10 to zero
					creditAndOutstanding.setBucket9(0.0);
					creditAndOutstanding.setBucket10(0.0);
					creditAndOutstanding.setCustomerCode(outstanding.getCustomerCode());

					//Total Outstanding is equal to the summation of the 8 Buckets
					Double totalOutstanding = bucket1 + bucket2 + bucket3 + bucket4 + bucket5 + bucket6 + bucket7 + bucket8;
					creditAndOutstanding.setTotalOutstanding(totalOutstanding);
					creditAndOutstanding.setNetOutstanding(totalOutstanding);
				}
				if (outstanding.getUpdateType().equals("O") || outstanding.getUpdateType().equals("o")) {
					creditAndOutstanding.setCustomerCode(outstanding.getCustomerCode());
					creditAndOutstanding.setDailyAverageSales(outstanding.getDailyAverageSales());
					creditAndOutstanding.setDso(outstanding.getDso());
					creditAndOutstanding.setLastUpdatedDate(outstanding.getLastUpdateDate());

					//Commenting out Security Deposit, Credit Limit because these fields will be populated from S4 integration
/*					if (creditAndOutstanding.getCreditLimit() != null && creditAndOutstanding.getCreditLimit() != outstanding.getCreditLimit()) {
						creditLimitFlag = true;
					}
					creditLimitMap.put(creditAndOutstanding.getCustomerCode(), creditLimitFlag);
					creditAndOutstanding.setCreditLimit(outstanding.getCreditLimit());

					if (creditAndOutstanding.getSecurityDeposit() != null && creditAndOutstanding.getSecurityDeposit() != outstanding.getSecurityDeposit()) {
						securityDepositFlag = true;
					}
					securityDepositMap.put(creditAndOutstanding.getCustomerCode(), securityDepositFlag);
					creditAndOutstanding.setSecurityDeposit(outstanding.getSecurityDeposit());
					creditAndOutstanding.setTotalOutstanding(outstanding.getTotalOutstanding());
					creditAndOutstanding.setNetOutstanding(outstanding.getTotalOutstanding()); */
					this.insertOutstandingHistoryDetails(outstanding);
				}
				modelList.add(creditAndOutstanding);
			}
			catch (RuntimeException e) {
				LOG.error(String.format("Exception Occured in the CreditAndOutstanding integration. CreditAndOutstanding Unique field is :%s, Update Type : %s and Error Message : %s",outstanding.getCustomerCode(),outstanding.getUpdateType(),e.getMessage()));
			}

		}
		modelService.saveAll(modelList);
//		try {
//			for (Map.Entry<String, Boolean> entry : creditLimitMap.entrySet()) {
//				String key = entry.getKey();
//				Boolean value = entry.getValue();
//				if (value != null && value) {
//					if (key != null) {
////						SclCustomerModel model = (SclCustomerModel) userService.getUserForUID(key);
//						SclCustomerModel model = slctCrmIntegrationDao.getCustomerByCustNo(key);
//						if (model != null) {
//							LOG.info("Sending credit limit update notification");
//							NotificationCategory category = NotificationCategory.CREDIT_LIMIT_UPDATE;
//
//							StringBuilder builder = new StringBuilder();
//							builder.append(" Credit Limit has been updated, Current credit limit: " + model.getCreditLimit() + " ,Security deposit amount: " + model.getSecurityDepositAmount());
//							builder.append(" ,Dealer name: " + model.getName() + " " + model.getCustomerNo() + " Updated on: " + new Date());
//							String body = builder.toString();
//
//							StringBuilder builder1 = new StringBuilder();
//							builder1.append("Credit limit updated ");
//							String subject = builder1.toString();
//
//
//							sclNotificationService.submitLimitNotification(model, model, body, subject, category);
//							SclUserModel so = territoryManagementService.getSOforCustomer(model);
//							sclNotificationService.submitLimitNotification(model, so, body, subject, category);
//
//							LOG.info("Sent credit limit update notification");
//						}
//					}
//
//				}
//			}
//		} catch (Exception e) {
//			LOG.error("Error while sending credit limit update notification");
//		}
//		try {
//			for (Map.Entry<String, Boolean> entry : securityDepositMap.entrySet()) {
//				String key = entry.getKey();
//				Boolean value = entry.getValue();
//				if (value != null && value) {
//					if (key != null) {
//						LOG.info("Sending Security deposit update notification");
//						SclCustomerModel model = (SclCustomerModel) userService.getUserForUID(key);
//						LOG.info("Sending credit limit update notification");
//						NotificationCategory category = NotificationCategory.SECURITY_DEPOSIT_UPDATE;
//
//						StringBuilder builder = new StringBuilder();
//						builder.append(" Security deposit has been updated, Dealer name: " + model.getName() + " " + model.getCustomerNo());
//						builder.append(" ,Security deposit amount: " + model.getSecurityDepositAmount() + " Updated on: " + new Date());
//						String body = builder.toString();
//
//						StringBuilder builder1 = new StringBuilder();
//						builder1.append("Credit limit updated ");
//						String subject = builder1.toString();
//
//
//						sclNotificationService.submitLimitNotification(model, model, body, subject, category);
//						SclUserModel so = territoryManagementService.getSOforCustomer(model);
//						sclNotificationService.submitLimitNotification(model, so, body, subject, category);
//
//						LOG.info("Sent security deposit update notification");
//					}
//				}
//			}
//		} catch (Exception e) {
//			LOG.error("Error while sending security deposit update notification");
//		}
		return true;
	}

	@Override
	public void insertOutstandingHistoryDetails(SlctCrmOutstandingDetailsData slctCrmOutstandingDetailsData) {
		OutstandingHistoryModel outstandingHistoryModel = slctCrmIntegrationDao.getOutstandingHistory(slctCrmOutstandingDetailsData.getCustomerCode());
		if (Objects.isNull(outstandingHistoryModel)) {
			outstandingHistoryModel = modelService.create(OutstandingHistoryModel.class);
		}
		outstandingHistoryModel.setCustomerCode(slctCrmOutstandingDetailsData.getCustomerCode());
		outstandingHistoryModel.setDailyAverageSales(slctCrmOutstandingDetailsData.getDailyAverageSales());
		outstandingHistoryModel.setOutstandingAmount(slctCrmOutstandingDetailsData.getTotalOutstanding());
		outstandingHistoryModel.setUpdatedDate(new Date());
		modelService.save(outstandingHistoryModel);
	}

	@Override
	public boolean updateRunMasterDetails(SlctRunMasterListData slctRunMasterListData) {
		return slctCrmIntegrationService.updateRunMasterDetails(slctRunMasterListData);
	}

	public boolean updateCounterScoreDetails(SlctCounterScoreListData slctCounterScoreListData) {
		return slctCrmIntegrationService.updateCounterScoreDetails(slctCounterScoreListData);
	}

	public boolean updateRouteScoreDetails(SlctRouteScoreListData slctRouteScoreListData) {
		return slctCrmIntegrationService.updateRouteScoreDetails(slctRouteScoreListData);
	}

	@Override
	public boolean insertNcrDetails(SlctNcrListData slctNcrListData) {
		return slctCrmIntegrationService.insertNcrDetails(slctNcrListData);
	}

	@Override
	public boolean insertMitraTransactionDetails(SlctMitraTransactionListData slctMitraTransactionListData) {
		return slctCrmIntegrationService.insertMitraTransactionDetails(slctMitraTransactionListData);
	}

	@Override
	public boolean insertUpdateMitraMasterDetails(SlctMitraMasterListData slctMitraMasterListData) {
		return slctCrmIntegrationService.insertUpdateMitraMasterDetails(slctMitraMasterListData);
	}

	@Override
	public slctOrderLineSchedulerDetailsListData getOrderLineSchedulerDetails() {
		slctOrderLineSchedulerDetailsListData resultOutput = new slctOrderLineSchedulerDetailsListData();
		List<slctOrderLineSchedulerDetailsData> result = new ArrayList<slctOrderLineSchedulerDetailsData>();
		List<OrderEntryModel> orderEntriesList = slctCrmIntegrationService.getOrderLineScheduleDetails();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<DeliverySlotMasterModel> deliverySlots = deliverySlotMasterDao.findAll();
		for (OrderEntryModel orderLineModel : orderEntriesList) {
			slctOrderLineSchedulerDetailsData orderLineData = new slctOrderLineSchedulerDetailsData();

			orderLineData.setCrmLineNumber(orderLineModel.getEntryNumber());
			orderLineData.setErpLineNumber(orderLineModel.getErpLineItemId());
			orderLineData.setTruckNo(orderLineModel.getTruckNo());
			orderLineData.setDriverContactNo(orderLineModel.getDriverContactNo());
			if (orderLineModel.getExpectedDeliveryDate() != null) {
				orderLineData.setExpectedDeliveryDate(dateFormat.format(orderLineModel.getExpectedDeliveryDate()));
			}
			if (orderLineModel.getExpectedSlot() != null) {
//				String startTime = deliverySlots.stream().filter(slot -> slot.getSlot().getCode().equals(orderLineModel.getExpectedDeliveryslot().getCode())).findAny().get().getStart();
				orderLineData.setExpectedDeliverySlot(orderLineModel.getExpectedSlot().getStart());
			}

			if (orderLineModel.getOrder() != null) {
				orderLineData.setCrmOrderNumber(orderLineModel.getOrder().getCode());
				orderLineData.setErpOrderNumber(orderLineModel.getOrder().getErpOrderNumber());
				if (orderLineModel.getRetailer() != null) {
					orderLineData.setCrmRetailerCode(orderLineModel.getRetailer().getCustomerNo());
				}
				if (orderLineModel.getOrder().getUser() != null) {
					orderLineData.setErpDealerCode(((SclCustomerModel) orderLineModel.getOrder().getUser()).getCustomerNo());
					if (((SclCustomerModel) orderLineModel.getOrder().getUser()).getDealerCategory() != null && !((SclCustomerModel) orderLineModel.getOrder().getUser()).getDealerCategory().getCode().isEmpty()) {
						orderLineData.setDealerCategory(((SclCustomerModel) orderLineModel.getOrder().getUser()).getDealerCategory().getCode());
					}
				}
				orderLineData.setEpodCompleted(orderLineModel.getEpodCompleted());
				if (orderLineModel.getDeliveryAddress() != null) {
					orderLineData.setErpCity(orderLineModel.getDeliveryAddress().getErpCity());
				}
				orderLineData.setCreditLimitBreached(orderLineModel.getOrder().getCreditLimitBreached());
				orderLineData.setIsDealerProvideOwnTransport(orderLineModel.getOrder().getIsDealerProvideOwnTransport());

			}

			result.add(orderLineData);
		}
		resultOutput.setOrderLineSchedulerDetails(result);
		return resultOutput;
	}

	@Override
	public boolean updateOrderFromErp(IntegrationOrderData order) {
		return slctCrmIntegrationService.updateOrderFromSlct(order);
	}

	@Override
	public boolean updateDealerCategorization(DealerCategorizationListData dealerCategorizationListData) {
		return slctCrmIntegrationService.updateDealerCategorization(dealerCategorizationListData);
	}

	@Override
	public SlctCrmCustomerListData getAllSlctCrmCustomerDetails() {
		SlctCrmCustomerListData resultOutput = new SlctCrmCustomerListData();
		List<List<Object>> customersList = slctCrmIntegrationService.getSlctCrmCustomerDetails();
		List<SlctCrmCustomerData> result = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		for (List<Object> customer : customersList) {
			SlctCrmCustomerData customerData = new SlctCrmCustomerData();
			if (customer.get(0) != null) {
				SclCustomerModel customerModel = (SclCustomerModel) customer.get(0);
				if (customerModel.getUid() == null || customerModel.getUid().isEmpty()) {
					continue;
				}
				if (customerModel.getCreationtime() != null) {
					customerData.setCreatedOn(dateFormat.format(customerModel.getCreationtime()));
				}
				customerData.setCounterName(customerModel.getName());
				customerData.setLatitude(customerModel.getLatitude());
				customerData.setLongitude(customerModel.getLongitude());
				customerData.setTotalRetailsale(customerModel.getRetailSale());
				customerData.setTotalWholesale(customerModel.getWholeSale());
				customerData.setCounterId(customerModel.getUid());
				customerData.setErpCustomerNumber(customerModel.getCustomerNo());
//				if(customerModel.getAddresses() != null) {
//					var addressOptional= customerModel.getAddresses().stream().findFirst();
//					if (addressOptional.isPresent()) {
//						customerData.setAddressBlock(addressOptional.get().getLine1());
//					}
//				}
			}

			if (customer.get(1) != null) {
				CustomerSubAreaMappingModel csamModel = (CustomerSubAreaMappingModel) customer.get(1);

				if (csamModel.getSubAreaMaster() == null || csamModel.getState() == null || csamModel.getState().isEmpty() || csamModel.getBrand() == null || csamModel.getCounterType() == null || csamModel.getCounterType().isEmpty()) {
					continue;
				} else {
					customerData.setState(csamModel.getState());
					customerData.setDistrcit(csamModel.getSubAreaMaster().getDistrict());
					customerData.setTaluka(csamModel.getSubAreaMaster().getTaluka());
					if(csamModel.getBrand().getUid().equalsIgnoreCase("scl")) {
						customerData.setBrand("SCL");
					}
//					if (csamModel.getBrand().getUid().equals("102")) {
//						customerData.setBrand("SHREE");
//					}
//					if (csamModel.getBrand().getUid().equals("103")) {
//						customerData.setBrand("BANGUR");
//					}
//					if (csamModel.getBrand().getUid().equals("104")) {
//						customerData.setBrand("ROCKSTRONG");
//					}
					if (csamModel.getCounterType() != null && !csamModel.getCounterType().isEmpty()) {
						if (csamModel.getCounterType().toUpperCase().equals("DEALER")) {
							customerData.setCounterType("D");
						} else if (csamModel.getCounterType().toUpperCase().equals("RETAILER")) {
							customerData.setCounterType("R");
						} else {
							continue;
						}
					}
				}

			}

			if (customer.get(2) != null) {
				String route = (String) customer.get(2);
				if (route == null || route.isEmpty()) {
					continue;
				}
				customerData.setRoute(route);
			}
			result.add(customerData);

		}
		;

		resultOutput.setSclCustomerMarketMapping(result);
		return resultOutput;
	}

	@Override
	public boolean insertProspectiveNetworksList(SlctProspectiveNetworksListData prospectiveNetworksListData) {
		return slctCrmIntegrationService.insertProspectiveNetworksList(prospectiveNetworksListData);
	}

	@Override
	public boolean insertDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException {
		return slctCrmIntegrationService.insertDealerLedgerDetails(dealerLedgerListData);
	}

	@Override
	public boolean insertUpdateDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException {
		return slctCrmIntegrationService.insertUpdateDealerLedgerDetails(dealerLedgerListData);
	}

	@Override
	public boolean deleteOldIsoMasterData() {
		return slctCrmIntegrationService.deleteOldIsoMasterData();
	}

	@Override
	public boolean insertIsoMasterDetails(SlctISOMasterListData slctISOMasterListData) throws ParseException {
		return slctCrmIntegrationService.insertIsoMasterDetails(slctISOMasterListData);
	}

	@Override
	public NewOrderReponseData createOrderFromErp(IntegrationOrderData integrationOrderData) {
		NewOrderReponseData data = new NewOrderReponseData();
		OrderModel order = slctCrmIntegrationService.createOrderFromErp(integrationOrderData);
		data.setCode(order.getCode());
		data.setErpOrderNo(order.getErpOrderNumber());
		data.setErpOrderType(order.getErpOrderType());
		return data;
	}

	@Override
	public boolean insertEtaDetails(SlctEtaListData slctEtaListData) {
		return slctCrmIntegrationService.insertEtaDetails(slctEtaListData);
	}

	@Override
	public boolean insertUpdateCustomerMasterDetails(SlctCrmIntegrationCustomerMasterListData slctCrmIntegrationCustomerMasterListData) {
		return slctCrmIntegrationService.insertUpdateCustomerMasterDetails(slctCrmIntegrationCustomerMasterListData);
	}

	@Override
	public SlctIntegrationSalesPlanningListData getSalesPlanningListData() {
		SlctIntegrationSalesPlanningListData resultOutput = new SlctIntegrationSalesPlanningListData();
		List<SlctIntegrationSalesPlanningData> result = new ArrayList<>();
		LocalDate localDate = LocalDate.now();
		LocalDate nextMonth = localDate.plusMonths(1);
		int currentYear = localDate.getYear();
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
		String month = monthFormat.format(Date.from(dateTime.toInstant()));
		String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));

		List<MonthlySalesModel> monthlySalesModelList = slctCrmIntegrationService.getMonthlySaleModel(formattedMonth);
		for (MonthlySalesModel monthlySalesModel : monthlySalesModelList) {
			if (CollectionUtils.isEmpty(monthlySalesModel.getDealerPlannedMonthlySales())) {
				continue;
			}
			SlctIntegrationSalesPlanningData salesPlanningData = new SlctIntegrationSalesPlanningData();
			salesPlanningData.setTotalBucket1(monthlySalesModel.getTotalBucket1());
			salesPlanningData.setTotalBucket2(monthlySalesModel.getTotalBucket2());
			salesPlanningData.setTotalBucket3(monthlySalesModel.getTotalBucket3());
			if (monthlySalesModel.getSo() != null) {
				salesPlanningData.setSoEmployeeCode(monthlySalesModel.getSo().getEmployeeCode());
				salesPlanningData.setSoEmployeeUid(monthlySalesModel.getSo().getUid());
			}
			List<DealerPlannedMonthlySalesModel> dealerPlannedMonthlySalesModelList = slctCrmIntegrationService.getDealerPlannedMonthlySalesDetails(monthlySalesModel.getSubAreaMasterList().stream().collect(Collectors.toList()), monthlySalesModel.getSo(), monthlySalesModel.getMonthName(), monthlySalesModel.getMonthYear());
			List<DealerPlannedMonthlySaleData> dealerPlannedMonthlySaleDataList = new ArrayList<>();
			if (dealerPlannedMonthlySalesModelList != null && !dealerPlannedMonthlySalesModelList.isEmpty()) {
				for (DealerPlannedMonthlySalesModel dealerPlannedMonthlySalesModel : dealerPlannedMonthlySalesModelList) {
					DealerPlannedMonthlySaleData dealerPlannedMonthlySaleData = new DealerPlannedMonthlySaleData();
					if (dealerPlannedMonthlySalesModel.getBrand() != null) {
						dealerPlannedMonthlySaleData.setBrand(dealerPlannedMonthlySalesModel.getBrand().getUid());
					}
					dealerPlannedMonthlySaleData.setCrmCustomerId(dealerPlannedMonthlySalesModel.getCustomerCode());
					dealerPlannedMonthlySaleData.setCustomerName(dealerPlannedMonthlySalesModel.getCustomerName());
					dealerPlannedMonthlySaleData.setCustomerPotential(dealerPlannedMonthlySalesModel.getCustomerPotential());
					if (dealerPlannedMonthlySalesModel.getCustomerCode() != null & !dealerPlannedMonthlySalesModel.getCustomerCode().isEmpty() && userService.getUserForUID(dealerPlannedMonthlySalesModel.getCustomerCode()) != null) {
						SclCustomerModel customerModel = (SclCustomerModel) userService.getUserForUID(dealerPlannedMonthlySalesModel.getCustomerCode());
						if (customerModel != null) {
							dealerPlannedMonthlySaleData.setErpCustomerNo(customerModel.getCustomerNo());
						}
					}
					if (dealerPlannedMonthlySalesModel.getSubAreaMaster() != null) {
						dealerPlannedMonthlySaleData.setTaluka(dealerPlannedMonthlySalesModel.getSubAreaMaster().getTaluka());
						dealerPlannedMonthlySaleData.setDistrict(dealerPlannedMonthlySalesModel.getSubAreaMaster().getDistrict());
					}
					dealerPlannedMonthlySaleData.setPlannedTarget(dealerPlannedMonthlySalesModel.getPlannedTarget());
					dealerPlannedMonthlySaleData.setRevisedTarget(dealerPlannedMonthlySalesModel.getRevisedTarget());
					dealerPlannedMonthlySaleData.setBucket1(dealerPlannedMonthlySalesModel.getBucket1());
					dealerPlannedMonthlySaleData.setBucket2(dealerPlannedMonthlySalesModel.getBucket2());
					dealerPlannedMonthlySaleData.setBucket3(dealerPlannedMonthlySalesModel.getBucket3());
					if (dealerPlannedMonthlySalesModel.getMonthName() != null && !dealerPlannedMonthlySalesModel.getMonthName().isEmpty()) {
						List<String> monthAndYear = Arrays.asList(dealerPlannedMonthlySalesModel.getMonthName().split("-"));
						if (monthAndYear != null && !monthAndYear.isEmpty()) {
							dealerPlannedMonthlySaleData.setMonth(monthAndYear.get(0));
							dealerPlannedMonthlySaleData.setYear(monthAndYear.get(1));
						}
					}
					List<SlctProductSaleData> productModelList = new ArrayList<>();
					for (ProductModel sku : dealerPlannedMonthlySalesModel.getListOfSkus()) {
						SlctProductSaleData productSaleData = new SlctProductSaleData();
						ProductSaleModel productSaleModel = salesPlanningService.fetchProductSaleForDealerPlannedMonthlySales(dealerPlannedMonthlySalesModel.getSubAreaMaster().getPk().toString(), monthlySalesModel.getSo(), sku.getCode(), dealerPlannedMonthlySalesModel.getCustomerCode(), dealerPlannedMonthlySalesModel.getMonthName(), dealerPlannedMonthlySalesModel.getMonthYear());
						String id = dealerPlannedMonthlySalesModel.getCustomerCode() + "_" + sku.getCode() + "_" + dealerPlannedMonthlySalesModel.getMonthName();
						productSaleData.setId(id);
						productSaleData.setProductCode(productSaleModel.getProductCode());
						productSaleData.setProductName(productSaleModel.getProductName());
						productSaleData.setProductGrade(sku.getGrade());
						productSaleData.setProductPackaging(sku.getPackagingCondition());
//						CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(productSaleModel.getBrand().getUid()+"ProductCatalog", "Online");
//						ProductModel productModel = productService.getProductForCode(catalogVersion, sku.getCode());
//						if(productModel!=null) {
//							productSaleData.setProductGrade(productModel.getGrade());
//							productSaleData.setProductPackaging(productModel.getPackagingCondition());
//						}
						productSaleData.setProductSaleBucket1(productSaleModel.getBucket1());
						productSaleData.setProductSaleBucket2(productSaleModel.getBucket2());
						productSaleData.setProductSaleBucket3(productSaleModel.getBucket3());
						productSaleData.setProductSalePlannedTarget(productSaleModel.getPlannedTarget());
						productSaleData.setProductSaleRevisedTarget(productSaleModel.getRevisedTarget());
						productModelList.add(productSaleData);
					}
					dealerPlannedMonthlySaleData.setListOfSku(productModelList);
					dealerPlannedMonthlySaleDataList.add(dealerPlannedMonthlySaleData);
				}
			}
			salesPlanningData.setListOfDealerPlannedMonthlySales(dealerPlannedMonthlySaleDataList);
			result.add(salesPlanningData);
		}
		resultOutput.setSalesPlanningDetailsList(result);
		return resultOutput;
	}

	@Override
	public boolean insertUpdateGeographicalMasterDetails(SlctGeographicalMasterListData slctGeographicalMasterListData) {
		return slctCrmIntegrationService.insertUpdateGeographicalMasterDetails(slctGeographicalMasterListData);
	}

	@Override
	public boolean insertUpdateFreightAndIncoTermsMasterDetails(SlctFreightAndIncoTermsListData slctFreightAndIncoTermsListData) {
		return slctCrmIntegrationService.insertUpdateFreightAndIncoTermsMasterDetails(slctFreightAndIncoTermsListData);
	}

	@Override
	public boolean insertUpdateProductMasterDetails(SlctProductMasterListData slctProductMasterListData) {
		return slctCrmIntegrationService.insertUpdateProductMasterDetails(slctProductMasterListData);
	}

	@Override
	public boolean insertUpdateSalesOrderDeliverySlaDetails(SlctOrderDeliverySlaListData slctOrderDeliverySlaListData) {
		return slctCrmIntegrationService.insertUpdateSalesOrderDeliverySlaDetails(slctOrderDeliverySlaListData);
	}

	@Override
	public boolean insertWarehouseDetails(SlctWarehouseListData slctWarehouseListData) {
		return slctCrmIntegrationService.insertWarehouseDetails(slctWarehouseListData);
	}

	@Override
	public SlctEpodOutboundListData getOrderLineForEpod() {
		SlctEpodOutboundListData resultOutput = new SlctEpodOutboundListData();
		List<SlctEpodOutboundData> result = new ArrayList<>();
		List<DeliveryItemModel> deliveryItemModelList = slctCrmIntegrationService.getOrderLinesForEpod();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (DeliveryItemModel deliveryItemModel : deliveryItemModelList) {
			if (deliveryItemModel.getDiNumber() == null) {
				continue;
			}

			SlctEpodOutboundData slctEpodOutboundData = new SlctEpodOutboundData();
			OrderEntryModel orderEntryModel = (OrderEntryModel) deliveryItemModel.getEntry();

			slctEpodOutboundData.setOrderCode(orderEntryModel.getOrder().getErpOrderNumber());
			slctEpodOutboundData.setOrderLineId(orderEntryModel.getErpLineItemId());
			slctEpodOutboundData.setDeliveryId(deliveryItemModel.getDiNumber());
			slctEpodOutboundData.setDeliveryLineNumber(deliveryItemModel.getDeliveryLineNumber());

			if (deliveryItemModel.getTruckReachedDate() != null) {
				slctEpodOutboundData.setTruckReachedDate(dateFormat.format(deliveryItemModel.getTruckReachedDate()));
			}

			if (deliveryItemModel.getEpodInitiateDate() != null) {
				slctEpodOutboundData.setEpodInitiateDate(dateFormat.format(deliveryItemModel.getEpodInitiateDate()));
			}

			if (deliveryItemModel.getEpodCompletedDate() != null) {
				slctEpodOutboundData.setEpodCompletedDate(dateFormat.format(deliveryItemModel.getEpodCompletedDate()));
			}

			if (deliveryItemModel.getEpodFeedback() != null && !deliveryItemModel.getEpodFeedback().isEmpty() ) {
				if (deliveryItemModel.getEpodFeedback().get("overallDeliveryExperience") != null && !deliveryItemModel.getEpodFeedback().get("overallDeliveryExperience").isEmpty()) {
					String overallFeedbackRating = deliveryItemModel.getEpodFeedback().get("overallDeliveryExperience");
					slctEpodOutboundData.setEpodFeedBackRating(overallFeedbackRating);
				}
			}

			result.add(slctEpodOutboundData);
		}

		resultOutput.setEpodDetails(result);


		return resultOutput;

	}

	@Override
	public boolean insertDestinationSourceData(LPSourceMasterListData lpSourceMasterListData) {
		return slctCrmIntegrationService.insertDestinationSourceData(lpSourceMasterListData);
	}

	@Override
	public boolean removeExistingLpSourceMasterDataThroughSqlQuery() {
		return slctCrmIntegrationService.removeExistingLpSourceMasterDataThroughSqlQuery();
	}

	@Override
	public boolean removeExistingDepotSubAreaMappingData() {
		return slctCrmIntegrationService.removeExistingDepotSubAreaMappingData();
	}

	@Override
	public boolean insertUpdateDepotSubAreaMappingDetails(SlctDepotSubAreaMappingListData slctDepotSubAreaMappingListData) {
		return slctCrmIntegrationService.insertUpdateDepotSubAreaMappingDetails(slctDepotSubAreaMappingListData);
	}

	@Override
	public List<String> updateOrderEntryFromErp(List<IntegrationOrderEntryData> orderEntryData) {
		return slctCrmIntegrationService.updateOrderEntryFromErp(orderEntryData);
	}

	@Override
	public SLCTruckReachedDateListData updateTruckReachData(SLCTruckReachedDateListData slctTruckReachedDateListData) {
		SLCTruckReachedDateListData updatedStatusData = new SLCTruckReachedDateListData();
		List<SLCTruckReachedDateData> slcTruckReachedData = new ArrayList<SLCTruckReachedDateData>();
		slcTruckReachedData = slctCrmIntegrationService.updateTruckReachData(slctTruckReachedDateListData);
		updatedStatusData.setTruckReached(slcTruckReachedData);
		return updatedStatusData;
	}

	@Override
	public boolean removeExistingDestinationSourceMasterData() {
		return slctCrmIntegrationService.removeExistingDestinationSourceMasterData();
	}

	@Override
	public SlctCrmIntegrationDOMasterListData insertUpdateDoMasterDetails(SlctCrmIntegrationDOMasterListData slctCrmIntegrationDOMasterListData) {
		return slctCrmIntegrationService.insertUpdateDoMasterDetails(slctCrmIntegrationDOMasterListData);
	}

	@Override
	public SlctDOSubAreaMappingListData insertUpdateDoSubAreaMapping(SlctDOSubAreaMappingListData slctDOSubAreaMappingListData) {
		return slctCrmIntegrationService.insertUpdateDoSubAreaMapping(slctDOSubAreaMappingListData);
	}

	@Override
	public LeadGeneratedDetailsListData getLeadCount(Integer year, Integer month) {
		LeadGeneratedDetailsListData lead = new LeadGeneratedDetailsListData();
		if (year != 0 && month != 0) {

			List<List<Object>> leadList = slctCrmIntegrationService.getLeadCount(year, month);
			List<LeadGeneratedDetailsData> result = new ArrayList<LeadGeneratedDetailsData>();

			leadList.forEach(leads -> {
				LeadGeneratedDetailsData leadData = new LeadGeneratedDetailsData();
				if (leads.get(0) != null) {
					leadData.setCount((Integer) leads.get(0));
				}
				if (leads.get(1) != null) {
					leadData.setDistrict((String) leads.get(1));
				}
				if (leads.get(2) != null) {
					leadData.setState((String) leads.get(2));
				}

				leadData.setMonth(String.valueOf(Month.of(month)).concat("-").concat(String.valueOf(year)));
				result.add(leadData);
			});
			lead.setLeadGeneratedDetails(result);

		}
		return lead;
	}

	@Override
	public boolean insertRoutesForSoDeliverySla(SlctRouteSLAListData slctRouteSLAListData) {
		return slctCrmIntegrationService.insertRoutesForSoDeliverySla(slctRouteSLAListData);
	}

	@Override
	public SalesPlanningBottomUpIntegrationListData getBottomUpSalesPlanningData() {
		SalesPlanningBottomUpIntegrationListData resultOutput = new SalesPlanningBottomUpIntegrationListData();
		List<SalesPlanningBottomUpIntegrationData> result = new ArrayList<>();

		int currentYear;
		LocalDate localDate = LocalDate.now();
		String currentMonth = localDate.getMonth().toString();
		LocalDate nextMonth = localDate.plusMonths(1);
		if (currentMonth.equals("DECEMBER")) {
			currentYear = nextMonth.getYear();
		} else {
			currentYear = localDate.getYear();
		}
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = nextMonth.atStartOfDay(zone);
		String month = monthFormat.format(Date.from(dateTime.toInstant()));
		String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
		LOG.info("Formatted Month is " + formattedMonth);
		List<MonthlySalesModel> monthlySalesModelList = slctCrmIntegrationService.getMonthlySaleModel(formattedMonth);
		for (MonthlySalesModel monthlySalesModel : monthlySalesModelList) {
			if (monthlySalesModel.getActionPerformed() != null && monthlySalesModel.getActionPerformed().equals(WorkflowActions.APPROVED) && monthlySalesModel.getActionPerformedBy() != null && monthlySalesModel.getActionPerformedBy().getUserType() != null && monthlySalesModel.getActionPerformedBy().getUserType().equals(SclUserType.RH)) {
				CMSSiteModel brand = (CMSSiteModel) monthlySalesModel.getBrand();
				List<List<Object>> productWiseBucketsDataList = slctCrmIntegrationService.getProductSaleDetails(monthlySalesModel.getSo(), monthlySalesModel.getMonthName(), monthlySalesModel.getMonthYear(), monthlySalesModel.getSubAreaMasterList().stream().collect(Collectors.toList()), monthlySalesModel.getDistrictMaster(), brand);

				productWiseBucketsDataList.forEach(productWiseBucketsData -> {
					SalesPlanningBottomUpIntegrationData salesPlanningData = new SalesPlanningBottomUpIntegrationData();
					if (monthlySalesModel.getSo() != null) {
						salesPlanningData.setSoEmployeeCode(monthlySalesModel.getSo().getEmployeeCode());
						salesPlanningData.setSoEmployeeUid(monthlySalesModel.getSo().getUid());
					}
					List<String> monthAndYear = Arrays.asList(monthlySalesModel.getMonthName().split("-"));
					if (monthAndYear != null && !monthAndYear.isEmpty()) {
						salesPlanningData.setMonth(monthAndYear.get(0));
						salesPlanningData.setYear(monthAndYear.get(1));
					}
					salesPlanningData.setBrand(monthlySalesModel.getBrand().getUid());
					SubAreaMasterModel subAreaMasterModel = monthlySalesModel.getSubAreaMasterList().stream().collect(Collectors.toList()).get(0);
					salesPlanningData.setTaluka(subAreaMasterModel.getTaluka());
					salesPlanningData.setDistrict(subAreaMasterModel.getDistrict());
					if (monthlySalesModel.getDistrictMaster() != null) {
						salesPlanningData.setDistrictCode(monthlySalesModel.getDistrictMaster().getCode());
					}
					if (monthlySalesModel.getStateMaster() != null) {
						salesPlanningData.setState(monthlySalesModel.getStateMaster().getCode());
					}
					String productCode = productWiseBucketsData.get(0).toString();
					String productGrade = productWiseBucketsData.get(4).toString();
					String productPackaging = productWiseBucketsData.get(5).toString();
					Boolean isPremiumProduct = (Boolean) productWiseBucketsData.get(6);
//					CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(monthlySalesModel.getBrand().getUid()+"ProductCatalog", "Online");
//					ProductModel productModel = slctCrmIntegrationService.getProductModelByCode(productCode, monthlySalesModel.getBrand().getUid()+"ProductCatalog", "Online");
//					if(productModel!=null) {
					salesPlanningData.setProductGrade(productGrade);
					salesPlanningData.setProductPackaging(productPackaging);
					salesPlanningData.setProductCode(productCode);
//					}
					Double bucket1 = (Double) productWiseBucketsData.get(1);
					Double bucket2 = (Double) productWiseBucketsData.get(2);
					Double bucket3 = (Double) productWiseBucketsData.get(3);

					Double plannedTarget = bucket1 + bucket2 + bucket3;

					salesPlanningData.setBucket1(bucket1);
					salesPlanningData.setBucket2(bucket2);
					salesPlanningData.setBucket3(bucket3);
					salesPlanningData.setPlannedTargetBucket1(plannedTarget);
					salesPlanningData.setPlannedTargetBucket2(0.0);
					salesPlanningData.setPlannedTargetBucket3(0.0);

					if (isPremiumProduct) {
						salesPlanningData.setPremiumOrNonPremium("Premium");
					} else {
						salesPlanningData.setPremiumOrNonPremium("Non Premium");
					}

					result.add(salesPlanningData);
				});
			}
		}
		resultOutput.setSalesPlanningBottomUpList(result);
		return resultOutput;
	}

	@Override
	public SalesPlanningTopDownIntegrationListData getTopDownIntegrationDetails(SalesPlanningTopDownIntegrationListData salesPlanningTopDownIntegrationListData) {
		return slctCrmIntegrationService.getTopDownIntegrationDetails(salesPlanningTopDownIntegrationListData);
	}

	@Override
	public AnnualSalesBottomUpIntegrationListData getAnnualSalesBottomUpData() {
		AnnualSalesBottomUpIntegrationListData resultOutput = new AnnualSalesBottomUpIntegrationListData();
		List<AnnualSalesBottomUpIntegrationData> result = new ArrayList<>();
		String nextFinancialYear = findNextFinancialYear();
		List<AnnualSalesModel> annualSalesModelList = slctCrmIntegrationService.getAnnualSalesBottomUpData(nextFinancialYear);
		for (AnnualSalesModel annualSalesModel : annualSalesModelList) {
			if (annualSalesModel.getActionPerformed() != null && annualSalesModel.getActionPerformed().equals(WorkflowActions.APPROVED) && annualSalesModel.getActionPerformedBy() != null && annualSalesModel.getActionPerformedBy().getUserType() != null && annualSalesModel.getActionPerformedBy().getUserType().equals(SclUserType.RH)) {
				BaseSiteModel brand = annualSalesModel.getBrand();
				DistrictMasterModel districtMasterModel = annualSalesModel.getDistrictMaster();
				if (brand != null && districtMasterModel != null) {
					List<List<Object>> productDetails = slctCrmIntegrationDao.getProductDetailsFromAnnualSales(districtMasterModel, brand);

					if (productDetails != null && !productDetails.isEmpty()) {
						productDetails.forEach(product -> {
							AnnualSalesBottomUpIntegrationData integrationData = new AnnualSalesBottomUpIntegrationData();
							String productCode = product.get(0) != null ? product.get(0).toString() : null;
							String productGrade = product.get(1) != null ? product.get(1).toString() : null;
							String productPackaging = product.get(2) != null ? product.get(2).toString() : null;
							String productBagType = product.get(3) != null ? product.get(3).toString() : null;

							integrationData.setProductCode(productCode);
							integrationData.setProductGrade(productGrade);
							integrationData.setProductPackagingCondtion(productPackaging);
							integrationData.setProductBagType(productBagType);

							integrationData.setRhUid(annualSalesModel.getActionPerformedBy().getUid());
							integrationData.setRhEmployeeCode(annualSalesModel.getActionPerformedBy().getEmployeeCode());
							if (annualSalesModel.getStateMaster() != null) {
								integrationData.setState(annualSalesModel.getStateMaster().getCode());
							}
							integrationData.setBrand(brand.getUid());
							integrationData.setDistrict(annualSalesModel.getDistrictMaster().getName());
							integrationData.setDistrictCode(annualSalesModel.getDistrictMaster().getCode());
							integrationData.setFinancialYear(nextFinancialYear);
							List<List<Object>> monthWiseTargets = slctCrmIntegrationDao.getMonthWiseTargets(districtMasterModel, brand, productCode);
							List<MonthWiseTargetData> monthWiseTargetDataList = new ArrayList<>();
							Double totalTarget = 0.0;

							for (List<Object> monthWiseTarget : monthWiseTargets) {
								MonthWiseTargetData monthWiseTargetData = new MonthWiseTargetData();
								if (monthWiseTarget.get(0) != null && monthWiseTarget.get(1) != null) {
									Double monthTarget = (Double) monthWiseTarget.get(1);
									monthWiseTargetData.setMonthYear(monthWiseTarget.get(0).toString());
									monthWiseTargetData.setMonthTarget(monthTarget);
									monthWiseTargetDataList.add(monthWiseTargetData);
									totalTarget += monthTarget;
								}
							}

							integrationData.setMonthWiseTarget(monthWiseTargetDataList);
							integrationData.setTotalTarget(totalTarget);
							result.add(integrationData);
						});
					}

				}
			}

		}

		resultOutput.setAnnualSalesBottomUpList(result);
		return resultOutput;
	}


	String findNextFinancialYear() {
		LocalDate date = LocalDate.now();
		int currentYear = date.getYear();
		int fyYear = currentYear + 1;
		StringBuilder f = new StringBuilder();
		return String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(fyYear)));
	}

	@Override
	public SlctBrandMasterListData insertUpdateBrandMaster(SlctBrandMasterListData slctBrandMasterListData) {
		return slctCrmIntegrationService.insertUpdateBrandMaster(slctBrandMasterListData);
	}

	@Override
	public SlctCompetitorProductMasterListData insertUpdateCompetitorProductMaster(SlctCompetitorProductMasterListData slctCompetitorProductMasterListData) {
		return slctCrmIntegrationService.insertUpdateCompetitorProductMaster(slctCompetitorProductMasterListData);
	}

	@Override
	public boolean insertUpdateSalesNcrData(SalesNcrIntegrationListData salesNcrIntegrationListData) {
		return slctCrmIntegrationService.insertUpdateSalesNcrData(salesNcrIntegrationListData);
	}
	
	@Override
	public SlctCrmOrderListData getModifiedOrders() {
		SlctCrmOrderListData resultOutput = new SlctCrmOrderListData();
		List<SlctCrmOrderData> result = new ArrayList<>();

		List<OrderModel> orderModelList = slctCrmIntegrationService.getModifiedOrders();
		if (orderModelList != null && !orderModelList.isEmpty()) {
			for (OrderModel orderModel : orderModelList) {
				SlctCrmOrderData orderData = new SlctCrmOrderData();
				orderData.setCrmOrderNo(orderModel.getCode());
				orderData.setErpOrderNo(orderModel.getErpOrderNumber());
				orderData.setErpOrderType(orderModel.getErpOrderType());
				if (orderModel.getStatus() != null) {
					orderData.setOrderStatus(orderModel.getStatus().getCode());
				}
				if (orderModel.getUser() != null) {
					SclCustomerModel customer = (SclCustomerModel) orderModel.getUser();
					orderData.setErpCustomerNo(customer.getCustomerNo());
					orderData.setCrmCustomerUid(customer.getUid());
					orderData.setCustomerName(customer.getName());
					orderData.setState(customer.getState());
				}
				if (orderModel.getSubAreaMaster() != null) {
					orderData.setTaluka(orderModel.getSubAreaMaster().getTaluka());
					orderData.setDistrict(orderModel.getSubAreaMaster().getDistrict());
				}
				orderData.setOrderValidatedDate(orderModel.getOrderValidatedDate());
				orderData.setOrderFailedValidationDate(orderModel.getOrderFailedValidationDate());
				orderData.setOrderSentForApprovalDate(orderModel.getOrderSentForApprovalDate());
				orderData.setOrderModifiedDate(orderModel.getOrderModifiedDate());
				orderData.setOrderAcceptedDate(orderModel.getOrderAcceptedDate());
				if(orderModel.getCreatedFromCRMorERP()!=null) {
					if(orderModel.getCreatedFromCRMorERP().equals(CreatedFromCRMorERP.S4HANA)) {
						orderData.setIsErpOrder(Boolean.TRUE);
					}
					else {
						orderData.setIsErpOrder(Boolean.FALSE);
					}
				}

				if (orderModel.getSite() != null) {
					orderData.setBrand(orderModel.getSite().getUid());
				}
				if (orderModel.getDeliveryAddress() != null && orderModel.getDeliveryAddress().getShippingAddress()) {
					orderData.setErpCity(orderModel.getDeliveryAddress().getErpCity());
				}
				if (orderModel.getPlacedBy() != null) {
					orderData.setPlacedByUid(orderModel.getPlacedBy().getUid());
					if (orderModel.getPlacedBy() instanceof SclUserModel) {
						SclUserModel sclUserModel = (SclUserModel) orderModel.getPlacedBy();
						if (sclUserModel.getUserType() != null) {
							orderData.setPlacedByUserType(sclUserModel.getUserType().getCode());
						}
					} else if (orderModel.getPlacedBy() instanceof SclCustomerModel) {
						SclCustomerModel sclCustomerModel = (SclCustomerModel) orderModel.getPlacedBy();
						if (sclCustomerModel.getCounterType() != null) {
							orderData.setPlacedByUserType(sclCustomerModel.getCounterType().getCode());
							orderData.setPlacedByCustomerNo(sclCustomerModel.getCustomerNo());
						}
					}
				}
				if (orderModel.getRejectionReasons() != null && !orderModel.getRejectionReasons().isEmpty()) {
					List<String> rejectionReasons = orderModel.getRejectionReasons().stream().collect(Collectors.toList());
					String rejectionReason = String.join(", ", rejectionReasons);
					orderData.setRejectionReasons(rejectionReason);
				}
				orderData.setModifiedtime(orderModel.getModifiedtime());
				orderData.setOrderCreationTime(orderModel.getCreationtime());
				orderData.setTotalPrice(orderModel.getTotalPrice());
				orderData.setCancelledDate(orderModel.getCancelledDate());

				if (orderModel.getRetailer() != null) {
					orderData.setRetailerErpCustomerNo(orderModel.getRetailer().getCustomerNo());
					orderData.setRetailerCrmUid(orderModel.getRetailer().getUid());
					orderData.setRetailerName(orderModel.getRetailer().getName());
				}

				result.add(orderData);
			}
		}
		resultOutput.setCrmOrders(result);
		return resultOutput;
	}

	@Override
	public SalesPlanningDealerReviewListData getSalesPlanningReviewedTargets() {
		SalesPlanningDealerReviewListData resultOutput = new SalesPlanningDealerReviewListData();
		List<SalesPlanningDealerReviewData> result = new ArrayList<>();
		List<ProductSaleModel> productSaleModelList = slctCrmIntegrationDao.getProductSaleModels();
		for (ProductSaleModel productSaleModel : productSaleModelList) {
			SalesPlanningDealerReviewData reviewData = new SalesPlanningDealerReviewData();
			reviewData.setDealerCrmCode(productSaleModel.getCustomerCode());
			if (productSaleModel.getMonthName() != null) {
				List<String> monthAndYear = Arrays.asList(productSaleModel.getMonthName().split("-"));
				if (monthAndYear != null && !monthAndYear.isEmpty()) {
					reviewData.setMonth(monthAndYear.get(0));
					reviewData.setYear(monthAndYear.get(1));
				}
			}
			if (productSaleModel.getBrand() != null) {
				reviewData.setBrand(productSaleModel.getBrand().getUid());
			}
			reviewData.setRevisedTarget(productSaleModel.getRevisedTarget());
			if (productSaleModel.getSalesOfficer() != null) {
				reviewData.setSoEmployeeUid(productSaleModel.getSalesOfficer().getUid());
				reviewData.setSoEmployeeCode(productSaleModel.getSalesOfficer().getEmployeeCode());
			}
			if (productSaleModel.getSubAreaMaster() != null) {
				reviewData.setTaluka(productSaleModel.getSubAreaMaster().getTaluka());
			}
			if (productSaleModel.getDistrictMaster() != null) {
				reviewData.setDistrict(productSaleModel.getDistrictMaster().getName());
				reviewData.setDistrictCode(productSaleModel.getDistrictMaster().getCode());
			}
			if (productSaleModel.getStateMaster() != null) {
				reviewData.setState(productSaleModel.getStateMaster().getName());
			}
			reviewData.setProductCode(productSaleModel.getProductCode());
			reviewData.setProductName(productSaleModel.getProductName());
			reviewData.setProductGrade(productSaleModel.getProductGrade());
			reviewData.setProductPackagingCondition(productSaleModel.getProductPackaging());
			reviewData.setProductPackType(productSaleModel.getProductPackType());

			result.add(reviewData);
		}
		resultOutput.setDealerRevisedTargets(result);
		return resultOutput;
	}

	@Override
	public boolean insertUpdateCustomerAddress(ERPCustomerAddressListData erpCustomerAddressListData) {
		return slctCrmIntegrationService.insertUpdateCustomerAddress(erpCustomerAddressListData);
	}

	@Override
	public SlctCrmOrderEntryListData getModifiedOrderEntries() {
		SlctCrmOrderEntryListData resultOutput = new SlctCrmOrderEntryListData();
		List<SlctCrmOrderEntryData> result = new ArrayList<>();
		List<OrderEntryModel> orderEntryModelList = slctCrmIntegrationService.getModifiedOrderEntries();
		for (OrderEntryModel orderEntryModel : orderEntryModelList) {
			SlctCrmOrderEntryData orderEntryData = new SlctCrmOrderEntryData();
			orderEntryData.setCrmOrderCode(orderEntryModel.getOrder().getCode());
			orderEntryData.setErpOrderCode(orderEntryModel.getOrder().getErpOrderNumber());
			orderEntryData.setErpLineItemId(orderEntryModel.getErpLineItemId());
			orderEntryData.setEntryNumber(orderEntryModel.getEntryNumber());
			if (orderEntryModel.getProduct() != null) {
				orderEntryData.setProductCode(orderEntryModel.getProduct().getCode());
				orderEntryData.setProductGrade(orderEntryModel.getProduct().getGrade());
				orderEntryData.setProductPackagingCondition(orderEntryModel.getProduct().getPackagingCondition());
				orderEntryData.setProductPackType(orderEntryModel.getProduct().getBagType());
			}
			orderEntryData.setInvoiceCreationDateAndTime(orderEntryModel.getInvoiceCreationDateAndTime());
			orderEntryData.setCancelledDate(orderEntryModel.getCancelledDate());
			orderEntryData.setDiCreationDateAndTime(orderEntryModel.getDiCreationDateAndTime());
			orderEntryData.setDeliveredDate(orderEntryModel.getDeliveredDate());
			if (orderEntryModel.getStatus() != null) {
				orderEntryData.setOrderEntryStatus(orderEntryModel.getStatus().getCode());
			}
			if (orderEntryModel.getSource() != null) {
				orderEntryData.setSourceCode(orderEntryModel.getSource().getCode());
			}
			orderEntryData.setQuantityInMT(orderEntryModel.getQuantityInMT());
			orderEntryData.setInvoiceQty(orderEntryModel.getInvoiceQuantity());
			orderEntryData.setTruckAllocatedQty(orderEntryModel.getTruckAllocatedQty());
			orderEntryData.setDeliveryQty(orderEntryModel.getDeliveryQty());
			orderEntryData.setRouteId(orderEntryModel.getRouteId());
			orderEntryData.setDistance(orderEntryModel.getDistance());
			if (orderEntryModel.getFreightTerms() != null) {
				orderEntryData.setFreightTerms(orderEntryModel.getFreightTerms().getCode());
			}
			if (orderEntryModel.getFob() != null) {
				orderEntryData.setFob(orderEntryModel.getFob().getCode());
			}
			orderEntryData.setEpodCompleted(orderEntryModel.getEpodCompleted());
			orderEntryData.setTruckNo(orderEntryModel.getTruckNo());
			orderEntryData.setDriverContactNo(orderEntryModel.getDriverContactNo());
			orderEntryData.setTruckAllocatedDate(orderEntryModel.getTruckAllocatedDate());
			orderEntryData.setTruckDispatcheddate(orderEntryModel.getTruckDispatcheddate());
			orderEntryData.setInvoiceNumber(orderEntryModel.getInvoiceNumber());
			orderEntryData.setDiNumber(orderEntryModel.getDiNumber());
			orderEntryData.setParentId(orderEntryModel.getParentId());
			orderEntryData.setTransporterName(orderEntryModel.getTransporterName());
			orderEntryData.setTransporterPhoneNumber(orderEntryModel.getTransporterPhoneNumber());
			orderEntryData.setErpDriverNumber(orderEntryModel.getErpDriverNumber());
			orderEntryData.setErpTruckNumber(orderEntryModel.getErpTruckNumber());
			orderEntryData.setConsigneeId(orderEntryModel.getConsigneeId());
			orderEntryData.setCarrierId(orderEntryModel.getCarrierId());
			orderEntryData.setInvoiceCancelDate(orderEntryModel.getInvoiceCancelDate());
			orderEntryData.setInvoiceCancelQty(orderEntryModel.getInvoiceCancelQuantity());
			if (orderEntryModel.getExpectedDeliveryslot() != null) {
				orderEntryData.setExpectedDeliverySlot(orderEntryModel.getExpectedSlot().getCentreTime());
			}
			if (orderEntryModel.getCalculatedDeliveryslot() != null) {
				orderEntryData.setCalculatedDeliverySlot(orderEntryModel.getCalculatedSlot().getCentreTime());
			}
			orderEntryData.setExpectedDeliveryDate(orderEntryModel.getExpectedDeliveryDate());
			orderEntryData.setCalculatedDeliveryDate(orderEntryModel.getCalculatedDeliveryDate());
			orderEntryData.setSequence(orderEntryModel.getSequence());
			orderEntryData.setTotalPrice(orderEntryModel.getTotalPrice());
			orderEntryData.setBasePrice(orderEntryModel.getBasePrice());
			orderEntryData.setQuantity(orderEntryModel.getQuantity());
			orderEntryData.setTokenNumber(orderEntryModel.getTokenNumber());
			orderEntryData.setEventDate(orderEntryModel.getEventDate());

			result.add(orderEntryData);
		}
		resultOutput.setSlctOrderEntry(result);
		return resultOutput;
	}

	@Override
	public InfluencerVisitListData getInfluencerVisitDetails() {
		InfluencerVisitListData listData = new InfluencerVisitListData();
		List<InfluencerVisitData> dataList = new ArrayList<>();
		List<InfluencerVisitMasterModel> influencerVisitDetails = slctCrmIntegrationService.getInfluencerVisitDetails();
		if (influencerVisitDetails != null && !influencerVisitDetails.isEmpty()) {
			for (InfluencerVisitMasterModel model : influencerVisitDetails) {
				if (model != null) {
					InfluencerVisitData data = new InfluencerVisitData();
					if (model.getSclCustomer() != null) {
						data.setName(model.getSclCustomer().getName());
						data.setCode(model.getSclCustomer().getUid());
						if (model.getSclCustomer().getInfluencerType() != null) {
							data.setTypeOfInfluencer(model.getSclCustomer().getInfluencerType().getCode());
						}
						data.setMobileNumber(model.getSclCustomer().getMobileNumber());
					}
					//data.setSiteAddress(addressConverter.convert(model.getSiteAddress()));
					data.setSiteAddress(model.getAddressSite());
					data.setLatitude(model.getLatitude());
					data.setLongitude(model.getLongitude());
					data.setNoOfSiteRunning(model.getNoOfSiteRunning());
					data.setNoOfShreeSiteRunning(model.getNoOfShreeSiteRunning());
					data.setShreeSalesPerBag(model.getShreeSalesPerBag());
					data.setMonthlyPotentialInBags(model.getMonthlyPotentialInBags());
					data.setSow(model.getSow());
					StringBuilder brand = new StringBuilder();
					if (model.getBrandsUsedByInfluencer() != null && !model.getBrandsUsedByInfluencer().isEmpty()) {
						for (String s : model.getBrandsUsedByInfluencer()) {
							if (StringUtils.isNotBlank(s)) {
								brand.append(s).append(",");
							}
						}
					}
					LOG.info("Brand Before comma:" + brand);
					if (brand.toString().endsWith(",")) {
						brand = new StringBuilder(brand.substring(0, brand.length() - 1));
					}
					LOG.info("Brand After Comma:" + brand);
					data.setBrand(brand.toString());
					data.setBrandsUsedByInfluencer(model.getBrandsUsedByInfluencer());
					data.setVisitId(model.getId());
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					if (model.getEndVisitTime() != null) {
						String visitDate = formatter.format(model.getEndVisitTime());
						data.setVisitDate(visitDate);
					}

					/*if(model.getVisit()!=null) {
						data.setVisitId(model.getVisit().getId());
					}*/
					if (model.getVisit() != null && model.getVisit().getUser() != null) {
						data.setTsoEmail(model.getVisit().getUser().getUid());
						data.setTsoName(model.getVisit().getUser().getName());
					}
					dataList.add(data);
				}
			}
		}
		listData.setInfluencerVisits(dataList);
		return listData;
	}

	@Override
	public EndCustomerComplaintDataList getEndCustomerComplaintsForSLCT(String startDate, String endDate) {
		EndCustomerComplaintDataList list = new EndCustomerComplaintDataList();
		List<EndCustomerComplaintData> dataList = new ArrayList<>();
		List<EndCustomerComplaintModel> endCustomerComplaintsForSLCT = slctCrmIntegrationService.getEndCustomerComplaintsForSLCT(startDate, endDate);
		if (endCustomerComplaintsForSLCT != null && !endCustomerComplaintsForSLCT.isEmpty()) {
			for (EndCustomerComplaintModel model : endCustomerComplaintsForSLCT) {
				if (model != null) {
					if (model.getRequestId() != null) {
						EndCustomerComplaintData data = technicalAssistanceFacade.getEndCustomerDetailsByRequestId(model.getRequestId(),false);
						dataList.add(data);
					}
				}
			}
		}
		list.setEndCustomerComplaintDataList(dataList);
		return list;
	}

	@Override
	public ScheduledMeetListData getinfluencerMeetCompletion() {
		ScheduledMeetListData listData = new ScheduledMeetListData();
		List<ScheduledMeetData> list = new ArrayList<>();
		List<MeetingScheduleModel> meetList = slctCrmIntegrationService.getinfluencerMeetCompletion();
		for (MeetingScheduleModel meet : meetList) {
			ScheduledMeetData data = new ScheduledMeetData();
			data = scheduledMeetConverter.convert(meet);
			if (meet.getMeetingForm() != null) {
				data.setMeetingForm(meetingCompletionFormConverter.convert(meet.getMeetingForm()));
			}
			list.add(data);
		}
		listData.setMeetCards(list);
		return listData;
	}

	@Override
	public PointRequisitionSalesListData getPointRequisitionDetails() {
		PointRequisitionSalesListData resultOutput = new PointRequisitionSalesListData();
		List<PointRequisitionSalesData> result = new ArrayList<>();
		List<PointRequisitionModel> pointRequisitionModelList = slctCrmIntegrationService.getPointRequisitonDetails();
		for (PointRequisitionModel pointRequisitionModel : pointRequisitionModelList) {
			PointRequisitionSalesData pointRequisitionSalesData = new PointRequisitionSalesData();

			pointRequisitionSalesData.setRequisitionId(pointRequisitionModel.getRequisitionId());
			pointRequisitionSalesData.setRequisitionCreationDate(pointRequisitionModel.getRequisitionCreationDate());
			pointRequisitionSalesData.setDeliveryDate(pointRequisitionModel.getDeliveryDate());
			if (pointRequisitionModel.getDeliverySlot() != null) {
				pointRequisitionSalesData.setDeliverySlot(pointRequisitionModel.getDeliverySlot().getCode());
			}

			if (pointRequisitionModel.getRequestRaisedBy() != null) {
				CustomerBasicDetailsData requestRaisedByData = new CustomerBasicDetailsData();
				requestRaisedByData.setName(pointRequisitionModel.getRequestRaisedBy().getName());
				requestRaisedByData.setCrmCustomerCode(pointRequisitionModel.getRequestRaisedBy().getUid());
				requestRaisedByData.setErpCustomerNo(pointRequisitionModel.getRequestRaisedBy().getCustomerNo());
				if (pointRequisitionModel.getRequestRaisedBy().getCounterType() != null) {
					requestRaisedByData.setCustomerType(pointRequisitionModel.getRequestRaisedBy().getCounterType().getCode());
				}
				pointRequisitionSalesData.setRequestRaisedBy(requestRaisedByData);
			}

			if (pointRequisitionModel.getRequestRaisedTo() != null) {
				CustomerBasicDetailsData requestRaisedToData = new CustomerBasicDetailsData();
				requestRaisedToData.setName(pointRequisitionModel.getRequestRaisedTo().getName());
				requestRaisedToData.setCrmCustomerCode(pointRequisitionModel.getRequestRaisedTo().getUid());
				requestRaisedToData.setErpCustomerNo(pointRequisitionModel.getRequestRaisedTo().getCustomerNo());
				if (pointRequisitionModel.getRequestRaisedTo().getCounterType() != null) {
					requestRaisedToData.setCustomerType(pointRequisitionModel.getRequestRaisedTo().getCounterType().getCode());
				}
				pointRequisitionSalesData.setRequestRaisedTo(requestRaisedToData);
			}

			if (pointRequisitionModel.getRequestRaisedFor() != null) {
				CustomerBasicDetailsData influencerData = new CustomerBasicDetailsData();
				influencerData.setName(pointRequisitionModel.getRequestRaisedFor().getName());
				influencerData.setCrmCustomerCode(pointRequisitionModel.getRequestRaisedFor().getUid());
				influencerData.setErpCustomerNo(pointRequisitionModel.getRequestRaisedFor().getCustomerNo());
				if (pointRequisitionModel.getRequestRaisedFor().getCounterType() != null) {
					influencerData.setCustomerType(pointRequisitionModel.getRequestRaisedFor().getCounterType().getCode());
				}
				pointRequisitionSalesData.setInfluencer(influencerData);

				pointRequisitionSalesData.setSchemeId(pointRequisitionModel.getSchemeId());

			}

			if (pointRequisitionModel.getApprovedBy() != null) {
				CustomerBasicDetailsData approvedByData = new CustomerBasicDetailsData();
				approvedByData.setName(pointRequisitionModel.getApprovedBy().getName());
				approvedByData.setCrmCustomerCode(pointRequisitionModel.getApprovedBy().getUid());
				approvedByData.setErpCustomerNo(pointRequisitionModel.getApprovedBy().getCustomerNo());
				if (pointRequisitionModel.getApprovedBy().getCounterType() != null) {
					approvedByData.setCustomerType(pointRequisitionModel.getApprovedBy().getCounterType().getCode());
				}
				pointRequisitionSalesData.setApprovedBy(approvedByData);
			}

			pointRequisitionSalesData.setAddressLine1(pointRequisitionModel.getAddressLine1());
			pointRequisitionSalesData.setAddressLine2(pointRequisitionModel.getAddressLine2());
			pointRequisitionSalesData.setState(pointRequisitionModel.getState());
			pointRequisitionSalesData.setDistrict(pointRequisitionModel.getDistrict());
			pointRequisitionSalesData.setCity(pointRequisitionModel.getCity());
			pointRequisitionSalesData.setTaluka(pointRequisitionModel.getTaluka());
			pointRequisitionSalesData.setPincode(pointRequisitionModel.getPincode());
			pointRequisitionSalesData.setPhoneNumber(pointRequisitionModel.getPhoneNumber());
			if (pointRequisitionModel.getStatus() != null) {
				pointRequisitionSalesData.setStatus(pointRequisitionModel.getStatus().getCode());
			}
			pointRequisitionSalesData.setClientName(pointRequisitionModel.getClientName());
			if (pointRequisitionModel.getProduct() != null) {
				ProductDetailsData productDetailsData = new ProductDetailsData();
				productDetailsData.setCode(pointRequisitionModel.getProduct().getCode());
				productDetailsData.setGrade(pointRequisitionModel.getProduct().getGrade());
				productDetailsData.setName(pointRequisitionModel.getProduct().getName());
				productDetailsData.setPackagingCondition(pointRequisitionModel.getProduct().getPackagingCondition());
				productDetailsData.setBagType(pointRequisitionModel.getProduct().getBagType());
				pointRequisitionSalesData.setProductDetails(productDetailsData);
			}
			if (pointRequisitionModel.getBrand() != null) {
				if (pointRequisitionModel.getBrand().getUid().equals("102")) {
					pointRequisitionSalesData.setBrand("SHREE");
				} else if (pointRequisitionModel.getBrand().getUid().equals("103")) {
					pointRequisitionSalesData.setBrand("BANGUR");
				} else if (pointRequisitionModel.getBrand().getUid().equals("104")) {
					pointRequisitionSalesData.setBrand("ROCKSTRONG");
				}
			}
			pointRequisitionSalesData.setRequestApprovedDate(pointRequisitionModel.getReqApprovedDate());
			pointRequisitionSalesData.setTotalPoints(pointRequisitionModel.getTotalPoints());
			pointRequisitionSalesData.setQuantity(pointRequisitionModel.getQuantity());
			pointRequisitionSalesData.setPoints(pointRequisitionModel.getPoints());
			pointRequisitionSalesData.setPointsPerBag(pointRequisitionModel.getPointsPerBag());

			result.add(pointRequisitionSalesData);
		}
		resultOutput.setPointRequisitionData(result);
		return resultOutput;
	}

	@Override
	public EndCustomerComplaintDataList getEndCustomerComplaintClosureRequest() {
		List<EndCustomerComplaintData> list = new ArrayList<>();
		EndCustomerComplaintDataList dataList = new EndCustomerComplaintDataList();
		List<EndCustomerComplaintModel> modelList = slctCrmIntegrationService.getEndCustomerComplaintClosureRequest();
		for (EndCustomerComplaintModel model : modelList) {
			EndCustomerComplaintData data = new EndCustomerComplaintData();
			technicalAssistanceFacadeImpl.populateEndCustomerComplaintModel(model, data, false);
			list.add(data);
		}
		dataList.setEndCustomerComplaintDataList(list);
		return dataList;
	}

	@Override
	public SlctCrmSiteMasterListData getSiteMasterDetailsForSLCT() {
		SlctCrmSiteMasterListData listData = new SlctCrmSiteMasterListData();
		List<SlctCrmSiteMasterData> dataList = new ArrayList<>();
		List<SclSiteMasterModel> siteMasterDetailsForSLCT = slctCrmIntegrationService.getSiteMasterDetailsForSLCT();
		if (siteMasterDetailsForSLCT != null && !siteMasterDetailsForSLCT.isEmpty()) {
			for (SclSiteMasterModel sclSiteMaster : siteMasterDetailsForSLCT) {

				double totalBagsCurrentMonth = 0.0;
				double premBagsCurrentMonth = 0.0;
				double nonPremBagsCurrentMonth = 0.0;
				Date currDate = new Date();

				SlctCrmSiteMasterData data = new SlctCrmSiteMasterData();
				data.setSiteCode(sclSiteMaster.getUid());
				data.setSiteName(sclSiteMaster.getName());
				if (sclSiteMaster.getCreatedBy() != null) {
					data.setTsoEmailId(sclSiteMaster.getCreatedBy().getUid());
					data.setTsoName(sclSiteMaster.getCreatedBy().getName());
				}
				if (sclSiteMaster.getAddresses() != null && !sclSiteMaster.getAddresses().isEmpty()) {
					String address = "";
					List<AddressModel> addresses = (List<AddressModel>) sclSiteMaster.getAddresses();
					if (addresses != null && !addresses.isEmpty()) {
						AddressModel addressModel = addresses.get(0);
						if (addressModel != null) {
							data.setDistrict(addressModel.getDistrict());
							data.setState(addressModel.getState());
							data.setTaluka(addressModel.getTaluka());
							data.setCity(addressModel.getErpCity());
							data.setPinCode(addressModel.getPostalcode());
							if(!addressModel.getLine1().isBlank()) {
								address += addressModel.getLine1();
								if(!addressModel.getLine2().isBlank()) {
									address += "," + addressModel.getLine2();
								}
								data.setSiteAddress(address);
							}
						}
					}
				}

				if (sclSiteMaster.getConstructionStage() != null) {
					data.setConstructionStage(sclSiteMaster.getConstructionStage().getCode());
				}
				if (sclSiteMaster.getSiteCategoryType() != null) {
					data.setSiteCategory(enumerationService.getEnumerationName(sclSiteMaster.getSiteCategoryType(),i18NService.getCurrentLocale()));
				}
				if (sclSiteMaster.getSiteStatus() != null) {
					data.setSiteStatus(sclSiteMaster.getSiteStatus().getCode());
				}
				if(sclSiteMaster.getCementProduct()!=null) {
					data.setBrand(sclSiteMaster.getCementProduct().getCode());
					if(sclSiteMaster.getCementProduct().getCompetitorProductType()!=null) {
						data.setCementType(enumerationService.getEnumerationName(sclSiteMaster.getCementProduct().getCompetitorProductType(),i18NService.getCurrentLocale()));
					}
				}
//				if (sclSiteMaster.getCementType() != null) {
//					data.setCementType(sclSiteMaster.getCementType().getName());
//				}
//				if (sclSiteMaster.getCementBrand() != null) {
//					data.setBrand(sclSiteMaster.getCementBrand().getName());
//				}
				data.setTotalBagsPurchased(sclSiteMaster.getNumberOfBagsPurchased());
				CustomerBasicDetailsData customerDetailedData = new CustomerBasicDetailsData();
				if (sclSiteMaster.getDealer() != null) {
					SclCustomerModel dealer = sclSiteMaster.getDealer();
					customerDetailedData.setCrmCustomerCode(dealer.getUid());
					customerDetailedData.setName(dealer.getName());
					customerDetailedData.setErpCustomerNo(dealer.getCustomerNo());
					customerDetailedData.setCustomerType(dealer.getCounterType().getCode());
				}
				data.setDealer(customerDetailedData);
				data.setRetailer(sclSiteMaster.getRetailer());
				List<B2BCustomerModel> taggedPartners = new ArrayList<B2BCustomerModel>();
				if (sclSiteMaster.getTaggedPartners() != null && !sclSiteMaster.getTaggedPartners().isEmpty()) {
					taggedPartners = (List<B2BCustomerModel>) sclSiteMaster.getTaggedPartners();
				}

				List<CustomerBasicDetailsData> customerModels = new ArrayList<>();
				if (taggedPartners != null && !taggedPartners.isEmpty()) {
					for (B2BCustomerModel taggedPartner : taggedPartners) {
						SclCustomerModel sclCustomerModel = (SclCustomerModel) userService.getUserForUID(taggedPartner.getUid());
						CustomerBasicDetailsData infDetailsData = new CustomerBasicDetailsData();
						infDetailsData.setCrmCustomerCode(sclCustomerModel.getUid());
						infDetailsData.setName(sclCustomerModel.getName());
						infDetailsData.setErpCustomerNo(sclCustomerModel.getCustomerNo());
						infDetailsData.setCustomerType(sclCustomerModel.getCounterType().getCode());
						customerModels.add(infDetailsData);
					}
				}
				data.setInfluencer(customerModels);
				data.setBagsPurchasedInCurrentMonth(sclSiteMaster.getCurrentMonthSale());
				data.setContactNumber(sclSiteMaster.getMobileNumber());
				if(sclSiteMaster.getPersonMetAtSite()!=null) {
					data.setPersonMet(sclSiteMaster.getPersonMetAtSite().getCode());
				}
				data.setBuildUpArea(sclSiteMaster.getBuiltUpArea());
				data.setNextSlabCasting(sclSiteMaster.getNextSlabCasting());
				data.setBalanceCementRequirement(sclSiteMaster.getBalanceCementRequirement());
				if(sclSiteMaster.getServiceProvidedAtSite()!=null) {
					if(sclSiteMaster.getServiceProvidedAtSite()) {
						data.setServiceProvided("YES");
					}
					else {
						data.setServiceProvided("NO");
					}
				}
				if(sclSiteMaster.getServiceType()!=null) {
					data.setServiceType(sclSiteMaster.getServiceType().getName());
				}
				data.setDateOfPurchase(sclSiteMaster.getDateOfPurchase());
				data.setRemarks(sclSiteMaster.getRemarks());
				if(sclSiteMaster.getSiteStatus()!=null) {
					if(sclSiteMaster.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
						if(sclSiteMaster.getPreviousCementProduct()!=null) {
							if(sclSiteMaster.getPreviousCementProduct().getCompetitorProductType()!=null && sclSiteMaster.getPreviousCementProduct().getCompetitorProductType().equals(CompetitorProductType.CS)) {
								data.setCompetitorProduct(sclSiteMaster.getPreviousCementProduct().getCode());
							}
						}
					}
					else {
						if(sclSiteMaster.getCementProduct()!=null) {
							if(sclSiteMaster.getCementProduct().getCompetitorProductType()!=null && sclSiteMaster.getCementProduct().getCompetitorProductType().equals(CompetitorProductType.CS)) {
								data.setCompetitorProduct(sclSiteMaster.getCementProduct().getCode());
							}
						}
					}
				}
				if(sclSiteMaster.getPreviousCementProduct()!=null && sclSiteMaster.getPreviousCementProduct().getPremiumProductType()!=null) {
					data.setProductType(enumerationService.getEnumerationName(sclSiteMaster.getPreviousCementProduct().getPremiumProductType(),i18NService.getCurrentLocale()));
				}
				premBagsCurrentMonth = siteManagementService.calculateBagsCount(sclSiteMaster,currDate,"premium");
				totalBagsCurrentMonth = siteManagementService.calculateBagsCount(sclSiteMaster,currDate,"total");
				nonPremBagsCurrentMonth = totalBagsCurrentMonth - premBagsCurrentMonth;

				data.setPremiumBagsCountMtd(premBagsCurrentMonth);
				data.setNonPremiumBagsCountMtd(nonPremBagsCurrentMonth);

				dataList.add(data);
			}
		}
		listData.setSlctCrmSiteMasterData(dataList);
		return listData;
	}

		@Override
	public SlctCrmSiteMasterListData getSiteVisitForms() {
		int count = 0;
		List<String> siteVisitCodesList = new ArrayList<>();
		SlctCrmSiteMasterListData resultOutput = new SlctCrmSiteMasterListData();
		List<SlctCrmSiteMasterData> result = new ArrayList<>();
		List<SiteVisitMasterModel> siteVisitMasterModelList = slctCrmIntegrationService.getSiteVisitForms();
		for (SiteVisitMasterModel siteVisitMasterModel : siteVisitMasterModelList) {
			try {
				SlctCrmSiteMasterData siteVisitData = new SlctCrmSiteMasterData();
				siteVisitData.setVisitId(siteVisitMasterModel.getId());
				if (siteVisitMasterModel.getSclCustomer() != null) {
					SclSiteMasterModel siteMasterModel = (SclSiteMasterModel) siteVisitMasterModel.getSclCustomer();
					populateBasicSiteVisitDetails(siteVisitData, siteMasterModel);
				}
				if (siteVisitMasterModel.getVisit() != null && siteVisitMasterModel.getVisit().getUser() != null) {
					siteVisitData.setTsoEmailId(siteVisitMasterModel.getVisit().getUser().getUid());
					siteVisitData.setTsoName(siteVisitMasterModel.getVisit().getUser().getName());
				}
				if (siteVisitMasterModel.getSiteStatus() != null) {
					switch (siteVisitMasterModel.getSiteStatus().getCode()) {
						case SclFacadesConstants.SITE_STATUS.SITE_CONVERTED:
							setBrandAndCementType(siteVisitData,siteVisitMasterModel.getConvertedToProduct());
							setCompetitorProduct(siteVisitData, siteVisitMasterModel.getCementProduct());
							break;
						case SclFacadesConstants.SITE_STATUS.SITE_LOST:
							setBrandAndCementType(siteVisitData, siteVisitMasterModel.getLostToCementProduct());
							break;
						default:
							setBrandAndCementType(siteVisitData, siteVisitMasterModel.getCementProduct());
							break;
					}
				} else {
					setBrandAndCementType(siteVisitData, siteVisitMasterModel.getCementProduct());
				}

				if (siteVisitMasterModel.getSiteStatus() != null) {
					siteVisitData.setSiteStatus(siteVisitMasterModel.getSiteStatus().getCode());
				}

				if(siteVisitMasterModel.getPersonMetAtSite()!=null){
					siteVisitData.setPersonMet(siteVisitMasterModel.getPersonMetAtSite().getCode());
				}
				if(siteVisitMasterModel.getBuiltUpArea()!=null){
					siteVisitData.setBuildUpArea(siteVisitMasterModel.getBuiltUpArea());
				}
				if(siteVisitMasterModel.getNextSlabCasting()!=null){
					siteVisitData.setNextSlabCasting(siteVisitMasterModel.getNextSlabCasting());
				}
				if(siteVisitMasterModel.getBalanceCementRequirement()!=null){
					siteVisitData.setBalanceCementRequirement(siteVisitMasterModel.getBalanceCementRequirement());
				}
				if(siteVisitMasterModel.getServiceProvidedAtSite()!=null){
					if(siteVisitMasterModel.getServiceProvidedAtSite()){
						siteVisitData.setServiceProvided("YES");
					}
					else{
						siteVisitData.setServiceProvided("NO");
					}
				}

				if(siteVisitMasterModel.getServiceType()!=null){
					siteVisitData.setServiceType(siteVisitMasterModel.getServiceType().getName());
				}
				if(siteVisitMasterModel.getServiceTypeTest()!=null && !(siteVisitMasterModel.getServiceTypeTest().isEmpty())) {
					String serviceTypeTest = siteVisitMasterModel.getServiceTypeTest().stream()
							.map(SiteServiceTestModel::getName)
							.filter(StringUtils::isNotBlank)
							.collect(Collectors.joining(","));
					siteVisitData.setServiceTypeTest(serviceTypeTest);
				}
				if(siteVisitMasterModel.getDateOfPurchase()!=null){
					siteVisitData.setDateOfPurchase(siteVisitMasterModel.getDateOfPurchase());
				}
				if(siteVisitMasterModel.getRemarks()!=null){
					siteVisitData.setRemarks(siteVisitMasterModel.getRemarks());
				}
				siteVisitData.setVisitDate(siteVisitMasterModel.getEndVisitTime());

				siteVisitData.setContractorName(siteVisitMasterModel.getContractorName());
				siteVisitData.setContractorPhoneNumber(siteVisitMasterModel.getContractorPhoneNumber());
				siteVisitData.setMasonName(siteVisitMasterModel.getMasonName());
				siteVisitData.setMasonPhoneNumber(siteVisitMasterModel.getMasonPhoneNumber());
				siteVisitData.setArchitectName(siteVisitMasterModel.getArchitectName());
				siteVisitData.setArchitectPhoneNumber(siteVisitMasterModel.getArchitectNumber());
				siteVisitData.setReasonForNonConversion(siteVisitMasterModel.getReasons());
				if (Objects.nonNull(siteVisitMasterModel.getConstructionStage())) {
					String constructionStage = enumerationService.getEnumerationName(siteVisitMasterModel.getConstructionStage(),i18NService.getCurrentLocale());
					siteVisitData.setConstructionStage(constructionStage);
				}
				if(Objects.nonNull(siteVisitMasterModel.getReasonForSiteLoss())) {
					String reasonForSiteLoss = enumerationService.getEnumerationName(siteVisitMasterModel.getReasonForSiteLoss(),i18NService.getCurrentLocale());
					siteVisitData.setReasonForSiteLoss(reasonForSiteLoss);
				}
				if(Objects.nonNull(siteVisitMasterModel.getPricePerRange())) {
					siteVisitData.setPricePerRange(siteVisitMasterModel.getPricePerRange());
				}
				result.add(siteVisitData);
				siteVisitCodesList.add(siteVisitMasterModel.getId());
				count++;
			}
			catch (RuntimeException e) {
				LOG.error(String.format("Exception Occured in the SiteVisitMaster integration. SiteVisitMaster Unique field is :%s and Error Message : %s",siteVisitMasterModel.getId(),e.getMessage()));
			}
		}
		LOG.info(String.format("Site Visit Master Integration - The total no of records processed in the API : %d", count));
		LOG.info(String.format("SiteVisit Codes processed in the Site Visit Master Integration : %s",siteVisitCodesList.toString()));
		resultOutput.setSlctCrmSiteMasterData(result);
		return resultOutput;
	}

	/**
	 * Populate Brand and CementType value
	 * @param siteVisitData
	 * @param cementProduct
	 */
	private void setBrandAndCementType(SlctCrmSiteMasterData siteVisitData ,CompetitorProductModel cementProduct) {
		if (cementProduct != null) {
			siteVisitData.setBrand(cementProduct.getCode());
			if (cementProduct.getPremiumProductType() != null) {
				siteVisitData.setCementType(enumerationService.getEnumerationName(cementProduct.getPremiumProductType(), i18NService.getCurrentLocale()));
			}
			if (cementProduct.getCompetitorProductType() != null) {
				siteVisitData.setSiteCategory(enumerationService.getEnumerationName(cementProduct.getCompetitorProductType(),i18NService.getCurrentLocale()));
			}
		}
	}

	/**
	 * Populate CompetitorProduct and ProductType value
	 * @param siteVisitData
	 * @param competitorProduct
	 */
	private void setCompetitorProduct(SlctCrmSiteMasterData siteVisitData ,CompetitorProductModel competitorProduct) {
		if (Objects.nonNull(competitorProduct) && Objects.nonNull(competitorProduct.getCompetitorProductType()) && competitorProduct.getCompetitorProductType().equals(CompetitorProductType.CS)) {
			siteVisitData.setCompetitorProduct(competitorProduct.getCode());
			if(Objects.nonNull(competitorProduct.getPremiumProductType())){
				siteVisitData.setProductType(enumerationService.getEnumerationName(competitorProduct.getPremiumProductType(), i18NService.getCurrentLocale()));
			}
		}
	}


	@Override
	public boolean insertUpdateProductPointMaster(SlctProductPointMasterListData slctProductPointMasterListData) {
		return slctCrmIntegrationService.insertUpdateProductPointMaster(slctProductPointMasterListData);
	}

	@Override
	public boolean schemesDefinitionFromSlct(SlctSchemesListData slctSchemesListData) {
		return slctCrmIntegrationService.schemesDefinitionFromSlct(slctSchemesListData);
	}

	@Override
	public SlctGiftRedemptionListData getGiftRedemptionForSLCT() {
		SlctGiftRedemptionListData listData = new SlctGiftRedemptionListData();
		List<SlctGiftRedemptionData> dataList = new ArrayList<>();

		List<OrderEntryModel> giftRedemptionsForSLCT = slctCrmIntegrationService.getGiftRedemptionsForSLCT();
		if (giftRedemptionsForSLCT != null && !giftRedemptionsForSLCT.isEmpty()) {
			for (OrderEntryModel orderEntryModel : giftRedemptionsForSLCT) {
				if (orderEntryModel != null) {
					SlctGiftRedemptionData data = new SlctGiftRedemptionData();
					data.setRequestId(orderEntryModel.getOrder()!=null ? orderEntryModel.getOrder().getCode():null);
					if(orderEntryModel.getOrder()!=null && orderEntryModel.getOrder().getUser()!=null) {
						data.setInfluencerCRMCode(orderEntryModel.getOrder().getUser().getUid());
					}
					data.setEntryNumber(orderEntryModel.getEntryNumber());
					data.setQuantity(Double.valueOf(orderEntryModel.getQuantity()));
					if(orderEntryModel.getProduct()!=null) {
						GiftModel product = (GiftModel) orderEntryModel.getProduct();
						data.setGiftCode(product.getGiftCode());
						data.setGiftName(product.getName());
					}
					if(orderEntryModel.getOrder()!=null && orderEntryModel.getOrder().getApprovedBy()!=null) {
						data.setTsoEmailId(orderEntryModel.getOrder().getApprovedBy().getEmail());
					}
					if(orderEntryModel.getOrder()!=null && orderEntryModel.getOrder().getOrderSentForApprovalDate()!=null) {
						data.setApprovedDate(orderEntryModel.getOrder().getOrderSentForApprovalDate().toString());
					}

					dataList.add(data);
				}
			}
		}
			listData.setGiftRedemptionData(dataList);
			return listData;
	}

	@Override
	public boolean insertUpdateGiftData(SlctGiftSchemesListData slctGiftSchemesListData) {
		return slctCrmIntegrationService.insertUpdateGiftData(slctGiftSchemesListData);
	}

	@Override
	public SlctCrmTSOMasterListData insertUpdateTsoMasterDetails(SlctCrmTSOMasterListData slctCrmTSOMasterListData) {
		return slctCrmIntegrationService.insertUpdateTSOMasterDetails(slctCrmTSOMasterListData);
	}

	@Override
	public SlctTSOSubAreaMappingListData insertUpdateTsoSubAreaMapping(SlctTSOSubAreaMappingListData slctTSOSubAreaMappingListData) {
		return slctCrmIntegrationService.insertUpdateTSOSubAreaMapping(slctTSOSubAreaMappingListData);
	}

	/**
	 * @param slctProductPointMasterListData
	 * @return
	 */
	@Override
	public boolean updateProductEquiCode(SlctProductEquivalenceDataListData slctProductPointMasterListData) {
		return slctCrmIntegrationService.updateProductEquiCode(slctProductPointMasterListData);
	}

	@Override
	public boolean syncMarketMappingIds(MarketMappingInboundSyncData marketMappingInboundSyncData) {
		return slctCrmIntegrationService.syncMarketMappingIds(marketMappingInboundSyncData);
	}

	@Override
	public boolean insertUpdateStockAllocationData(DealerInvoiceSummaryData dealerInvoiceSummaryData) {
		return slctCrmIntegrationService.insertUpdateStockAllocationData(dealerInvoiceSummaryData);
	}

	public SlctCrmSiteTransactionListData getSiteTransactionDetailsForSLCT() {

		int count = 0;
		List<String> siteCodesList = new ArrayList<>();
		SlctCrmSiteTransactionListData listData = new SlctCrmSiteTransactionListData();
		List<SlctCrmSiteTransactionData> dataList = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<SclSiteMasterModel> siteTransactionDetailsForSLCT = slctCrmIntegrationService.getSiteTransactionDetailsForSLCT();
		LOG.info(String.format("Site Transaction Integration - The total no of records processed from Query : %d ",siteTransactionDetailsForSLCT.size()));
		if (siteTransactionDetailsForSLCT != null && !siteTransactionDetailsForSLCT.isEmpty()) {
			for (SclSiteMasterModel sclSiteMaster : siteTransactionDetailsForSLCT) {
				try {
					SlctCrmSiteTransactionData data = new SlctCrmSiteTransactionData();
					populateBasicSiteData(sclSiteMaster, data);

					if (sclSiteMaster.getConstructionStage() != null) {
						data.setConstructionStage(sclSiteMaster.getConstructionStage().getCode());
					}
					if (sclSiteMaster.getSiteCategoryType() != null) {
						data.setSiteCategory(enumerationService.getEnumerationName(sclSiteMaster.getSiteCategoryType(),i18NService.getCurrentLocale()));
					}
					if (sclSiteMaster.getSiteStatus() != null) {
						data.setSiteStatus(sclSiteMaster.getSiteStatus().getCode());
					}
					if(sclSiteMaster.getCementProduct()!=null) {
						data.setBrand(sclSiteMaster.getCementProduct().getCode());
						if(sclSiteMaster.getCementProduct().getCompetitorProductType()!=null) {
							data.setCementType(enumerationService.getEnumerationName(sclSiteMaster.getCementProduct().getCompetitorProductType(),i18NService.getCurrentLocale()));
						}
					}

					data.setTotalBagsPurchased(sclSiteMaster.getNumberOfBagsPurchased());
					CustomerBasicDetailsData customerDetailedData = new CustomerBasicDetailsData();
					if (sclSiteMaster.getDealer() != null) {
						SclCustomerModel dealer = sclSiteMaster.getDealer();
						customerDetailedData.setCrmCustomerCode(dealer.getUid());
						customerDetailedData.setName(dealer.getName());
						customerDetailedData.setErpCustomerNo(dealer.getCustomerNo());
						customerDetailedData.setCustomerType(dealer.getCounterType().getCode());
					}
					data.setDealer(customerDetailedData);
					data.setRetailer(sclSiteMaster.getRetailer());
					List<B2BCustomerModel> taggedPartners = new ArrayList<B2BCustomerModel>();
					if (sclSiteMaster.getTaggedPartners() != null && !sclSiteMaster.getTaggedPartners().isEmpty()) {
						taggedPartners = (List<B2BCustomerModel>) sclSiteMaster.getTaggedPartners();
					}

					List<CustomerBasicDetailsData> customerModels = new ArrayList<>();
					if (taggedPartners != null && !taggedPartners.isEmpty()) {
						for (B2BCustomerModel taggedPartner : taggedPartners) {
							SclCustomerModel sclCustomerModel = (SclCustomerModel) userService.getUserForUID(taggedPartner.getUid());
							CustomerBasicDetailsData infDetailsData = new CustomerBasicDetailsData();
							infDetailsData.setCrmCustomerCode(sclCustomerModel.getUid());
							infDetailsData.setName(sclCustomerModel.getName());
							infDetailsData.setErpCustomerNo(sclCustomerModel.getCustomerNo());
							infDetailsData.setCustomerType(sclCustomerModel.getCounterType().getCode());
							customerModels.add(infDetailsData);
						}
					}
					data.setInfluencer(customerModels);
					data.setBagsPurchasedInCurrentMonth(sclSiteMaster.getCurrentMonthSale());
					data.setContactNumber(sclSiteMaster.getMobileNumber());
					if(sclSiteMaster.getPersonMetAtSite()!=null) {
						data.setPersonMet(sclSiteMaster.getPersonMetAtSite().getCode());
					}
					data.setBuildUpArea(sclSiteMaster.getBuiltUpArea());
					data.setNextSlabCasting(sclSiteMaster.getNextSlabCasting());
					data.setBalanceCementRequirement(sclSiteMaster.getBalanceCementRequirement());
					if(sclSiteMaster.getServiceProvidedAtSite()!=null) {
						if(sclSiteMaster.getServiceProvidedAtSite()) {
							data.setServiceProvided("YES");
						}
						else {
							data.setServiceProvided("NO");
						}
					}
					if(sclSiteMaster.getServiceType()!=null) {
						data.setServiceType(sclSiteMaster.getServiceType().getName());
					}
					data.setDateOfPurchase(sclSiteMaster.getDateOfPurchase());
					data.setRemarks(sclSiteMaster.getRemarks());
					if(sclSiteMaster.getSiteStatus()!=null) {
						if(sclSiteMaster.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
							if(sclSiteMaster.getPreviousCementProduct()!=null) {
								if(sclSiteMaster.getPreviousCementProduct().getCompetitorProductType()!=null && sclSiteMaster.getPreviousCementProduct().getCompetitorProductType().equals(CompetitorProductType.CS)) {
									data.setCompetitorProduct(sclSiteMaster.getPreviousCementProduct().getCode());
								}
							}
						}
						else {
							if(sclSiteMaster.getCementProduct()!=null) {
								if(sclSiteMaster.getCementProduct().getCompetitorProductType()!=null && sclSiteMaster.getCementProduct().getCompetitorProductType().equals(CompetitorProductType.CS)) {
									data.setCompetitorProduct(sclSiteMaster.getCementProduct().getCode());
								}
							}
						}
					}
					if(sclSiteMaster.getPreviousCementProduct()!=null && sclSiteMaster.getPreviousCementProduct().getPremiumProductType()!=null) {
						data.setProductType(enumerationService.getEnumerationName(sclSiteMaster.getPreviousCementProduct().getPremiumProductType(),i18NService.getCurrentLocale()));
					}

					if(sclSiteMaster.getSiteBagQtyMap()!=null && !sclSiteMaster.getSiteBagQtyMap().isEmpty()) {
						List<SiteTransactionData> siteTransactionDataList = new ArrayList<>();
						for (Map.Entry<Date, String> entry : sclSiteMaster.getSiteBagQtyMap().entrySet()) {
							SiteTransactionData siteTransactionData = new SiteTransactionData();
							List<String> qtyValues = Arrays.asList(entry.getValue().split(":"));
							if(qtyValues.size() == 2) {
								String premOrNonPrem = qtyValues.get(0);
								double qty = Double.parseDouble(qtyValues.get(1));
								String transactionDate = dateFormat.format(entry.getKey());

								siteTransactionData.setTransactionDate(transactionDate);
								if(premOrNonPrem.equalsIgnoreCase("PREMIUM")) {
									siteTransactionData.setPremium(qty);
								} else if (premOrNonPrem.equalsIgnoreCase("NON-PREMIUM")) {
									siteTransactionData.setNonPremium(qty);
								}
								siteTransactionDataList.add(siteTransactionData);
							}
						}
						data.setTransactionDetails(siteTransactionDataList);
					}
					else {
						continue;
					}
					dataList.add(data);
					count++;
					siteCodesList.add(sclSiteMaster.getUid());
				}
				catch (RuntimeException e) {
					LOG.error(String.format("Exception Occured in the Site Transaction integration. Site Transaction Unique field is :%s and Error Message : %s",sclSiteMaster.getUid(),e.getMessage()));
				}
			}
		}
		LOG.info(String.format("Site Transaction Integration - The total no of records processed in the API : %d", count));
		LOG.info(String.format("Site Codes processed in the Site Transaction Integration : %s",siteCodesList.toString()));
		listData.setSiteTransactionListData(dataList);
		return listData;

	}

	/**
	 * Populate Basic Site Master Data
	 * @param sclSiteMaster
	 * @param data
	 */
	private static void populateBasicSiteData(SclSiteMasterModel sclSiteMaster, SlctCrmSiteTransactionData data) {
		data.setSiteCode(sclSiteMaster.getUid());
		data.setSiteName(sclSiteMaster.getName());
		if (sclSiteMaster.getCreatedBy() != null) {
			data.setTsoEmailId(sclSiteMaster.getCreatedBy().getUid());
			data.setTsoName(sclSiteMaster.getCreatedBy().getName());
		}
		if (sclSiteMaster.getAddresses() != null && !sclSiteMaster.getAddresses().isEmpty()) {
			List<AddressModel> billingAddressList = sclSiteMaster.getAddresses().stream().toList();
			billingAddressList = billingAddressList.stream().filter(siteAddress -> (siteAddress.getBillingAddress())).collect(Collectors.toList());
			if (billingAddressList != null && !billingAddressList.isEmpty()) {
				String billAddress="";
				AddressModel billingAddress = billingAddressList.get(0);
				data.setState(billingAddress.getState());
				data.setDistrict(billingAddress.getDistrict());
				data.setCity(billingAddress.getErpCity());
				data.setTaluka(billingAddress.getTaluka());
				data.setPinCode(billingAddress.getPostalcode());
				if(!billingAddress.getLine1().isBlank()) {
					billAddress += billingAddress.getLine1();
					if(!billingAddress.getLine2().isBlank()) {
						billAddress += "," + billingAddress.getLine2();
					}
					data.setSiteAddress(billAddress);
				}
			}

		}
	}


	private Double getBucketValue(Double bucket) {
		return bucket != null ? bucket : 0.0;
	}


	@Override
	public SiteConversionListData getSiteConversionDetailsForSLCT() {
		SiteConversionListData listData = new SiteConversionListData();
		List<SiteTransactionModel> siteTransactionModelList = slctCrmIntegrationService.getSiteConversionDetailsForSLCT();
		LOG.info(String.format("Site Conversion Integration - The total no of records processed from Query : %d ",siteTransactionModelList.size()));

		List<SiteConversionData> dataList = slctSiteConversionConverter.convertAll(siteTransactionModelList);
		LOG.info(String.format("Site Conversion Integration - The total no of records processed from API : %d ",dataList.size()));

		listData.setSiteConversionListData(dataList);
		return listData;
	}

	/**
	 * Populate Basic Site Visit Details
	 * @param slctCrmSiteMasterData
	 * @param siteMasterModel
	 */
	private static void populateBasicSiteVisitDetails(SlctCrmSiteMasterData slctCrmSiteMasterData, SclSiteMasterModel siteMasterModel) {
		slctCrmSiteMasterData.setSiteCode(siteMasterModel.getUid());
		slctCrmSiteMasterData.setSiteName(siteMasterModel.getName());
		slctCrmSiteMasterData.setContactNumber(siteMasterModel.getMobileNumber());
		if (siteMasterModel.getAddresses() != null) {
			List<AddressModel> billingAddressList = siteMasterModel.getAddresses().stream().toList();
			populateSiteAddress(slctCrmSiteMasterData, billingAddressList);
		}
	}

	/**
	 * Populate Address details of the Site
	 * @param slctCrmSiteMasterData
	 * @param billingAddressList
	 */
	private static void populateSiteAddress(SlctCrmSiteMasterData slctCrmSiteMasterData, List<AddressModel> billingAddressList) {
		billingAddressList = billingAddressList.stream().filter(address -> (address.getBillingAddress())).collect(Collectors.toList());
		if (billingAddressList != null && !billingAddressList.isEmpty()) {
			AddressModel billingAddress = billingAddressList.get(0);
			slctCrmSiteMasterData.setState(billingAddress.getState());
			slctCrmSiteMasterData.setDistrict(billingAddress.getDistrict());
			slctCrmSiteMasterData.setCity(billingAddress.getErpCity());
			slctCrmSiteMasterData.setTaluka(billingAddress.getTaluka());
			slctCrmSiteMasterData.setPinCode(billingAddress.getPostalcode());
			slctCrmSiteMasterData.setLatitude(billingAddress.getLatitude());
			slctCrmSiteMasterData.setLongitude(billingAddress.getLongitude());
			if(StringUtils.isNotBlank(billingAddress.getLine1())) {
				String billAddress = billingAddress.getLine1();
				if(StringUtils.isNotBlank(billingAddress.getLine2())) {
					billAddress += "," + billingAddress.getLine2();
				}
				slctCrmSiteMasterData.setSiteAddress(billAddress);
			}
		}
	}

	@Override
	public boolean syncSiteTransactionData(SiteConversionInboundSyncData siteConversionInboundSyncData) {
		return slctCrmIntegrationService.syncSiteTransactionData(siteConversionInboundSyncData);
	}

}

