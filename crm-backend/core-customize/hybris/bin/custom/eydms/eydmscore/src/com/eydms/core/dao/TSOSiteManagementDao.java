package com.eydms.core.dao;

import java.util.List;

import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SiteAnnualBudgetModel;
import com.eydms.core.model.SiteDemonstrationModel;
import com.eydms.core.model.SiteQualityTestModel;

public interface TSOSiteManagementDao {
	
	List<SiteDemonstrationModel> getAllSiteDemos(EyDmsUserModel eydmsTsoUser);
	
	List<SiteAnnualBudgetModel> getAllSiteBudgets(EyDmsUserModel eydmsTsoUser);
	
	List<SiteQualityTestModel> getSiteQualityTestReports(EyDmsUserModel eydmsTsoUser);
}
