package com.eydms.occ.controllers;

import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.ProductSearchFacade;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.search.facetdata.ProductSearchPageData;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.search.facetdata.ProductSearchPageWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@Tag(name = "EYDMS Products")
@RequestMapping(value = "/{baseSiteId}/products")
public class EYDMSProductsController extends EyDmsBaseController {

    @Resource(name = "productSearchFacade")
    private ProductSearchFacade<ProductData> productSearchFacade;

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
}
