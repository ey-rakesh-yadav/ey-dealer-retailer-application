package com.scl.core.region.service.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SlctCrmIntegrationService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.spockframework.util.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.persistence.security.PasswordEncoderFactory;
import de.hybris.platform.spring.security.CoreAuthenticationProvider;
import com.scl.core.services.SmsOtpService;

import java.util.Objects;

public class SclOtpAuthenticationProvider  extends CoreAuthenticationProvider{//implements AuthenticationProvider, InitializingBean, MessageSourceAware {

	private static final Logger LOG = Logger.getLogger(SclOtpAuthenticationProvider.class.getName());


	@Autowired
	PasswordEncoderFactory passwordEncoderFactory;
	
	@Autowired 
	PasswordEncoder passwordEncoder;
	
	@Autowired 
	SmsOtpService smsOtpService;
	@Autowired
	UserService userService;

	@Autowired
	SlctCrmIntegrationService slctCrmIntegrationService;



	@Autowired
	SessionService sessionService;
	
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		LOG.error("Testing OTP API");
		LOG.error("Jrebel testing 111");

		String username = authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
		UserDetails userDetails = null;
		try {
			userDetails = this.retrieveUser(username);
		} catch (UsernameNotFoundException var6) {
			throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"), var6);
		}
		getPreAuthenticationChecks().check(userDetails);
		User user = UserManager.getInstance().getUserByLogin(userDetails.getUsername());
		if (authentication.getCredentials() instanceof String) {
			//String encodedOTP = passwordEncoder.encode("1234");
			//boolean check = passwordEncoder.matches((String)authentication.getCredentials(), encodedOTP);
			final String partnerID = sessionService.getAttribute("partnerID");
			boolean check = smsOtpService.validateOTP((String)authentication.getCredentials(),username,partnerID);
			if (!check) {
				throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
			}
		}
		JaloSession.getCurrentSession().setUser(user);
		return this.createSuccessAuthentication(authentication, userDetails);
	}


}
