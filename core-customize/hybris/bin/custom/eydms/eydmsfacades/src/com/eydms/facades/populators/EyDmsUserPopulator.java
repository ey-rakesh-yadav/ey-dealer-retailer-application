package com.eydms.facades.populators;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.EyDmsUserData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EyDmsUserPopulator implements Populator<EyDmsUserModel,EyDmsUserData> {

	@Autowired
    TerritoryManagementService territoryManagementService;
	
	@Override
	public void populate(EyDmsUserModel source, EyDmsUserData target) throws ConversionException {

		target.setName(source.getName());
		target.setUid(source.getUid());
		target.setContactNumber(source.getMobileNumber());
		target.setEmailId(source.getEmail());
		target.setEmployeeId(source.getEmployeeCode());
		target.setProfileUrl(source.getProfilePicture()!=null ? source.getProfilePicture().getURL() : null);
		if(source.getUserType()!=null && source.getUserType().equals(EyDmsUserType.SO)) {
			List<SubAreaMasterModel> subareas = territoryManagementService.getTerritoriesForSO(source.getUid());
			if(subareas!=null) {
				target.setSubAreas(subareas.stream().map(subArea -> subArea.getTaluka()).collect(Collectors.toList()));
			}
		}
	}

}
