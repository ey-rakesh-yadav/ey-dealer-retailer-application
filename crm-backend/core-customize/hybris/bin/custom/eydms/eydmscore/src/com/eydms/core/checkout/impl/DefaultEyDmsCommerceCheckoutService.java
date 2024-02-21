package com.eydms.core.checkout.impl;

import com.eydms.core.cart.dao.EyDmsWarehouseDao;
import com.eydms.core.checkout.EyDmsCommerceCheckoutService;
import com.eydms.core.enums.*;
import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.model.DistrictMasterModel;
import com.eydms.core.model.FreightAndIncoTermsMasterModel;
import com.eydms.core.model.RegionMasterModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.StateMasterModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCheckoutService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eydms.core.model.CustDepotMasterModel;

import org.springframework.beans.factory.annotation.Autowired;

public class DefaultEyDmsCommerceCheckoutService extends DefaultCommerceCheckoutService implements EyDmsCommerceCheckoutService
{
	@Autowired
	UserService userService;

    @Autowired
    EyDmsWarehouseDao eydmsWarehouseDao;

    @Autowired
    TerritoryManagementService territoryService;
    
    @Autowired
    FlexibleSearchService flexibleSearchService;
    
    @Autowired
    DestinationSourceMasterDao destinationSourceMasterDao;
    
    @Override
    public boolean setCartDetails(CommerceCheckoutParameter parameter) {

        final CartModel cartModel = parameter.getCart();
        SubAreaMasterModel subAreaMaster = null;
        DistrictMasterModel districtMaster = null;
        RegionMasterModel regionMaster = null;
        StateMasterModel stateMaster = null;
        
        validateParameterNotNull(cartModel, "Cart model cannot be null");
        cartModel.setOrderType(OrderType.valueOf(parameter.getOrderType()));
        cartModel.setIsDealerProvideOwnTransport(parameter.getIsDealerProvideOwnTransport());
        cartModel.setDeliveryMode(parameter.getDeliveryMode());
        cartModel.setPaymentType(CheckoutPaymentType.ACCOUNT);
        cartModel.setTotalQuantity(parameter.getTotalQuantity());
        cartModel.setWarehouse(eydmsWarehouseDao.findWarehouseByCode(parameter.getOrderSource()));
        if(null != parameter.getRetailerCode()) {
           cartModel.setRetailer((EyDmsCustomerModel) userService.getUserForUID(parameter.getRetailerCode()));
        }
        cartModel.setProductCode(parameter.getProductCode());
        cartModel.setProductName(parameter.getProductName());
        if(parameter.getOrderType().equals(OrderType.ISO.getCode()) && null != parameter.getDestination()) {
            cartModel.setDestination(eydmsWarehouseDao.findWarehouseByCode(parameter.getDestination()));
        }
        
		if(OrderType.ISO.getCode().equals(parameter.getOrderType())) {
			final AddressModel addressModel = parameter.getAddress();
			cartModel.setDeliveryAddress(addressModel);
			addressModel.setShippingAddress(Boolean.TRUE);
			getModelService().save(addressModel);
		}
		if(cartModel.getUser() instanceof EyDmsCustomerModel) {
			List<SubAreaMasterModel> subareas = territoryService.getTerritoriesForCustomer((EyDmsCustomerModel)cartModel.getUser());
			if(subareas!=null && !subareas.isEmpty()) {
				subAreaMaster = subareas.get(0);
				if(subAreaMaster!=null) {
					cartModel.setSubAreaMaster(subAreaMaster);	
					districtMaster = subAreaMaster.getDistrictMaster();
					if(districtMaster!=null) {
						cartModel.setDistrictMaster(districtMaster);
						regionMaster = districtMaster.getRegion();
						if(regionMaster!=null) {
							cartModel.setRegionMaster(regionMaster);
							stateMaster = regionMaster.getState();
							if(stateMaster!=null){
								cartModel.setStateMaster(stateMaster);
							}
						}
					}
				}
			}

		}
		List<FreightAndIncoTermsMasterModel> freightAndIncoTerms = findFreightAndIncoTerms(cartModel.getDeliveryAddress().getState(), cartModel.getDeliveryAddress().getDistrict(),
				cartModel.getSite(), cartModel.getWarehouse().getType().getCode());
		IncoTerms incoTerm = null;
		FreightTerms freightTerm = null;
		if(freightAndIncoTerms != null && !freightAndIncoTerms.isEmpty()) {
			for (FreightAndIncoTermsMasterModel f : freightAndIncoTerms) {
				incoTerm = IncoTerms.valueOf(f.getIncoTerms());
				freightTerm = FreightTerms.valueOf(f.getFrieghtTerms());
				break;
			}
		}
		
		if(cartModel.getEntries()!=null) {
			for(AbstractOrderEntryModel cartEntryModel : cartModel.getEntries()) {
				cartEntryModel.setSubAreaMaster(subAreaMaster);
				cartEntryModel.setDistrictMaster(districtMaster);
				cartEntryModel.setRegionMaster(regionMaster);
				cartEntryModel.setStateMaster(stateMaster);
				cartEntryModel.setSite(cartModel.getSite());
				cartEntryModel.setUser(cartModel.getUser());
				
				cartEntryModel.setFob(incoTerm);
				cartEntryModel.setFreightTerms(freightTerm);
				if(cartEntryModel.getFob()!=null && cartEntryModel.getFob().equals(IncoTerms.EX)) {
					cartEntryModel.setEpodCompleted(true);
				}
				if(cartEntryModel.getProduct()!=null && cartModel.getDeliveryMode()!=null && cartModel.getDeliveryAddress()!=null && cartModel.getSite()!=null) {
					DestinationSourceMasterModel destinationSource =  destinationSourceMasterDao.getDestinationSourceBySource(OrderType.SO,  CustomerCategory.TR, cartEntryModel.getSource(), cartModel.getDeliveryMode(), cartModel.getDeliveryAddress().getErpCity(), cartModel.getDeliveryAddress().getDistrict(), cartModel.getDeliveryAddress().getState(), cartModel.getSite(), 
							cartEntryModel.getProduct().getGrade(), cartEntryModel.getProduct().getBagType(), cartModel.getDeliveryAddress().getTaluka());
					if(destinationSource!=null) {
						if(destinationSource.getDistance()!=null) {
							cartEntryModel.setDistance(destinationSource.getDistance().doubleValue());
						}
						cartEntryModel.setRouteId(destinationSource.getRoute());
						
				        cartModel.setRouteId(destinationSource.getRoute());

					}
				}
				getModelService().save(cartEntryModel);
			}
		}
		CustDepotMasterModel custDepotForCustomer =  territoryService.getCustDepotForCustomer((EyDmsCustomerModel)cartModel.getUser());		
		cartModel.setCustDepot(custDepotForCustomer);

		if(parameter.getRequestedDeliverySlot()!=null)
		cartModel.setRequestedDeliveryslot(DeliverySlots.valueOf(parameter.getRequestedDeliverySlot()));
		if(parameter.getRequestedDeliveryDate()!=null)
			cartModel.setRequestedDeliveryDate(parameter.getRequestedDeliveryDate());

        getModelService().save(cartModel);

        final CommerceCartParameter commerceCartParameter = new CommerceCartParameter();
        commerceCartParameter.setEnableHooks(true);
        commerceCartParameter.setCart(cartModel);
        getCommerceCartCalculationStrategy().calculateCart(commerceCartParameter);
        return true;
    }
    
    
    private List<FreightAndIncoTermsMasterModel> findFreightAndIncoTerms(String state, String district, BaseSiteModel brand, String orgType) {
        final Map<String, Object> attr = new HashMap<String, Object>();
        attr.put(FreightAndIncoTermsMasterModel.STATE, state.toUpperCase());
        attr.put(FreightAndIncoTermsMasterModel.DISTRICT, district.toUpperCase());
        attr.put(FreightAndIncoTermsMasterModel.BRAND, brand.getUid());
        attr.put(FreightAndIncoTermsMasterModel.ORGTYPE, orgType);
        String queryResult="SELECT {f:pk} from {FreightAndIncoTermsMaster as f} where UPPER({f:state})=?state and UPPER({f:district})=?district and {f:brand}=?brand and {f:orgType}=?orgType";

        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
        query.getQueryParameters().putAll(attr);
        final SearchResult<FreightAndIncoTermsMasterModel> result = flexibleSearchService.search(query);
        if(result.getResult() != null && !result.getResult().isEmpty())
        {
            return result.getResult();
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

}
