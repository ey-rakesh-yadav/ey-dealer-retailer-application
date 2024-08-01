package com.scl.core.dao.impl;


import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.ProductAliasDao;
import com.scl.core.dao.SCLGenericDao;
import com.scl.core.dao.SCLProductDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;


import java.util.*;
import java.util.stream.Collectors;

public class SCLProductDaoImpl implements SCLProductDao {


    private static final Logger LOG = Logger.getLogger(SCLProductDaoImpl.class);
    @Resource
    private GeographicalRegionDao geographicalRegionDao;

 @Resource
 private CatalogVersionService catalogVersionService;

 @Resource
 private SCLGenericDao sclGenericDao;

 @Autowired
 private ProductAliasDao productAliasDao;

 @Resource
 private BaseSiteService baseSiteService;

 @Autowired
 private ModelService modelService;

 @Autowired
 PaginatedFlexibleSearchService paginatedFlexibleSearchService;

 @Autowired
 FlexibleSearchService flexibleSearchService;

    @Autowired
    UserService userService;

    @Autowired
    TerritoryMasterDao territoryMasterDao;

    Map<String,String> productSortCodeToQueryAlias;

    private final static String CATALOG_ID = "ProductCatalog";
    private final static String VERSION_ONLINE = "Online";

    private final static String CEMENT="cement";

    private final static String APPROVED="approved";

    @Resource
    private EnumerationService enumerationService;

    @Autowired
    private CategoryService categoryService;
    public Map<String, String> getProductSortCodeToQueryAlias() {
        return productSortCodeToQueryAlias;
    }

    public void setProductSortCodeToQueryAlias(Map<String, String> productSortCodeToQueryAlias) {
        this.productSortCodeToQueryAlias = productSortCodeToQueryAlias;
    }

    /**
     * @param searchPageData
     * @param sclCustomer
     * @param transportationZone
     * @param baseSiteId
     * @return
     */
    @Override
    public SearchPageData<ProductModel> getProductList(SearchPageData searchPageData, SclCustomerModel sclCustomer, String transportationZone, String baseSiteId) {

     LOG.info(String.format("Inside getProductList for sclCustomer %s",sclCustomer.getCustomerNo()));
        final StringBuilder sql = new StringBuilder();
        final Map<String, Object> params = new HashMap<String, Object>();
        sql.append("SELECT Distinct({p:pk}),{p:name} FROM {Product as p join DestinationSourceMaster as d on {p:code}={d:productCode} and {p:custCategory}={d:customerCategory} join CategoryProductRelation as cpr on {p:pk}={cpr:target} join ArticleApprovalStatus as aas on {p:approvalStatus}={aas:pk} }");

        try {
            GeographicalMasterModel geographicalMaster = geographicalRegionDao.fetchGeographicalMaster(transportationZone);
            sql.append(" where {d:transportationZone}=?transportationZone");
            params.put("transportationZone", geographicalMaster.getTransportationZone());

            if (Objects.nonNull(sclCustomer.getCustomerCategory())) {
                sql.append(" and {p:custCategory}=?customerCategory");
                params.put("customerCategory", sclCustomer.getCustomerCategory());
            }
            List<SclBrandModel> sclBrandModels = getNeilsonBrandMapping(sclCustomer);

            sql.append(" and {p:brandCode} IN (?sclBrandList)");
            params.put("sclBrandList", sclBrandModels);
            BaseSiteModel currentSite = baseSiteService.getBaseSiteForUID(baseSiteId);
             String uid= Objects.nonNull(currentSite)?currentSite.getUid(): Strings.EMPTY;
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(uid.concat(CATALOG_ID), VERSION_ONLINE);
            sql.append(" and {p:catalogVersion}=?catalogVersion");
            params.put("catalogVersion", catalogVersion);
            CategoryModel cementCategory = categoryService.getCategoryForCode(catalogVersion, CEMENT);

            sql.append(" and {cpr:source}=?cementCategory");
            params.put("cementCategory", cementCategory);

            sql.append(" and {aas:code}=?approvalStatus");
            params.put("approvalStatus", APPROVED);

            final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
            parameter.setSearchPageData(searchPageData);
            final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
            query.setResultClassList(Collections.singletonList(ProductModel.class));
            query.addQueryParameters(params);
            parameter.setFlexibleSearchQuery(query);
            parameter.setSortCodeToQueryAlias(getProductSortCodeToQueryAlias());
            LOG.info(String.format("Get product list query::%s", query));
            SearchPageData<ProductModel> searchPageproductModelList = paginatedFlexibleSearchService.search(parameter);

            if (Objects.nonNull(searchPageproductModelList) && CollectionUtils.isNotEmpty(searchPageproductModelList.getResults())) {
             
                return searchPageproductModelList;
            }
        } catch (Exception ex) {
            LOG.error(String.format("Exception occurred during getProductList::%s ", ex.getMessage()));
        }
        return null;
    }


    /**
     *
     * @param sclCustomer
     * @return sclBrandModels
     */
    @Override
    public List<SclBrandModel> getNeilsonBrandMapping(SclCustomerModel sclCustomer) {
        Date currentDate = new Date();
        List<SclBrandModel> sclBrandModels = new ArrayList<>();
        List<NeilsonBrandMappingModel> neilsonBrandMappingModelList;

        List<ItemModel> neilsonBrandMappingModels = sclGenericDao.findListItemByTypeCodeAndUidParam(NeilsonBrandMappingModel._TYPECODE, NeilsonBrandMappingModel.NEILSON, sclCustomer.getNeilsonBrandMapping());
        neilsonBrandMappingModelList = neilsonBrandMappingModels.stream().map(n -> (NeilsonBrandMappingModel) n).collect(Collectors.toList());
        // neilsonBrandMappingModelList.addAll((Collection<? extends NeilsonBrandMappingModel>) neilsonBrandMappingModels);

        if (CollectionUtils.isNotEmpty(neilsonBrandMappingModelList)) {

            //filter neilson brand model based on valid date
            List<NeilsonBrandMappingModel> filterdList = neilsonBrandMappingModelList.stream().filter(n -> (currentDate.after(n.getValidFrom()) || currentDate.equals(n.getValidFrom())) && (currentDate.before(n.getValidTo()) || currentDate.equals(n.getValidTo()))).collect(Collectors.toList());
            if (Objects.nonNull(filterdList)) {
                for (NeilsonBrandMappingModel brandMappingModel : filterdList) {
                    sclBrandModels.add(brandMappingModel.getSclBrand());
                }
            }
        }
        return sclBrandModels;
    }

    @Override
    public List<String> getProductsAliasForSalesPerformance() {
        String customerNo="";
        final Map<String, Object> params = new HashMap<String, Object>();
        B2BCustomerModel b2BCustomerModel = (B2BCustomerModel) userService.getCurrentUser();
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT distinct{s:alias} FROM {SalesSummary as s} where ");
        if(b2BCustomerModel!=null && b2BCustomerModel instanceof SclCustomerModel) {
                if (StringUtils.isNotEmpty(((SclCustomerModel) b2BCustomerModel).getCustomerNo())) {
                    customerNo = ((SclCustomerModel) b2BCustomerModel).getCustomerNo();
                    builder.append(" {s:customerNo}=?customerNo");
                    params.put("customerNo",customerNo);
                }
            }
       if(b2BCustomerModel!=null && b2BCustomerModel instanceof SclUserModel)
        {
            List<TerritoryMasterModel> territoryMasterList = territoryMasterDao.getTerritoryForUser(null);
            if(CollectionUtils.isNotEmpty(territoryMasterList))
            {
                builder.append(" {s:territoryMaster} in (?territoryMasterList)");
                params.put("territoryMasterList",territoryMasterList);
            }
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(String.class));
        query.addQueryParameters(params);
        final SearchResult<String> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
    }

    @Override
    public List<String> getProductsAliasForRetailer() {
        final Map<String, Object> params = new HashMap<String, Object>();
        B2BCustomerModel b2BCustomerModel = (B2BCustomerModel) userService.getCurrentUser();
        final StringBuilder builder1 = new StringBuilder();
        final StringBuilder builder2 = new StringBuilder();
        builder1.append("SELECT distinct{pp:aliasName} from {MasterStockAllocation AS m JOIN ProductAlias as pp on {m:aliasCode}={pp:aliasName} " +
                " JOIN Product as p on {pp.product}={p.pk}} where {m.retailer} =?sclRetailer and " +
                "  {m:aliasCode} is not null ");
        params.put("sclRetailer", b2BCustomerModel);
        final FlexibleSearchQuery query1 = new FlexibleSearchQuery(builder1.toString());
        query1.setResultClassList(Arrays.asList(String.class));
        query1.addQueryParameters(params);
        LOG.info(String.format("Query 1:%s",query1));
        final SearchResult<String> searchResult1 = flexibleSearchService.search(query1);

        builder2.append("SELECT distinct{pp:aliasName} from {OrderRequisition AS or JOIN Product as p on {or:product}={p:pk} JOIN ProductAlias as pp on {pp.product}={p.pk}}" +
                " where  {or.toCustomer} = ?sclRetailer and {or:product} is not null ");
        params.put("sclRetailer", b2BCustomerModel);
        final FlexibleSearchQuery query2 = new FlexibleSearchQuery(builder2.toString());
        query2.setResultClassList(Arrays.asList(String.class));
        query2.addQueryParameters(params);
        LOG.info(String.format("Query 2:%s",query2));
        final SearchResult<String> searchResult2 = flexibleSearchService.search(query2);

        List<String> productList=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(searchResult1.getResult())){
            List<String> result = searchResult1.getResult();
            productList.addAll(result);
        }
        if(CollectionUtils.isNotEmpty(searchResult2.getResult())){
            List<String> result = searchResult2.getResult();
            productList.addAll(result);
        }
        if(CollectionUtils.isNotEmpty(productList)) {
            return productList.stream().distinct().collect(Collectors.toList());
        }else{
            return null;
        }
    }
}
