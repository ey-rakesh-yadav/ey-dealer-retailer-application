package com.scl.facades.impl;

import com.scl.core.model.SclUserModel;

import com.scl.core.services.TSOSiteManagementService;
import com.scl.facades.TSOSiteManagementFacade;
import com.scl.facades.data.*;
import de.hybris.platform.servicelayer.user.UserService;
import javax.annotation.Resource;
import java.util.List;

public class TSOSiteManagementFacadeImpl implements TSOSiteManagementFacade {

    @Resource
    UserService userService;

    @Resource
    TSOSiteManagementService tsoSiteManagementService;

    @Override
    public String submitMaterialTest(QualityTestListData qualityTestListData) {
        return tsoSiteManagementService.submitMaterialTest(qualityTestListData);
    }

    @Override
    public String submitConcreteTest(QualityTestListData qualityTestListData) {
        return tsoSiteManagementService.submitConcreteTest(qualityTestListData);
    }

    @Override
    public String submitOtherTest(QualityTestListData qualityTestListData) {
        return tsoSiteManagementService.submitOtherTest(qualityTestListData);
    }

    @Override
    public boolean submitSalesFromTradeSite(SalesFromTradeSiteListData salesFromTradeSiteListData) {
        return tsoSiteManagementService.submitSalesFromTradeSite(salesFromTradeSiteListData);
    }

    @Override
    public List<SiteIdData> submitSiteDemonstration(SiteDemonstrationListData siteDemonstrationListData) {
        return tsoSiteManagementService.submitSiteDemonstration(siteDemonstrationListData);
    }

    @Override
    public boolean submitSiteVisitForm(TSOSiteVisitFormData siteVisitFormData) {
       return tsoSiteManagementService.submitSiteVisitForm(siteVisitFormData);
    }

    @Override
    public SiteDemonstrationListData getAllSiteDemonstrations(String tsoUserCode) {
    	SclUserModel tsoUser = (SclUserModel) userService.getUserForUID(tsoUserCode);
    	return tsoSiteManagementService.getAllSiteDemonstrations(tsoUser);
    }
    
    public DropdownListData getSiteDemoStages(String demoName) {
        return tsoSiteManagementService.getSiteDemoStages(demoName);
    }

    @Override
    public boolean submitAnnualBudgetSite(SiteAnnualBudgetData siteAnnualBudgetData) {
        return tsoSiteManagementService.submitAnnualBudgetSite(siteAnnualBudgetData);
    }

    @Override
    public SiteAnnualBudgetListData getSiteAnnualBudgets(String tsoUserCode) {
    	SclUserModel tsoUser = (SclUserModel) userService.getUserForUID(tsoUserCode);
    	return tsoSiteManagementService.getSiteAnnualBudgets(tsoUser);
    }
    
    @Override
    public QualityTestReportListData getQualityTestReports(String tsoUserCode){
        SclUserModel tsoUser = (SclUserModel) userService.getUserForUID(tsoUserCode);
    	return tsoSiteManagementService.getQualityTestReports(tsoUser);
    }
    
    @Override    
    public boolean submitSiteFeedback(SiteFeedbackListData siteFeedbackData) {
       return tsoSiteManagementService.submitSiteFeedback(siteFeedbackData);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public TSOSiteManagementService getTsoSiteManagementService() {
        return tsoSiteManagementService;
    }

    public void setTsoSiteManagementService(TSOSiteManagementService tsoSiteManagementService) {
        this.tsoSiteManagementService = tsoSiteManagementService;
    }
}
