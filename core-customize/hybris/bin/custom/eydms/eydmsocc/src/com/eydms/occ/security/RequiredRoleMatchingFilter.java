package com.eydms.occ.security;

import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.jalo.DistrictMaster;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.DistrictMasterDao;
import com.eydms.core.region.dao.RegionMasterDao;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.TerritoryManagementServiceImpl;
import com.eydms.facades.data.FilterTalukaData;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RequiredRoleMatchingFilter extends OncePerRequestFilter {

	private static final String ROLE_PREFIX = "ROLE_";
	private String regexp;
	private static final Logger LOG = Logger.getLogger(RequiredRoleMatchingFilter.class);

	@Resource
	private BaseSiteService baseSiteService;

	private SessionService sessionService;

	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementDao territoryDao;

	@Autowired
	TerritoryManagementService territoryService;

	@Autowired
	DistrictMasterDao districtMasterDao;

	@Autowired
	RegionMasterDao regionMasterDao;	
	
	@Autowired
	private SearchRestrictionService searchRestrictionService;
		
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		final Authentication auth = getAuth();
		boolean isCustomer = false;
		if (hasRole(EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, auth) || hasRole(EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, auth)) {
			isCustomer = true;
		}

		boolean isAllowed = false;
		final Optional<BaseSiteModel> currentBaseSite = Optional.ofNullable(baseSiteService.getCurrentBaseSite());
		if (isCustomer && currentBaseSite.isPresent() && currentBaseSite.get().getAllowedUnit() != null) {
			List<B2BUnitModel> allowedUnit = currentBaseSite.get().getAllowedUnit();
			for(B2BUnitModel b2BUnitModel : allowedUnit){
				final String REQUIRED_ROLE = ROLE_PREFIX + b2BUnitModel.getUid().toUpperCase();
				if (hasRole(REQUIRED_ROLE, auth)) {
					LOG.debug("Authorized as " + b2BUnitModel.getUid());
					isAllowed = true;
					break;
				}
			}
			if(!isAllowed){
				throw new AccessDeniedException("Access is denied , User Does not belong to current site");
			}

		}

		
		if(userService.getCurrentUser()!=null && userService.getCurrentUser() instanceof EyDmsUserModel) {
			EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
			if(currentUser.getUserType()!=null && currentUser.getUserType().equals(EyDmsUserType.SO)) {
				Collection<SubAreaMasterModel> subAreas = new ArrayList<SubAreaMasterModel>();
				final String territories = request.getParameter("territory");
				if(StringUtils.isNotBlank(territories) && !territories.equalsIgnoreCase("ALL")) {
					List<String> territoryList = Arrays.asList(territories.split(","));
					for(String territory: territoryList) {
						SubAreaMasterModel subArea = territoryDao.getTerritoryByIdInLocalView(territory);
						if(subArea!=null) {
							subAreas.add(subArea);
						}
					}
				}
				territoryService.setCurrentTerritory(subAreas);
			}

			else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(EyDmsUserType.TSM)) {
				Collection<DistrictMasterModel> districtMasterModels = new ArrayList<>();
				final String districts = request.getParameter("district");
				if(StringUtils.isNotBlank(districts) && !districts.equalsIgnoreCase("ALL")) {
					List<String> districtList = Arrays.asList(districts.split(","));
					for(String district : districtList) {
						DistrictMasterModel districtMaster = districtMasterDao.getDistrictByCodeInLocalView(district);
						if(districtMaster!=null) {
							districtMasterModels.add(districtMaster);
						}
					}
				}
				territoryService.setCurrentDistrict(districtMasterModels);
			}

			else if(currentUser.getUserType()!=null && currentUser.getUserType().equals(EyDmsUserType.RH)) {
				Collection<RegionMasterModel> regionMasterModels = new ArrayList<>();
				final String regions = request.getParameter("region");
				if(StringUtils.isNotBlank(regions) && !regions.equalsIgnoreCase("ALL")) {
					List<String> regionList = Arrays.asList(regions.split(","));
					for (String region : regionList) {
						RegionMasterModel regionMaster = regionMasterDao.getRegionByCodeInLocalView(region);
						if(regionMaster != null) {
							regionMasterModels.add(regionMaster);
						}
					}
				}
				territoryService.setCurrentRegion(regionMasterModels);
			}

		}
		else if(userService.getCurrentUser()!=null && userService.getCurrentUser() instanceof EyDmsCustomerModel &&
				((EyDmsCustomerModel)userService.getCurrentUser()).getCounterType()!=null &&
				((EyDmsCustomerModel)userService.getCurrentUser()).getCounterType().equals(CounterType.SP)) {
			Collection<CustDepotMasterModel> custDepotModelList = new ArrayList<>();
			final String custDepots = request.getParameter("custDepots");
			if(StringUtils.isNotBlank(custDepots) && !custDepots.equalsIgnoreCase("ALL")) {
				List<String> custDepotList = Arrays.asList(custDepots.split(","));
				for (String custDepot : custDepotList) {
					CustDepotMasterModel custDepotModel = getCustDepotForCode(custDepot);
					if(custDepotModel != null) {
						custDepotModelList.add(custDepotModel);
					}
				}
			}
			territoryService.setCurrentCustDepot(custDepotModelList);
		}

		String loggedInUser = getSessionService().getAttribute("ACTING_USER_UID");
		final String userId =  getValue(request, regexp);
		boolean isUidsMatched = false;

		if(loggedInUser!=null && userId!=null && userService.getUserForUID(loggedInUser)!=null) {
			if(!loggedInUser.equals(userId)) {
				if(userService.getUserForUID(loggedInUser) instanceof EyDmsUserModel) {
					FilterTalukaData filterTalukaData = new FilterTalukaData();
					List<SubAreaMasterModel> subAreasList = territoryDao.getTalukaForUserInLocalView((B2BCustomerModel)userService.getUserForUID(loggedInUser),filterTalukaData);
					if(subAreasList!=null && !subAreasList.isEmpty()) {
						List<EyDmsCustomerModel> customersInASubAreaList = territoryDao.getAllCustomerForTerritoriesInLocalView(subAreasList);
						if(customersInASubAreaList!=null) {
							for(EyDmsCustomerModel customer : customersInASubAreaList) {
								if(customer!=null && customer.getUid().equals(userId)) {
									isUidsMatched = true;
									break;
								}
							}
						}
						if(!isUidsMatched) {
							throw new AccessDeniedException("Access is denied, the user doesn't belong to this employee");
						}
					}
				}
				else if(userService.getUserForUID(loggedInUser) instanceof EyDmsCustomerModel && 
						(((EyDmsCustomerModel)userService.getUserForUID(loggedInUser)).getCounterType()!=null && 
						((EyDmsCustomerModel)userService.getUserForUID(loggedInUser)).getCounterType().equals(CounterType.SP))) {
					List<EyDmsCustomerModel> customersForSP = territoryDao.getDealersForSPInLocalView((B2BCustomerModel)userService.getUserForUID(loggedInUser));
					if(customersForSP!=null) {
						for(EyDmsCustomerModel customer : customersForSP) {
							if(customer!=null && customer.getUid().equals(userId)) {
								isUidsMatched = true;
								break;
							}
						}
					}
					if(!isUidsMatched) {
						throw new AccessDeniedException("Access is denied, the user doesn't belong to this employee");
					}
				}
				else if (userService.getUserForUID(loggedInUser) instanceof EyDmsCustomerModel) {
					throw new AccessDeniedException("Access is denied, User IDs do not match");
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	protected Authentication getAuth()
	{
		return SecurityContextHolder.getContext().getAuthentication();
	}

	protected boolean hasRole(final String role, final Authentication auth)
	{
		if (auth != null)
		{
			for (final GrantedAuthority ga : auth.getAuthorities())
			{
				if (ga.getAuthority().equals(role))
				{
					return true;
				}
			}
		}
		return false;
	}

	private CustDepotMasterModel getCustDepotForCode(String code) {                                                                                            
		return (CustDepotMasterModel) sessionService.executeInLocalView(new SessionExecutionBody()  
		{                                                                                         
			@Override                                                                              
			public CustDepotMasterModel execute()                                                    
			{              
				try {
					searchRestrictionService.disableSearchRestrictions();
					return territoryService.getCustDepotForCode(code);
				}
				finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}                                                                                      
		});                                                                                       
	}
	public SessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
 	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public static final String BASE_SITES_ENDPOINT_PATH = "/basesites";

	protected boolean matchesUrl(final HttpServletRequest request, final String regexp)
	{
		final Matcher matcher = getMatcher(request, regexp);
		return matcher.find();
	}

	protected String getBaseSiteValue(final HttpServletRequest request, final String regexp)
	{
		if (BASE_SITES_ENDPOINT_PATH.equals(getPath(request)))
		{
			return null;
		}

		final Matcher matcher = getMatcher(request, regexp);
		if (matcher.find())
		{
			return matcher.group().substring(1);
		}
		return null;
	}

	protected String getValue(final HttpServletRequest request, final String regexp)
	{
		final Matcher matcher = getMatcher(request, regexp);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		return null;
	}

	protected String getValue(final HttpServletRequest request, final String regexp, final String groupName)
	{
		final Matcher matcher = getMatcher(request, regexp);
		if (matcher.find())
		{
			return matcher.group(groupName);
		}
		return null;
	}

	protected Matcher getMatcher(final HttpServletRequest request, final String regexp)
	{
		final Pattern pattern = Pattern.compile(regexp);
		final String path = getPath(request);
		return pattern.matcher(path);
	}

	protected String getPath(final HttpServletRequest request)
	{
		return StringUtils.defaultString(request.getPathInfo());
	}

	protected String updateStringValueFromRequest(final HttpServletRequest request, final String paramName,
			final String defaultValue)
	{
		final String requestParameterValue = getRequestParameterValue(request, paramName);
		if ("".equals(requestParameterValue))
		{
			return null;
		}
		return StringUtils.defaultIfBlank(requestParameterValue, defaultValue);
	}
	
	protected String getRequestParameterValue(final HttpServletRequest request, final String paramName)
	{
		return request.getParameter(paramName);
	}
}


