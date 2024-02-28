package com.eydms.facades.region;

import java.util.List;

import com.eydms.facades.data.CityData;
import com.eydms.facades.data.DistrictData;
import com.eydms.facades.data.StateData;
import com.eydms.facades.data.TalukaData;

public interface RegionFacade {


	List<CityData> findCityByTaluka(String talukaCode);

	List<TalukaData> findTalukaByDistrict(String districtCode);

	List<DistrictData> findDistrictByState(String stateCode);

	List<StateData> findAllState();


}
