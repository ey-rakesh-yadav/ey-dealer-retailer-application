package com.eydms.core.job;

import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.parameters.P;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class EpodPendingStatusAlertNotificationJob extends AbstractJobPerformable<CronJobModel> {

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;

    private final static Logger LOG = Logger.getLogger(EpodPendingStatusAlertNotificationJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {

        List<OrderEntryModel> epodPendingOrderEntriesList = getEpodPendingOrderEntries();
        if(epodPendingOrderEntriesList.isEmpty()) {
            LOG.error("There are no Order Entries with EPOD_PENDING status and EPOD initiate date difference greater than 6 hours");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        //Iterating through the list of Order Entry Models
        for(OrderEntryModel orderEntry : epodPendingOrderEntriesList) {

            if(orderEntry.getNotificationSentDate() == null && orderEntry.getNotificationSentCount() == 0) {
//              Date currentTime = new Date();
//              Date epodInitiateDate = orderEntry.getEpodInitiateDate();
                String orderCode = orderEntry.getOrder().getCode();
                String entryNumber = String.valueOf(orderEntry.getEntryNumber());

//              long differenceInMilliseconds = Math.abs(currentTime.getTime()-epodInitiateDate.getTime());
//              long differenceInHours = (differenceInMilliseconds / (60*60*1000)) % 24;
//              long differenceInDays = (differenceInMilliseconds / (60*60*1000*24));
//
//              if(differenceInHours >= 6) {

                SiteMessageModel notification = modelService.create(SiteMessageModel.class);
                notification.setNotificationType(NotificationType.NOTIFICATION);
                notification.setBody(String.format("Kindly mark the %s / %s (Order Code/ Entry Number) in EPOD Pending as delivered",orderCode,entryNumber));
                notification.setSubject("EPOD pending for more than 6 hours");

                SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
                notification.setOwner(orderEntry.getOrder().getUser());
                customer.setCustomer((EyDmsCustomerModel) orderEntry.getOrder().getUser());
                notification.setEntryNumber(orderEntry.getEntryNumber());
                notification.setDealerCode(orderEntry.getOrder().getUser().getUid());
                notification.setOrderCode(orderCode);
                notification.setType(SiteMessageType.SYSTEM);
                notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
                notification.setExpiryDate(getExpiryDate());
                notification.setOrderStatus(orderEntry.getStatus().getCode());
                notification.setOrderType(orderEntry.getOrder().getOrderType().getCode());
                orderEntry.setNotificationSentCount(orderEntry.getNotificationSentCount()+1);
                orderEntry.setNotificationSentDate(new Date());
                customer.setMessage(notification);
                customer.setSentDate(new Date());
                modelService.save(orderEntry);
                modelService.save(notification);
                modelService.save(customer);
                LOG.info(String.format("EPOD pending for more than 6 hours for the ordercode / Entry Number - %s / %s for the user name %s and UID %s ",orderCode, entryNumber, orderEntry.getOrder().getUser().getName(), orderEntry.getOrder().getUser().getUid()));
//              }
            }
            else {
                LOG.error("The notification is already sent to the dealer");
                return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<OrderEntryModel> getEpodPendingOrderEntries() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {oe:pk} from {OrderEntry as oe join Order as o on {oe:order}={o:pk}" +
                " join OrderStatus as s on {oe:status}={s:pk}} where {s:code} = ?orderStatus and" +
                " datediff(MINUTE, {epodInitiateDate}, current_timestamp) > 360");
        params.put("orderStatus","EPOD_PENDING");

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderEntryModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderEntryModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }

    private Date getExpiryDate(){
        LocalDate date = LocalDate.now().plusDays(30);
        Date expiryDate = null;
        try {
            expiryDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return expiryDate;
    }
}
