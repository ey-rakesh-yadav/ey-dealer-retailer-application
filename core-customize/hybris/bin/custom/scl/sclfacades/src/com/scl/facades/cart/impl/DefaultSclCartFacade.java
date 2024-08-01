package com.scl.facades.cart.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.stream.Collectors.groupingBy;

import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.model.*;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.region.dao.RegionMasterDao;
import com.scl.core.region.dao.StateMasterDao;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import com.scl.facades.data.*;

import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.cart.service.SclCartService;
import com.scl.core.checkout.SclCommerceCheckoutService;
import com.scl.core.depot.operations.dao.DepotOperationsDao;
import com.scl.facades.cart.SclCartFacade;

import de.hybris.platform.commercefacades.order.impl.DefaultCartFacade;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.product.ProductService;


/**
 * Class for SCL Carts related implementation
 */
public class DefaultSclCartFacade extends DefaultCartFacade implements SclCartFacade {

	public static final String PLANT = "PLANT";
	public static final String DEPOT = "DEPOT";
	public static final String ROAD = "ROAD";
	public static final String SHOW_ROAD_SOURCE = "SHOW_ROAD_SOURCE";
	public static final String RAIL = "RAIL";
	public static final String SHOW_RAIL_SOURCE = "SHOW_RAIL_SOURCE";
	public static final String ENABLE_LOT_SIZE_VALIDATION = "ENABLE_LOT_SIZE_VALIDATION";
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSclCartFacade.class);

	@Autowired
	SclCommerceCheckoutService sclCommerceCheckoutService;

	@Autowired
	SclCartService sclCartService;

	@Autowired
	Converter<DestinationSourceMasterModel, DestinationSourceMasterData> destinationSourceConverter;


	@Autowired
	Converter<SclIncoTermMasterModel, SclIncoTermMasterData> sclIncoTermConverter;

	@Autowired
	ProductService productService;
	
	@Autowired
	DepotOperationsDao depotOperationsDao;
	
	@Autowired
	ModelService modelService;

	@Autowired
	DistrictMasterDao districtMasterDao;

	@Autowired
	RegionMasterDao regionMasterDao;

	@Autowired
	StateMasterDao stateMasterDao;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	DataConstraintDao dataConstraintDao;

	@Autowired
	Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter;

	/**
	 * Method to set cart details with the values passed
	 * @param cartDetails
	 * @return
	 */
	@Override
	public boolean setCartDetails(CartWsDTO cartDetails) {
		validateParameterNotNullStandardMessage("cartDetails", cartDetails);

		final CartModel cartModel = getCart();
		if(null != cartModel)
		{
			final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
			parameter.setOrderType(cartDetails.getOrderType());
			parameter.setTotalQuantity(cartDetails.getTotalQuantity());
			parameter.setIsPartnerCustomer(cartDetails.getIsPartnerCustomer());
			parameter.setPlacedByCustomer(cartDetails.getPlacedByCustomer());
			return sclCommerceCheckoutService.setCartDetails(parameter);
		}
		return false;
	}

	@Override
	public boolean setOrderRequistionOnOrder(CartWsDTO cartDetails) {
		validateParameterNotNullStandardMessage("cartDetails", cartDetails);

		final CartModel cartModel = getCart();
		if(null != cartModel)
		{
			List<String> requistionList = cartDetails.getOrderRequistions();
			if(requistionList!=null) {
				cartModel.setRequisitionNumberList(requistionList);
				modelService.save(cartModel);
			}
			modelService.save(cartModel);
		}

		else {
			throw new IllegalArgumentException("Cart not found");
		}
		return false;
	}

	/**
	 * @param orderType
	 * @param deliveryMode
	 * @param productCode
	 * @param transportationZone
	 * @return
	 */
	@Override
	public SCLIncoTermMasterListData fetchIncoTerms(String orderType, String deliveryMode, String productCode, String transportationZone) {
		SCLIncoTermMasterListData incoTermListData = new SCLIncoTermMasterListData();
                  List<SclIncoTermMasterModel> sclIncoTermList =sclCartService.fetchIncoTerms(orderType,deliveryMode,productCode,transportationZone);
		     if(CollectionUtils.isNotEmpty(sclIncoTermList)) {
				 List<SclIncoTermMasterData> incoTermMasterData=sclIncoTermConverter.convertAll(sclIncoTermList);
				 incoTermMasterData.sort(Comparator.comparing(SclIncoTermMasterData::getSequence));
				 incoTermListData.setSclIncoTermMasterDataList(incoTermMasterData);
			 }
				  return incoTermListData;
	}

	/**
	 * @param orderQty
	 * @param cityUid
	 * @param warehouseCode
	 * @return
	 */
	@Override
	public Integer getCountOfDI(Integer orderQty, String cityUid, String warehouseCode) {
		try{
			Integer maxTruckLoadSize = sclCartService.getMaxTruckLoadSize(cityUid, warehouseCode);
			if (maxTruckLoadSize<=0) {
				throw new IllegalArgumentException("maxTruckLoadSize must be a positive non-zero value");
			}
			else {
				return orderQty/maxTruckLoadSize;
			}
		}
		catch(Exception e){
			return 0;
		}
	}

	/**
	 * @param orderType
	 * @param deliveryMode
	 * @param productCode
	 * @param transportationZone
	 * @param incoTerm
	 * @return
	 */
	@Override
	public DestinationSourceListData fetchDestinationSource(String orderType, String deliveryMode, String productCode, String transportationZone, String incoTerm, double orderQty) {

		DestinationSourceListData sourceListData = new DestinationSourceListData();
		try {
			List<DestinationSourceMasterModel> destinationSourceMasterModelList = sclCartService.fetchDestinationSource(orderType, deliveryMode, productCode, transportationZone, incoTerm);
			//List<DestinationSourceMasterData> destinationSourceMasterData = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(destinationSourceMasterModelList) && destinationSourceMasterModelList.size() > 0) {

				List<DestinationSourceMasterData> destinationSourceMasterData = destinationSourceConverter.convertAll(destinationSourceMasterModelList);
			/*LOG.info(String.format("destinationSource list size :: %s",destinationSourceMasterModelList.size()));
			Map<WarehouseModel, List<DestinationSourceMasterModel>> groupBySource=destinationSourceMasterModelList.stream().collect(groupingBy(DestinationSourceMasterModel::getSource));
           LOG.info(String.format("Group by result::%s",groupBySource));

			for (Map.Entry<WarehouseModel,List<DestinationSourceMasterModel>> modelListEntry:groupBySource.entrySet())
			{
				List<SclIncoTermMasterData> sclIncoTermMasterDataList=new ArrayList<>();
			List<DestinationSourceMasterModel>	sourceMasterModels= modelListEntry.getValue();
			LOG.info(String.format("sourceMasterModels size::%s",sourceMasterModels.size()));
				sourceMasterData  =destinationSourceConverter.convert(sourceMasterModels.get(0));
				for (DestinationSourceMasterModel destinationSourceMaster:sourceMasterModels) {
						if(Objects.nonNull(destinationSourceMaster.getIncoterms())) {
							SclIncoTermMasterData sclIncoTermMaster = sclIncoTermConverter.convert(destinationSourceMaster.getIncoterms());
							if (sourceMasterModels.size()>1)
							{
								boolean isIncoTermPresent = false;
								LOG.info(String.format("sourceMaster size greater than 1 for telcoMaster::%s",sclIncoTermMaster.getIncoTerm()));
								if(CollectionUtils.isNotEmpty(sclIncoTermMasterDataList)) {
									 isIncoTermPresent = sclIncoTermMasterDataList.stream().anyMatch(incoTerm -> incoTerm.getIncoTerm().equalsIgnoreCase(sclIncoTermMaster.getIncoTerm()));
								 LOG.info(String.format("isIncoTermPresent in list::%s for incoTerm ::%s",isIncoTermPresent,sclIncoTermMaster.getIncoTerm()));
								}
								if (!isIncoTermPresent) {
										sclIncoTermMasterDataList.add(sclIncoTermMaster);
									LOG.info(String.format("isIncoTerm not Present in list::%s for incoTerm ::%s",isIncoTermPresent,sclIncoTermMaster.getIncoTerm()));
									}
							}else {
								sclIncoTermMasterDataList = new ArrayList<>();
								sclIncoTermMasterDataList.add(sclIncoTermMaster);
							}
						}
				}
				LOG.info(String.format("sclIncoTerm master Data list size ::%s",sclIncoTermMasterDataList.size()));
				sclIncoTermMasterDataList.sort(Comparator.comparing(SclIncoTermMasterData::getSequence));
				sourceMasterData.setIncoTerms(sclIncoTermMasterDataList);
				destinationSourceMasterData.add(sourceMasterData);
			}*/

				String enableLotSizeValidation = dataConstraintDao.findVersionByConstraintName(ENABLE_LOT_SIZE_VALIDATION);

				if (BooleanUtils.isFalse(Boolean.valueOf(enableLotSizeValidation))) {
					destinationSourceMasterData.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
					sourceListData.setDestinationSourceDataList(destinationSourceMasterData);
					Optional<DestinationSourceMasterData> sourceMaster = destinationSourceMasterData.stream().filter(d -> d.getSourcePriority().equals("L1")).findAny();
					if (sourceMaster.isPresent()) {
						sourceListData.setDefaultSource(sourceMaster.get());
					}
				} else if (BooleanUtils.isTrue(Boolean.valueOf(enableLotSizeValidation))) {
					List<DestinationSourceMasterData> plantDpotSourceList = new ArrayList<>();
					destinationSourceMasterData.forEach(source -> {
						/*if (source.getSourceType().equals(PLANT)) {*/
							if (StringUtils.isNotBlank(source.getLotSize()) && orderQty >= Double.valueOf(source.getLotSize())) {
								plantDpotSourceList.add(source);
								LOG.info(String.format("plantDpotSourceList sources ::%s", plantDpotSourceList));
							} else if (StringUtils.isBlank(source.getLotSize())) {
								plantDpotSourceList.add(source);
							}
						/*} else if (source.getSourceType().equals(DEPOT)) {
							plantDpotSourceList.add(source);
							LOG.info(String.format("List of Depot Source ::%s", plantDpotSourceList));
						}*/
					});

					//cases
					if (CollectionUtils.isNotEmpty(plantDpotSourceList)) {

						LOG.info(String.format("plantDpotSourceList size::%s with pk::%s",plantDpotSourceList.size(),plantDpotSourceList));
						List<DestinationSourceMasterData> sourceList=filterSourceByDeliveryMode(plantDpotSourceList, sourceListData, deliveryMode);
						sourceList.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
						sourceListData.setDefaultSource(Objects.nonNull(sourceList.get(0))?sourceList.get(0):null);
						sourceListData.setDestinationSourceDataList(sourceList);
					} else {
						LOG.info(String.format("destinationSourceMasterData size::%s with pk::%s",destinationSourceMasterData.size(),destinationSourceMasterData));
						List<DestinationSourceMasterData> sourceList=filterSourceByDeliveryMode(destinationSourceMasterData, sourceListData, deliveryMode);
						sourceList.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
						sourceListData.setDefaultSource(Objects.nonNull(sourceList.get(0))?sourceList.get(0):null);
						sourceListData.setDestinationSourceDataList(sourceList);
					}
				}
			}
			return sourceListData;
		}catch (Exception ex){
			LOG.error(String.format("Getting exception in fetchDestinationSource API with error ::%s and message ::%s ",ex.getStackTrace(),ex.getMessage()));
		}
		return sourceListData;
	}

	@Override
	public DropdownListData getListOfERPCityByDistrictCode(String districtIsoCode) {
		DropdownListData dropdownListData = new DropdownListData();
		Collection<ERPCityModel> erpCityList = sclCartService.getListOfERPCityByDistrictCode(districtIsoCode);
		List<DropdownData> dataList = erpCityList.stream().map(this::populateDropdownData).collect(Collectors.toList());
		dropdownListData.setDropdown(dataList);
		return dropdownListData;
	}

	/**
	 * Fetch Delivery Mode Data
	 * @param orderType
	 * @param productCode
	 * @param transportationZone
	 * @return
	 */
	@Override
	public DeliveryModeListData fetchDeliveryMode(String orderType, String productCode, String transportationZone) {
		DeliveryModeListData deliveryModeListData = new DeliveryModeListData();
		List<DeliveryModeModel> deliveryModeModelList = sclCartService.fetchDeliveryMode(orderType, productCode, transportationZone);
		if (CollectionUtils.isNotEmpty(deliveryModeModelList)) {
			List<DeliveryModeData> deliveryModeData = deliveryModeConverter.convertAll(deliveryModeModelList);
			deliveryModeListData.setDeliveryModeListData(deliveryModeData);
		}
		return deliveryModeListData;
	}

	private DropdownData populateDropdownData(ERPCityModel model) {
		DropdownData data = new DropdownData();
		data.setCode(model.getIsocode());
		data.setName(model.getName());
		return data;
	}

	protected CartModel getCart()
	{
		return hasSessionCart() ? getCartService().getSessionCart() : null;
	}

	protected CommerceCheckoutParameter createCommerceCheckoutParameter(final CartModel cart, final boolean enableHooks)
	{
		final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
		parameter.setEnableHooks(enableHooks);
		parameter.setCart(cart);
		return parameter;
	}

	/**
	 * Filter Source By DeliveryMode
	 *
	 * @param plantDepotSourceList
	 * @param sourceListData
	 * @param deliveryMode
	 */
	private List<DestinationSourceMasterData> filterSourceByDeliveryMode(List<DestinationSourceMasterData> plantDepotSourceList, DestinationSourceListData sourceListData, String deliveryMode) {

		List<DestinationSourceMasterData> finalSubList = null;
		if(deliveryMode.equalsIgnoreCase(ROAD)) {
			Integer roadSource = dataConstraintDao.findDaysByConstraintName(SHOW_ROAD_SOURCE);
			if (Objects.nonNull(roadSource) && plantDepotSourceList.size()>=roadSource) {
				LOG.info(String.format("showing no of road source ::%s",roadSource));
				plantDepotSourceList.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
				finalSubList=plantDepotSourceList.subList(0, roadSource);
			}
		}
		if(deliveryMode.equalsIgnoreCase(RAIL)) {
			Integer railSource = dataConstraintDao.findDaysByConstraintName(SHOW_RAIL_SOURCE);
			if (Objects.nonNull(railSource) && plantDepotSourceList.size()>=railSource) {
				LOG.info(String.format("showing no of rail source ::%s",railSource));
				plantDepotSourceList.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
				finalSubList=plantDepotSourceList.subList(0, railSource);
			}
		}

		if(CollectionUtils.isNotEmpty(finalSubList)){
			LOG.info(String.format("List of Destination Source Master Data Delivery Mode ::%s source size ::%s and %s",deliveryMode,finalSubList.size(),finalSubList));
			return finalSubList;
		}else{
			LOG.info(String.format("List of Destination Source Master Data Delivery Mode ::%s source size ::%s and %s",deliveryMode,plantDepotSourceList.size(),plantDepotSourceList));
			return plantDepotSourceList;
		}
	}



}
