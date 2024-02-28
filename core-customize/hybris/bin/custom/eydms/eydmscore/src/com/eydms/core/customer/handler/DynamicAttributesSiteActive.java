package com.eydms.core.customer.handler;

import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.enums.SiteStatus;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsSiteMasterModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DynamicAttributesSiteActive implements DynamicAttributeHandler<String , EyDmsSiteMasterModel> {
    @Autowired
    DataConstraintDao dataConstraintDao;

    @Override
    public String get(EyDmsSiteMasterModel siteMasterModel) {
        String active ="";
        Integer last_site_active_days = dataConstraintDao.findDaysByConstraintName("LAST_SITE_ACTIVE_DAYS");
        LocalDate last6MonthsDate = LocalDate.now().minusDays(last_site_active_days);
        Date date = Date.from(last6MonthsDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if(siteMasterModel!=null)
        {
            if(siteMasterModel.getSiteStatus()!=null && siteMasterModel.getSiteStatus().equals(SiteStatus.CLOSED))
            {
                active="NO";
            }
            else if(siteMasterModel.getLastSiteVisitDate()!=null && siteMasterModel.getLastSiteVisitDate().before(date))
            {
                active="NO";
            }
            else
                active="YES";
        }
        return active;
    }

    @Override
    public void set(EyDmsSiteMasterModel siteMasterModel, String s) {

    }
}
