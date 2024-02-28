package com.eydms.core.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import com.eydms.core.model.SiteDemonstrationModel;
import com.eydms.core.model.SiteQualityTestModel;
import com.eydms.core.dao.TSOSiteManagementDao;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.TsmDistrictMappingModel;
import com.eydms.core.model.SiteAnnualBudgetModel;
import com.eydms.core.model.SiteDemonstrationModel;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class TSOSiteManagementDaoImpl implements TSOSiteManagementDao {
	
	@Autowired
	FlexibleSearchService flexibleSearchService;
	
	@Autowired
	UserService userService;
	
	@Resource
	BaseSiteService baseSiteService;

	@Override
	public List<SiteDemonstrationModel> getAllSiteDemos(EyDmsUserModel eydmsTsoUser){
		List<SiteDemonstrationModel> siteDemos = new ArrayList<SiteDemonstrationModel>();
			
		final Map<String, Object> params = new HashMap<String, Object>();
		EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder();
		builder.append("select {sd:pk} from SiteDemonstration as sd " +
				" where {sd:tsoUser} = ?tsoUser and {sd:brand} = ?brand");
		
		params.put("tsoUser", eydmsTsoUser);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SiteDemonstrationModel.class));
		query.addQueryParameters(params);
		final SearchResult<SiteDemonstrationModel> searchResult = flexibleSearchService.search(query);
		siteDemos = searchResult.getResult();
		
		return (siteDemos!=null && !siteDemos.isEmpty()) ? siteDemos : Collections.emptyList();
	}
	
	@Override
	public List<SiteAnnualBudgetModel> getAllSiteBudgets(EyDmsUserModel eydmsTsoUser){
		List<SiteAnnualBudgetModel> siteBudgets = new ArrayList<SiteAnnualBudgetModel>();
			
		final Map<String, Object> params = new HashMap<String, Object>();
		EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder();
		builder.append("select {sab:pk} from SiteAnnualBudget as sab " +
				" where {sab:tsoUser} = ?tsoUser and {sab:brand} = ?brand");
		
		params.put("tsoUser", eydmsTsoUser);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SiteAnnualBudgetModel.class));
		query.addQueryParameters(params);
		final SearchResult<SiteAnnualBudgetModel> searchResult = flexibleSearchService.search(query);
		siteBudgets = searchResult.getResult();
		
		return (siteBudgets!=null && !siteBudgets.isEmpty()) ? siteBudgets : Collections.emptyList();
	}
	
	
	@Override
	public List<SiteQualityTestModel> getSiteQualityTestReports(EyDmsUserModel eydmsTsoUser){
		List<SiteQualityTestModel> siteQualityTests = new ArrayList<SiteQualityTestModel>();
			
		final Map<String, Object> params = new HashMap<String, Object>();
		EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
		final StringBuilder builder = new StringBuilder();
		builder.append("select {sqt:pk} from SiteQualityTest as sqt " +
				" where {sqt:tsoUser} = ?tsoUser and {sqt:brand} = ?brand");
		
		params.put("tsoUser", eydmsTsoUser);
		params.put("brand", baseSiteService.getCurrentBaseSite());
		FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SiteQualityTestModel.class));
		query.addQueryParameters(params);
		final SearchResult<SiteQualityTestModel> searchResult = flexibleSearchService.search(query);
		siteQualityTests = searchResult.getResult();
		
		return (siteQualityTests!=null && !siteQualityTests.isEmpty()) ? siteQualityTests : Collections.emptyList();
	}
}
