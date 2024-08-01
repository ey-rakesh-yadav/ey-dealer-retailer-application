package com.scl.core.checkout.impl;

import com.scl.core.cart.dao.SclWarehouseDao;
import com.scl.core.checkout.SclCommerceCheckoutService;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2b.model.B2BCustomerModel;
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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

public class DefaultSclCommerceCheckoutService extends DefaultCommerceCheckoutService implements SclCommerceCheckoutService
{
	@Autowired
	UserService userService;

    @Autowired
    SclWarehouseDao sclWarehouseDao;

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
        cartModel.setPaymentType(CheckoutPaymentType.ACCOUNT);
        cartModel.setTotalQuantity(parameter.getTotalQuantity());
		if(cartModel.getUser() instanceof SclCustomerModel) {
	        cartModel.setTerritoryMaster(((SclCustomerModel)cartModel.getUser()).getTerritoryCode());
			List<SubAreaMasterModel> subareas = territoryService.getTerritoriesForCustomer((SclCustomerModel)cartModel.getUser());
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

        if(Objects.nonNull(parameter.getPlacedByCustomer())){
            if(cartModel.getUser() instanceof SclCustomerModel){
                SclCustomerModel sclCustomerModel = (SclCustomerModel) cartModel.getUser();
                if (sclCustomerModel!=null && sclCustomerModel.getPartnerCustomer()!=null){
                    List<PartnerCustomerModel> partnerCustomerModel = sclCustomerModel.getPartnerCustomer().stream().filter(a -> a.getId().equalsIgnoreCase(parameter.getPlacedByCustomer())).collect(Collectors.toList());
                   cartModel.setPlacedByCustomer(partnerCustomerModel.get(0).getName());
                    cartModel.setIsPartnerCustomer(parameter.getIsPartnerCustomer());
                }
                else {
                    if(Objects.nonNull(parameter.getPlacedByCustomer()) && Objects.nonNull(userService.getUserForUID(parameter.getPlacedByCustomer()))) {
                    sclCustomerModel = (SclCustomerModel) userService.getUserForUID(parameter.getPlacedByCustomer());
                    cartModel.setPlacedByCustomer(sclCustomerModel.getName());
                    cartModel.setIsPartnerCustomer(Objects.nonNull(parameter.getIsPartnerCustomer() ? parameter.getIsPartnerCustomer():Boolean.FALSE));
                   }
                }
            }
            if(cartModel.getUser() instanceof SclUserModel){
                if(Objects.nonNull(parameter.getPlacedByCustomer()) && Objects.nonNull(userService.getUserForUID(parameter.getPlacedByCustomer()))) {
                    SclUserModel sclUserModel = (SclUserModel) userService.getUserForUID(parameter.getPlacedByCustomer());
                    cartModel.setPlacedByCustomer(sclUserModel.getContactEmail());
                    cartModel.setIsPartnerCustomer(Objects.nonNull(parameter.getIsPartnerCustomer() ? parameter.getIsPartnerCustomer():Boolean.FALSE));
                }
            }
        }
		
		CustDepotMasterModel custDepotForCustomer =  territoryService.getCustDepotForCustomer((SclCustomerModel)cartModel.getUser());		
		cartModel.setCustDepot(custDepotForCustomer);

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
