package com.scl.facades.populators;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.enums.SclUserType;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.SclUserData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SclUserPopulator implements Populator<SclUserModel,SclUserData> {

	@Autowired
    TerritoryManagementService territoryManagementService;
	
	@Override
	public void populate(SclUserModel source, SclUserData target) throws ConversionException {

		target.setName(source.getName());
		target.setUid(source.getUid());
		target.setContactNumber(source.getMobileNumber());
		target.setEmailId(source.getEmail());
		target.setEmployeeId(source.getEmployeeCode());
		target.setProfileUrl(source.getProfilePicture()!=null ? source.getProfilePicture().getURL() : null);
		if(source.getUserType()!=null && source.getUserType().equals(SclUserType.SO)) {
			List<SubAreaMasterModel> subareas = territoryManagementService.getTerritoriesForSO(source.getUid());
			if(subareas!=null) {
				target.setSubAreas(subareas.stream().filter(subarea->subarea.getTaluka()!=null).map(SubAreaMasterModel::getTaluka).collect(Collectors.toList()));
			}
		}
	}

}
