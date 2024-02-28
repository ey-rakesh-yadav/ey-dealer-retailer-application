package com.eydms.occ.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.eydms.facades.TransferGoodsFacade;
import com.eydms.facades.data.InventoryStockListData;
import com.eydms.facades.data.EyDmsUserListData;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/transferGoods")
@ApiVersion("v2")
@Tag(name = "TransferGoods")
public class TransferGoodsController extends EyDmsBaseController {

	
	@Autowired
	TransferGoodsFacade transferGoodsFacade;
	
	
	@RequestMapping(value = "/salesOfficers", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EyDmsUserListData getSalesOfficers(@Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "searchKey") @RequestParam(required = true) String searchKey)
	{
		return transferGoodsFacade.getSalesOfficers(userId, searchKey);
	}
	
	@RequestMapping(value = "/inventoryStock", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public InventoryStockListData getInventoryStock(@Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "soUid") @RequestParam(required = true) String soUid)
	{
		return transferGoodsFacade.getInventoryStock(userId, soUid);
	}
}
