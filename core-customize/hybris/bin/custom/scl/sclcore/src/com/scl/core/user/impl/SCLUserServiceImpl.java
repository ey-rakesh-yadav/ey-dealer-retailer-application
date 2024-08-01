package com.scl.core.user.impl;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.services.AmountFormatService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.facades.data.RequestCustomerData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.SclUserDao;
import com.scl.core.jalo.SclCustomer;
import com.scl.core.jalo.SclUser;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.user.SCLUserService;
import com.scl.facades.data.SOCockpitData;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

public class SCLUserServiceImpl implements SCLUserService {

	private static final Logger LOG = Logger.getLogger(SCLUserServiceImpl.class);

	@Autowired
	UserService userService;

	@Autowired
	SclUserDao sclUserDao;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
	SalesPerformanceService salesPerformanceService;
	@Autowired
	AmountFormatService amountFormatService;


	@Override
	public SOCockpitData getOutstandingAmountAndBucketsForSO(String uid) {
		UserModel so = userService.getUserForUID(uid);
		SOCockpitData data = new SOCockpitData();
		try {
			//New Territory Change
			Collection<SclCustomerModel> list = territoryManagementService.getDealersForSubArea();
			List<String> customerNo = new ArrayList<>();
			for(SclCustomerModel customer : list)
			{
				customerNo.add(customer.getCustomerNo());
			}

			data.setOutstandingAmount(sclUserDao.getOutstandingAmountForSO(customerNo));
			//data.setOutstandingAmount(Double.valueOf(amountFormatService.getFormattedValue(sclUserDao.getOutstandingAmountForSO(customerNo))));

			List<List<Double>> bucketList = sclUserDao.getOutstandingBucketsForSO(customerNo);
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
		
	//	List<SclCustomerModel> dealerList = territoryManagementService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(Arrays.asList("Dealer"));
		//List<SclCustomerModel> dealerList = territoryManagementService.getCustomerforUser(requestCustomerData);

		List<SclCustomerModel> dealerList= salesPerformanceService.getCustomersByLeadType("Dealer",null,null,null);

		List<String> customerNos = new ArrayList<>();
		
		for(SclCustomerModel customer : dealerList)
		{
			if(customer.getCustomerNo()!=null)
				customerNos.add(customer.getCustomerNo());
		}
		
		Integer count = 0;
		
		if(!customerNos.isEmpty())
		{
			count = sclUserDao.getDealersCountForDSOGreaterThanThirty(customerNos);
		}
		
		return count;
	}

}