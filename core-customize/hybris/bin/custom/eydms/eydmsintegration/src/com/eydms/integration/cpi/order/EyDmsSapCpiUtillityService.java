package com.eydms.integration.cpi.order;

import com.eydms.core.model.FreightAndIncoTermsMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.List;

public interface EyDmsSapCpiUtillityService {
    List<FreightAndIncoTermsMasterModel> findFreightAndIncoTerms(String state, String district, BaseSiteModel brand, String orgType);

}

