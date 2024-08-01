package com.scl.facades.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.CounterType;
import com.scl.core.model.*;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.data.*;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.TerritoryManagementFacade;
import com.scl.facades.prosdealer.data.CustomerListData;
import com.scl.facades.prosdealer.data.DealerListData;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class TerritoryManagementFacadeImpl implements TerritoryManagementFacade {

	private static final Logger LOGGER = Logger.getLogger(TerritoryManagementFacadeImpl.class);
	@Autowired
	TerritoryManagementService territoryManagementService;
	@Autowired
	TerritoryMasterService territoryMasterService;
	@Autowired
	SalesPerformanceService salesPerformanceService;
	@Autowired
	Converter<AddressModel, AddressData> addressConverter;
	@Autowired
	DJPVisitService djpVisitService;

	@Autowired
	UserService userService;
	@Autowired
	DataConstraintDao dataConstraintDao;
	
	@Autowired
	Converter<SclCustomerModel,CustomerData> dealerBasicConverter;
	
	@Autowired
	Converter<SclUserModel,SclUserData> sclUserConverter;
		
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
		List<SclCustomerModel> customerList = territoryManagementService.getAllCustomerForSO();
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
	public DealerListData getAllDealersForSubArea(List<String> territoryList) {
		//New Territory Change
		//List<SclCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
		List<SclCustomerModel> dealerList = salesPerformanceService.getCustomersByLeadType("DEALER",null,null,null);
		//instead of above salesPerfomanceService.getCustomerByLeadType
		//if territoryList is not empty then filter customerFilteredList based upon territory list
		dealerList=salesPerformanceService.getCustomersByTerritoryCode(dealerList,territoryList);
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
		List<SclCustomerModel> retailerList = territoryManagementService.getRetailersForSubArea();
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
		List<SclCustomerModel> influencerList = territoryManagementService.getInfluencersForSubArea();
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
		List<SclCustomerModel> siteList = territoryManagementService.getSitesForSubArea();
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
		List<SclCustomerModel> retailerList = territoryManagementService.getAllRetailersForSubAreaTOP(subArea,dealerCode);
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
		List<SclUserModel> salesOfficerList = territoryManagementService.getAllSalesOfficersByState(state);
		DealerListData dataList = new DealerListData();
		if(salesOfficerList!=null && !salesOfficerList.isEmpty())
		{
			List<CustomerData> salesOfficerDataList=new ArrayList<>();
			for (SclUserModel sclUserModel : salesOfficerList) {
				CustomerData customerData=new CustomerData();
				customerData.setUid(sclUserModel.getUid());
				customerData.setName(sclUserModel.getName());
				customerData.setContactNumber(sclUserModel.getMobileNumber());
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
		List<SclCustomerModel> retailerListForDealer = territoryManagementService.getRetailerListForDealer();
		if(retailerListForDealer!=null && !retailerListForDealer.isEmpty()) {
			 customerDataList = Optional.of(retailerListForDealer.stream().distinct()
					.map(b2BCustomer -> dealerBasicConverter
							.convert(b2BCustomer)).collect(Collectors.toList())).get();
		}
		data.setDealers(customerDataList);
		return data;
	}

	@Override
	public DealerListData getInfluencerListForDealer() {
		List<SclCustomerModel> influencerListForDealer = territoryManagementService.getInfluencerListForDealer();
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
		List<SclCustomerModel> influencerListForRetailer = territoryManagementService.getInfluencerListForRetailer();
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
		SearchPageData<SclCustomerModel> retailerListForDealer = territoryManagementService.getRetailerListForDealerPagination(searchPageData, networkType, isNew, filter, isTop);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> sclCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(sclCustomerData);
		return result;
	}
	@Override
	public SearchPageData<CustomerData> getInfluencerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
			String influencerType, String dealerCategory) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<SclCustomerModel> retailerListForDealer = territoryManagementService.getInfluencerListForDealerPagination(searchPageData, networkType, isNew, filter, influencerType, dealerCategory);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> sclCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(sclCustomerData);
		return result;
	}
	@Override
	public SearchPageData<CustomerData> getInfluencerListForRetailerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
			String influencerType, String dealerCategory) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<SclCustomerModel> retailerListForDealer = territoryManagementService.getInfluencerListForRetailerPagination(searchPageData, networkType, isNew, filter, influencerType, dealerCategory);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> sclCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(sclCustomerData);
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
		SearchPageData<SclCustomerModel> retailerListForDealer = territoryManagementService.getDealerListForRetailerPagination(searchPageData,filter);
		result.setPagination(retailerListForDealer.getPagination());
		result.setSorts(retailerListForDealer.getSorts());
		List<CustomerData> sclCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		result.setResults(sclCustomerData);
		return result;
	}

	@Override
	public List<CustomerData> getDealerListForRetailer(){
		final List<CustomerData> result = new ArrayList<>();
		List<SclCustomerModel> dealerListForRetailer = territoryManagementService.getDealerListForRetailer();
		List<CustomerData> sclCustomerData = dealerBasicConverter.convertAll(dealerListForRetailer);
		result.addAll(sclCustomerData);
		return result;
	};

	@Override
	public TerritoryListData getTerritoriesForCustomer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
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
		List<TerritoryData> territoryList= new ArrayList<TerritoryData>();
		List<SubAreaData> subAreaList = new ArrayList<SubAreaData>();

		List<SubAreaMasterModel> subAreaListModel = territoryManagementService.getTaulkaForUser(filterTalukaData);
		if(subAreaListModel!=null && !subAreaListModel.isEmpty()) {
			for(SubAreaMasterModel subArea : subAreaListModel) {
				if(subArea!=null) {
					SubAreaData subAreaData = new SubAreaData();
					subAreaData.setId(subArea.getPk().toString());
					subAreaData.setDistrict(subArea.getDistrict());
					subAreaData.setTaluka(subArea.getTaluka());
					subAreaList.add(subAreaData);
				}
			}
		}

		if(CollectionUtils.isNotEmpty(subAreaList)){
		subAreaList = subAreaList.stream().filter(sub -> Objects.nonNull(sub.getTaluka())).sorted(Comparator.comparing(SubAreaData::getTaluka)).distinct().collect(Collectors.toList());
		}

		List<TerritoryMasterModel> territoryMasterModel =territoryMasterService.getTerritoryForUser(null);
		if(territoryMasterModel!=null && !territoryMasterModel.isEmpty()) {
			for(TerritoryMasterModel territoryMaster  : territoryMasterModel) {
				if(territoryMaster!=null) {
					TerritoryData territoryData= new TerritoryData();
					territoryData.setTerritoryCode(territoryMaster.getTerritoryCode());
					territoryData.setTerritoryName(territoryMaster.getTerritoryCode());
					territoryList.add(territoryData);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(territoryList)){
			territoryList = territoryList.stream().filter(ter -> Objects.nonNull(ter.getTerritoryCode())).sorted(Comparator.comparing(TerritoryData::getTerritoryCode)).distinct().collect(Collectors.toList());
		}
		listData.setSubAreas(subAreaList);
		listData.setTerritoryList(territoryList);
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
				if(subArea.getSclUser()!=null) {
					SOSubAreaMappingData soSubAreaMappingData = new SOSubAreaMappingData();
					//RH & TSM - SO subarea master
					soSubAreaMappingData.setUserCode(subArea.getSclUser().getUid());
					soSubAreaMappingData.setUserName(subArea.getSclUser().getName());
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

		List<TerritoryData> territoryList= new ArrayList<TerritoryData>();

		List<TerritoryMasterModel> territoryMasterModel =territoryMasterService.getTerritoryForUser(null);
		if(territoryMasterModel!=null && !territoryMasterModel.isEmpty()) {
			for(TerritoryMasterModel territoryMaster  : territoryMasterModel) {
				if(territoryMaster!=null) {
					TerritoryData territoryData= new TerritoryData();
					territoryData.setTerritoryCode(territoryMaster.getTerritoryCode());
					territoryData.setTerritoryName(territoryMaster.getTerritoryCode());
					territoryList.add(territoryData);
				}
			}
		}

		if(CollectionUtils.isNotEmpty(regionList)){
			regionListData.setRegions(regionList.stream().filter(obj->Objects.nonNull(obj.getName())).sorted(Comparator.comparing(RegionData::getName)).collect(Collectors.toList()));
		}else {
			regionListData.setRegions(regionList);
		}
		if(CollectionUtils.isNotEmpty(territoryList)){
			regionListData.setTerritoryList(territoryList.stream().filter(obj->Objects.nonNull(obj.getTerritoryCode())).sorted(Comparator.comparing(TerritoryData::getTerritoryCode)).collect(Collectors.toList()));
		}else{
			regionListData.setTerritoryList(territoryList);
		}

		return regionListData;
	}

	private List<SclCustomerModel> filterCustomerByDOTerritoryCodeforCustomerList(List<SclCustomerModel> sclCustomerModels)
	{
		Collection<TerritoryMasterModel> territoryMasterModels=territoryMasterService.getCurrentTerritory();
		LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
		List<SclCustomerModel> filterdList=new ArrayList<>();
		if (CollectionUtils.isNotEmpty(territoryMasterModels)) {
			List<TerritoryMasterModel> territoryMasterModelList=territoryMasterModels.stream().distinct().collect(Collectors.toList());
			sclCustomerModels.stream().forEach(sclCustomer -> {
				//dealer
				if(Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)) {
					if (sclCustomer.getCounterType().equals(CounterType.DEALER) && Objects.nonNull(sclCustomer.getTerritoryCode()))
					{
						LOGGER.info(String.format("Inside dealer check ::%s and territoryCode::%s",sclCustomer,sclCustomer.getTerritoryCode()));
						if (territoryMasterModelList.contains(sclCustomer.getTerritoryCode())) {
							LOGGER.info(String.format("territoryModels dealer check ::%s ",territoryMasterModelList.contains(sclCustomer.getTerritoryCode())));
							filterdList.add(sclCustomer);
						}
						//retailer
					} else if ( Objects.nonNull(sclCustomer.getCounterType()) && sclCustomer.getCounterType().equals(CounterType.RETAILER)) {
						Integer retailerCount = djpVisitService.getRetailerCountByTerritory(sclCustomer, territoryMasterModelList);
						LOGGER.info(String.format("Inside retailer check ::%s and territoryCode::%s and retailerCount::%s",sclCustomer,territoryMasterModels,retailerCount));
						if (retailerCount > 0) {
							filterdList.add(sclCustomer);
						}
					}
				}
				else {
					String enbleInfuencerSite = dataConstraintDao.findVersionByConstraintName(SclCoreConstants.CUSTOMER.EXCLUDE_INFLUENCER_SITE);
					if(BooleanUtils.isTrue(Boolean.valueOf(enbleInfuencerSite))) {
						//non-scl user
						if (Objects.nonNull(sclCustomer.getCounterType()) && (!sclCustomer.getCounterType().equals(CounterType.INFLUENCER) && !(sclCustomer.getCounterType().equals(CounterType.SITE)))) {
							LOGGER.info(String.format("non scl user(Excluding Inf-Site) ::%s ", sclCustomer));
							filterdList.add(sclCustomer);
						}
					}else{
						LOGGER.info(String.format("non scl user(Including Inf-Site) ::%s ", sclCustomer));
						filterdList.add(sclCustomer);
					}

				}
			});
			return filterdList.stream().distinct().collect(Collectors.toList());
		}
		return filterdList;
	}

	@Override
	public CustomerListData getCustomerForUser(RequestCustomerData customerData) {
		CustomerListData dataList = new CustomerListData();
		String visitDate = "";
		try {
			List<SclCustomerModel> customerModels = territoryManagementService.getCustomerforUser(customerData);
			List<SclCustomerModel> customerList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(customerModels)) {
				customerList = filterCustomerByDOTerritoryCodeforCustomerList(customerModels);
				LOGGER.info(String.format("Customer List size:%s", customerList.size()));
			}

			List<CustomerData> allData = new ArrayList<CustomerData>();
			if (customerList != null && !customerList.isEmpty()) {

				for (SclCustomerModel source : customerList) {
					CustomerData target = new CustomerData();
					if(source.getUid()!=null) {
						target.setUid(source.getUid());
					}
					if(source.getName()!=null) {
						target.setName(source.getName());
					}
					if(source.getEmail()!=null) {
						target.setEmail(source.getEmail());
					}
					if(source.getState()!=null) {
						target.setState(source.getState());
					}
					if(source.getCustomerNo()!=null) {
						target.setCustomerId(source.getCustomerNo());
					}
					if(source.getMobileNumber()!=null) {
						target.setContactNumber(source.getMobileNumber());
					}
					if(source.getCounterPotential()!=null) {
						target.setPotential(source.getCounterPotential());
					}
					if(source.getLastVisitTime()!=null){
						DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
						visitDate = dateFormat.format(source.getLastVisitTime());
						target.setLastVisit(visitDate);
					}

					target.setDealerCategory(source.getDealerCategory() != null ? source.getDealerCategory().getCode() : "");
					if (source.getCounterType() != null) {
						target.setPartnerType(source.getCounterType().getCode());
					}
					if (source.getNetworkType() != null) {
						target.setNetworkType(source.getNetworkType());
					}

					if(djpVisitService.isNonSclCounter(source)){
						target.setIsNonSclCounter(Boolean.TRUE);
						if(CollectionUtils.isNotEmpty(source.getAddresses())){
							AddressModel addressModel=source.getAddresses().iterator().next();
							target.setLine1(addressModel.getLine1());
							target.setLine2(addressModel.getLine2());
						}
					}else{
						target.setIsNonSclCounter(Boolean.FALSE);
						AddressModel addressModel=djpVisitService.getCustomerOwnAddress(source);
						if(Objects.nonNull(addressModel)) {
							target.setLine1(addressModel.getLine1());
							target.setLine2(addressModel.getLine2());
						}
					}

//				if(source.getCustomerNo()!=null) {
//					target.setCustomerNo(source.getCustomerNo());
//				}
//				if(null!=source.getProfilePicture()){
//					populateProfilePicture(source.getProfilePicture(),target);
//				}
					target.setIsBillingBlock(source.getIsBillingBlock());
					target.setIsDeliveryBlock(source.getIsDeliveryBlock());
					target.setIsOrderBlock(source.getIsOrderBlock());

					allData.add(target);
				}
			}

			List<CustomerData> sortedCustomersData = new ArrayList<>();
			if (Objects.isNull(customerData.getSortByDate()) || BooleanUtils.isFalse(customerData.getSortByDate())) {
				sortedCustomersData = allData.stream().sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList());
				dataList.setCustomers(sortedCustomersData);
			} else {
				dataList.setCustomers(allData);
			}
			return dataList;
		}catch (Exception ex){
			LOGGER.error(String.format("got exception in getCustomerForUser ::%s with cause::%s",ex.getMessage(),ex.getCause()));
		}
		return dataList;
	}

	@Override
	public CustomerListData getCustomerForWJP(RequestCustomerData customerData) {
		CustomerListData dataList = new CustomerListData();
		String visitDate = "";
		try {
			customerData.setCounterType(List.of("DEALER"));
			List<SclCustomerModel> customerModelsDealer=salesPerformanceService.getCurrentNetworkCustomersForTSMRH(customerData);
			customerData.setCounterType(List.of("RETAILER"));
			List<SclCustomerModel> customerModelsRetailer=salesPerformanceService.getCurrentNetworkCustomersForTSMRH(customerData);
			customerData.setCounterType(null);
			List<SclCustomerModel> nonSclCustomers=salesPerformanceService.getCurrentNetworkCustomersForTSMRH(customerData);
			List<SclCustomerModel> customerList = new ArrayList<>();
			customerList.addAll(customerModelsRetailer);
			customerList.addAll(customerModelsDealer);
			customerList.addAll(nonSclCustomers);
			customerList=customerList.stream().sorted(Comparator.comparing(SclCustomerModel::getCreationtime).reversed()).distinct().collect(Collectors.toList());

			List<CustomerData> allData = new ArrayList<CustomerData>();
			if (customerList != null && !customerList.isEmpty()) {

				for (SclCustomerModel source : customerList) {
					CustomerData target = new CustomerData();
					if(source.getUid()!=null) {
						target.setUid(source.getUid());
					}
					if(source.getName()!=null) {
						target.setName(source.getName());
					}
					if(source.getEmail()!=null) {
						target.setEmail(source.getEmail());
					}
					if(source.getState()!=null) {
						target.setState(source.getState());
					}
					if(source.getCustomerNo()!=null) {
						target.setCustomerId(source.getCustomerNo());
					}
					if(source.getMobileNumber()!=null) {
						target.setContactNumber(source.getMobileNumber());
					}
					if(source.getCounterPotential()!=null) {
						target.setPotential(source.getCounterPotential());
					}
					if(source.getLastVisitTime()!=null){
						DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
						visitDate = dateFormat.format(source.getLastVisitTime());
						target.setLastVisit(visitDate);
					}

					target.setDealerCategory(source.getDealerCategory() != null ? source.getDealerCategory().getCode() : "");
					if (source.getCounterType() != null) {
						target.setPartnerType(source.getCounterType().getCode());
					}
					if (source.getNetworkType() != null) {
						target.setNetworkType(source.getNetworkType());
					}

					if(djpVisitService.isNonSclCounter(source)){
						target.setIsNonSclCounter(Boolean.TRUE);
						if(CollectionUtils.isNotEmpty(source.getAddresses())){
							AddressModel addressModel=source.getAddresses().iterator().next();
							target.setLine1(addressModel.getLine1());
							target.setLine2(addressModel.getLine2());
						}
					}else{
						target.setIsNonSclCounter(Boolean.FALSE);
						AddressModel addressModel=djpVisitService.getCustomerOwnAddress(source);
						if(Objects.nonNull(addressModel)) {
							target.setLine1(addressModel.getLine1());
							target.setLine2(addressModel.getLine2());
						}
					}

//				if(source.getCustomerNo()!=null) {
//					target.setCustomerNo(source.getCustomerNo());
//				}
//				if(null!=source.getProfilePicture()){
//					populateProfilePicture(source.getProfilePicture(),target);
//				}
					target.setIsBillingBlock(source.getIsBillingBlock());
					target.setIsDeliveryBlock(source.getIsDeliveryBlock());
					target.setIsOrderBlock(source.getIsOrderBlock());

					allData.add(target);
				}
			}

			List<CustomerData> sortedCustomersData = new ArrayList<>();
			if (Objects.isNull(customerData.getSortByDate()) || BooleanUtils.isFalse(customerData.getSortByDate())) {
				sortedCustomersData = allData.stream().sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList());
				dataList.setCustomers(sortedCustomersData);
			} else {
				dataList.setCustomers(allData);
			}
			return dataList;
		}catch (Exception ex){
			LOGGER.error(String.format("got exception in getCustomerForUser ::%s with cause::%s",ex.getMessage(),ex.getCause()));
		}
		return dataList;
	}
	@Override
	public SearchPageData<CustomerData> getCustomerForUserPagination(SearchPageData searchPageData,
			RequestCustomerData customerData) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<SclCustomerModel> customerListResult = territoryManagementService.getCustomerForUserPagination(searchPageData, customerData);
		result.setPagination(customerListResult.getPagination());
		result.setSorts(customerListResult.getSorts());
		List<CustomerData> allData = new ArrayList<CustomerData>();
		if(customerListResult.getResults()!=null && !customerListResult.getResults().isEmpty()) {

			for(SclCustomerModel source: customerListResult.getResults()) {
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
				if(source.getNetworkType()!=null){
					target.setNetworkType(source.getNetworkType());
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
				target.setIsBillingBlock(source.getIsBillingBlock());
				target.setIsDeliveryBlock(source.getIsDeliveryBlock());
				target.setIsOrderBlock(source.getIsOrderBlock());

				allData.add(target);
			}
		}
		List<CustomerData> allDataSorted = allData.stream().sorted(Comparator.comparing(customer->customer.getName().toLowerCase())).collect(Collectors.toList());
		result.setResults(allDataSorted);
		return result;
	}

	@Override
	public SclUserListData getSOForUser(FilterTalukaData filterTalukaData) {
		SclUserListData listData = new SclUserListData();
		List<SclUserData> list = new ArrayList<>();
		List<SclUserModel> salesOfficers = territoryManagementService.getSOForUser(filterTalukaData);
		if(salesOfficers!=null && !salesOfficers.isEmpty()) {
			list = sclUserConverter.convertAll(salesOfficers);
		}
		List<SclUserData> sclUserDataList = list.stream().sorted(Comparator.comparing(SclUserData::getName)).collect(Collectors.toList());
		listData.setSclUsers(sclUserDataList);
		return listData;
	}

	@Override
	public SclUserListData getTSMForUser(FilterDistrictData filterDistrictData) {
		SclUserListData listData = new SclUserListData();
		List<SclUserData> list = new ArrayList<>();
		List<SclUserModel> tsmList = territoryManagementService.getTSMForUser(filterDistrictData);
		if(tsmList!=null && !tsmList.isEmpty()) {
			list = sclUserConverter.convertAll(tsmList);
		}
		listData.setSclUsers(list);
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
	public SclCustomerData getSPForCustomer(String uid) {
		SclCustomerData sclCustomerData = new SclCustomerData();

		SclCustomerModel dealer = (SclCustomerModel) userService.getUserForUID(uid);

		SclCustomerModel model = territoryManagementService.getSpForCustomer(dealer);
		if(model!=null) {
			sclCustomerData.setName(model.getName());
			sclCustomerData.setUid(model.getUid());
			sclCustomerData.setErpCustomerNo(model.getCustomerNo());
		}
		return sclCustomerData;
	}

	public Converter<AddressModel, AddressData> getAddressConverter() {
		return addressConverter;
	}

	public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
		this.addressConverter = addressConverter;
	}

	@Override
	public SearchPageData<CustomerData> fetchRetailerListForDealer(SearchPageData searchPageData, String searchKey) {
		final SearchPageData<CustomerData> result = new SearchPageData<>();
		SearchPageData<SclCustomerModel> retailerListForDealer = territoryManagementService.fetchRetailersForDealer(searchPageData,searchKey);
		result.setPagination(retailerListForDealer.getPagination());
		List<CustomerData> sclCustomerData = dealerBasicConverter.convertAll(retailerListForDealer.getResults());
		if(CollectionUtils.isNotEmpty(sclCustomerData)) {
			List<CustomerData> sortedCustomerData = sclCustomerData.stream().sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList());
			result.setResults(sortedCustomerData);
		}
		return result;
	}

}
