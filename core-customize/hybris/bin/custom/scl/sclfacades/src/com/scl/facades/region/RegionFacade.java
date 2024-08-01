package com.scl.facades.region;

import java.util.List;

import com.scl.facades.data.CityData;
import com.scl.facades.data.DistrictData;
import com.scl.facades.data.StateData;
import com.scl.facades.data.TalukaData;

public interface RegionFacade {


	List<CityData> findCityByTaluka(String talukaCode);

	List<TalukaData> findTalukaByDistrict(String districtCode);

	List<DistrictData> findDistrictByState(String stateCode);

	List<StateData> findAllState();


}
