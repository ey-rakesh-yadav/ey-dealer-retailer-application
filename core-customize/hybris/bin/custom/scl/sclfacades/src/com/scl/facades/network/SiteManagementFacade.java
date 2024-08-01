package com.scl.facades.network;

import com.scl.facades.data.*;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SiteManagementFacade {

    DropdownListData getSiteServiceType();

    DropdownListData getSiteServiceTest(String serviceTypeCode);

    DropdownListData getSiteCategoryType();

    DropdownListData getSiteCementType(String siteCategoryType);

    DropdownListData getSiteCementBrand(String siteCementType);

    MapNewSiteData mapNewSite(MapNewSiteData siteData);

    SiteManagementHomePageData getTotalAndActualTargetForSiteVisit(String filter);

	VisitMasterData createAndStartComplaintVisit(String siteId, String requestId);

    SearchPageData<SclSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday, List<String> filterBySubAreas);
    SclSiteMasterData toCloseTheSite(String siteId,String closeComment);

    MapNewSiteData getSiteDetailsById(String siteId);

    SiteManagementHomePageData getTotalPremiumOfSitesAndBags();


    Boolean addTaggedInfluencersForSite(List<String> influencer, String site) throws DuplicateUidException;

	VisitMasterData endComplaintVisit(String visitId);

    List<String> getSiteMasterListTaluka();
}
