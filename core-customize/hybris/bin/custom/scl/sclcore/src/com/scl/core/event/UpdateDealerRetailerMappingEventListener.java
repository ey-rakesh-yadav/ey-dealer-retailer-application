package com.scl.core.event;

import com.scl.core.customer.services.SclCustomerAccountService;
import com.scl.core.model.DealerRetailerMapModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class UpdateDealerRetailerMappingEventListener extends AbstractEventListener<UpdateDealerRetailerMappingEvent> {

    @Resource(name="customerAccountService")
    private SclCustomerAccountService sclCustomerAccountService;

    @Resource
    private ModelService modelService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    private static final Logger LOG = Logger.getLogger(UpdateDealerRetailerMappingEventListener.class);

    @Override
    protected void onEvent(UpdateDealerRetailerMappingEvent updateDealerRetailerMappingEvent) {

        try{
            OrderModel order = updateDealerRetailerMappingEvent.getOrder();
            AddressModel deliveryAddress = order.getDeliveryAddress();
            if(null!= deliveryAddress  && StringUtils.isBlank(deliveryAddress.getPartnerFunctionId())){
                updateDeliveryAddress(deliveryAddress,order.getRetailer());
            }

            SclCustomerModel dealer = (SclCustomerModel) order.getUser();
            if(order.getUser() instanceof SclCustomerModel){
                dealer = (SclCustomerModel)  order.getUser();
            }
            else {
                LOG.error(String.format("User %s in the order is not Dealer",order.getUser().getUid()));
                return;
            }

            SclCustomerModel retailer = order.getRetailer();
            if(dealer!= null && retailer !=null) {
               // updateDealerRetailerMapping(dealer.getCustomerNo(), retailer.getCustomerNo());
            }
        }
        catch (Exception e){
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void updateDealerRetailerMapping(String dealer, String retailer) {
        DealerRetailerMapModel dealerRetailerMap = new DealerRetailerMapModel();
        dealerRetailerMap.setDealerCustNo(dealer);
        dealerRetailerMap.setRetailerCustNo(retailer);

        List<DealerRetailerMapModel> dealerRetailerMaps = flexibleSearchService.getModelsByExample(dealerRetailerMap);

        if(dealerRetailerMaps.size() == 0){
            DealerRetailerMapModel newDealerRetailerMap = modelService.create(DealerRetailerMapModel.class);
            newDealerRetailerMap.setDealerCustNo(dealer);
            newDealerRetailerMap.setRetailerCustNo(retailer);
            //newDealerRetailerMap.setOrderCount(1L);
            newDealerRetailerMap.setOrderCount(1);
            modelService.save(newDealerRetailerMap);
        }
        else if(dealerRetailerMaps.size() == 1){
            DealerRetailerMapModel dealerRetailerMapModel = dealerRetailerMaps.get(0);
            //dealerRetailerMapModel.setOrderCount(null == dealerRetailerMapModel.getOrderCount() ? 1L : dealerRetailerMapModel.getOrderCount()+1);
            dealerRetailerMapModel.setOrderCount(null == dealerRetailerMapModel.getOrderCount() ? 1 : dealerRetailerMapModel.getOrderCount()+1);
            modelService.save(dealerRetailerMapModel);
        }
        else {
            Optional<DealerRetailerMapModel> toPersistOptional = dealerRetailerMaps.stream().min(Comparator.comparing(DealerRetailerMapModel::getCreationtime));
            List<DealerRetailerMapModel> toRemove = dealerRetailerMaps.stream().filter(map -> !(map.getPk().equals(toPersistOptional.get().getPk()))).collect(Collectors.toList());
            modelService.removeAll(toRemove);
            DealerRetailerMapModel toPersist = toPersistOptional.get();
            //toPersist.setOrderCount(null == toPersist.getOrderCount() ? 1L : toPersist.getOrderCount()+1);
            toPersist.setOrderCount(null == toPersist.getOrderCount() ? 1 : toPersist.getOrderCount()+1);
            modelService.save(toPersist);
        }

    }

    private void updateDeliveryAddress(AddressModel deliveryAddress, SclCustomerModel retailer) {
        deliveryAddress.setPartnerFunctionId(retailer.getUid());
        deliveryAddress.setRetailerName(retailer.getName());
        modelService.save(deliveryAddress);

        //update original Address as well
        if(null!= deliveryAddress.getOriginal()){
            AddressModel dealerAddress = deliveryAddress.getOriginal();
            dealerAddress.setPartnerFunctionId(retailer.getUid());
            dealerAddress.setRetailerName(retailer.getName());
            modelService.save(dealerAddress);
        }

        boolean makeThisAddressTheDefault  =  null!= deliveryAddress.getIsPrimaryAddress() ?
                (deliveryAddress.getIsPrimaryAddress() || CollectionUtils.isEmpty(retailer.getAddresses()) || !checkForExistingPrimaryAddress(new ArrayList<>(retailer.getAddresses())))
                : (CollectionUtils.isEmpty(retailer.getAddresses()) || !checkForExistingPrimaryAddress(new ArrayList<>(retailer.getAddresses())));

        sclCustomerAccountService.saveAddressEntryForRetailer(retailer,deliveryAddress,makeThisAddressTheDefault);

    }

    private boolean checkForExistingPrimaryAddress(List<AddressModel> addressess) {

        List<AddressModel> dealerPrimaryAddressList = addressess.stream().filter(AddressModel::getIsPrimaryAddress).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(dealerPrimaryAddressList)){
            return true;
        }
        else{
            return false;
        }
    }
}