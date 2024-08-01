package com.scl.occ.controllers;

import java.text.ParseException;
import java.util.List;

import com.scl.facades.BrandingFacade;
import com.scl.facades.data.*;
import com.scl.facades.order.data.IntegrationOrderEntryData;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//import com.scl.facades.DJPVisitFacade;
//import com.scl.facades.data.CRMVisitListData;

import com.scl.facades.SlctCrmIntegrationFacade;
import com.scl.facades.order.data.IntegrationOrderData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/{baseSiteId}/integration")
public class SlctCrmIntegrationController extends SclBaseController {

	@Autowired
	BrandingFacade brandingFacade;

	@Autowired
	SlctCrmIntegrationFacade slctCrmIntegrationFacade;
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/marketMapping", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public MarketMappingListData getAllMarketMappingDetails() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAllMarketMappingDetails();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getLeadCount", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public LeadGeneratedDetailsListData getLeadCount(@RequestParam(required = false) Integer year,
													 @RequestParam(required = false) Integer month) throws CMSItemNotFoundException, ConversionException
	{
		int month1 = 0, year1 = 0;
		if (month != null)
			month1 = month.intValue();
		if (year != null)
			year1 = year.intValue();
		return slctCrmIntegrationFacade.getLeadCount(year1,month1);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/visitMaster", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CRMVisitListData getAllVisit() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAllVisit();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/counterVisits", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CRMCounterVisitListData getAllCounterVisit() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAllCounterVisit();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/influencerOnboarding", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmInfluencerListData getAllInfluencerDetails() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAllInfluencerDetails();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/retailerSalesInfo", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public RetailerSalesInfoListData getAllRetailerSalesInfo() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAllRetailerSalesInfo();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deleteLPSourceMaster", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean deleteLPSourceMasterData(@RequestBody LPSourceMasterListData lpSourceMasterListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.deleteLPSourceMasterData(lpSourceMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateOutstandingDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateOutstandingDetails(@RequestBody SlctCrmOutstandingDetailsListData slctCrmOutstandingDetailsListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.insertUpdateOutstandingDetails(slctCrmOutstandingDetailsListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/djprunMasterDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean updateRunMasterDetails(@RequestBody SlctRunMasterListData slctRunMasterListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.updateRunMasterDetails(slctRunMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/djpCounterScoreDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean updateCounterScoreDetails(@RequestBody SlctCounterScoreListData slctCounterScoreListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.updateCounterScoreDetails(slctCounterScoreListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/djpRouteScoreDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean updateRouteScoreDetails(@RequestBody SlctRouteScoreListData slctRouteScoreListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.updateRouteScoreDetails(slctRouteScoreListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/ncrData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertNcrDetails(@RequestBody SlctNcrListData slctNcrListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.insertNcrDetails(slctNcrListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/nirmanMitraTransactionData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertMitraTransactionDetails(@RequestBody SlctMitraTransactionListData slctMitraTransactionListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.insertMitraTransactionDetails(slctMitraTransactionListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/mitraMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateMitraMasterDetails(@RequestBody SlctMitraMasterListData slctMitraMasterListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.insertUpdateMitraMasterDetails(slctMitraMasterListData);
	}

//	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
//	@RequestMapping(value = "/lineSplitOrderStatus", method = RequestMethod.POST)
//	@ResponseBody
//	@ApiBaseSiteIdAndUserIdParam
//	public boolean lineSplitOrderStatus(@RequestBody SlctOrderEntryListData slctOrderEntryListData) throws CMSItemNotFoundException, ConversionException
//	{
//		return slctCrmIntegrationFacade.updateLineSplitOrderDetails(slctOrderEntryListData);
//	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/OrderLineScheduler", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public slctOrderLineSchedulerDetailsListData getOrderLineSchedulerDetails() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getOrderLineSchedulerDetails();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "update/orders", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean updateOrderFromErp(@RequestBody List<IntegrationOrderData> order) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.updateOrderFromErp(order.get(0));
	}

	//new api update/ordersentry
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "update/orderEntry", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<String> updateOrderEntryFromErp(@RequestBody List<IntegrationOrderEntryData> orderEntryData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.updateOrderEntryFromErp(orderEntryData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "create/orders", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public NewOrderReponseData createOrderFromErp(@RequestBody List<IntegrationOrderData> order) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.createOrderFromErp(order.get(0));
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/dealerCategorizationData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean updateDealerCategorization(@RequestBody DealerCategorizationListData dealerCategorizationListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.updateDealerCategorization(dealerCategorizationListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/sclCustomerMarketMapping", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmCustomerListData getAllSlctCrmCustomerDetails() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAllSlctCrmCustomerDetails();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/prospectiveNetworksList", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertProspectiveNetworksList(@RequestBody SlctProspectiveNetworksListData prospectiveNetworksListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.insertProspectiveNetworksList(prospectiveNetworksListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/dealerLedgerDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertDealerLedgerDetails(@RequestBody DealerLedgerListData dealerLedgerListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateDealerLedgerDetails(dealerLedgerListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/isoMasterDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertIsoMasterDetails(@RequestBody SlctISOMasterListData slctISOMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertIsoMasterDetails(slctISOMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deleteExistingIsoMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean deleteOldIsoMasterData() throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.deleteOldIsoMasterData();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertEtaDetails", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertEtaDetails(@RequestBody SlctEtaListData slctEtaListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertEtaDetails(slctEtaListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateCustomerMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateCustomerMasterData(@RequestBody SlctCrmIntegrationCustomerMasterListData slctCrmIntegrationCustomerMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateCustomerMasterDetails(slctCrmIntegrationCustomerMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/salesPlanningDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctIntegrationSalesPlanningListData getSalesPlanningListData() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getSalesPlanningListData();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateGeographicalMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateGeographicalMasterData(@RequestBody SlctGeographicalMasterListData slctGeographicalMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateGeographicalMasterDetails(slctGeographicalMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateFreightAndIncoTermsMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateFreightAndIncoTermsMasterData(@RequestBody SlctFreightAndIncoTermsListData slctFreightAndIncoTermsListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateFreightAndIncoTermsMasterDetails(slctFreightAndIncoTermsListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateProductMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateProductMasterDetails(@RequestBody SlctProductMasterListData slctProductMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateProductMasterDetails(slctProductMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/noTimeEntryZonesForRoutes", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateSalesOrderDeliverySlaDetails(@RequestBody SlctOrderDeliverySlaListData slctOrderDeliverySlaListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateSalesOrderDeliverySlaDetails(slctOrderDeliverySlaListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/createNewWarehouses", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertWarehouseDetails(@RequestBody SlctWarehouseListData slctWarehouseListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertWarehouseDetails(slctWarehouseListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/epodDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctEpodOutboundListData getEpodDetailsByOrderLines() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getOrderLineForEpod();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/updateTruckReachData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SLCTruckReachedDateListData updateTruckReachData(@RequestBody SLCTruckReachedDateListData slctTruckReachedDateListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.updateTruckReachData(slctTruckReachedDateListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deleteAllLpSourceData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean removeExistingDestinationSourceMasterData() throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.removeExistingDestinationSourceMasterData();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertLpSourceData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertDestinationSourceData(@RequestBody LPSourceMasterListData lpSourceMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertDestinationSourceData(lpSourceMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/updateProductEquiCode", method = RequestMethod.PATCH)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean updateProductEquiCode(@RequestBody SlctProductEquivalenceDataListData slctProductEquivalenceDataListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.updateProductEquiCode(slctProductEquivalenceDataListData);
	}


	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deleteDealerRetailerMapping", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public ResponseEntity<T> deleteDealerRetailerMapping(@RequestBody DealerRetailerMappingDataList dealerRetailerMappingDataList) throws CMSItemNotFoundException, ConversionException, ParseException {
		//logic yet to be definded
		return (ResponseEntity<T>) ResponseEntity.ok();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deleteAllLpSourceDataWithSqlQuery", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean removeExistingLpSourceMasterDataViaSqlQuery() throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.removeExistingLpSourceMasterDataThroughSqlQuery();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertDepotSubAreaMappingData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateDepotSubAreaMappingDetails(@RequestBody SlctDepotSubAreaMappingListData slctDepotSubAreaMappingListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateDepotSubAreaMappingDetails(slctDepotSubAreaMappingListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/deleteAllDepotSubAreaMappingData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean removeExistingDepotSubAreaMappingData() throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.removeExistingDepotSubAreaMappingData();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateDOMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmIntegrationDOMasterListData updateDOMasterData(@RequestBody SlctCrmIntegrationDOMasterListData slctCrmIntegrationDOMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateDoMasterDetails(slctCrmIntegrationDOMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateDOSubAreaMappingData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctDOSubAreaMappingListData updateDOSubAreaMappingData(@RequestBody SlctDOSubAreaMappingListData slctDOSubAreaMappingListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateDoSubAreaMapping(slctDOSubAreaMappingListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertRoutesForSoDeliverySLA", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertRoutesForSoDeliverySla(@RequestBody SlctRouteSLAListData slctRouteSLAListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertRoutesForSoDeliverySla(slctRouteSLAListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/BottomUpSalesPlanning", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SalesPlanningBottomUpIntegrationListData getBottomUpSalesPlanningData() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getBottomUpSalesPlanningData();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/TopDownSalesPlanning", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SalesPlanningTopDownIntegrationListData getTopDownSalesPlanningData(@RequestBody SalesPlanningTopDownIntegrationListData salesPlanningTopDownIntegrationListData) throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getTopDownIntegrationDetails(salesPlanningTopDownIntegrationListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/annualSalesBottomUpTargets", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public AnnualSalesBottomUpIntegrationListData getAnnualSalesBottomUpTargetsData() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getAnnualSalesBottomUpData();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateBrandMaster", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctBrandMasterListData insertUpdateBrandMaster(@RequestBody SlctBrandMasterListData slctBrandMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateBrandMaster(slctBrandMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateCompetitorProductMaster", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCompetitorProductMasterListData insertUpdateCompetitorProductMaster(@RequestBody SlctCompetitorProductMasterListData slctCompetitorProductMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateCompetitorProductMaster(slctCompetitorProductMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateSalesNcrData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateSalesNcrData(@RequestBody SalesNcrIntegrationListData salesNcrIntegrationListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateSalesNcrData(salesNcrIntegrationListData);
	}

	@RequestMapping(value = "/orderHeaders", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmOrderListData getModifiedOrders() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getModifiedOrders();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/orderEntries", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmOrderEntryListData getModifiedOrderEntries() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getModifiedOrderEntries();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/salesPlanningRevisedTargets", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SalesPlanningDealerReviewListData getRevisedTargets() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getSalesPlanningReviewedTargets();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateErpAddresses", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateCustomerAddress(@RequestBody ERPCustomerAddressListData erpCustomerAddressListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateCustomerAddress(erpCustomerAddressListData);
	}


	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getInfluencerVisitDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public InfluencerVisitListData getInfluencerVisitDetails() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getInfluencerVisitDetails();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getEndCustomerComplaintRequestListForSLCT", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintDataList getEndCustomerComplaintRequestListForSLCT(@RequestParam(required = false) final String startDate, @RequestParam(required = false) String endDate)
	{
		return  slctCrmIntegrationFacade.getEndCustomerComplaintsForSLCT(startDate, endDate);
  }
	//Submit POS Request form & (SAVE) Enter secondary contact no, Quantity, dimensions, Details &) & Requisition submitted popup
	//Upload photos before branding activity performed - camera capture
	//Submit Outdoors Request form & Submit Dealer cost Request form
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
	@RequestMapping(value="/submitBrandingRequisitionSlct", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public BrandingRequestData submitBrandingRequisitionSlct(@RequestBody BrandingRequestDetailsData brandingRequestDetailsData,
															 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
		return brandingFacade.submitBrandingRequisitionSlct(brandingRequestDetailsData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/influencerMeetCompletion", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public ScheduledMeetListData getinfluencerMeetCompletion()
	{
		return slctCrmIntegrationFacade.getinfluencerMeetCompletion();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/pointRequisitionSalesData", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public PointRequisitionSalesListData getPointRequisitionDetails() throws CMSItemNotFoundException, ConversionException
	{
		return slctCrmIntegrationFacade.getPointRequisitionDetails();
	}
	
	@RequestMapping(value = "/getEndCustomerComplaintClosureRequest", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EndCustomerComplaintDataList getEndCustomerComplaintClosureRequest()
	{
		return slctCrmIntegrationFacade.getEndCustomerComplaintClosureRequest();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getSiteMasterDetailsForSLCT", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmSiteMasterListData getSiteMasterDetailsForSLCT()
	{
		return slctCrmIntegrationFacade.getSiteMasterDetailsForSLCT();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getSiteVisitDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmSiteMasterListData getSiteVisitDetails()
	{
		return slctCrmIntegrationFacade.getSiteVisitForms();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/productPointMaster", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateProductPointMaster(@RequestBody SlctProductPointMasterListData slctProductPointMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateProductPointMaster(slctProductPointMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/schemesDefinitionFromSlct", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean schemesDefinitionFromSlct(@RequestBody SlctSchemesListData slctSchemesListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.schemesDefinitionFromSlct(slctSchemesListData);
	}
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getGiftRedemptionForSLCT", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctGiftRedemptionListData getGiftRedemptionForSLCT()
	{
		return slctCrmIntegrationFacade.getGiftRedemptionForSLCT();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/giftsData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateGiftData(@RequestBody SlctGiftSchemesListData slctGiftSchemesListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateGiftData(slctGiftSchemesListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateTsoMasterData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmTSOMasterListData updateTsoMasterData(@RequestBody SlctCrmTSOMasterListData slctCrmTSOMasterListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateTsoMasterDetails(slctCrmTSOMasterListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/insertUpdateTsoSubAreaMappingData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctTSOSubAreaMappingListData updateTsoSubAreaMappingData(@RequestBody SlctTSOSubAreaMappingListData slctTSOSubAreaMappingListData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateTsoSubAreaMapping(slctTSOSubAreaMappingListData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/syncMarketMappingData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean syncMarketMappingData(@RequestBody MarketMappingInboundSyncData marketMappingInboundSyncData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.syncMarketMappingIds(marketMappingInboundSyncData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/masterStockAllocationData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean insertUpdateStockAllocationData(@RequestBody DealerInvoiceSummaryData dealerInvoiceSummaryData) throws CMSItemNotFoundException, ConversionException, ParseException {
		return slctCrmIntegrationFacade.insertUpdateStockAllocationData(dealerInvoiceSummaryData);
	}

	@RequestMapping(value = "/getSiteTransactionDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SlctCrmSiteTransactionListData getSiteTransactionDetailsForSLCT()
	{
		return slctCrmIntegrationFacade.getSiteTransactionDetailsForSLCT();

	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getSiteConversionDetails", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SiteConversionListData getSiteConversionDetailsForSlct()
	{
		return slctCrmIntegrationFacade.getSiteConversionDetailsForSLCT();
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/syncSiteConversionData", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean syncSiteTransactionData(@RequestBody SiteConversionInboundSyncData siteConversionInboundSyncData, final HttpServletResponse response) throws CMSItemNotFoundException, ConversionException, ParseException {
		boolean returnValue = slctCrmIntegrationFacade.syncSiteTransactionData(siteConversionInboundSyncData);
		//If the return value is false, then the HTTP response code should be sent as 204
		if(!returnValue) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}

		return returnValue;
	}
}