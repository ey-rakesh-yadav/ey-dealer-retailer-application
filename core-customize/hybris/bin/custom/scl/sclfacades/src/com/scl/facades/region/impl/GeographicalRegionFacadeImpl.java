package com.scl.facades.region.impl;

import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.region.service.GeographicalRegionService;
import com.scl.facades.data.GeographicalMasterData;
import com.scl.facades.data.PincodeData;
import com.scl.facades.region.GeographicalRegionFacade;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeographicalRegionFacadeImpl implements GeographicalRegionFacade{

	Logger LOG=Logger.getLogger(GeographicalRegionFacadeImpl.class);

	@Resource
	GeographicalRegionService geographicalRegionService;
	
	@Override
	public List<String> findAllState() {
		return geographicalRegionService.findAllState();
	}

	@Override
	public List<String> findAllDistrict(String state) {
		return geographicalRegionService.findAllDistrict(state);
	}

	@Override
	public List<String> findAllTaluka(String state, String district) {
		return geographicalRegionService.findAllTaluka(state, district);
	}

	@Override
	public List<String> findAllErpCity(String state, String district, String taluka) {
		return geographicalRegionService.findAllErpCity(state, district, taluka);
	}

	@Override
	public List<String> findAllErpCity(String state, String district) {
		return geographicalRegionService.findAllErpCity(state, district);
	}
	
	@Override
	public List<GeographicalMasterData> getGeographyByPincode(String pincode) {
		List<GeographicalMasterData> dataList = new ArrayList<GeographicalMasterData>();
		List<GeographicalMasterModel> modelList = geographicalRegionService.getGeographyByPincode(pincode);
		if(modelList!=null) {
			modelList.stream().forEach(model -> {
				GeographicalMasterData data = new GeographicalMasterData();
				data.setZone(model.getZone());
				data.setState(model.getState());
				data.setGeographicalSate(model.getGeographicalState());
				data.setGoogleMapState(model.getGoogleMapState());
				data.setDistrict(model.getDistrict());
				data.setTaluka(model.getTaluka());
				data.setErpCity(model.getErpCity());
				data.setPincode(model.getPincode());
				dataList.add(data);
			});
		}
		return dataList;
	}

	@Override
	public List<String> getBusinessState(String state, String district, String taluka, String erpCity){
		return geographicalRegionService.getBusinessState(state, district, taluka, erpCity);
	}
	
	@Override
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState){
		return geographicalRegionService.getGeographicalStateByGoogleMapState(googleMapState);
	}

	@Override
	public String getErpStateForGstState(String gstState) {
		return geographicalRegionService.getErpStateForGstState(gstState);
	}
	
	@Override
	public List<String> findAllLpSourceErpCity(String dealerId,String retailerUid,String state, String district, String taluka) {
		return geographicalRegionService.findAllLpSourceErpCity(dealerId,retailerUid,state, district, taluka);
	}
	
	@Override
	public List<String> findAllLpSourceTaluka(String state, String district,String dealerId,String retailerUid) {
		return geographicalRegionService.findAllLpSourceTaluka(state, district,dealerId,retailerUid);
	}
	
	@Override
	public List<String> findAllLpSourceDistrict(String dealerId,String retailerUid, String state) {
		return geographicalRegionService.findAllLpSourceDistrict(dealerId,retailerUid, state);
	}

	/**
	 *
	 * @return
	 */
	@Override
	public List<String> findAllLpSourceState() {
		return geographicalRegionService.findAllLpSourceState();
	}

	@Override
	public List<String> findUserState(String customerCode) {
		return geographicalRegionService.findUserState(customerCode);
	}

	/**
	 *
	 * @param dealerId
	 * @param state
	 * @param district
	 * @param taluka
	 * @param city
	 * @return
	 */
	@Override
	public List<PincodeData> findAllLpSourcePincode(String dealerId,String retailerUid,String state, String district, String taluka, String city) {
		List<PincodeData> list = new ArrayList<>();
		try {
			List<List<Object>> pincodes = geographicalRegionService.findAllLpSourcePincode(dealerId, retailerUid, state, district, taluka, city);
			if (CollectionUtils.isNotEmpty(pincodes)) {
				for (List<Object> pincode : pincodes) {
					if (pincode != null && pincode.size() == 2) {
						PincodeData pincodeData = new PincodeData();
						if (pincode.get(0) != null) {
							pincodeData.setPincode((String) pincode.get(0));
						}
						if (pincode.get(1) != null) {
							//GeographicalMasterModel geography = (GeographicalMasterModel) pincode.get(1);
							pincodeData.setTransportationZone((String) pincode.get(1));
							if (pincodeData.getPincode() == null) {
								GeographicalMasterModel geographicalMasterModel = geographicalRegionService.getGeographicalMaster((String) pincode.get(1));
								pincodeData.setPincode(geographicalMasterModel.getPincode());
							}
						}
						list.add(pincodeData);
					}
				}
			}
			return list;
		}catch (Exception ex){
			LOG.error(String.format("Exception got in fetch pincode API ::%s with cause::%s",ex.getMessage(),ex.getCause()));
		}
		return Collections.EMPTY_LIST;
	}


	/**
	 * @param retailerId
	 * @param dealerId
	 * @return
	 */
	@Override
	public List<String> findAllLpSourceState(String dealerId,String retailerId) {
		return geographicalRegionService.findAllLpSourceState(dealerId,retailerId);
	}

	/**
	 * @param state
	 * @param district
	 * @param taluka
	 * @param erpCity
	 * @return
	 */
	@Override
	public List<PincodeData> findPincode(String state, String district, String taluka, String erpCity) {

		List<List<Object>> pinCodes =  geographicalRegionService.findPincode(state,district,taluka,erpCity);
		return setPinCodeData(pinCodes);
	}

	/**
	 *
	 * @param pinCodeList
	 * @return
	 */
	private List<PincodeData> setPinCodeData(List<List<Object>> pinCodeList){

		List<PincodeData> list = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(pinCodeList)) {
			for(List<Object> pinCode : pinCodeList) {
				if(pinCode!=null && pinCode.size()==2) {
					PincodeData pincodeData = new PincodeData();
					if(pinCode.get(0)!=null) {
						pincodeData.setPincode(String.valueOf(pinCode.get(0)));
					}
					if(pinCode.get(1)!=null) {
						pincodeData.setTransportationZone(String.valueOf(pinCode.get(1)));
					}
					list.add(pincodeData);
				}
			}
		}
		return list;
	}
}
