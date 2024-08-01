package com.scl.facades.populators;

import com.scl.core.dao.InfluencerDao;
import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InfluencerSummaryData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class InfluencerSummaryPopulator implements Populator<SclCustomerModel, InfluencerSummaryData> {
    @Resource
    private EnumerationService enumerationService;

	@Autowired
	InfluencerDao influencerDao;

    @Override
    public void populate(SclCustomerModel source, InfluencerSummaryData target) throws ConversionException {
    	target.setCode(source.getUid());
    	target.setName(source.getName());
    	if(source.getInfluencerType()!=null) {
    		target.setCategory(enumerationService.getEnumerationName(source.getInfluencerType()));
    	}
    	target.setContactNumber(source.getMobileNumber());
    	target.setTimesContacted(source.getTimesContacted());
    	target.setType(source.getNetworkType());
    	target.setPoints(Objects.nonNull(source.getAvailablePoints())?source.getAvailablePoints():0);
		if(source.getLastVisitTime()!=null) {
			target.setLastVisitDate(getParsedDate(source.getLastVisitTime()));
		}
		if(source.getDealerCategory()!=null) {
			target.setDealerCategory(source.getDealerCategory().getCode());
		}
		else
		{
			target.setDealerCategory("");
		}

		Integer infLeadGeneratedCount = influencerDao.getInfLeadGeneratedCount(source);
		target.setLeadGenerated(infLeadGeneratedCount!=0?infLeadGeneratedCount:0);
		target.setModifiedTime(source.getModifiedtime());
    }

	private String getParsedDate(Date date) {
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedDate = localDate.format(formatter);
		return formattedDate;
	}
}
