package com.eydms.core.dao;

import com.eydms.core.model.CompetitorProductModel;
import de.hybris.platform.core.model.c2l.BrandModel;

import java.util.List;

public interface CompetitorProductDao {
    CompetitorProductModel findCompetitorProductById(String otherProductId,BrandModel brandId, List<String> states);

    List<CompetitorProductModel> getCompetitorProductsByState(BrandModel brandId, List<String> states);
}