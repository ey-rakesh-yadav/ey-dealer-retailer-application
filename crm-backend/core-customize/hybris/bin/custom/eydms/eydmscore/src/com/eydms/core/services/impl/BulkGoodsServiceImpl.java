package com.eydms.core.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.stylesheets.MediaList;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.BulkGoodsDao;
import com.eydms.core.model.PurchaseOrderBatchEntryModel;
import com.eydms.core.model.PurchaseOrderBatchModel;
import com.eydms.core.model.PurchaseOrderEntryModel;
import com.eydms.core.model.PurchaseOrderModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.BulkGoodsService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.InventoryStockData;
import com.eydms.facades.data.InventoryStockListData;
import com.eydms.facades.data.PurchaseOrderBatchData;
import com.eydms.facades.data.PurchaseOrderBatchEntryData;
import com.eydms.facades.data.PurchaseOrderBatchEntryListData;
import com.eydms.facades.data.PurchaseOrderData;
import com.eydms.facades.data.PurchaseOrderEntryData;

import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.basecommerce.enums.StockLevelUpdateType;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.sap.sapmodel.enums.ConsignmentEntryStatus;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.stock.model.StockLevelHistoryEntryModel;

public class BulkGoodsServiceImpl implements BulkGoodsService {

	private static final Logger LOG = Logger.getLogger(CollectionServiceImpl.class);
	
	@Autowired
	UserService userService;
	
	@Autowired
	BulkGoodsDao bulkGoodsDao;
	
	@Autowired
	ModelService modelService;
	
	@Autowired
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Autowired
	MediaService mediaService;
	
	@Autowired
    KeyGenerator grnNoGenerator;
	
	@Autowired
    TerritoryManagementService territoryManagementService;
	
	@Override
	public List<String> getListOfPurchaseOrderList(String userId) {
		
		List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
		
		return bulkGoodsDao.getListOfPurchaseOrderList(subAreas);
	}

	@Override
	public PurchaseOrderModel getPurchaseOrderForOrderNo(String orderNo) {
		
		return bulkGoodsDao.getPurchaseOrderDetails(orderNo);
	}

	@Override
	public String saveBatch(String userId, PurchaseOrderData orderData, String batchCode) {
		
		PurchaseOrderModel order = getPurchaseOrderForOrderNo(orderData.getPurchaseOrderNo());
		
		PurchaseOrderBatchModel batch = bulkGoodsDao.getPurchaseOrderBatchForCode(batchCode);
		
		List<PurchaseOrderBatchEntryModel> entries = new ArrayList<>();
		
		List<PurchaseOrderEntryData> entriesData = orderData.getEntries().getPurchaseOrderEntries();
		
		int entryNo=0;
		
		for(PurchaseOrderEntryData entryData : entriesData )
		{
			PurchaseOrderBatchEntryModel batchEntry = modelService.create(PurchaseOrderBatchEntryModel.class);
			batchEntry.setBatch(batch);
			batchEntry.setBatchEntryNo(entryNo);
			batchEntry.setConsignment(batch);
			batchEntry.setOrderEntry(order.getPurchaseOrderEntries().stream().collect(Collectors.toList()).get(entryData.getEntryNo()));
			batchEntry.setQuantity(Long.valueOf(entryData.getQtyReceived()));
			batchEntry.setStatus(ConsignmentEntryStatus.SHIPPED);
			Date dateOfReceiving = null;
			try {
				dateOfReceiving = new SimpleDateFormat("dd/MM/yyyy").parse(entryData.getDateOfReceiving());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			batchEntry.setDateOfReceiving(dateOfReceiving);
			
			entries.add(batchEntry);
			
			++entryNo;
		}
		
		batch.setBatchEntries(entries);
		
		modelService.save(batch);
		
		return batch.getCode();
	}

	@Override
	public List<PurchaseOrderBatchData> getListOfTicketsWithoutGRN(String userId) {
		
		List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
		
		List<PurchaseOrderBatchModel> modelList = bulkGoodsDao.getPurchaseOrderBatchWithoutGRNNoListForUser(subAreas);
		
		List<PurchaseOrderBatchData> dataList = new ArrayList<>();
		
		for(PurchaseOrderBatchModel model : modelList)
		{
			PurchaseOrderBatchData data = new PurchaseOrderBatchData();
			data.setTicketNo(model.getCode());
			
			PurchaseOrderModel po = model.getPurchaseOrder();
			
			if(po!=null) 
			{
				data.setPurchaseOrderNo(po.getCode());
				data.setVendorName(po.getVendorName());
				data.setItemType(po.getIncentiveType()!=null ? po.getIncentiveType().getCode() : null);
			}
			
			Date dateOfReceiving =  model.getShippingDate();
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");  
			
		    if(dateOfReceiving!=null)
		    {
		    	data.setDateOfReceiving(formatter.format(dateOfReceiving));
		    }
			
			dataList.add(data);
		}
		
		return dataList;
	}

	@Override
	public List<PurchaseOrderBatchData> getListOfTicketsWithGRN(String userId) {
		
		List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
		
		List<PurchaseOrderBatchModel> modelList = bulkGoodsDao.getPurchaseOrderBatchWithGRNNoListForUser(subAreas);
		
		List<PurchaseOrderBatchData> dataList = new ArrayList<>();
		
		for(PurchaseOrderBatchModel model : modelList)
		{
			PurchaseOrderBatchData data = new PurchaseOrderBatchData();
			data.setTicketNo(model.getCode());
			data.setGrnNo(model.getGrnNo());
			
			PurchaseOrderModel po = model.getPurchaseOrder();
			
			if(po!=null) 
			{
				data.setPurchaseOrderNo(po.getCode());
				data.setVendorName(po.getVendorName());
				data.setItemType(po.getIncentiveType()!=null ? po.getIncentiveType().getCode() : null);
			}
			
			Date dateOfReceiving =  model.getShippingDate();
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");  
			
		    if(dateOfReceiving!=null)
		    {
		    	data.setDateOfReceiving(formatter.format(dateOfReceiving));
		    }
			
			
			dataList.add(data);
		}
		
		return dataList;
	}

	 private CatalogUnawareMediaModel createMediaFromFile(final String uid, final MultipartFile file )  {

	        final String mediaCode = uid.concat(EyDmsCoreConstants.UNDERSCORE_CHARACTER).concat("invoice");

	        final MediaFolderModel imageMediaFolder = mediaService.getFolder(EyDmsCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
	        CatalogUnawareMediaModel documentMedia = null;

	        try{
	            documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
	        }
	        catch (AmbiguousIdentifierException ex){
	            LOG.error("More than one media found with code : "+mediaCode);
	            LOG.error("Removing duplicate media : "+mediaCode);
	            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
	            duplicateMedia.setCode(mediaCode);
	            List<CatalogUnawareMediaModel> duplicateMedias = flexibleSearchService.getModelsByExample(duplicateMedia);
	            modelService.removeAll(duplicateMedias);
	        }
	        catch (UnknownIdentifierException uie){
	            if(LOG.isDebugEnabled()){
	                LOG.error("No Media found with code : "+mediaCode);
	            }
	        }
	        finally {
	            if(null == documentMedia){
	                documentMedia = modelService.create(CatalogUnawareMediaModel.class);
	                documentMedia.setCode(mediaCode);
	            }
	        }
	        documentMedia.setFolder(imageMediaFolder);
	        documentMedia.setMime(file.getContentType());
	        modelService.save(documentMedia);
	        try{
	            mediaService.setStreamForMedia(documentMedia,file.getInputStream());
	        }
	        catch (IOException ioe){
	            LOG.error("IO Exception occured while creating invoiceImage for batch with code: "+uid);
	        }

	        return (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);

	    }
	 
	@Override
	public String uploadInvoiceImage(String userId, String purchaseOrderNo, String batchNo, MultipartFile file) {
		
		Date currentDate = Calendar.getInstance().getTime();
		
		PurchaseOrderModel order = getPurchaseOrderForOrderNo(purchaseOrderNo);
		
		String uid = order.getCode();
		
		String batchCode = uid.concat(batchNo);
		
		PurchaseOrderBatchModel b = modelService.create(PurchaseOrderBatchModel.class); 
		b.setCode(batchCode);
		PurchaseOrderBatchModel batch = null;
		
		try{
            batch = flexibleSearchService.getModelByExample(b);
        }
        catch (AmbiguousIdentifierException ex){
            LOG.error("More than one batch found with code : "+batchCode);
            LOG.error("Removing duplicate batch : "+batchCode);
            PurchaseOrderBatchModel duplicateBatch = new PurchaseOrderBatchModel();
            duplicateBatch.setCode(batchCode);
            List<PurchaseOrderBatchModel> duplicateBatches = flexibleSearchService.getModelsByExample(duplicateBatch);
            modelService.removeAll(duplicateBatches);
        }
        catch (Exception e){
            if(LOG.isDebugEnabled()){
                LOG.error("No Media found with code : "+batchCode);
            }
        }
        finally {
            if(null == batch){
                batch = modelService.create(PurchaseOrderBatchModel.class);
                
                String warehouseCode = baseSiteService.getCurrentBaseSite().getUid().concat("Warehouse");
        		
        		WarehouseModel dummyWarehouse = bulkGoodsDao.getWarehouseByCode(warehouseCode);
        		
        		AddressModel dummyAddress =  dummyWarehouse.getAddress();
        		
                batch.setCode(batchCode);
                batch.setWarehouse(dummyWarehouse);				//set dummy warehouse
        		batch.setShippingAddress(dummyAddress);			//set dummy address
        		batch.setPurchaseOrder(order);
            }
        }
		
		CatalogUnawareMediaModel invoiceImage = null;
		 
		if(file!=null)
		{
			invoiceImage =  createMediaFromFile(uid.concat(batchNo).concat("invoiceImage"),file);
			invoiceImage.setAltText("Invoice Image");
			modelService.save(invoiceImage);
		}
		
		batch.setStatus(ConsignmentStatus.DELIVERY_COMPLETED);
		batch.setInvoiceImage(invoiceImage);
		
		batch.setShippingDate(currentDate);		
		
		modelService.save(batch);
		
		List<PurchaseOrderBatchModel> batches = new ArrayList<>(order.getBatches());
		
		if(batches==null|| batches == Collections.EMPTY_LIST)
		{
			batches = new ArrayList<PurchaseOrderBatchModel>();
		}
		
		batches.add(batch);
		
		order.setBatches(batches);
		
		modelService.save(order);
		
		return batch.getCode();
	}

	@Override
	public PurchaseOrderBatchData getBatchDetails(String batchCode) {
		
		PurchaseOrderBatchData data = new PurchaseOrderBatchData();
		PurchaseOrderBatchModel model = bulkGoodsDao.getPurchaseOrderBatchForCode(batchCode);
		
		data.setTicketNo(model.getCode());	
		
		PurchaseOrderModel po = model.getPurchaseOrder();
		
		if(po!=null) 
		{
			data.setPurchaseOrderNo(po.getCode());
			data.setVendorName(po.getVendorName());
			data.setItemType(po.getIncentiveType()!=null ? po.getIncentiveType().getCode() : null);
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");  
		
	    if(model.getShippingDate()!=null)
	    {
	    	data.setDateOfReceiving(formatter.format(model.getShippingDate()));
	    }
		
		List<PurchaseOrderBatchEntryModel> entryList = (List<PurchaseOrderBatchEntryModel>) model.getBatchEntries();
		
		List<PurchaseOrderBatchEntryData> dataList =  new ArrayList<>();
		
		for(PurchaseOrderBatchEntryModel entry : entryList)
		{
			PurchaseOrderBatchEntryData entryData = new PurchaseOrderBatchEntryData();
			
			if(entry.getOrderEntry()!=null)
			{
				entryData.setItemName(entry.getOrderEntry().getProduct()!=null ? entry.getOrderEntry().getProduct().getName() : null);
			}
			entryData.setQtyReceived(entry.getQuantity().intValue());
			
			dataList.add(entryData);
		}
		
		PurchaseOrderBatchEntryListData list = new PurchaseOrderBatchEntryListData();
		list.setPurchaseOrderBatchEntries(dataList);
		data.setBatchEntries(list);		
		
		return data;
	}

	@Override
	public String generateGRN(String userId, PurchaseOrderBatchData batchData, String batchCode) {
		
		Calendar cal = Calendar.getInstance();
		Date currentDate = cal.getTime();
		
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date ytdDate = cal.getTime();
		
		EyDmsUserModel user = (EyDmsUserModel) userService.getUserForUID(userId);
		
		PurchaseOrderBatchModel model = bulkGoodsDao.getPurchaseOrderBatchForCode(batchCode);
		
		List<PurchaseOrderBatchEntryModel> entryList = (List<PurchaseOrderBatchEntryModel>) model.getBatchEntries();
		
		List<PurchaseOrderBatchEntryData> dataList = batchData.getBatchEntries().getPurchaseOrderBatchEntries();
		
		List<PurchaseOrderEntryModel> orderEntryList = new ArrayList<>();
		
		String warehouseCode = user.getEmployeeCode()!=null ? user.getEmployeeCode().concat("Warehouse") : null;
		
		WarehouseModel warehouse = null;
		
		if(warehouseCode!=null)
		{
			warehouse = bulkGoodsDao.getWarehouseByCode(warehouseCode);
			
			if(Objects.isNull(warehouse))
			{
				warehouse = modelService.create(WarehouseModel.class);
				warehouse.setCode(warehouseCode);
				
				VendorModel v = new VendorModel();
				v.setCode("default");	
				
				VendorModel vendor = flexibleSearchService.getModelByExample(v);
				
				warehouse.setVendor(vendor);
				warehouse.setActive(Boolean.TRUE);
				modelService.save(warehouse);	
			}
		}
		
		Map<String,StockLevelModel> stocks =  warehouse.getStockLevels().stream().collect(Collectors.toMap(s->s.getProductCode(), s->s));
		
		int i=0;
		
		for(PurchaseOrderBatchEntryModel entry : entryList)
		{
			try {
			PurchaseOrderBatchEntryData data = dataList.get(i);
			if(data.getQtyReceived()!=null)
			{
				entry.setQuantity(Long.valueOf(data.getQtyReceived()));
			}
			entry.setQtyDamaged(data.getQtyDamaged());
			entry.setQtyMissing(data.getQtyMissing());
			entry.setDamagedItemsDescription(data.getDamagedItemsDescription());
			entry.setMissingItemsDescription(data.getMissingItemsDescription());	
			}catch(Exception e)
			{
				LOG.error(e);
			}
			
			PurchaseOrderEntryModel orderEntry = (PurchaseOrderEntryModel) entry.getOrderEntry();
			orderEntry.setQtyAlreadyReceived(orderEntry.getQtyAlreadyReceived()!=null ? orderEntry.getQtyAlreadyReceived() + entry.getQuantity().intValue() : entry.getQuantity().intValue());
			
			orderEntryList.add(orderEntry);
			
			StockLevelModel stock = stocks.get(orderEntry.getProduct().getCode());
			
			if(Objects.isNull(stock))
			{
				stock = modelService.create(StockLevelModel.class);
				stock.setProductCode(orderEntry.getProduct().getCode());
				stock.setProduct(orderEntry.getProduct());
				stock.setWarehouse(warehouse);
				
				Set<StockLevelModel> stockModels = null;
				
				if(warehouse.getStockLevels()!=null)
				{
					stockModels = new HashSet<>(warehouse.getStockLevels());
				}
				else
				{
					stockModels = new HashSet<>();
				}
				
				stockModels.add(stock);
				warehouse.setStockLevels(stockModels);
			}
			
			List<StockLevelHistoryEntryModel> stockHistories = null;
			
			if(stock.getStockLevelHistoryEntries()!=null)
			{
				stockHistories = new ArrayList<>(stock.getStockLevelHistoryEntries());
			}
			else
			{
				stockHistories = new ArrayList<>();
			}
			
			StockLevelHistoryEntryModel sh = modelService.create(StockLevelHistoryEntryModel.class);
			
			sh.setStockLevel(stock);
			sh.setUpdateDate(currentDate);
			sh.setGoodsIn(orderEntry.getQuantity().intValue());
			
			stock.setGoodsIn(stock.getGoodsIn()!=null ? stock.getGoodsIn() + orderEntry.getQuantity().intValue() : orderEntry.getQuantity().intValue());
			
			modelService.save(stock);
			modelService.refresh(stock);
			
			modelService.save(sh);
			
			stockHistories.add(sh);
			
			stock.setStockLevelHistoryEntries(stockHistories);			
			
			int availableStock = stock.getAvailable() + sh.getGoodsIn();
					 
			stock.setAvailable(availableStock);
			
			modelService.save(stock);
			modelService.save(warehouse);
			++i;
		}
		
		modelService.saveAll(orderEntryList);
		modelService.saveAll(entryList);
		
		model.setCustomerExperience(batchData.getCustomerExperience());
		
		model.setGrnNo(grnNoGenerator.generate().toString());
		
		modelService.save(model);
		
		return model.getGrnNo();
	}

	@Override
	public Boolean uploadImagesForGRNGeneration(String batchCode, int batchEntryNo, MultipartFile[] files) {
		
		PurchaseOrderBatchModel model = bulkGoodsDao.getPurchaseOrderBatchForCode(batchCode);
		PurchaseOrderBatchEntryModel entry = model.getBatchEntries().stream().collect(Collectors.toList()).get(batchEntryNo);
		
		List<MediaModel> mediaList = new ArrayList<>();
		
		String uid = batchCode.concat(batchCode).concat(Integer.toString(batchEntryNo));
		
		int i=0;
		for(MultipartFile file : files)
		{
			CatalogUnawareMediaModel image = null;
			image =  createMediaFromFile(uid.concat(Integer.toString(i)),file);
			image.setAltText("Item Image");
			
			mediaList.add(image);
			
			++i;
		}
		
		modelService.saveAll(mediaList);
		
		entry.setImages(mediaList);
		
		modelService.save(entry);
		
		return Boolean.TRUE;
	}

	@Override
	public PurchaseOrderBatchData getGRNDetails(String batchCode) {
		
		PurchaseOrderBatchData data = new PurchaseOrderBatchData();
		
		PurchaseOrderBatchModel batch = bulkGoodsDao.getPurchaseOrderBatchForCode(batchCode);
		
		PurchaseOrderBatchEntryListData dataList = new PurchaseOrderBatchEntryListData();
		
		List<PurchaseOrderBatchEntryData> list = new ArrayList<>();
		
		data.setTicketNo(batchCode);
		data.setGrnNo(batch.getGrnNo());
		data.setPurchaseOrderNo(batch.getPurchaseOrder().getCode());
		data.setVendorName(batch.getPurchaseOrder().getVendorName());
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");  
		
	    if(batch.getShippingDate()!=null)
	    {
	    	data.setDateOfReceiving(formatter.format(batch.getShippingDate()));
	    }
	    
		data.setItemType(batch.getPurchaseOrder().getIncentiveType().toString());
		data.setCustomerExperience(batch.getCustomerExperience());
		List<PurchaseOrderBatchEntryModel> batchEntries = (List<PurchaseOrderBatchEntryModel>) batch.getBatchEntries();
		
		for(PurchaseOrderBatchEntryModel batchEntry : batchEntries)
		{
			PurchaseOrderBatchEntryData entryData = new PurchaseOrderBatchEntryData();
			entryData.setItemName(batchEntry.getOrderEntry().getProduct().getName());
			entryData.setQtyReceived(batchEntry.getQuantity().intValue());
			entryData.setQtyDamaged(batchEntry.getQtyDamaged());
			entryData.setQtyMissing(batchEntry.getQtyMissing());
			entryData.setDamagedItemsDescription(batchEntry.getDamagedItemsDescription());
			entryData.setMissingItemsDescription(batchEntry.getMissingItemsDescription());
			
			List<String> images = batchEntry.getImages().stream().map(i->i.getURL()).collect(Collectors.toList());
			
			entryData.setImages(images);
			
			list.add(entryData);
		}
		
		dataList.setPurchaseOrderBatchEntries(list);
		
		data.setBatchEntries(dataList);
		return data;
	}

	@Override
	public InventoryStockListData getInventoryStock(String userId) {
		
		Calendar cal = Calendar.getInstance();
		Date currentDate = cal.getTime();
		
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date ytdDate = cal.getTime();
		
		EyDmsUserModel user = (EyDmsUserModel) userService.getUserForUID(userId);
		
		List<InventoryStockData> listData = new ArrayList<>();
		
		InventoryStockListData list = new InventoryStockListData();
		
		String warehouseCode = user.getEmployeeCode()!=null ? user.getEmployeeCode().concat("Warehouse") : null;
		
		WarehouseModel warehouse = null;
		
		if(warehouseCode!=null)
		{
			warehouse = bulkGoodsDao.getWarehouseByCode(warehouseCode);
			
			if(Objects.isNull(warehouse))
			{
				warehouse = modelService.create(WarehouseModel.class);
				warehouse.setCode(warehouseCode);
				
				VendorModel v = new VendorModel();
				v.setCode("default");	
				
				VendorModel vendor = flexibleSearchService.getModelByExample(v);
				
				warehouse.setVendor(vendor);
				warehouse.setActive(Boolean.TRUE);
				modelService.save(warehouse);	
			}
		}
		
		Set<StockLevelModel> stocks = warehouse.getStockLevels();
		
		for(StockLevelModel stock : stocks)
		{
			InventoryStockData data = new InventoryStockData();
			
			data.setProductName(stock.getProduct().getName());
			data.setOpeningStock(stock.getOpeningStock());
			data.setCurrentStock(stock.getAvailable());
			
			data.setTotalReceived(stock.getGoodsIn());	
			data.setGoodsOut(stock.getGoodsOut());
			data.setDisbursed(stock.getDisbursed());
			
			listData.add(data);
		}
		
		list.setStocks(listData);
		list.setName(user.getName());
		list.setCode(user.getEmployeeCode());
		
		return list;
	}

	@Override
	public Boolean uploadImageForGRNGeneration(String batchCode, int batchEntryNo, MultipartFile file) {
		
		PurchaseOrderBatchModel model = bulkGoodsDao.getPurchaseOrderBatchForCode(batchCode);
		PurchaseOrderBatchEntryModel entry = model.getBatchEntries().stream().collect(Collectors.toList()).get(batchEntryNo);
		
		List<MediaModel> mediaList = new ArrayList<>(entry.getImages());
		
		int size = mediaList.size();
		
		String uid = batchCode.concat(batchCode).concat(Integer.toString(batchEntryNo).concat(Integer.toString(size)));
		
		CatalogUnawareMediaModel image =  createMediaFromFile(uid,file);
		image.setAltText("Item Image");
		
		mediaList.add(image);
		
		entry.setImages(mediaList);
		
		modelService.save(entry);
		
		return Boolean.TRUE;
	}
}
