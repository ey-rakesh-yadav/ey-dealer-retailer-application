package com.scl.facades.populators.order;

import com.scl.core.model.OrderStageModel;
import com.scl.core.model.OrderSubStageModel;
import com.scl.facades.order.data.OrderStagesData;
import com.scl.facades.order.data.OrderSubStagesData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultOrderStagesPopulator implements Populator<OrderModel, OrderData> {


    @Override
    public void populate(OrderModel source, OrderData target) throws ConversionException {

        /*validateParameterNotNullStandardMessage("source", source);
        Collection<OrderStageModel> orderStages = source.getStages();
        List<OrderStagesData>  orderStagesDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(orderStages)){

            for(OrderStageModel orderStage : orderStages){
                OrderStagesData orderStagesData = new OrderStagesData();
                populateStageAttributes(orderStage,orderStagesData);

                final Set<OrderSubStageModel> subStages = orderStage.getSubStages();
                List<OrderSubStagesData>  orderSubStagesDataList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(subStages)){
                    for(OrderSubStageModel orderSubStage : subStages){
                        OrderSubStagesData orderSubStagesData = new OrderSubStagesData();
                        populateSubStageAttributes(orderSubStage,orderSubStagesData);
                        orderSubStagesDataList.add(orderSubStagesData);
                    }
                    orderStagesData.setSubStages(orderSubStagesDataList);
                }
                orderStagesDataList.add(orderStagesData);
            }

        }

        target.setOrderStages(orderStagesDataList);*/
    }

    /*private void populateStageAttributes(OrderStageModel orderStage, OrderStagesData orderStagesData) {
        orderStagesData.setCode(orderStage.getCode());
        orderStagesData.setName(null!= orderStage.getName() ? orderStage.getName().getCode() : null );
        orderStagesData.setStageCompleted(orderStage.getStageCompleted());
        orderStagesData.setEstimatedTime(orderStage.getEstimatedTime());
        orderStagesData.setActualTime(orderStage.getActualTime());
    }
    private void populateSubStageAttributes(OrderSubStageModel orderSubStage, OrderSubStagesData orderSubStagesData) {
        orderSubStagesData.setCode(orderSubStage.getCode());
        orderSubStagesData.setName(null != orderSubStage.getName() ? orderSubStage.getName().getCode() :null);
        orderSubStagesData.setStageCompleted(orderSubStage.getStageCompleted());
        orderSubStagesData.setEstimatedTime(orderSubStage.getEstimatedTime());
        orderSubStagesData.setActualTime(orderSubStage.getActualTime());
    }*/

}
