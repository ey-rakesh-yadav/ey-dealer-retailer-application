package com.eydms.core.order.strategy.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.DeliverySlots;
import com.eydms.core.enums.FreightTerms;
import com.eydms.core.enums.IncoTerms;
import com.eydms.core.enums.OrderType;
import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.model.FreightAndIncoTermsMasterModel;
import com.eydms.core.order.EYDMSB2BOrderService;
import com.eydms.core.order.strategy.EyDmsModifyOrderStrategy;
import com.eydms.core.services.SlctCrmIntegrationService;
import com.eydms.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.order.impl.AbstractCommerceCartStrategy;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceAddToCartStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.*;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.order.AbstractOrderEntryTypeService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultEyDmsModifyOrderStrategy extends DefaultCommerceAddToCartStrategy implements EyDmsModifyOrderStrategy {

    private static final Logger LOGGER = Logger.getLogger(DefaultEyDmsModifyOrderStrategy.class);

    protected static final int APPEND_AS_LAST = -1;

    @Resource
    private CartService cartService;

    @Resource
    private OrderService orderService ;

    @Resource
    private  AbstractOrderEntryTypeService abstractOrderEntryTypeService;

    @Resource
    private EYDMSB2BOrderService b2bOrderService;

    @Resource
    private BusinessProcessService businessProcessService;

    @Autowired
    WarehouseService warehouseService;
    
    @Autowired
    SlctCrmIntegrationService slctCrmIntegrationService;
    
    @Autowired
    DestinationSourceMasterDao destinationSourceMasterDao;
    
    @Override
    public void modiyOrderEntry(final OrderModel orderModel , final CommerceCartParameter parameter ) throws CommerceCartModificationException {

        final UnitModel unit = getUnit(parameter);
        final long actualAllowedQuantityChange = getAllowedOrderAdjustmentForProduct(parameter.getProduct(),parameter.getQuantity() ,parameter.getPointOfService());
        final AbstractOrderEntryModel abstractOrderEntryModel = b2bOrderService.addNewOrderEntry(orderModel,parameter.getProduct(),(int)actualAllowedQuantityChange,unit,(int)parameter.getEntryNumber());
        abstractOrderEntryModel.setTruckNo(parameter.getTruckNo());
        abstractOrderEntryModel.setDriverContactNo(parameter.getDriverContactNo());
        if(parameter.getSelectedDeliverySlot()!=null) {
            abstractOrderEntryModel.setExpectedDeliveryslot(DeliverySlots.valueOf(parameter.getSelectedDeliverySlot()));
        }
        if(parameter.getSelectedDeliveryDate()!=null) {
            abstractOrderEntryModel.setExpectedDeliveryDate(setSelectedDeliveryDate(parameter.getSelectedDeliveryDate()));
        }
        if(parameter.getCalculatedDeliveryDate()!=null) {
            abstractOrderEntryModel.setCalculatedDeliveryDate(setSelectedDeliveryDate(parameter.getCalculatedDeliveryDate()));
        }
        if(parameter.getCalculatedDeliverySlot()!=null) {
            abstractOrderEntryModel.setCalculatedDeliveryslot(DeliverySlots.valueOf(parameter.getCalculatedDeliverySlot()));
        }
        if(parameter.getQuantityMT()!=null) {
            abstractOrderEntryModel.setQuantityInMT(parameter.getQuantityMT());
        }
        abstractOrderEntryModel.setSequence(parameter.getSequence());
        
        if(parameter.getWarehouseCode()!=null) {
        	WarehouseModel warehouse =  warehouseService.getWarehouseForCode(parameter.getWarehouseCode());
        	abstractOrderEntryModel.setSource(warehouse);
        	
        	List<FreightAndIncoTermsMasterModel> freightAndIncoTerms = slctCrmIntegrationService.findFreightAndIncoTerms(orderModel.getDeliveryAddress().getState(), orderModel.getDeliveryAddress().getDistrict(),
        			orderModel.getSite(), abstractOrderEntryModel.getSource().getType().getCode());
	        if(freightAndIncoTerms != null && !freightAndIncoTerms.isEmpty()) {
	            for (FreightAndIncoTermsMasterModel f : freightAndIncoTerms) {
	            	abstractOrderEntryModel.setFob(IncoTerms.valueOf(f.getIncoTerms()));
	            	abstractOrderEntryModel.setFreightTerms(FreightTerms.valueOf(f.getFrieghtTerms()));
	            	if(abstractOrderEntryModel.getFob()!=null && abstractOrderEntryModel.getFob().equals(IncoTerms.EX))
	            		abstractOrderEntryModel.setEpodCompleted(true);
	                break;
	            }
	        }
        }
        if(abstractOrderEntryModel.getProduct()!=null && orderModel.getDeliveryMode()!=null && orderModel.getDeliveryAddress()!=null && orderModel.getSite()!=null) {
				DestinationSourceMasterModel destinationSource =  destinationSourceMasterDao.getDestinationSourceBySource(OrderType.SO,  CustomerCategory.TR, abstractOrderEntryModel.getSource(), orderModel.getDeliveryMode(), orderModel.getDeliveryAddress().getErpCity(), orderModel.getDeliveryAddress().getDistrict(), orderModel.getDeliveryAddress().getState(), orderModel.getSite(), 
						abstractOrderEntryModel.getProduct().getGrade(), abstractOrderEntryModel.getProduct().getBagType(), orderModel.getDeliveryAddress().getTaluka());
				if(destinationSource!=null) {
					if(destinationSource.getDistance()!=null) {
						abstractOrderEntryModel.setDistance(destinationSource.getDistance().doubleValue());
					}
					abstractOrderEntryModel.setRouteId(destinationSource.getRoute());
					orderModel.setRouteId(destinationSource.getRoute());
				}
			}
        //abstractOrderEntryModel.setRouteId(parameter.getRouteId());
        abstractOrderEntryModel.setRemarks(parameter.getRemarks());
        getModelService().save(abstractOrderEntryModel);
    }

    private Date setSelectedDeliveryDate(String selectedDeliveryDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDeliveryDate);
        } catch (ParseException e) {
            LOGGER.error("Error Parsing Selected Delivery Date", e);
            throw new IllegalArgumentException(String.format("Please provide valid date %s", selectedDeliveryDate));
        }
        return date;
    }

    protected long getAllowedOrderAdjustmentForProduct(final ProductModel productModel,
                                                      final long quantityToAdd, final PointOfServiceModel pointOfServiceModel)
    {

        final long stockLevel = getAvailableStockLevel(productModel, pointOfServiceModel);

        // How many will we have in our cart if we add quantity
        //final long newTotalQuantity = quantityToAdd;

        // Now limit that to the total available in stock
        final long newTotalQuantityAfterStockLimit = Math.min(quantityToAdd, stockLevel);

        // So now work out what the maximum allowed to be added is (note that
        // this may be negative!)
        final Integer maxOrderQuantity = productModel.getMaxOrderQuantity();

        if (isMaxOrderQuantitySet(maxOrderQuantity))
        {
            return Math.min(newTotalQuantityAfterStockLimit, maxOrderQuantity.longValue());
        }
        return newTotalQuantityAfterStockLimit ;
    }


}
