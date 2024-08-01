package com.scl.core.services;

import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface SclDealerRetailerService {

    List<DealerRetailerMappingModel> getDealerRetailerMappingList();

    List<DealerRetailerMappingModel> getDealerRetailerMappingListForDealer(SclCustomerModel dealer);

    List<AddressModel> getAddressListForDealerAndRetailer(SearchPageData searchPageData, CustomerModel dealer, String retailerId, String transportationZone, String filter);

    SclCustomerModel getDealerForRetailer(SclCustomerModel retailer);

    List<SclCustomerModel> getDealerRetailerList( Date xOldDate);

    List<GeographicalMasterModel> getSclGeoMasterList(Date xOldDate);

    List<AddressModel> getSclAddressByGeoMaster(String transportationZone);

}
