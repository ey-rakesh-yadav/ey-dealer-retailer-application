package com.scl.core.job;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.CreditAndOutstandingModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.order.dao.OrderValidationProcessDao;
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
        List<SclCustomerModel> dealersList = getDealersList();
        if(dealersList.isEmpty()) {
            LOG.error("There are no dealers with creditLimitBreached being true");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }
        //Iterating through the list of dealers
        for(SclCustomerModel dealer : dealersList) {

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

    public List<SclCustomerModel> getDealersList() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {pk} from {SclCustomer} where {isCreditLimitBreached} = ?status");

        params.put("status", Boolean.TRUE);

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
        query.addQueryParameters(params);
        final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
        List<SclCustomerModel> dealersList = searchResult.getResult().stream().filter(cust -> cust.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
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


