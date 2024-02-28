package com.eydms.facades.network.impl;

import com.eydms.core.constants.GeneratedEyDmsCoreConstants;
import com.eydms.core.dao.*;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.services.EYDMSNewNetworkService;
import com.eydms.core.services.EyDmsWorkflowService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.NetworkServiceImpl;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.*;
import com.eydms.facades.network.EYDMSNewNetworkFacade;
import com.eydms.occ.dto.EYDMSNetworkAdditionPlanWsDto;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.fop.fonts.type1.AdobeStandardEncoding.l;
import static org.apache.fop.fonts.type1.AdobeStandardEncoding.n;

public class EYDMSNewNetworkFacadeImpl implements EYDMSNewNetworkFacade {

    @Autowired
    EYDMSNewNetworkService eydmsNewNetworkService;
    @Autowired
    PointRequisitionDao pointRequisitionDao;

    @Resource
    private Converter<EyDmsCustomerModel, InfluencerSummaryData> influencerSummaryConverter;

    @Resource
    private Converter<NetworkAdditionPlanModel,EYDMSNetworkAdditionPlanData> networkAdditionPlanConverter;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    OrderRequisitionDao orderRequisitionDao;
    @Autowired
    NetworkServiceImpl networkService;
    @Autowired
    EyDmsUserDao eydmsUserDao;
    @Autowired
    DJPVisitDao djpVisitDao;
    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    NetworkDao networkDao;

    @Autowired
    UserService userService;
    @Autowired
    EyDmsWorkflowService eydmsWorkflowService;
    @Autowired
    private ModelService modelService;

    @Autowired
    private Converter<EYDMSNetworkAdditionPlanData, NetworkAdditionPlanModel> networkAdditionPlanReverseConverter;

    DecimalFormat df = new DecimalFormat("#.#");

    private static final Logger LOG = Logger.getLogger(EYDMSNewNetworkFacadeImpl.class);


    @Override
    public List<InfluencerSummaryData> getInfluencerDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData) {
    	String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
    	 String endDate = LocalDate.now().plusDays(1).toString();
        if (requestCustomerData.getStartDate() != null) {
            startDate = requestCustomerData.getStartDate();
        }
        if (requestCustomerData.getEndDate() != null) {
            endDate = requestCustomerData.getEndDate();
        }
        List<EyDmsCustomerModel> influencerList = territoryManagementService.getCustomerforUser(requestCustomerData);

    	List<List<Object>> list = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, startDate, endDate,null,null);
    	Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
    	.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
    	
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = currentYearCurrentDate.minusYears(1);


        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        
        List<InfluencerSummaryData> summaryDataList = new ArrayList<>();
        if(influencerList!=null && !influencerList.isEmpty()){
            List<List<Object>> currentYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);
            Map<String, Double> mapCurrentYTD = currentYTD.stream().filter(each -> each != null && each.size() > 1 && each.get(0) != null && each.get(1) != null)
                    .collect(Collectors.toMap(each -> ((EyDmsCustomerModel) each.get(0)).getUid(), each -> (Double) each.get(1)));

            List<List<Object>> lastYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);


            Map<String, Double> mapLastYTD = lastYTD.stream().filter(each -> each != null && each.size() > 1 && each.get(0) != null && each.get(1) != null)
                    .collect(Collectors.toMap(each -> ((EyDmsCustomerModel) each.get(0)).getUid(), each -> (Double) each.get(1)));



            for (EyDmsCustomerModel influencer  : influencerList) {
                var influencerData = influencerSummaryConverter.convert(influencer);
                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(influencer);
                var bagLifted = 0.0;
	        	if(map.containsKey(influencer.getUid())) {
	        		bagLifted = map.get(influencer.getUid());
	        	}
                var salesQuantity = (bagLifted / 20);
                influencerData.setBagLifted(bagLifted);
                influencerData.setBagLiftedNo(bagLifted);
                influencerData.setBagLiftedQty(String.valueOf(salesQuantity));
                if (influencer.getLastLiftingDate() != null) {
                    LocalDate today = LocalDate.now();
                    LocalDate transactionDate = influencer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    influencerData.setDaySinceLastLifting(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate, today))));
                }
                if (CollectionUtils.isNotEmpty(subAraMappinglist)) {
                    var subareaMaster = subAraMappinglist.get(0);
                    influencerData.setDistrict(subareaMaster.getDistrict());
                    influencerData.setTaluka(subareaMaster.getTaluka());
                }
                influencerData.setPotential(Objects.nonNull(influencer.getCounterPotential()) ? String.valueOf(influencer.getCounterPotential()) : "0");


                double salesCurrentYear = 0.0;
                if (mapCurrentYTD.containsKey(influencer.getUid())) {
                    salesCurrentYear = mapCurrentYTD.get(influencer.getUid());
                }
                double salesCurrentYearQty = (salesCurrentYear / 20);


                double salesLastYear = 0.0;
                if (mapLastYTD.containsKey(influencer.getUid())) {
                    salesLastYear = mapLastYTD.get(influencer.getUid());
                }
                double salesLastYearQty = (salesLastYear / 20);
                influencerData.setSalesYtd(df.format(salesCurrentYearQty));
                influencerData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty, salesLastYearQty)));


                summaryDataList.add(influencerData);

            }
        }
        AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(InfluencerSummaryData::getBagLifted).reversed()).forEach(infdata-> infdata.setRank(rank.getAndIncrement()));
        return summaryDataList;
    }

    @Override
    public List<DealerCurrentNetworkData> getRetailedDetailedPaginatedSummaryList(RequestCustomerData requestCustomerData) {
    	String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
    	String endDate = LocalDate.now().plusDays(1).toString();
        if (requestCustomerData.getStartDate() != null) {
            startDate = requestCustomerData.getStartDate();
        }
        if (requestCustomerData.getEndDate() != null) {
            endDate = requestCustomerData.getEndDate();
        }
        List<EyDmsCustomerModel> retailerList = territoryManagementService.getCustomerforUser(requestCustomerData);

    	List<List<Object>> list = orderRequisitionDao.getSalsdMTDforRetailer(retailerList, startDate, endDate,null,null);
    	Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
    	.collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
    
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = currentYearCurrentDate.minusYears(1);


        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

        List<DealerCurrentNetworkData> summaryDataList = new ArrayList<>();
        if(retailerList!=null && !retailerList.isEmpty()){


            List<List<Object>> currentYTD = orderRequisitionDao.getSalsdMTDforRetailer(retailerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);

            Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)

                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));


            List<List<Object>> lastYTD = orderRequisitionDao.getSalsdMTDforRetailer(retailerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);



            Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)

                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));




            for (EyDmsCustomerModel retailer : retailerList){

                DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();

                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(retailer);

                dealerCurrentNetworkData.setCode(retailer.getUid());

                dealerCurrentNetworkData.setName(retailer.getName());

                dealerCurrentNetworkData.setPotential(Objects.nonNull(retailer.getCounterPotential()) ? String.valueOf(retailer.getCounterPotential()) : "0");

                var salesMtd = 0.0;
	            if(map.containsKey(retailer.getUid())) {
	            	salesMtd = map.get(retailer.getUid());
	            }

                var salesQuantity = (salesMtd / 20);



                SalesQuantityData sales = new SalesQuantityData();

                sales.setRetailerSaleQuantity(salesQuantity);

                dealerCurrentNetworkData.setSalesQuantity(sales);


                if(retailer.getLastLiftingDate()!=null) {

                    LocalDate today = LocalDate.now();

                    LocalDate transactionDate = retailer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate,today))));

                }

                double salesCurrentYear = 0.0;

                if(mapCurrentYTD.containsKey(retailer.getUid())) {

                    salesCurrentYear = mapCurrentYTD.get(retailer.getUid());

                }

                double salesCurrentYearQty = (salesCurrentYear / 20);


                double salesLastYear = 0.0;

                if(mapLastYTD.containsKey(retailer.getUid())) {

                    salesLastYear = mapLastYTD.get(retailer.getUid());

                }

                double salesLastYearQty = (salesLastYear / 20);

                dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));

                dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));

                if(CollectionUtils.isNotEmpty(subAraMappinglist)) {

                    var subareaMaster=subAraMappinglist.get(0);

                    dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());

                    dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());

                }

                summaryDataList.add(dealerCurrentNetworkData);
            }

        }
                return summaryDataList;
    }

    @Override
    public List<DealerCurrentNetworkData> getDealerDetailedSummaryList(RequestCustomerData requestCustomerData) {
        if (requestCustomerData.getStartDate() == null) {
            String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
            requestCustomerData.setStartDate(startDate);
        }
        if (requestCustomerData.getEndDate() == null) {
            String endDate = LocalDate.now().plusDays(1).toString();
            requestCustomerData.setEndDate(endDate);
        }
        List<List<Object>> list = eydmsNewNetworkService.getDealerDetailedSummaryList(requestCustomerData);

        BaseSiteModel site = baseSiteService.getCurrentBaseSite();


        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = currentYearCurrentDate.minusYears(1);


        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        List<EyDmsCustomerModel> dealerList = new ArrayList<>();
        if(list!=null && !list.isEmpty()){
            dealerList = list.stream().filter(each -> each.get(0)!=null).map(each->(EyDmsCustomerModel)each.get(0)).collect(Collectors.toList());
        }
        List<DealerCurrentNetworkData> summaryDataList = new ArrayList<>();
        if(dealerList!=null && !dealerList.isEmpty()){


            List<List<Object>> currentYTD = orderRequisitionDao.getSalsdMTDforRetailer(dealerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);

            Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)

                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));


            List<List<Object>> lastYTD = orderRequisitionDao.getSalsdMTDforRetailer(dealerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);



            Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)

                    .collect(Collectors.toMap(each->((EyDmsCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

           for (List<Object> subList : list){
                EyDmsCustomerModel dealer = (EyDmsCustomerModel) subList.get(0);

                DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();

                var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(dealer);

                dealerCurrentNetworkData.setCode(dealer.getUid());

                dealerCurrentNetworkData.setName(dealer.getName());

                dealerCurrentNetworkData.setPotential(Objects.nonNull(dealer.getCounterPotential()) ? String.valueOf(dealer.getCounterPotential()) : "0");

                var salesMtd = 0.0;

                if(subList.get(1)!=null) {

                    salesMtd = (double) subList.get(1);

                }

                var salesQuantity = (salesMtd / 20);



                SalesQuantityData sales = new SalesQuantityData();

                sales.setRetailerSaleQuantity(salesQuantity);

                dealerCurrentNetworkData.setSalesQuantity(sales);


                if(dealer.getLastLiftingDate()!=null) {

                    LocalDate today = LocalDate.now();

                    LocalDate transactionDate = dealer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate,today))));

                }

                double salesCurrentYear = 0.0;

                if(mapCurrentYTD.containsKey(dealer.getUid())) {

                    salesCurrentYear = mapCurrentYTD.get(dealer.getUid());

                }

                double salesCurrentYearQty = (salesCurrentYear / 20);


                double salesLastYear = 0.0;

                if(mapLastYTD.containsKey(dealer.getUid())) {

                    salesLastYear = mapLastYTD.get(dealer.getUid());

                }

                double salesLastYearQty = (salesLastYear / 20);

                dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));

                dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));

                if(CollectionUtils.isNotEmpty(subAraMappinglist)) {

                    var subareaMaster=subAraMappinglist.get(0);

                    dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());

                    dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());

                }
                dealerCurrentNetworkData.setCounterShare(String.valueOf(networkService.getCounterShareForDealer(dealer, site)));

                double target = eydmsUserDao.getCustomerTarget(dealer.getCustomerNo(), EyDmsDateUtility.getMonth(new Date()), EyDmsDateUtility.getYear(new Date()));

                dealerCurrentNetworkData.setTarget(df.format(target));

                double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(dealer.getCustomerNo());

                dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));

                summaryDataList.add(dealerCurrentNetworkData);
            }

        }
        computeRankForRetailer(summaryDataList);
    	List<DealerCurrentNetworkData> collect = summaryDataList.stream().sorted(Comparator.comparing(nw -> nw.getRank())).collect(Collectors.toList());
    	
        return summaryDataList;
    }

    @Override
    public Integer getProposalCount(String leadType) {
         EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
         if (currentUser.getUserType().getCode().equals("TSM")) {

             FilterTalukaData filterTalukaData = new FilterTalukaData();
             List<EyDmsUserModel> soForUser = territoryManagementService.getSOForUser(filterTalukaData);
             LeadType leadType1 = LeadType.valueOf(leadType);
             List<NetworkAdditionPlanModel> data = eydmsNewNetworkService.getProposedPlanSummaryList(leadType1, soForUser);
             if (data.size() > 0)
                 return data.size();
             else
                 return 0;
         }
        if (currentUser.getUserType().getCode().equals("RH")) {

            FilterDistrictData filterDistrictData = new FilterDistrictData();
            List<EyDmsUserModel> tsmForUser = territoryManagementService.getTSMForUser(filterDistrictData);
            LeadType leadType1 = LeadType.valueOf(leadType);
            List<NetworkAdditionPlanModel> data = eydmsNewNetworkService.getCountOfProposedPlanSummaryListForRH(leadType1, tsmForUser);
            if (data.size() > 0)
                return data.size();
            else
                return 0;
        }
         return 0;
    }

    @Override
    public ProposePlanListData getProposedPlanSummaryList(LeadType leadType) {
        ProposePlanListData summaryListData = new ProposePlanListData();
        ProposePlanData proposePlanData = new ProposePlanData();
        List<ProposePlanData> list = new ArrayList<>();

        Integer soProposedAddition = 0;
        Integer diApprovedAddition = 0;
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();

        FilterTalukaData filterTalukaData = new FilterTalukaData();
        List<EyDmsUserModel> soForUser = territoryManagementService.getSOForUser(filterTalukaData);

        List<NetworkAdditionPlanModel> data = eydmsNewNetworkService.getProposedPlanSummaryList(leadType, soForUser);
        if(data!=null && !data.isEmpty())
        {
            for (NetworkAdditionPlanModel dataSet : data) {
                proposePlanData.setRevisionReason(dataSet.getReason());
                proposePlanData.setSoName(dataSet.getRaisedBy().getName());
                proposePlanData.setProposedAddition(dataSet.getRevisedPlan());
                proposePlanData.setProposedAdditionByDI(dataSet.getSystemProposed());
                proposePlanData.setSubArea((dataSet.getSubAreaMaster().getTaluka()));
                proposePlanData.setSoId(dataSet.getRaisedBy().getUid());
                proposePlanData.setRevisedByUid(dataSet.getRevisedBy().getUid());
                proposePlanData.setRaisedBy(String.valueOf(dataSet.getRaisedBy()));
                proposePlanData.setRaisedByUid(dataSet.getRaisedBy().getUid());
                proposePlanData.setSubAreaMasterId(String.valueOf(dataSet.getSubAreaMaster().getPk()));
                if(dataSet.getActionPerformedBy()!=null && dataSet.getActionPerformedBy().equals(currentUser) && dataSet.getActionPerformed()!=null && dataSet.getActionPerformed().equals(WorkflowActions.APPROVED)){
                   proposePlanData.setIsTargetApproved(Boolean.TRUE);
                }
                else {
                    proposePlanData.setIsTargetApproved(Boolean.FALSE);
                }
            }
            list.add(proposePlanData);
        }

        if(list!=null && !list.isEmpty()){
            for(ProposePlanData planData: list){
                soProposedAddition += planData.getProposedAddition();
                diApprovedAddition += planData.getProposedAdditionByDI();
            }
        }
        if(list!=null && !list.isEmpty()){
            for(ProposePlanData planData: list){
                if(planData.getIsTargetApproved()!=null){
                    if(planData.getIsTargetApproved().equals(Boolean.TRUE)){
                        summaryListData.setIsTargetSentForUser(Boolean.TRUE);
                    }
                    else{
                        summaryListData.setIsTargetSentForUser(Boolean.FALSE);
                    }
                }
            }
        }


    //    summaryListData.setDIname(currentUser.getName());
        summaryListData.setDistrictName(currentUser.getDistrict());
        summaryListData.setSoProposedAddition(soProposedAddition);
        summaryListData.setDIApprovedAddition(diApprovedAddition);
        summaryListData.setProposedPlans(list);

        return summaryListData;
    }

    @Override
    public ProposePlanListData getProposedPlansBySO(LeadType leadType, String filter) {

        ProposePlanListData summaryListData = new ProposePlanListData();
        ProposePlanData proposePlanData = new ProposePlanData();
        List<ProposePlanData> list = new ArrayList<>();

        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();

        FilterTalukaData filterTalukaData = new FilterTalukaData();
        List<EyDmsUserModel> soForUser = territoryManagementService.getSOForUser(filterTalukaData);

        List<NetworkAdditionPlanModel> data = eydmsNewNetworkService.getProposedPlansBySO(leadType, soForUser,filter);
        if(data!=null && !data.isEmpty())
        {
            for (NetworkAdditionPlanModel dataSet : data) {
                proposePlanData.setRevisionReason(dataSet.getReason());
                proposePlanData.setSoName(dataSet.getRaisedBy().getName());
                proposePlanData.setProposedAddition(dataSet.getRevisedPlan());
                proposePlanData.setProposedAdditionByDI(dataSet.getSystemProposed());
                proposePlanData.setSubArea((dataSet.getSubAreaMaster().getTaluka()));
                proposePlanData.setSoId(dataSet.getRaisedBy().getUid());
            }
            list.add(proposePlanData);
        }
        summaryListData.setProposedPlans(list);
        return summaryListData;
    }

    @Override
    public String getProposedPlanViewDetails(EYDMSNetworkAdditionPlanData planData) {
        var planModel = getPlanModel(planData.getTaluka(), planData.getApplicableLead());
        if (null != planModel) {
            planModel = networkAdditionPlanReverseConverter.convert(planData, planModel);
        } else {
            planModel = networkAdditionPlanReverseConverter.convert(planData);
        }
        if (Objects.nonNull(planModel)) {
            modelService.save(planModel);
            return "true";
        }
        return null;
    }

    @Override
    public boolean updateTargetStatusForApprovalNwAddition(EYDMSNetworkAdditionPlanData salesApprovalData) {

        return eydmsNewNetworkService.updateTargetStatusForApprovalNwAddition(salesApprovalData);

    }

    @Override
    public boolean targetSendForRevisionNwAddition(ProposePlanData salesRevisedTargetData) {
        return eydmsNewNetworkService.targetSendForRevisionNwAddition(salesRevisedTargetData);
    }

    @Override
    public EYDMSNetworkAdditionPlanData proposedPlanViewForTSMRH(String status, String id) {
        EYDMSNetworkAdditionPlanData eydmsNetworkAdditionPlanData = new EYDMSNetworkAdditionPlanData();
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();


        NetworkAdditionPlanModel networkAdditionPlanModel = eydmsNewNetworkService.getProposedPlanViewForTSMRH(status, id);
        if(networkAdditionPlanModel!=null){
            eydmsNetworkAdditionPlanData  = networkAdditionPlanConverter.convert(networkAdditionPlanModel, eydmsNetworkAdditionPlanData);

        }
        else{
            eydmsNetworkAdditionPlanData  = networkAdditionPlanConverter.convert(networkAdditionPlanModel,eydmsNetworkAdditionPlanData);
        }

        /*if(currentUser.getUserType().getCode().equals("TSM")){

           // NetworkAdditionPlanModel networkAdditionPlanModel = eydmsNewNetworkService.getProposedPlanViewForTSMRH(status, id);

            if (networkAdditionPlanModel != null) {
                eydmsNetworkAdditionPlanData.setSubAreaName(networkAdditionPlanModel.getSubAreaMaster().getTaluka());
                eydmsNetworkAdditionPlanData.setSoName(networkAdditionPlanModel.getRaisedBy().getName());
                eydmsNetworkAdditionPlanData.setAdditionCount(networkAdditionPlanModel.getRevisedPlan());
                    //eydmsNetworkAdditionPlanData.setCity(networkAdditionPlanModel.getCity());
                eydmsNetworkAdditionPlanData.setTaluka(networkAdditionPlanModel.getTaluka());
                eydmsNetworkAdditionPlanData.setDistrictName(networkAdditionPlanModel.getDistrictMaster().getName());
                eydmsNetworkAdditionPlanData.setTotalCounter(networkAdditionPlanModel.getTotalCounter());
                eydmsNetworkAdditionPlanData.setShreeCounter(networkAdditionPlanModel.getShreeCounter());
                eydmsNetworkAdditionPlanData.setEnableApproveFormCompletion(Boolean.TRUE);
                eydmsNetworkAdditionPlanData.setEnableRevisedFormCompletion(Boolean.TRUE);

            }

        }
        else if(currentUser.getUserType().getCode().equals("RH")){

            NetworkAdditionPlanModel networkAdditionPlanModel = eydmsNewNetworkService.getProposedPlanViewForTSMRH(status, id);
            if (networkAdditionPlanModel != null){
                eydmsNetworkAdditionPlanData.setDistrict(networkAdditionPlanModel.getDistrict());
                eydmsNetworkAdditionPlanData.setDiName(networkAdditionPlanModel.getRevisedBy().getName());
                eydmsNetworkAdditionPlanData.setAdditionCount(networkAdditionPlanModel.getRevisedPlan());
                eydmsNetworkAdditionPlanData.setTaluka(networkAdditionPlanModel.getTaluka());
                eydmsNetworkAdditionPlanData.setDistrictName(networkAdditionPlanModel.getDistrictMaster().getName());
                eydmsNetworkAdditionPlanData.setTotalCounter(networkAdditionPlanModel.getTotalCounter());
                eydmsNetworkAdditionPlanData.setShreeCounter(networkAdditionPlanModel.getShreeCounter());
                eydmsNetworkAdditionPlanData.setEnableApproveFormCompletion(Boolean.TRUE);
                eydmsNetworkAdditionPlanData.setEnableRevisedFormCompletion(Boolean.TRUE);
            }
        }*/
        return eydmsNetworkAdditionPlanData;
    }

    @Override
    public ProposePlanListData getProposedPlanSummaryListForTSMRH(SearchPageData searchPageData,LeadType leadType,List<String> statuses,boolean isPendingForApproval) {
        ProposePlanListData proposePlanListData = new ProposePlanListData();
        List<ProposePlanData> proposePlanDataList = new ArrayList<>();

        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();

        int diProposedAddition = 0;
        int rhApprovedAddition = 0;
        int soProposedAddition = 0;
        int diApprovedAddition = 0;

        List<EyDmsUserModel> soForUser = new ArrayList<>();
        List<EyDmsUserModel> tsmForUser = new ArrayList<>();

        if (currentUser.getUserType().getCode().equals("TSM")) {
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            soForUser = territoryManagementService.getSOForUser(filterTalukaData);
        } else if (currentUser.getUserType().getCode().equals("RH")) {
            FilterDistrictData filterDistrictData = new FilterDistrictData();
            tsmForUser = territoryManagementService.getTSMForUser(filterDistrictData);
        }

        SearchPageData<NetworkAdditionPlanModel> searchResult = eydmsNewNetworkService.getProposedPlanSummaryListForTSMRH(searchPageData, leadType, statuses, isPendingForApproval, soForUser, tsmForUser, currentUser);

        if (currentUser.getUserType().getCode().equals("TSM")) {

            if (searchResult != null && searchResult.getResults() != null) {
                List<NetworkAdditionPlanModel> list = searchResult.getResults();
                for (NetworkAdditionPlanModel networkAdditionPlanModel : list) {
                    ProposePlanData proposePlanData = new ProposePlanData();
                    proposePlanData.setSubArea(networkAdditionPlanModel.getSubAreaMaster().getTaluka());
                    proposePlanData.setSoName(networkAdditionPlanModel.getRaisedBy().getName());
                    proposePlanData.setProposedAddition(networkAdditionPlanModel.getRevisedPlan());
                    proposePlanData.setProposedAdditionByDI(networkAdditionPlanModel.getRevisedPlan());
                    proposePlanData.setRevisionReason(networkAdditionPlanModel.getReason());
                    proposePlanData.setSoId(networkAdditionPlanModel.getRaisedBy().getUid());
                    //proposePlanData.setRevisedByUid(currentUser.getUid());
                    proposePlanData.setRaisedBy(String.valueOf(networkAdditionPlanModel.getRaisedBy()));
                    proposePlanData.setRaisedByUid(networkAdditionPlanModel.getRaisedBy().getUid());
                    proposePlanData.setId(networkAdditionPlanModel.getId());
                    proposePlanData.setSubAreaMasterId(String.valueOf(networkAdditionPlanModel.getSubAreaMaster().getPk()));
                    if (networkAdditionPlanModel.getStatus().equals(NetworkAdditionStatus.PENDING_FOR_APPROVAL_BY_RH)) {
                        proposePlanData.setStatus("Sent To RH");
                    } else {
                        proposePlanData.setStatus(String.valueOf(networkAdditionPlanModel.getStatus()));
                    }
                    if (networkAdditionPlanModel.getActionPerformedBy() != null && networkAdditionPlanModel.getActionPerformedBy().equals(currentUser) && networkAdditionPlanModel.getActionPerformed() != null && networkAdditionPlanModel.getActionPerformed().equals(WorkflowActions.APPROVED)) {
                        proposePlanData.setIsTargetApproved(Boolean.TRUE);
                    } else {
                        proposePlanData.setIsTargetApproved(Boolean.FALSE);
                    }

                    if (networkAdditionPlanModel.getStatus().equals(NetworkAdditionStatus.APPROVED_BY_TSM)) {
                        proposePlanData.setIsTargetSentForUser(true);
                    } else {
                        proposePlanData.setIsTargetSentForUser(false);
                    }

                    proposePlanDataList.add(proposePlanData);
                }
            }
             /*if(proposePlanDataList!=null && !proposePlanDataList.isEmpty()){
                 for(ProposePlanData planData: proposePlanDataList){
                     if(planData.getIsTargetApproved()!=null){
                         if(planData.getIsTargetApproved().equals(Boolean.TRUE)){
                             proposePlanListData.setIsTargetSentForUser(Boolean.TRUE);
                         }
                         else{
                             proposePlanListData.setIsTargetSentForUser(Boolean.FALSE);
                         }
                     }
                 }
             }*/
            if (proposePlanDataList != null && !proposePlanDataList.isEmpty()) {
                for (ProposePlanData planData : proposePlanDataList) {
                    if (planData.getIsTargetSentForUser() != null) {
                        if (planData.getIsTargetSentForUser().equals(Boolean.TRUE)) {
                            proposePlanListData.setIsTargetSentForUser(Boolean.TRUE);
                        } else {
                            proposePlanListData.setIsTargetSentForUser(Boolean.FALSE);
                        }
                    } else {
                        proposePlanListData.setIsTargetSentForUser(Boolean.TRUE);
                    }
                }
            }

                if (proposePlanDataList != null && !proposePlanDataList.isEmpty()) {
                    for (ProposePlanData planData : proposePlanDataList) {
                        if (planData.getProposedAddition() != null) {
                            soProposedAddition += planData.getProposedAddition();
                        }
                     /*if(planData.getProposedAdditionByDI()!=null ){
                         diApprovedAddition += planData.getProposedAdditionByDI();
                     }*/

                    }
                }

            if (proposePlanDataList != null && !proposePlanDataList.isEmpty()) {
                for (ProposePlanData planData : proposePlanDataList) {
                     if(planData.getProposedAdditionByDI()!=null ){
                         diApprovedAddition += planData.getProposedAdditionByDI();
                     }

                }
            }
                LOG.info("DI Name" + currentUser.getName());
                proposePlanListData.setDiName(currentUser.getName());
                Collection<DistrictMasterModel> currentDistrict = territoryManagementService.getCurrentDistrict();
                if (currentDistrict != null && !currentDistrict.isEmpty()) {
                    for (DistrictMasterModel districtMasterModel : currentDistrict) {
                        LOG.info("District Name:" + districtMasterModel.getName());
                        proposePlanListData.setDistrictName(districtMasterModel.getName());
                    }
                }
                proposePlanListData.setSoProposedAddition(soProposedAddition);
            //    diApprovedAddition = eydmsNewNetworkService.getApprovedAdditionSumForTSMRH(leadType, soForUser, tsmForUser, currentUser);
                proposePlanListData.setDIApprovedAddition(diApprovedAddition);
                proposePlanListData.setTotalCount((int) searchResult.getPagination().getTotalNumberOfResults());
                proposePlanListData.setProposedPlans(proposePlanDataList);


            } else if (currentUser.getUserType().getCode().equals("RH")) {

                if (searchResult != null && searchResult.getResults() != null) {
                    List<NetworkAdditionPlanModel> list = searchResult.getResults();
                    for (NetworkAdditionPlanModel networkAdditionPlanModel : list) {
                        ProposePlanData proposePlanData = new ProposePlanData();
                        proposePlanData.setRevisionReason(networkAdditionPlanModel.getReason());
                        proposePlanData.setSoName(networkAdditionPlanModel.getApprovedBy().getName());
                        proposePlanData.setProposedAddition(networkAdditionPlanModel.getRevisedPlan());
                        proposePlanData.setSubArea((networkAdditionPlanModel.getSubAreaMaster().getTaluka()));
                        proposePlanData.setProposedAdditionByDI(networkAdditionPlanModel.getSystemProposed());
                        if (networkAdditionPlanModel.getRevisedTargetByRH() != null) {
                            proposePlanData.setProposedAdditionByRH(networkAdditionPlanModel.getRevisedTarget());
                        }
                        if (networkAdditionPlanModel.getRevisionReasonByRH() != null) {
                            proposePlanData.setRevisionReasonByRH(networkAdditionPlanModel.getRevisionReasonByRH());
                        }
                        if (networkAdditionPlanModel.getDistrictMaster() != null) {
                            proposePlanData.setDistrictName(networkAdditionPlanModel.getDistrictMaster().getName());
                        }
                        if (networkAdditionPlanModel.getRevisedBy() != null) {
                            proposePlanData.setTsmName(networkAdditionPlanModel.getRevisedBy().getName());
                        }
                        proposePlanData.setSubAreaMasterId(String.valueOf(networkAdditionPlanModel.getSubAreaMaster().getPk()));
                        if (networkAdditionPlanModel.getRaisedBy() != null) {
                            proposePlanData.setRaisedByUid(networkAdditionPlanModel.getRaisedBy().getUid());
                        }
                        if (networkAdditionPlanModel.getRevisedBy() != null) {
                            proposePlanData.setRevisedByUid(networkAdditionPlanModel.getRevisedBy().getUid());
                        }
                        proposePlanData.setId(networkAdditionPlanModel.getId());
                        if (networkAdditionPlanModel.getRevisedTargetBySH() != null) {
                            proposePlanData.setProposedAdditionBySH(networkAdditionPlanModel.getRevisedTargetBySH());
                            proposePlanData.setRevisionReasonBySH(networkAdditionPlanModel.getRevisionReasonBySH());
                        }
                        proposePlanData.setStatus(String.valueOf(networkAdditionPlanModel.getStatus()));
                        if (networkAdditionPlanModel.getActionPerformedBy() != null && networkAdditionPlanModel.getActionPerformedBy().equals(currentUser) && networkAdditionPlanModel.getActionPerformed() != null && networkAdditionPlanModel.getActionPerformed().equals(WorkflowActions.APPROVED)) {
                            proposePlanData.setIsTargetApproved(Boolean.TRUE);
                        } else {
                            proposePlanData.setIsTargetApproved(Boolean.FALSE);
                        }
                        if (networkAdditionPlanModel.getStatus().equals(NetworkAdditionStatus.APPROVED_BY_RH)) {
                            proposePlanData.setIsTargetSentForUser(Boolean.TRUE);
                        } else {
                            proposePlanData.setIsTargetSentForUser(Boolean.FALSE);
                        }
                        if (networkAdditionPlanModel.getStatus().equals(NetworkAdditionStatus.APPROVED_BY_TSM)) {
                            proposePlanData.setStatus("Pending For Approval");
                        } else {
                            proposePlanData.setStatus(String.valueOf(networkAdditionPlanModel.getStatus()));
                        }

                        proposePlanDataList.add(proposePlanData);
                    }
                }

                if (proposePlanDataList != null && !proposePlanDataList.isEmpty()) {
                    for (ProposePlanData planData : proposePlanDataList) {
                        if (planData.getIsTargetSentForUser() != null) {
                            if (planData.getIsTargetSentForUser().equals(Boolean.TRUE)) {
                                proposePlanListData.setIsTargetSentForUser(Boolean.TRUE);
                            } else {
                                proposePlanListData.setIsTargetSentForUser(Boolean.FALSE);
                            }
                        }
                    }
                }

                if (proposePlanDataList != null && !proposePlanDataList.isEmpty()) {
                    for (ProposePlanData planData : proposePlanDataList) {
                        if (planData.getProposedAdditionByDI() != null) {
                            diProposedAddition += planData.getProposedAdditionByDI();
                        }
                     /*if(planData.getProposedAdditionByRH()!=null){
                         rhApprovedAddition += planData.getProposedAdditionByRH();
                     }*/
                    }
                }
            if (proposePlanDataList != null && !proposePlanDataList.isEmpty()) {
                for (ProposePlanData planData : proposePlanDataList) {
                     if(planData.getProposedAdditionByRH()!=null){
                         rhApprovedAddition += planData.getProposedAdditionByRH();
                     }
                }
            }

                Collection<RegionMasterModel> regionName = territoryManagementService.getCurrentRegion();
                for (RegionMasterModel regionMasterModel : regionName) {
                    proposePlanListData.setRegionName(regionMasterModel.getName());
                }
                proposePlanListData.setRhName(currentUser.getName());
                proposePlanListData.setDIProposedAddition(diProposedAddition);
            //    rhApprovedAddition = eydmsNewNetworkService.getApprovedAdditionSumForTSMRH(leadType, soForUser, tsmForUser, currentUser);
                proposePlanListData.setRhApprovedAddition(rhApprovedAddition);
                proposePlanListData.setTotalCount((int) searchResult.getPagination().getTotalNumberOfResults());
                proposePlanListData.setProposedPlans(proposePlanDataList);

            }

            return proposePlanListData;
        }
    @Override
    public boolean targetSendToRhShNwAddition(LeadType leadType) {
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();

        EyDmsWorkflowModel eydmsWorkflowModel=null;



        if(currentUser.getUserType()!=null && currentUser.getUserType().equals(EyDmsUserType.TSM)) {
            try {
                if(leadType.equals(LeadType.RETAILER)){
                    List<NetworkAdditionPlanModel> networkAdditionPlanModel = networkDao.getNetworkAdditionSummaryForRH(leadType);
                    if(networkAdditionPlanModel!=null && !networkAdditionPlanModel.isEmpty()){
                        for(NetworkAdditionPlanModel planModel: networkAdditionPlanModel) {
                            eydmsWorkflowModel = planModel.getApprovalWorkflow();

                            if (eydmsWorkflowModel != null) {
                                planModel.setApprovalLevel(TerritoryLevels.REGION);
                                EyDmsWorkflowModel approvalWorkflowModel = planModel.getApprovalWorkflow();
                                EyDmsWorkflowActionModel eydmsWorkflowActionModel = eydmsWorkflowService.saveWorkflowAction(approvalWorkflowModel, "", planModel.getBrand(), planModel.getSubAreaMaster(), TerritoryLevels.REGION);
                                planModel.setStatus(NetworkAdditionStatus.PENDING_FOR_APPROVAL_BY_RH);
                                //modelService.save(eydmsWorkflowActionModel);
                                modelService.save(approvalWorkflowModel);
                                modelService.save(planModel);
                                return true;
                            }
                        }
                    }

                }

                else if(leadType.equals(LeadType.DEALER)){
                    List<NetworkAdditionPlanModel> networkAdditionPlanModel = networkDao.getNetworkAdditionSummaryForRH(leadType);
                    if(networkAdditionPlanModel!=null && !networkAdditionPlanModel.isEmpty()){
                        for(NetworkAdditionPlanModel planModel: networkAdditionPlanModel) {
                            eydmsWorkflowModel = planModel.getApprovalWorkflow();

                            if (eydmsWorkflowModel != null) {
                                planModel.setApprovalLevel(TerritoryLevels.REGION);
                                EyDmsWorkflowModel approvalWorkflowModel = planModel.getApprovalWorkflow();
                                EyDmsWorkflowActionModel eydmsWorkflowActionModel = eydmsWorkflowService.saveWorkflowAction(approvalWorkflowModel, "", planModel.getBrand(), planModel.getSubAreaMaster(), TerritoryLevels.REGION);
                                planModel.setStatus(NetworkAdditionStatus.PENDING_FOR_APPROVAL_BY_SH);
                                modelService.save(approvalWorkflowModel);
                                modelService.save(planModel);
                                return true;
                            }
                        }
                    }

                }

            }
            catch (ModelSavingException e)
            {
                LOG.error("Error occurred while sending to RH "+e.getMessage()+"\n");
                return false;
            }
        }
        else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(EyDmsUserType.RH))
        {
            try {


                return true;
            }
            catch (ModelSavingException e)
            {
                LOG.error("Error occurred while approving target "+e.getMessage()+"\n");
                return false;
            }
        }
        else
        {
            return false;
        }

        return true;
    }


    private Double getYearToYearGrowth(double salesCurrentYearQty, double salesLastYearQty){
        if(salesLastYearQty>0) {
            return   (((salesLastYearQty - salesCurrentYearQty) / salesLastYearQty) * 100);
        }
        return 0.0;
    }

    private void computeRankForDealer(List<DealerCurrentNetworkData> currentNetworkWsDataList) {
        AtomicInteger rank=new AtomicInteger(1);
        currentNetworkWsDataList.stream().sorted(Collections.reverseOrder(Comparator.comparing(nw -> nw.getSalesQuantity().getActual()))).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));

    }

    private void computeRankForRetailer(List<DealerCurrentNetworkData> currentNetworkWsDataList) {
        AtomicInteger rank=new AtomicInteger(1);
        currentNetworkWsDataList.stream().sorted(Collections.reverseOrder(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity()))).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));
    }

    private NetworkAdditionPlanModel getPlanModel(String taluka, String leadType) {
        Date timestamp=EyDmsDateUtility.getFirstDayOfFinancialYear();

        List<SubAreaMasterModel> talukas = new ArrayList<>();

        if(taluka.equalsIgnoreCase("ALL")) {

            if(userService.getCurrentUser() instanceof EyDmsUserModel)
            {
                talukas = territoryManagementService.getTerritoriesForSO();
            }
            else
            {
                talukas = territoryManagementService.getTerritoriesForCustomer((EyDmsCustomerModel)userService.getCurrentUser());
            }

        }
        else
        {
            talukas.add(territoryManagementService.getTerritoryById(taluka));
        }

        if (!talukas.isEmpty()) {
            return networkDao.findNeworkPlanByTalukaAndLeadType(talukas, leadType, timestamp);
        }

        return null;
    }
}
