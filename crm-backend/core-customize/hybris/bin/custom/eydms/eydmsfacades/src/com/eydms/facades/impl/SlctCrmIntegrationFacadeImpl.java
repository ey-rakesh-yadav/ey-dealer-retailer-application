package com.eydms.facades.impl;

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


import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.enums.SiteStatus;
import com.eydms.core.enums.WorkflowActions;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.order.EYDMSB2BOrderService;
import com.eydms.core.services.SalesPlanningService;
import com.eydms.core.services.SchemesAndDiscountService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.TechnicalAssistanceFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.order.data.IntegrationOrderEntryData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.DeliverySlotMasterDao;
import com.eydms.core.dao.SlctCrmIntegrationDao;
import com.eydms.core.services.SlctCrmIntegrationService;
import com.eydms.facades.SlctCrmIntegrationFacade;
import com.eydms.facades.order.data.IntegrationOrderData;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
//import com.eydms.facades.populators.var;


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
	EyDmsNotificationService eydmsNotificationService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	Converter<MarketMappingDetailsModel, MarketMappingDetailsData> visitDataConverter;

	@Autowired
	Converter<MarketMappingDetailsModel, MarketMappingDetailsData> reviewLogsVisitDataConverter;

	@Resource
	private Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;

	@Autowired
	private Converter<AddressModel, AddressData> addressConverter;


	@Autowired
	Converter<MeetingCompletionFormModel, MeetingCompletionFormData> meetingCompletionFormConverter;

	@Autowired
	private Converter<MeetingScheduleModel, ScheduledMeetData> scheduledMeetConverter;

	@Autowired
	TechnicalAssistanceFacadeImpl technicalAssistanceFacadeImpl;

	@Resource
	SchemesAndDiscountService schemesAndDiscountService;

	@Autowired
	EYDMSB2BOrderService eydmsB2BOrderService;

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
					if (marketMapping.getCounterVisit().getEyDmsCustomer() != null) {
						data.setErpCustomerNo(marketMapping.getCounterVisit().getEyDmsCustomer().getCustomerNo());
						data.setCustomerName(marketMapping.getCounterVisit().getEyDmsCustomer().getName());
						data.setCrmCustomerCode(marketMapping.getCounterVisit().getEyDmsCustomer().getUid());
						data.setState(marketMapping.getCounterVisit().getEyDmsCustomer().getState());
						data.setLatitude(marketMapping.getCounterVisit().getEyDmsCustomer().getLatitude());
						data.setLongitude(marketMapping.getCounterVisit().getEyDmsCustomer().getLongitude());
						data.setMobileNumber(marketMapping.getCounterVisit().getEyDmsCustomer().getMobileNumber());
						if (marketMapping.getCounterVisit().getEyDmsCustomer().getAddresses() != null) {
							List<AddressModel> primaryAddressList = marketMapping.getCounterVisit().getEyDmsCustomer().getAddresses().stream().toList();
							primaryAddressList = primaryAddressList.stream().filter(address -> ((address.getIsPrimaryAddress() && address.getBillingAddress()) || address.getBillingAddress())).collect(Collectors.toList());
							if (primaryAddressList != null && !primaryAddressList.isEmpty()) {
								AddressModel primaryBillingAddress = primaryAddressList.get(0);
								data.setAddressLine1(primaryBillingAddress.getLine1());
								data.setAddressLine2(primaryBillingAddress.getLine2());
								data.setCity(primaryBillingAddress.getErpCity());
							}
						}

						CustomerSubAreaMappingModel customerSubAreaMappingModel = slctCrmIntegrationDao.getCustomerSubAreaMapByCustomer(marketMapping.getCounterVisit().getEyDmsCustomer());
						if (customerSubAreaMappingModel != null) {
							data.setTaluka(customerSubAreaMappingModel.getSubArea());
							data.setDistrict(customerSubAreaMappingModel.getDistrict());
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
			if (counterVisit.getEyDmsCustomer() != null) {
				counter.setCustomerNo(counterVisit.getEyDmsCustomer().getCustomerNo());
				counter.setCustomerName(counterVisit.getEyDmsCustomer().getName());
				counter.setCrmCustomerCode(counterVisit.getEyDmsCustomer().getUid());
				counter.setLatitude(counterVisit.getEyDmsCustomer().getLatitude());
				counter.setLongitude(counterVisit.getEyDmsCustomer().getLongitude());
				counter.setMobileNumber(counterVisit.getEyDmsCustomer().getMobileNumber());
				if (counterVisit.getEyDmsCustomer().getAddresses() != null) {
					List<AddressModel> primaryAddressList = counterVisit.getEyDmsCustomer().getAddresses().stream().toList();
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
		List<EyDmsCustomerModel> influencerList = slctCrmIntegrationService.getAllInfluencerDetails();
		for (EyDmsCustomerModel influencer : influencerList) {
			SlctCrmInfluencerData influencerData = new SlctCrmInfluencerData();

			influencerData.setCrmCustomerCode(influencer.getUid());
			influencerData.setName(influencer.getName());
			influencerData.setInfluencerCreationDate(influencer.getCreationtime());
			if (influencer.getInfluencerType() != null) {
				influencerData.setInfluencerType(influencer.getInfluencerType().getCode());
			}
			influencerData.setMobileNumber(influencer.getMobileNumber());
			if (influencer.getDefaultB2BUnit() != null) {
				if (influencer.getDefaultB2BUnit().getUid().equals("EyDmsOtherUnit")) {
					influencerData.setBrand("NON EYDMS");
					influencerData.setIsEyDmsInfluencer(false);
				} else {
					if (influencer.getDefaultB2BUnit().getUid().equals("EyDmsShreeUnit")) {
						influencerData.setBrand("SHREE");
					} else if (influencer.getDefaultB2BUnit().getUid().equals("EyDmsBangurUnit")) {
						influencerData.setBrand("BANGUR");
					} else if (influencer.getDefaultB2BUnit().getUid().equals("EyDmsRockstrongUnit")) {
						influencerData.setBrand("ROCKSTRONG");
					}
					influencerData.setIsEyDmsInfluencer(true);
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
			BaseSiteModel brand = null;
			DeliveryModeModel deliveryMode = null;
			DeliveryModeModel roadDeliveryModel = slctCrmIntegrationService.getTheDeliveryMode("ROAD");
			DeliveryModeModel railDeliveryModel = slctCrmIntegrationService.getTheDeliveryMode("RAIL");
			BaseSiteModel shreeBand = baseSiteService.getBaseSiteForUID("102");
			BaseSiteModel bangurBrand = baseSiteService.getBaseSiteForUID("103");
			BaseSiteModel rockStrongBrand = baseSiteService.getBaseSiteForUID("104");

			List<LPSourceMasterData> lpSourceMaster = lpSourceMasterListData.getLpSourceMaster();
			for (LPSourceMasterData lpSource : lpSourceMaster) {
				if (lpSource.getBrand() != null && lpSource.getDeliveryMode() != null) {
					if (lpSource.getBrand().equals("102")) {
						brand = shreeBand;
					} else if (lpSource.getBrand().equals("103")) {
						brand = bangurBrand;
					} else if (lpSource.getBrand().equals("104")) {
						brand = rockStrongBrand;
					}

					if (lpSource.getDeliveryMode().equals("ROAD")) {
						deliveryMode = roadDeliveryModel;
					} else if (lpSource.getDeliveryMode().equals("RAIL")) {
						deliveryMode = railDeliveryModel;
					}

					List<DestinationSourceMasterModel> destinationSourceMaster = slctCrmIntegrationService.getLpSourceMasterList(brand, deliveryMode, lpSource.getGrade(), lpSource.getPackaging(), lpSource.getDestCityId());
					if (destinationSourceMaster != null && !destinationSourceMaster.isEmpty()) {
						modelService.removeAll(destinationSourceMaster);
					}
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
			CreditAndOutstandingModel creditAndOutstanding = slctCrmIntegrationService.getCrmOutstandingDetails(outstanding.getCustomerCode());
			if (Objects.isNull(creditAndOutstanding)) {
				creditAndOutstanding = modelService.create(CreditAndOutstandingModel.class);
			}

			if (outstanding.getUpdateType().equals("B") || outstanding.getUpdateType().equals("b")) {

				Double bucket1 = outstanding.getBucket1() != null ? outstanding.getBucket1() : 0.0;
				Double bucket2 = outstanding.getBucket2() != null ? outstanding.getBucket2() : 0.0;
				Double bucket3 = outstanding.getBucket3() != null ? outstanding.getBucket3() : 0.0;
				Double bucket4 = outstanding.getBucket4() != null ? outstanding.getBucket4() : 0.0;
				Double bucket5 = outstanding.getBucket5() != null ? outstanding.getBucket5() : 0.0;
				Double bucket6 = outstanding.getBucket6() != null ? outstanding.getBucket6() : 0.0;
				Double bucket7 = outstanding.getBucket7() != null ? outstanding.getBucket7() : 0.0;
				Double bucket8 = outstanding.getBucket8() != null ? outstanding.getBucket8() : 0.0;
				Double bucket9 = outstanding.getBucket9() != null ? outstanding.getBucket9() : 0.0;
				Double bucket10 = outstanding.getBucket10() != null ? outstanding.getBucket10() : 0.0;

				creditAndOutstanding.setBucket1(bucket1);
				creditAndOutstanding.setBucket2(bucket2);
				creditAndOutstanding.setBucket3(bucket3);
				creditAndOutstanding.setBucket4(bucket4);
				creditAndOutstanding.setBucket5(bucket5);
				creditAndOutstanding.setBucket6(bucket6);
				creditAndOutstanding.setBucket7(bucket7);
				creditAndOutstanding.setBucket8(bucket8);
				creditAndOutstanding.setBucket9(bucket9);
				creditAndOutstanding.setBucket10(bucket10);
				creditAndOutstanding.setCustomerCode(outstanding.getCustomerCode());

				Double totalOutstanding = bucket1 + bucket2 + bucket3 + bucket4 + bucket5 + bucket6 + bucket7 + bucket8 + bucket9 + bucket10;
				creditAndOutstanding.setTotalOutstanding(totalOutstanding);
				creditAndOutstanding.setNetOutstanding(totalOutstanding);
			}
			if (outstanding.getUpdateType().equals("O") || outstanding.getUpdateType().equals("o")) {
				if (creditAndOutstanding.getCreditLimit() != null && creditAndOutstanding.getCreditLimit() != outstanding.getCreditLimit()) {
					creditLimitFlag = true;
				}
				creditLimitMap.put(creditAndOutstanding.getCustomerCode(), creditLimitFlag);
				creditAndOutstanding.setCreditLimit(outstanding.getCreditLimit());
				creditAndOutstanding.setCustomerCode(outstanding.getCustomerCode());
				creditAndOutstanding.setDailyAverageSales(outstanding.getDailyAverageSales());
				creditAndOutstanding.setDso(outstanding.getDso());
				creditAndOutstanding.setLastUpdatedDate(outstanding.getLastUpdateDate());
				if (creditAndOutstanding.getSecurityDeposit() != null && creditAndOutstanding.getSecurityDeposit() != outstanding.getSecurityDeposit()) {
					securityDepositFlag = true;
				}
				securityDepositMap.put(creditAndOutstanding.getCustomerCode(), securityDepositFlag);
				creditAndOutstanding.setSecurityDeposit(outstanding.getSecurityDeposit());
//				creditAndOutstanding.setTotalOutstanding(outstanding.getTotalOutstanding());
//				creditAndOutstanding.setNetOutstanding(outstanding.getTotalOutstanding());
				this.insertOutstandingHistoryDetails(outstanding);
			}
			modelList.add(creditAndOutstanding);
		}
		modelService.saveAll(modelList);
//		try {
//			for (Map.Entry<String, Boolean> entry : creditLimitMap.entrySet()) {
//				String key = entry.getKey();
//				Boolean value = entry.getValue();
//				if (value != null && value) {
//					if (key != null) {
////						EyDmsCustomerModel model = (EyDmsCustomerModel) userService.getUserForUID(key);
//						EyDmsCustomerModel model = slctCrmIntegrationDao.getCustomerByCustNo(key);
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
//							eydmsNotificationService.submitLimitNotification(model, model, body, subject, category);
//							EyDmsUserModel so = territoryManagementService.getSOforCustomer(model);
//							eydmsNotificationService.submitLimitNotification(model, so, body, subject, category);
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
//						EyDmsCustomerModel model = (EyDmsCustomerModel) userService.getUserForUID(key);
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
//						eydmsNotificationService.submitLimitNotification(model, model, body, subject, category);
//						EyDmsUserModel so = territoryManagementService.getSOforCustomer(model);
//						eydmsNotificationService.submitLimitNotification(model, so, body, subject, category);
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
			if (orderLineModel.getExpectedDeliveryslot() != null) {
				String startTime = deliverySlots.stream().filter(slot -> slot.getSlot().getCode().equals(orderLineModel.getExpectedDeliveryslot().getCode())).findAny().get().getStart();
				orderLineData.setExpectedDeliverySlot(startTime);
			}

			if (orderLineModel.getOrder() != null) {
				orderLineData.setCrmOrderNumber(orderLineModel.getOrder().getCode());
				orderLineData.setErpOrderNumber(orderLineModel.getOrder().getErpOrderNumber());
				if (orderLineModel.getOrder().getRetailer() != null) {
					orderLineData.setCrmRetailerCode(orderLineModel.getOrder().getRetailer().getCustomerNo());
				}
				if (orderLineModel.getOrder().getUser() != null) {
					orderLineData.setErpDealerCode(((EyDmsCustomerModel) orderLineModel.getOrder().getUser()).getCustomerNo());
					if (((EyDmsCustomerModel) orderLineModel.getOrder().getUser()).getDealerCategory() != null && !((EyDmsCustomerModel) orderLineModel.getOrder().getUser()).getDealerCategory().getCode().isEmpty()) {
						orderLineData.setDealerCategory(((EyDmsCustomerModel) orderLineModel.getOrder().getUser()).getDealerCategory().getCode());
					}
				}
				orderLineData.setEpodCompleted(orderLineModel.getEpodCompleted());
				if (orderLineModel.getOrder().getDeliveryAddress() != null) {
					orderLineData.setErpCity(orderLineModel.getOrder().getDeliveryAddress().getErpCity());
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
				EyDmsCustomerModel customerModel = (EyDmsCustomerModel) customer.get(0);
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
					if (csamModel.getBrand().getUid().equals("102")) {
						customerData.setBrand("SHREE");
					}
					if (csamModel.getBrand().getUid().equals("103")) {
						customerData.setBrand("BANGUR");
					}
					if (csamModel.getBrand().getUid().equals("104")) {
						customerData.setBrand("ROCKSTRONG");
					}
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

		resultOutput.setEyDmsCustomerMarketMapping(result);
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
						EyDmsCustomerModel customerModel = (EyDmsCustomerModel) userService.getUserForUID(dealerPlannedMonthlySalesModel.getCustomerCode());
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
		List<OrderEntryModel> orderEntryModelList = slctCrmIntegrationService.getOrderLinesForEpod();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (OrderEntryModel orderEntryModel : orderEntryModelList) {
			if (orderEntryModel.getErpLineItemId() == null) {
				continue;
			}

			SlctEpodOutboundData slctEpodOutboundData = new SlctEpodOutboundData();

			slctEpodOutboundData.setOrderCode(orderEntryModel.getOrder().getCode());
			slctEpodOutboundData.setOrderLineId(orderEntryModel.getErpLineItemId());
			slctEpodOutboundData.setDeliveryId(orderEntryModel.getDiNumber());

			if (orderEntryModel.getTruckReachedDate() != null) {
				slctEpodOutboundData.setTruckReachedDate(dateFormat.format(orderEntryModel.getTruckReachedDate()));
			}

			if (orderEntryModel.getEpodInitiateDate() != null) {
				slctEpodOutboundData.setEpodInitiateDate(dateFormat.format(orderEntryModel.getEpodInitiateDate()));
			}

			if (orderEntryModel.getEpodCompletedDate() != null) {
				slctEpodOutboundData.setEpodCompletedDate(dateFormat.format(orderEntryModel.getEpodCompletedDate()));
			}

			if (orderEntryModel.getEpodFeedback() != null) {
				if (orderEntryModel.getEpodFeedback().get("overallDeliveryExperience") != null && !orderEntryModel.getEpodFeedback().get("overallDeliveryExperience").isEmpty()) {
					String overallFeedbackRating = orderEntryModel.getEpodFeedback().get("overallDeliveryExperience");
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
			if (monthlySalesModel.getActionPerformed() != null && monthlySalesModel.getActionPerformed().equals(WorkflowActions.APPROVED) && monthlySalesModel.getActionPerformedBy() != null && monthlySalesModel.getActionPerformedBy().getUserType() != null && monthlySalesModel.getActionPerformedBy().getUserType().equals(EyDmsUserType.RH)) {
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
			if (annualSalesModel.getActionPerformed() != null && annualSalesModel.getActionPerformed().equals(WorkflowActions.APPROVED) && annualSalesModel.getActionPerformedBy() != null && annualSalesModel.getActionPerformedBy().getUserType() != null && annualSalesModel.getActionPerformedBy().getUserType().equals(EyDmsUserType.RH)) {
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
					EyDmsCustomerModel customer = (EyDmsCustomerModel) orderModel.getUser();
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
				orderData.setIsErpOrder(orderModel.getIsErpOrder());
				if (orderModel.getSite() != null) {
					orderData.setBrand(orderModel.getSite().getUid());
				}
				if (orderModel.getDeliveryAddress() != null && orderModel.getDeliveryAddress().getShippingAddress()) {
					orderData.setErpCity(orderModel.getDeliveryAddress().getErpCity());
				}
				if (orderModel.getPlacedBy() != null) {
					orderData.setPlacedByUid(orderModel.getPlacedBy().getUid());
					if (orderModel.getPlacedBy() instanceof EyDmsUserModel) {
						EyDmsUserModel eydmsUserModel = (EyDmsUserModel) orderModel.getPlacedBy();
						if (eydmsUserModel.getUserType() != null) {
							orderData.setPlacedByUserType(eydmsUserModel.getUserType().getCode());
						}
					} else if (orderModel.getPlacedBy() instanceof EyDmsCustomerModel) {
						EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel) orderModel.getPlacedBy();
						if (eydmsCustomerModel.getCounterType() != null) {
							orderData.setPlacedByUserType(eydmsCustomerModel.getCounterType().getCode());
							orderData.setPlacedByCustomerNo(eydmsCustomerModel.getCustomerNo());
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
				orderEntryData.setExpectedDeliverySlot(orderEntryModel.getExpectedDeliveryslot().getCode());
			}
			if (orderEntryModel.getCalculatedDeliveryslot() != null) {
				orderEntryData.setCalculatedDeliverySlot(orderEntryModel.getCalculatedDeliveryslot().getCode());
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
					if (model.getEyDmsCustomer() != null) {
						data.setName(model.getEyDmsCustomer().getName());
						data.setCode(model.getEyDmsCustomer().getUid());
						if (model.getEyDmsCustomer().getInfluencerType() != null) {
							data.setTypeOfInfluencer(model.getEyDmsCustomer().getInfluencerType().getCode());
						}
						data.setMobileNumber(model.getEyDmsCustomer().getMobileNumber());
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
		List<EyDmsSiteMasterModel> siteMasterDetailsForSLCT = slctCrmIntegrationService.getSiteMasterDetailsForSLCT();
		if (siteMasterDetailsForSLCT != null && !siteMasterDetailsForSLCT.isEmpty()) {
			for (EyDmsSiteMasterModel eydmsSiteMaster : siteMasterDetailsForSLCT) {
				SlctCrmSiteMasterData data = new SlctCrmSiteMasterData();
				data.setSiteCode(eydmsSiteMaster.getUid());
				data.setSiteName(eydmsSiteMaster.getName());
				if (eydmsSiteMaster.getCreatedBy() != null) {
					data.setTsoEmailId(eydmsSiteMaster.getCreatedBy().getUid());
					data.setTsoName(eydmsSiteMaster.getCreatedBy().getName());
				}
				if (eydmsSiteMaster.getAddresses() != null && !eydmsSiteMaster.getAddresses().isEmpty()) {
					List<AddressModel> addresses = (List<AddressModel>) eydmsSiteMaster.getAddresses();
					if (addresses != null && !addresses.isEmpty()) {
						AddressModel addressModel = addresses.get(0);
						if (addressModel != null) {
							data.setDistrict(addressModel.getDistrict());
							data.setState(addressModel.getState());
							data.setTaluka(addressModel.getTaluka());
							data.setCity(addressModel.getErpCity());
						}
					}
				}
				if (eydmsSiteMaster.getConstructionStage() != null) {
					data.setConstructionStage(eydmsSiteMaster.getConstructionStage().getCode());
				}
				if (eydmsSiteMaster.getSiteCategoryType() != null) {
					data.setSiteCategory(eydmsSiteMaster.getSiteCategoryType().getName());
				}
				if (eydmsSiteMaster.getSiteStatus() != null) {
					data.setSiteStatus(eydmsSiteMaster.getSiteStatus().getCode());
				}
				if (eydmsSiteMaster.getCementType() != null) {
					data.setCementType(eydmsSiteMaster.getCementType().getName());
				}
				if (eydmsSiteMaster.getCementBrand() != null) {
					data.setBrand(eydmsSiteMaster.getCementBrand().getName());
				}
				data.setTotalBagsPurchased(eydmsSiteMaster.getNumberOfBagsPurchased());
				CustomerBasicDetailsData customerDetailedData = new CustomerBasicDetailsData();
				if (eydmsSiteMaster.getDealer() != null) {
					EyDmsCustomerModel dealer = eydmsSiteMaster.getDealer();
					customerDetailedData.setCrmCustomerCode(dealer.getUid());
					customerDetailedData.setName(dealer.getName());
					customerDetailedData.setErpCustomerNo(dealer.getCustomerNo());
					customerDetailedData.setCustomerType(dealer.getCounterType().getCode());
				}
				data.setDealer(customerDetailedData);
				data.setRetailer(eydmsSiteMaster.getRetailer());
				List<B2BCustomerModel> taggedPartners = new ArrayList<B2BCustomerModel>();
				if (eydmsSiteMaster.getTaggedPartners() != null && !eydmsSiteMaster.getTaggedPartners().isEmpty()) {
					taggedPartners = (List<B2BCustomerModel>) eydmsSiteMaster.getTaggedPartners();
				}

				List<CustomerBasicDetailsData> customerModels = new ArrayList<>();
				if (taggedPartners != null && !taggedPartners.isEmpty()) {
					for (B2BCustomerModel taggedPartner : taggedPartners) {
						EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel) userService.getUserForUID(taggedPartner.getUid());
						CustomerBasicDetailsData infDetailsData = new CustomerBasicDetailsData();
						infDetailsData.setCrmCustomerCode(eydmsCustomerModel.getUid());
						infDetailsData.setName(eydmsCustomerModel.getName());
						infDetailsData.setErpCustomerNo(eydmsCustomerModel.getCustomerNo());
						infDetailsData.setCustomerType(eydmsCustomerModel.getCounterType().getCode());
						customerModels.add(infDetailsData);
					}
				}
				data.setInfluencer(customerModels);
				data.setBagsPurchasedInCurrentMonth(eydmsSiteMaster.getCurrentMonthSale());
				dataList.add(data);
			}
		}
		listData.setSlctCrmSiteMasterData(dataList);
		return listData;
	}

	@Override
	public SlctCrmSiteMasterListData getSiteVisitForms() {
		SlctCrmSiteMasterListData resultOutput = new SlctCrmSiteMasterListData();
		List<SlctCrmSiteMasterData> result = new ArrayList<>();
		List<SiteVisitMasterModel> siteVisitMasterModelList = slctCrmIntegrationService.getSiteVisitForms();
		for (SiteVisitMasterModel siteVisitMasterModel : siteVisitMasterModelList) {
			SlctCrmSiteMasterData siteVisitData = new SlctCrmSiteMasterData();
			siteVisitData.setVisitId(siteVisitMasterModel.getId());
			if (siteVisitMasterModel.getEyDmsCustomer() != null) {
				siteVisitData.setSiteCode(siteVisitMasterModel.getEyDmsCustomer().getUid());
				siteVisitData.setSiteName(siteVisitMasterModel.getEyDmsCustomer().getName());
				if (siteVisitMasterModel.getEyDmsCustomer().getAddresses() != null) {
					List<AddressModel> billingAddressList = siteVisitMasterModel.getEyDmsCustomer().getAddresses().stream().toList();
					billingAddressList = billingAddressList.stream().filter(address -> (address.getBillingAddress())).collect(Collectors.toList());
					if (billingAddressList != null && !billingAddressList.isEmpty()) {
						AddressModel billingAddress = billingAddressList.get(0);
						siteVisitData.setState(billingAddress.getState());
						siteVisitData.setDistrict(billingAddress.getDistrict());
						siteVisitData.setCity(billingAddress.getErpCity());
						siteVisitData.setTaluka(billingAddress.getTaluka());
					}
				}
			}
			if (siteVisitMasterModel.getVisit() != null && siteVisitMasterModel.getVisit().getUser() != null) {
				siteVisitData.setTsoEmailId(siteVisitMasterModel.getVisit().getUser().getUid());
				siteVisitData.setTsoName(siteVisitMasterModel.getVisit().getUser().getName());
			}
			if (siteVisitMasterModel.getSiteStatus() != null) {
				if (siteVisitMasterModel.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
					if (siteVisitMasterModel.getConvertedToCementType() != null) {
						siteVisitData.setCementType(siteVisitMasterModel.getConvertedToCementType().getName());
					}
					if (siteVisitMasterModel.getConvertedToBrand() != null) {
						siteVisitData.setBrand(siteVisitMasterModel.getConvertedToBrand().getName());
					}
				} else if (siteVisitMasterModel.getSiteStatus().equals(SiteStatus.SITE_UPGRADED)) {
					if (siteVisitMasterModel.getUpgradeToCementType() != null) {
						siteVisitData.setCementType(siteVisitMasterModel.getUpgradeToCementType().getName());
					}
					if (siteVisitMasterModel.getUpgradeToBrand() != null) {
						siteVisitData.setBrand(siteVisitMasterModel.getUpgradeToBrand().getName());
					}
				} else {
					if (siteVisitMasterModel.getCementType() != null) {
						siteVisitData.setCementType(siteVisitMasterModel.getCementType().getName());
					}
					if (siteVisitMasterModel.getCementBrand() != null) {
						siteVisitData.setBrand(siteVisitMasterModel.getCementBrand().getName());
					}
				}
			}
			if (siteVisitMasterModel.getSiteStatus() != null) {
				siteVisitData.setSiteStatus(siteVisitMasterModel.getSiteStatus().getCode());
			}
			if (siteVisitMasterModel.getSiteCategoryType() != null) {
				siteVisitData.setSiteCategory(siteVisitMasterModel.getSiteCategoryType().getName());
			}
			siteVisitData.setVisitDate(siteVisitMasterModel.getEndVisitTime());
			result.add(siteVisitData);
		}
		resultOutput.setSlctCrmSiteMasterData(result);
		return resultOutput;
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

}