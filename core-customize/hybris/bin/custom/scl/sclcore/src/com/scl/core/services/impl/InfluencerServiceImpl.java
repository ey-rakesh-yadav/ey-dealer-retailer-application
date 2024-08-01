package com.scl.core.services.impl;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.MeetStatus;
import com.scl.core.model.*;
import com.scl.facades.data.*;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.InfluencerDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.services.InfluencerService;
import com.scl.core.services.TerritoryManagementService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

public class InfluencerServiceImpl implements InfluencerService{

	@Autowired
	InfluencerDao influencerDao;
	@Autowired
	BaseSiteService baseSiteService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	TerritoryManagementDao territoryManagementDao;
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	
	@Autowired
	ModelService modelService;

	@Autowired
	private Converter<LeadMasterModel,SclLeadData> leadMasterConverter;

	@Override
	public B2BCustomerModel searchOnboardingPartner(List<String> searchKey) throws Exception {
		
		B2BCustomerModel partner = influencerDao.searchSalesOfficer(searchKey);
		
		if(Objects.isNull(partner))
		{
			partner = influencerDao.searchSclCustomer(searchKey);
		}
		
		if(Objects.isNull(partner))
		{
			throw new Exception("User Not found for given search parameters");
		}
		
		return partner;
	}

	@Override
	public Boolean assignOnboardingPartner(String influencerUid, String partnerUid) {
		
		
		SclCustomerModel influencer = (SclCustomerModel) userService.getUserForUID(influencerUid);
		
		B2BCustomerModel partner = null;
		
		if(partnerUid!=null)
		{
			partner = (B2BCustomerModel) userService.getUserForUID(partnerUid);
		}
		else
		{
			List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(influencer);
			List<SclUserModel> sclUsers = territoryManagementDao.getSclUsersForSubArea(subAreas);
			partner = sclUsers.get(0);
		}
		
		
		if(Objects.isNull(partner))
		{
			return Boolean.FALSE;
		}
		
		influencer.setOnboardingPartner(partner);
		modelService.save(influencer);
		
		return Boolean.TRUE;
	}

	@Override
	public String getOnboardingStatus(String influencerId) {
		SclCustomerModel influencer = (SclCustomerModel) userService.getUserForUID(influencerId);
		
		if(Objects.isNull(influencer))
		{
			return "";
		}
		
		return influencer.getCustomerOnboardingStatus()!=null ? influencer.getCustomerOnboardingStatus().getCode() : "";
	}

	@Override
	public List<List<Object>> getGiftSchemeStatusDetails(String influencerType, String state) {
		return influencerDao.getGiftSchemeStatusDetails(influencerType, state);
	}

	@Override
	public SclCustomerModel fetchOnboardingPartnerDetails(String influencerId) {
		SclCustomerModel model = (SclCustomerModel) userService.getUserForUID(influencerId);
		return model;
	}

	@Override
	public MeetingScheduleModel getMeetingScheduleByCode(String meetingCode) {
		return influencerDao.getMeetingScheduleByCode(meetingCode);
	}
	

	@Override
	public List<List<Object>> getInfluencerTypeList(String influencerType) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		return influencerDao.getInfluencerTypeList(influencerType, subAreaMasterList);
	}

	@Override
	public Integer getLeadsPendingForApproval(String influencerType) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		return influencerDao.getLeadsPendingForApproval(influencerType, subAreaMasterList);
	}
	
	@Override
	public Integer getOnboardingsPendingForApproval(String influencerType) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		return influencerDao.getOnboardingsPendingForApproval(influencerType, subAreaMasterList);
	}

    @Override
    public int getInfluencerVisitHistoryCount(String startDate, String endDate) {
        return influencerDao.getInfluencerVisitHistoryCount(startDate,endDate);
    }


	@Override
	public List<List<Object>> getInfluencerNetworkAddition(String influencerType) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		return influencerDao.getInfluencerNetworkAddition(influencerType, subAreaMasterList);
	}

	@Override
	public SearchPageData<SclLeadData> getPaginatedLeadList(SearchPageData searchPageData, LeadRequestData leadRequestData) {

		final SearchPageData<SclLeadData> results = new SearchPageData<>();
		List<SclLeadData> list = new ArrayList<>();
		SearchPageData<LeadMasterModel> leadMasterList = influencerDao.getPaginatedLeadList(searchPageData,leadRequestData);
		if(leadMasterList.getResults()!=null && !leadMasterList.getResults().isEmpty())
		{
			list= leadMasterConverter.convertAll(leadMasterList.getResults());
		}
		results.setSorts(leadMasterList.getSorts());
		results.setResults(list);
		results.setPagination(leadMasterList.getPagination());
		return results;
	}

    @Override
    public SearchPageData<MeetingScheduleModel> getPaginatedScheduleMeetList(SearchPageData searchPageData, String meetingType, String startDate, String endDate, String searchFilter, List<String> status) {
        return influencerDao.getPaginatedScheduleMeetList(searchPageData,meetingType,startDate,endDate,searchFilter,status);
    }

	@Override
	public List<InviteesData> getInviteesForMeeting(MeetingScheduleModel meet, List<SclCustomerModel> customers) {
			List<InviteesData> inviteesDataList=new ArrayList<>();
			if(Objects.nonNull(customers)) {
				customers.forEach(cust -> {
					InviteesData invitee = new InviteesData();
					invitee.setCode(cust.getUid());
					invitee.setName(cust.getName());
					//TODO Set reason
					if (MeetStatus.COMPLETED.equals(meet.getStatus())) {
						if (meet.getAttendees().contains(cust)) {
							invitee.setAttended(true);
						}
						if (Objects.nonNull(cust.getUid())) {
							var salesHistry = influencerDao.getInfluencerSalesData(cust.getUid(), baseSiteService.getCurrentBaseSite());
							var bagLiftedBefore = getBagLiftedBeforeMeet(salesHistry, meet.getEventDate());
							var bagLiftedAfter = getBagLiftedAfterMeet(salesHistry, meet.getEventDate());
							invitee.setBagLiftedBefore(bagLiftedBefore);
							invitee.setBagLiftedAfter(bagLiftedAfter);
							invitee.setGrowth(getGrowth(bagLiftedBefore, bagLiftedAfter));
						}
					}
					inviteesDataList.add(invitee);
				});
			}
			return inviteesDataList;
	}

	private Integer getBagLiftedAfterMeet(List<PointRequisitionModel> salesHistry, Date eventDate) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(eventDate);
		cal.add(Calendar.MONTH,1);
		Date nextMonthDate= cal.getTime();
		var baglifted= salesHistry.stream().filter(sale->sale.getDeliveryDate().after(eventDate) && sale.getDeliveryDate().before(nextMonthDate)).mapToDouble(PointRequisitionModel::getPoints).sum();
		long diff = nextMonthDate.getTime() - eventDate.getTime();
		var days= TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		return (int) (baglifted/days);
	}

	private Integer getBagLiftedBeforeMeet(List<PointRequisitionModel> salesHistry,Date eventDate) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(eventDate);
		cal.add(Calendar.MONTH,-3);
		Date threeMonthBackDate= cal.getTime();
		var baglifted= salesHistry.stream().filter(sale->sale.getDeliveryDate().after(threeMonthBackDate) && sale.getDeliveryDate().before(eventDate)).mapToDouble(PointRequisitionModel::getPoints).sum();
		long diff = eventDate.getTime()-threeMonthBackDate.getTime();
		var days= TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
		return (int) (baglifted/days);
	}
	private String getGrowth(Integer bagLiftedBefore, Integer bagLiftedAfter) {
		DecimalFormat df = new DecimalFormat("#0.00");
		return df.format(((bagLiftedAfter-bagLiftedBefore)/bagLiftedBefore)* 100L);
	}


	@Override
	public List<List<Object>> getInfluencerNetworkTypeList(String influencerType) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		return influencerDao.getInfluencerNetworkTypeList(influencerType, subAreaMasterList);
	}
	
	@Override
	public SearchPageData<SclCustomerModel> getInfluencerOnboardingList(SearchPageData searchPageData,
			RequestCustomerData customerRequestData) {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<SubAreaMasterModel> subAreaMasterList = territoryManagementService.getTaulkaForUser(filterTalukaData);
		return influencerDao.getInfluencerOnboardingList(searchPageData, customerRequestData, subAreaMasterList);
	}

}
