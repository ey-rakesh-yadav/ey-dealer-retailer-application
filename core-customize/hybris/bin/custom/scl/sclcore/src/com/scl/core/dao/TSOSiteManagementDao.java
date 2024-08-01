package com.scl.core.dao;

import java.util.List;

import com.scl.core.model.SclUserModel;
import com.scl.core.model.SiteAnnualBudgetModel;
import com.scl.core.model.SiteDemonstrationModel;
import com.scl.core.model.SiteQualityTestModel;

public interface TSOSiteManagementDao {
	
	List<SiteDemonstrationModel> getAllSiteDemos(SclUserModel sclTsoUser);
	
	List<SiteAnnualBudgetModel> getAllSiteBudgets(SclUserModel sclTsoUser);
	
	List<SiteQualityTestModel> getSiteQualityTestReports(SclUserModel sclTsoUser);
}
