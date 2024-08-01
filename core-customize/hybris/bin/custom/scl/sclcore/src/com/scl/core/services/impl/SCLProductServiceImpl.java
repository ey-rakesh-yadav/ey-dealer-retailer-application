package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.SCLProductDao;
import com.scl.core.model.ProductAliasModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SCLProductService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SCLProductServiceImpl implements SCLProductService {


    @Autowired
    private ModelService modelService;

    @Resource
    private SCLProductDao sclProductDao;

    @Autowired
    private UserService userService;
    /**
     * @param searchPageData
     * @param sclCustomer
     * @param transportationZone
     * @param baseSiteId
     * @return
     */
    @Override
    public SearchPageData<ProductModel> fetchProducts(SearchPageData searchPageData, SclCustomerModel sclCustomer, String transportationZone, String baseSiteId) {
        return sclProductDao.getProductList(searchPageData, sclCustomer,transportationZone,baseSiteId);
    }

    @Override
    public List<String> getProductsAliasForSalesPerformance() {
        B2BCustomerModel b2BCustomerModel = (B2BCustomerModel) userService.getCurrentUser();
        if((b2BCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
            return sclProductDao.getProductsAliasForRetailer();
        }else {
            return sclProductDao.getProductsAliasForSalesPerformance();
        }
    }

    @Override
    public String   getProductAlias(ProductModel productModel, String state, String district) {
        String aliasName = "";
        LocalDate currentDate = LocalDate.now();

        Optional<String> matchedAlias = productModel.getProductAlias()
                .stream()
                .filter(productAliasModel -> validAliasForStateDistrict(productAliasModel, currentDate, state, district))
                .map(ProductAliasModel::getAliasName)
                .findFirst();

        if(!matchedAlias.isPresent()) {
            matchedAlias = productModel.getProductAlias()
                    .stream()
                    .filter(productAliasModel -> validAliasForState(productAliasModel, currentDate, state, district))
                    .map(ProductAliasModel::getAliasName)
                    .findFirst();
        }

        if (matchedAlias.isPresent()) {
            aliasName = matchedAlias.get();
        } else {
            aliasName = StringUtils.defaultIfBlank(aliasName, productModel.getName());
        }

        return aliasName;
    }

    /**
     * @param productModel
     * @param state
     * @param district
     * @return
     */
    @Override
    public boolean getProductType(ProductModel productModel, String state, String district) {

        boolean isPremiumProduct=Boolean.FALSE;
        LocalDate currentDate = LocalDate.now();

        Optional<Boolean> isPremium = productModel.getProductAlias()
                .stream()
                .filter(productAliasModel -> validAliasForStateDistrict(productAliasModel, currentDate, state, district))
                .map(ProductAliasModel::getIsPremium)
                .findFirst();

        if(!isPremium.isPresent()) {
            isPremium = productModel.getProductAlias()
                    .stream()
                    .filter(productAliasModel -> validAliasForState(productAliasModel, currentDate, state, district))
                    .map(ProductAliasModel::getIsPremium)
                    .findFirst();
        }

        if (isPremium.isPresent()) {
            isPremiumProduct= isPremium.get();
           return isPremiumProduct;
        }
        return isPremiumProduct;
    }


    private boolean validAliasForStateDistrict(ProductAliasModel aliasModel, LocalDate currentDate, String state, String district) {
        LocalDate validFrom = aliasModel.getValidFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate validTo = aliasModel.getValidTo().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        boolean isStateMatch = StringUtils.isNotBlank(aliasModel.getState()) && aliasModel.getState().equalsIgnoreCase(state);
        boolean isDistrictMatch = StringUtils.isNotBlank(aliasModel.getDistrict()) && aliasModel.getDistrict().equalsIgnoreCase(district);

        return currentDate.isAfter(validFrom) && currentDate.isBefore(validTo) &&
                StringUtils.isNotBlank(aliasModel.getAliasName()) &&
                (isStateMatch && isDistrictMatch);
    }

    private boolean validAliasForState(ProductAliasModel aliasModel, LocalDate currentDate, String state, String district) {
        LocalDate validFrom = aliasModel.getValidFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate validTo = aliasModel.getValidTo().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        boolean isStateMatch = StringUtils.isNotBlank(aliasModel.getState()) && aliasModel.getState().equalsIgnoreCase(state);

        return currentDate.isAfter(validFrom) && currentDate.isBefore(validTo) &&
                StringUtils.isNotBlank(aliasModel.getAliasName()) &&
                (isStateMatch && StringUtils.isBlank(aliasModel.getDistrict()));
    }


}
