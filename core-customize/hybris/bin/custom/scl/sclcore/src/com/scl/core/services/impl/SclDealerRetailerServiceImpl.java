package com.scl.core.services.impl;

import com.scl.core.dao.SclDealerRetailerDao;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SclDealerRetailerService;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

/**
 * The type Scl dealer retailer service.
 */
public class SclDealerRetailerServiceImpl implements SclDealerRetailerService {

    private SclDealerRetailerDao sclDealerRetailerDao;

    /**
     * Gets dealer retailer mapping list.
     *
     * @return the dealer retailer mapping list
     */
    @Override
    public List<DealerRetailerMappingModel> getDealerRetailerMappingList() {
        return getSclDealerRetailerDao().getDealerRetailerMappingList();
    }

    @Override
    public List<SclCustomerModel> getDealerRetailerList( Date xOldDate) {
        return getSclDealerRetailerDao().getSclCustomerModelList( xOldDate);
    }

    @Override
    public List<GeographicalMasterModel> getSclGeoMasterList( Date xOldDate) {
        return getSclDealerRetailerDao().getSclGeoMasterList(xOldDate);
    }

    @Override
    public List<AddressModel> getSclAddressByGeoMaster(String transportationZone){
        return getSclDealerRetailerDao().getSclAddressByGeoMaster(transportationZone);
    }

    @Override
    public List<DealerRetailerMappingModel> getDealerRetailerMappingListForDealer(SclCustomerModel dealer) {
        return getSclDealerRetailerDao().getDealerRetailerMappingListForDealer(dealer);
    }

    /**
     * Gets address list for dealer and retailer.
     *
     * @param searchPageData     the search page data
     * @param dealer             the dealer
     * @param retailerId         the retailer id
     * @param transportationZone the transportation zone
     * @param filter             the filter
     * @return the address list for dealer and retailer
     */
    @Override
    public List<AddressModel> getAddressListForDealerAndRetailer(SearchPageData searchPageData, CustomerModel dealer, String retailerId, String transportationZone, String filter) {
        return getSclDealerRetailerDao().getAddressListForDealerAndRetailer(searchPageData,dealer,retailerId,transportationZone,filter);
    }

    /**
     * @param retailer
     * @return
     */
    @Override
    public SclCustomerModel getDealerForRetailer(SclCustomerModel retailer) {
        return getSclDealerRetailerDao().getDealerForRetailer(retailer);
    }


    /**
     * Gets scl dealer retailer dao.
     *
     * @return the scl dealer retailer dao
     */
    public SclDealerRetailerDao getSclDealerRetailerDao() {
        return sclDealerRetailerDao;
    }

    /**
     * Sets scl dealer retailer dao.
     *
     * @param sclDealerRetailerDao the scl dealer retailer dao
     */
    public void setSclDealerRetailerDao(SclDealerRetailerDao sclDealerRetailerDao) {
        this.sclDealerRetailerDao = sclDealerRetailerDao;
    }
}
