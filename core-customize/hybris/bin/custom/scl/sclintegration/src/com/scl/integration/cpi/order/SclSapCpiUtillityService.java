package com.scl.integration.cpi.order;

import com.scl.core.model.FreightAndIncoTermsMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import java.util.List;

public interface SclSapCpiUtillityService {
    List<FreightAndIncoTermsMasterModel> findFreightAndIncoTerms(String state, String district, BaseSiteModel brand, String orgType);

}

