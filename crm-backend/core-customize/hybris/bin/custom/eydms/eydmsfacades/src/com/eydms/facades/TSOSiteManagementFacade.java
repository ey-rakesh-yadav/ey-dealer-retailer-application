package com.eydms.facades;

import com.eydms.facades.data.*;

import java.util.List;

public interface TSOSiteManagementFacade {
    String submitMaterialTest(QualityTestListData qualityTestData);
    String submitConcreteTest(QualityTestListData concreteTestData);

    String submitOtherTest(QualityTestListData otherTestData);

    boolean submitSalesFromTradeSite(SalesFromTradeSiteListData salesFromTradeSiteListData);

    List<SiteIdData> submitSiteDemonstration(SiteDemonstrationListData siteDemonstrationListData);

    boolean submitSiteVisitForm(TSOSiteVisitFormData siteVisitFormData);
    
    SiteDemonstrationListData getAllSiteDemonstrations(String tsoUserCode);

    DropdownListData getSiteDemoStages(String demoName);

    boolean submitAnnualBudgetSite(SiteAnnualBudgetData siteAnnualBudgetData);

    boolean submitSiteFeedback(SiteFeedbackListData siteFeedbackData);

    SiteAnnualBudgetListData getSiteAnnualBudgets(String tsoUserCode);
    
    QualityTestReportListData getQualityTestReports(String tsoUser);
    
}
