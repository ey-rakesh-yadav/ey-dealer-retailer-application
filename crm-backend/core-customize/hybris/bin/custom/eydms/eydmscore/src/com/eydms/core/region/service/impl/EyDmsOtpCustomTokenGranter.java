package com.eydms.core.region.service.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsEndCustomerDao;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.services.SlctCrmIntegrationService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import de.hybris.platform.oauth2.provider.custom.DefaultCustomTokenGranter;

public class EyDmsOtpCustomTokenGranter extends DefaultCustomTokenGranter{

	private static final Logger LOG = Logger.getLogger(EyDmsOtpCustomTokenGranter.class.getName());

	private final AuthenticationManager authenticationManager;

	SlctCrmIntegrationService slctCrmIntegrationService;
	
	private static final String GRANT_TYPE = "custom";

	public EyDmsEndCustomerDao getEyDmsEndCustomerDao() {
		return eydmsEndCustomerDao;
	}

	public void setEyDmsEndCustomerDao(EyDmsEndCustomerDao eydmsEndCustomerDao) {
		this.eydmsEndCustomerDao = eydmsEndCustomerDao;
	}

	public SlctCrmIntegrationService getSlctCrmIntegrationService() {
		return slctCrmIntegrationService;
	}

	public void setSlctCrmIntegrationService(SlctCrmIntegrationService slctCrmIntegrationService) {
		this.slctCrmIntegrationService = slctCrmIntegrationService;
	}

	private EyDmsEndCustomerDao eydmsEndCustomerDao;
	
	protected EyDmsOtpCustomTokenGranter(AuthenticationManager authenticationManager,AuthorizationServerTokenServices tokenServices,
			ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
		super(tokenServices, clientDetailsService, requestFactory);
		this.authenticationManager = authenticationManager;
	}

	protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest){
		LOG.error("Test token granter");


		Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
		String username = parameters.get("username");
		String otp = parameters.get("otp");
		String isMobileLogin = parameters.get("isMobileLogin");
		// Protect from downstream leaks of password
		parameters.remove("otp");
		
		if(Objects.isNull(otp))
		{
			throw new InvalidGrantException("ERROR: OTP is null");
		}
		
		if(Objects.isNull(username))
		{
			throw new InvalidGrantException("ERROR: Username is null");
		}

		String uid = username;
		if(Objects.nonNull(isMobileLogin))
		{
			LOG.info("inside mobile login");
			B2BCustomerModel endCustomer = eydmsEndCustomerDao.getEndCustomerDetails(username);
			if(endCustomer ==  null)
			{
				throw new BadCredentialsException("Bad Credentials");
			}
			else
			{
				uid = endCustomer.getUid();
			}
		}
		else {
			B2BCustomerModel Customer = slctCrmIntegrationService.findCustomerByCustomerNo(username);
			if(Customer ==  null)
			{
				throw new BadCredentialsException("Bad Credentials");
			}
			else
			{
				uid = Customer.getUid();
			}
		}
		Authentication userAuth = new UsernamePasswordAuthenticationToken(uid, otp);
		((AbstractAuthenticationToken) userAuth).setDetails(parameters);
		try {
			userAuth = authenticationManager.authenticate(userAuth);
		}
		catch (AccountStatusException ase) {
			//covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
			throw new InvalidGrantException(ase.getMessage());
		}
		catch (BadCredentialsException e) {
			// If the username/password are wrong the spec says we should send 400/invalid grant
			throw new InvalidGrantException(e.getMessage());
		}
		catch (UsernameNotFoundException e) {
			// If the user is not found, report a generic error message
			throw new InvalidGrantException(e.getMessage());
		}
		if (userAuth == null || !userAuth.isAuthenticated()) {
			throw new InvalidGrantException("Could not authenticate user: " + username);
		}
		
		OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);		
		return new OAuth2Authentication(storedOAuth2Request, userAuth);
	
		//return super.getOAuth2Authentication(client, tokenRequest);
	}

	@Override
	protected void validateGrantType(String grantType, ClientDetails clientDetails) {
		if (!GRANT_TYPE.equals(grantType)) {
			throw new InvalidClientException("Unauthorized grant type: " + grantType);
		}
	}
}
