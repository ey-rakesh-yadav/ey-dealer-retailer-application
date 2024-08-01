package com.scl.core.job;

import com.scl.core.enums.NotificationCategory;
import com.scl.core.enums.RequisitionStatus;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
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

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PendingRequisitionsConfirmationJob extends AbstractJobPerformable<CronJobModel> {


    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Resource
    private TerritoryManagementService territoryManagementService;

    @Autowired
    KeyGenerator siteMessageUidGenerator;

    private final static Logger LOG = Logger.getLogger(PendingRequisitionsConfirmationJob.class);

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {

        List<OrderRequisitionModel> orderEntryModel= getOrderRequisitionEntries();
        if(orderEntryModel.isEmpty()) {
            LOG.error("No Retailer order request no. XXX with product xx and quantity xx bags is pending");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        for(OrderRequisitionModel entryModel : orderEntryModel) {
                 notifyDealer(entryModel);
            if (entryModel.getQuantity() > 1000) {
                LOG.info(String.format("entryModelPK :: %s",entryModel));
                notifySO(entryModel);
            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<OrderRequisitionModel> getOrderRequisitionEntries() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {OrderRequisition} where {status}=?status and {requisitionDate}>=?startDate and {requisitionDate}<=?endDate");
        params.put("status", RequisitionStatus.PENDING_CONFIRMATION);
        LocalDate endDate1 = LocalDate.now();
        LocalDate startDate1 = endDate1.minusDays(30);

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endDateWithTime = currentTime.minusHours(2);

        Instant instantStart = startDate1.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant instantEnd = endDateWithTime.atZone(ZoneId.systemDefault()).toInstant();

        Date startDate = Date.from(instantStart);
        Date endDate = Date.from(instantEnd);
       LOG.info(String.format("startDate :: %s",startDate));
        params.put("startDate",startDate);
        LOG.info(String.format("endDate :: %s",endDate));
        params.put("endDate",endDate);


        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(OrderRequisitionModel.class));
        query.addQueryParameters(params);
        final SearchResult<OrderRequisitionModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult() != null && !(searchResult.getResult().isEmpty())) {
            return searchResult.getResult();
        }
        return Collections.emptyList();
    }


    //notification to Dealer
    public void notifyDealer(OrderRequisitionModel orderRequisition){
        B2BCustomerModel dealer = (B2BCustomerModel) orderRequisition.getFromCustomer();
        final StringBuilder builder = new StringBuilder();
        //builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending.");
        builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() + " by "+ orderRequisition.getToCustomer().getUid() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending for " +orderRequisition.getFromCustomer().getUid());
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.PENDING_REQUISITIONS_CONFIRMATION);
        notification.setSubject("Pending requisitions Confirmation");
        notification.setBody(body);

        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(dealer);
        customer.setCustomer(dealer);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);
    }

    //notification to SO
    public void notifySO(OrderRequisitionModel orderRequisition){
        B2BCustomerModel so = territoryManagementService.getSOforCustomer(orderRequisition.getFromCustomer());

        final StringBuilder builder = new StringBuilder();
        //builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending.");
        builder.append("Retailer order request no. " + orderRequisition.getRequisitionId() + " by "+ orderRequisition.getToCustomer().getUid() +" with product " +orderRequisition.getProduct().getName() +" and quantity " + orderRequisition.getQuantity() + " bags is pending for " +orderRequisition.getFromCustomer().getUid());
        String body = builder.toString();
        SiteMessageModel notification = modelService.create(SiteMessageModel.class);
        notification.setNotificationType(NotificationType.NOTIFICATION);
        notification.setCategory(NotificationCategory.PENDING_REQUISITIONS_CONFIRMATION);
        notification.setSubject("Pending requisitions Confirmation");
        notification.setBody(body);



        SiteMessageForCustomerModel customer = modelService.create(SiteMessageForCustomerModel.class);
        notification.setOwner(so);
        customer.setCustomer(so);
        notification.setType(SiteMessageType.SYSTEM);
        notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
        notification.setExpiryDate(getExpiryDate());
        customer.setMessage(notification);
        customer.setSentDate(new Date());
        modelService.save(notification);
        modelService.save(customer);

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
