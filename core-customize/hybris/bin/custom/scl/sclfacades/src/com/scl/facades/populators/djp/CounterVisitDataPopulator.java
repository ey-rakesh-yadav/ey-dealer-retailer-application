package com.scl.facades.populators.djp;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DJPVisitDao;
import com.scl.core.enums.CounterType;
import com.scl.core.jalo.SclCustomer;
import com.scl.core.model.CounterVisitMasterModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.DJPVisitService;
import com.scl.facades.data.CounterVisitData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class CounterVisitDataPopulator implements Populator<CounterVisitMasterModel,CounterVisitData>{

	@Autowired
	DJPVisitService djpVisitService;

	@Autowired
	UserService userService;

	@Autowired
	DJPVisitDao djpVisitDao;
	@Override
	public void populate(CounterVisitMasterModel source, CounterVisitData target) throws ConversionException {
		target.setCounterName(source.getSclCustomer().getName());
		String endVisitDate = "";
		/*if(source.getSclCustomer().getLastVisitTime()!=null){
			DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
			visitDate = dateFormat.format(source.getSclCustomer().getLastVisitTime());
		}*/
		UserModel userModel =userService.getCurrentUser();
		//set endVisitTime
		if(userModel != null) {
			SclUserModel sclUser = new SclUserModel();
			if (userModel instanceof SclUserModel) {
				 sclUser = (SclUserModel) userModel;
			}
			Date endVisitTime=djpVisitDao.getEndVisitTime(sclUser,source.getSclCustomer());
			if(Objects.nonNull(endVisitTime)) {
				DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
				endVisitDate  =dateFormat.format(endVisitTime);
				target.setLastVisitDate(endVisitDate);
			}
		}
		target.setId(source.getPk().toString());
		target.setCounterPotential(source.getSclCustomer().getCounterPotential());
		double latitude=0,longitude=0;
		if(source.getSclCustomer().getLatitude()!=null)
			latitude = source.getSclCustomer().getLatitude();
		if(source.getSclCustomer().getLongitude()!=null)
			longitude = source.getSclCustomer().getLongitude();
		target.setLatitude(latitude);
		target.setLongitude(longitude);
		target.setCounterType(source.getCounterType()!=null?source.getCounterType().getCode():"");
		target.setCounterCode(source.getSclCustomer().getUid());
		target.setIsAdoc(source.getIsAdHoc());
		target.setSequence(source.getSequence());
		target.setCustomerNo(source.getSclCustomer().getCustomerNo());

		if(djpVisitService.isNonSclCounter(source.getSclCustomer())){
			target.setIsNonSclCounter(Boolean.TRUE);
			if(CollectionUtils.isNotEmpty(source.getSclCustomer().getAddresses())){
				AddressModel addressModel=source.getSclCustomer().getAddresses().iterator().next();
				target.setLine1(addressModel.getLine1());
				target.setLine2(addressModel.getLine2());
			}
		}else{
			target.setIsNonSclCounter(Boolean.FALSE);
			AddressModel addressModel=djpVisitService.getCustomerOwnAddress(source.getSclCustomer());
			if(Objects.nonNull(addressModel)) {
				target.setLine1(addressModel.getLine1());
				target.setLine2(addressModel.getLine2());
			}
		}
	}
}
