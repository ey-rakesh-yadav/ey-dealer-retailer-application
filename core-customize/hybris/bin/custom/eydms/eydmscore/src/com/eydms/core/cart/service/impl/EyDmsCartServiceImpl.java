package com.eydms.core.cart.service.impl;

import com.eydms.core.cart.dao.EyDmsERPCityDao;
import com.eydms.core.cart.dao.EyDmsSalesOrderDeliverySLADao;
import com.eydms.core.cart.dao.EyDmsTruckMaxLoadDao;
import com.eydms.core.cart.dao.EyDmsWarehouseDao;
import com.eydms.core.cart.service.EyDmsCartService;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.region.dao.ERPCityDao;

import com.eydms.core.region.dao.GeographicalRegionDao;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.searchservices.support.util.ObjectUtils;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import com.eydms.core.enums.OrderType;
import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.ERPCityModel;
import com.eydms.core.model.SalesOrderDeliverySLAModel;
import com.eydms.core.model.TerritoryUnitModel;
import com.eydms.core.region.dao.DistrictDao;
import com.eydms.core.source.dao.DestinationSourceMasterDao;
import com.eydms.core.territory.TerritoryUnitService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.site.BaseSiteService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import de.hybris.platform.core.model.product.ProductModel;

public class EyDmsCartServiceImpl implements EyDmsCartService {

    @Autowired
    TerritoryUnitService territoryUnitService;

    @Autowired
    DestinationSourceMasterDao destinationSourceMasterDao;

    @Autowired
    DeliveryModeDao deliveryModeDao;
    
    EyDmsWarehouseDao eydmsWarehouseDao;
	EyDmsTruckMaxLoadDao eydmsTruckMaxLoadDao;
	DistrictDao districtDao;
	
    private EyDmsERPCityDao eydmsERPCityDao;
    private WarehouseService warehouseService;
    private ProductService productService;
    private EyDmsSalesOrderDeliverySLADao eydmsSalesOrderDeliverySLADao;
    
	@Resource
	private CatalogVersionService catalogVersionService;

	@Autowired
	ERPCityDao erpCityDao;

	@Autowired
	GeographicalRegionDao geographicalRegionDao;
	
	@Autowired
	BaseSiteService baseSiteService;

   @Override
   public List<DestinationSourceMasterModel> fetchDestinationSourceByCity(String cityCode, String orderType, String deliveryMode, String productCode,String district, String state, String taluka) {

	   List<DestinationSourceMasterModel> list = new ArrayList<>();
	   ProductModel product = productService.getProductForCode(productCode);
	   String grade = product.getGrade();
	   String packaging= product.getBagType();

	   if(null == deliveryMode)
	   {
		   deliveryMode= "ROAD";
	   }

	   List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode(deliveryMode);
	   list = destinationSourceMasterDao.findDestinationSourceByCode(cityCode, deliveryModeList.get(0), OrderType.valueOf(orderType), CustomerCategory.TR ,grade, packaging, district, state, baseSiteService.getCurrentBaseSite(), taluka);
	   return list;
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
		WarehouseModel source = getEyDmsWarehouseDao().findWarehouseByCode(warehouseCode);
		if(null != city || null != source) {
			return getEyDmsTruckMaxLoadDao().findTruckMaxLoadSize(source,city);
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

	public DistrictDao getDistrictDao() {
		return districtDao;
	}

	public void setDistrictDao(DistrictDao districtDao) {
		this.districtDao = districtDao;
	}
   
	public EyDmsWarehouseDao getEyDmsWarehouseDao() {
		return eydmsWarehouseDao;
	}

	public void setEyDmsWarehouseDao(EyDmsWarehouseDao eydmsWarehouseDao) {
		this.eydmsWarehouseDao = eydmsWarehouseDao;
	}

	public EyDmsTruckMaxLoadDao getEyDmsTruckMaxLoadDao() {
		return eydmsTruckMaxLoadDao;
	}

	public void setEyDmsTruckMaxLoadDao(EyDmsTruckMaxLoadDao eydmsTruckMaxLoadDao) {
		this.eydmsTruckMaxLoadDao = eydmsTruckMaxLoadDao;
	}
	
	
    @Override
    public SalesOrderDeliverySLAModel getSalesOrderDeliverySLA(BaseSiteModel brand, String productCode, String isocode, WarehouseModel source) {
        SalesOrderDeliverySLAModel salesOrderDeliverySLAModel = null;
       List<ERPCityModel> erpCityModelList = getEyDmsERPCityDao().findERPCityByISOCode(isocode);
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
            salesOrderDeliverySLAModel =null;// getEyDmsSalesOrderDeliverySLADao().findSalesOrderDeliverySLA(
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


    public EyDmsERPCityDao getEyDmsERPCityDao() {
        return eydmsERPCityDao;
    }

    public void setEyDmsERPCityDao(EyDmsERPCityDao eydmsERPCityDao) {
        this.eydmsERPCityDao = eydmsERPCityDao;
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
   public EyDmsSalesOrderDeliverySLADao getEyDmsSalesOrderDeliverySLADao() {
        return eydmsSalesOrderDeliverySLADao;
    }

    public void setEyDmsSalesOrderDeliverySLADao(EyDmsSalesOrderDeliverySLADao eydmsSalesOrderDeliverySLADao) {
        this.eydmsSalesOrderDeliverySLADao = eydmsSalesOrderDeliverySLADao;
    }

}
