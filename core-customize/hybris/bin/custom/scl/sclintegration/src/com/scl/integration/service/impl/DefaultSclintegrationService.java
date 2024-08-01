/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.enums.AddressCreatedStatus;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.event.SclAddressEvent;
import com.scl.core.model.*;
import com.scl.core.services.DJPVisitService;
import com.scl.integration.cpi.address.SclSapCpiOutboundAddressConversionService;
import com.scl.integration.cpi.order.SclSapCpiOutboundService;
import com.scl.occ.dto.AddressWsShipToDTO;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.daos.CMSSiteDao;
import de.hybris.platform.cms2.servicelayer.services.admin.CMSAdminSiteService;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import com.scl.integration.service.SclintegrationService;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;

import static org.glassfish.jersey.internal.util.PropertiesHelper.convertValue;


public class DefaultSclintegrationService implements SclintegrationService
{

	public static final String WE = "WE";
	private static final Logger LOG = LoggerFactory.getLogger(DefaultSclintegrationService.class);

	public static final String SHIPTO_SUCCESSMESSAGE = "SHIPTO_SUCCESSMESSAGE";
	public static final String SHIPTO_FAILUREMESSAGE = "SHIPTO_FAILUREMESSAGE";

	public static final String SHIPTO_CONTACTSALES = "SHIPTO_CONTACTSALES";
	private MediaService mediaService;
	private ModelService modelService;
	private FlexibleSearchService flexibleSearchService;

	private CMSSiteDao cmsSiteDao;

	private Converter<AddressModel, AddressData> addressConverter;
	@Resource
	OrderRequisitionDao orderRequisitionDao;

	@Resource
	TerritoryMasterDao territoryMasterDao;

	@Resource
	DataConstraintDao dataConstraintDao;

	@Resource
	CMSAdminSiteService cmsAdminSiteService;

	@Resource
	private DJPVisitService djpVisitService;

	@Resource
	private UserService userService;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	EventService eventService;

	@Autowired
	CustomerAccountService customerAccountService;

	@Resource
	private SclSapCpiOutboundService sclSapCpiDefaultOutboundService;
	@Resource
	private SclSapCpiOutboundAddressConversionService sclSapCpiOutboundAddressConversionService;

	@Resource
	SclStageGateDao sclStageGateDao;



	@Override
	public String getHybrisLogoUrl(final String logoCode)
	{
		final MediaModel media = mediaService.getMedia(logoCode);

		// Keep in mind that with Slf4j you don't need to check if debug is enabled, it is done under the hood.
		LOG.debug("Found media [code: {}]", media.getCode());

		return media.getURL();
	}

	@Override
	public void createLogo(final String logoCode)
	{
		final Optional<CatalogUnawareMediaModel> existingLogo = findExistingLogo(logoCode);

		final CatalogUnawareMediaModel media = existingLogo.isPresent() ? existingLogo.get()
				: modelService.create(CatalogUnawareMediaModel.class);
		media.setCode(logoCode);
		media.setRealFileName("sap-hybris-platform.png");
		modelService.save(media);

		mediaService.setStreamForMedia(media, getImageStream());
	}

	private final static String FIND_LOGO_QUERY = "SELECT {" + CatalogUnawareMediaModel.PK + "} FROM {"
			+ CatalogUnawareMediaModel._TYPECODE + "} WHERE {" + CatalogUnawareMediaModel.CODE + "}=?code";

	private Optional<CatalogUnawareMediaModel> findExistingLogo(final String logoCode)
	{
		final FlexibleSearchQuery fQuery = new FlexibleSearchQuery(FIND_LOGO_QUERY);
		fQuery.addQueryParameter("code", logoCode);

		try
		{
			return Optional.of(flexibleSearchService.searchUnique(fQuery));
		}
		catch (final SystemException e)
		{
			return Optional.empty();
		}
	}
	
	@Override
	public TerritoryMasterModel getTerritoryMasterByTrriId(String trriId){
		
		return  orderRequisitionDao.getTerritoryMasterByTrriId(trriId);
	}

	@Override
	public TerritoryUserMappingModel getTerritoryUserMapping(String trriId, String Uid){

		return  orderRequisitionDao.getTerritoryUserMapping(trriId,Uid);
	}

	@Override
	public CustomerSubAreaMappingModel getCustomerSubAreaMapping(String sclCust, SubAreaMasterModel subareamaster, String State, String subArea, String district) {
		final Map<String, Object> params = new HashMap<String, Object>();
		String query = "SELECT {csam.pk} FROM {CustomerSubAreaMapping AS csam  join sclcustomer as sclcus on {csam.sclCustomer}={sclcus.pk} }  WHERE {sclcus.uid}=?sclCust and {csam.subAreaMaster} =?subareamaster" +
				" and {csam.brand}=?brand and {csam.state}=?state and {csam.subArea}=?subArea and {csam.district}=?district";

		params.put("sclCust",sclCust);
		params.put("subareamaster",subareamaster);
		params.put("state",State);
		params.put("subArea",subArea);
		params.put("brand",cmsAdminSiteService.getSiteForId(SclCoreConstants.SCL_SITE));
		params.put("district",district);
		final SearchResult<CustomerSubAreaMappingModel> searchResult = flexibleSearchService.search(query, params);
		return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;
	}

	/**
	 * @param warehouseType
	 * @return
	 */
	@Override
	public List<StageGateSequenceMapperModel> getStageGateSequenceMapperListForSource(WarehouseType warehouseType) {
		return sclStageGateDao.getStageGateSequenceMapperListForSource(warehouseType);
	}

	@Override
	public CMSSiteModel getCMSSiteForID(String id) {
		List<CMSSiteModel> searchResult= getCmsSiteDao().findCMSSitesById(id);
		return CollectionUtils.isNotEmpty(searchResult)? searchResult.get(0):null;
	}

	@Override
	public DealerRetailerMappingModel getDealerRetailerMappingModel(String sclCust, String addPK, String retailerPK){
		final Map<String, Object> params = new HashMap<String, Object>();
		String query = "SELECT {drm.pk} FROM {DealerRetailerMapping AS drm join address as add on {drm.shipTo}={add.pk}}  WHERE {drm.dealer}=?sclCust and {add.pk} =?addPK";

		params.put("sclCust",sclCust);
		params.put("addPK",addPK);
		if(StringUtils.isNotEmpty(retailerPK)){
			query+=" and {drm.retailer}=?retailerPK";
			params.put("retailerPK",retailerPK);
		}
		final SearchResult<DealerRetailerMappingModel> searchResult = flexibleSearchService.search(query, params);
		return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult().get(0):null;

	}



	@Override
	public OrderModel getOrderFromERPOrderNumber(String erpOrderNo){
		return orderRequisitionDao.getOrderFromERPOrderNumber(erpOrderNo);
	}

	@Override
	public ProductModel getProductFromEquiCode(String equiCode, CatalogVersionModel catalogVer){
		return orderRequisitionDao.getProductFromEquiCode(equiCode,catalogVer);
	}

	@Override
	public DistrictMasterModel getDistrictMaster(String district) {
		return territoryMasterDao.getDistrictMaster(district);
	}

	@Override
	public TalukaMasterModel getTalukaMaster(String taluka) {
		return territoryMasterDao.getTalukaMaster(taluka);
	}

	@Override
	public RegionMasterModel getRegionMaster(String region) {
		return territoryMasterDao.getRegionMaster(region);
	}

	@Override
	public StateMasterModel getStateMaster(String state) {
		return territoryMasterDao.getStateMaster(state);
	}

	@Override
	public AddressData triggerShipToPartyAddress(String addressId, SclCustomerModel dealer) {
		
		AddressData addressData=null;

		UserModel currentUser = userService.getCurrentUser();
		AddressModel address = null;

		if (currentUser instanceof SclCustomerModel) {
			SclCustomerModel sclCustomer = (SclCustomerModel) currentUser;
			final String customerType = djpVisitService.getCustomerType(sclCustomer);
			if (StringUtils.isNotBlank(customerType) && customerType.equals(CounterType.DEALER.getCode())) {
				CustomerModel customer = (CustomerModel) currentUser;
				 address = customerAccountService.getAddressForCode(customer, addressId);

				LOG.error("ADDRESS_DUPLICATE_ISSUE" + " Current User is " + currentUser.getUid() + "-" + currentUser.getName() + " and the address pk is " + address.getPk().toString());

				addressData = trigeringToS4Directly(sclCustomer,address);
			} else if (StringUtils.isNotBlank(customerType) && customerType.equals(CounterType.RETAILER.getCode())) {
				CustomerModel customer = (CustomerModel) dealer;
			     address = customerAccountService.getAddressForCode(customer, addressId);

				LOG.error("ADDRESS_DUPLICATE_ISSUE" + " Current User is " + currentUser.getUid() + "-" + currentUser.getName() + " and the address pk is " + address.getPk().toString());

			addressData = trigeringToS4Directly(dealer,address);
			}
		}
		if(Objects.isNull(addressData.getRetailerUid())){
			address.setCRMaddressStatus(dataConstraintDao.findVersionByConstraintName(SHIPTO_FAILUREMESSAGE));
			modelService.save(address);
			addressData=getAddressConverter().convert(address);
		}
		return  addressData;
	}

	@Override
	public AddressData trigeringToS4Directly(SclCustomerModel customer, AddressModel address) {

		if (StringUtils.isNotEmpty(address.getPartnerFunctionId())) {
			address.setRetailerUid(address.getPartnerFunctionId());
		}

		sclSapCpiDefaultOutboundService.sendShipToPartyAddress(sclSapCpiOutboundAddressConversionService
				.convertShipToAddrToSapCpiAddress(address, customer, null, null)).subscribe(

						// onNext
						responseEntityMap -> {
							if (isShipToCreatedSuccessfully(responseEntityMap, address)) {
								LOG.info(String.format(
										"Ship To Party Address [%s] has been sent to the SAP backend through SCPI and ShipTo has been Created!",
										address.getOwner()));
							} else {
								LOG.error(String.format(
										"Ship To Party Address [%s] has been sent to the SAP backend through SCPI but ShipTo Not created!",
										address.getOwner()));
							}
						}
						// onError
						, error -> {
							LOG.error(String.format(
									"Ship To Party Address [%s] has been not sent to the SAP backend through SCPI!",
									address.getOwner()));
						});

		return getAddressConverter().convert(address);
	}

	private boolean isShipToCreatedSuccessfully(ResponseEntity<Map> responseEntityMap, AddressModel addressModel)
	{
		AddressWsShipToDTO addressWsDTO = null;
		if (Objects.nonNull(responseEntityMap.getStatusCode())
				&& responseEntityMap.getStatusCode().toString().equalsIgnoreCase(SclCoreConstants.CREATED_SUCCESSFULLY)
				&& null != responseEntityMap.getBody()) {
			Map<String, Object> resBody = (HashMap<String, Object>) responseEntityMap.getBody();
			LOG.info(" Shipto creation response " + resBody.toString());
			ObjectMapper mapper = new ObjectMapper();

			addressWsDTO = mapper.convertValue(resBody, AddressWsShipToDTO.class);

			if (Objects.nonNull(addressWsDTO) && StringUtils.isNotEmpty(addressWsDTO.getShipToId())) {
				addressModel.setPartnerFunctionId(addressWsDTO.getShipToId());
				addressModel.setSapAddressUsage(WE);
				addressModel.setAddressCreatedStatus(AddressCreatedStatus.CREATEDFROMCRM);
				addressModel.setSapCustomerID(((SclCustomerModel) addressModel.getOwner()).getUid());
				addressModel.setErpAddressStatusDesc(addressWsDTO.getMsg());
				addressModel.setErpAddressStatus(SclCoreConstants.SUCCESSFULLY);
				addressModel.setDealerStatus(addressWsDTO.getDealerstatus());
				addressModel.setRetailerStatus(addressWsDTO.getRetailerstatus());

				if ((StringUtils.isEmpty(addressModel.getRetailerUid())
						&& BooleanUtils.isTrue(addressModel.getDealerStatus()))
						|| (StringUtils.isNotEmpty(addressModel.getRetailerUid())
								&& BooleanUtils.isTrue(addressModel.getDealerStatus())
								&& BooleanUtils.isTrue(addressModel.getRetailerStatus()))) {
					addressModel
							.setCRMaddressStatus(dataConstraintDao.findVersionByConstraintName(SHIPTO_SUCCESSMESSAGE));

					DealerRetailerMappingModel drm = new DealerRetailerMappingModel();
					drm.setDealer((SclCustomerModel) addressModel.getOwner());
					drm.setShipTo(addressModel);
					drm.setLastUsed(new Date());
					if (Objects.nonNull(addressModel.getGeographicalMaster())) {
						drm.setDistrict((StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getDistrict()))
								? addressModel.getGeographicalMaster().getDistrict()
								: StringUtils.EMPTY);
						drm.setState((StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getState()))
								? addressModel.getGeographicalMaster().getState()
								: StringUtils.EMPTY);
						drm.setPinCode((StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getPincode()))
								? addressModel.getGeographicalMaster().getPincode()
								: StringUtils.EMPTY);
						drm.setErpCity((StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getErpCity()))
								? addressModel.getGeographicalMaster().getErpCity()
								: StringUtils.EMPTY);
						drm.setTaluka((StringUtils.isNotEmpty(addressModel.getGeographicalMaster().getTaluka()))
								? addressModel.getGeographicalMaster().getTaluka()
								: StringUtils.EMPTY);
					}
					drm.setPartnerFunctionId((StringUtils.isNotEmpty(addressModel.getPartnerFunctionId()))
							? addressModel.getPartnerFunctionId()
							: StringUtils.EMPTY);

					if (StringUtils.isNotEmpty(addressModel.getRetailerUid())) {
						drm.setRetailer((SclCustomerModel) userService.getUserForUID(addressModel.getRetailerUid()));
					}
					modelService.save(drm);

				} else if ((StringUtils.isEmpty(addressModel.getRetailerUid())
						&& (BooleanUtils.isFalse(addressModel.getDealerStatus())
								|| null == addressModel.getDealerStatus()))
						|| (StringUtils.isNotEmpty(addressModel.getRetailerUid())
								&& (BooleanUtils.isFalse(addressModel.getDealerStatus())
										|| null == addressModel.getDealerStatus())
								&& (BooleanUtils.isFalse(addressModel.getRetailerStatus())
										|| null == addressModel.getRetailerStatus()))) {
					addressModel
							.setCRMaddressStatus(dataConstraintDao.findVersionByConstraintName(SHIPTO_FAILUREMESSAGE));

				} else if (StringUtils.isNotEmpty(addressModel.getRetailerUid())
						&& BooleanUtils.isFalse(addressModel.getDealerStatus())
						&& BooleanUtils.isTrue(addressModel.getRetailerStatus())) {
					addressModel.setCRMaddressStatus(
							MessageFormat.format(dataConstraintDao.findVersionByConstraintName(SHIPTO_CONTACTSALES),
									addressModel.getPartnerFunctionId(), addressModel.getRetailerUid()));

				}

				modelService.save(addressModel);

				return true;
			} else {

				addressModel.setErpAddressStatus(SclCoreConstants.ERROR);
				addressModel.setErpAddressStatusDesc(addressWsDTO.getMsg());
				addressModel.setCRMaddressStatus(dataConstraintDao.findVersionByConstraintName(SHIPTO_FAILUREMESSAGE));
				modelService.save(addressModel);
			}
		}

		return false;
	}

	private InputStream getImageStream()
	{
		return DefaultSclintegrationService.class.getResourceAsStream("/sclintegration/sap-hybris-platform.png");
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	public CMSSiteDao getCmsSiteDao() {
		return cmsSiteDao;
	}

	protected Converter<AddressModel, AddressData> getAddressConverter()
	{
		return addressConverter;
	}

	@Required
	public void setAddressConverter(final Converter<AddressModel, AddressData> addressConverter)
	{
		this.addressConverter = addressConverter;
	}

	public void setCmsSiteDao(CMSSiteDao cmsSiteDao) {
		this.cmsSiteDao = cmsSiteDao;
	}
}
