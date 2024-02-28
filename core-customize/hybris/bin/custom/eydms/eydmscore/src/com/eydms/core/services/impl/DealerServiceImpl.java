package com.eydms.core.services.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.eydms.core.dao.CollectionDao;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.facades.CreditLimitData;
import de.hybris.platform.core.model.user.UserModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DealerDao;
import com.eydms.core.model.ReceiptAllocaltionModel;
import com.eydms.core.model.RetailerRecAllocateModel;
import com.eydms.core.enums.CounterType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import com.eydms.core.services.DealerService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.MonthlySalesData;
import com.eydms.facades.data.RequestCustomerData;
import com.eydms.facades.data.EYDMSDealerSalesAllocationData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;

public class DealerServiceImpl implements DealerService{

	private static final Logger LOG = Logger.getLogger(DealerServiceImpl.class);
	private static final String DEALER = "DEALER";
	
	@Autowired
	UserService userService;
	
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
	
	@Override
	public List<MonthlySalesData> getLastSixMonthSalesForDealer(String customerId, String Filter, String customerType,String retailerId) {
		
		 List<MonthlySalesData> dataList = new ArrayList<>();
	     
	     EyDmsCustomerModel customer = (EyDmsCustomerModel) userService.getUserForUID(customerId);
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
		    
	    	List<EyDmsCustomerModel> customers = new ArrayList<>();
	    	List<String> customerIds = new ArrayList<>();

	    	/*if(retailerId==null) {
				if (ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
					if (customerType.equalsIgnoreCase("RETAILER")) {
						customers = territoryManagementService.getRetailerListForDealer();
					} else if (customerType.equalsIgnoreCase("INFLUENCER")) {
						customers = territoryManagementService.getInfluencerListForDealer();
					}
				} else if (ugSet.contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {

					if (customerType.equalsIgnoreCase("INFLUENCER")) {
						customers = territoryManagementService.getInfluencerListForRetailer();
					}
				}
			}*/
			 if(retailerId!=null) {

				EyDmsCustomerModel retailer = (EyDmsCustomerModel) userService.getUserForUID(retailerId);
				customers.add(retailer);

			}

	    	for(EyDmsCustomerModel cus : customers)
	    	{
	    		customerIds.add(cus.getUid());
	    	}
	    	
			if (Filter.equalsIgnoreCase("MTD")) {
				
				List<List<Object>> sales = new ArrayList<>();
				
				/* cal = Calendar.getInstance();
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
				int startYear=cal.get(Calendar.YEAR);*/

				/*cal = Calendar.getInstance();
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
				int startMonth = cal.get(Calendar.MONTH);*/

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

			     
			     if(customerType.equalsIgnoreCase("RETAILER"))
			    	{
						LOG.info("Getting sales List");
			    	 	sales =  dealerDao.getMonthWiseForRetailerMTD(customers, startDate, endDate);
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
					 /*for(int i=0;i<sales.size();i++)
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
				    	 }*/
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
				    	 dataList.add(data);
				     }
			     }catch(Exception e)
			     {
			    	 LOG.error(e);
			     }
			}
			else
			{
				/*int currentYr = 0;
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
				
				List<Double> yearWiseSales = new ArrayList<>();*/
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
							currentSales =  dealerDao.getMonthWiseForRetailerYTD(customers, startDate, currentDate);
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
							currentSales =  dealerDao.getMonthWiseForRetailerYTD(customers, startDate, endDate);
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
				    	
				    	dataList.add(data);
				    }
			    }catch(Exception e) {
			    	LOG.error(e);
			    }
			    
			}
	     }
		
		return dataList;
	}

	 private String getMonthName(int month){
	        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
	        return monthNames[month];
	    }
	
	@Override
	public CreditLimitData getHighPriorityActions(String uid) {
		EyDmsCustomerModel dealer = (EyDmsCustomerModel) userService.getUserForUID(uid);
		CreditLimitData creditLimitData = new CreditLimitData();
		
		if (dealer!=null){
			creditLimitData = getCreditLimitForCustomer(dealer);
		}
		return creditLimitData;
	}

	@Override
	public List<CreditLimitData> getHighPriorityActionsForDealer(String uid) {
		EyDmsCustomerModel dealer = (EyDmsCustomerModel) userService.getUserForUID(uid);
		CreditLimitData creditLimitData = new CreditLimitData();
		List<CreditLimitData> lstCreditLimitData = new ArrayList<CreditLimitData>();
		
		if (null != dealer && dealer.getCounterType().equals(CounterType.SP)) {
            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterTypes = List.of(DEALER);
            requestData.setCounterType(counterTypes);
            List<EyDmsCustomerModel> customerforUser = territoryManagementService.getCustomerforUser(requestData);
            for (EyDmsCustomerModel eydmsCustomer : customerforUser) {
            	creditLimitData = getCreditLimitForCustomer(eydmsCustomer);
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
	 * @param eydmsCustomer	EYDMSCustomer object passed
	 * @return
	 */
	private CreditLimitData getCreditLimitForCustomer(EyDmsCustomerModel eydmsCustomer) {
		CreditLimitData creditLimitDataForDealer = new CreditLimitData();
		Boolean isCreditLimitBreached = false;
		Boolean isOutStandingAmountMoreThan30Days = false;
		String customerNumber = eydmsCustomer.getCustomerNo();
		double totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(customerNumber);
		Double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNumber);
		List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(customerNumber);
		double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(eydmsCustomer.getPk().toString());
         
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
		creditLimitDataForDealer.setIsOutstandingAmountMoreThan30Days(isOutStandingAmountMoreThan30Days);
		return creditLimitDataForDealer;
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
	public EYDMSDealerSalesAllocationData	getStockAllocationForDealer(String productCode) {
		EYDMSDealerSalesAllocationData dealerAllocationData = new EYDMSDealerSalesAllocationData();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		EyDmsCustomerModel dealerModel = (EyDmsCustomerModel) currentUser;
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
	public EYDMSDealerSalesAllocationData getStockAllocationForRetailer(String productCode) {
		EYDMSDealerSalesAllocationData dealerAllocationData = new EYDMSDealerSalesAllocationData();
		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
		EyDmsCustomerModel dealerModel = (EyDmsCustomerModel) currentUser;
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
}
