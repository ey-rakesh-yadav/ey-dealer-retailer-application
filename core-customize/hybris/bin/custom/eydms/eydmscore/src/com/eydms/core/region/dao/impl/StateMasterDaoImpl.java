package com.eydms.core.region.dao.impl;

import com.eydms.core.model.StateMasterModel;
import com.eydms.core.region.dao.StateMasterDao;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMasterDaoImpl extends DefaultGenericDao<StateMasterModel> implements StateMasterDao {

    public StateMasterDaoImpl()
    {
        super(StateMasterModel._TYPECODE);
    }

    @Override
    public StateMasterModel findByCode(String stateCode) {
        if(stateCode!=null){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(StateMasterModel.CODE,stateCode);

            final List<StateMasterModel> stateMasterList = this.find(map);
            if(stateMasterList!=null && !stateMasterList.isEmpty()){
                return stateMasterList.get(0);
            }
        }
        return null;
    }
}
