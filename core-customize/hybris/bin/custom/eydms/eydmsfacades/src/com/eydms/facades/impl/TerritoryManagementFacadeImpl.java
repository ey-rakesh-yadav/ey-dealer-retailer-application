package com.eydms.facades.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.*;
import com.eydms.facades.data.*;
import com.eydms.facades.network.impl.EYDMSNetworkFacadeImpl;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.GenericSearchConstants;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.TerritoryManagementFacade;
import com.eydms.facades.prosdealer.data.CustomerListData;
import com.eydms.facades.prosdealer.data.DealerListData;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class TerritoryManagementFacadeImpl implements TerritoryManagementFacade {

	private static final Logger LOGGER = Logger.getLogger(TerritoryManagementFacadeImpl.class);
	@Autowired
	TerritoryManagementService territoryManagementService;
	@Autowired
	Converter<AddressModel, AddressData> addressConverter;

	@Autowired
	UserService userService;
	
	@Autowired
	Converter<EyDmsCustomerModel,CustomerData> dealerBasicConverter;
	
	@Autowired
	Converter<EyDmsUserModel,EyDmsUserData> eydmsUserConverter;
		
	@Override
	public List<String> getAllSubAreaForSO(String userId) {
		return territoryManagementService.getAllSubAreaForSO(userId);
	}

	@Override
	public List<String> getAllSubAreaForCustomer(String customerId) {
		return territoryManagementService.getAllSubAreaForCustomer(customerId);
	}
	
	@Override
	public DealerListData getAllCustomerForSubArea() {
		//New Territory Change
		List<EyDmsCustomerModel> customerList = territoryManagementService.getAllCustomerForSO();
		List<CustomerData> allData = new ArrayList<CustomerData>();
		if(customerList!=null && !customerList.isEmpty()) {
			allData=Optional.of(customerList.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		DealerListData dataList = new DealerListData();
		dataList.setDealers(allData);
		return dataList;
	}
	
	@Override
	public DealerListData getAllDealersForSubArea() {
		//New Territory Change
		List<EyDmsCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
		List<CustomerData> dealerData = new ArrayList<CustomerData>();
		if(dealerList!=null && !dealerList.isEmpty()) {
			dealerData=Optional.of(dealerList.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		DealerListData dataList = new DealerListData();
		dataList.setDealers(dealerData);
		return dataList;
	}

	@Override
	public DealerListData getAllRetailersForSubArea() {
		//New Territory Change
		List<EyDmsCustomerModel> retailerList = territoryManagementService.getRetailersForSubArea();
		List<CustomerData> retailerData = new ArrayList<CustomerData>();
		if(retailerList!=null && !retailerList.isEmpty()) {
			retailerData=Optional.of(retailerList.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		DealerListData dataList = new DealerListData();
		dataList.setDealers(retailerData);
		return dataList;
	}
	
	@Override
	public DealerListData getAllInfluencersForSubArea() {
		//New Territory Change
		List<EyDmsCustomerModel> influencerList = territoryManagementService.getInfluencersForSubArea();
		List<CustomerData> influencerData = new ArrayList<CustomerData>();
		if(influencerList!=null && !influencerList.isEmpty()) {
			influencerData=Optional.of(influencerList.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		DealerListData dataList = new DealerListData();
		dataList.setDealers(influencerData);
		return dataList;
	}

	@Override
	public DealerListData getAllSitesForSubArea() {
		//New Territory Change
		List<EyDmsCustomerModel> siteList = territoryManagementService.getSitesForSubArea();
		List<CustomerData> siteData = new ArrayList<CustomerData>();
		if(siteList!=null && !siteList.isEmpty()) {
			siteData=Optional.of(siteList.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		DealerListData dataList = new DealerListData();
		dataList.setDealers(siteData);
		return dataList;
	}

	@Override
	public DealerListData getAllRetailersForSubAreaTOP(String subArea, String dealerCode) {
		List<EyDmsCustomerModel> retailerList = territoryManagementService.getAllRetailersForSubAreaTOP(subArea,dealerCode);
		List<CustomerData> retailerData = new ArrayList<CustomerData>();
		if(retailerList!=null && !retailerList.isEmpty()) {
			retailerData=Optional.of(retailerList.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		DealerListData dataList = new DealerListData();
		dataList.setDealers(retailerData);
		return dataList;
	}

	@Override
	public TerritoryListData getAllTerritoryForSO(String subArea) {
		TerritoryListData listData = new TerritoryListData();
		List<TerritoryData> dataList = new ArrayList<TerritoryData>();
		List<List<Object>> list = territoryManagementService.getAllTerritoryForSO();
		for(List<Object> objList: list) {
			String state="";
			String district="";
			String taluka="";
			if(objList!=null) {
				if(objList.size()>0 && objList.get(0)!=null) {
					state = (String) objList.get(0);
				}
				if(objList.size()>1 && objList.get(1)!=null) {
					district = (String) objList.get(1);
				}
				if(objList.size()>2 && objList.get(2)!=null) {
					taluka = (String) objList.get(2);
				}
				TerritoryData data = new TerritoryData();
				data.setState(state);
				data.setDistrict(district);
				data.setSubArea(taluka);
				dataList.add(data);
			}
		}
		if(StringUtils.isNotBlank(subArea)) {
			dataList = dataList.stream().filter(each->each.getSubArea()!=null && each.getSubArea().equals(subArea)).collect(Collectors.toList());
		}
		listData.setTerritoryList(dataList);
		return listData;
	}

	@Override
	public DealerListData getAllSalesOfficersByState(String state) {
		List<EyDmsUserModel> salesOfficerList = territoryManagementService.getAllSalesOfficersByState(state);
		DealerListData dataList = new DealerListData();
		if(salesOfficerList!=null && !salesOfficerList.isEmpty())
		{
			List<CustomerData> salesOfficerDataList=new ArrayList<>();
			for (EyDmsUserModel eydmsUserModel : salesOfficerList) {
				CustomerData customerData=new CustomerData();
				customerData.setUid(eydmsUserModel.getUid());
				customerData.setName(eydmsUserModel.getName());
				customerData.setContactNumber(eydmsUserModel.getMobileNumber());
				salesOfficerDataList.add(customerData);
			}
			dataList.setDealers(salesOfficerDataList);
		}
		return dataList;
	}

	@Override
	public List<String> getAllStatesForSO() {
		return territoryManagementService.getAllStatesForSO();
	}

	@Override
	public TerritoryListData getTerritoriesForSO() {
		TerritoryListData listData = new TerritoryListData();
		List<SubAreaData> list = new ArrayList<SubAreaData>();
		List<SubAreaMasterModel> subAreaListModel = territoryManagementService.getTerritoriesForSO();
		if(subAreaListModel!=null && !subAreaListModel.isEmpty()) {
			for(SubAreaMasterModel subArea : subAreaListModel) {
				SubAreaData subAreaData = new SubAreaData();
				subAreaData.setId(subArea.getPk().toString());
				subAreaData.setDistrict(subArea.getDistrict());
				subAreaData.setTaluka(subArea.getTaluka());
				list.add(subAreaData);
			}
			
		}
		List<SubAreaData> sortedList  = list.stream().sorted(Comparator.comparing(SubAreaData::getTaluka)).collect(Collectors.toList());

		listData.setSubAreas(sortedList);
		return listData;
	}

	@Override
	public DealerListData getRetailerListForDealer() {
		DealerListData data=new DealerListData();
		List<CustomerData> customerDataList=new ArrayList<>();
		List<EyDmsCustomerModel> retailerListForDealer = territoryManagementService.getRetailerListForDealer();
		if(retailerListForDealer!=null && !retailerListForDealer.isEmpty()) {
			 customerDataList = Optional.of(retailerListForDealer.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		data.setDealers(customerDataList);
		return data;
	}

	@Override
	public DealerListData getInfluencerListForDealer() {
		List<EyDmsCustomerModel> influencerListForDealer = territoryManagementService.getInfluencerListForDealer();
		List<CustomerData> customerDataList=new ArrayList<>();
		DealerListData data=new DealerListData();
		if(influencerListForDealer!=null && !influencerListForDealer.isEmpty()) {
			customerDataList = Optional.of(influencerListForDealer.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		data.setDealers(customerDataList);
		return data;
	}

	@Override
	public DealerListData getInfluencerListForRetailer() {
		List<EyDmsCustomerModel> influencerListForRetailer = territoryManagementService.getInfluencerListForRetailer();
		CustomerData customerData=new CustomerData();
		List<CustomerData> customerDataList=new ArrayList<>();
		DealerListData data=new DealerListData();
		if(influencerListForRetailer!=null && !influencerListForRetailer.isEmpty()) {
			customerDataList = Optional.of(influencerListForRetailer.stream()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		data.setDealers(customerDataList);
		return data;
	}
	@Override
	public SearchPageData<CustomerData> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter, Boolean isTop) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<EyDmsCustomerModel> retailerListForDealer = territoryManagementService.getRetailerListForDealerPagination(searchPageData, networkType, isNew, filter, isTop);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> eydmsCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(eydmsCustomerData);
		return result;
	}
	@Override
	public SearchPageData<CustomerData> getInfluencerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
			String influencerType, String dealerCategory) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<EyDmsCustomerModel> retailerListForDealer = territoryManagementService.getInfluencerListForDealerPagination(searchPageData, networkType, isNew, filter, influencerType, dealerCategory);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> eydmsCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(eydmsCustomerData);
		return result;
	}
	@Override
	public SearchPageData<CustomerData> getInfluencerListForRetailerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
			String influencerType, String dealerCategory) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<EyDmsCustomerModel> retailerListForDealer = territoryManagementService.getInfluencerListForRetailerPagination(searchPageData, networkType, isNew, filter, influencerType, dealerCategory);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> eydmsCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(eydmsCustomerData);
		return result;
	}



	@Override
	public Integer getDealerCountForRetailer() {
		return territoryManagementService.getDealerCountForRetailer();
	}

	@Override
	public Integer getRetailerCountForDealer() {
		return territoryManagementService.getRetailerCountForDealer();
	}

	@Override
	public Integer getInfluencerCountForDealer() {
		return territoryManagementService.getInfluencerCountForDealer();
	}

	@Override
	public SearchPageData<CustomerData> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<EyDmsCustomerModel> retailerListForDealer = territoryManagementService.getDealerListForRetailerPagination(searchPageData,filter);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> eydmsCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(eydmsCustomerData);
		return result;
	}

	@Override
	public TerritoryListData getTerritoriesForCustomer() {
		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
		TerritoryListData listData = new TerritoryListData();
		List<SubAreaData> list = new ArrayList<SubAreaData>();
		List<SubAreaMasterModel> territoriesForCustomer = territoryManagementService.getTerritoriesForCustomer(currentUser);
		if(territoriesForCustomer!=null && !territoriesForCustomer.isEmpty()) {
			for(SubAreaMasterModel subArea : territoriesForCustomer) {
				SubAreaData subAreaData = new SubAreaData();
				subAreaData.setId(subArea.getPk().toString());
				subAreaData.setDistrict(subArea.getDistrict());
				subAreaData.setTaluka(subArea.getTaluka());
				list.add(subAreaData);
			}

		}
		listData.setSubAreas(list);
		return listData;
	}
	
	@Override
	public Integer getInfluencerCountForRetailer() {
		return territoryManagementService.getInfluencerCountForRetailer();
	}

	@Override
	public TerritoryListData getTalukaForUser(FilterTalukaData filterTalukaData) {
		TerritoryListData listData = new TerritoryListData();
		List<SubAreaData> list = new ArrayList<SubAreaData>();
		List<SubAreaMasterModel> subAreaListModel = territoryManagementService.getTaulkaForUser(filterTalukaData);
		if(subAreaListModel!=null && !subAreaListModel.isEmpty()) {
			for(SubAreaMasterModel subArea : subAreaListModel) {
				if(subArea!=null) {
					SubAreaData subAreaData = new SubAreaData();
					subAreaData.setId(subArea.getPk().toString());
					subAreaData.setDistrict(subArea.getDistrict());
					subAreaData.setTaluka(subArea.getTaluka());
					list.add(subAreaData);
				}
			}
		}
		listData.setSubAreas(list);
		return listData;
	}

	@Override
	public DistrictMasterListData getDistrictForUser(FilterDistrictData filterDistrictData) {
		DistrictMasterListData districtMasterListData = new DistrictMasterListData();
		List<DistrictMasterData> districtMasterList = new ArrayList<>();
		List<DistrictMasterModel> districtMasterModels = territoryManagementService.getDistrictForUser(filterDistrictData);
		if(districtMasterModels!=null && !districtMasterModels.isEmpty()) {
			for(DistrictMasterModel districtMasterModel : districtMasterModels) {
				DistrictMasterData districtMasterData = new DistrictMasterData();
				districtMasterData.setId(districtMasterModel.getPk().toString());
				districtMasterData.setDistrictCode(districtMasterModel.getCode());
				districtMasterData.setDistrictName(districtMasterModel.getName());
				districtMasterList.add(districtMasterData);
			}
		}
		districtMasterListData.setDistricts(districtMasterList);
		return districtMasterListData;
	}

	@Override
	public SOSubAreaMappingListData getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData) {
		List<SOSubAreaMappingData> soSubAreaMappingDataList=new ArrayList<>();
		SOSubAreaMappingListData soSubAreaMappingListData=new SOSubAreaMappingListData();
		List<UserSubAreaMappingModel> subAreaListModel = territoryManagementService.getSOSubAreaMappingForUser(filterTalukaData);
		if(subAreaListModel!=null && !subAreaListModel.isEmpty()) {
			for(UserSubAreaMappingModel subArea : subAreaListModel) {
				if(subArea.getEyDmsUser()!=null) {
					SOSubAreaMappingData soSubAreaMappingData = new SOSubAreaMappingData();
					//RH & TSM - SO subarea master
					soSubAreaMappingData.setUserCode(subArea.getEyDmsUser().getUid());
					soSubAreaMappingData.setUserName(subArea.getEyDmsUser().getName());
					soSubAreaMappingData.setSubAreaName(subArea.getSubAreaMaster().getTaluka());
					soSubAreaMappingData.setSubAreaPk(subArea.getSubAreaMaster().getPk().toString());
					soSubAreaMappingData.setDistrictName(subArea.getSubAreaMaster().getDistrict());
					soSubAreaMappingDataList.add(soSubAreaMappingData);
				}
			}
		}
		soSubAreaMappingListData.setSoSubAreaMappingData(soSubAreaMappingDataList);
		return soSubAreaMappingListData;
	}

	@Override
	public TSMDistrictMappingListData getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData) {
		List<TSMDistrictMappingData> tsmDistrictMappingDataList=new ArrayList<>();
		TSMDistrictMappingListData tsmDistrictMappingListData=new TSMDistrictMappingListData();
		List<TsmDistrictMappingModel> tsmDistrcitMappingForUser = territoryManagementService.getTSMDistrcitMappingForUser(filterDistrictData);
		if(tsmDistrcitMappingForUser!=null && !tsmDistrcitMappingForUser.isEmpty()) {
			for(TsmDistrictMappingModel subArea : tsmDistrcitMappingForUser) {
				TSMDistrictMappingData tsmDistrictMappingData = new TSMDistrictMappingData();
				// TSM - Distric master
				tsmDistrictMappingData.setUserCode(subArea.getTsmUser().getUid());
				tsmDistrictMappingData.setUserName(subArea.getTsmUser().getName());
				tsmDistrictMappingData.setDistrictCode(subArea.getDistrict().getCode());
				tsmDistrictMappingData.setDistrictPk(subArea.getDistrict().getPk().toString());
				tsmDistrictMappingData.setDistrictName(subArea.getDistrict().getName());
				tsmDistrictMappingDataList.add(tsmDistrictMappingData);
			}
		}
		tsmDistrictMappingListData.setTSMDistrictMappingData(tsmDistrictMappingDataList);
		return tsmDistrictMappingListData;
	}

	@Override
	public RegionListData getRegionsForUser(FilterRegionData filterRegionData) {
		RegionListData regionListData = new RegionListData();
		List<RegionData> regionList = new ArrayList<>();
		List<RegionMasterModel> regionMasterList =  territoryManagementService.getRegionsForUser(filterRegionData);
		if(regionMasterList!=null && !regionMasterList.isEmpty()){
			for(RegionMasterModel list: regionMasterList){
				RegionData regionData = new RegionData();
				regionData.setId(list.getPk().toString());
				regionData.setName(list.getName());
				regionData.setCode(list.getCode());
				regionList.add(regionData);
			}
		}
		regionListData.setRegions(regionList);
		return regionListData;
	}

	@Override
	public CustomerListData getCustomerForUser(RequestCustomerData customerData) {
		List<EyDmsCustomerModel> customerList = territoryManagementService.getCustomerforUser(customerData);
		List<CustomerData> allData = new ArrayList<CustomerData>();
		if(customerList!=null && !customerList.isEmpty()) {
			
			for(EyDmsCustomerModel source: customerList) {
				CustomerData target = new CustomerData();
				target.setUid(source.getUid());
				target.setName(source.getName());
				target.setEmail(source.getEmail());
				target.setState(source.getState());
				target.setCustomerId(source.getCustomerNo());
				target.setContactNumber(source.getMobileNumber());
				target.setDealerCategory(source.getDealerCategory()!=null?source.getDealerCategory().getCode():"");
				if(source.getCounterType()!=null) {
					target.setPartnerType(source.getCounterType().getCode());
				}
//				if(null!=source.getProfilePicture()){
//					populateProfilePicture(source.getProfilePicture(),target);
//				}
				allData.add(target);
			}
		}
		CustomerListData dataList = new CustomerListData();
		dataList.setCustomers(allData);
		return dataList;
	}

	@Override
	public SearchPageData<CustomerData> getCustomerForUserPagination(SearchPageData searchPageData,
			RequestCustomerData customerData) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<EyDmsCustomerModel> customerListResult = territoryManagementService.getCustomerForUserPagination(searchPageData, customerData);
		result.setPagination(customerListResult.getPagination());
		result.setSorts(customerListResult.getSorts());
		List<CustomerData> allData = new ArrayList<CustomerData>();
		if(customerListResult.getResults()!=null && !customerListResult.getResults().isEmpty()) {

			for(EyDmsCustomerModel source: customerListResult.getResults()) {
				CustomerData target = new CustomerData();
				target.setUid(source.getUid());
				target.setName(source.getName());
				target.setEmail(source.getEmail());
				target.setState(source.getState());
				target.setCustomerId(source.getCustomerNo());
				target.setContactNumber(source.getMobileNumber());
				target.setDealerCategory(source.getDealerCategory()!=null?source.getDealerCategory().getCode():"");
				if(source.getCounterType()!=null) {
					target.setPartnerType(source.getCounterType().getCode());
				}
				if(source.getInfluencerType()!=null){
					target.setInfluencerType(source.getInfluencerType().getCode());
				}
				if(source.getLastVisitTime()!=null) {
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
					target.setLastVisit(dateFormat.format(source.getLastVisitTime()));
				}
				target.setPotential(source.getCounterPotential()!=null?source.getCounterPotential():0.0);
				//				if(null!=source.getProfilePicture()){
				//					populateProfilePicture(source.getProfilePicture(),target);
				//				}
				if(customerData.getIsFlag()!=null) {
					if (customerData.getIsFlag().equals(Boolean.TRUE)) {
						if(source.getFlaggedBy()!=null) {
							target.setFlaggedBy(source.getFlaggedBy().getName());
						}
						target.setFlaggedDate(source.getFlagTime());
						target.setFlaggedReason(source.getRemarkForFlag());
						target.setDealerContactNumber(source.getContactNumber());
						Collection<AddressModel> list = source.getAddresses();
						List<AddressModel> billingAddressList = list.stream().filter(AddressModel::getBillingAddress).collect(Collectors.toList());
						if (billingAddressList != null && !billingAddressList.isEmpty()) {
							AddressModel billingAddress = billingAddressList.get(0);
							if (null != billingAddress) {
								target.setDefaultAddress(getAddressConverter().convert(billingAddress));
							}
						}
					}
				}
				if(customerData.getIsUnFlag()!=null) {
					if (customerData.getIsUnFlag().equals(Boolean.TRUE)) {
						target.setFlaggedBy(source.getUnFlagRequestRaisedBy().getName());
						target.setFlaggedDate(source.getUnflagRequestTime());
						target.setFlaggedReason(source.getRemarkForUnflag());
						target.setDealerContactNumber(source.getContactNumber());
						target.setUnFlagRequestTime(source.getUnflagRequestTime());
						Date unflagRequestTime = source.getUnflagRequestTime();
						LocalDate unFlagReqTime=unflagRequestTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						if(source.getUnflagRequestTime()!=null && source.getRemarkForUnflag()!=null) {
							LOGGER.info(String.format("Print:%s,%s,%s,%s,%s",String.valueOf(source), String.valueOf(source.getIsUnFlagRequestRaised()),String.valueOf(source.getUnflagRequestTime()),String.valueOf(source.getRemarkForUnflag()),unFlagReqTime));
							CounterVisitMasterModel counterVisitForUnFlaggedDealer = territoryManagementService.findCounterVisitForUnFlaggedDealer(source, customerData.getIsUnFlag(), source.getUnflagRequestTime(), source.getRemarkForUnflag());
							if (counterVisitForUnFlaggedDealer != null) {
								LOGGER.info(counterVisitForUnFlaggedDealer);
								target.setCounterVisitIdforUnFlag(counterVisitForUnFlaggedDealer.toString());
							}
						}
						Collection<AddressModel> list = source.getAddresses();
						List<AddressModel> billingAddressList = list.stream().filter(AddressModel::getBillingAddress).collect(Collectors.toList());
						if (billingAddressList != null && !billingAddressList.isEmpty()) {
							AddressModel billingAddress = billingAddressList.get(0);
							if (null != billingAddress) {
								target.setDefaultAddress(getAddressConverter().convert(billingAddress));
							}
						}
					}
				}
				allData.add(target);
			}
		}		result.setResults(allData);
		return result;
	}

	@Override
	public EyDmsUserListData getSOForUser(FilterTalukaData filterTalukaData) {
		EyDmsUserListData listData = new EyDmsUserListData();
		List<EyDmsUserData> list = new ArrayList<>();
		List<EyDmsUserModel> salesOfficers = territoryManagementService.getSOForUser(filterTalukaData);
		if(salesOfficers!=null && !salesOfficers.isEmpty()) {
			list = eydmsUserConverter.convertAll(salesOfficers);
		}
		listData.setEyDmsUsers(list);
		return listData;
	}

	@Override
	public EyDmsUserListData getTSMForUser(FilterDistrictData filterDistrictData) {
		EyDmsUserListData listData = new EyDmsUserListData();
		List<EyDmsUserData> list = new ArrayList<>();
		List<EyDmsUserModel> tsmList = territoryManagementService.getTSMForUser(filterDistrictData);
		if(tsmList!=null && !tsmList.isEmpty()) {
			list = eydmsUserConverter.convertAll(tsmList);
		}
		listData.setEyDmsUsers(list);
		return listData;
	}


    @Override
    public DistrictMasterListData getDistrictForSP(FilterDistrictData filterDistrictData) {
		DistrictMasterListData districtMasterListData = new DistrictMasterListData();
		List<DistrictMasterData> districtMasterList = new ArrayList<>();
		List<DistrictMasterModel> districtMasterModels = territoryManagementService.getDistrictForSP(filterDistrictData);
		if(districtMasterModels!=null && !districtMasterModels.isEmpty()) {
			for(DistrictMasterModel districtMasterModel : districtMasterModels) {
				DistrictMasterData districtMasterData = new DistrictMasterData();
				districtMasterData.setId(districtMasterModel.getPk().toString());
				districtMasterData.setDistrictCode(districtMasterModel.getCode());
				districtMasterData.setDistrictName(districtMasterModel.getName());
				districtMasterList.add(districtMasterData);
			}
		}
		districtMasterListData.setDistricts(districtMasterList);
		return districtMasterListData;
    }

	@Override
	public EyDmsCustomerData getSPForCustomer(String uid) {
		EyDmsCustomerData eydmsCustomerData = new EyDmsCustomerData();

		EyDmsCustomerModel dealer = (EyDmsCustomerModel) userService.getUserForUID(uid);

		EyDmsCustomerModel model = territoryManagementService.getSpForCustomer(dealer);

		eydmsCustomerData.setName(model.getName());
		eydmsCustomerData.setUid(model.getUid());
		eydmsCustomerData.setErpCustomerNo(model.getCustomerNo());

		return eydmsCustomerData;
	}

	public Converter<AddressModel, AddressData> getAddressConverter() {
		return addressConverter;
	}

	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
		this.addressConverter = addressConverter;
	}

}
