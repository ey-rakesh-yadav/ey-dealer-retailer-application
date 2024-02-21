package com.eydms.core.services;

import com.eydms.core.model.RetailerSalesSummaryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface RetailerSalesSummaryService {

    SearchPageData<RetailerSalesSummaryModel> getCurrentProratedFyDetails(String subArea, SearchPageData searchPageData, String filter);
    List<RetailerSalesSummaryModel> getCurrentFYDetails(String subArea);
    List<RetailerSalesSummaryModel> getLastYearFYDetails(String subArea);
    List<RetailerSalesSummaryModel> getLastYearSameMonthDetails(String subArea);
}
