package com.scl.core.source.dao.impl;

import java.util.*;

import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclIncoTermMasterModel;
import com.scl.core.region.dao.GeographicalRegionDao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.FreightType;
import com.scl.core.enums.OrderType;
import com.scl.core.enums.SpecialProcessIndicator;
import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.FreightSPIMappingModel;
import com.scl.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.site.BaseSiteService;

public class DefaultDestinationSourceMasterDao extends DefaultGenericDao<DestinationSourceMasterModel> implements DestinationSourceMasterDao {


	Logger LOG=Logger.getLogger(DefaultDestinationSourceMasterDao.class);
	public DefaultDestinationSourceMasterDao() {
		super(DestinationSourceMasterModel._TYPECODE);
	}

	private static final String DEFAULTDATEFORMAT = "yyyy-MM-dd";

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	private GeographicalRegionDao geographicalRegionDao;

	/**
	 * @param deliveryMode
	 * @param orderType
	 * @param customerCategory
	 * @param productCode
	 * @param transportationZone
	 * @param brand
	 * @param incoTerm
	 * @return
	 */
	@Override
	public List<DestinationSourceMasterModel> findDestinationSource(DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String productCode, String transportationZone , BaseSiteModel brand, String incoTerm) {


		if(StringUtils.isNotBlank(transportationZone)) {

			//GeographicalMasterModel geographicalMaster =geographicalRegionDao.fetchGeographicalMaster(transportationZone);
			//String salesDistrict=StringUtils.isNotBlank(geographicalMaster.getSalesDistrict())?geographicalMaster.getSalesDistrict(): Strings.EMPTY;
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
            map.put(DestinationSourceMasterModel.TRANSPORTATIONZONE,transportationZone);
			map.put(DestinationSourceMasterModel.PRODUCTCODE,productCode);
			map.put(DestinationSourceMasterModel.INCOTERMS,Objects.nonNull(findIncoTermByCode(incoTerm))?findIncoTermByCode(incoTerm):Strings.EMPTY);
			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds } where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and {ds:transportationZone}=?transportationZone and {ds:productCode}=?productCode  and {ds:incoterms}=?incoTerms";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.setResultClassList(Arrays.asList(DestinationSourceMasterModel.class));
			query.getQueryParameters().putAll(map);
			LOG.info(String.format("Source list query ::%s",query));
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult();
		}
		return Collections.emptyList();
	}

	@Override
	public List<DestinationSourceMasterModel> findDestinationSourceByCode(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String productCode, String district, String state, BaseSiteModel brand, String taluka) {

		if(city!= null && district!= null && state!=null) {
			GeographicalMasterModel geographicalMaster =geographicalRegionDao.fetchGeographicalMaster(state,district,taluka,city);
			String salesDistrict=StringUtils.isNotBlank(geographicalMaster.getDistrict())?geographicalMaster.getDistrict(): Strings.EMPTY;

			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.SALESDISTRICT,salesDistrict);
			map.put(DestinationSourceMasterModel.PRODUCTCODE, productCode);
			//map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
			/*if(StringUtils.isNotBlank(taluka)) {
				map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
			}*/
			//sap productCode
			//map.put(DestinationSourceMasterModel.GRADE, grade);
			//map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			//map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
			//map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());

			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:salesDistrict})=?salesDistrict and {ds:productCode}=?productCode" ;
			/*if(StringUtils.isNotBlank(taluka)) {
				queryResult = queryResult + " and UPPER({ds:destinationTaluka})=?destinationTaluka ";
			}
			queryResult = queryResult + " and UPPER({ds:destinationCity})=?destinationCity and {ds:grade}=?grade and {ds:packaging}=?packaging ";
*/
			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult();
		}
		return Collections.emptyList();
	}



	@Override
	public List<DestinationSourceMasterModel> findL1Source(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka) {
		if(city!= null && district!= null && state!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
			map.put(DestinationSourceMasterModel.GRADE, grade);
			map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationState})=?destinationState and UPPER({ds:destinationDistrict})=?destinationDistrict and UPPER({ds:destinationTaluka})=?destinationTaluka " +
					" and UPPER({ds:destinationCity})=?destinationCity and {ds:grade}=?grade and {ds:packaging}=?packaging and {ds.sourcePriority} like '%1' ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult();
		}
		return Collections.emptyList();

	}

	@Override
	public DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType,
			CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city,
			String district, String state, BaseSiteModel brand, String grade, String packaging, String taluka) {
		
		if(city!= null && district!= null && state!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.DESTINATIONCITY, city.toUpperCase());
			map.put(DestinationSourceMasterModel.GRADE, grade);
			map.put(DestinationSourceMasterModel.PACKAGING, packaging);
			map.put(DestinationSourceMasterModel.DESTINATIONDISTRICT, district.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONSTATE, state.toUpperCase());
			map.put(DestinationSourceMasterModel.DESTINATIONTALUKA, taluka.toUpperCase());
			map.put(DestinationSourceMasterModel.SOURCE, source);
			
			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and UPPER({ds:destinationState})=UPPER(?destinationState) and UPPER({ds:destinationDistrict})=UPPER(?destinationDistrict) and UPPER({ds:destinationTaluka})=UPPER(?destinationTaluka) " +
					" and UPPER({ds:destinationCity})=UPPER(?destinationCity) and {ds:grade}=?grade and {ds:packaging}=?packaging and {ds.source}=?source";
			
			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult()!=null && !result.getResult().isEmpty()? result.getResult().get(0) : new DestinationSourceMasterModel();
		}
		return new DestinationSourceMasterModel();
	}   
	
	@Override
	public DestinationSourceMasterModel getDestinationSourceBySourceAndSapProductCode(OrderType orderType,
			CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String transportationZone,
			String sapProductCode, BaseSiteModel brand, SclIncoTermMasterModel incoTerm) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.TRANSPORTATIONZONE, transportationZone);
  		map.put(DestinationSourceMasterModel.PRODUCTCODE, sapProductCode);
			map.put(DestinationSourceMasterModel.SOURCE, source);
			map.put(DestinationSourceMasterModel.INCOTERMS, incoTerm);

			String queryResult = "SELECT {ds:pk} from {DestinationSourceMaster as ds} where {ds:brand}=?brand "
					+ "and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode "
					+ "and {ds:orderType}=?orderType and {ds:transportationZone}=?transportationZone "
					+ " and {ds:productCode}=?productCode and {ds.source}=?source and {ds.incoterms}=?incoterms ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<DestinationSourceMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult()!=null && !result.getResult().isEmpty()? result.getResult().get(0) : new DestinationSourceMasterModel();
	} 
	
	@Override
	public SclIncoTermMasterModel findIncoTermByCode(String incoTerm) {
		if(incoTerm!=null) {
			Map<String, Object> map = new HashMap<>();
			map.put(SclIncoTermMasterModel.INCOTERM, incoTerm);
			String queryResult = "SELECT {s:pk} from {SclIncoTermMaster as s} where {s:incoTerm}=?incoTerm ";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			final SearchResult<SclIncoTermMasterModel> result = this.getFlexibleSearchService().search(query);
			return CollectionUtils.isNotEmpty(result.getResult())?result.getResult().get(0):null;
		}
		return null;

	}

	/**
	 * @param deliveryModeModel
	 * @param orderType
	 * @param custCategory
	 * @param productCode
	 * @param transportationZone
	 * @param currentBaseSite
	 * @return
	 */
	@Override
	public List<SclIncoTermMasterModel> findIncoTerms(DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String productCode, String transportationZone, BaseSiteModel currentBaseSite) {
		if(StringUtils.isNotBlank(transportationZone)) {

			//GeographicalMasterModel geographicalMaster =geographicalRegionDao.fetchGeographicalMaster(transportationZone);
			//String salesDistrict=StringUtils.isNotBlank(geographicalMaster.getSalesDistrict())?geographicalMaster.getSalesDistrict(): Strings.EMPTY;
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.DELIVERYMODE, deliveryMode);
			map.put(DestinationSourceMasterModel.BRAND, currentBaseSite);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.TRANSPORTATIONZONE,transportationZone);
			map.put(DestinationSourceMasterModel.PRODUCTCODE,productCode);
			String queryResult = "SELECT DISTINCT({ds:incoterms}) from {DestinationSourceMaster as ds } where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:deliveryMode}=?deliveryMode and {ds:orderType}=?orderType and {ds:transportationZone}=?transportationZone and {ds:productCode}=?productCode";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.setResultClassList(Arrays.asList(SclIncoTermMasterModel.class));
			query.getQueryParameters().putAll(map);
			LOG.info(String.format("Source list query ::%s",query));
			final SearchResult<SclIncoTermMasterModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult();
		}
		return Collections.emptyList();
	}

	/**
	 * Return Distinct Delivery Mode
	 * @param orderType
	 * @param customerCategory
	 * @param productCode
	 * @param transportationZone
	 * @param brand
	 * @return
	 */
	@Override
	public List<DeliveryModeModel> findDeliveryMode(OrderType orderType, CustomerCategory customerCategory, String productCode, String transportationZone, BaseSiteModel brand) {
		if(StringUtils.isNotBlank(transportationZone)) {
			Map<String, Object> map = new HashMap<>();
			map.put(DestinationSourceMasterModel.BRAND, brand);
			map.put(DestinationSourceMasterModel.ORDERTYPE, orderType);
			map.put(DestinationSourceMasterModel.CUSTOMERCATEGORY, customerCategory);
			map.put(DestinationSourceMasterModel.TRANSPORTATIONZONE,transportationZone);
			map.put(DestinationSourceMasterModel.PRODUCTCODE,productCode);
			String queryResult = "SELECT distinct {dm:pk} from {DestinationSourceMaster as ds join DeliveryMode as dm on {ds.deliveryMode}={dm.pk}} where {ds:brand}=?brand and {ds:customerCategory}=?customerCategory and {ds:orderType}=?orderType and {ds:transportationZone}=?transportationZone and {ds:productCode}=?productCode";
			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.setResultClassList(Arrays.asList(DeliveryModeModel.class));
			query.getQueryParameters().putAll(map);
			LOG.info(String.format("Delivery Mode Query ::%s",query));
			final SearchResult<DeliveryModeModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult() != null && !result.getResult().isEmpty() ? result.getResult() : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the SPI from freight type.
	 *
	 * @param freightType the freight type
	 * @return the SPI from freight type
	 */
	@Override
	public FreightSPIMappingModel getSPIFromFreightType(FreightType freightType) {
		if(Objects.nonNull(freightType)) {
			Map<String, Object> map = new HashMap<>();
			map.put(FreightSPIMappingModel.FREIGHTTYPE, freightType);
			String queryResult = "SELECT ({fm:pk}) from {FreightSPIMapping as fm} where {fm:freightType}=?freightType";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			LOG.info(String.format("SPI list query ::%s",query));
			final SearchResult<FreightSPIMappingModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult().get(0);
		}
		return null;
	}

	/**
	 * Gets the freight type from SPI.
	 *
	 * @param specialProcessIndicator the special process indicator
	 * @return the freight type from SPI
	 */

	@Override
	public FreightSPIMappingModel getFreightTypeFromSPI(SpecialProcessIndicator specialProcessIndicator) {
		if(Objects.nonNull(specialProcessIndicator)) {
			Map<String, Object> map = new HashMap<>();
			map.put(FreightSPIMappingModel.SPI, specialProcessIndicator);
			String queryResult = "SELECT ({fm:pk}) from {FreightSPIMapping as fm} where {fm:spi}=?spi";

			final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
			query.getQueryParameters().putAll(map);
			LOG.info(String.format("SPI list query ::%s",query));
			final SearchResult<FreightSPIMappingModel> result = this.getFlexibleSearchService().search(query);
			return result.getResult().get(0);
		}
		return null;
	}
}
