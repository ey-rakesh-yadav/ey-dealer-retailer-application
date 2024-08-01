package com.scl.core.dao;

import com.scl.core.jalo.TerritoryUserMapping;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.*;
import com.scl.core.model.TerritoryMasterModel;
import com.scl.facades.data.RequestCustomerData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;

import java.util.List;

public interface TerritoryMasterDao {

    TerritoryMasterModel getTerritoryById(String territoryId);
    TerritoryMasterModel getTerritoryByPk(String territoryPk);
    List<TerritoryMasterModel> getTerritoriesForCustomer(UserModel customer);
    List<TerritoryMasterModel> getTerritoriesForSO(SclUserModel user);
    List<SclCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData, List<TerritoryMasterModel> territoryMasterModelList);
    List<TerritoryMasterModel> getTerritoryForUser(TerritoryMasterModel territoryId);
    List<SclUserModel> getAllSalesOfficersByState(String state, BaseSiteModel site);
    List<List<Object>> getDistrictAndTalukaForCustomer(SclCustomerModel customer);


    List<TerritoryMasterModel> getTerritoryForSO();


     DistrictMasterModel getDistrictMaster(String district);

    TalukaMasterModel getTalukaMaster(String taluka);

    RegionMasterModel getRegionMaster(String region);

    StateMasterModel getStateMaster(String state);
    List<SclUserModel> getUserByTerritory(TerritoryMasterModel territoryMaster);

    List<SCLIntSalesHierarchyModel>  getAllIntSalesHierarchy();

    SCLIntSalesHierarchyModel findIntSalesHierarchyForTerritoryCode(String territoryCode);
    List<TerritoryUserMappingModel> getTerritoryUserMappingForUser(SclUserModel userModel);

    boolean checkValidTSOMapping(SclUserModel sclUser);


}

