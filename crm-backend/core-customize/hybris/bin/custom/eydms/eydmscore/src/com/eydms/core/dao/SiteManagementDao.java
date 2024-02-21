package com.eydms.core.dao;

import com.eydms.core.model.*;
import com.eydms.facades.data.SiteRequestData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SiteManagementDao {
    List<SiteServiceTypeModel> getSiteServiceType();

    List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode);

    List<SiteCategoryTypeModel> getSiteCategoryType();

    List<SiteCementTypeModel> getSiteCementType(String siteCategoryType);

    List<SiteCementBrandModel> getSiteCementBrand(String siteCementType);

    Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser);

    Double getMonthlySalesTarget(EyDmsUserModel eydmsUser);

    Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser);



    Integer getNewSiteVists(EyDmsUserModel user);

    Integer getNewSiteVistsForLastMonth(EyDmsUserModel user);

    SiteServiceTypeModel findServiceTypeByCode(String code);

    SiteServiceTestModel findServiceTypeTestByCode(String code);

    SiteCategoryTypeModel findCategoryTypeByCode(String code);

    SiteCementTypeModel findCementTypeByCode(String code);

    SiteCementBrandModel findCementBrandByCode(String code);

    List<List<Object>> getSiteTypeStagesCount(EyDmsUserModel user);

    SearchPageData<EyDmsSiteMasterModel> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday);

   Integer cmConvertedTargetVisitPremium(EyDmsUserModel eydmsUser,Integer month,Integer currentYear);


    Double cmConvertedActualBagTotal(EyDmsUserModel eydmsUser,Integer month, Integer currentYear);

    List<List<Object>> getNumberOfBagsPurchased(EyDmsUserModel user);


    Double getTotalPremiumOfSite(EyDmsUserModel eydmsUser,String startDate,String endDate);
}
