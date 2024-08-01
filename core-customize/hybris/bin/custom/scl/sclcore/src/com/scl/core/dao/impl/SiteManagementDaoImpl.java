package com.scl.core.dao.impl;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SiteManagementDao;
import com.scl.core.enums.CompetitorProductType;
import com.scl.core.enums.ConstructionStage;
import com.scl.core.enums.PremiumProductType;
import com.scl.core.enums.SiteStatus;
import com.scl.core.model.*;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.SiteRequestData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class SiteManagementDaoImpl implements SiteManagementDao {

    private static final Logger LOG = Logger.getLogger(SiteManagementDaoImpl.class);

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
    public List<CompetitorProductType> getSiteCategoryType() {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select distinct {cpt:pk} from {CompetitorProductType as cpt}");
        // params.put("buildNumber", buildNumber);

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CompetitorProductType.class));
        query.addQueryParameters(params);
        final SearchResult<CompetitorProductType> searchResult = flexibleSearchService.search(query);
        List<CompetitorProductType> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<PremiumProductType> getSiteCementType(String siteCategoryType) {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select distinct {ppt:pk} from {CompetitorProduct as cp join CompetitorProductType as cpt on {cp:competitorProductType}={cpt:pk} join PremiumProductType as ppt on {cp:premiumProductType}={ppt:pk}} where {cpt:code}=?siteCategoryType and {cp:state} =?state  ");
        params.put("siteCategoryType", siteCategoryType);

        if (userService.getCurrentUser() instanceof SclUserModel) {
            params.put("state", ((SclUserModel) userService.getCurrentUser()).getState());
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(PremiumProductType.class));
        query.addQueryParameters(params);
        final SearchResult<PremiumProductType> searchResult = flexibleSearchService.search(query);
        List<PremiumProductType> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public List<CompetitorProductModel> getSiteCementBrand(String siteCementType) {
        Map<String, Object> params = new HashMap<String, Object>();

        final StringBuilder builder = new StringBuilder("select {cp:pk} from {CompetitorProduct as cp join PremiumProductType as ppt on {cp:premiumProductType}={ppt:pk}} where {ppt:code}=?siteCementType and {state} = ?state ");
        params.put("siteCementType", siteCementType);
        if (userService.getCurrentUser() instanceof SclUserModel) {
            params.put("state", ((SclUserModel) userService.getCurrentUser()).getState());
        }

        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CompetitorProductModel.class));
        query.addQueryParameters(params);
        final SearchResult<CompetitorProductModel> searchResult = flexibleSearchService.search(query);
        List<CompetitorProductModel> result = searchResult.getResult();

        return result != null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public Double getActualTargetForSalesMTD(SclUserModel sclUser) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select sum({currentMonthSiteVisit}) from {SclSiteMaster} where {createdBy}=?currentUser and " + SclDateUtility.getMtdClauseQuery("lastVisitTime", params));

        params.put("currentUser", sclUser);
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
    public Double getMonthlySalesTarget(SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();

        int currentMonth = LocalDate.now().getMonth().getValue();

        String attributeName = "m" + currentMonth;
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();


        builder.append("select {" + attributeName + "} from {SiteVisitTargetMaster} where {user}=?currentUser and {year}=?year");

        params.put("currentUser", sclUser);
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
    public Double getLastMonthSalesTarget(SclUserModel sclUser) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        int lastMonth = LocalDate.now().getMonth().minus(1).getValue();
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.minusMonths(1).getYear();
        String attributeName = "m" + lastMonth;

        builder.append("select {" + attributeName + "} from {SiteVisitTargetMaster} where {user}=?currentUser and {year}=?year");

        params.put("currentUser", sclUser);
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
    public Integer getNewSiteVists(SclUserModel user) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select count({pk}) from {SclSiteMaster} where {createdBy}=?currentUser and " + SclDateUtility.getMtdClauseQuery("creationTime", params));
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


    public Integer getNewSiteVistsForLastMonth(SclUserModel user) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select count({pk}) from {SclSiteMaster} where {createdBy}=?currentUser and " + SclDateUtility.getLastMonthClauseQuery("creationTime", params));
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
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
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
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public CompetitorProductType findCategoryTypeByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {CompetitorProductType} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<CompetitorProductType> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public PremiumProductType findCementTypeByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {PremiumProductType} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<PremiumProductType> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
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
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    public List<List<Object>> getSiteTypeStagesCount(SclUserModel user) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();


            builder.append("select {constructionStage},{enum.code}, count({s.pk}) from {SclSiteMaster as s JOIN EnumerationValue AS enum ON {enum.pk}={s.constructionStage} } where {s.createdBy} =?currentUser and {s.siteActive} = ?siteActive group by {s.constructionStage},{enum.code}");

            params.put("currentUser", user);
            params.put("siteActive", "YES");

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class, Integer.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }

    }

    @Override
    public List<List<Object>> getNumberOfBagsPurchased(SclUserModel user) {
        try {
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder();


            builder.append("select Year({s.endVisitTime}),Month({s.endVisitTime}),{s.isPremium},count({s.pk}),sum({s.numberOfBagsPurchased}) from {VisitMaster as v join SiteVisitMaster as s on {s.visit}={v.pk}} where {v.user}=?currentUser  and {s.siteStatus}=?siteStatus and ");
            builder.append(SclDateUtility.getCurrentPreviousMonthClauseQuery("s.endVisitTime", params));

            params.put("currentUser", user);
            params.put("siteStatus", SiteStatus.SITE_CONVERTED);

            builder.append(" group by Year({s.endVisitTime}),Month({s.endVisitTime}),{s.isPremium}");

            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Boolean.class, Integer.class, Double.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return result != null && !result.isEmpty() ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));

        }

    }

    @Override
    public List<SclSiteMasterModel> getTotalPremiumOfSite(SclUserModel sclUser, String startDate, String endDate, String conversionType) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select {pk} from {sclSiteMaster} where {createdBy}=?currentUser and {modifiedtime} >=?startDate and {modifiedtime} <?endDate and {siteBagQtyMap} is not null");
//            if(conversionType.equalsIgnoreCase("premium")) {
//                builder.append(" and {isPremium} = ?isPremium");
//                params.put("isPremium",Boolean.TRUE);
//            }
        params.put("currentUser", sclUser);
//            params.put("siteStatus", SiteStatus.SITE_CONVERTED);
        params.put("startDate", startDate);
        params.put("endDate", endDate);


        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclSiteMasterModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclSiteMasterModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult();
        else
            return Collections.emptyList();
    }

    @Override
    public SearchPageData<SclSiteMasterModel> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData, Boolean plannedVisitForToday, List<String> filterBySubAreas) {
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final Map<String, Object> params = new HashMap<String, Object>();
        StringBuilder builder = new StringBuilder("select {sm:pk} from {SclSiteMaster as sm ");

        if (StringUtils.isNotBlank(siteRequestData.getSearchKey()) || CollectionUtils.isNotEmpty(filterBySubAreas)) {
            builder.append(" join Address as ad on {sm.pk}={ad.owner}");
        }
        builder.append(" } where {sm:createdBy}=?user ");
        params.put("user", userService.getCurrentUser());
        if (StringUtils.isNotBlank(siteRequestData.getSearchKey())) {
            String search = "%".concat(siteRequestData.getSearchKey().toUpperCase()).concat("%");

            builder.append(" AND (UPPER({sm:name}) like ?search OR UPPER({sm:uid}) like ?search OR " +
                    "UPPER({sm:mobileNumber}) like ?search OR UPPER({sm:contractorName}) like ?search OR UPPER({sm:contractorPhoneNumber}) like ?search OR " +
                    "UPPER({sm:masonName}) like ?search OR UPPER({sm:masonPhoneNumber}) like ?search OR UPPER({sm:architectName}) like ?search OR UPPER({sm:architectNumber}) like ?search OR " +
                    "UPPER({ad:line3}) like ?search OR UPPER({ad:line4}) like ?search OR UPPER({ad:state}) like ?search OR UPPER({ad:district}) like ?search OR " +
                    "UPPER({ad:taluka}) like ?search OR UPPER({ad:erpCity}) like ?search OR UPPER({ad:postalcode}) like ?search)");
            params.put("search", search);
        }
        if(CollectionUtils.isNotEmpty(filterBySubAreas)){
            builder.append("AND {ad:taluka} IN (?subAreas)");
            params.put("subAreas", filterBySubAreas);
        }
        if (siteRequestData.getActiveSite() != null && !siteRequestData.getActiveSite().isEmpty()) {
            String siteActive = siteRequestData.getActiveSite();
            params.put("siteActive", siteActive);
            builder.append(" and {sm:siteActive}=?siteActive ");
        }
        if (siteRequestData.getIsYesterdayVisit() != null && siteRequestData.getIsYesterdayVisit()) {
            String checkDate = new Date().toString();
            String startDate = LocalDate.now().minusDays(1).toString();
            String endDate = LocalDate.now().minusDays(1).toString() + " 23:59:59";
            if (checkDate.contains("GMT")) {

                startDate = LocalDate.now().minusDays(2).toString() + " 18:30:00";
                endDate = LocalDate.now().minusDays(1).toString() + " 18:30:00";
            }
            builder.append(" AND {sm.lastVisitTime} >=?startDate and {sm.lastVisitTime} < ?endDate ");
            params.put("startDate", startDate);
            params.put("endDate", endDate);

        }

        if (siteRequestData.getRemoveClosedSite() != null && siteRequestData.getRemoveClosedSite()) {
            Integer last_site_active_days = dataConstraintDao.findDaysByConstraintName("LAST_SITE_ACTIVE_DAYS");
            builder.append(" AND ({sm:siteStatus} =?siteStatus OR ").append(SclDateUtility.getLastXDayQuery("sm:lastVisitTime", params, last_site_active_days)).append(")");
            params.put("siteStatus", SiteStatus.CLOSED);
        }

      /*  if(siteRequestData.getIsApplicableForSiteConversion()!=null && siteRequestData.getIsApplicableForSiteConversion())
        {
            builder.append(" AND {sm:siteCategoryType}=?siteCategory ");
            params.put("siteCategory", findCategoryTypeByCode("CS"));
        }
*/
        if (siteRequestData.getSiteStatus() != null && !siteRequestData.getSiteStatus().isEmpty()) {
            List<SiteStatus> siteStatusList = siteRequestData.getSiteStatus().stream().map(status -> SiteStatus.valueOf(status)).collect(Collectors.toList());
            if (siteStatusList != null && !siteStatusList.isEmpty()) {
                builder.append(" AND {sm:siteStatus} IN (?siteStatusList) ");
                params.put("siteStatusList", siteStatusList);
            }
        }
        if (siteRequestData.getConstructionStages() != null && !siteRequestData.getConstructionStages().isEmpty()) {
            List<ConstructionStage> constructionStageList = siteRequestData.getConstructionStages().stream().map(status -> ConstructionStage.valueOf(status)).collect(Collectors.toList());
            if (constructionStageList != null && !constructionStageList.isEmpty()) {
                builder.append(" AND {sm:constructionStage} IN (?constructionStageList) ");
                params.put("constructionStageList", constructionStageList);
            }
        }

        if (Objects.nonNull(plannedVisitForToday)) {
            if (plannedVisitForToday) {
                String today = LocalDate.now().toString();
                String nextDay = LocalDate.now().plusDays(1).toString();
                builder.append(" and {sm:nextSlabCasting} >=?today and {sm:nextSlabCasting} <?nextDay ");
                params.put("today", today);
                params.put("nextDay", nextDay);

            }
        }

      /*  if(siteRequestData.getCreationTimeFilter()!=null){
            if (siteRequestData.getCreationTimeFilter()) {
                builder.append(" Order By {sm:creationTime} desc ");
            }
        }*/
        if (StringUtils.isNotBlank(siteRequestData.getCreationTimeFilter())) {

            Date date = getParsedDate(siteRequestData.getCreationTimeFilter());
            LocalDate startDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = startDate.plusDays(1);

            params.put("startDate", getDateFromLocalDate(startDate));
            params.put("endDate", getDateFromLocalDate(endDate));

            builder.append(" and {sm:lastVisitTime}>=?startDate and {sm:lastVisitTime}<?endDate ");
        }

      //builder.append(" order by {sm:creationtime} desc");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        parameter.setFlexibleSearchQuery(query);
        parameter.setSortCodeToQueryAlias(getSiteMasterSortCodeToQueryAlias());
        LOG.info(String.format("getPaginatedSiteMasterList ::%s",query));
        return getPaginatedFlexibleSearchService().search(parameter);
    }

    private Date getDateFromLocalDate(LocalDate localDate) {
        Date convertedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return convertedDate;
    }

    private Date getParsedDate(String date) {
        Date startDate = null;
        if (date != null) {
            try {
                startDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);

            } catch (ParseException e) {
                throw new IllegalArgumentException(String.format("Please provide valid date %s", date));
            }
        }
        return startDate;
    }

    @Override
    public Integer cmConvertedTargetVisitPremium(SclUserModel sclUser, Integer month, Integer currentYear) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        String attributeName = "m" + month;

        builder.append("select {" + attributeName + "} from {TargetSiteConvertedVisitPremium} where {tso}=?currentUser and {year}=?currentYear");
        params.put("currentUser", sclUser);
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
    public Double cmConvertedActualBagTotal(SclUserModel sclUser, Integer month, Integer currentYear) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        int lastMonth = Calendar.getInstance().get(Calendar.MONTH);
        String attributeName = "m" + month;
        builder.append("select {" + attributeName + "} from {TargetSiteConvertedBagPremium} where {tso}=?currentUser and {year}=?currentYear");
        params.put("currentUser", sclUser);
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

    @Override
    public CompetitorProductModel findCementProductByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {cp:pk} FROM {CompetitorProduct as cp join CompetitorProductType as cpt on {cp:CompetitorProductType}={cpt:pk} join PremiumProductType as ppt on {cp:premiumProductType}={ppt:pk}} WHERE {cp:code} = ?code and {cp:state}=?state";
        params.put("code", code);
        if (userService.getCurrentUser() instanceof SclUserModel && ((SclUserModel) userService.getCurrentUser()).getState() != null) {
            params.put("state", ((SclUserModel) userService.getCurrentUser()).getState());

        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        LOG.info(String.format("findCementProductByCode::%s",query));
        final SearchResult<CompetitorProductModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public SiteCementTypeModel findSiteCementTypeByCode(String code) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "SELECT {pk} FROM {SiteCementType} WHERE {code} = ?code";
        params.put("code", code);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<SiteCementTypeModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public CompetitorProductModel findCementProductByCodeAndBrand(String code, String state, String brand) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString = "select {cp:pk} from {CompetitorProduct as cp join Brand as b on {cp:brand}={b:pk}} where {cp:code} = ?code and {cp:state} = ?state and {b:isoCode}=?brand";
        params.put("code", code);
        params.put("state", state);
        params.put("brand", brand);
        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.addQueryParameters(params);
        final SearchResult<CompetitorProductModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {

            return searchResult.getResult().get(0);
        }
        return null;
    }

    @Override
    public Double getSiteConversionSale(SclUserModel tsoUser, String startDate, String endDate, String conversionType) {

        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder();
        builder.append("select sum({st:numberOfBagsPurchased}) from {SiteTransaction as st join SclSiteMaster as ssm on {st:site}={ssm:pk}} where {ssm:createdBy}=?currentUser and {st:siteConvertedDate}>=?startDate and {st:siteConvertedDate}<?endDate");
        if(conversionType.equalsIgnoreCase("premium")) {
                builder.append(" and {st:isPremium} = ?isPremium");
                params.put("isPremium",Boolean.TRUE);
        }
        params.put("currentUser", tsoUser);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        LOG.info(String.format("getSiteConversionSale query for TSO %s ::%s", tsoUser.getUid(), query));
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : Double.valueOf(0);
        else
            return Double.valueOf(0);
    }
  
    public List<String> getSiteMasterListTaluka(){
        final Map<String, Object> params = new HashMap<String, Object>();
        final String queryString ="Select Distinct {ad.taluka} FROM {SclSiteMaster as sm join Address as ad on {sm.pk}={ad.owner}} Where {sm:createdBy}=?user";
        params.put("user", userService.getCurrentUser());
        final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        LOG.info(String.format("getSiteMasterListTaluka :: %s ",query));
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();

    }
}
