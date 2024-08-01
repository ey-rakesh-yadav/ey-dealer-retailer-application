package com.scl.core.services;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.TerritoryMasterModel;
import com.scl.facades.data.RequestCustomerData;

import java.util.Collection;
import java.util.List;

public interface TerritoryMasterService {

    TerritoryMasterModel getTerritoryById(String territoryId);
    TerritoryMasterModel getTerritoryMaster(String territoryPk);
    List<TerritoryMasterModel> getTerritoriesForCustomer(String customerId);
    List<TerritoryMasterModel> getTerritoriesForCustomer(SclCustomerModel customer);
    List<TerritoryMasterModel> getTerritoriesForSO();
    List<TerritoryMasterModel> getTerritoriesForSO(String uid);
    List<SclCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData);
    List<TerritoryMasterModel> getTerritoryForUser(String territoryId);
    List<SclUserModel> getAllSalesOfficersByState(String state);
    List<List<Object>> getDistrictAndTalukaForCustomer(SclCustomerModel customer);
    void setCurrentTerritory(Collection<TerritoryMasterModel> territories);
    Collection<TerritoryMasterModel> getCurrentTerritory();
    List<SclUserModel> getUserByTerritory(TerritoryMasterModel territoryMaster);
}
