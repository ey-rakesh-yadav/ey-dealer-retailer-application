package com.scl.core.services.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.TSOSiteManagementDao;
import com.scl.core.enums.ConcreteTestName;
import com.scl.core.enums.MaterialTestName;
import com.scl.core.enums.MaterialTestSubCategory;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.services.TSOSiteManagementService;
import com.scl.facades.data.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TSOSiteManagementServiceImpl implements TSOSiteManagementService {

    private static final Logger LOG = Logger.getLogger(TSOSiteManagementServiceImpl.class);

    @Resource
    private KeyGenerator materialTestIdGenerator;

    @Resource
    private KeyGenerator concreteTestIdGenerator;

    @Resource
    private KeyGenerator otherTestIdGenerator;

    @Resource
    private KeyGenerator siteDemoIdGenerator;

    @Resource
    ModelService modelService;

    @Resource
    UserService userService;

    @Resource
    TSOSiteManagementDao tsoSiteManagementDao;
    
    @Resource
    EnumerationService enumerationService;

    @Resource
    I18NService i18NService;

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    MediaService mediaService;

    @Resource
    BaseSiteService baseSiteService;

    @Resource
    ProductService productService;

    @Resource
    CatalogVersionService catalogVersionService;

    @Autowired
    Populator<AddressData, AddressModel> addressReversePopulator;

    @Resource
    CustomerAccountService customerAccountService;

    @Override
    public String submitMaterialTest(QualityTestListData qualityTestListData) {
        SclUserModel tso=(SclUserModel) userService.getCurrentUser();
        String materialTestId="";
        SiteQualityTestModel siteQualityTestModel=null;
        if(Objects.nonNull(qualityTestListData)) {
            SclCustomerModel site = (SclCustomerModel) userService.getUserForUID(qualityTestListData.getSiteCode());
            siteQualityTestModel=modelService.create(SiteQualityTestModel.class);
            siteQualityTestModel.setSite(site);
            siteQualityTestModel.setTsoUser(tso);
            if (qualityTestListData.getQualityTests() != null && !qualityTestListData.getQualityTests().isEmpty())
            {
                for (QualityTestData qualityTestData : qualityTestListData.getQualityTests()) {
                    siteQualityTestModel.setMaterialTestId(String.valueOf(materialTestIdGenerator.generate()));
                    if (SiteQualityTestType.MATERIAL.getCode().equalsIgnoreCase(qualityTestData.getQualityTestType())) {
                        siteQualityTestModel.setQualityTestType(SiteQualityTestType.MATERIAL);
                    }
                    siteQualityTestModel.setSiteLocation(qualityTestData.getSiteLocation());
                    siteQualityTestModel.setSiteLocationDetails(qualityTestData.getLocationDetails());
                    if(qualityTestData.getMaterialTestSubCategory()!=null)
                        siteQualityTestModel.setMaterialTestSubCategory(MaterialTestSubCategory.valueOf(qualityTestData.getMaterialTestSubCategory()));
                    if(qualityTestData.getMaterialTestName()!=null)
                        siteQualityTestModel.setMaterialTestName(MaterialTestName.valueOf(qualityTestData.getMaterialTestName()));
                    siteQualityTestModel.setAcceptableRangeResult(qualityTestData.getAcceptableRangeResult());
                    siteQualityTestModel.setCommentsForTest(qualityTestData.getComments());
                    siteQualityTestModel.setQualityTestDate(new Date());
                    if(qualityTestData.getImage() != null) {
                        CatalogUnawareMediaModel qualityTestImage = getMaterialTestImage(qualityTestData.getImage());
                        siteQualityTestModel.setQualityTestImage(qualityTestImage);
                    }
                    modelService.save(siteQualityTestModel);
                }
        }
        }
        if (Objects.nonNull(siteQualityTestModel)) {
            materialTestId=siteQualityTestModel.getMaterialTestId();
            return materialTestId;
        }
        return materialTestId;
    }

    @Override
    public String submitConcreteTest(QualityTestListData qualityTestListData) {
        SclUserModel tso=(SclUserModel) userService.getCurrentUser();
        String concreteTestId="";
        SiteQualityTestModel siteQualityTestModel=null;
        if(Objects.nonNull(qualityTestListData)) {
           SclCustomerModel site = (SclCustomerModel) userService.getUserForUID(qualityTestListData.getSiteCode());
           siteQualityTestModel = modelService.create(SiteQualityTestModel.class);
            siteQualityTestModel.setSite(site);
            siteQualityTestModel.setTsoUser(tso);
            if(qualityTestListData.getQualityTests()!=null && !qualityTestListData.getQualityTests().isEmpty()) {
                for (QualityTestData qualityTestData : qualityTestListData.getQualityTests()) {
                    siteQualityTestModel.setConcreteTestId(String.valueOf(concreteTestIdGenerator.generate()));
                    if (SiteQualityTestType.CONCRETE.getCode().equalsIgnoreCase(qualityTestData.getQualityTestType())) {
                        siteQualityTestModel.setQualityTestType(SiteQualityTestType.CONCRETE);
                    }
                    siteQualityTestModel.setSiteLocation(qualityTestData.getSiteLocation());
                    siteQualityTestModel.setSiteLocationDetails(qualityTestData.getLocationDetails());
                    if(qualityTestData.getConcreteTestName()!=null)
                        siteQualityTestModel.setConcreteTestName(ConcreteTestName.valueOf(qualityTestData.getConcreteTestName()));
                    siteQualityTestModel.setAcceptableRangeResult(qualityTestData.getAcceptableRangeResult());
                    siteQualityTestModel.setCommentsForTest(qualityTestData.getComments());
                    if(qualityTestData.getGrade()!=null)
                        siteQualityTestModel.setGrade(GradeOfConcrete.valueOf(qualityTestData.getGrade()));
                    if(qualityTestData.getNormalMixRatio()!=null)
                        siteQualityTestModel.setNormalMixRatio(NormalMixRatio.valueOf(qualityTestData.getNormalMixRatio()));

                    siteQualityTestModel.setQualityTestDate(new Date());
                    if(qualityTestData.getImage() != null) {
                        CatalogUnawareMediaModel qualityTestImage = getConcreteTestImage(qualityTestData.getImage());
                        siteQualityTestModel.setQualityTestImage(qualityTestImage);
                    }
                    modelService.save(siteQualityTestModel);
                }
            }
        }

        if (Objects.nonNull(siteQualityTestModel)) {
            concreteTestId=siteQualityTestModel.getConcreteTestId();
            return concreteTestId;
        }
        return concreteTestId;
    }

    @Override
    public String submitOtherTest(QualityTestListData qualityTestListData) {
        SclUserModel tso=(SclUserModel) userService.getCurrentUser();
        String otherTestId="";
        SiteQualityTestModel siteQualityTestModel=null;
        if(Objects.nonNull(qualityTestListData)) {
            SclCustomerModel site = (SclCustomerModel) userService.getUserForUID(qualityTestListData.getSiteCode());
            siteQualityTestModel=modelService.create(SiteQualityTestModel.class);
            siteQualityTestModel.setSite(site);
            siteQualityTestModel.setTsoUser(tso);

            if (qualityTestListData.getQualityTests() != null && !qualityTestListData.getQualityTests().isEmpty()) {
                for (QualityTestData qualityTestData : qualityTestListData.getQualityTests()) {
                    if (SiteQualityTestType.OTHER.getCode().equalsIgnoreCase(qualityTestData.getQualityTestType())) {
                        siteQualityTestModel.setQualityTestType(SiteQualityTestType.OTHER);
                    }
                    siteQualityTestModel.setOtherTestId(String.valueOf(otherTestIdGenerator.generate()));
                    siteQualityTestModel.setOtherTestName(qualityTestData.getOtherTestName());
                    siteQualityTestModel.setAcceptableRangeResult(qualityTestData.getAcceptableRangeResult());
                    siteQualityTestModel.setCommentsForTest(qualityTestData.getComments());
                    siteQualityTestModel.setQualityTestDate(new Date());
                    if(qualityTestData.getImage() != null) {
                        CatalogUnawareMediaModel qualityTestImage = getOtherTestImage(qualityTestData.getImage());
                        siteQualityTestModel.setQualityTestImage(qualityTestImage);
                    }
                    modelService.save(siteQualityTestModel);
                }
            }
        }

        if (Objects.nonNull(siteQualityTestModel)) {
            otherTestId=siteQualityTestModel.getOtherTestId();
            return otherTestId;
        }
        return otherTestId;
    }
    
    @Override
    public SiteDemonstrationListData getAllSiteDemonstrations(SclUserModel tsoUser) {
    	SiteDemonstrationListData allSiteDemos = new SiteDemonstrationListData();
    	List<SiteDemonstrationData> listSiteDemos = new ArrayList<SiteDemonstrationData>();
    	//Fetch all the Demo Sites for TSO
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    	List<SiteDemonstrationModel> siteDemosModel = tsoSiteManagementDao.getAllSiteDemos(tsoUser);
        
		for (SiteDemonstrationModel siteDemonstrationModel : siteDemosModel) {
			SiteDemonstrationData siteDemoData = new SiteDemonstrationData();
			siteDemoData.setDemoName(siteDemonstrationModel.getDemoName().getCode());
			siteDemoData.setDemoId(siteDemonstrationModel.getId());
            if (siteDemonstrationModel.getSiteDemoDate() != null) {
                siteDemoData.setSiteDemoDate(dateFormat.format(siteDemonstrationModel.getSiteDemoDate()));
            }
            //siteDemoData.setSiteDemoTime();
			siteDemoData.setComments(siteDemonstrationModel.getCommentsForDemo());
			listSiteDemos.add(siteDemoData);
		}
		allSiteDemos.setDemoList(listSiteDemos);
        return allSiteDemos;
    }
    
    @Override
    public SiteAnnualBudgetListData getSiteAnnualBudgets(SclUserModel tsoUser) {
    	SiteAnnualBudgetListData allSiteBudgets = new SiteAnnualBudgetListData();
    	List<SiteAnnualBudgetData> listSiteBudgets = new ArrayList<SiteAnnualBudgetData>();
    	//Fetch all the Budget for TSO
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    	List<SiteAnnualBudgetModel> siteBudgetsModel = tsoSiteManagementDao.getAllSiteBudgets(tsoUser);
        
		for (SiteAnnualBudgetModel siteBudgetModel : siteBudgetsModel) {
			SiteAnnualBudgetData siteBudgetData = new SiteAnnualBudgetData();

			siteBudgetData.setSiteName(siteBudgetModel.getSite().getName());
			siteBudgetData.setSiteCode(siteBudgetModel.getSite().getOriginalUid());
			siteBudgetData.setTaskDone(siteBudgetModel.getTaskDone());
			siteBudgetData.setBudgetConsumed(siteBudgetModel.getBudgetConsumed());
            if (siteBudgetModel.getSiteVisitDate() != null) {
                siteBudgetData.setSiteVisitDate(dateFormat.format(siteBudgetModel.getSiteVisitDate()));
            }
			
			listSiteBudgets.add(siteBudgetData);
		}
		allSiteBudgets.setBudgetList(listSiteBudgets);
        return allSiteBudgets;

    }
    
    @Override
    public QualityTestReportListData getQualityTestReports(SclUserModel tsoUser) {
    	QualityTestReportListData allSiteBudgets = new QualityTestReportListData();
    	List<QualityTestReportData> listSiteQualityReports = new ArrayList<QualityTestReportData>();
    	//Fetch all the Budget for TSO
    	List<SiteQualityTestModel> siteQualityTestReportsModel = tsoSiteManagementDao.getSiteQualityTestReports(tsoUser);
        
		for (SiteQualityTestModel siteQualityTestModel : siteQualityTestReportsModel) {
			QualityTestReportData siteQualityTestData = new QualityTestReportData();

			siteQualityTestData.setSiteName(siteQualityTestModel.getSite().getName());
			siteQualityTestData.setSiteCode(siteQualityTestModel.getSite().getOriginalUid());
			siteQualityTestData.setQualityTestName(siteQualityTestModel.getQualityTestName());
			siteQualityTestData.setQualityTestDate(siteQualityTestModel.getQualityTestDate());
			siteQualityTestData.setIsQualityTestReport(siteQualityTestModel.getIsQualityTestReport());
			siteQualityTestData.setQualityTestReport(siteQualityTestModel.getQualityTestReport());
			siteQualityTestData.setQualityTestType(siteQualityTestModel.getQualityTestType().getType());
			
			listSiteQualityReports.add(siteQualityTestData);
		}
		allSiteBudgets.setQualityTestReportListData(listSiteQualityReports);
        return allSiteBudgets;

    }
    
    @Override
    public DropdownListData getSiteDemoStages(String demoName) {
            DropdownListData data=new DropdownListData();
            //recheck
            if(demoName.equals(SiteDemoName.PRODUCT_KNOWLEDGE_PDF.getCode())){
                List<StageOfConstruction> pos = enumerationService.getEnumerationValues(StageOfConstruction.class);
                pos.set(0, StageOfConstruction.EXCAVATION);
                pos.set(1, StageOfConstruction.FOUNDATION);
                pos.set(2, StageOfConstruction.PLINTH);
                pos.set(6, StageOfConstruction.FLOORING);
                List<DropdownData> dataList = pos.stream().map(this::getDropdownData).collect(Collectors.toList());
                data.setDropdown(dataList);
            }
            else  if(demoName.equals(SiteDemoName.BRICK_MORTAR.getCode())){
                List<StageOfConstruction> pos = enumerationService.getEnumerationValues(StageOfConstruction.class);
                pos.set(3, StageOfConstruction.BRICK_WORK);
                List<DropdownData> dataList = pos.stream().map(this::getDropdownData).collect(Collectors.toList());
                data.setDropdown(dataList);
            }
            else  if(demoName.equals(SiteDemoName.SLAB_CASTING_SERVICE.getCode())){
                List<StageOfConstruction> pos = enumerationService.getEnumerationValues(StageOfConstruction.class);
                pos.set(4, StageOfConstruction.SLAB_LEVEL);
                List<DropdownData> dataList = pos.stream().map(this::getDropdownData).collect(Collectors.toList());
                data.setDropdown(dataList);
            }
            else  if(demoName.equals(SiteDemoName.PLASTERING.getCode())){
                List<StageOfConstruction> pos = enumerationService.getEnumerationValues(StageOfConstruction.class);
                pos.set(5, StageOfConstruction.PLASTERING);
                List<DropdownData> dataList = pos.stream().map(this::getDropdownData).collect(Collectors.toList());
                data.setDropdown(dataList);
            }
            else
            {
                LOG.error("No Data is provided for this type");
            }
            return data;
    }

    @Override
    public List<SiteIdData> submitSiteDemonstration(SiteDemonstrationListData siteDemonstrationListData) {
        List<SiteIdData> siteIdDataList = new ArrayList<>();
        SiteDemonstrationModel siteDemonstrationModel =null;
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        if(sclUser.getUserType().getCode().equalsIgnoreCase(SclUserType.TSO.getCode())) {
            if(siteDemonstrationListData!=null)
            {
                if(siteDemonstrationListData.getDemoList()!=null && !siteDemonstrationListData.getDemoList().isEmpty())
                    for (SiteDemonstrationData siteDemonstrationData : siteDemonstrationListData.getDemoList()) {
                        siteDemonstrationModel=modelService.create(SiteDemonstrationModel.class);
                        SclCustomerModel site = (SclCustomerModel) userService.getUserForUID(siteDemonstrationListData.getSiteCode());
                        siteDemonstrationModel.setId(String.valueOf(siteDemoIdGenerator.generate()));
                        siteDemonstrationModel.setSite(site);
                        siteDemonstrationModel.setDemoName(SiteDemoName.valueOf(siteDemonstrationData.getDemoName()));
                        siteDemonstrationModel.setCommentsForDemo(siteDemonstrationData.getComments());
                        siteDemonstrationModel.setStageOfConstruction(StageOfConstruction.valueOf(siteDemonstrationData.getStageOfConstruction()));
                        siteDemonstrationModel.setTsoUser(sclUser);
                        siteDemonstrationModel.setBrand(currentBaseSite);
                        siteDemonstrationModel.setSiteDemoDate(new Date());
                        if(siteDemonstrationData.getImage() != null) {
                            CatalogUnawareMediaModel demoImage = getSiteDemoImage(siteDemonstrationData.getImage());
                            siteDemonstrationModel.setDemoImage(demoImage);
                        }
                        modelService.save(siteDemonstrationModel);

                        if(Objects.nonNull(siteDemonstrationModel)) {
                            SiteIdData siteIdData = new SiteIdData();
                            siteIdData.setId(siteDemonstrationModel.getId());
                            siteIdData.setName(String.valueOf(siteDemonstrationModel.getDemoName()));
                            siteIdDataList.add(siteIdData);
                        }
                    }
            }
        }

        return siteIdDataList;
    }

    @Override
    public boolean submitSalesFromTradeSite(SalesFromTradeSiteListData salesFromTradeSiteListData) {
        SclUserModel sclUser= (SclUserModel) userService.getCurrentUser();
        BaseSiteModel baseSite= baseSiteService.getCurrentBaseSite();
        if(sclUser.getUserType().getCode().equalsIgnoreCase(SclUserType.TSO.getCode())) {

            if (salesFromTradeSiteListData != null) {
                if (salesFromTradeSiteListData.getSalesFromTradeSiteList() != null && salesFromTradeSiteListData.getSalesFromTradeSiteList().isEmpty()) {
                    TradeSiteSalesModel tradeSiteSalesModel = modelService.create(TradeSiteSalesModel.class);
                    SclCustomerModel site = (SclCustomerModel) userService.getUserForUID(salesFromTradeSiteListData.getSiteCode());
                    tradeSiteSalesModel.setSite(site);
                    tradeSiteSalesModel.setBrand(baseSite);
                    tradeSiteSalesModel.setTsoUser(sclUser);
                    for (SalesFromTradeSiteData salesFromTradeSiteData : salesFromTradeSiteListData.getSalesFromTradeSiteList()) {
                        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                        ProductModel product = productService.getProductForCode(catalogVersion, salesFromTradeSiteData.getProductCode());
                        tradeSiteSalesModel.setProduct(product);
                        tradeSiteSalesModel.setPackging(salesFromTradeSiteData.getPackaging());

                        tradeSiteSalesModel.setBrandForPurchase(salesFromTradeSiteData.getBrand());
                        tradeSiteSalesModel.setPurchaseType(salesFromTradeSiteData.getPurchaseType());
                        SclCustomerModel sclCustomer = (SclCustomerModel) userService.getUserForUID(salesFromTradeSiteData.getCustomerCode());
                        tradeSiteSalesModel.setCustomer(sclCustomer);
                        tradeSiteSalesModel.setQtyPurchased(salesFromTradeSiteData.getQtyPurchased());
                        tradeSiteSalesModel.setUnit(salesFromTradeSiteData.getUnit());
                        tradeSiteSalesModel.setPurchaseDate(new Date());
                        modelService.save(tradeSiteSalesModel);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean submitSiteVisitForm(TSOSiteVisitFormData siteVisitFormData) {
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        String state = StringUtils.EMPTY;
        String district = StringUtils.EMPTY;
        String taluka = StringUtils.EMPTY;
        SclCustomerModel site = null;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        if(sclUser.getUserType()!=null && sclUser.getUserType().getCode().equalsIgnoreCase(SclUserType.TSO.getCode())) {
            site = (SclCustomerModel) userService.getUserForUID(siteVisitFormData.getSiteCode());
            site.setSiteStage(SiteStage.valueOf(siteVisitFormData.getSiteStage()));
            site.setPurposeOfVisit(siteVisitFormData.getPurposeOfVisit());
            site.setContactPersonName(siteVisitFormData.getContactedPersonName());
            site.setContactPersonNumber(siteVisitFormData.getContactedPersonNumber());
            site.setContactPersonEmail(siteVisitFormData.getContactedPersonEmail());
            site.setContactPersonRole(siteVisitFormData.getContactedPersonRole());
            site.setSiteDescription(siteVisitFormData.getSiteDescription());
            if(siteVisitFormData.getAddress()!=null) {
                AddressModel newAddress = modelService.create(AddressModel.class);
                addressReversePopulator.populate(siteVisitFormData.getAddress(), newAddress);
                newAddress.setBillingAddress(true);
                newAddress.setDuplicate(true);
                newAddress.setShippingAddress(false);
                newAddress.setIsPrimaryAddress(false);
                newAddress.setVisibleInAddressBook(false);
                customerAccountService.saveAddressEntry(site, newAddress);
            }
            site.setSinglePOCName(siteVisitFormData.getSinglePocName());
            site.setSinglePOCEmail(siteVisitFormData.getSinglePocEmail());
            site.setSinglePOCNumber(siteVisitFormData.getSinglePocNumber());
            site.setSinglePOCRole(siteVisitFormData.getSinglePocRole());
            site.setSiteProjectConsultant(siteVisitFormData.getProjectConsultant());
            site.setSiteProjectOwner(siteVisitFormData.getProjectOwner());
            site.setAreaOfConstruction(siteVisitFormData.getAreaOfConstruction());
            site.setSitetAreaConstructUnit(siteVisitFormData.getUnit());

            if (siteVisitFormData.getSiteStartMonth() != null && !(siteVisitFormData.getSiteStartMonth().isEmpty())) {
                try {
                    site.setSiteStartMonth(dateFormat.parse(siteVisitFormData.getSiteStartMonth()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            if (siteVisitFormData.getDurationLeft() != null && !(siteVisitFormData.getDurationLeft().isEmpty())) {
                try {
                    site.setSiteCompletionMonth(dateFormat.parse(siteVisitFormData.getDurationLeft()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            site.setSitePotential(siteVisitFormData.getSitePotential());
            site.setBalancePotential(siteVisitFormData.getBalancePotential());
            site.setWorkOrderCopyDocName(siteVisitFormData.getWorkOrderCopyName());
            site.setRemarksForSiteVisit(siteVisitFormData.getRemarks());
            site.setNextActionForSiteVisit(siteVisitFormData.getNextAction());

            //remainder visit date and time in field
            if (siteVisitFormData.getRemainderDateForNextVisit() != null && !(siteVisitFormData.getRemainderDateForNextVisit().isEmpty())) {
                try {
                    site.setRemainderForNextVisitDate(dateFormat.parse(siteVisitFormData.getRemainderDateForNextVisit()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            site.setWillingnessToBuy(siteVisitFormData.getWillingnessToBuy());

            if(siteVisitFormData.getWorkOrderCopyDoc() != null) {
                CatalogUnawareMediaModel demoImage = getWorkOrderCopyImage(siteVisitFormData.getWorkOrderCopyDoc());
                site.setWorkOrderCopyDoc(demoImage);
            }
            List<SiteExpectedRequirementModel> expectedRequirementModelList = new ArrayList<>();
            if(siteVisitFormData.getExpectedRequirementList()!=null && !siteVisitFormData.getExpectedRequirementList().isEmpty())
            {
                for (ExpectedRequirementData expectedRequirementData : siteVisitFormData.getExpectedRequirementList()) {
                    SiteExpectedRequirementModel siteExpectedRequirementModel = modelService.create(SiteExpectedRequirementModel.class);
                    siteExpectedRequirementModel.setSite(site);
                    siteExpectedRequirementModel.setBrand(currentBaseSite);
                    CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                    ProductModel product = productService.getProductForCode(catalogVersion, expectedRequirementData.getProductCode());
                    siteExpectedRequirementModel.setProduct(product);
                    siteExpectedRequirementModel.setEstQuantity(expectedRequirementData.getEstimatedQty());
                    modelService.save(siteExpectedRequirementModel);
                    expectedRequirementModelList.add(siteExpectedRequirementModel);
                }
            }
            //set expected requirement in site
            site.setExpectedRequirements(expectedRequirementModelList);
            List<SiteCompetitorInformationModel> siteCompetitorInformationList = new ArrayList<>();
            if(siteVisitFormData.getCompetitorInformationList()!=null && !siteVisitFormData.getCompetitorInformationList().isEmpty())
            {
                for (CompetitorInformationData competitorInformationData : siteVisitFormData.getCompetitorInformationList()) {
                    SiteCompetitorInformationModel siteCompetitorInformationModel = modelService.create(SiteCompetitorInformationModel.class);
                    siteCompetitorInformationModel.setSite(site);
                    siteCompetitorInformationModel.setBrand(competitorInformationData.getCurrentBrand());
                    siteCompetitorInformationModel.setEstQuantity(competitorInformationData.getEstimatedQty());
                    siteCompetitorInformationModel.setForPrice(competitorInformationData.getForPrice());
                    CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                    ProductModel product = productService.getProductForCode(catalogVersion, competitorInformationData.getProductCode());
                    siteCompetitorInformationModel.setProduct(product);
                    modelService.save(siteCompetitorInformationModel);
                    siteCompetitorInformationList.add(siteCompetitorInformationModel);
                }

            }
            //set competitor information requirement in site
            site.setCompetitorInfos(siteCompetitorInformationList);
            site.setLastVisitTime(new Date());
            modelService.save(site);
            return true;
        }
        return false;
    }

    @Override
    public boolean submitAnnualBudgetSite(SiteAnnualBudgetData siteAnnualBudgetData) {
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        SclCustomerModel site = null;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        if(sclUser.getUserType()!=null && sclUser.getUserType().getCode().equalsIgnoreCase(SclUserType.TSO.getCode())){
            site= (SclCustomerModel) userService.getUserForUID(siteAnnualBudgetData.getSiteCode());
            SiteAnnualBudgetModel siteAnnualBudgetModel= modelService.create(SiteAnnualBudgetModel.class);
            siteAnnualBudgetModel.setBudgetAllocated(siteAnnualBudgetData.getBudgetAllocated());
            siteAnnualBudgetModel.setBudgetRemaining(siteAnnualBudgetData.getBudgetRemaining());
            siteAnnualBudgetModel.setBudgetConsumed(siteAnnualBudgetData.getBudgetConsumed());
            if (siteAnnualBudgetData.getSiteVisitDate() != null && !(siteAnnualBudgetData.getSiteVisitDate().isEmpty())) {
                try {
                    siteAnnualBudgetModel.setSiteVisitDate(dateFormat.parse(siteAnnualBudgetData.getSiteVisitDate()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            siteAnnualBudgetModel.setTaskDone(siteAnnualBudgetData.getTaskDone());
            siteAnnualBudgetModel.setSite(site);
            siteAnnualBudgetModel.setTsoUser(sclUser);
            siteAnnualBudgetModel.setBrand(currentBaseSite);
            modelService.save(siteAnnualBudgetModel);
            return true;
        }
        return false;
    }

    @Override
    public boolean submitSiteFeedback(SiteFeedbackListData siteFeedbackData) {
        SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        SclCustomerModel site = null;
        if(sclUser.getUserType()!=null && sclUser.getUserType().getCode().equalsIgnoreCase(SclUserType.TSO.getCode())) {
            site = (SclCustomerModel) userService.getUserForUID(siteFeedbackData.getSiteCode());

            if (siteFeedbackData.getSiteFeedback() != null && !siteFeedbackData.getSiteFeedback().isEmpty())
            {
                for (SiteFeedbackData feedbackData : siteFeedbackData.getSiteFeedback()) {
                    SiteFeedbackModel siteFeedbackModel = modelService.create(SiteFeedbackModel.class);
                    siteFeedbackModel.setSite(site);
                    siteFeedbackModel.setTsoUser(sclUser);
                    siteFeedbackModel.setBrand(currentBaseSite);
                    siteFeedbackModel.setFeedbackType(FeedbackType.valueOf(feedbackData.getFeedbackType()));
                    siteFeedbackModel.setRating(feedbackData.getRating());
                    siteFeedbackModel.setAdditionalFeedback(feedbackData.getAdditionalFeedback());
                    siteFeedbackModel.setFeedbackSubmitDate(new Date());
                    modelService.save(siteFeedbackModel);
                }
                return true;
            }
        }
        return false;
    }

    public DropdownData getDropdownData(HybrisEnumValue e)
    {
        DropdownData dropdownData= new DropdownData();
        dropdownData.setCode(e.getCode());
        dropdownData.setName(null != enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) : e.getCode());
        return dropdownData;
    }

    private CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file) {

        Long currentTimeInMillis = System.currentTimeMillis();
        final String mediaCode = documentType.concat(SclCoreConstants.UNDERSCORE_CHARACTER).concat(uid).concat(String.valueOf(currentTimeInMillis));

        final MediaFolderModel imageMediaFolder = mediaService.getFolder(SclCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        CatalogUnawareMediaModel documentMedia = null;

        try {
            documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
        } catch (AmbiguousIdentifierException ex) {
            LOG.error("More than one media found with code : " + mediaCode);
            LOG.error("Removing duplicate media : " + mediaCode);
            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
            duplicateMedia.setCode(mediaCode);
            List<CatalogUnawareMediaModel> duplicateMedias = flexibleSearchService.getModelsByExample(duplicateMedia);
            modelService.removeAll(duplicateMedias);
        } catch (UnknownIdentifierException uie) {
            if (LOG.isDebugEnabled()) {
                LOG.error("No Media found with code : " + mediaCode);
            }
        } finally {
            if (null == documentMedia) {
                documentMedia = modelService.create(CatalogUnawareMediaModel.class);
                documentMedia.setCode(mediaCode);
            }
        }
        documentMedia.setFolder(imageMediaFolder);
        documentMedia.setMime(file.getContentType());
        modelService.save(documentMedia);
        try {
            mediaService.setStreamForMedia(documentMedia, file.getInputStream());
        } catch (IOException ioe) {
            LOG.error("IO Exception occured while creating: " + documentType + " ,for dealer with uid: " + uid);
        }

        return (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
    }

    private CatalogUnawareMediaModel getMaterialTestImage(String image) {
        byte[] bytes = Base64.getDecoder().decode(image);
        MultipartFile multipartFile = getMultipartFile("materialTestImage", bytes);
        CatalogUnawareMediaModel mediaFromFile = createMediaFromFile("materialTestImage", "materialTestImage", multipartFile);
        return mediaFromFile;
    }

    private CatalogUnawareMediaModel getConcreteTestImage(String image) {
        byte[] bytes = Base64.getDecoder().decode(image);
        MultipartFile multipartFile = getMultipartFile("concreteTestImage", bytes);
        CatalogUnawareMediaModel mediaFromFile = createMediaFromFile("concreteTestImage", "concreteTestImage", multipartFile);
        return mediaFromFile;
    }

    private CatalogUnawareMediaModel getOtherTestImage(String image) {
        byte[] bytes = Base64.getDecoder().decode(image);
        MultipartFile multipartFile = getMultipartFile("otherTestImage", bytes);
        CatalogUnawareMediaModel mediaFromFile = createMediaFromFile("otherTestImage", "otherTestImage", multipartFile);
        return mediaFromFile;
    }

    private CatalogUnawareMediaModel getSiteDemoImage(String image) {
        byte[] bytes = Base64.getDecoder().decode(image);
        MultipartFile multipartFile = getMultipartFile("siteDemoImage", bytes);
        CatalogUnawareMediaModel mediaFromFile = createMediaFromFile("siteDemoImage", "siteDemoImage", multipartFile);
        return mediaFromFile;
    }

    private CatalogUnawareMediaModel getWorkOrderCopyImage(String image) {
        byte[] bytes = Base64.getDecoder().decode(image);
        MultipartFile multipartFile = getMultipartFile("workOrderCopy", bytes);
        CatalogUnawareMediaModel mediaFromFile = createMediaFromFile("workOrderCopy", "workOrderCopy", multipartFile);
        return mediaFromFile;
    }

    public MultipartFile getMultipartFile(String name, byte[] bytes) {

        MultipartFile mfile = null;
        ByteArrayInputStream in = null;
        try {

            in = new ByteArrayInputStream(bytes);
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            FileItem fileItem = factory.createItem("mainFile", "jpeg", false, name);
            IOUtils.copy(new ByteArrayInputStream(bytes), fileItem.getOutputStream());
            mfile = new CommonsMultipartFile(fileItem);
            in.close();
        }catch (IOException e){
            LOG.error("unexpected error for getting multipart file" + e.getMessage());
        }
        return mfile;
    }

    public TSOSiteManagementDao getTsoSiteManagementDao() {
        return tsoSiteManagementDao;
    }

    public void setTsoSiteManagementDao(TSOSiteManagementDao tsoSiteManagementDao) {
        this.tsoSiteManagementDao = tsoSiteManagementDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }

    public CustomerAccountService getCustomerAccountService() {
        return customerAccountService;
    }

    public void setCustomerAccountService(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;
    }
}
