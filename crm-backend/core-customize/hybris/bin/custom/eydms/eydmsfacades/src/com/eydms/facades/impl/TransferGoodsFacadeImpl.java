package com.eydms.facades.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.services.TransferGoodsService;
import com.eydms.facades.TransferGoodsFacade;
import com.eydms.facades.data.InventoryStockListData;
import com.eydms.facades.data.EyDmsUserData;
import com.eydms.facades.data.EyDmsUserListData;

import de.hybris.platform.servicelayer.dto.converter.Converter;

public class TransferGoodsFacadeImpl implements TransferGoodsFacade{

	@Autowired
	TransferGoodsService transferGoodsService;
	
	@Autowired
	Converter<EyDmsUserModel,EyDmsUserData> eydmsUserConverter;
	
	@Override
	public EyDmsUserListData getSalesOfficers(String userId, String key) {
		
		EyDmsUserListData listData = new EyDmsUserListData();
		
		List<EyDmsUserData> list = new ArrayList<>();
		
		List<EyDmsUserModel> salesOfficers = transferGoodsService.getSalesOfficers(userId, key);
		
		list = eydmsUserConverter.convertAll(salesOfficers);
		
		listData.setEyDmsUsers(list);
		
		return listData;
	}

	@Override
	public InventoryStockListData getInventoryStock(String userId, String soUid) {
		return transferGoodsService.getInventoryStock(userId, soUid);
	}

}
