package com.eydms.core.dao.impl;

import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.DjpRunDao;
import com.eydms.core.model.DJPRunMasterModel;
import com.eydms.core.model.DataConstraintModel;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class DataConstraintDaoImpl extends DefaultGenericDao<DataConstraintModel> implements DataConstraintDao {

    @Resource
    UserService userService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public DataConstraintDaoImpl() {
        super(DataConstraintModel._TYPECODE);
    }

    @Override
    public Integer findDaysByConstraintName(String constraintName) {
		if(constraintName!=null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(DataConstraintModel.CONSTRAINTNAME, constraintName);

			final List<DataConstraintModel> dataList = this.find(map);
			if(dataList!=null && !dataList.isEmpty())
				return dataList.get(0).getDay();
		}
		return null;
	}

    @Override
    public String findVersionByConstraintName(String constraintName) {
        if(constraintName!=null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DataConstraintModel.CONSTRAINTNAME, constraintName);

            final List<DataConstraintModel> dataList = this.find(map);
            if(dataList!=null && !dataList.isEmpty())
                return dataList.get(0).getVersion();
        }
        return null;
    }

    @Override
    public List<DataConstraintModel> findAll() {

            final List<DataConstraintModel> dataList = this.find();
            if(dataList!=null && !dataList.isEmpty())
                return dataList;
            else{
                return Collections.emptyList();
            }
    }

}
