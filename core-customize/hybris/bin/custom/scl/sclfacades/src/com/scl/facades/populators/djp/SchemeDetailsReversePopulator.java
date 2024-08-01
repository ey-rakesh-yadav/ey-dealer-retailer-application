package com.scl.facades.populators.djp;

import com.scl.core.brand.dao.BrandDao;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.CompetitorProductDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;

import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.facades.data.CompetitorProductData;
import com.scl.facades.marketvisit.data.scheme.ProductPointsInfoData;
import com.scl.facades.marketvisit.data.scheme.SchemeSlabData;
import com.scl.facades.marketvisit.scheme.SchemeDetailsData;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SchemeDetailsReversePopulator implements Populator<SchemeDetailsData, SchemeDetailsModel> {

    @Resource
    private BrandDao brandDao;
    @Autowired
    EnumerationService enumerationService;

    @Resource
    private SclCustomerService sclCustomerService;

    @Resource
    private ModelService modelService;

    @Resource
    private CompetitorProductDao competitorProductDao;

    @Autowired
    UserService userService;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    GeographicalRegionDao geographicalRegionDao;

    private static final Logger LOG = Logger.getLogger(SchemeDetailsReversePopulator.class);

    @Override
    public void populate(SchemeDetailsData schemeDetailsData, SchemeDetailsModel schemeDetailsModel) throws ConversionException {
        if(null!= schemeDetailsData.getBrand()){
            BrandModel brandById = brandDao.findBrandById(schemeDetailsData.getBrand().getIsocode());
            schemeDetailsModel.setBrand(brandById);
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getSchemeType())){
            schemeDetailsModel.setSchemeType(SchemeType.valueOf(schemeDetailsData.getSchemeType()));
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getSchemeUnit())){
            schemeDetailsModel.setSchemeUnit(SchemeUnit.valueOf(schemeDetailsData.getSchemeUnit()));
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getIncentiveType())){
            if(schemeDetailsData.getIncentiveType()!=null){
                IncentiveType enumerationValue = enumerationService.getEnumerationValue(IncentiveType.class, schemeDetailsData.getIncentiveType());
                schemeDetailsModel.setIncentiveType(enumerationValue);
            }
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getNoOfSlabs())){
            schemeDetailsModel.setNoOfSlabs(Integer.parseInt(schemeDetailsData.getNoOfSlabs()));
        }

        if(StringUtils.isNotBlank(schemeDetailsData.getSchemeObjective())){
            schemeDetailsModel.setObjective(SchemeObjective.valueOf(schemeDetailsData.getSchemeObjective()));
        }

        if(StringUtils.isNotBlank(schemeDetailsData.getStartDate())){
            schemeDetailsModel.setStartDate(getDate(schemeDetailsData.getStartDate()));
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getEndDate())){
            schemeDetailsModel.setEndDate(getDate(schemeDetailsData.getEndDate()));
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getMarketRating())) {
            schemeDetailsModel.setMarketRating(Double.valueOf(schemeDetailsData.getMarketRating()));
        }
        if(StringUtils.isNotBlank(schemeDetailsData.getPartenerLevel())){
            schemeDetailsModel.setPartnerLevel((PartnerLevel.valueOf(schemeDetailsData.getPartenerLevel())));
        }

        schemeDetailsModel.setRemark(schemeDetailsData.getRemark());
        if(CollectionUtils.isNotEmpty(schemeDetailsData.getSlabs())){
            populateSlabDetails(schemeDetailsModel,schemeDetailsData.getSlabs());
        }

        populateProductPointsDetails(schemeDetailsModel,schemeDetailsData.getProductPointsInfos(),schemeDetailsData.getProducts());

    }

    private void populateProductPointsDetails(SchemeDetailsModel schemeDetailsModel, List<ProductPointsInfoData> productPointsInfoDataList, List<CompetitorProductData> products) {

        Integer basePoint = 0;
        SclUserModel sclUser =(SclUserModel) userService.getCurrentUser();
        List<String> states = new ArrayList<>();
        List<UserSubAreaMappingModel> userSubAreaList =  geographicalRegionDao.getUserSubAreaMappingForUser(sclUser);
        if(CollectionUtils.isNotEmpty(userSubAreaList)) {
            states.addAll(userSubAreaList.stream().map(data -> data.getState()).distinct().collect(Collectors.toList()));
        }
        List<ProductPointsInfoModel> productPointsInfoModels = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(productPointsInfoDataList)){
            for(ProductPointsInfoData productPointsInfoData : productPointsInfoDataList){
                ProductPointsInfoModel productPointsInfoModel = modelService.create(ProductPointsInfoModel.class);
                if(null!= productPointsInfoData.getProduct()){
                    CompetitorProductModel competitorProduct = competitorProductDao.findCompetitorProductById(productPointsInfoData.getProduct().getCode(),schemeDetailsModel.getBrand(),states);
                    if(competitorProduct!=null)
                    productPointsInfoModel.setProduct(competitorProduct);
                }
                basePoint = Integer.parseInt(productPointsInfoData.getBasePoint());
                productPointsInfoModel.setBasePoint(basePoint);
                productPointsInfoModel.setBonusPoint(Integer.parseInt(productPointsInfoData.getBonusPoint()));
                productPointsInfoModel.setTotalPoints(Integer.sum(Integer.parseInt(productPointsInfoData.getBasePoint()),Integer.parseInt(productPointsInfoData.getBonusPoint())));
                modelService.save(productPointsInfoModel);

                productPointsInfoModels.add(productPointsInfoModel);
            }
        }
        List<CompetitorProductData> basePointProducts;
        if(CollectionUtils.isNotEmpty(productPointsInfoDataList)){
            Set<String> productCodeFilterSet = productPointsInfoDataList.stream().map(ProductPointsInfoData::getProduct).map(CompetitorProductData::getCode).collect(Collectors.toSet());
            basePointProducts = products.stream().filter(product -> !productCodeFilterSet.contains(product.getCode())).collect(Collectors.toList());
        }
        else {
            basePointProducts = products;
        }
        if(CollectionUtils.isNotEmpty(basePointProducts)){
            for(CompetitorProductData competitorProductData : basePointProducts){
                ProductPointsInfoModel productPointsInfoModel = modelService.create(ProductPointsInfoModel.class);

                CompetitorProductModel competitorProduct = competitorProductDao.findCompetitorProductById(competitorProductData.getCode(),schemeDetailsModel.getBrand(),states);
                if(competitorProduct!=null)
                productPointsInfoModel.setProduct(competitorProduct);
                productPointsInfoModel.setBasePoint(basePoint);
                productPointsInfoModel.setBonusPoint(0);
                productPointsInfoModel.setTotalPoints(basePoint);
                modelService.save(productPointsInfoModel);

                productPointsInfoModels.add(productPointsInfoModel);
            }
        }
        schemeDetailsModel.setProductPointsInfos(productPointsInfoModels);
    }



    private void populateSlabDetails(final SchemeDetailsModel schemeDetailsModel ,final List<SchemeSlabData> slabs) {
        List<SchemeSlabModel> schemeSlabModels = new ArrayList<>();
        for(SchemeSlabData  schemeSlabData : slabs ){
            SchemeSlabModel schemeSlabModel = modelService.create(SchemeSlabModel.class);
            schemeSlabModel.setSlabNo(schemeSlabData.getSlabNo());
            schemeSlabModel.setIncentive(schemeSlabData.getIncentive());
            schemeSlabModel.setThreshold(schemeSlabData.getThreshold());
            schemeSlabModel.setValue(schemeSlabData.getValue());

            modelService.save(schemeSlabModel);
            schemeSlabModels.add(schemeSlabModel);
        }
        schemeDetailsModel.setSlabs(schemeSlabModels);
    }


    private Date getDate(final String dateStr){
        try{
            return new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1).parse(dateStr);
        }
        catch (ParseException ex){
            LOG.error(String.format("Date %s is not in correct format %s",dateStr,SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1));
            throw new AmbiguousIdentifierException(String.format("Date %s is not in correct format %s",dateStr,SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1));
        }


    }
}
