package com.scl.facades;

import com.scl.facades.data.RequestCustomerData;
import com.scl.facades.data.TerritoryData;
import com.scl.facades.data.TerritoryListData;
import com.scl.facades.prosdealer.data.CustomerListData;
import com.scl.facades.prosdealer.data.DealerListData;

public interface TerritoryMasterFacade {

    TerritoryData getTerritoryById(String territoryId);
    TerritoryListData getTerritoriesForCustomer();
    TerritoryListData getTerritoriesForSO();
    CustomerListData getCustomerForUser(RequestCustomerData customerData);
    TerritoryListData getTerritoryForUser(String territoryId);
    DealerListData getAllSalesOfficersByState(String state);


}
