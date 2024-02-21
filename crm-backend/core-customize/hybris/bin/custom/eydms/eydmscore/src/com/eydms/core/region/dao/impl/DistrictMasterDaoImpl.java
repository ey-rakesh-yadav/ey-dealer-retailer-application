package com.eydms.core.region.dao.impl;

import com.eydms.core.model.DistrictMasterModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.region.dao.DistrictMasterDao;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistrictMasterDaoImpl extends DefaultGenericDao<DistrictMasterModel> implements DistrictMasterDao {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;

    @Autowired
    UserService userService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    public DistrictMasterDaoImpl(){
        super(DistrictMasterModel._TYPECODE);
    }
    @Override
    public DistrictMasterModel findByCode(String districtCode) {
        if(districtCode!=null){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DistrictMasterModel.CODE, districtCode);

            final List<DistrictMasterModel> districtMasterList = this.find(map);

            if(districtMasterList!=null && !districtMasterList.isEmpty()){
                return districtMasterList.get(0);
            }
        }
        return null;
    }

    @Override
    public DistrictMasterModel getDistrictByCodeInLocalView(String districtCode) {
        return (DistrictMasterModel) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public DistrictMasterModel execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    if(districtCode!=null) {
                        final Map<String, Object> params = new HashMap<String, Object>();
                        final String queryString = "SELECT {pk} FROM {DistrictMaster} WHERE {code} = ?districtCode";
                        params.put("districtCode", districtCode);

                        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
                        query.addQueryParameters(params);
                        final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
                        if(CollectionUtils.isNotEmpty(searchResult.getResult())){
                            return searchResult.getResult().get(0);
                        }
                    }
                    return null;
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }

            }
        });
    }

    @Override
    public List<DistrictMasterModel> getDistrictsForTsmInLocalView() {
        return (List<DistrictMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<DistrictMasterModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder();
                    builder.append("select {d:pk} from {TsmDistrictMapping as tsm join DistrictMaster as d on {tsm:district}={d:pk}}" +
                            " where {tsm:tsmUser} = ?tsmUser and {tsm:brand} = ?brand and {tsm:isActive} = ?active order by {d:pk}");
                    params.put("tsmUser", (EyDmsUserModel) userService.getCurrentUser());
                    params.put("active", Boolean.TRUE);
                    params.put("brand", baseSiteService.getCurrentBaseSite());
                    FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Collections.singletonList(DistrictMasterModel.class));
                    query.addQueryParameters(params);
                    final SearchResult<DistrictMasterModel> searchResult = flexibleSearchService.search(query);
                    if(searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
                        return searchResult.getResult();
                    }
                    else {
                        return Collections.emptyList();
                    }
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }
}
