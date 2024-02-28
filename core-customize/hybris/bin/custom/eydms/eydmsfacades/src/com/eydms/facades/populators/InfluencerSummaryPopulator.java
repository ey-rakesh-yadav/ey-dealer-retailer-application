package com.eydms.facades.populators;

import com.eydms.core.dao.InfluencerDao;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.InfluencerSummaryData;
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

public class InfluencerSummaryPopulator implements Populator<EyDmsCustomerModel, InfluencerSummaryData> {
    @Resource
    private EnumerationService enumerationService;

	@Autowired
	InfluencerDao influencerDao;

    @Override
    public void populate(EyDmsCustomerModel source, InfluencerSummaryData target) throws ConversionException {
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
    }

	private String getParsedDate(Date date) {
		Instant instant = date.toInstant();
		LocalDate localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String formattedDate = localDate.format(formatter);
		return formattedDate;
	}
}
