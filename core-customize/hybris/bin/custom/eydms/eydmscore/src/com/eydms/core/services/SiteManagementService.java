package com.eydms.core.services;

import com.eydms.core.model.*;
import com.eydms.facades.data.EyDmsSiteMasterData;
import com.eydms.facades.data.SiteRequestData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.time.LocalDate;
import java.util.List;

public interface SiteManagementService {
    List<SiteServiceTypeModel> getSiteServiceType();

    List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode);

    List<SiteCategoryTypeModel> getSiteCategoryType();

    List<SiteCementTypeModel> getSiteCementType(String siteCategoryType);

    List<SiteCementBrandModel> getSiteCementBrand(String siteCementType);

    Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser);

    Double getMonthlySalesTarget(EyDmsUserModel user);

    Double getLastMonthSalesTarget(EyDmsUserModel user);

    Integer getNewSiteVists(EyDmsUserModel user);

    Integer getNewSiteVistsForLastMonth(EyDmsUserModel user);
    List<List<Object>> getSiteTypeStagesCount(EyDmsUserModel user);
    
	VisitMasterModel createAndStartComplaintVisit(String siteId, String requestId);

    SearchPageData<EyDmsSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday);

    List<List<Object>> getTotalPremiumOfSitesAndBags(EyDmsUserModel eydmsUser);

    Integer cmConvertedTargetVisitPremium(EyDmsUserModel eydmsUser);

    Integer lmConvertedActualVisitPremium(EyDmsUserModel eydmsUser);

    Double cmConvertedActualBagTotal(EyDmsUserModel eydmsUser);

    Double lmConvertedActualBagTotal(EyDmsUserModel eydmsUser);

    Double getTotalPremiumOfSite(EyDmsUserModel eydmsUser, String startDate,String endDate);
}
