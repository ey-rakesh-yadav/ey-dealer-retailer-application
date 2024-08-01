package com.scl.occ.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scl.facades.data.*;
import com.scl.facades.djp.data.AddNewSiteData;
import com.scl.facades.network.SiteManagementFacade;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.DropdownListWsDTO;
import com.scl.occ.dto.SclSiteMasterListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.util.Config;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.log4j.Logger;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/tso/sclSiteManagementController")
@ApiVersion("v2")
@Tag(name = "SCL Site Management Controller")
public class SiteManagementController extends SclBaseController{

    private static final Logger LOG = LogManager.getLogger(SiteManagementController.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MAX_PAGE_SIZE_KEY = "webservicescommons.pagination.maxPageSize";

    @Autowired
    SiteManagementFacade siteManagementFacade;

    @Autowired
    WebPaginationUtils webPaginationUtils;

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteServiceType", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public DropdownListWsDTO getSiteServiceType()
    {
        return getDataMapper().map(siteManagementFacade.getSiteServiceType(),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteServiceTest", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public DropdownListWsDTO getSiteServiceTest(@RequestParam final String serviceTypeCode)
    {
        return getDataMapper().map(siteManagementFacade.getSiteServiceTest(serviceTypeCode),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteCategoryType", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public DropdownListWsDTO getSiteCategoryType()
    {
        return getDataMapper().map(siteManagementFacade.getSiteCategoryType(),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteCementType", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public DropdownListWsDTO getSiteCementType(@RequestParam final String siteCategoryType)
    {
        return getDataMapper().map(siteManagementFacade.getSiteCementType(siteCategoryType),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteCementBrand", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public DropdownListWsDTO getSiteCementBrand(@RequestParam final String siteCementType)
    {
        return getDataMapper().map(siteManagementFacade.getSiteCementBrand(siteCementType),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/mapNewSite", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public MapNewSiteData mapNewSite(@RequestBody MapNewSiteData siteData) {
        try {
            String json = objectMapper.writeValueAsString(siteData);
            LOG.info("Site Data : " + json);
        } catch (Exception e) {
            LOG.error("Error occurred while Site Data object", e);
        }
        return siteManagementFacade.mapNewSite(siteData);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTotalAndActualTargetForSiteVisit", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public SiteManagementHomePageData getTotalAndActualTargetForSiteVisit( @RequestParam(required = false) String filter) {
        return siteManagementFacade.getTotalAndActualTargetForSiteVisit(filter);
    }

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/startComplaintVisit", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public VisitMasterData createAndStartComplaintVisit(@RequestParam String siteId, @RequestParam(required = false) String requestId) {
		return siteManagementFacade.createAndStartComplaintVisit(siteId, requestId);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/endComplaintVisit", method = RequestMethod.POST)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public VisitMasterData endComplaintVisit(@RequestParam String visitId) {
		return siteManagementFacade.endComplaintVisit(visitId);
	}
	
    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTotalPremiumOfSitesAndBags", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public SiteManagementHomePageData getTotalPremiumOfSitesAndBags() {
        return siteManagementFacade.getTotalPremiumOfSitesAndBags();
    }

    @RequestMapping(value = "/{userId}/addTaggedInfluencersForSite", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean addTaggedInfluencersForSite(@RequestParam(required = true) List<String> influencer, @RequestParam(required = true) String site) throws DuplicateUidException {
        return siteManagementFacade.addTaggedInfluencersForSite(influencer,site);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteMasterList", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public SclSiteMasterListWsDTO getSiteMasterList(@RequestParam(name = "currentPage", required = false, defaultValue = "0") final int currentPage,
                                                   @RequestParam(name = "pageSize", required = false, defaultValue = "10") final int pageSize,
                                                   @RequestParam(name = "sort", defaultValue = "lastvisittime:desc") final String sort,
                                                   @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
                                                    @RequestParam(required = false) final Boolean plannedVisitForToday,
                                                   @RequestParam(name = "filterBySubAreas", required = false) final List<String> filterBySubAreas,
                                                   @RequestParam(defaultValue = "DEFAULT") final String fields,final HttpServletResponse response,
                                                    @RequestBody SiteRequestData siteRequestData
                                                    ) {

        final SearchPageData searchPageData = webPaginationUtils.buildSearchPageData(sort, currentPage, pageSize, needsTotal);
        recalculatePageSize(searchPageData);

        SclSiteMasterListData sclSiteMasterListData = new SclSiteMasterListData();
        SearchPageData<SclSiteMasterData> paginatedSiteMasterList = siteManagementFacade.getPaginatedSiteMasterList(searchPageData,siteRequestData,plannedVisitForToday,filterBySubAreas);
        sclSiteMasterListData.setSclSiteMasterDataList(paginatedSiteMasterList.getResults());
        if (paginatedSiteMasterList.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(paginatedSiteMasterList.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(sclSiteMasterListData, SclSiteMasterListWsDTO.class, fields);
    }

    protected void recalculatePageSize(final SearchPageData searchPageData)
    {
        int pageSize = searchPageData.getPagination().getPageSize();
        if (pageSize <= 0)
        {
            final int maxPageSize = Config.getInt(MAX_PAGE_SIZE_KEY, 1000);
            pageSize = webPaginationUtils.getDefaultPageSize();
            pageSize = pageSize > maxPageSize ? maxPageSize : pageSize;
            searchPageData.getPagination().setPageSize(pageSize);
        }
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value = "/toCloseTheSite", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public SclSiteMasterData toCloseTheSite(@RequestParam String siteId, @RequestParam String closeComment)
    {
        return siteManagementFacade.toCloseTheSite(siteId,closeComment);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value = "/getSiteDetailsById", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public MapNewSiteData getSiteDetailsById(@RequestParam String siteId)
    {
        return siteManagementFacade.getSiteDetailsById(siteId);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getSiteMasterListTaluka", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public List<String> getSiteMasterListTaluka() {
          return siteManagementFacade.getSiteMasterListTaluka();
    }
}
