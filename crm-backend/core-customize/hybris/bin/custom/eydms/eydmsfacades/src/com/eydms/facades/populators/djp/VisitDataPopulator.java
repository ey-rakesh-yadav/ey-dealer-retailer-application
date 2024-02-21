package com.eydms.facades.populators.djp;

import com.eydms.core.model.VisitMasterModel;
import com.eydms.facades.data.VisitMasterData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class VisitDataPopulator implements Populator<VisitMasterModel,VisitMasterData>{

	@Override
	public void populate(VisitMasterModel source, VisitMasterData target) throws ConversionException {
		target.setId(source.getPk().toString());
		target.setVisitDate(source.getVisitPlannedDate());
		target.setStatus(source.getStatus().toString());
		target.setCounterCount(source.getCounterVisits().size());		
		if(source.getRoute()!=null) {
			target.setRoute(source.getRoute().getRouteId());
			target.setLocation(source.getRoute().getRouteName()!=null ? source.getRoute().getRouteName() : source.getRoute().getRouteId());
		}
		else if(source.getSubAreaMaster()!=null) {
			target.setLocation(source.getSubAreaMaster().getTaluka());
		}
		
		if(source.getSubAreaMaster()!=null) {
			target.setSubArea(source.getSubAreaMaster().getPk().toString());
			target.setTaluka(source.getSubAreaMaster().getTaluka());
			target.setDistrict(source.getSubAreaMaster().getDistrict());
		}
	}

}
