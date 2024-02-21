package com.eydms.core.user.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.eydms.facades.data.RequestCustomerData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.jalo.EyDmsCustomer;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.user.EYDMSUserService;
import com.eydms.facades.data.SOCockpitData;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

public class EYDMSUserServiceImpl implements EYDMSUserService {

	private static final Logger LOG = Logger.getLogger(EYDMSUserServiceImpl.class);

	@Autowired
	UserService userService;

	@Autowired
	EyDmsUserDao eydmsUserDao;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Override
	public SOCockpitData getOutstandingAmountAndBucketsForSO(String uid) {
		UserModel so = userService.getUserForUID(uid);
		SOCockpitData data = new SOCockpitData();
		try {
			//New Territory Change
			Collection<EyDmsCustomerModel> list = territoryManagementService.getDealersForSubArea();
			List<String> customerNo = new ArrayList<>();
			for(EyDmsCustomerModel customer : list)
			{
				customerNo.add(customer.getCustomerNo());
			}

			data.setOutstandingAmount(eydmsUserDao.getOutstandingAmountForSO(customerNo));

			List<List<Double>> bucketList = eydmsUserDao.getOutstandingBucketsForSO(customerNo);
			if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
			{
				try {
					data.setBucket1(bucketList.get(0).get(0)!=null?bucketList.get(0).get(0):0.0);
					data.setBucket2(bucketList.get(0).get(1)!=null?bucketList.get(0).get(1):0.0);
					data.setBucket3(bucketList.get(0).get(2)!=null?bucketList.get(0).get(2):0.0);
					data.setBucket4(bucketList.get(0).get(3)!=null?bucketList.get(0).get(3):0.0);
					data.setBucket5(bucketList.get(0).get(4)!=null?bucketList.get(0).get(4):0.0);
					data.setBucket6(bucketList.get(0).get(5)!=null?bucketList.get(0).get(5):0.0);
					data.setBucket7(bucketList.get(0).get(6)!=null?bucketList.get(0).get(6):0.0);
					data.setBucket8(bucketList.get(0).get(7)!=null?bucketList.get(0).get(7):0.0);
					data.setBucket9(bucketList.get(0).get(8)!=null?bucketList.get(0).get(8):0.0);
					data.setBucket10(bucketList.get(0).get(9)!=null?bucketList.get(0).get(9):0.0);
				}catch(Exception e)
				{
					LOG.info(e);
				}

			}
			else
			{
				data.setBucket1(0.0);
				data.setBucket2(0.0);
				data.setBucket3(0.0);
				data.setBucket4(0.0);
				data.setBucket5(0.0);
				data.setBucket6(0.0);
				data.setBucket7(0.0);
				data.setBucket8(0.0);
				data.setBucket9(0.0);
				data.setBucket10(0.0);
			}
			return data;
		}catch(NullPointerException e)
		{
			LOG.info(e);
		}

		return data;
	}

	@Override
	public Integer getDealersCountForDSOGreaterThanThirty(String userId) {

	//	List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
		
	//	List<EyDmsCustomerModel> dealerList = territoryManagementService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(Arrays.asList("Dealer"));
		List<EyDmsCustomerModel> dealerList = territoryManagementService.getCustomerforUser(requestCustomerData);

		List<String> customerNos = new ArrayList<>();
		
		for(EyDmsCustomerModel customer : dealerList)
		{
			if(customer.getCustomerNo()!=null)
				customerNos.add(customer.getCustomerNo());
		}
		
		Integer count = 0;
		
		if(!customerNos.isEmpty())
		{
			count = eydmsUserDao.getDealersCountForDSOGreaterThanThirty(customerNos);
		}
		
		return count;
	}

}