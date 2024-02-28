package com.eydms.core.orderhistory.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eydms.core.enums.OrderType;
import com.eydms.core.orderhistory.dao.OrderHistoryDao;
import com.eydms.core.orderhistory.service.OrderHistoryService;
import com.eydms.facades.orderhistory.data.DispatchDetailsData;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;


public class OrderHistoryServiceImpl implements OrderHistoryService{

	private UserService userService;
	private OrderHistoryDao orderHistoryDao;
	private BaseSiteService baseSiteService;
	private TimeService timeService;

	@Override
	public Map<String, Object> getDispatchDetails(String sourceType, String date) {
		final UserModel user = getUserService().getCurrentUser();
		if(date==null)
		{
			date = LocalDate.now().toString();
		}
		return getOrderHistoryDao().getDispatchDetails(sourceType, date, user);
	}
	
	@Override
	public SearchPageData<OrderEntryModel> getTradeOrderListing(SearchPageData paginationData, String sourceType, String filter, int month, int year,String productName , String orderType, String status)
	{
		final UserModel user = getUserService().getCurrentUser();
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
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return getOrderHistoryDao().getTradeOrderListing(paginationData, sourceType, user, startDate, endDate, site, filter,productName,orderTypeEnum,status);
	}
	
	protected static Date getDateConstraint(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = localDate.atStartOfDay(zone);
		Date date = Date.from(dateTime.toInstant());
		return date;
	}
	
	public OrderHistoryDao getOrderHistoryDao() {
		return orderHistoryDao;
	}

	public void setOrderHistoryDao(OrderHistoryDao orderHistoryDao) {
		this.orderHistoryDao = orderHistoryDao;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public BaseSiteService getBaseSiteService() {
		return baseSiteService;
	}

	public void setBaseSiteService(BaseSiteService baseSiteService) {
		this.baseSiteService = baseSiteService;
	}

	public TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

}
