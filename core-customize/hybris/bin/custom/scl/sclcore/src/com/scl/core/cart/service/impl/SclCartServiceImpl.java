package com.scl.core.cart.service.impl;

import com.scl.core.cart.dao.SclERPCityDao;
import com.scl.core.cart.dao.SclSalesOrderDeliverySLADao;
import com.scl.core.cart.dao.SclTruckMaxLoadDao;
import com.scl.core.cart.dao.SclWarehouseDao;
import com.scl.core.cart.service.SclCartService;
import com.scl.core.model.*;
import com.scl.core.region.dao.ERPCityDao;

import com.scl.core.region.dao.GeographicalRegionDao;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import com.scl.core.enums.OrderType;
import com.scl.core.region.dao.DistrictDao;
import com.scl.core.source.dao.DestinationSourceMasterDao;
import com.scl.core.territory.TerritoryUnitService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.site.BaseSiteService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import javax.annotation.Resource;

import de.hybris.platform.core.model.product.ProductModel;

public class SclCartServiceImpl implements SclCartService {

	@Autowired
	private TerritoryUnitService territoryUnitService;

	@Autowired
	private DestinationSourceMasterDao destinationSourceMasterDao;

	@Autowired
	private DeliveryModeDao deliveryModeDao;

	private SclWarehouseDao sclWarehouseDao;
	private SclTruckMaxLoadDao sclTruckMaxLoadDao;
	private DistrictDao districtDao;

	private SclERPCityDao sclERPCityDao;
	private WarehouseService warehouseService;
	private ProductService productService;
	private SclSalesOrderDeliverySLADao sclSalesOrderDeliverySLADao;

	@Resource
	private CatalogVersionService catalogVersionService;

	@Autowired
	private ERPCityDao erpCityDao;

	@Autowired
	private GeographicalRegionDao geographicalRegionDao;

	@Autowired
	private BaseSiteService baseSiteService;


	/**
	 * @param orderType
	 * @param deliveryMode
	 * @param productCode
	 * @param transportationZone
	 * @param incoTerm
	 * @return
	 */
	@Override
	public List<DestinationSourceMasterModel> fetchDestinationSource(String orderType, String deliveryMode, String productCode, String transportationZone, String incoTerm) {

		List<DestinationSourceMasterModel> sourceList = new ArrayList<>();
		BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
		CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSite.getUid() + "ProductCatalog", "Online");
		ProductModel product = getProductService().getProductForCode(catalogVersion, productCode);

		if (null == deliveryMode) {
			deliveryMode = "ROAD";
		}
		if (Objects.nonNull(product)) {
			List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode(deliveryMode);
			if(CollectionUtils.isNotEmpty(deliveryModeList))
			sourceList = destinationSourceMasterDao.findDestinationSource(deliveryModeList.get(0), OrderType.valueOf(orderType), product.getCustCategory(), productCode, transportationZone, baseSiteService.getCurrentBaseSite(),incoTerm);
		}
		return sourceList;
	}




   /**
	 * Service to get max truck load size
	 * @param cityUid
	 * @param warehouseCode
	 * @return
	 * @throws Exception 
	 */
	@Override
	public Integer getMaxTruckLoadSize(String cityUid, String warehouseCode) throws Exception {
		TerritoryUnitModel city = territoryUnitService.getTerritotyUnitforUid(cityUid);
		WarehouseModel source = getSclWarehouseDao().findWarehouseByCode(warehouseCode);
		if(null != city || null != source) {
			return getSclTruckMaxLoadDao().findTruckMaxLoadSize(source,city);
		}
		else {
			throw new Exception("source or city must not be null.");
		}
	}

	//not used
	@Override
	public Collection<ERPCityModel> getListOfERPCityByDistrictCode(String districtIsoCode) {
		List<DistrictModel> districtList = getDistrictDao().findDistrictByCode(districtIsoCode);
		DistrictModel district;
		if(!districtList.isEmpty()) {
			district = districtList.get(0);
		}
		else {
			throw new ModelNotFoundException("No Distrcict found for code: "+districtIsoCode);
		}
		return !district.getErpCities().isEmpty()?district.getErpCities():null;
	}

	@Override
	public List<String> getListOfERPCityByDistrict(String districtCode) {
		List<String> erpCity = geographicalRegionDao.findAllErpCityByDistrictCode(districtCode);
		if(!erpCity.isEmpty() && erpCity != null)
		{
			return erpCity;
		}
		else
		{
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * @param orderType
	 * @param deliveryMode
	 * @param productCode
	 * @param transportationZone
	 * @return
	 */
	@Override
	public List<SclIncoTermMasterModel> fetchIncoTerms(String orderType, String deliveryMode, String productCode, String transportationZone) {
		List<SclIncoTermMasterModel> incoTermsList=new ArrayList<>();
		BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
		CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSite.getUid() + "ProductCatalog", "Online");
		ProductModel product = getProductService().getProductForCode(catalogVersion, productCode);

		if (null == deliveryMode) {
			deliveryMode = "ROAD";
		}
		if (Objects.nonNull(product)) {
			List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode(deliveryMode);
			if(CollectionUtils.isNotEmpty(deliveryModeList))
				 incoTermsList = destinationSourceMasterDao.findIncoTerms(deliveryModeList.get(0), OrderType.valueOf(orderType), product.getCustCategory(), productCode, transportationZone, baseSiteService.getCurrentBaseSite());
		}
		return incoTermsList;
	}

	public DistrictDao getDistrictDao() {
		return districtDao;
	}

	public void setDistrictDao(DistrictDao districtDao) {
		this.districtDao = districtDao;
	}
   
	public SclWarehouseDao getSclWarehouseDao() {
		return sclWarehouseDao;
	}

	public void setSclWarehouseDao(SclWarehouseDao sclWarehouseDao) {
		this.sclWarehouseDao = sclWarehouseDao;
	}

	public SclTruckMaxLoadDao getSclTruckMaxLoadDao() {
		return sclTruckMaxLoadDao;
	}

	public void setSclTruckMaxLoadDao(SclTruckMaxLoadDao sclTruckMaxLoadDao) {
		this.sclTruckMaxLoadDao = sclTruckMaxLoadDao;
	}
	
	
    @Override
    public SalesOrderDeliverySLAModel getSalesOrderDeliverySLA(BaseSiteModel brand, String productCode, String isocode, WarehouseModel source) {
        SalesOrderDeliverySLAModel salesOrderDeliverySLAModel = null;
       List<ERPCityModel> erpCityModelList = getSclERPCityDao().findERPCityByISOCode(isocode);
        ERPCityModel erpCity;
        if(!erpCityModelList.isEmpty()) {
            erpCity = erpCityModelList.get(0);
        }
        else {
            throw new ModelNotFoundException("No ERPCity found for isocode: "+isocode);
        }
        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(brand.getUid()+ "ProductCatalog", "Online");
        ProductModel product = getProductService().getProductForCode(catalogVersion, productCode);
        if(null != source && null !=product) {
            salesOrderDeliverySLAModel =null;// getSclSalesOrderDeliverySLADao().findSalesOrderDeliverySLA(
                   // brand, source, erpCity, product);
        }
        else {
            throw new ModelNotFoundException("Either Warehouse or Product is not found.");
        }
        if(null == salesOrderDeliverySLAModel){
            throw new ModelNotFoundException("No Sales Order Delivery Masters Found for  brand: "+brand+ " , " +
                    "source: "+source+" , erpcity: "+erpCity+" , product "+productCode);
        }
        return salesOrderDeliverySLAModel;
    }
	/**
	 * Return Delivery Mode
	 * @param orderType
	 * @param productCode
	 * @param transportationZone
	 * @return
	 */
	@Override
	public List<DeliveryModeModel> fetchDeliveryMode(String orderType, String productCode, String transportationZone) {
		List<DeliveryModeModel> deliveryModeList = new ArrayList<>();
		BaseSiteModel baseSite = baseSiteService.getCurrentBaseSite();
		CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSite.getUid() + "ProductCatalog", "Online");
		ProductModel product = getProductService().getProductForCode(catalogVersion, productCode);
		if (Objects.nonNull(product)) {
			deliveryModeList = destinationSourceMasterDao.findDeliveryMode(OrderType.valueOf(orderType), product.getCustCategory(), productCode, transportationZone, baseSiteService.getCurrentBaseSite());
		}
		return deliveryModeList;
	}

    public SclERPCityDao getSclERPCityDao() {
        return sclERPCityDao;
    }

    public void setSclERPCityDao(SclERPCityDao sclERPCityDao) {
        this.sclERPCityDao = sclERPCityDao;
    }
    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
   public SclSalesOrderDeliverySLADao getSclSalesOrderDeliverySLADao() {
        return sclSalesOrderDeliverySLADao;
    }

    public void setSclSalesOrderDeliverySLADao(SclSalesOrderDeliverySLADao sclSalesOrderDeliverySLADao) {
        this.sclSalesOrderDeliverySLADao = sclSalesOrderDeliverySLADao;
    }

}
