package com.scl.core.user.impl;

import com.scl.core.constants.SclCoreConstants;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.user.AbstractUserAuditModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.model.user.UserPasswordChangeAuditModel;
import de.hybris.platform.servicelayer.i18n.L10NService;
import de.hybris.platform.servicelayer.user.PasswordPolicy;
import de.hybris.platform.servicelayer.user.PasswordPolicyViolation;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.user.impl.DefaultPasswordPolicyViolation;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class OldPasswordPolicy implements PasswordPolicy {

    private UserService userService;
    private L10NService l10NService;
    private final String policyName;

    private static final String PASSWORD_NOT_SAME_AS_OLD = "password.policy.violation.old.password";
    private static final String PASSWORD_NOT_SAME_AS_OLD_KEY = "NotOld";

    public OldPasswordPolicy(String policyName) {
        this.policyName = policyName;
    }

    @Override
    public List<PasswordPolicyViolation> verifyPassword(UserModel userModel, String plainPassword, String encoding) {

        final int limit = Registry.getCurrentTenantNoFallback().getConfig().getInt(SclCoreConstants.CUSTOMER_ACCOUNT.LAST_PASSWORD_CHECK_LIMIT,3);
        List<PasswordPolicyViolation> violations = new ArrayList();

        List<AbstractUserAuditModel> userAudits = getUserService().getUserAudits(userModel);
        List<UserPasswordChangeAuditModel> userPasswordChangeAudits =
                Optional.ofNullable(userAudits).orElseGet(Collections:: emptyList).stream()
                        .filter(UserPasswordChangeAuditModel.class :: isInstance)
                        .map(UserPasswordChangeAuditModel.class::cast)
                        .collect(Collectors.toList());
        List<UserPasswordChangeAuditModel> lastPasswordChangeAudits =
                userPasswordChangeAudits.stream()
                        .sorted(Comparator.comparing(UserPasswordChangeAuditModel :: getCreationtime).reversed()).limit(limit).collect(Collectors.toList());

        String localizedMsg;
        if(CollectionUtils.isNotEmpty(lastPasswordChangeAudits)){
            for(UserPasswordChangeAuditModel passwordChangeAuditModel : lastPasswordChangeAudits ){
                if(getUserService().isPasswordIdenticalToAudited(userModel,plainPassword,passwordChangeAuditModel)){
                    localizedMsg = getL10NService().getLocalizedString(PASSWORD_NOT_SAME_AS_OLD );
                    violations.add(new DefaultPasswordPolicyViolation(PASSWORD_NOT_SAME_AS_OLD_KEY, localizedMsg));
                    break;
                }
            }
        }
        return violations;
    }

    @Override
    public String getPolicyName() {
        return this.policyName;
    }
    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    public L10NService getL10NService() {
        return l10NService;
    }

    public void setL10NService(L10NService l10NService) {
        this.l10NService = l10NService;
    }
}
