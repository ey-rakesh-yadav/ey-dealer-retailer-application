package com.scl.core.services;

import com.scl.facades.data.*;
import com.scl.core.model.SclUserModel;

import java.util.List;

public interface TSOSiteManagementService {
    String submitMaterialTest(QualityTestListData qualityTestData);

    String submitConcreteTest(QualityTestListData qualityTestData);

    String submitOtherTest(QualityTestListData qualityTestData);
    
    SiteDemonstrationListData getAllSiteDemonstrations(SclUserModel tsoUser);

    DropdownListData getSiteDemoStages(String demoName);
    
    SiteAnnualBudgetListData getSiteAnnualBudgets(SclUserModel tsoUserCode);
    
    QualityTestReportListData getQualityTestReports(SclUserModel tsoUserCode);

    List<SiteIdData> submitSiteDemonstration(SiteDemonstrationListData siteDemonstrationListData);

    boolean submitSalesFromTradeSite(SalesFromTradeSiteListData salesFromTradeSiteListData);

    boolean submitSiteVisitForm(TSOSiteVisitFormData siteVisitFormData);

    boolean submitAnnualBudgetSite(SiteAnnualBudgetData siteAnnualBudgetData);

    boolean submitSiteFeedback(SiteFeedbackListData siteFeedbackData);
}
