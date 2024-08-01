package com.scl.core.services;

import com.scl.core.enums.CompetitorProductType;
import com.scl.core.enums.PremiumProductType;
import com.scl.core.model.*;
import com.scl.facades.data.SclSiteMasterData;
import com.scl.facades.data.SiteRequestData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface SiteManagementService {
    List<SiteServiceTypeModel> getSiteServiceType();

    List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode);

    List<CompetitorProductType> getSiteCategoryType();

    List<PremiumProductType> getSiteCementType(String siteCategoryType);

    List<CompetitorProductModel> getSiteCementBrand(String siteCementType);

    Double getActualTargetForSalesMTD(SclUserModel sclUser);

    Double getMonthlySalesTarget(SclUserModel user);

    Double getLastMonthSalesTarget(SclUserModel user);

    Integer getNewSiteVists(SclUserModel user);

    Integer getNewSiteVistsForLastMonth(SclUserModel user);
    List<List<Object>> getSiteTypeStagesCount(SclUserModel user);
    
	VisitMasterModel createAndStartComplaintVisit(String siteId, String requestId);

    SearchPageData<SclSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday,List<String> filterBySubAreas);

    List<List<Object>> getTotalPremiumOfSitesAndBags(SclUserModel sclUser);

    Integer cmConvertedTargetVisitPremium(SclUserModel sclUser);

    Integer lmConvertedActualVisitPremium(SclUserModel sclUser);

    Double cmConvertedActualBagTotal(SclUserModel sclUser);

    Double lmConvertedActualBagTotal(SclUserModel sclUser);

    Double getTotalPremiumOfSite(SclUserModel sclUser, String startDate,String endDate, String conversionType);

    double calculateBagsCount(SclSiteMasterModel siteMasterModel, Date currDate, String type);

    /**
     * Fetches the Premium/Total Conversion Sale for a given TSO within a daterange
     * @param tsoUser
     * @param startDate
     * @param endDate
     * @param conversionType = Premium/Total
     * @return the Premium/Total Conversion Sale for a given TSO within a daterange
     */
    Double getSiteConversionSale(SclUserModel tsoUser, String startDate, String endDate, String conversionType);
    List<String> getSiteMasterListTaluka();
}
