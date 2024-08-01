package com.scl.core.cart.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

import com.scl.core.cart.dao.SclB2BCartDao;
import com.scl.core.cart.service.SclB2BCartService;

import com.scl.core.enums.OrderType;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.commons.lang.StringUtils;

public class SclB2BCartServiceImpl implements SclB2BCartService  {
	
	private SclB2BCartDao sclB2BCartDao;
	
	public SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, int month, int year,String productName , String orderType)
	{
		Date startDate = new Date();
		Date endDate = new Date();
		if(month==0 && year==0) {
			LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
			LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
			startDate = getDateConstraint(firstDayOfMonth);
			Calendar cal = Calendar.getInstance();
			cal.setTime(getDateConstraint(lastDayOfMonth));
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			endDate = cal.getTime();
			
		}
		else {
			Calendar cal = Calendar.getInstance();
			cal.set(year, month-1, 1, 0, 0, 0);
			startDate=cal.getTime();
			cal.set(year, month-1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
			endDate=cal.getTime();
		}
		OrderType orderTypeEnum = null;
		if(StringUtils.isNotBlank(orderType)){
			orderTypeEnum = OrderType.valueOf(orderType);
		}
		return getSclB2BCartDao().getSavedCartsBySavedBy(user, searchPageData, filter, startDate, endDate,productName,orderTypeEnum);
	}

	protected static Date getDateConstraint(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = localDate.atStartOfDay(zone);
		Date date = Date.from(dateTime.toInstant());
		return date;
	}
	public SclB2BCartDao getSclB2BCartDao() {
		return sclB2BCartDao;
	}

	public void setSclB2BCartDao(SclB2BCartDao sclB2BCartDao) {
		this.sclB2BCartDao = sclB2BCartDao;
	}
}
