package com.scl.core.dao;

import com.scl.core.enums.CompetitorProductType;
import com.scl.core.enums.PremiumProductType;
import com.scl.core.model.*;
import com.scl.facades.data.SiteRequestData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SiteManagementDao {
    List<SiteServiceTypeModel> getSiteServiceType();

    List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode);

    List<CompetitorProductType> getSiteCategoryType();

    List<PremiumProductType> getSiteCementType(String siteCategoryType);

    List<CompetitorProductModel> getSiteCementBrand(String siteCementType);

    Double getActualTargetForSalesMTD(SclUserModel sclUser);

    Double getMonthlySalesTarget(SclUserModel sclUser);

    Double getLastMonthSalesTarget(SclUserModel sclUser);



    Integer getNewSiteVists(SclUserModel user);

    Integer getNewSiteVistsForLastMonth(SclUserModel user);

    SiteServiceTypeModel findServiceTypeByCode(String code);

    SiteServiceTestModel findServiceTypeTestByCode(String code);

    CompetitorProductType findCategoryTypeByCode(String code);

    PremiumProductType findCementTypeByCode(String code);

    SiteCementBrandModel findCementBrandByCode(String code);

    List<List<Object>> getSiteTypeStagesCount(SclUserModel user);

    SearchPageData<SclSiteMasterModel> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday, List<String> filterBySubAreas);

   Integer cmConvertedTargetVisitPremium(SclUserModel sclUser,Integer month,Integer currentYear);


    Double cmConvertedActualBagTotal(SclUserModel sclUser,Integer month, Integer currentYear);

    List<List<Object>> getNumberOfBagsPurchased(SclUserModel user);

    List<SclSiteMasterModel> getTotalPremiumOfSite(SclUserModel sclUser,String startDate,String endDate, String conversionType);

    CompetitorProductModel findCementProductByCode(String code);

    SiteCementTypeModel findSiteCementTypeByCode(String code);

    CompetitorProductModel findCementProductByCodeAndBrand(String code, String state, String brand);

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
