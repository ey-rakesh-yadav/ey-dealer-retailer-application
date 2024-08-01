package com.scl.core.oauth;


import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.TerritoryUserMappingModel;
import de.hybris.platform.audit.AuditableActions;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloConnection;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.LoginToken;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.spring.security.CoreAuthenticationProvider;
import de.hybris.platform.spring.security.CoreUserDetails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SclAuthenticationProvider extends CoreAuthenticationProvider {


    private static final Logger LOG = Logger.getLogger(SclAuthenticationProvider.class.getName());

    private static final String BAD_CREDENTIALS_CODE = "CoreAuthenticationProvider.badCredentials";
    private static final String BAD_CREDENTIALS_MESSAGE = "Bad credentials";

    @Autowired
    private TerritoryMasterDao territoryMasterDao;
    @Autowired
    private DataConstraintDao dataConstraintDao;

    @Autowired
    private UserService userService;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    public SclAuthenticationProvider() {
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }




    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (Registry.hasCurrentTenant() && JaloConnection.getInstance().isSystemInitialized()) {
            String username = authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
            UserDetails userDetails = null;

            try {
                userDetails = this.retrieveUser(username);
            } catch (UsernameNotFoundException var7) {
                throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"), var7);
            }

            this.validateUserDetails(userDetails);
            User user = this.createUser(userDetails);
            Object credential = authentication.getCredentials();
            AuditableActions.ActionBuilder auditActionBuilder = AuditableActions.builder();
            auditActionBuilder.withAttribute("user", user.getPK());
            if (credential instanceof String) {
                if (!user.checkPassword((String) credential)) {
                    throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
                }
            } else {
                if (!(credential instanceof LoginToken)) {
                    throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
                }

                auditActionBuilder.withAttribute("loginToken", true);
                if (!user.checkPassword((LoginToken) credential)) {
                    throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
                }
            }

            this.additionalAuthenticationChecks(userDetails, (AbstractAuthenticationToken) authentication);
            boolean isValidSO = Boolean.FALSE;
            UserModel userModel = userService.getUserForUID(username);
            if (userModel != null && userModel instanceof SclUserModel && ((SclUserModel) userModel).getUserType() != null && !(((SclUserModel) userModel).getUserType().equals(SclUserType.TSO))) {
                List<TerritoryUserMappingModel> territoryUserMappings = territoryMasterDao.getTerritoryUserMappingForUser((SclUserModel) userModel);
                if (CollectionUtils.isNotEmpty(territoryUserMappings)) {
                    if (territoryUserMappings.size() > 0) {
                        isValidSO = Boolean.TRUE;
                    }
                }
                if (!isValidSO) {
                    throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", dataConstraintDao.findVersionByConstraintName("NO_VALID_TERRITORY_USER_MAPPING_FOUND")));
                }
            }
            this.postAuthenticationChecks.check(userDetails);
            JaloSession.getCurrentSession().setUser(user);
            AuditableActions.audit(auditActionBuilder.withName("successful user authentication"));
            return this.createSuccessAuthentication(authentication, userDetails);
        } else {
            return this.createSuccessAuthentication(authentication, new CoreUserDetails("systemNotInitialized", "systemNotInitialized", true, false, true, true, Collections.EMPTY_LIST, (String)null));
        }
    }

/*
    protected void validateUserDetails(UserDetails userDetails) {
        try {
            this.getPreAuthenticationChecks().check(userDetails);
        } catch (JaloItemNotFoundException var3) {
            throw new BadCredentialsException(this.messages.getMessage("CoreAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
*/

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
        private DefaultPostAuthenticationChecks() {
        }

        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException(SclAuthenticationProvider.this.messages.getMessage("CoreAuthenticationProvider.credentialsExpired", "User credentials have expired"));
            }
        }
    }

}
