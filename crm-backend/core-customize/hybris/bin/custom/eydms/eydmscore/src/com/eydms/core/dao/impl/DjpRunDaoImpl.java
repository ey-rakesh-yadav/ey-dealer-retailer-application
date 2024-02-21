package com.eydms.core.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.DjpRunDao;
import com.eydms.core.model.DJPRunMasterModel;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.user.UserService;

public class DjpRunDaoImpl extends DefaultGenericDao<DJPRunMasterModel> implements DjpRunDao {

	@Autowired
	UserService userService;
	public DjpRunDaoImpl() {
		super(DJPRunMasterModel._TYPECODE);
	}

	@Override
	public DJPRunMasterModel findByPlannedDateAndUser(String plannedDate, String district, String taluka, String brand) {
		if(plannedDate!=null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(DJPRunMasterModel.PLANDATE, plannedDate);
			map.put(DJPRunMasterModel.DISTRICT, district);
			map.put(DJPRunMasterModel.TALUKA, taluka);
			map.put(DJPRunMasterModel.BRAND, brand);

			final List<DJPRunMasterModel> djpRunList = this.find(map);
			if(djpRunList!=null && !djpRunList.isEmpty())
				return djpRunList.get(0);
		}
		return null;
	}
}
