package com.scl.facades.impl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.annotation.Resource;

import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.model.*;
import com.scl.core.services.AmountFormatService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.impl.CollectionServiceImpl;
import com.scl.core.services.impl.SlctCrmIntegrationServiceImpl;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.scl.core.services.CollectionService;
import com.scl.facades.CollectionFacade;
import com.scl.facades.data.*;
import com.scl.facades.util.GenericMediaUtil;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

public class CollectionFacadeImpl implements CollectionFacade{

	private static final Logger LOG = Logger.getLogger(CollectionFacadeImpl.class);
	@Resource
	CollectionService collectionService;
	
	@Resource
	Converter<LedgerDetailsModel,CollectionLedgerData> ledgerConverter;
	
	@Resource
	ModelService modelService;

	@Resource
	AmountFormatService amountFormatService;
	
	@Resource
    private Converter<MultipartFile, MediaModel> sclMediaReverseConverter;
	
	@Resource
	private GenericMediaUtil genericMediaUtil;
	
	@Resource
	UserService userService;

	@Autowired
	SlctCrmIntegrationServiceImpl slctCrmIntegrationService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Override
	public CollectionOutstandingData getOutstandingData() {
		return collectionService.getOutstandingData();
	}

	@Override
	public CollectionDealerOutstandingDetailsListData getDealerOutstandingDetails() {
		return collectionService.getDealerOutstandingDetails();
	}

	@Override
	public TopDealerOutstandingListData topDealerOutstanding() {
		return collectionService.topDealerOutstanding();
	}

	@Override
	public CollectionDealerDetailsData getDealerDetails(String dealerCode) {
		return collectionService.getDealerDetails(dealerCode)	;
	}

	@Override
	public CollectionCreditDetailsListData getDealerCreditDetails(String dealerCode) {
		return collectionService.getDealerCreditDetails(dealerCode);
	}

	@Override
	public CollectionLedgerListData getLedgerDetails(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate, String endDate) {
		CollectionLedgerListData listData =  new CollectionLedgerListData();
		
		List<CollectionLedgerData> list = ledgerConverter.convertAll(collectionService.getLedgerDetails(dealerCode, isDebit, isCredit, startDate, endDate));
		
		listData.setCollectionLedgerData(list);
		
		return listData;
	}

	@Override
	public CollectionCDDetailsListData getCashDiscountDetails(Boolean isMTD, Boolean isYTD) {
		return collectionService.getCashDiscountDetails(isMTD, isYTD);
	}

	@Override
	public void sendLedgerReportMail(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate,
			String endDate) {
		
	}

	@Override
	public CollectionOutstandingData getOutstandingDataForSP() {
		return collectionService.getOutstandingDataForSP();
	}


	@Override
	public Boolean uploadSPInvoice(SPInvoiceData data) {
		
		SPInvoiceModel invoice = modelService.create(SPInvoiceModel.class);
		
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		
		invoice.setSalesPromoter((SclCustomerModel) userService.getCurrentUser());
		if(data.getFromDate()!=null)
		{
			try {
				invoice.setFromDate(format.parse(data.getFromDate()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		if(data.getToDate()!=null)
		{
			try {
				invoice.setToDate(format.parse(data.getFromDate()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		try {
			invoice.setInvoiceRaisedDate(format.parse(data.getRaisedOnDate()));

		} catch (ParseException e) {
			e.printStackTrace();
		}

		invoice.setAmount(data.getClaimAmount());
		if(data.getInvoiceFile()!=null)
		{
			try {
				invoice.setInvoiceFile(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(data.getInvoiceFile().getByteStream(),data.getInvoiceFile().getFileName())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		modelService.save(invoice);
			
		return Boolean.TRUE;
	}

	@Override
	public CollectionDealerOutstandingDetailsListData getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData,List<String> so,List<String> tsm) {

		CollectionDealerOutstandingDetailsListData listData = new CollectionDealerOutstandingDetailsListData();
		List<CollectionDealerOutstandingDetailsData> collectionDealerOutstandingDetailsDataList = new ArrayList<>();
		//final SearchPageData<CollectionDealerOutstandingDetailsData> result = new SearchPageData<>();
		List<SclUserModel> soList = new ArrayList<>();
		List<SclUserModel> tsmList = new ArrayList<>();
		if(so!=null && !so.isEmpty()){
			for(String str : so){

				LOG.info("so: " + str);
				SclUserModel user = (SclUserModel) userService.getUserForUID(str);
				soList.add(user);
			}
		}

		if(tsm!=null && !tsm.isEmpty()){
			for(String str1: tsm){
				LOG.info("tsm: " + tsm);
				SclUserModel user1 = (SclUserModel) userService.getUserForUID(str1);
				tsmList.add(user1);
			}
		}


		SearchPageData<CreditAndOutstandingModel> searchResult = collectionService.getDealerOutstandingDetailsForTSMRH(searchPageData,soList,tsmList);

		if(searchResult!=null && searchResult.getResults()!=null) {
			List<CreditAndOutstandingModel> list = searchResult.getResults();
			double outstandingDays = 0.0;

			for (CreditAndOutstandingModel creditAndOutstandingModel : list) {
				CollectionDealerOutstandingDetailsData collectionDealerOutstandingDetailsData = new CollectionDealerOutstandingDetailsData();
				SclCustomerModel sclCustomer = slctCrmIntegrationService.findCustomerByCustomerNo(creditAndOutstandingModel.getCustomerCode());
				SclUserModel sclUser = territoryManagementService.getSOforCustomer(sclCustomer);
				if (sclUser != null) {
					collectionDealerOutstandingDetailsData.setSoName(sclUser.getName());
					collectionDealerOutstandingDetailsData.setSoEmployeeCode(sclUser.getEmployeeCode());
				}
					collectionDealerOutstandingDetailsData.setCounterName(sclCustomer.getName());
				collectionDealerOutstandingDetailsData.setTotalOutstanding(String.valueOf(creditAndOutstandingModel.getTotalOutstanding()!=null?creditAndOutstandingModel.getTotalOutstanding():0.0));
				collectionDealerOutstandingDetailsData.setSecurityDeposit(String.valueOf(creditAndOutstandingModel.getSecurityDeposit()!=null?creditAndOutstandingModel.getSecurityDeposit():0.0));
				collectionDealerOutstandingDetailsData.setBucket1(String.valueOf(creditAndOutstandingModel.getBucket1()!=null?creditAndOutstandingModel.getBucket1():0.0));
				collectionDealerOutstandingDetailsData.setBucket2(String.valueOf(creditAndOutstandingModel.getBucket2()!=null?creditAndOutstandingModel.getBucket2():0.0));
				collectionDealerOutstandingDetailsData.setBucket3(String.valueOf(creditAndOutstandingModel.getBucket3()!=null?creditAndOutstandingModel.getBucket3():0.0));
				collectionDealerOutstandingDetailsData.setBucket4(String.valueOf(creditAndOutstandingModel.getBucket4()!=null?creditAndOutstandingModel.getBucket4():0.0));
				collectionDealerOutstandingDetailsData.setBucket5(String.valueOf(creditAndOutstandingModel.getBucket5()!=null?creditAndOutstandingModel.getBucket5():0.0));
				collectionDealerOutstandingDetailsData.setBucket6(String.valueOf(creditAndOutstandingModel.getBucket6()!=null?creditAndOutstandingModel.getBucket6():0.0));
				collectionDealerOutstandingDetailsData.setBucket7(String.valueOf(creditAndOutstandingModel.getBucket7()!=null?creditAndOutstandingModel.getBucket7():0.0));
				collectionDealerOutstandingDetailsData.setBucket8(String.valueOf(creditAndOutstandingModel.getBucket8()!=null?creditAndOutstandingModel.getBucket8():0.0));
				collectionDealerOutstandingDetailsData.setBucket9(String.valueOf(creditAndOutstandingModel.getBucket9()!=null?creditAndOutstandingModel.getBucket9():0.0));
				collectionDealerOutstandingDetailsData.setBucket10(String.valueOf(creditAndOutstandingModel.getBucket10()!=null?creditAndOutstandingModel.getBucket10():0.0));
					collectionDealerOutstandingDetailsData.setErpCode(sclCustomer.getCustomerNo());
					collectionDealerOutstandingDetailsData.setCounterCode(sclCustomer.getUid());
					if(creditAndOutstandingModel.getTotalOutstanding() !=null && creditAndOutstandingModel.getDailyAverageSales()!=null ){
						if(creditAndOutstandingModel.getDailyAverageSales()!=0.0){
							outstandingDays = creditAndOutstandingModel.getTotalOutstanding()/creditAndOutstandingModel.getDailyAverageSales();
						}
						collectionDealerOutstandingDetailsData.setDaysSaleOutstanding(outstandingDays);
					}
//					collectionDealerOutstandingDetailsData.setTsmName("TSM 1");
//					collectionDealerOutstandingDetailsData.setTsmEmployeeCode("TSM1");
				collectionDealerOutstandingDetailsDataList.add(collectionDealerOutstandingDetailsData);
			}

		}
		/*result.setResults(collectionDealerOutstandingDetailsDataList);
		return result;
		*/
		listData.setCollectionDealerOutstandingDetailsData(collectionDealerOutstandingDetailsDataList);
		listData.setTotalCount((int) searchResult.getPagination().getTotalNumberOfResults());
		return listData;
  }
  
  @Override
	public SearchPageData<SPInvoiceData> getSPInvoiceList(SearchPageData searchPageData, String startDate,
			String endDate, String userId, String sortKey, String sort) {
		
		SearchPageData<SPInvoiceModel> searchResult = collectionService.getSPInvoiceList(searchPageData,startDate,endDate,userId,sortKey,sort);
		
		List<SPInvoiceModel> result = searchResult.getResults();
		
		List<SPInvoiceData> dataList = new ArrayList<>();
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		for(SPInvoiceModel model : result)
		{
			SPInvoiceData data = new SPInvoiceData();
			if(model.getInvoiceRaisedDate()!=null)
			{
				data.setRaisedOnDate(dateFormat.format(model.getInvoiceRaisedDate()));

				if(model.getStatus()!=null){
					data.setStatus(model.getStatus().getCode());
				}
				else{
					data.setStatus("PENDING");
				}
				data.setClaimAmount(model.getAmount());
				
			}
			
			dataList.add(data);
		}
		
		SearchPageData<SPInvoiceData> dataResult = new SearchPageData<>();
		
		dataResult.setPagination(searchResult.getPagination());
		dataResult.setSorts(searchResult.getSorts());
		dataResult.setResults(dataList);
		
		return dataResult;

	}

	@Override
	public CollectionOutstandingData getOutstandingDataForTSMRH() {
		CollectionOutstandingData data = new CollectionOutstandingData();
		double outstandingDays = 0.0;
		double dailyAverageSales = 0.0;
		double totalOutstanding = 0.0;

		List<List<Object>> result =  collectionService.getOutstandingDataForTSMRH();

		if(result!=null && !result.isEmpty()) {
			 totalOutstanding = result.get(0).get(0) != null ? (double) result.get(0).get(0) : 0.0;
			 dailyAverageSales = result.get(0).get(1)!=null? (double) result.get(0).get(1) :0.0;
				if(dailyAverageSales!=0.0){
					outstandingDays = totalOutstanding / dailyAverageSales;
				}
				data.setDaysSaleOutstanding(outstandingDays);
			}

		List<List<Double>> bucketList = new ArrayList<>();
		double bucketsTotal;

		bucketList = territoryManagementDao.getBucketListForTSMRH();
		bucketsTotal = bucketList.get(0).stream().filter(b->b!=null).mapToDouble(b->b.doubleValue()).sum();
		data.setBucketsTotal(amountFormatService.getFormattedValue(bucketsTotal));

		if(bucketList!=null && !bucketList.isEmpty()){
			data.setBucket1(bucketList.get(0).get(0)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(0)):"0");
			data.setBucket2(bucketList.get(0).get(1)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(1)):"0");
			data.setBucket3(bucketList.get(0).get(2)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(2)):"0");
			data.setBucket4(bucketList.get(0).get(3)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(3)):"0");
			data.setBucket5(bucketList.get(0).get(4)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(4)):"0");
			data.setBucket6(bucketList.get(0).get(5)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(5)):"0");
			data.setBucket7(bucketList.get(0).get(6)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(6)):"0");
			data.setBucket8(bucketList.get(0).get(7)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(7)):"0");
			data.setBucket9(bucketList.get(0).get(8)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(8)):"0");
			data.setBucket10(bucketList.get(0).get(9)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(9)):"0");

		}
		data.setTotalOutstanding(amountFormatService.getFormattedValue(totalOutstanding));
		return data;
	}

}
