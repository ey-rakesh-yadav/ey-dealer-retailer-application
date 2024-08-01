package com.scl.occ.controllers;

import com.scl.core.model.SclCustomerModel;

import com.scl.facades.data.ProductAliasListData;
import com.scl.facades.product.SCLProductFacade;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.ProductSearchFacade;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.search.facetdata.ProductSearchPageData;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Tag(name = "SCL Products")
@RequestMapping(value = "/{baseSiteId}/products")
public class SCLProductsController extends SclBaseController{



    @Resource(name = "productSearchFacade")
    private ProductSearchFacade<ProductData> productSearchFacade;

    @Resource(name = "webPaginationUtils")
    private WebPaginationUtils webPaginationUtils;
    @Resource(name="sclProductFacade")
    private SCLProductFacade sclProductFacade;


    @Resource
    private UserService userService;

    @RequestMapping(value = "/product-names", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getProductsName", summary = "Get a list of product names", description =
            "Get a list of product names")
    @ApiBaseSiteIdParam
    public Set<String> getProductNames(
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {

        ProductSearchPageData<SearchStateData, ProductData> result = productSearchFacade.textSearch(StringUtils.EMPTY);
        if(null != result && CollectionUtils.isNotEmpty(result.getResults())){
            return filterUniqueProductNames(result.getResults());
        }
        else{
            return new HashSet<>();
        }
    }

    Set<String> filterUniqueProductNames(List<ProductData> products){
        return products.stream().map(ProductData::getName).collect(Collectors.toSet());
    }


    @RequestMapping(value = "/getProducts", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getProducts", summary = "Get a list of products", description =
            "Returns a list of products available sorting, and pagination options. It can also include spelling suggestions. To make spelling suggestions work, you need to make sure ")
    @ApiBaseSiteIdParam
    public ProductListWsDTO getProducts(@RequestParam(name = "dealerId") final String dealerId, @RequestParam(name ="transportationZone")final String transportationZone, @RequestParam(name = "sort", required = false,defaultValue = "name") final String sort, @Parameter(description = "Base site ID", required = true) @PathVariable(required = true) final String baseSiteId, @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage, @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize, @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal, final HttpServletResponse response){
        final SearchPageData searchPageData = webPaginationUtils.buildSearchPageData(sort, currentPage, pageSize, needsTotal);
        recalculatePageSize(searchPageData);

        SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(dealerId);
        SearchPageData<ProductData> productSearchResult = sclProductFacade.getProductList(searchPageData, sclCustomer,transportationZone,baseSiteId);
        List<ProductData> productDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(productSearchResult.getResults())) {
            productDataList.addAll(productSearchResult.getResults());
        }
        if (productSearchResult.getPagination() != null) {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(productSearchResult.getPagination().getTotalNumberOfResults()));
        }

        final ProductListWsDTO productListWsDTO = new ProductListWsDTO();
        productListWsDTO.setProducts(productDataList
                .stream() //
                .map(productData -> getDataMapper().map(productData, ProductWsDTO.class)) //
                .collect(Collectors.toList()));
       // productListWsDTO.setPagination(getWebPaginationUtils().buildPagination(productSearchResult));
        return productListWsDTO;

    }

    @RequestMapping(value = "/getProductsAlias", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getProductsAlias", summary = "Get a list of product alias names", description =
            "Get a list of product alias names")
    @ApiBaseSiteIdParam
    public ProductAliasListData getProductsAlias(
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        return sclProductFacade.getProductsAlias();
    }

}
