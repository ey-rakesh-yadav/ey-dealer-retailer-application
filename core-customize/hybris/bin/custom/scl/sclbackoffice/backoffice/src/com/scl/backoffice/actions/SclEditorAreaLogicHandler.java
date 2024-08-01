package com.scl.backoffice.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.FreightTerms;
import com.scl.core.enums.IncoTerms;
import com.scl.core.enums.OrderType;
import com.scl.core.model.FreightAndIncoTermsMasterModel;
import com.scl.core.order.services.OrderValidationProcessService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;

import com.hybris.backoffice.editorarea.BackofficeEditorAreaLogicHandler;

public class SclEditorAreaLogicHandler extends BackofficeEditorAreaLogicHandler{

	@Resource
	ModelService modelService;
	
	@Resource
	OrderValidationProcessService orderValidationProcessService;
	
	@Resource
	B2BUnitService b2bUnitService;
	
	@Resource
	BaseSiteService baseSiteService;
	
	@Resource
	BusinessProcessService businessProcessService;
	
	@Autowired
    FlexibleSearchService flexibleSearchService;
	
	@Autowired
    TerritoryManagementService territoryManagementService;
	
	@Autowired
	DestinationSourceMasterDao destinationSourceMasterDao;
	
	@Autowired
	DeliveryModeDao deliveryModeDao;
	
	@Override
    public Object performSave(final WidgetInstanceManager widgetInstanceManager, final Object currentObject)
            throws ObjectSavingException
    {
		if(currentObject.getClass().equals(OrderEntryModel.class)) {
			OrderEntryModel entry = ((OrderEntryModel)currentObject);
			entry.setCalculatedDeliveryDate(entry.getExpectedDeliveryDate());
			//entry.setCalculatedDeliveryslot(entry.getExpectedDeliveryslot());
			entry.setCalculatedSlot(entry.getExpectedSlot());

			if(entry.getOrder()!=null)
			{
				if(entry.getOrder().getProductName()==null)
				{
					entry.getOrder().setProductName(entry.getProduct()!=null? entry.getProduct().getName():null);
				}
				
				if(entry.getOrder().getProductCode()==null)
				{
					entry.getOrder().setProductCode(entry.getProduct()!=null? entry.getProduct().getCode():null);
				}
				
				if(entry.getIsQuantityAdded().equals(Boolean.FALSE))
				{
					if(entry.getOrder().getTotalQuantity()!=null)
					{
						entry.getOrder().setTotalQuantity(entry.getOrder().getTotalQuantity() + (((double)(entry.getQuantity()))/1000));
					}
					else
					{
						entry.getOrder().setTotalQuantity((((double)(entry.getQuantity()))/1000));
					}
					
					entry.setIsQuantityAdded(Boolean.TRUE);
				}
						
				if(entry.getOrder().getWarehouse()==null)
				{
					entry.getOrder().setWarehouse(entry.getSource());
				}
				
				modelService.save(entry.getOrder());
				
				if(!Objects.isNull(entry.getOrder().getDeliveryAddress()))
				{
					List<FreightAndIncoTermsMasterModel> freightAndIncoTerms = findFreightAndIncoTerms(entry.getOrder().getDeliveryAddress().getState(), entry.getOrder().getDeliveryAddress().getDistrict(),
							entry.getOrder().getSite(), entry.getSource().getType().getCode());
					IncoTerms incoTerm = null;
					FreightTerms freightTerm = null;
					if(freightAndIncoTerms != null && !freightAndIncoTerms.isEmpty()) {
						for (FreightAndIncoTermsMasterModel f : freightAndIncoTerms) {
							incoTerm = IncoTerms.valueOf(f.getIncoTerms());
							freightTerm = FreightTerms.valueOf(f.getFrieghtTerms());
							break;
						}
					}
					
					entry.setFob(incoTerm);
					entry.setFreightTerms(freightTerm);
					
					String route = destinationSourceMasterDao.getDestinationSourceBySource(OrderType.SO,  CustomerCategory.TR, entry.getSource(), entry.getOrder().getDeliveryMode(), entry.getOrder().getDeliveryAddress().getErpCity(), entry.getOrder().getDeliveryAddress().getDistrict(), entry.getOrder().getDeliveryAddress().getState(), entry.getOrder().getSite(), 
							entry.getProduct().getGrade(), entry.getProduct().getBagType(), entry.getOrder().getDeliveryAddress().getTaluka()).getRoute();
					entry.setRouteId(route);
				}
				
				
			}
			
			entry.setQuantityInMT((((double)(entry.getQuantity()))/1000));
			entry.setTotalPrice((entry.getBasePrice()!=null ? entry.getBasePrice() : 0.0) * (entry.getQuantity()!=null ? entry.getQuantity() : 0.0));
				
			return super.performSave(widgetInstanceManager, entry);
		}
		
		if(currentObject.getClass().equals(OrderModel.class)) {
			OrderModel order = ((OrderModel) currentObject);
			BaseSiteModel site = null;
			B2BUnitModel shree = (B2BUnitModel) b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID);
			B2BUnitModel bangur = (B2BUnitModel) b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID);
			B2BUnitModel rockstrong = (B2BUnitModel) b2bUnitService.getUnitForUid(SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID);
			
			
			order.setOrderType(OrderType.SO);
			
			List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode("ROAD");
			if(deliveryModeList!=null && (!deliveryModeList.isEmpty()) )
			{
				order.setDeliveryMode(deliveryModeList.get(0));
			}
			
			if(order.getUser()!=null)
			{
				if(order.getUser().getGroups().contains(shree))
				{
					site = baseSiteService.getBaseSiteForUID(SclCoreConstants.SITE.SHREE_SITE);
				}
				else if(order.getUser().getGroups().contains(bangur))
				{
					site = baseSiteService.getBaseSiteForUID(SclCoreConstants.SITE.BANGUR_SITE);
				}
				else if(order.getUser().getGroups().contains(rockstrong))
				{
					site = baseSiteService.getBaseSiteForUID(SclCoreConstants.SITE.ROCKSTRONG_SITE);
				}
				
				order.setSite(site);
				order.setStore(site.getStores().get(0));
				
//				order.setCurrency(order.getStore().getDefaultCurrency());
				
				baseSiteService.setCurrentBaseSite(site, Boolean.FALSE);
				order.setSubAreaMaster(territoryManagementService.getTerritoriesForCustomer(order.getUser().getUid()).get(0));
				
			}
			
			
			if(!Objects.isNull(order.getPlacedBy()))
			{
				order.setRouteId(order.getEntries().get(0).getRouteId());
				final OrderProcessModel orderProcessModel = (OrderProcessModel) businessProcessService.createProcess(
						"order-process-" + order.getCode() + "-" + System.currentTimeMillis(),
						"order-process");
				orderProcessModel.setOrder(order);
				modelService.save(orderProcessModel);
				businessProcessService.startProcess(orderProcessModel);
			}
			
			return super.performSave(widgetInstanceManager, order);
		}
		
        return super.performSave(widgetInstanceManager, currentObject);
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
            return Collections.emptyList();
        }
    }
    
		
}
	
	

