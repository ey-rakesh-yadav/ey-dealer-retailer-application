/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.integration.service;

import java.util.List;

import com.scl.core.enums.WarehouseType;
import com.scl.core.model.*;
import com.scl.occ.dto.AddressWsShipToDTO;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;

public interface SclintegrationService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);

	TerritoryMasterModel getTerritoryMasterByTrriId(String trriId);
	TerritoryUserMappingModel getTerritoryUserMapping(String trriId, String Uid);
	CustomerSubAreaMappingModel getCustomerSubAreaMapping(String sclCust, SubAreaMasterModel subareamaster, String State, String subArea, String district);
	CMSSiteModel getCMSSiteForID(String id);
	DealerRetailerMappingModel getDealerRetailerMappingModel(String sclCust, String addPK, String retailerId);
	OrderModel getOrderFromERPOrderNumber(String erpOrderNo);

	ProductModel getProductFromEquiCode(String equiCode, CatalogVersionModel catalogVer);

    DistrictMasterModel getDistrictMaster(String district);

	TalukaMasterModel getTalukaMaster(String taluka);

	RegionMasterModel getRegionMaster(String region);

	/* To trigger the addressed created from crm to s4
	 */
	AddressData triggerShipToPartyAddress(String addressId, SclCustomerModel dealer);

	/* To trigger the addressed created from crm to s4 using odata outbound
	*/
	AddressData trigeringToS4Directly(SclCustomerModel customer, AddressModel address);

	StateMasterModel getStateMaster(String state);

	List<StageGateSequenceMapperModel> getStageGateSequenceMapperListForSource(WarehouseType warehouseType);

	}
