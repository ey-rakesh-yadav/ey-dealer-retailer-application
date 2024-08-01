    package com.scl.core.job;

    import com.scl.core.constants.SclCoreConstants;
    import com.scl.core.model.ProspectiveDealerModel;
    import com.scl.core.prosdealer.service.ProsDealerService;
    import de.hybris.platform.cronjob.enums.CronJobResult;
    import de.hybris.platform.cronjob.enums.CronJobStatus;
    import de.hybris.platform.cronjob.model.CronJobModel;
    import de.hybris.platform.notificationservices.enums.NotificationType;
    import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
    import de.hybris.platform.notificationservices.model.SiteMessageModel;
    import de.hybris.platform.servicelayer.config.ConfigurationService;
    import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
    import de.hybris.platform.servicelayer.cronjob.PerformResult;
    import de.hybris.platform.servicelayer.user.UserService;
    import org.apache.log4j.Logger;
    import org.springframework.beans.factory.annotation.Autowired;

    import java.time.LocalDate;
    import java.time.ZoneId;
    import java.util.Date;
    import java.util.List;

    public class CustomerQueryAcknowledgeAlertJob extends AbstractJobPerformable<CronJobModel> {

        private static final Logger LOG = Logger.getLogger(CustomerQueryAcknowledgeAlertJob.class);

        @Autowired
        ProsDealerService prosDealerService;

        @Autowired
        UserService userService;

        @Autowired
        ConfigurationService configurationService;

        @Override
        public PerformResult perform(CronJobModel cronJobModel) {
            try{

                final int hrs = configurationService.getConfiguration().getInt(SclCoreConstants.NOTIFICATION_EXPIRY);

                List<ProspectiveDealerModel> sOProsDealersList = prosDealerService.getProsDealerForSOCustomerQueryAlert();
                List<ProspectiveDealerModel> sHProsDealersList = prosDealerService.getProsDealerForSOCustomerQueryAlert();

                Date currentTime = new Date();
                LocalDate currentTimeDate = LocalDate.ofInstant(currentTime.toInstant(), ZoneId.systemDefault());

                sOProsDealersList.stream()
                        .filter(prosDealer -> !LocalDate.ofInstant(prosDealer.getCreationtime().toInstant(), ZoneId.systemDefault()).equals(currentTimeDate))
                        .forEach(prosDealer -> {
                            prosDealer.setIsSOAlertTriggered(true);
                            SiteMessageModel notification =modelService.create(SiteMessageModel.class);
                            notification.setNotificationType(NotificationType.ALERT);
                            notification.setSubject(SclCoreConstants.CUSTOMER_QUERY_ALERT_TITLE);
                            notification.setBody(SclCoreConstants.CUSTOMER_QUERY_ALERT_CONTENT);
                            SiteMessageForCustomerModel customer= modelService.create(SiteMessageForCustomerModel.class);
                            notification.setOwner(customer.getCustomer());
                            modelService.save(notification);
                        });
                sHProsDealersList.stream()
                        .filter(prosDealer -> {LocalDate triggerDate=LocalDate.ofInstant(prosDealer.getCreationtime().toInstant(), ZoneId.systemDefault()).plusDays(3);
                           return triggerDate.isEqual(currentTimeDate) ||
                                   triggerDate.isBefore(currentTimeDate);
                        })
                        .forEach(prosDealer -> {
                            prosDealer.setIsSHAlertTriggered(true);
                            SiteMessageModel notification =modelService.create(SiteMessageModel.class);
                            notification.setNotificationType(NotificationType.ESCALATION);
                            notification.setSubject(SclCoreConstants.CUSTOMER_QUERY_ALERT_TITLE);
                            notification.setBody(SclCoreConstants.CUSTOMER_QUERY_ALERT_CONTENT);

                            SiteMessageForCustomerModel customer= modelService.create(SiteMessageForCustomerModel.class);
                            notification.setOwner(customer.getCustomer());
                            // notification.setB2bCustomer((Collection<B2BCustomerModel>) prosDealer); //recheck
                            modelService.save(notification);
                        });

            }
            catch (Exception e){
                LOG.error("Unknown exception occured, reason: {} ", e);
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
            }
            return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
        }
    }
