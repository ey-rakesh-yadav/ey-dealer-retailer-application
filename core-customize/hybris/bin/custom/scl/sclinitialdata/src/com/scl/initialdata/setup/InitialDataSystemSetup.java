/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.initialdata.setup;

import de.hybris.platform.commerceservices.dataimport.impl.CoreDataImportService;
import de.hybris.platform.commerceservices.dataimport.impl.SampleDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.CoreDataImportedEvent;
import de.hybris.platform.commerceservices.setup.events.SampleDataImportedEvent;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import com.scl.initialdata.constants.SclInitialDataConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class provides hooks into the system's initialization and update processes.
 */
@SystemSetup(extension = SclInitialDataConstants.EXTENSIONNAME)
public class InitialDataSystemSetup extends AbstractSystemSetup
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(InitialDataSystemSetup.class);

	//public static final String SHREE = "shree";
	//public static final String BANGUR = "bangur";
	//public static final String ROCKSTRONG = "rockstrong";
	public static final String SITE102 = "102";
	public static final String SITE103 = "103";
	public static final String SITE104 = "104";
	public static final String SITESCL = "scl";


	private static final String IMPORT_CORE_DATA = "importCoreData";
	private static final String IMPORT_SAMPLE_DATA = "importSampleData";
	private static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";

	private CoreDataImportService coreDataImportService;
	private SampleDataImportService sampleDataImportService;

	@Autowired
	ConfigurationService configurationService;

	/**
	 * Generates the Dropdown and Multi-select boxes for the project data import
	 */
	@Override
	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

		params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "Import Core Data", true));
		params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA, "Import Sample Data", true));
		params.add(createBooleanSystemSetupParameter(ACTIVATE_SOLR_CRON_JOBS, "Activate Solr Cron Jobs", true));
		// Add more Parameters here as you require

		return params;
	}

	/**
	 * Implement this method to create initial objects. This method will be called by system creator during
	 * initialization and system update. Be sure that this method can be called repeatedly.
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createEssentialData(final SystemSetupContext context)
	{
		// Add Essential Data here as you require
		//getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclMasterData.impex", context.getExtensionName()), false);
		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/DefaultSearchRestriction.impex", context.getExtensionName()), false);
		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/102/solr.impex", context.getExtensionName()), false);
		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/productCatalogs/102ProductCatalog/products.impex", context.getExtensionName()), false);
		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/productCatalogs/102ProductCatalog/categories-classifications.impex", context.getExtensionName()), false);
		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/productCatalogs/102ProductCatalog/products-classifications.impex", context.getExtensionName()), false);


		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/scl/solr.impex", context.getExtensionName()), false);

		String devEnv = configurationService.getConfiguration().getString(SclInitialDataConstants.ENVIRONMENTS.DEV_ENV);
		if(devEnv.equals("true")) {
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclIntegration.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-SLCT-CRM-Integration-Objects.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-Config-Dev.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomer.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundOrderStageGate.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclPlantIncoTerm.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProductIncoTerm.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclGeographicalMaster.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundPlantDepotMaster.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/IndoundSclSalesOrderDeliverySLA.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomerBlockStatus.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclInternalSalesHierarchy.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProduct.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclNilsonBrandMapping.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundShipToPartyAddress.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundOMMOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/cronjob.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomerCreditLimit.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclNeilsonBrandMapping.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProductAlias.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundCancelOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundCancelOMMOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundStageGate.impex", context.getExtensionName()), false);


		}
		String stageEnv = configurationService.getConfiguration().getString(SclInitialDataConstants.ENVIRONMENTS.STAGE_ENV);
		if(stageEnv.equals("true"))
		{
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclIntegration.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-SLCT-CRM-Integration-Objects.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-Config-Stage.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomer.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundOrderStageGate.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclPlantIncoTerm.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProductIncoTerm.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclGeographicalMaster.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundPlantDepotMaster.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/IndoundSclSalesOrderDeliverySLA.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomerBlockStatus.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclInternalSalesHierarchy.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProduct.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclNilsonBrandMapping.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundShipToPartyAddress.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundOMMOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/cronjob.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomerCreditLimit.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclNeilsonBrandMapping.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProductAlias.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundCancelOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundCancelOMMOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundStageGate.impex", context.getExtensionName()), false);
		}
		String prodEnv = configurationService.getConfiguration().getString(SclInitialDataConstants.ENVIRONMENTS.PROD_ENV);
		if(prodEnv.equals("true")) {
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-Config-Stage-Prod.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-Integration-Objects.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/CPI-SLCT-CRM-Integration-Objects.impex", context.getExtensionName()), false);

			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomer.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundOrderStageGate.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclPlantIncoTerm.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProductIncoTerm.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclGeographicalMaster.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundPlantDepotMaster.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/IndoundSclSalesOrderDeliverySLA.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomerBlockStatus.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclInternalSalesHierarchy.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProduct.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclNilsonBrandMapping.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundShipToPartyAddress.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundOMMOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/cronjob.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclCustomerCreditLimit.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundSclNeilsonBrandMapping.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundProductAlias.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundCancelOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/InboundCancelOMMOrder.impex", context.getExtensionName()), false);
			getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/common/SclOutboundStageGate.impex", context.getExtensionName()), false);
		}

		getCoreDataImportService().getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/productCatalogs/sclProductCatalog/products.impex", context.getExtensionName()), false);

	}

	/**
	 * Implement this method to create data that is used in your project. This method will be called during the system
	 * initialization. <br>
	 * Add import data for each site you have configured
	 *
	 * <pre>
	 * final List<ImportData> importData = new ArrayList<ImportData>();
	 *
	 * final ImportData sampleImportData = new ImportData();
	 * sampleImportData.setProductCatalogName(SAMPLE_PRODUCT_CATALOG_NAME);
	 * sampleImportData.setContentCatalogNames(Arrays.asList(SAMPLE_CONTENT_CATALOG_NAME));
	 * sampleImportData.setStoreNames(Arrays.asList(SAMPLE_STORE_NAME));
	 * importData.add(sampleImportData);
	 *
	 * getCoreDataImportService().execute(this, context, importData);
	 * getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
	 *
	 * getSampleDataImportService().execute(this, context, importData);
	 * getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
	 * </pre>
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{
		// Add import data for each site you have configured

		final List<ImportData> importData = new ArrayList<ImportData>();

		// Import data for shree
		/*final ImportData shreeImportData = new ImportData();
		shreeImportData.setProductCatalogName(SHREE);
		shreeImportData.setContentCatalogNames(Arrays.asList(SHREE));
		shreeImportData.setStoreNames(Arrays.asList(SHREE));
		importData.add(shreeImportData);

		// Import data for bangur
		final ImportData bangurImportData = new ImportData();
		bangurImportData.setProductCatalogName(BANGUR);
		bangurImportData.setContentCatalogNames(Arrays.asList(BANGUR));
		bangurImportData.setStoreNames(Arrays.asList(BANGUR));
		importData.add(bangurImportData);

		// Import data for rockstrong
		final ImportData rockstrongImportData = new ImportData();
		rockstrongImportData.setProductCatalogName(ROCKSTRONG);
		rockstrongImportData.setContentCatalogNames(Arrays.asList(ROCKSTRONG));
		rockstrongImportData.setStoreNames(Arrays.asList(ROCKSTRONG));
		importData.add(rockstrongImportData);*/

		/*final ImportData site102ImportData = new ImportData();
		site102ImportData.setProductCatalogName(SITE102);
		site102ImportData.setContentCatalogNames(Arrays.asList(SITE102));
		site102ImportData.setStoreNames(Arrays.asList(SITE102));
		importData.add(site102ImportData);

		final ImportData site103ImportData = new ImportData();
		site103ImportData.setProductCatalogName(SITE103);
		site103ImportData.setContentCatalogNames(Arrays.asList(SITE103));
		site103ImportData.setStoreNames(Arrays.asList(SITE103));
		importData.add(site103ImportData);

		final ImportData site104ImportData = new ImportData();
		site104ImportData.setProductCatalogName(SITE104);
		site104ImportData.setContentCatalogNames(Arrays.asList(SITE104));
		site104ImportData.setStoreNames(Arrays.asList(SITE104));
		importData.add(site104ImportData);

		final ImportData site1000ImportData = new ImportData();
		site1000ImportData.setProductCatalogName(SITESCL);
		site1000ImportData.setContentCatalogNames(Arrays.asList(SITESCL));
		site1000ImportData.setStoreNames(Arrays.asList(SITESCL));
		importData.add(site1000ImportData);*/

		final ImportData sclImportData = new ImportData();
		sclImportData.setProductCatalogName(SITESCL);
		sclImportData.setContentCatalogNames(Arrays.asList(SITESCL));
		sclImportData.setStoreNames(Arrays.asList(SITESCL));
		importData.add(sclImportData);

		getCoreDataImportService().execute(this, context, importData);
		getEventService().publishEvent(new CoreDataImportedEvent(context, importData));

		getSampleDataImportService().execute(this, context, importData);
		getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
	}

	public CoreDataImportService getCoreDataImportService()
	{
		return coreDataImportService;
	}

	@Required
	public void setCoreDataImportService(final CoreDataImportService coreDataImportService)
	{
		this.coreDataImportService = coreDataImportService;
	}

	public SampleDataImportService getSampleDataImportService()
	{
		return sampleDataImportService;
	}

	@Required
	public void setSampleDataImportService(final SampleDataImportService sampleDataImportService)
	{
		this.sampleDataImportService = sampleDataImportService;
	}
}
