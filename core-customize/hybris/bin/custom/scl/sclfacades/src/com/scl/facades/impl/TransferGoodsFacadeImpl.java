package com.scl.facades.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.model.SclUserModel;
import com.scl.core.services.TransferGoodsService;
import com.scl.facades.TransferGoodsFacade;
import com.scl.facades.data.InventoryStockListData;
import com.scl.facades.data.SclUserData;
import com.scl.facades.data.SclUserListData;

import de.hybris.platform.servicelayer.dto.converter.Converter;

public class TransferGoodsFacadeImpl implements TransferGoodsFacade{

	@Autowired
	TransferGoodsService transferGoodsService;
	
	@Autowired
	Converter<SclUserModel,SclUserData> sclUserConverter;
	
	@Override
	public SclUserListData getSalesOfficers(String userId, String key) {
		
		SclUserListData listData = new SclUserListData();
		
		List<SclUserData> list = new ArrayList<>();
		
		List<SclUserModel> salesOfficers = transferGoodsService.getSalesOfficers(userId, key);
		
		list = sclUserConverter.convertAll(salesOfficers);
		
		listData.setSclUsers(list);
		
		return listData;
	}

	@Override
	public InventoryStockListData getInventoryStock(String userId, String soUid) {
		return transferGoodsService.getInventoryStock(userId, soUid);
	}

}
