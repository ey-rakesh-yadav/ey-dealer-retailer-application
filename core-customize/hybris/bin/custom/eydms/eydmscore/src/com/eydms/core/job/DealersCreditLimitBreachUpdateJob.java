package com.eydms.core.job;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.model.CreditAndOutstandingModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public class DealersCreditLimitBreachUpdateJob extends AbstractJobPerformable<CronJobModel> {

    @Resource
    UserService userService;

    @Resource
    FlexibleSearchService flexibleSearchService;

    @Resource
    ModelService modelService;

    @Autowired
    OrderValidationProcessDao orderValidationProcessDao;

    private static final Logger LOG = Logger.getLogger(DealersCreditLimitBreachUpdateJob.class);

    @Override
    public PerformResult perform(CronJobModel arg0) {
        List<EyDmsCustomerModel> dealersList = getDealersList();
        if(dealersList.isEmpty()) {
            LOG.error("There are no dealers with creditLimitBreached being true");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
        //Iterating through the list of dealers
        for(EyDmsCustomerModel dealer : dealersList) {

            CreditAndOutstandingModel dealerCreditModel = getDealerOutstandingDetails(dealer.getCustomerNo());

            if(!Objects.isNull(dealerCreditModel)) {
                Double totalOutstanding = dealerCreditModel.getTotalOutstanding()!=null ? dealerCreditModel.getTotalOutstanding() : 0.0;
                Double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(dealer.getPk().toString());
                Double creditLimit = dealerCreditModel.getCreditLimit()!=null ? dealerCreditModel.getCreditLimit() : 0.0;

                if(totalOutstanding + pendingOrderAmount < creditLimit) {
                    dealer.setIsCreditLimitBreached(false);
                    dealer.setCreditLimitBreachedDate(null);
                    modelService.save(dealer);
                    LOG.info(String.format("The credit limit breached has been set to false and credit limit breached date to null for the customer with customerNo = %s",dealer.getCustomerNo()));
                }
            }
        }

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public List<EyDmsCustomerModel> getDealersList() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {EyDmsCustomer} where {isCreditLimitBreached} = ?status");

        params.put("status", Boolean.TRUE);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(EyDmsCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<EyDmsCustomerModel> searchResult = flexibleSearchService.search(query);
        List<EyDmsCustomerModel> dealersList = searchResult.getResult().stream().filter(cust -> cust.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
        if(dealersList!=null && !dealersList.isEmpty()) {
            return dealersList;
        }
        else {
            return Collections.emptyList();
        }
    }

    public CreditAndOutstandingModel getDealerOutstandingDetails(String custCode) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {CreditAndOutstanding} where {customerCode} = ?custCode");

        params.put("custCode", custCode);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(CreditAndOutstandingModel.class));
        query.addQueryParameters(params);
        final SearchResult<CreditAndOutstandingModel> searchResult = flexibleSearchService.search(query);
        if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
        else
            return null;
    }
}


