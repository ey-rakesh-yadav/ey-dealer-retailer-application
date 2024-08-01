package com.scl.core.dao;

import com.scl.core.model.CompetitorProductModel;
import com.scl.core.model.CounterVisitMasterModel;
import com.scl.core.model.MarketMappingDetailsModel;
import de.hybris.platform.core.model.c2l.BrandModel;

import java.util.List;

public interface CompetitorProductDao {
    CompetitorProductModel findCompetitorProductById(String otherProductId,BrandModel brandId, List<String> states);

    List<CompetitorProductModel> getCompetitorProductsByState(BrandModel brandId, List<String> states);

    /**
     * Get Selected CompetitorProduct with Latest Counter Visit
     * @param visitId
     * @return
     */
    List<CompetitorProductModel> getCompetitorProductByVisitId(CounterVisitMasterModel visitId);


    /**
     * Get List of Competitor Products by State
     * @param brands
     * @param states
     * @return
     */
    List<CompetitorProductModel> getListOfCompetitorProductsByState(List<BrandModel> brands, List<String> states);


}