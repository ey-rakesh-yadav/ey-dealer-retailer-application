package com.eydms.facades.populators;

import java.util.ArrayList;
import java.util.List;

import com.eydms.core.model.DealersFleetDetailsModel;
import com.eydms.core.model.TruckModelMasterModel;
import com.eydms.facades.data.DealerFleetData;
import com.eydms.facades.data.TruckModelData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class DealerFleetDetailsPopulator implements Populator<DealersFleetDetailsModel,DealerFleetData>
{

	@Override
	public void populate(DealersFleetDetailsModel source, DealerFleetData target) throws ConversionException {
		if(source!=null) {
			target.setId(source.getPk().toString());
			target.setCount(source.getCount());
			target.setCapacity(source.getTruckModel()!=null ? source.getTruckModel().getCapacity() : null);
			if(source.getTruckModel()!=null)
			{
				TruckModelData truckData = new TruckModelData();
				truckData.setTruckModel(source.getTruckModel().getTruckModel());
				truckData.setCapacity(source.getTruckModel().getCapacity());
				target.setTruckModel(truckData);
			}
			
			
		}
	}
	
	private List<TruckModelData> getTruckData(List<TruckModelMasterModel> truckModel)
	{
		List<TruckModelData> truckData = new ArrayList<>();
		if(!truckModel.isEmpty()&&(truckModel!=null)) {
			truckModel.stream().forEach(truck->{
				TruckModelData data = new TruckModelData();
				data.setTruckModel(truck.getTruckModel());
				data.setCapacity(truck.getCapacity());
				truckData.add(data);
			});
		}
		return truckData;
	}
}
