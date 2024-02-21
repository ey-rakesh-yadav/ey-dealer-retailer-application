package com.eydms.core.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.*;

import com.eydms.core.dao.VisitMasterDao;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.model.VisitMasterModel;

import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
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

public class VisitMasterDaoImpl extends DefaultGenericDao<VisitMasterModel> implements VisitMasterDao {

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

	public VisitMasterDaoImpl() {
		super(VisitMasterModel._TYPECODE);
	}

	@Override
	public VisitMasterModel findById(String visitId) {
        validateParameterNotNullStandardMessage("visitId", visitId);
        final List<VisitMasterModel> visitList = this.find(Collections.singletonMap(VisitMasterModel.PK, visitId));
        if (visitList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d visits with the visitId value: '%s', which should be unique", visitList.size(),
                    		visitId));
        }
        else
        {
            return visitList.isEmpty() ? null : visitList.get(0);
        }
    }
	
	@Override
	public VisitMasterModel findByRouteAndDate(EyDmsUserModel eydmsUserModel, Date date) {
		validateParameterNotNullStandardMessage("eydmsUserModel", eydmsUserModel);
		validateParameterNotNullStandardMessage("date", date);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(VisitMasterModel.USER, eydmsUserModel);
		map.put(VisitMasterModel.VISITPLANNEDDATE, date);

		final List<VisitMasterModel> visitList = this.find(map);
		if (visitList!=null && !visitList.isEmpty())
		{
			visitList.get(0);
		}
		return null;
	}

	@Override
	public VisitMasterModel findVisitMasterByIdInLocalView(String visitMasterId) {
		return (VisitMasterModel) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public VisitMasterModel execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					final Map<String, Object> params = new HashMap<String, Object>();
					final String queryString = "SELECT {pk} FROM {VisitMaster} WHERE {pk} = ?visitMasterId";
					params.put("visitMasterId", visitMasterId);

					FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
					query.addQueryParameters(params);
					final SearchResult<VisitMasterModel> searchResult = flexibleSearchService.search(query);
					if (CollectionUtils.isNotEmpty(searchResult.getResult())) {
						return searchResult.getResult().get(0);
					}
					return null;
				} finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}
}
