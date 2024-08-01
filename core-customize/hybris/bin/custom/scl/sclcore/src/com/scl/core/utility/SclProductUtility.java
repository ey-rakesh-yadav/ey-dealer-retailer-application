package com.scl.core.utility;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class SclProductUtility {

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

    @Resource
    private BaseSiteService baseSiteService;

    @Resource
    private CatalogVersionService catalogVersionService;

    @Resource
    private ProductService productService;

    public ProductModel getProductByCatalogVersion(String productCode)
    {
        BaseSiteModel baseSiteModel = getBaseSiteService().getCurrentBaseSite();
        CatalogVersionModel version = getCatalogVersionService().getCatalogVersion(baseSiteModel.getUid() + "ProductCatalog", "Staged");
        ProductModel productModel = getProductService().getProductForCode(version, productCode);
        return productModel;
    }
}
