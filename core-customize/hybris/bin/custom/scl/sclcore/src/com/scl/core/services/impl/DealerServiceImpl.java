package com.scl.core.services.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.scl.core.dao.*;
import com.scl.core.model.*;
import com.scl.core.services.NetworkService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.facades.CreditLimitData;
import com.scl.facades.data.*;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.BooleanUtils;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.CounterType;
import com.scl.core.order.dao.OrderValidationProcessDao;
import com.scl.core.services.DealerService;
import com.scl.core.services.TerritoryManagementService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;

public class DealerServiceImpl implements DealerService{

	private static final Logger LOG = Logger.getLogger(DealerServiceImpl.class);
	private static final String DEALER = "DEALER";
	private static final String RETAILER = "RETAILER";

	@Autowired
	UserService userService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	NetworkService networkService;
	
	@Autowired
	DealerDao dealerDao;

	@Autowired
	private DJPVisitDao djpVisitDao;
	@Autowired
	private CollectionDao collectionDao;
	
	@Autowired
	OrderValidationProcessDao orderValidationProcessDao;

	@Autowired
	ProductService productService;
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	@Autowired
	SalesPerformanceService salesPerformanceService;
	@Autowired
	SalesPerformanceDao salesPerformanceDao;

	@Autowired
	ModelService modelService;

	@Autowired
	KeyGenerator partnerCustomerIdGenerator;

	@Autowired
	DataConstraintDao dataConstraintDao;

	@Autowired
	private Populator<PartnerCustomerData, PartnerCustomerModel> partnerCustomerReversePopulator;
	
	@Override
	public List<MonthlySalesData> getLastSixMonthSalesForDealer(String customerId, String Filter, String customerType,String retailerId) {
		List<MonthlySalesData> dataList = new ArrayList<>();
		final B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		List<SclCustomerModel> customers = new ArrayList<>();
		List<SclCustomerModel> dealersList = new ArrayList<>();
		if (!Objects.isNull(currentUser)) {
			Set<PrincipalGroupModel> ugSet = currentUser.getGroups();

			if (retailerId == null) {
				if (ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
					if (customerType.equalsIgnoreCase("RETAILER")) {
						customers = territoryManagementService.getRetailerListForDealer();
						dealersList.add((SclCustomerModel) currentUser);
					} else if (customerType.equalsIgnoreCase("INFLUENCER")) {
						customers = territoryManagementService.getInfluencerListForDealer();
					}
				} else if (ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {

					if (customerType.equalsIgnoreCase("INFLUENCER")) {
						customers = territoryManagementService.getInfluencerListForRetailer();
					}
				}
			} else {
				SclCustomerModel retailer = (SclCustomerModel) userService.getUserForUID(retailerId);
				customers.add(retailer);
			}
		}

			Map<String, Double> salesQtyCM = new HashMap<>();
			Map<String, Double> salesQtyLM = new HashMap<>();
			if (Filter.equalsIgnoreCase("MTD")) {

				//    if (taluka.equalsIgnoreCase("ALL")) {

				List<Double> monthlySale = new ArrayList<>();
				List<Double> lastMonthWiseList = new ArrayList<>();
				List<Double> listOfCurrentMonthSalesTarget = new ArrayList<>();
				List<Double> listOfLastMonthSalesTarget = new ArrayList<>();
				List<Double> growthList = new ArrayList<>();

				LocalDate date = LocalDate.now();
				// LocalDate currentMonth = LocalDate.now();
				for (int i = 0; i <= 6; i++) {
					LocalDate lastMonth1 = date.minusMonths(i);
					int month = lastMonth1.getMonthValue();
					int year = lastMonth1.getYear();
					String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
					String formattedMonth = monthName.concat("-").concat(String.valueOf(year));
					if (customerType.equalsIgnoreCase("RETAILER")) {
						salesQtyCM = networkService.getSalesQuantityForRetailerByMonthYear(customers, date.getMonthValue(), date.getYear(), dealersList, null);
						salesQtyLM = networkService.getSalesQuantityForRetailerByMonthYear(customers, lastMonth1.getMonthValue(), lastMonth1.getYear(), dealersList, null);


						Double currentMonthSalesTarget = salesPerformanceService.getMonthlySalesTargetForRetailer(customers, baseSiteService.getCurrentBaseSite(), null, formattedMonth);
						LOG.info(String.format("Retailer currentMonthSalesTarget:%s", currentMonthSalesTarget));
						Double lastMonthSalesTarget = salesPerformanceService.getMonthlySalesTargetForRetailer(customers, baseSiteService.getCurrentBaseSite(), null, formattedMonth);
						LOG.info(String.format("Dealers lastMonthSalesTarget:%s", lastMonthSalesTarget));
						if (!(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
							double growth = salesQtyCM.get("quantityInMT") - salesQtyLM.get("quantityInMT");
							monthlySale.add(salesQtyCM.get("quantityInMT"));
							lastMonthWiseList.add(salesQtyLM.get("quantityInMT"));
							growthList.add(growth);
						}
						else{
							double growth = salesQtyCM.get("quantityInBags") - salesQtyLM.get("quantityInBags");
							monthlySale.add(salesQtyCM.get("quantityInBags"));
							lastMonthWiseList.add(salesQtyLM.get("quantityInBags"));
							growthList.add(growth);
						}
						listOfCurrentMonthSalesTarget.add(currentMonthSalesTarget);
						listOfLastMonthSalesTarget.add(lastMonthSalesTarget);
					}

                /*monthlySale.add(monthSale);
                lastMonthWiseList.add(lastMonthWiseSale);*/
				}

				for (int i = 0; i <= 5; i++) {
					MonthlySalesData data = new MonthlySalesData();
					LocalDate lastMonth2 = date.minusMonths(i);
					int month = lastMonth2.getMonthValue();
					int year = lastMonth2.getYear();

					data.setMonthYear(getMonthName(month - 1).concat(" ").concat(String.valueOf(year)));

					double currentMonthSales = lastMonthWiseList.get(i);
					double lastMonthSales = lastMonthWiseList.get(i + 1);
					double monthWiseAvgSale = monthlySale.get(i);
					double lastMonthAvgSale = lastMonthWiseList.get(i);

					//* to do - optimize *//*
					if (date.getMonthValue() != 0 && monthWiseAvgSale != 0)
						data.setActualSales(monthWiseAvgSale);
					else
						data.setActualSales(0.0);

					if (lastMonth2.getMonthValue() != 0 && lastMonthAvgSale != 0)
						data.setActualSales(lastMonthAvgSale);
					else
						data.setActualSales(0.0);
					LOG.info(String.format("Dealers Actual:%s", data.getActualSales()));

					data.setGrowth(currentMonthSales - lastMonthSales);
					LOG.info(String.format("Dealers Growth:%s", data.getGrowth()));

					double monthlySalesTarget = listOfCurrentMonthSalesTarget.get(i);
					double lastMonthSalesTarget = listOfLastMonthSalesTarget.get(i);

					if (date.getMonthValue() != 0 && monthlySalesTarget != 0.0)
						data.setTargetSales(monthlySalesTarget);
					else
						data.setTargetSales(0.0);

					if (lastMonth2.getMonthValue() != 0 && lastMonthSalesTarget != 0.0)
						data.setTargetSales(lastMonthSalesTarget);
					else
						data.setTargetSales(0.0);

					if (monthlySalesTarget > 0) {
						data.setPercentage((currentMonthSales / monthlySalesTarget) * 100);
					} else {
						data.setPercentage(0.0);
					}
					data.setGrowth(growthList.get(i));
					LOG.info(String.format("Retailer Percentage:%s", data.getPercentage()));
					dataList.add(data);

					if (month != 0)
						--month;
					else {
						month = 11;
						--year;
					}
				}
			}

			if (Filter.equalsIgnoreCase("YTD")) {
				List<Double> yearWiseSales = new ArrayList<>();
				List<Double> yearWiseTarget = new ArrayList<>();

				int startYear = 0, endYear = 0;
				if (LocalDate.now().getMonth().getValue() >= 4) {
					endYear = LocalDate.now().getYear() + 1;//24
					startYear = LocalDate.now().getYear();
				} else {
					startYear = LocalDate.now().getYear() - 1;
					endYear = LocalDate.now().getYear();
				}

				for (int i = 0; i <= 3; i++) {
					double currentSales = 0.0, targetSalesAnnual = 0.0;
					if (customerType.equalsIgnoreCase("RETAILER")) {
						salesQtyCM=networkService.getSalesQuantityForRetailerByYTD(customers, dealersList, null, startYear, endYear);
						StringBuilder f = new StringBuilder();
						String financialYear = String.valueOf(f.append(startYear - i).append("-").append(endYear - i));
						targetSalesAnnual = salesPerformanceDao.getAnnualSalesTargetForRetailer(customers, financialYear, null);
						if (!(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
							if (salesQtyCM.get("quantityInMT") != null) {
								yearWiseSales.add(salesQtyCM.get("quantityInMT"));
							} else {
								yearWiseSales.add(0.0);
							}
						}else{
							if (salesQtyCM.get("quantityInBags") != null) {
								yearWiseSales.add(salesQtyCM.get("quantityInBags"));
							} else {
								yearWiseSales.add(0.0);
							}
						}
						yearWiseTarget.add(targetSalesAnnual);
					}
				}


				if (LocalDate.now().getMonth().getValue() >= 4) {
					endYear = LocalDate.now().getYear() + 1;//24
					startYear = LocalDate.now().getYear();
				} else {
					startYear = LocalDate.now().getYear() - 1;
					endYear = LocalDate.now().getYear();
				}
				for (int i = 0; i <= 2; i++) {
					MonthlySalesData data = new MonthlySalesData();
					double currentYrSales = yearWiseSales.get(i);
					double previousYrSales = yearWiseSales.get(i + 1);
					double currentYearTarget = yearWiseTarget.get(i);

					data.setActualSales(currentYrSales);
					//if (previousYrSales != 0.0) {
					data.setGrowth(currentYrSales - previousYrSales);
					//}
					LOG.info("Growth YTD" + data.getGrowth());
					StringBuilder f = new StringBuilder();
					String financialYear = String.valueOf(f.append(startYear - i).append("-").append(endYear - i));
					data.setMonthYear(financialYear);
					data.setTargetSales(currentYearTarget);
					if (currentYearTarget > 0) {
						data.setPercentage((currentYrSales / currentYearTarget) * 100);
					} else {
						data.setPercentage(0.0);
					}
					LOG.info("Percentage YTD" + data.getPercentage());
					dataList.add(data);
				}
			}
			return dataList;
		}

	/*{
		
		 List<MonthlySalesData> dataList = new ArrayList<>();
	     
	     SclCustomerModel customer = (SclCustomerModel) userService.getUserForUID(customerId);
	     Calendar cal = Calendar.getInstance();
	     Date currentDate = cal.getTime();// jul-2023
	     
	     Date startDate = null;
	     Date endDate = null;
	     
	     cal.set(Calendar.MONTH, 2);
	     cal.set(Calendar.DATE, 31);
	     cal.set(Calendar.HOUR, 23);
	     cal.set(Calendar.MINUTE, 59);
	     cal.set(Calendar.SECOND, 59);
	     
	     Date financialYrEndDate = cal.getTime();
	     
	     if(!Objects.isNull(customer))
	     {
	    	Set<PrincipalGroupModel> ugSet=customer.getGroups();
		    
	    	List<SclCustomerModel> customers = new ArrayList<>();
	    	List<String> customerIds = new ArrayList<>();

	    	if(retailerId==null) {
				if (ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
					if (customerType.equalsIgnoreCase("RETAILER")) {
						customers = territoryManagementService.getRetailerListForDealer();
					} else if (customerType.equalsIgnoreCase("INFLUENCER")) {
						customers = territoryManagementService.getInfluencerListForDealer();
					}
				} else if (ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {

					if (customerType.equalsIgnoreCase("INFLUENCER")) {
						customers = territoryManagementService.getInfluencerListForRetailer();
					}
				}
			}
			*//* if(retailerId!=null) {

				SclCustomerModel retailer = (SclCustomerModel) userService.getUserForUID(retailerId);
				customers.add(retailer);

			}*//*

	    	for(SclCustomerModel cus : customers)
	    	{
	    		customerIds.add(cus.getUid());
	    	}
	    	
			if (Filter.equalsIgnoreCase("MTD")) {
				
				List<List<Object>> sales = new ArrayList<>();
				
				*//* cal = Calendar.getInstance();
				 cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
			     cal.set(Calendar.HOUR, 23);
			     cal.set(Calendar.MINUTE, 59);
			     cal.set(Calendar.SECOND, 59);
			     
			     endDate = cal.getTime();
				int endMonth=cal.get(Calendar.MONTH)+1;
				int endYear=cal.get(Calendar.YEAR);
			     
			     cal.add(Calendar.MONTH, -8);
			     cal.set(Calendar.HOUR, 0);
			     cal.set(Calendar.MINUTE, 0);
			     cal.set(Calendar.SECOND, 0);
			     
			     startDate = cal.getTime();
				 int startMonth=cal.get(Calendar.MONTH)+1;
				int startYear=cal.get(Calendar.YEAR);*//*

				*//*cal = Calendar.getInstance();
				cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
				cal.set(Calendar.HOUR, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);

				endDate = cal.getTime();
				int endYear = cal.get(Calendar.YEAR);
				int endMonth = cal.get(Calendar.MONTH);
				cal.add(Calendar.MONTH, -8);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);

				startDate = cal.getTime();
				int startYear = cal.get(Calendar.YEAR);
				int startMonth = cal.get(Calendar.MONTH);*//*

				cal = Calendar.getInstance();

				// Set the end date to the current date
				endDate = cal.getTime();
				int endYear = cal.get(Calendar.YEAR);
				int endMonth = cal.get(Calendar.MONTH)+1;

				// Set the start date to 8 months before the current date
				cal.add(Calendar.MONTH, -8);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				startDate = cal.getTime();

				// Check if the start date is before the 30th of the month
				int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
				if (dayOfMonth < 30) {
					// Set the start date to the 30th of that month
					cal.set(Calendar.DAY_OF_MONTH, 30);
					startDate = cal.getTime();
				}
				int startYear = cal.get(Calendar.YEAR);
				int startMonth = cal.get(Calendar.MONTH)+1;

			     List<SclCustomerModel> sclDealer=new ArrayList<>();
				 sclDealer.add(customer);
			     if(customerType.equalsIgnoreCase("RETAILER"))
			    	{
						LOG.info("Getting sales List");
						*//*double salesMtd=0.0;
						Map<String, Double> salesQuantityForRetailerByDate = networkService.getSalesQuantityForRetailerByDate(customers, convertDateToString(startDate), convertDateToString(endDate), sclDealer, null);
						if(salesQuantityForRetailerByDate!=null){
							salesMtd=salesQuantityForRetailerByDate.get("quantityInBags");
						}*//*
						sales =  dealerDao.getMonthWiseForRetailerMTD(customers, null, null, startDate, endDate);
						for (List<Object> sale : sales) {
							LOG.info(String.format("Sales %s ::",sale));
						}
			    	}
			     else if(customerType.equalsIgnoreCase("INFLUENCER"))
			    	{
						LOG.info("Getting Influencer sales List");
			    	 	sales =  dealerDao.getMonthWiseForInfluencerMTD(customers, startDate, endDate);
						for (List<Object> sale : sales) {
						LOG.info(String.format("Sales %s ::",sale));
					}
			    	}
			     
			     try {
					 *//*for(int i=0;i<sales.size();i++)
				     {
						 LOG.info("Entering loop");
				    	 MonthlySalesData data = new MonthlySalesData();
						 LOG.info("Setting monthYear");
						 String monthYr = getMonthName((int)sales.get(i).get(1)-1).concat(" ").concat(Integer.toString((int)sales.get(i).get(0)));
				    	 double currentSales = (double) sales.get(i).get(2);
				    	 double previousSales = (double) sales.get(i+1).get(2);
				    	 LOG.info(String.format("CurrentSales :: %s",currentSales));
				    	 data.setMonthYear(monthYr);
				    	 data.setActualSales(currentSales);
						 LOG.info(String.format("Before Growth Percent :"));
				    	 if((int)sales.get(i).get(1)==1)
				    	 {   LOG.info(String.format("After Growth Percent :"));
				    		 if(((int)sales.get(i).get(1) - (int)sales.get(i+1).get(1)) == (-11))
				    		 {
								 data.setGrowth(((currentSales-previousSales)/previousSales)*100);
								 LOG.info(String.format("Set Growth Percent :"));

				    		 }
				    		 else
				    		 {
				    			 data.setGrowth(0.0);
				    		 }
				    	 }
				    	 else
				    	 {     LOG.info(String.format("Before Growth Percentage :"));
				    		 if(((int)sales.get(i).get(1) - (int)sales.get(i+1).get(1)) == 1)
				    		 {    LOG.info(String.format("Before Growth Percentage :"));

								 data.setGrowth(((currentSales-previousSales)/previousSales)*100);
								 LOG.info(String.format("Set Growth Percentage :"));
				    		 }
				    		 else
				    		 {
				    			 data.setGrowth(0.0);
				    		 }
				    	 }*//*
					 int i=0;
					 for(int j=0;j<8;j++)
					 {
						 // int currentMonth = endMonth - j < 1 ? 12 - (j - 1) : endMonth - j;
						 MonthlySalesData data = new MonthlySalesData();
						 double previousSales=0.0;
						 double currentSales=0.0;
						 String monthYr;
						 if(i>=sales.size())
						 {
							 monthYr = getMonthName(endMonth-1).concat(" ");
							 if(startMonth-endMonth>0)
							 {
								 monthYr = monthYr + Integer.toString(endYear);
								 if(endMonth==1){
									 endMonth=12;
								 }
								 else {
									 endMonth--;
								 }
							 }
							 else {
								 monthYr = monthYr +Integer.toString(startYear);
								 endMonth--;
							 }
							 data.setGrowth(0.0);
						 }
						 else if(endMonth!=(int)sales.get(i).get(1)){
							 monthYr = getMonthName(endMonth-1).concat(" ");
							 if(startMonth-endMonth>0)
							 {
								 monthYr = monthYr + Integer.toString(endYear);
								 if(endMonth==1){
									 endMonth=12;
								 }
								 else {
									 endMonth--;
								 }
							 }
							 else {
								 monthYr = monthYr +Integer.toString(startYear);
								 endMonth--;
							 }
							 data.setGrowth(0.0);

						 }
						 else
						 {
							 monthYr = getMonthName((int)sales.get(i).get(1)-1).concat(" ").concat(Integer.toString((int)sales.get(i).get(0)));
							 currentSales = (double) sales.get(i).get(2);
							 if((i+1)<sales.size())
							 {
								 previousSales = (double) sales.get(i+1).get(2);
								 if((int)sales.get(i).get(1)==1)
								 {
									 if(((int)sales.get(i).get(1) - (int)sales.get(i+1).get(1)) == (-11))
									 {
										 data.setGrowth(((currentSales-previousSales)/previousSales)*100);
									 }
									 else
									 {
										 data.setGrowth(0.0);
									 }
								 }
								 else
								 {
									 if(((int)sales.get(i).get(1) - (int)sales.get(i+1).get(1)) == 1)
									 {
										 data.setGrowth(((currentSales-previousSales)/previousSales)*100);
									 }
									 else
									 {
										 data.setGrowth(0.0);
									 }
								 }
							 }
							 else
							 {
								 data.setGrowth(0.0);
							 }
							 i++;
							 if(endMonth==1){
								 endMonth=12;
							 }
							 else {
								 endMonth--;
							 }
						 }
						 data.setMonthYear(monthYr);
						 data.setActualSales(currentSales);
						 data.setTargetSales(0.0);
						 data.setPercentage(0.0);
				    	 dataList.add(data);
				     }
			     }catch(Exception e)
			     {
			    	 LOG.error(e);
			     }
			}
			else
			{
				*//*int currentYr = 0;
				int previousYr = 0;
				
				if(currentDate.before(financialYrEndDate))
				{
					endDate = financialYrEndDate;
					cal.setTime(endDate);
					
					currentYr = cal.get(Calendar.YEAR);
					
					cal = Calendar.getInstance();
					cal.add(Calendar.YEAR, -1);
				    cal.set(Calendar.MONTH, 3);
				    cal.set(Calendar.DATE, 1);
				    cal.set(Calendar.HOUR, 0);
				    cal.set(Calendar.MINUTE, 0);
				    cal.set(Calendar.SECOND, 0);
				    
				    startDate = cal.getTime();
				    
				    previousYr = cal.get(Calendar.YEAR);
				    
				}
				else
				{
					cal.setTime(financialYrEndDate);
					cal.add(Calendar.YEAR, 1);
					endDate = cal.getTime();
					currentYr = cal.get(Calendar.YEAR);
					
					cal.add(Calendar.YEAR, -1);
				    cal.set(Calendar.MONTH, 3);
				    cal.set(Calendar.DATE, 1);
				    cal.set(Calendar.HOUR, 0);
				    cal.set(Calendar.MINUTE, 0);
				    cal.set(Calendar.SECOND, 0);
				    
				    startDate = cal.getTime();
				    
				    previousYr = cal.get(Calendar.YEAR);
					
				}
				
				List<Double> yearWiseSales = new ArrayList<>();*//*
				int currentYr = 0;
				int previousYr = 0;

				if (currentDate.before(financialYrEndDate)) {
					endDate = financialYrEndDate;
					cal.setTime(endDate);

					currentYr = cal.get(Calendar.YEAR);

					cal = Calendar.getInstance();
					cal.add(Calendar.YEAR, -1);
					cal.set(Calendar.MONTH, 3);
					cal.set(Calendar.DATE, 1);
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);

					startDate = cal.getTime();

					previousYr = cal.get(Calendar.YEAR);

				} else {
					cal.setTime(financialYrEndDate);
					cal.add(Calendar.YEAR, 1);
					endDate = cal.getTime();
					currentYr = cal.get(Calendar.YEAR);

					cal.add(Calendar.YEAR, -1);
					cal.set(Calendar.MONTH, 3);
					cal.set(Calendar.DATE, 1);
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);

					startDate = cal.getTime();

					previousYr = cal.get(Calendar.YEAR);
				}

				List<Double> yearWiseSales = new ArrayList<>();
				double currentSales = 0.0;
			    
			    for(int i=0;i<=3;i++)
			    {
					LOG.info(String.format("StartDate ::%s",startDate));
					  if (i == 0) {
						LOG.info(String.format("EndDate ::%s",currentDate));

						if(customerType.equalsIgnoreCase("RETAILER"))
						{
							currentSales =  dealerDao.getMonthWiseForRetailerYTD(customers, null, null, startDate, currentDate);
						}
						else if(customerType.equalsIgnoreCase("INFLUENCER"))
						{
							currentSales =  dealerDao.getMonthWiseForInfluencerYTD(customers, startDate, currentDate);
						}
					} else {
						cal.setTime(endDate);
						cal.add(Calendar.YEAR, -1);
						cal.set(Calendar.MONTH, 2); // Set the month to March
						cal.set(Calendar.DATE, 31); // Set the date to the last day of March
						cal.set(Calendar.HOUR, 23);
						cal.set(Calendar.MINUTE, 59);
						cal.set(Calendar.SECOND, 59);
						endDate = cal.getTime();
						LOG.info(String.format("EndDates ::%s",endDate));


						if(customerType.equalsIgnoreCase("RETAILER"))
						{
							currentSales =  dealerDao.getMonthWiseForRetailerYTD(customers, null, null, startDate, endDate);
						}
						else if(customerType.equalsIgnoreCase("INFLUENCER"))
						{
							currentSales =  dealerDao.getMonthWiseForInfluencerYTD(customers, startDate, endDate);
						}
					}



					cal.setTime(startDate);
					cal.add(Calendar.YEAR, -1);
					startDate = cal.getTime();
			    	
			    	yearWiseSales.add(currentSales);
			    }
			    
			    try {
			    	for(int i=0;i<=2;i++)
				    {
				    	MonthlySalesData data = new MonthlySalesData();
				    	
				    	data.setMonthYear(Filter);
				    	double currentYrSales = yearWiseSales.get(i);
				    	double previousYrSales = yearWiseSales.get(i+1);

						data.setActualSales(currentYrSales);
				    	if(previousYrSales!=0.0)
				    	{
							data.setGrowth(((currentYrSales/previousYrSales)*100));
				    	}
				    	
				    	String year = Integer.toString((previousYr-i)).concat("-").concat(Integer.toString((currentYr-i)));
				    	data.setMonthYear(year);
						data.setTargetSales(0.0);
						data.setPercentage(0.0);
				    	dataList.add(data);
				    }
			    }catch(Exception e) {
			    	LOG.error(e);
			    }
			    
			}
	     }
		
		return dataList;
	}*/

	 private String getMonthName(int month){
	        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
	        return monthNames[month];
	    }
	
	@Override
	public CreditLimitData getHighPriorityActions(String uid) {
		SclCustomerModel dealer = (SclCustomerModel) userService.getUserForUID(uid);
		CreditLimitData creditLimitData = new CreditLimitData();
		
		if (dealer!=null){
			creditLimitData = getCreditLimitForCustomer(dealer);
		}
		return creditLimitData;
	}

	@Override
	public List<CreditLimitData> getHighPriorityActionsForDealer(String uid) {
		SclCustomerModel dealer = (SclCustomerModel) userService.getUserForUID(uid);
		CreditLimitData creditLimitData = new CreditLimitData();
		List<CreditLimitData> lstCreditLimitData = new ArrayList<CreditLimitData>();
		
		if (null != dealer && dealer.getCounterType().equals(CounterType.SP)) {
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterTypes = List.of(DEALER);
            requestData.setCounterType(counterTypes);
            List<SclCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            for (SclCustomerModel sclCustomer : customerforUser) {
            	creditLimitData = getCreditLimitForCustomer(sclCustomer);
            	lstCreditLimitData.add(creditLimitData);
            }
		} else if (dealer!=null){
			creditLimitData = getCreditLimitForCustomer(dealer);
			lstCreditLimitData.add(creditLimitData);
		}
		return lstCreditLimitData;
	}
	
	/**
	 * To get the credit data for customer using customer No
	 * @param sclCustomer	SCLCustomer object passed
	 * @return
	 */
	private CreditLimitData getCreditLimitForCustomer(SclCustomerModel sclCustomer) {
		CreditLimitData creditLimitDataForDealer = new CreditLimitData();
		Boolean isCreditLimitBreached = false;
		Boolean isOutStandingAmountMoreThan30Days = false;
		Date current = new Date();

		String customerNumber = sclCustomer.getCustomerNo();
		double totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(customerNumber);
		Double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNumber);
		List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(customerNumber);
		double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(sclCustomer.getPk().toString());
         
		Double utilizeCredit = totalOutstandingAmount + pendingOrderAmount;
		Double totalCreditLimit = djpVisitDao.getDealerCreditLimit(customerNumber);
		if(utilizeCredit>totalCreditLimit){
			isCreditLimitBreached = true;
		}
		creditLimitDataForDealer.setIsCreditLimitBreached(isCreditLimitBreached);


		if(bucketList!=null && !bucketList.isEmpty()){
			for(List<Double> bucket:bucketList){
				for(int i=5; i<10;i++){
					if(bucket.get(i)!=null && bucket.get(i)!=0){
						isOutStandingAmountMoreThan30Days = true;
						break;
					}
				}
			}
		}
		if (Objects.nonNull(sclCustomer.getPartnerCustomer())) {

			int validityMinusXHours = dataConstraintDao.findDaysByConstraintName("VALIDITY_MINUS_X_HRS");
			int validityMinus24Hours = dataConstraintDao.findDaysByConstraintName("VALIDITY_MINUS_24_HRS");
			String validityMinusXHrsMsg = dataConstraintDao.findVersionByConstraintName("VALIDITY_MSG_MINUS_X_HRS");
			String validityMinus24HrsMsg = dataConstraintDao.findVersionByConstraintName("VALIDITY_MSG_MINUS_24_HRS");

			List<PartnerCustomerModel> partnerCustomers = sclCustomer.getPartnerCustomer().stream().filter(partnerCustomerModel -> (partnerCustomerModel.getActive() && partnerCustomerModel.getValidityExpired().after(current))).sorted(Comparator.comparing(PartnerCustomerModel::getValidityExpired)).toList();
			List<String> messages = new ArrayList<>();
			for (PartnerCustomerModel partnerCustomer : partnerCustomers) {
				StringBuilder builder = new StringBuilder();
				if (Objects.nonNull(partnerCustomer.getValidityExpired())) {
					Date validityExpired =  convertUtcToIst(partnerCustomer.getValidityExpired());
					Date currentTimeInIst = convertUtcToIst(current);
					Date tMinusXHrsDate = subtractHoursFromDate(validityExpired,validityMinusXHours);
					Date tMinus24HrsDate = subtractHoursFromDate(validityExpired,validityMinus24Hours);

					SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
					String formattedDate = formatter.format(partnerCustomer.getValidityExpired());

					//If the current time is within 24 hrs of partner's validity expiry time
					if(currentTimeInIst.after(tMinus24HrsDate) && currentTimeInIst.before(validityExpired)) {
						builder.append(String.format(validityMinus24HrsMsg,partnerCustomer.getName()));//If the current time is within 24 hrs of partner's validity expiry time
					}
					//If the current time is within X hrs and before 24 hrs of partner's validity expiry time
					else if (currentTimeInIst.before(tMinus24HrsDate) && currentTimeInIst.after(tMinusXHrsDate)) {
						builder.append(String.format(validityMinusXHrsMsg,partnerCustomer.getName(),formattedDate));
					}

					if (builder.length() > 0) {
						messages.add(builder.toString());
					}
				}
			}
			List<String> body = new ArrayList<>(new LinkedHashSet<>(messages));
			creditLimitDataForDealer.setValidExpiredMessageToPartnerCustomer(body);
		}
		creditLimitDataForDealer.setIsOutstandingAmountMoreThan30Days(isOutStandingAmountMoreThan30Days);
		return creditLimitDataForDealer;
	}

	/**
	 * Converts date from UTC to IST timezone
	 * @param date
	 * @return
	 */
	public Date convertUtcToIst(Date date) {
		LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();

		//Convert UTC to IST using ZoneId and ZonedDateTime
		ZoneId istZoneId = ZoneId.of("Asia/Kolkata");
		ZonedDateTime istDateTime = localDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(istZoneId);
		return Date.from(istDateTime.toInstant());
	}

	/**
	 * Substracts X hrs from the date and return the new date
	 * @param date
	 * @param hoursToSubtract
	 * @return
	 */
	public Date subtractHoursFromDate(Date date, int hoursToSubtract) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, -hoursToSubtract);
		return calendar.getTime();
	}

	@Override
	public int getStockAvailForRetailer(int receipt, int saleToRetailer, int saleToInfluencer) {
		//F3-G3-H3
		int stockRetailer=0;
		stockRetailer=receipt-saleToRetailer-saleToInfluencer;
		return stockRetailer;
	}

	@Override
	public int getStockAvailForInfluencer(int receipt, int saleToRetailer, int saleToInfluencer) {
		//(0.7*(F3-G3))-H3
		int stockInfluencer=0;
		stockInfluencer = (int) ((0.7 * (receipt - saleToRetailer)) - saleToInfluencer);
		return stockInfluencer;
	}
	
	@Override
	public SCLDealerSalesAllocationData	getStockAllocationForDealer(String productCode) {
		SCLDealerSalesAllocationData dealerAllocationData = new SCLDealerSalesAllocationData();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		SclCustomerModel dealerModel = (SclCustomerModel) currentUser;
		//Check for Product code is null then always return the total values of stocks for dealer available for all products
		if (dealerModel != null) {
			if (productCode == null) {
				List<List<Integer>> stockAvailList = dealerDao.getDealerTotalAllocation(dealerModel);
				//Always it should return one row with total stocks available
				List<Integer> totalStocksAvail = (stockAvailList != null)? stockAvailList.get(0): new ArrayList<Integer>();
				if (null != totalStocksAvail && !totalStocksAvail.isEmpty()) {
					dealerAllocationData.setStockAvailableForInfluencer(totalStocksAvail.get(0));
					dealerAllocationData.setStockAvailableForRetailer(totalStocksAvail.get(1));
				}
			} else {
				//When request for each product code and each dealer received
				ProductModel productModel = productService.getProductForCode(productCode);
				if (null != productModel) {
					ReceiptAllocaltionModel receiptAllocation = dealerDao.getDealerAllocation(productModel, dealerModel);
					if (receiptAllocation != null) {
						dealerAllocationData.setProductCode(productCode);
						dealerAllocationData.setStockAvailableForInfluencer(receiptAllocation.getStockAvlForInfluencer());
						dealerAllocationData.setStockAvailableForRetailer(receiptAllocation.getStockAvlForRetailer());
					}
				}
			}
		}
		
		return dealerAllocationData;
	}
	
	@Override
	public SCLDealerSalesAllocationData getStockAllocationForRetailer(String productCode) {
		SCLDealerSalesAllocationData dealerAllocationData = new SCLDealerSalesAllocationData();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		SclCustomerModel dealerModel = (SclCustomerModel) currentUser;
		// Check for Product code is null then always return the total values of stocks
		// for dealer available for all products
		if (dealerModel != null) {
			if (productCode == null) {
				List<List<Integer>> stockAvailList = dealerDao.getRetailerTotalAllocation(dealerModel);
				// Always it should return one row with total stocks available
				List<Integer> totalStocksAvail = (stockAvailList != null) ? stockAvailList.get(0)
						: new ArrayList<Integer>();
				if (null != totalStocksAvail && !totalStocksAvail.isEmpty()) {
					dealerAllocationData.setStockAvailableForInfluencer(totalStocksAvail.get(0));
				}
			} else {
				// When request for each product code and each dealer received
				ProductModel productModel = productService.getProductForCode(productCode);
				if (null != productModel) {
					RetailerRecAllocateModel retailerReceiptAllocation = 
							dealerDao.getRetailerAllocation(productModel, dealerModel);
					if (retailerReceiptAllocation != null) {
						dealerAllocationData.setProductCode(productCode);
						dealerAllocationData.setStockAvailableForInfluencer(retailerReceiptAllocation.getStockAvlForInfluencer());
					}
				}
			}
		}

		return dealerAllocationData;
	}
	public static String convertDateToString(Date date)
	{	String pattern = "MM/dd/yyyy HH:mm:ss";
		DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}

	@Override
	public PartnerCustomerData saveExtendedPartnerInfo(PartnerCustomerData partnerCustomerData) {
		SclCustomerModel dealer = (SclCustomerModel) userService.getCurrentUser();
		String successMessage = dataConstraintDao.findVersionByConstraintName("SAVE_PARTNER_SUCCESS_MESSAGE");
		String errorMessage = dataConstraintDao.findVersionByConstraintName("SAVE_PARTNER_ERROR_MESSAGE");
		PartnerCustomerModel partnerCustomerModel = modelService.create(PartnerCustomerModel.class);
		partnerCustomerModel.setId(partnerCustomerIdGenerator.generate().toString());
		partnerCustomerData.setId(partnerCustomerModel.getId());
		try {
			partnerCustomerModel.setSclcustomer(dealer);
			partnerCustomerReversePopulator.populate(partnerCustomerData, partnerCustomerModel);
			if (StringUtils.isNotEmpty(partnerCustomerModel.getId()) && partnerCustomerModel.getActive() != null && StringUtils.isNotEmpty(partnerCustomerModel.getMobileNumber())) {
				String mobileNumber = partnerCustomerModel.getMobileNumber();
				String dealerMobileNumber = dealer.getMobileNumber();
				if (BooleanUtils.isFalse(dealerDao.isPartnerActiveMobilePresent(partnerCustomerModel.getActive(), mobileNumber, partnerCustomerModel.getId(), dealer)) && BooleanUtils.isFalse(mobileNumber.equalsIgnoreCase(dealerMobileNumber))) {
					modelService.save(partnerCustomerModel);
					modelService.refresh(partnerCustomerModel);
					partnerCustomerData.setValidityExpired(partnerCustomerModel.getValidityExpired());
					partnerCustomerData.setSuccessMessage(String.format(successMessage, partnerCustomerData.getName(), dealer.getUid()));
					partnerCustomerData.setIsSuccessful(Boolean.TRUE);
				}
				else {
					String mobileNoExistsMsg = dataConstraintDao.findVersionByConstraintName("PARTNER_MOBILE_EXISTS_MSG");
					LOG.warn(String.format("Validation failed for Partner Customer ID::%s, mobilenumber::%s. Dealer Name::%s, Dealer UID::%s.",
							partnerCustomerData.getId(), partnerCustomerData.getMobileNumber(), dealer.getName(), dealer.getUid()));
					partnerCustomerData.setErrorMessage(String.format(mobileNoExistsMsg, partnerCustomerData.getMobileNumber()));
					partnerCustomerData.setIsSuccessful(Boolean.FALSE);
				}
			}
		}
		catch (Exception ex){
			LOG.error(String.format("Exception got in saveExtendedPartnerInfo => Partner Customer ID::%s and name::%s, Dealer Name::%s and Dealer UID::%s with errorMessage::%s and cause::%s and stack trace::%s",partnerCustomerData.getId(),partnerCustomerData.getName(),dealer.getName(),dealer.getUid(),ex.getMessage(),ex.getCause(), Arrays.toString(ex.getStackTrace())));
			partnerCustomerData.setErrorMessage(String.format(errorMessage,partnerCustomerData.getName(),ex.getMessage()));
			partnerCustomerData.setIsSuccessful(Boolean.FALSE);
		}
		return partnerCustomerData;
	}

	@Override
	public PartnerCustomerData editPartnerCustomerInfo(PartnerCustomerData partnerCustomerData, SclCustomerModel dealer, String operationType) {
		return handlePartnerCustomerUpdate(partnerCustomerData, dealer,
				"EDIT_PARTNER_SUCCESS_MESSAGE", "EDIT_PARTNER_ERROR_MESSAGE",
				(partnerCustomerModel) -> {
					partnerCustomerModel.setMobileNumber(partnerCustomerData.getMobileNumber());
					partnerCustomerModel.setValidity(partnerCustomerData.getValidityInMonths());

					Date validityExpiredDate = getValidityExpiredDate(partnerCustomerData.getValidityInMonths(), LocalDate.now());
					partnerCustomerModel.setValidityExpired(validityExpiredDate);
					partnerCustomerData.setValidityExpired(partnerCustomerModel.getValidityExpired());
				},
				partnerCustomerData.getMobileNumber(),operationType);
	}

	@Override
	public PartnerCustomerData deletePartnerCustomer(PartnerCustomerData partnerCustomerData, SclCustomerModel dealer, String operationType) {
		return handlePartnerCustomerUpdate(partnerCustomerData, dealer,
				"DELETE_PARTNER_SUCCESS_MESSAGE", "DELETE_PARTNER_ERROR_MESSAGE",
				(partnerCustomerModel) -> {
					partnerCustomerModel.setActive(Boolean.FALSE);
					partnerCustomerModel.setInactiveDate(new Date());
					partnerCustomerModel.setIsDeactivatedByDealer(Boolean.TRUE);
				},
				null, operationType);
	}

	/**
	 * Update(delete/edit) Partner Customer data
	 * @param partnerCustomerData
	 * @param dealer
	 * @param successMessageKey
	 * @param errorMessageKey
	 * @param updateFunction
	 * @param mobileNumber
	 * @param operationType
	 * @return
	 */
	private PartnerCustomerData handlePartnerCustomerUpdate(PartnerCustomerData partnerCustomerData, SclCustomerModel dealer,
															String successMessageKey, String errorMessageKey, Consumer<PartnerCustomerModel> updateFunction,
															String mobileNumber, String operationType) {

		String successMessage = dataConstraintDao.findVersionByConstraintName(successMessageKey);
		String errorMessage = dataConstraintDao.findVersionByConstraintName(errorMessageKey);

		PartnerCustomerModel partnerCustomerModel = dealerDao.getPartnerCustomerById(partnerCustomerData.getId());
		try {
			if (Objects.nonNull(partnerCustomerModel)) {
				if(StringUtils.isNotBlank(operationType) && operationType.equalsIgnoreCase("edit") && StringUtils.isNotBlank(mobileNumber)) {
					boolean isPartnerActiveMobilePresent = dealerDao.isPartnerActiveMobilePresent(partnerCustomerModel.getActive(), mobileNumber, partnerCustomerModel.getId(), dealer);
					boolean dealerMobileNumerMatch = mobileNumber.equalsIgnoreCase(dealer.getMobileNumber());
					if (isPartnerActiveMobilePresent || dealerMobileNumerMatch) {
						String mobileNoExistsMsg = dataConstraintDao.findVersionByConstraintName("PARTNER_MOBILE_EXISTS_MSG");
						partnerCustomerData.setErrorMessage(String.format(mobileNoExistsMsg, mobileNumber));
						partnerCustomerData.setIsSuccessful(Boolean.FALSE);
						return partnerCustomerData;
					}
				}

				updateFunction.accept(partnerCustomerModel);
				modelService.save(partnerCustomerModel);
				modelService.refresh(partnerCustomerModel);

				partnerCustomerData.setSuccessMessage(String.format(successMessage));
				partnerCustomerData.setIsSuccessful(Boolean.TRUE);

			} else {
				partnerCustomerData.setErrorMessage(String.format("There is no partner customer with the given ID:%s", partnerCustomerData.getId()));
				partnerCustomerData.setIsSuccessful(Boolean.FALSE);
			}
		} catch (Exception ex) {
			LOG.error(String.format("Exception got in Update Partner Customer Info => Partner Customer ID::%s and name::%s, Dealer Name::%s and Dealer UID::%s with errorMessage::%s and cause::%s and stack trace::%s", partnerCustomerData.getId(), partnerCustomerData.getName(), dealer.getName(), dealer.getUid(), ex.getMessage(), ex.getCause(), Arrays.toString(ex.getStackTrace())));
			partnerCustomerData.setErrorMessage(String.format(errorMessage, partnerCustomerData.getId(), ex.getMessage()));
			partnerCustomerData.setIsSuccessful(Boolean.FALSE);
		}
		return partnerCustomerData;
	}

	@Override
	public Date getValidityExpiredDate(Integer validityInMonths, LocalDate currentDate) {
		int partnerMonthDaysMultiplier = dataConstraintDao.findDaysByConstraintName("PARTNER_MONTH_DAYS_MULTIPLIER");
		int validityInDays = validityInMonths * partnerMonthDaysMultiplier;

		// Calculate the new date by adding validity days to the current date
		LocalDate newDate = currentDate.plusDays(validityInDays);

		//Adding Timestamp 23:59:59 to the newDate
		LocalTime endOfDay = LocalTime.of(23, 59, 59);
		LocalDateTime newDateTime = LocalDateTime.of(newDate, endOfDay);

		//Convert to ZonedDateTime with the IST time zone
		ZoneId istZoneId = ZoneId.of("Asia/Kolkata"); // IST time zone
		ZonedDateTime zonedDateTime = ZonedDateTime.of(newDateTime, istZoneId);

		return Date.from(zonedDateTime.toInstant());
	}

}
