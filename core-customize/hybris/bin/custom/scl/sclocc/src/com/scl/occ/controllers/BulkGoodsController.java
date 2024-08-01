package com.scl.occ.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.scl.facades.BulkGoodsFacade;
import com.scl.facades.data.InventoryStockData;
import com.scl.facades.data.InventoryStockListData;
import com.scl.facades.data.PurchaseOrderBatchData;
import com.scl.facades.data.PurchaseOrderData;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/bulkGoods")
@ApiVersion("v2")
@Tag(name = "BulkGoods")
public class BulkGoodsController extends SclBaseController{
	
	@Autowired
	BulkGoodsFacade bulkGoodsFacade;
	
	@RequestMapping(value = "/orderNoList", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<String> getListOfPurchaseOrderNumber(@Parameter(description = "userId") @PathVariable(required = true) String userId)
	{
		return bulkGoodsFacade.getListOfPurchaseOrderList(userId);
	}
	
	@RequestMapping(value = "/purchaseOrderDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public PurchaseOrderData getPurchaseOrderDetails(@Parameter(description = "orderNo") @RequestParam(required = true) String orderNo)
	{
		return bulkGoodsFacade.getPurchaseOrderDetails(orderNo);
	}
	
	@RequestMapping(value = "/saveBatch", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public String saveBatchDetails(@Parameter(description = "userId") @PathVariable(required = true) String userId,@Parameter(description = "batchCode") @RequestParam(required = true) String batchCode,
			@Parameter(description = "Order object.", required = true) @RequestBody final PurchaseOrderData orderData)
	{
		return bulkGoodsFacade.saveBatch(userId, orderData, batchCode);
	}
	
	@RequestMapping(value = "/invoiceImageUpload", method = RequestMethod.POST,consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} )
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public String uploadInvoiceImage(@Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "purchaseOrderNo") @RequestParam(required = true) String purchaseOrderNo,
			@Parameter(description = "batchNo") @RequestParam(required = true) String batchNo, @Parameter(description = "Object containing invoice image file") @RequestParam(value = "file") final MultipartFile file)
	{
		return bulkGoodsFacade.uploadInvoiceImage(userId, purchaseOrderNo, batchNo, file);
	}
	
	@RequestMapping(value = "/ticketsListWithoutGRN", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<PurchaseOrderBatchData>  getListOfTicketsWithoutGRN(@Parameter(description = "userId") @PathVariable(required = true) String userId)
	{
		return bulkGoodsFacade.getListOfTicketsWithoutGRN(userId);
	}
	
	@RequestMapping(value = "/ticketsListWithGRN", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<PurchaseOrderBatchData> getListOfTicketsWithGRN(@Parameter(description = "userId") @PathVariable(required = true) String userId)
	{
		return bulkGoodsFacade.getListOfTicketsWithGRN(userId);
	}
	
	@RequestMapping(value = "/batchDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public PurchaseOrderBatchData getBatchDetails(@Parameter(description = "batchCode") @RequestParam(required = true) String batchCode)
	{
		return bulkGoodsFacade.getBatchDetails(batchCode);
	}
	
	@RequestMapping(value = "/generateGRN", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public String generateGRN( @Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "batchCode") @RequestParam(required = true) String batchCode,
			@Parameter(description = "Batch object.", required = true) @RequestBody final PurchaseOrderBatchData batchData)
	{
		return bulkGoodsFacade.generateGRN(userId, batchData, batchCode);	
	}
	
	@PostMapping(value = "/generateGRNMultipleImageUpload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean uploadImagesForGRNGeneration( @Parameter(description = "batchCode") @RequestParam(required = true) String batchCode,
			@Parameter(description = "batchEntryNo") @RequestParam(required = true) int batchEntryNo, @RequestParam final MultipartFile[] files)
	{
		return bulkGoodsFacade.uploadImagesForGRNGeneration(batchCode, batchEntryNo, files);
		
	}
	
	@RequestMapping(value = "/grnDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public PurchaseOrderBatchData getGRNDetails(@Parameter(description = "batchCode") @RequestParam(required = true) String batchCode)
	{
		return bulkGoodsFacade.getGRNDetails(batchCode);
	}
	
	@RequestMapping(value = "/inventoryStock", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public InventoryStockListData getInventoryStock(@Parameter(description = "userId") @PathVariable(required = true) String userId)
	{
		return bulkGoodsFacade.getInventoryStock(userId);
	}

	@PostMapping(value = "/generateGRNImageUpload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean uploadImageForGRNGeneration( @Parameter(description = "batchCode") @RequestParam(required = true) String batchCode,
			@Parameter(description = "batchEntryNo") @RequestParam(required = true) int batchEntryNo, @RequestParam final MultipartFile file)
	{
		return bulkGoodsFacade.uploadImageForGRNGeneration(batchCode, batchEntryNo, file);
		
	}
}
