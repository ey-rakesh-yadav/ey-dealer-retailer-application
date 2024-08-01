package com.scl.core.services.impl;

import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.model.TerritoryMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.jalo.JaloObjectNoLongerValidException;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TerritoryMasterServiceImpl implements TerritoryMasterService {

    private static final Logger LOG = Logger.getLogger(TerritoryMasterServiceImpl.class);

    @Autowired
    TerritoryMasterDao territoryMasterDao;
    @Autowired
    UserService userService;
    @Autowired
    TerritoryManagementDao territoryManagementDao;
    @Autowired
    TerritoryManagementService territoryManagementService;
    @Autowired
    BaseSiteService baseSiteService;
    @Autowired
    SessionService sessionService;

    @Override
    public TerritoryMasterModel getTerritoryById(String territoryId) {
        return territoryMasterDao.getTerritoryById(territoryId) ;
    }

    @Override
    public TerritoryMasterModel getTerritoryMaster(String territoryPk) {
        return territoryMasterDao.getTerritoryByPk(territoryPk) ;
    }
    @Override
    public List<TerritoryMasterModel> getTerritoriesForCustomer(String customerId) {
        return getTerritoriesForCustomer((SclCustomerModel)userService.getUserForUID(customerId));
    }

    @Override
    public List<TerritoryMasterModel> getTerritoriesForCustomer(SclCustomerModel customer) {
        return territoryMasterDao.getTerritoriesForCustomer(customer);
    }

    @Override
    public List<TerritoryMasterModel> getTerritoriesForSO() {
        return territoryMasterDao.getTerritoryForSO();
    }

    @Override
    public List<TerritoryMasterModel> getTerritoriesForSO(String uid) {
        return territoryMasterDao.getTerritoriesForSO((SclUserModel) userService.getUserForUID(uid));
    }

    @Override
    public List<SclUserModel> getUserByTerritory(TerritoryMasterModel territoryMasster) {
        return territoryMasterDao.getUserByTerritory(territoryMasster);
    }
    
    
    @Override
    public List<SclCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData) {
        B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> customerList = null;
        if(currentUser instanceof SclUserModel && currentUser.getUserType()!=null && currentUser.getUserType().equals(SclUserType.TSO)){
            List<SubAreaMasterModel> subAreaMasterList = new ArrayList<SubAreaMasterModel>();
            if(StringUtils.isNotBlank(requestCustomerData.getSubAreaMasterPk())) {
                subAreaMasterList.add(territoryManagementService.getTerritoryById(requestCustomerData.getSubAreaMasterPk()));
            }
            else{
                FilterTalukaData filterTalukaData = new FilterTalukaData();
                subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
            }
            if(subAreaMasterList!=null && !subAreaMasterList.isEmpty()) {
                customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subAreaMasterList);
                return customerList;
            }
        }
       else if(currentUser instanceof SclUserModel ||
                (((SclCustomerModel) currentUser).getCounterType()==null) ||
                (( !((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP)))){
            List<TerritoryMasterModel> territoryMasterModelList = new ArrayList<TerritoryMasterModel>();
            if(StringUtils.isNotBlank(requestCustomerData.getTerritoryCode())) {
                territoryMasterModelList.add(getTerritoryMaster(requestCustomerData.getTerritoryCode()));
            }
            else{
                territoryMasterModelList = getTerritoryForUser(null);
            }
            if(territoryMasterModelList!=null && !territoryMasterModelList.isEmpty()) {
                customerList = territoryMasterDao.getCustomerForUser(requestCustomerData, territoryMasterModelList);
                return customerList;
            }
        }
        else{
            customerList=territoryManagementDao.getDealersForSP(requestCustomerData);
            return customerList;
        }
        return Collections.emptyList();
    }

    @Override
    public List<TerritoryMasterModel> getTerritoryForUser(String territoryId) {
        TerritoryMasterModel territory=null;
        if(territoryId!=null)
        {
            territory = getTerritoryById(territoryId);
        }
    return territoryMasterDao.getTerritoryForUser(territory);
    }

    @Override
    public List<SclUserModel> getAllSalesOfficersByState(String state) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        return territoryMasterDao.getAllSalesOfficersByState(state,site) ;
    }

   @Override
   public List<List<Object>> getDistrictAndTalukaForCustomer(SclCustomerModel customer){
        return territoryMasterDao.getDistrictAndTalukaForCustomer(customer);
    }

    @Override
    public void setCurrentTerritory(Collection<TerritoryMasterModel> territories) {
        Collection<TerritoryMasterModel> list = new ArrayList<TerritoryMasterModel>();
        if(territories==null || territories.isEmpty()) {
            if( userService.getCurrentUser() instanceof SclUserModel) {
                list.addAll(territoryMasterDao.getTerritoryForUser(null));
            }
            else if(userService.getCurrentUser() instanceof SclCustomerModel) {
                //TODO
            }
        }
        else {
            LOG.error("Territory Set from UI");
            list.addAll(territories);
        }
        sessionService.setAttribute("territoryMaster", list);
    }

    @Override
    public Collection<TerritoryMasterModel> getCurrentTerritory() {
        try {
            return sessionService.getAttribute("territoryMaster");
        } catch (JaloObjectNoLongerValidException var2) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Session Territory no longer valid. Removing from session. getCurrentTerritory will return empty list. {}", var2);
            }
            sessionService.setAttribute("territoryMaster", (Object)null);
            return Collections.emptyList();
        }
    }

}
