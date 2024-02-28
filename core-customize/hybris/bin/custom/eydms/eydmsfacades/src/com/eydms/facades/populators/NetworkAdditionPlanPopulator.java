package com.eydms.facades.populators;

import com.eydms.core.model.NetworkAdditionPlanModel;
import com.eydms.facades.data.EYDMSNetworkAdditionPlanData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class NetworkAdditionPlanPopulator implements Populator<NetworkAdditionPlanModel, EYDMSNetworkAdditionPlanData> {
    @Autowired
    UserService userService;
    @Override
    public void populate(NetworkAdditionPlanModel source, EYDMSNetworkAdditionPlanData target) throws ConversionException {
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(currentUser!=null && currentUser.getUserType().getCode().equals("SO")){

            if(Objects.nonNull(source.getSubAreaMaster())) {
                target.setTaluka(source.getSubAreaMaster().getPk().toString());
            }
            target.setTotalCounter(source.getTotalCounter());
            target.setShreeCounter(source.getShreeCounter());
            if(Objects.nonNull(source.getSystemProposed())){
                target.setSystemProposed(source.getSystemProposed());
            }
            else {
                target.setSystemProposed(0);
            }
            if(Objects.nonNull(source.getDisableFormCompletion())){
                if (source.getDisableFormCompletion().equals(Boolean.TRUE)) {
                    target.setDisableFormCompletion(Boolean.TRUE);
                }
                else
                    target.setDisableFormCompletion(Boolean.FALSE);
            }
            else
                target.setDisableFormCompletion(Boolean.FALSE);

            if(Objects.nonNull(source.getRevisedPlan())) {
                target.setRevisedPlan(source.getRevisedPlan());
            }else{
                target.setRevisedPlan(0);
            }
            if(Objects.nonNull(source.getReason())) {
                target.setReason(source.getReason());
            }else{
                target.setReason(" ");
            }

        }
        /*if(Objects.nonNull(source.getSubAreaMaster())) {
            target.setTaluka(source.getSubAreaMaster().getPk().toString());
        }
        target.setTotalCounter(source.getTotalCounter());
        target.setShreeCounter(source.getShreeCounter());
        target.setSystemProposed(source.getSystemProposed());
        if(Objects.nonNull(source.getRevisedPlan())) {
            target.setRevisedPlan(source.getRevisedPlan());
        }else{
            target.setRevisedPlan(0);
        }
        if(Objects.nonNull(source.getReason())) {
            target.setReason(source.getReason());
        }else{
            target.setReason(" ");
        }*/

        else if(currentUser.getUserType().getCode().equals("TSM")){
            target.setSubAreaName(source.getSubAreaMaster().getTaluka());
            target.setSoName(source.getRaisedBy().getName());
            target.setAdditionCount(source.getRevisedPlan());
            target.setCity(source.getCity());
            target.setTaluka(source.getTaluka());
            target.setDistrict(source.getDistrict());
           // target.setDistrictName(source.getDistrictMaster().getName());
            target.setDistrictName(source.getDistrict());
            target.setTotalCounter(source.getTotalCounter());
            target.setShreeCounter(source.getShreeCounter());

            if(Objects.nonNull(source.getEnableApproveFormCompletion())){
                if (source.getEnableApproveFormCompletion().equals(Boolean.FALSE)) {
                    target.setEnableApproveFormCompletion(Boolean.FALSE);
                }
                else
                    target.setEnableApproveFormCompletion(Boolean.TRUE);
            }
            else
                target.setEnableApproveFormCompletion(Boolean.TRUE);

            if(Objects.nonNull(source.getEnableRevisedFormCompletion())){
                if (source.getEnableRevisedFormCompletion().equals(Boolean.FALSE)) {
                    target.setEnableRevisedFormCompletion(Boolean.FALSE);
                }
                else
                    target.setEnableRevisedFormCompletion(Boolean.TRUE);
            }
            else
                target.setEnableRevisedFormCompletion(Boolean.TRUE);
        }

        else if(currentUser.getUserType().getCode().equals("RH")){
            target.setDistrict(source.getDistrict());
            target.setDiName(source.getRevisedBy().getName());
            target.setAdditionCount(source.getRevisedPlan());
            target.setTaluka(source.getTaluka());
            target.setDistrictName(source.getDistrictMaster().getName());
            target.setTotalCounter(source.getTotalCounter());
            target.setShreeCounter(source.getShreeCounter());
            target.setCity(source.getCity());
            if(Objects.nonNull(source.getEnableApproveFormCompletion())){
                if (source.getEnableApproveFormCompletion().equals(Boolean.FALSE)) {
                    target.setEnableApproveFormCompletion(Boolean.FALSE);
                }
                else
                    target.setEnableApproveFormCompletion(Boolean.TRUE);
            }
            else
                target.setEnableApproveFormCompletion(Boolean.TRUE);

            if(Objects.nonNull(source.getEnableRevisedFormCompletion())){
                if (source.getEnableRevisedFormCompletion().equals(Boolean.FALSE)) {
                    target.setEnableRevisedFormCompletion(Boolean.FALSE);
                }
                else
                    target.setEnableRevisedFormCompletion(Boolean.TRUE);
            }
            else
                target.setEnableRevisedFormCompletion(Boolean.TRUE);
        }
    }
}
