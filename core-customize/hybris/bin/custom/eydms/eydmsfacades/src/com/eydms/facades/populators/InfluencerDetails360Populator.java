package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.CollectionDao;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.enums.NetworkType;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.order.dao.EyDmsOrderCountDao;
import com.eydms.core.services.AmountFormatService;
import com.eydms.facades.customer.EyDmsCustomerFacade;
import com.eydms.facades.data.InfluencersDetails360WsData;
import com.eydms.facades.data.EyDmsSiteData;
import com.eydms.facades.data.EyDmsSiteListData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class InfluencerDetails360Populator implements Populator<EyDmsCustomerModel, InfluencersDetails360WsData> {

    @Resource
    private DJPVisitDao djpVisitDao;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private EyDmsOrderCountDao eydmsOrderCountDao;
    @Resource
    private CollectionDao collectionDao;
    @Resource
    private AmountFormatService amountFormatService;
    @Resource
    private EyDmsCustomerFacade eydmsCustomerFacade;

    @Override
    public void populate(EyDmsCustomerModel source, InfluencersDetails360WsData target) throws ConversionException {

        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        String transactionType = EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE;
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        if (Objects.nonNull(source)) {
            //network type
            target.setNetworkType(Objects.nonNull(source.getInfluencerType()) ? source.getInfluencerType().getCode(): StringUtils.EMPTY);
            int visits = djpVisitDao.getVisitCountMTD(source, month, year);
            target.setCode(source.getUid());
            //network category
            target.setInfluencerCategory(Objects.nonNull(source.getDealerCategory())? source.getDealerCategory().getCode() : StringUtils.EMPTY);
            target.setVisits(visits);
            target.setName(source.getName());
            target.setPotential(source.getCounterPotential());
            target.setLastVisitDate(source.getLastVisitTime());

            if(Objects.nonNull(source)) {
                //Date maxDate = djpVisitDao.getLastLiftingDateForRetailerOrInfluencer(source.getNirmanMitraCode(), brand, transactionType);
                Date maxDate = djpVisitDao.getLastLiftingDateForInfluencerFromPointReq(source,brand,transactionType);
                if (maxDate != null) {
                    target.setLastLiftingDate(maxDate);

                    //Double lastLiftingQuantity = djpVisitDao.getLastLiftingQuantityForRetailerOrInfluencer(source.getNirmanMitraCode(), brand, maxDate, transactionType);
                    Double lastLiftingQuantity = djpVisitDao.getLastLiftingQuantityForInfluencerFromPointReq(source,brand,maxDate,transactionType);
                    target.setLastLiftingQuantity(lastLiftingQuantity);

                    cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, -1);
                    Date date = cal.getTime();
                    cal.add(Calendar.MONTH, -2);
                    Date date2 = cal.getTime();

                    if (target.getLastLiftingDate() != null) {
                        if (date.compareTo(target.getLastLiftingDate()) < 0)
                            target.setNetworkType(NetworkType.ACTIVE.getCode());
                        else if (date2.compareTo(target.getLastLiftingDate()) < 0)
                            target.setNetworkType(NetworkType.INACTIVE.getCode());
                        else
                            target.setNetworkType(NetworkType.DORMANT.getCode());
                    }

                }
            }
            target.setSites(eydmsCustomerFacade.getSitesTaggedtoInfluencers(source));
        }
    }
}
