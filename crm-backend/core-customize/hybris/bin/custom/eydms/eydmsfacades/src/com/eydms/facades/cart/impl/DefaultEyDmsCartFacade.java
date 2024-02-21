package com.eydms.facades.cart.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.region.dao.DistrictMasterDao;
import com.eydms.core.region.dao.RegionMasterDao;
import com.eydms.core.region.dao.StateMasterDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.cart.service.EyDmsCartService;
import com.eydms.core.checkout.EyDmsCommerceCheckoutService;
import com.eydms.core.dao.OrderRequisitionDao;
import com.eydms.core.depot.operations.dao.DepotOperationsDao;
import com.eydms.core.enums.OrderType;
import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.model.ERPCityModel;
import com.eydms.core.model.OrderRequisitionModel;
import com.eydms.facades.cart.EyDmsCartFacade;
import com.eydms.facades.data.DestinationSourceListData;
import com.eydms.facades.data.DestinationSourceMasterData;
import com.eydms.facades.data.DropdownData;
import com.eydms.facades.data.DropdownListData;

import de.hybris.platform.commercefacades.order.impl.DefaultCartFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.product.ProductService;

/**
 * Class for EYDMS Carts related implementation
 */
public class DefaultEyDmsCartFacade extends DefaultCartFacade implements EyDmsCartFacade {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEyDmsCartFacade.class);

	@Autowired
	EyDmsCommerceCheckoutService eydmsCommerceCheckoutService;

	@Autowired
	EyDmsCartService eydmsCartService;

	@Autowired
	Converter<DestinationSourceMasterModel, DestinationSourceMasterData> destinationSourceConverter;

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
			final DeliveryModeModel deliveryModeModel = getDeliveryService().getDeliveryModeForCode(cartDetails.getDeliveryMode().getCode());

			final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cartModel, true);
			parameter.setOrderSource(cartDetails.getOrderSource());
			parameter.setOrderType(cartDetails.getOrderType());
			parameter.setIsDealerProvideOwnTransport(cartDetails.getIsDealerProvideOwnTransport());
			parameter.setDeliveryMode(deliveryModeModel);
			parameter.setTotalQuantity(cartDetails.getTotalQuantity());
			parameter.setRetailerCode(cartDetails.getRetailerCode());	
			parameter.setProductCode(cartDetails.getProductCode());
			if(cartDetails.getProductCode() != null)
			{
				ProductModel product= productService.getProductForCode(cartDetails.getProductCode());
				parameter.setProductName(product.getName());	
			}		
			parameter.setDestination(cartDetails.getDestination());
			parameter.setRouteId(cartDetails.getRouteId());
			if(OrderType.ISO.getCode().equals(cartDetails.getOrderType())) {
				AddressModel addressModel = depotOperationsDao.findDepotAddressByPk(cartDetails.getDeliveryAddress().getId());
				parameter.setAddress(addressModel);
				parameter.setIsDeliveryAddress(true);
			}
			if(cartDetails.getRequestedDeliveryDate()!=null)
			parameter.setRequestedDeliveryDate(setRequestedDeliveryDate(cartDetails.getRequestedDeliveryDate()));
			if(cartDetails.getRequestedDeliverySlot()!=null)
			parameter.setRequestedDeliverySlot(cartDetails.getRequestedDeliverySlot());
			return eydmsCommerceCheckoutService.setCartDetails(parameter);
		}
		return false;
	}


	private Date setRequestedDeliveryDate(String requestedDeliveryDate) {
		Date date=null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(requestedDeliveryDate);
			return date;
		} catch (ParseException e) {
			LOG.error("Error Parsing Requested Delivery Date", e);
			throw new IllegalArgumentException(String.format("Please provide valid date %s", requestedDeliveryDate));
		}
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
			if(cartDetails.getDistrictCode()!=null){
				cartModel.setDistrictMaster(districtMasterDao.findByCode(cartDetails.getDistrictCode()));
			}
			else{

			}
			if(cartDetails.getSubAreaCode()!=null){
				cartModel.setSubAreaMaster(territoryManagementDao.getTerritoryById(cartDetails.getSubAreaCode()));
			}
			else{

			}
			if(cartDetails.getStateCode()!=null){
				cartModel.setStateMaster(stateMasterDao.findByCode(cartDetails.getStateCode()));
			}
			else{

			}
			if(cartDetails.getRegionCode()!=null){
				cartModel.setRegionMaster(regionMasterDao.findByCode(cartDetails.getRegionCode()));
			}
			else{

			}
			modelService.save(cartModel);
		}

		else {
			throw new IllegalArgumentException("Cart not found");
		}
		return false;
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
			Integer maxTruckLoadSize = eydmsCartService.getMaxTruckLoadSize(cityUid, warehouseCode);
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

	@Override
	public DestinationSourceListData fetchDestinationSource(String city, String orderType, String deliveryMode, String productCode, String district,String state, String taluka) {

		DestinationSourceListData sourceListData = new DestinationSourceListData();
		List<DestinationSourceMasterModel> destinationSourceMasterList = eydmsCartService.fetchDestinationSourceByCity(city, orderType, deliveryMode,productCode, district, state, taluka);
		if( destinationSourceMasterList !=null && !destinationSourceMasterList.isEmpty()) {
			List<DestinationSourceMasterData> destinationSourceMasterData = destinationSourceConverter.convertAll(destinationSourceMasterList);
			
			destinationSourceMasterData.sort(Comparator.comparing(DestinationSourceMasterData::getPriority));
			sourceListData.setDestinationSourceDataList(destinationSourceMasterData);

			Optional<DestinationSourceMasterData> sourceMaster = destinationSourceMasterData.stream().filter(d -> d.getSourcePriority().equals("L1")).findAny();
			if (sourceMaster.isPresent()) {
				sourceListData.setDefaultSource(sourceMaster.get());
			}
		}
		return sourceListData;
	}
	@Override
	public DropdownListData getListOfERPCityByDistrictCode(String districtIsoCode) {
		DropdownListData dropdownListData = new DropdownListData();
		Collection<ERPCityModel> erpCityList = eydmsCartService.getListOfERPCityByDistrictCode(districtIsoCode);
		List<DropdownData> dataList = erpCityList.stream().map(this::populateDropdownData).collect(Collectors.toList());
		dropdownListData.setDropdown(dataList);
		return dropdownListData;
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

}
