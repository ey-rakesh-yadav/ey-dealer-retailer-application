package com.eydms.facades.populators.djp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.enums.ApprovalStatus;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.*;
import com.eydms.core.services.DJPVisitService;
import com.eydms.facades.data.VisitMasterData;

import com.eydms.facades.data.marketvisit.MarketVisitDeviationData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ReviewLogsVisitDataPopulator implements Populator<VisitMasterModel,VisitMasterData>{

	@Resource
	EnumerationService enumerationService;

	@Resource
	UserService userService;

	@Autowired
	DJPVisitService djpVisitService;
	
	@Override
	public void populate(VisitMasterModel source, VisitMasterData target) throws ConversionException {
		target.setId(source.getPk().toString());
		target.setVisitDate(source.getVisitPlannedDate());
		target.setVisitedCount(source.getCounterVisits().stream().filter(c -> c.getEndVisitTime() != null).collect(Collectors.toList()).size());
		target.setNotVisitedCount(source.getCounterVisits().stream().filter(c -> c.getEndVisitTime() == null).collect(Collectors.toList()).size());
		target.setObjective(source.getObjective().getObjectiveName());
		target.setApprovalStatus(source.getApprovalStatus() != null ? enumerationService.getEnumerationName(source.getApprovalStatus()) : "");
		if (source.getRoute() != null) {
			target.setRoute(source.getRoute().getRouteId());
		}
		if (source.getSubAreaMaster() != null) {
			target.setLocation(source.getSubAreaMaster().getTaluka());
			target.setSubArea(source.getSubAreaMaster().getPk().toString());
		}

		EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
		if (currentUser.getUserType().getCode().equals("TSM") || currentUser.getUserType().getCode().equals("RH")) {
			if (source.getUser() != null && source.getUser().getUserType().equals(EyDmsUserType.SO)) {

				target.setSalesOfficerName(source.getUser().getName());

				if (source.getEndVisitTime() != null && source.getApprovalStatus()!=null && source.getApprovalStatus().equals(ApprovalStatus.PENDING_APPROVAL)) {
					Calendar cal1 = Calendar.getInstance();
					cal1.setTime(source.getEndVisitTime());
					cal1.add(Calendar.DATE, 1);
					cal1.set(Calendar.HOUR, 0);
					cal1.set(Calendar.MINUTE, 0);
					cal1.set(Calendar.SECOND, 0);
					cal1.set(Calendar.MILLISECOND, 0);
					Date startDate = cal1.getTime();

					Calendar cal2 = Calendar.getInstance();
					cal2.setTime(startDate);
					cal2.add(Calendar.DATE, 3);
					Date endDate = cal2.getTime();

					target.setTimerStartTime(new Date());
					target.setTimerEndTime(endDate);
				}

				List<MarketVisitDeviationData> marketVisitDeviationDataList = new ArrayList<>();
				String routeAdherence = "YES", objectiveAdherence = "YES", counterAdherence = "YES";
				if (source.getRouteScore() != null) {
					MarketVisitDeviationData routeDeviationData = djpVisitService.getRouteDeviationData(source, true);
					if (null != routeDeviationData) {
						if (!routeDeviationData.getSuggested().contains(routeDeviationData.getSelected().get(0))) {
							routeAdherence = "NO";
						}
						marketVisitDeviationDataList.add(routeDeviationData);
					}

					MarketVisitDeviationData objectiveDeviationData = djpVisitService.getObjectiveDeviationData(source, true);
					if (null != objectiveDeviationData) {
						if (!objectiveDeviationData.getSuggested().contains(objectiveDeviationData.getSelected().get(0))) {
							objectiveAdherence = "NO";
						}
						marketVisitDeviationDataList.add(objectiveDeviationData);
					}

					MarketVisitDeviationData counterDeviationData = djpVisitService.getCounterDeviationData(source,true);
					if (null != counterDeviationData) {
						if (!counterDeviationData.getSuggested().contains(counterDeviationData.getSelected().get(0))) {
							counterAdherence = "NO";
						}
						marketVisitDeviationDataList.add(counterDeviationData);
					}

					target.setRouteAdherence(routeAdherence);
					target.setObjectiveAdherence(objectiveAdherence);
					target.setCounterAdherence(counterAdherence);
					target.setDeviations(marketVisitDeviationDataList);

				}
			}


		}

	}
}
