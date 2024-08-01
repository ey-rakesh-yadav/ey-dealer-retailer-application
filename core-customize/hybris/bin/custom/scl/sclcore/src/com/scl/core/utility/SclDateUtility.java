package com.scl.core.utility;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class SclDateUtility {

	private static final String DEFAULTDATEFORMAT = "yyyy-MM-dd";

	public static Date getStartDateConstraint(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = localDate.atStartOfDay(zone);
		Date date = Date.from(dateTime.toInstant());
		return date;
	}

	public static String getMtdClauseQuery(String columnName, Map<String, Object> map) {
		LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
		return getDateQuery(columnName, firstDayOfMonth, lastDayOfMonth, map);
	}

	public static String getLastMonthClauseQuery(String columnName, Map<String, Object> map) {
		LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
		LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).minusMonths(1);
		return getDateQuery(columnName, firstDayOfMonth, lastDayOfMonth, map);
	}
	
	public static String getCurrentPreviousMonthClauseQuery(String columnName, Map<String, Object> map) {
		LocalDate previousMonthFirstDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
		LocalDate nextDay = LocalDate.now().plusDays(1);
		return getDateQuery(columnName, previousMonthFirstDay, nextDay, map);
	}

	public static String getMtdClauseQueryRetailer(String columnName, Map<String, Object> map) {
		LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
		LocalDate lastDayOfMonth = LocalDate.now().plusDays(1);
		return getDateQuery(columnName, firstDayOfMonth, lastDayOfMonth, map);
	}

	public static String getLastXDayQuery(String columnName, Map<String, Object> map, Integer lastXDay) {
		LocalDate startDate = LocalDate.now().minusDays(lastXDay);
		LocalDate endDate = LocalDate.now().plusDays(1);
		return getDateQuery(columnName, startDate, endDate, map);
	}

	public static String getLastXMonthQuery(String columnName, Map<String, Object> map, Integer lastXMonths) {
		LocalDate startDate = LocalDate.now().minusMonths(lastXMonths);
		LocalDate endDate = LocalDate.now().plusDays(1);
		return getDateQuery(columnName, startDate, endDate, map);
	}
	
	public static String getDateRangeClauseQuery(String columnName, String dateFormat, String startDate, String endDate, Map<String, Object> map) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		LocalDate firstDayOfMonth = LocalDate.parse(startDate, formatter);
		LocalDate lastDayOfMonth = LocalDate.parse(endDate, formatter).plusDays(1);
		return getDateQuery(columnName, firstDayOfMonth, lastDayOfMonth, map);
	}

	public static String getDateRangeClauseQuery(String columnName,String startDate, String endDate, Map<String, Object> map) {
		return getDateRangeClauseQuery(columnName, DEFAULTDATEFORMAT, startDate, endDate, map);
	}
	
	public static String getDateClauseQueryByMonthYear(String columnName,int month, int year, Map<String, Object> map) {
		if(year==0 && month==0) {
			return getMtdClauseQuery(columnName, map);
		}
		YearMonth ym = YearMonth.of(year, month);
		LocalDate firstDayOfMonth = ym.atDay(1);
		LocalDate lastDayOfMonth = ym.atEndOfMonth().plusDays(1);

		return getDateQuery(columnName, firstDayOfMonth, lastDayOfMonth, map);
	}

	private static String getDateQuery(String columnName, LocalDate firstDayOfMonth, LocalDate lastDayOfMonth, Map<String, Object> map) {
		StringBuilder builder = new StringBuilder();
		builder.append(" {").append(columnName)
		.append("} >= ?start and {").append(columnName).append("} < ?end ");

		map.put("start", getStartDateConstraint(firstDayOfMonth));
		map.put("end", getStartDateConstraint(lastDayOfMonth));

		return builder.toString();
	}

	//count
	public static String getSixAndFourMonthsClauseQuery(String columnName, Map<String, Object> map) {
		LocalDate date = LocalDate.now();
		LocalDate startDate = date.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endDate= date.minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
		return getDateQuery(columnName, startDate, endDate, map);
	}

	//count>0 and count=0
	public static String getSixMonthsClauseQuery(String columnName, Map<String, Object> map) {
		LocalDate date = LocalDate.now();
		LocalDate startDate= date.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endDate=date.plusDays(1);
		return getDateQuery(columnName, startDate, endDate, map);
	}
	public static String getYear(Date date){
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		return yearFormat.format(date);
	}
	public static String getMonth(Date date){
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
		return monthFormat.format(date);
	}
	public static Date parseMeetingDate(String date) {
		if(Objects.nonNull(date)) {
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm");
			try{
				return format.parse(date);
			}catch (ParseException pe){
				throw new ConversionException(String.format("Unable to parse Date for %s", date));
			}
		}
		return null;
	}
	public static Date getMonthStartDate() {
		Calendar cal=Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	public static Date getThreeMonthBackDate() {
		Calendar cal=Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH,-3);
		return cal.getTime();
	}
	public static String getFormattedDate(Date date,String format){
		if(Objects.isNull(date)){
			return null;
		}
		SimpleDateFormat monthFormat = new SimpleDateFormat(format);
		return monthFormat.format(date);
	}
	public static Date getFirstDayOfFinancialYear() {
		Calendar cal = Calendar.getInstance();
		var currentMonth=cal.get(Calendar.MONTH);
		if(currentMonth<4){
			cal.add(Calendar.YEAR,-1);
		}
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}



	public static LocalDate getDateMinusXDays(int days) {
		LocalDate date = LocalDate.now().minusDays(days);
		return date;
	}
	public static LocalDate getSpecificDayOfMonth(int day) {
		LocalDate date = LocalDate.now().withDayOfMonth(day);
		return date;
	}
}
