package com.scl.facades.impl;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.TerritoryMasterModel;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.TerritoryMasterFacade;
import com.scl.facades.data.RequestCustomerData;
import com.scl.facades.data.TerritoryData;
import com.scl.facades.data.TerritoryListData;
import com.scl.facades.prosdealer.data.CustomerListData;
import com.scl.facades.prosdealer.data.DealerListData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TerritoryMasterFacadeImpl implements TerritoryMasterFacade {

    @Autowired
    TerritoryMasterService territoryMasterService;
    @Autowired
    UserService userService;

    @Override
    public TerritoryData getTerritoryById(String territoryId) {
        TerritoryMasterModel territoryMasterModel=territoryMasterService.getTerritoryById(territoryId);
        TerritoryData data = new TerritoryData();
        data.setTerritoryCode(territoryMasterModel.getTerritoryCode());
        data.setTerritoryName(territoryMasterModel.getTerritoryCode());
        return data;
    }

    @Override
    public TerritoryListData getTerritoriesForCustomer() {
        TerritoryListData listData = new TerritoryListData();
        List<TerritoryData> list = new ArrayList<TerritoryData>();
        SclCustomerModel customer =(SclCustomerModel)userService.getCurrentUser();
        List<TerritoryMasterModel> territoriesList = territoryMasterService.getTerritoriesForCustomer(customer);
        if(territoriesList!=null && !territoriesList.isEmpty()) {
            for(TerritoryMasterModel territoryMaster : territoriesList) {
                if(territoryMaster!=null) {
                    TerritoryData data = new TerritoryData();
                    data.setTerritoryCode(territoryMaster.getTerritoryCode());
                    data.setTerritoryName(territoryMaster.getTerritoryCode());
                    list.add(data);
                }
            }
        }
        listData.setTerritoryList(list);
        return listData;
    }

    @Override
    public TerritoryListData getTerritoriesForSO() {
        TerritoryListData listData = new TerritoryListData();
        List<TerritoryData> list = new ArrayList<TerritoryData>();
        List<TerritoryMasterModel> territoriesList = territoryMasterService.getTerritoriesForSO();
        if(territoriesList!=null && !territoriesList.isEmpty()) {
            for(TerritoryMasterModel territoryMaster : territoriesList) {
                if(territoryMaster!=null) {
                    TerritoryData data = new TerritoryData();
                    data.setTerritoryCode(territoryMaster.getTerritoryCode());
                    data.setTerritoryName(territoryMaster.getTerritoryCode());
                    list.add(data);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(list)){
            listData.setTerritoryList(list.stream().filter(obj-> Objects.nonNull(obj.getTerritoryCode())).sorted(Comparator.comparing(TerritoryData::getTerritoryCode)).collect(Collectors.toList()));
        }else {
            listData.setTerritoryList(list);
        }
        return listData;
    }

    @Override
    public CustomerListData getCustomerForUser(RequestCustomerData customerData) {
        List<SclCustomerModel> customerList = territoryMasterService.getCustomerForUser(customerData);
        List<CustomerData> allData = new ArrayList<CustomerData>();
        if(customerList!=null && !customerList.isEmpty()) {

            for(SclCustomerModel source: customerList) {
                CustomerData target = new CustomerData();
                target.setUid(source.getUid());
                target.setName(source.getName());
                target.setEmail(source.getEmail());
                target.setState(source.getState());
                target.setCustomerId(source.getCustomerNo());
                target.setContactNumber(source.getMobileNumber());
                target.setDealerCategory(source.getDealerCategory()!=null?source.getDealerCategory().getCode():"");
                if(source.getCounterType()!=null) {
                    target.setPartnerType(source.getCounterType().getCode());
                }

                target.setIsBillingBlock(source.getIsBillingBlock());
                target.setIsDeliveryBlock(source.getIsDeliveryBlock());
                target.setIsOrderBlock(source.getIsOrderBlock());
                allData.add(target);
            }
        }
        CustomerListData dataList = new CustomerListData();
        List<CustomerData> sortedCustomersData = allData.stream().sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList());
        dataList.setCustomers(sortedCustomersData);
        return dataList;
    }

    @Override
    public TerritoryListData getTerritoryForUser(String territoryId) {
        TerritoryListData listData = new TerritoryListData();
        List<TerritoryData> list = new ArrayList<TerritoryData>();
        List<TerritoryMasterModel> territoryMasterModelList = territoryMasterService.getTerritoryForUser(territoryId);
        if(territoryMasterModelList!=null && !territoryMasterModelList.isEmpty()) {
            for(TerritoryMasterModel territoryMaster : territoryMasterModelList) {
                if(territoryMaster!=null) {
                    TerritoryData data = new TerritoryData();
                    data.setTerritoryCode(territoryMaster.getTerritoryCode());
                    data.setTerritoryName(territoryMaster.getTerritoryCode());
                    list.add(data);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(list)) {
            listData.setTerritoryList(list.stream().filter(obj->Objects.nonNull(obj.getTerritoryCode())).sorted(Comparator.comparing(TerritoryData::getTerritoryCode)).collect(Collectors.toList()));
        }else{
            listData.setTerritoryList(list);
        }
        return listData;
    }

    @Override
    public DealerListData getAllSalesOfficersByState(String state) {
        List<SclUserModel> salesOfficerList = territoryMasterService.getAllSalesOfficersByState(state);
        DealerListData dataList = new DealerListData();
        if(salesOfficerList!=null && !salesOfficerList.isEmpty())
        {
            List<CustomerData> salesOfficerDataList=new ArrayList<>();
            for (SclUserModel sclUserModel : salesOfficerList) {
                CustomerData customerData=new CustomerData();
                customerData.setUid(sclUserModel.getUid());
                customerData.setName(sclUserModel.getName());
                customerData.setContactNumber(sclUserModel.getMobileNumber());
                salesOfficerDataList.add(customerData);
            }
            dataList.setDealers(salesOfficerDataList);
        }
        return dataList;
    }
}
