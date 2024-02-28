package com.eydms.core.dao.impl;

import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.SiteManagementDao;
import com.eydms.core.enums.ConstructionStage;
import com.eydms.core.enums.SiteStatus;
import com.eydms.core.model.*;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.SiteRequestData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class SiteManagementDaoImpl implements SiteManagementDao {

    FlexibleSearchService flexibleSearchService;
    UserService userService;
    PaginatedFlexibleSearchService paginatedFlexibleSearchService;
    Map<String, String> siteMasterSortCodeToQueryAlias;
    DataConstraintDao dataConstraintDao;

    @Override
    public List<SiteServiceTypeModel> getSiteServiceType() {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select {pk} from {SiteServiceType}  ");
        // params.put("buildNumber", buildNumber);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SiteServiceTypeModel.class));
        query.addQueryParameters(params);
        final SearchResult<SiteServiceTypeModel> searchResult = flexibleSearchService.search(query);
        List<SiteServiceTypeModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode) {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select {s.pk} from {SiteServiceTest as s join SiteServiceType as t on {s.serviceType}={t.pk}} where {t.code}=?serviceTypeCode  ");
        params.put("serviceTypeCode", serviceTypeCode);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SiteServiceTestModel.class));
        query.addQueryParameters(params);
        final SearchResult<SiteServiceTestModel> searchResult = flexibleSearchService.search(query);
        List<SiteServiceTestModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SiteCategoryTypeModel> getSiteCategoryType() {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select {pk} from {SiteCategoryType}  ");
        // params.put("buildNumber", buildNumber);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SiteCategoryTypeModel.class));
        query.addQueryParameters(params);
        final SearchResult<SiteCategoryTypeModel> searchResult = flexibleSearchService.search(query);
        List<SiteCategoryTypeModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SiteCementTypeModel> getSiteCementType(String siteCategoryType) {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select {s.pk} from {SiteCementType as s join SiteCategoryType as t on {s.siteCategoryType}={t.pk}} where {t.code}=?siteCategoryType  ");
        params.put("siteCategoryType", siteCategoryType);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SiteCementTypeModel.class));
        query.addQueryParameters(params);
        final SearchResult<SiteCementTypeModel> searchResult = flexibleSearchService.search(query);
        List<SiteCementTypeModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<SiteCementBrandModel> getSiteCementBrand(String siteCementType) {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select {s.pk} from {SiteCementBrand as s join SiteCementType as t on {s.siteCementType}={t.pk}} where {t.code}=?siteCementType  ");
        params.put("siteCementType", siteCementType);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SiteCementBrandModel.class));
        query.addQueryParameters(params);
        final SearchResult<SiteCementBrandModel> searchResult = flexibleSearchService.search(query);
        List<SiteCementBrandModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select sum({currentMonthSiteVisit}) from {EyDmsSiteMaster} where {createdBy}=?currentUser and " + EyDmsDateUtility.getMtdClauseQuery("lastVisitTime", params));

        params.put("currentUser", eydmsUser);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getMonthlySalesTarget(EyDmsUserModel eydmsUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        int currentMonth= LocalDate.now().getMonth().getValue();

        String attributeName = "m" + currentMonth;
        LocalDate currentDate =LocalDate.now();
        int year=currentDate.getYear();


        builder.append("select {" + attributeName + "} from {SiteVisitTargetMaster} where {user}=?currentUser and {year}=?year");

        params.put("currentUser", eydmsUser);
        params.put("year", year);


        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
       int lastMonth= LocalDate.now().getMonth().minus(1).getValue();
       LocalDate currentDate =LocalDate.now();
       int year=currentDate.minusMonths(1).getYear();
       String attributeName = "m" + lastMonth;

        builder.append("select {" + attributeName + "} from {SiteVisitTargetMaster} where {user}=?currentUser and {year}=?year");

        params.put("currentUser", eydmsUser);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public Integer getNewSiteVists(EyDmsUserModel user) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select count({pk}) from {EyDmsSiteMaster} where {createdBy}=?currentUser and " + EyDmsDateUtility.getMtdClauseQuery("creationTime", params));
        params.put("currentUser", user);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }



    public Integer getNewSiteVistsForLastMonth(EyDmsUserModel user) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select count({pk}) from {EyDmsSiteMaster} where {createdBy}=?currentUser and " + EyDmsDateUtility.getLastMonthClauseQuery("creationTime", params));
        params.put("currentUser", user);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;
    }
    
    @Override
    public SiteServiceTypeModel findServiceTypeByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SiteServiceType} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SiteServiceTypeModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public SiteServiceTestModel findServiceTypeTestByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SiteServiceTest} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SiteServiceTestModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public SiteCategoryTypeModel findCategoryTypeByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SiteCategoryType} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SiteCategoryTypeModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }
    @Override
    public SiteCementTypeModel findCementTypeByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SiteCementType} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SiteCementTypeModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public SiteCementBrandModel findCementBrandByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SiteCementBrand} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SiteCementBrandModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult()!=null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    public List<List<Object>> getSiteTypeStagesCount(EyDmsUserModel user) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();


            builder.append("select {constructionStage},{enum.code}, count({s.pk}) from {EyDmsSiteMaster as s JOIN EnumerationValue AS enum ON {enum.pk}={s.constructionStage} } where {s.createdBy} =?currentUser group by {s.constructionStage},{enum.code}");

            params.put("currentUser", user);

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class,Integer.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }

    }

    public List<List<Object>> getNumberOfBagsPurchased(EyDmsUserModel user) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();


            builder.append("select Year({s.endVisitTime}),Month({s.endVisitTime}),{s.isPremium},count({s.pk}),sum({s.numberOfBagsPurchased}) from {VisitMaster as v join SiteVisitMaster as s on {s.visit}={v.pk}} where {v.user}=?currentUser  and {s.siteStatus}=?siteStatus and ");
            builder.append(EyDmsDateUtility.getCurrentPreviousMonthClauseQuery("s.endVisitTime", params));
            
            params.put("currentUser", user);
            params.put("siteStatus", SiteStatus.SITE_CONVERTED);

            builder.append(" group by Year({s.endVisitTime}),Month({s.endVisitTime}),{s.isPremium}");

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Integer.class, Integer.class,Boolean.class,Integer.class,Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }

    }

    @Override
    public Double getTotalPremiumOfSite(EyDmsUserModel eydmsUser,String startDate,String endDate) {

            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();

            builder.append("select sum({numberOfBagsPurchased}) from {eydmsSiteMaster} where {createdBy}=?currentUser  and {siteStatus}=?siteStatus and {creationTime} >=?startDate and {creationTime} <?endDate and {ispremium} =?isPremium " );
            params.put("currentUser", eydmsUser);
            params.put("siteStatus", SiteStatus.SITE_CONVERTED);
           params.put("startDate", startDate);
           params.put("endDate",endDate);
        params.put("isPremium",Boolean.TRUE);

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.setResultClassList(Arrays.asList(Double.class));
            query.addQueryParameters(params);
            final SearchResult<Double> searchResult = flexibleSearchService.search(query);
            if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
                return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
            else
                return 0.0;
    }

    @Override
    public SearchPageData<EyDmsSiteMasterModel> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday) {
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder builder = new StringBuilder("select {sm:pk} from {EyDmsSiteMaster as sm} where {sm:createdBy}=?user");
        params.put("user", userService.getCurrentUser());

        if(siteRequestData.getIsYesterdayVisit()!=null && siteRequestData.getIsYesterdayVisit())
        {
                String checkDate = new Date().toString();
                String startDate=LocalDate.now().minusDays(1).toString();
                String endDate=LocalDate.now().minusDays(1).toString() + " 23:59:59";
                if(checkDate.contains("GMT")) {
                    startDate = LocalDate.now().minusDays(2).toString() + " 18:30:00";
                    endDate = LocalDate.now().minusDays(1).toString() + " 18:30:00";
                }
                builder.append(" AND {sm.lastVisitTime} >=?startDate and {sm.lastVisitTime} < ?endDate ");
                params.put("startDate",startDate);
                params.put("endDate",endDate);
        }

        if(siteRequestData.getRemoveClosedSite()!=null && siteRequestData.getRemoveClosedSite())
        {
            Integer last_site_active_days = dataConstraintDao.findDaysByConstraintName("LAST_SITE_ACTIVE_DAYS");
            builder.append(" AND ({sm:siteStatus} =?siteStatus OR ").append(EyDmsDateUtility.getLastXDayQuery("sm:lastVisitTime", params, last_site_active_days)).append(")");
            params.put("siteStatus",SiteStatus.CLOSED);
        }

        if(siteRequestData.getIsApplicableForSiteConversion()!=null && siteRequestData.getIsApplicableForSiteConversion())
        {
            builder.append(" AND {sm:siteCategoryType}=?siteCategory ");
            params.put("siteCategory", findCategoryTypeByCode("CS"));
        }

        if(StringUtils.isNotBlank(siteRequestData.getSearchKey())){
            String search= "%".concat(siteRequestData.getSearchKey().toUpperCase()).concat("%");
            builder.append(" AND (UPPER({sm:name}) like ?search OR UPPER({sm:uid}) like ?search OR " +
                    "UPPER({sm:mobileNumber}) like ?search) ");
            params.put("search", search);
        }

        if(siteRequestData.getSiteStatus()!=null && !siteRequestData.getSiteStatus().isEmpty()) {
            List<SiteStatus> siteStatusList = siteRequestData.getSiteStatus().stream().map(status -> SiteStatus.valueOf(status)).collect(Collectors.toList());
            if(siteStatusList!=null && !siteStatusList.isEmpty())
            {
                builder.append(" AND {sm:siteStatus} IN (?siteStatusList) ");
                params.put("siteStatusList", siteStatusList);
            }
        }
        if(siteRequestData.getConstructionStages()!=null && !siteRequestData.getConstructionStages().isEmpty())
        {
            List<ConstructionStage> constructionStageList = siteRequestData.getConstructionStages().stream().map(status -> ConstructionStage.valueOf(status)).collect(Collectors.toList());
            if(constructionStageList!=null && !constructionStageList.isEmpty())
            {
                builder.append(" AND {sm:constructionStage} IN (?constructionStageList) ");
                params.put("constructionStageList", constructionStageList);
            }
        }

        if(Objects.nonNull(plannedVisitForToday)){
            if (plannedVisitForToday) {
                String today= LocalDate.now().toString();
                String nextDay= LocalDate.now().plusDays(1).toString();
                builder.append(" and {sm:nextSlabCasting} >=?today and {sm:nextSlabCasting} <?nextDay ");
                params.put("today", today);
                params.put("nextDay", nextDay);

            }
        }

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        parameter.setFlexibleSearchQuery(query);
        parameter.setSortCodeToQueryAlias(getSiteMasterSortCodeToQueryAlias());
        return getPaginatedFlexibleSearchService().search(parameter);
    }

    @Override
    public Integer cmConvertedTargetVisitPremium(EyDmsUserModel eydmsUser, Integer month,Integer currentYear) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        String attributeName = "m" + month;

        builder.append("select {" + attributeName + "} from {TargetSiteConvertedVisitPremium} where {tso}=?currentUser and {year}=?currentYear");
        params.put("currentUser", eydmsUser);
        params.put("currentYear", currentYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Integer.class));
        query.addQueryParameters(params);
        final SearchResult<Integer> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0;
        else
            return 0;

    }



    @Override
    public Double cmConvertedActualBagTotal(EyDmsUserModel eydmsUser,Integer month, Integer currentYear) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        int lastMonth = Calendar.getInstance().get(Calendar.MONTH);
        String attributeName = "m" + month;
        builder.append("select {" + attributeName + "} from {TargetSiteConvertedBagPremium} where {tso}=?currentUser and {year}=?currentYear");
        params.put("currentUser", eydmsUser);
        params.put("currentYear", currentYear);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }



    public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
        return paginatedFlexibleSearchService;
    }

    public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
        this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
    }

    public Map<String, String> getSiteMasterSortCodeToQueryAlias() {
        return siteMasterSortCodeToQueryAlias;
    }

    public void setSiteMasterSortCodeToQueryAlias(Map<String, String> siteMasterSortCodeToQueryAlias) {
        this.siteMasterSortCodeToQueryAlias = siteMasterSortCodeToQueryAlias;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public DataConstraintDao getDataConstraintDao() {
        return dataConstraintDao;
    }

    public void setDataConstraintDao(DataConstraintDao dataConstraintDao) {
        this.dataConstraintDao = dataConstraintDao;
    }
}
