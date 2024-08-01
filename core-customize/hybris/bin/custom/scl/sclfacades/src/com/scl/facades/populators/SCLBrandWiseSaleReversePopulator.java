package com.scl.facades.populators;

import com.scl.core.dao.SCLGenericDao;
import com.scl.core.model.BrandWiseSaleModel;
import com.scl.facades.prosdealer.data.BrandWiseSaleData;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.c2l.C2LItemModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;

import javax.annotation.Resource;
import java.util.Objects;

public class SCLBrandWiseSaleReversePopulator implements Populator<BrandWiseSaleData, BrandWiseSaleModel> {
    @Resource
    private SCLGenericDao sclGenericDao;
    @Resource
    private ModelService modelService;
    @Resource
    private BaseSiteService baseSiteService;

    @Override
    public void populate(BrandWiseSaleData source, BrandWiseSaleModel target) throws ConversionException {
        target.setBrand(getOrCreateBrand(source.getBrandCode()));
        target.setSaleInMT(source.getSaleInMT());
        if (Objects.nonNull(source.getWholeSale()) && Objects.nonNull(source.getRetailSale())) {
            target.setWholeSale(source.getWholeSale());
            target.setRetailSale(source.getRetailSale());
        }
    }

    private BrandModel getOrCreateBrand(String brandCode) {
        var brand = sclGenericDao.findItemByTypeCodeAndUidParam(BrandModel._TYPECODE, C2LItemModel.ISOCODE, brandCode);
        if (null != brand) {
            return (BrandModel) brand;
        }
        BrandModel brandModel = modelService.create(BrandModel.class);
        brandModel.setIsocode(brandCode);
        brandModel.setSclBrand((CMSSiteModel) baseSiteService.getCurrentBaseSite());
        modelService.save(brandModel);
        return brandModel;
    }
}
