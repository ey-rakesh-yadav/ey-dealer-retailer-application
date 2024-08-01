package com.scl.facades.product.impl;

import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.services.SCLProductService;
import com.scl.facades.data.ProductAliasData;
import com.scl.facades.data.ProductAliasListData;
import com.scl.facades.product.SCLProductFacade;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.url.impl.DefaultProductModelUrlResolver;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SCLProductFacadeImpl implements SCLProductFacade {

    private static final Logger LOG = Logger.getLogger(SCLProductFacadeImpl.class);

    @Resource
    private SCLProductService sclProductService;

    @Autowired
    private GeographicalRegionDao geographicalRegionDao;


    @Autowired
    private DefaultProductModelUrlResolver productModelUrlResolver;

    @Resource(name = "productConverter")
    private Converter<ProductModel, ProductData> productConverter;

    /**
     * @param searchPageData
     * @param sclCustomer
     * @param transportationZone
     * @param baseSiteId
     * @return
     */
    @Override
    public SearchPageData<ProductData> getProductList(SearchPageData searchPageData, SclCustomerModel sclCustomer, String transportationZone, String baseSiteId) {

        SearchPageData<ProductModel> fetchedProducts= sclProductService.fetchProducts(searchPageData,sclCustomer,transportationZone,baseSiteId);
        List<ProductData> productDataList = new ArrayList<>();
        SearchPageData<ProductData> productDataSearchPageData = new SearchPageData<>();

        if(Objects.nonNull(fetchedProducts) && (CollectionUtils.isNotEmpty(fetchedProducts.getResults()))) {
           List<ProductModel>  productModelList=  fetchedProducts.getResults();
              GeographicalMasterModel geographicalMaster =geographicalRegionDao.fetchGeographicalMaster(transportationZone);
           if(Objects.nonNull(geographicalMaster) && (geographicalMaster.getState()!=null && geographicalMaster.getDistrict()!=null)) {
               for (ProductModel product : productModelList) {
                   String aliasName=sclProductService.getProductAlias(product, geographicalMaster.getState(), geographicalMaster.getDistrict());
                  ProductData productData=new ProductData();
                  productData.setCode(product.getCode());
                  productData.setName(aliasName);
                  productData.setPremium(StringUtils.isNotBlank(product.getPremium())?product.getPremium():Strings.EMPTY);
                  productData.setUrl(productModelUrlResolver.resolve(product));
                  productDataList.add(productData);
               }
           }
            productDataSearchPageData.setResults(productDataList.stream().sorted(Comparator.comparing(ProductData::getName,String::compareToIgnoreCase)).collect(Collectors.toList()));
            productDataSearchPageData.setPagination(fetchedProducts.getPagination());
            productDataSearchPageData.setSorts(fetchedProducts.getSorts());
        }
        return productDataSearchPageData;
    }

    @Override
    public ProductAliasListData getProductsAlias() {
        List<String> aliasList = sclProductService.getProductsAliasForSalesPerformance();
        List<ProductAliasData> list = new ArrayList<>();
        ProductAliasListData listData = new ProductAliasListData();
        if(aliasList!=null && !aliasList.isEmpty()) {
            for (String alias : aliasList) {
                ProductAliasData productAliasData = new ProductAliasData();
                productAliasData.setCode(alias);
                productAliasData.setName(alias);
                list.add(productAliasData);
            }
            if(CollectionUtils.isNotEmpty(list)){
                list=list.stream().filter(obj->Objects.nonNull(obj.getName())).sorted(Comparator.comparing(ProductAliasData::getName)).collect(Collectors.toList());
            }
            listData.setProductAliasList(list);
        }
        return listData;
    }
}
