package com.scl.facades.populators.prosdealer;

import com.scl.core.model.NominationModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.facades.prosdealer.data.FinancialDetailsData;
import com.scl.facades.prosdealer.data.NominationData;
import com.scl.facades.prosdealer.data.ProsDealerData;
import de.hybris.platform.converters.Populator;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ProsDealerFinancialDetailsPopulator implements Populator<ProspectiveDealerModel, ProsDealerData> {

    @Override
    public void populate(ProspectiveDealerModel source, ProsDealerData target) {


        List<NominationModel> nominations = new ArrayList<>(source.getNominees());
        List<NominationData> nominationDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(nominations)){

            for(NominationModel nomination: nominations){
                NominationData nominationData = new NominationData();
                nominationData.setName(nomination.getName());
                nominationData.setFathersName(nomination.getFathersName());
                nominationData.setPanCard(nomination.getPanCard());
                nominationData.setRelation(nomination.getRelation());
                nominationData.setAadharCard(nomination.getAadharCard());

                nominationDataList.add(nominationData);
            }
        }

        FinancialDetailsData financialDetailsData = new FinancialDetailsData();
        financialDetailsData.setCode(source.getUid());
        financialDetailsData.setNominees(nominationDataList);
        //TODO:: UPDATE CODE
        financialDetailsData.setPanNumber(source.getPanCard());
        financialDetailsData.setAccountNo(source.getBankAccountNo());
        financialDetailsData.setGstIN(source.getGstIN());
        financialDetailsData.setStateOfRegistration(source.getStateOfRegistration());
        financialDetailsData.setIfscCode(source.getIfscCode());

        target.setFinanicialDetails(financialDetailsData);
    }
}