package com.scl.core.region.dao.impl;

import com.scl.core.jalo.SclUser;
import com.scl.core.model.DistrictMasterModel;
import com.scl.core.model.RegionMasterModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.region.dao.RegionMasterDao;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionMasterDaoImpl extends DefaultGenericDao<RegionMasterModel> implements RegionMasterDao {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    UserService userService;

    @Autowired
    FlexibleSearchService flexibleSearchService;

    public RegionMasterDaoImpl() {
        super(RegionMasterModel._TYPECODE);
    }
    @Override
    public RegionMasterModel findByCode(String regionCode){
        if(regionCode!=null){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(RegionMasterModel.CODE,regionCode);

            final List<RegionMasterModel> regionMasterList = this.find(map);
            if(regionMasterList!=null && !regionMasterList.isEmpty()){
                return regionMasterList.get(0);
            }
        }
        return null;
    }

    @Override
    public RegionMasterModel getRegionByCodeInLocalView(String regionCode) {
        return (RegionMasterModel) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public RegionMasterModel execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    if(regionCode!=null){
                        final Map<String, Object> params = new HashMap<String, Object>();
                        final String queryString = "SELECT {pk} FROM {RegionMaster} WHERE {code} = ?regionCode";
                        params.put("regionCode", regionCode);

                        FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
                        query.addQueryParameters(params);
                        final SearchResult<RegionMasterModel> searchResult = flexibleSearchService.search(query);
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
    public List<RegionMasterModel> getRegionsForRhInLocalView() {
        return sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public List<RegionMasterModel> execute() {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    final Map<String, Object> params = new HashMap<String, Object>();
                    final StringBuilder builder = new StringBuilder("Select {r.pk} from {RhRegionMapping as rh join RegionMaster as r on {rh.region}={r.pk}} where {rh:rhUser} = ?rhUser and {rh:brand} = ?brand and {rh:isActive} = ?active");
                    params.put("rhUser",(SclUserModel) userService.getCurrentUser());
                    params.put("brand", baseSiteService.getCurrentBaseSite());
                    params.put("active",Boolean.TRUE);
                    FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
                    query.setResultClassList(Collections.singletonList(RegionMasterModel.class));
                    query.addQueryParameters(params);
                    final SearchResult<RegionMasterModel> searchResult = flexibleSearchService.search(query);
                    List<RegionMasterModel> result = searchResult.getResult();
                    return result != null && !result.isEmpty() ? result : Collections.emptyList();
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }
}
