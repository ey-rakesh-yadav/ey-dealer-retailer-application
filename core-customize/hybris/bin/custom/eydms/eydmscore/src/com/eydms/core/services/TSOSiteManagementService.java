package com.eydms.core.services;

import com.eydms.facades.data.*;
import com.eydms.core.model.EyDmsUserModel;

import java.util.List;

public interface TSOSiteManagementService {
    String submitMaterialTest(QualityTestListData qualityTestData);

    String submitConcreteTest(QualityTestListData qualityTestData);

    String submitOtherTest(QualityTestListData qualityTestData);
    
    SiteDemonstrationListData getAllSiteDemonstrations(EyDmsUserModel tsoUser);

    DropdownListData getSiteDemoStages(String demoName);
    
    SiteAnnualBudgetListData getSiteAnnualBudgets(EyDmsUserModel tsoUserCode);
    
    QualityTestReportListData getQualityTestReports(EyDmsUserModel tsoUserCode);

    List<SiteIdData> submitSiteDemonstration(SiteDemonstrationListData siteDemonstrationListData);

    boolean submitSalesFromTradeSite(SalesFromTradeSiteListData salesFromTradeSiteListData);

    boolean submitSiteVisitForm(TSOSiteVisitFormData siteVisitFormData);

    boolean submitAnnualBudgetSite(SiteAnnualBudgetData siteAnnualBudgetData);

    boolean submitSiteFeedback(SiteFeedbackListData siteFeedbackData);
}
