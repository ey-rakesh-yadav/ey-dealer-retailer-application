package com.eydms.facades.populators;

import com.eydms.core.model.ComplaintTestPerformedModel;
import com.eydms.core.model.SiteServiceTestModel;
import com.eydms.facades.data.ComplaintTestPerformedData;
import com.eydms.facades.data.ServiceTypeData;
import com.eydms.facades.data.ServiceTypeTestData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.ArrayList;
import java.util.List;

public class ComplaintTestPerformedPopulator implements Populator<ComplaintTestPerformedModel, ComplaintTestPerformedData> {
    @Override
    public void populate(ComplaintTestPerformedModel source, ComplaintTestPerformedData target) throws ConversionException {

        target.setId(source.getId());
        target.setComplaintId(source.getComplaint().getRequestId());
        if(source.getServiceType()!=null){
            ServiceTypeData serviceTypeData = new ServiceTypeData();
            serviceTypeData.setCode(source.getServiceType().getCode());
            serviceTypeData.setName(source.getServiceType().getName());
            target.setServiceType(serviceTypeData);
        }
        List<ServiceTypeTestData> list = new ArrayList<>();
        if(source.getServiceTypeTest()!=null && !source.getServiceTypeTest().isEmpty()){
            for(SiteServiceTestModel siteServiceTestModel: source.getServiceTypeTest()){
                ServiceTypeTestData serviceTypeTestData = new ServiceTypeTestData();
                serviceTypeTestData.setCode(siteServiceTestModel.getCode());
                serviceTypeTestData.setName(siteServiceTestModel.getName());
                list.add(serviceTypeTestData);
            }

        }
        target.setServiceTest(list);

    }
}
