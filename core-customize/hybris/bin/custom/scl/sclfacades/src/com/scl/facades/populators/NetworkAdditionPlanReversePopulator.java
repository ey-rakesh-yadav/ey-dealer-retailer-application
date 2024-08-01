package com.scl.facades.populators;

import com.scl.core.enums.LeadType;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.NetworkAdditionPlanModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.SCLNetworkAdditionPlanData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Objects;

public class NetworkAdditionPlanReversePopulator implements Populator<SCLNetworkAdditionPlanData, NetworkAdditionPlanModel> {
    @Resource
    private TerritoryManagementService territoryManagementService;
    @Autowired
    UserService userService;

    @Resource
    BaseSiteService baseSiteService;

    @Resource
    private KeyGenerator customCodeGenerator;

    @Override
    public void populate(SCLNetworkAdditionPlanData source, NetworkAdditionPlanModel target) throws ConversionException {
        var subArea=territoryManagementService.getTerritoryById(source.getTaluka());
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        if(Objects.nonNull(subArea)) {
            target.setSubAreaMaster(subArea);
        }
        target.setBrand(currentBaseSite);
        target.setId(String.valueOf(customCodeGenerator.generate()));
        target.setTotalCounter(source.getTotalCounter());
        target.setShreeCounter(source.getShreeCounter());
        target.setSystemProposed(source.getSystemProposed());
        target.setRevisedPlan(source.getRevisedPlan());
        target.setCity(source.getCity());
        target.setDistrict(source.getDistrict());
        target.setTaluka(source.getTaluka());
        target.setReason(source.getReason());
        target.setApplicableTo(LeadType.valueOf(source.getApplicableLead()));
        target.setDisableFormCompletion(Boolean.TRUE);
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(currentUser!=null && currentUser.getUserType().equals(SclUserType.SO)){
            target.setRaisedBy((SclUserModel) currentUser);
        }
        if (currentUser.getUserType().getCode().equals("TSM")) {
            if(Objects.nonNull(subArea)) {
                target.setTaluka(subArea.getTaluka());
                target.setDistrict(subArea.getDistrict());
            }
        target.setRevisedPlan(source.getRevisedPlan()); //additonproposedbySO
        
        }



//        if(currentUser!=null && currentUser.getUserType().equals(SclUserType.TSM)){
//            target.setSubAreaMaster(so);
//        }

    }
}
