package com.scl.core.dao;

import com.scl.core.model.*;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface SclDealerRetailerDao {

    List<DealerRetailerMappingModel> getDealerRetailerMappingList();

    List<DealerRetailerMappingModel> getDealerRetailerMappingListForDealer(SclCustomerModel dealer);

    List<AddressModel>  getAddressListForDealerAndRetailer(SearchPageData searchPageData, CustomerModel dealer, String retailerId, String transportationZone, String filter);

    DealerRetailerMappingModel getDealerRetailerMappingForDealerAndShipto(SclCustomerModel dealer, String shipToId);

    List<SclCustomerModel> getRetailerMappingListForDealer(SclCustomerModel dealer);

    UserSubAreaMappingModel getUserSubAreaMappingModelModel(SclUserModel sclUser, SubAreaMasterModel subAreaMaster, String district, String state, String subArea);

    SclCustomerModel getDealerForRetailer(SclCustomerModel retailer);

    DealerRetailerMappingModel getDealerRetailerMappingModel(SclCustomerModel sclCust, String addPK, String retailerPK);

    List<DealerRetailerMappingModel> getDealerRetailerMappingListModel(SclCustomerModel sclCust, String addPK, String retailerPK);

    List<SclCustomerModel> getSclCustomerModelList(Date xOldDate);

    List<GeographicalMasterModel> getSclGeoMasterList( Date xOldDate);

    List<AddressModel> getSclAddressByGeoMaster(String transportationZone);

    CustomerSubAreaMappingModel getCustomerSubAreaMapping(String sclCust, SubAreaMasterModel subareamaster, String State, String subArea, String district);
}
