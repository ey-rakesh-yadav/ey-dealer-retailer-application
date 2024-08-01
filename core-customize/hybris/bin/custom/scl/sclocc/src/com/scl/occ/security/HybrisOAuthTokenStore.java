package com.scl.occ.security;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import de.hybris.platform.oauth2.util.SHAGenerator;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.Utilities;
import de.hybris.platform.webservicescommons.model.OAuthAccessTokenModel;
import de.hybris.platform.webservicescommons.model.OAuthRefreshTokenModel;
import de.hybris.platform.webservicescommons.oauth2.token.OAuthTokenService;
import de.hybris.platform.webservicescommons.oauth2.token.dao.OAuthTokenDao;
import de.hybris.platform.webservicescommons.util.YSanitizer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class HybrisOAuthTokenStore implements TokenStore {
    private static final Logger LOG = Logger.getLogger(com.scl.occ.security.HybrisOAuthTokenStore.class);
    private final AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
    private OAuthTokenService oauthTokenService;

    @Autowired
    private SessionService sessionService;
    @Autowired
    private OAuthTokenDao oauthTokenDao;
    @Autowired
    private SearchRestrictionService searchRestrictionService;


    public HybrisOAuthTokenStore() {
    }

    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken accessToken = null;
        OAuthAccessTokenModel accessTokenModel = null;
        String authenticationId = this.authenticationKeyGenerator.extractKey(authentication);

        try {
            accessTokenModel = this.oauthTokenService.getAccessTokenForAuthentication(authenticationId);
            accessToken = this.deserializeAccessToken((byte[])accessTokenModel.getToken());
        } catch (ClassCastException | IllegalArgumentException var7) {
            LOG.warn("Could not extract access token for authentication " + authentication +  " For User :- " +authentication.getUserAuthentication().getPrincipal().toString());
            this.oauthTokenService.removeAccessTokenForAuthentication(authenticationId);
        } catch (UnknownIdentifierException var8) {
            if (LOG.isInfoEnabled()) {
                LOG.debug("Failed to find access token for authentication " + authentication +  " For User :- " +authentication.getUserAuthentication().getPrincipal().toString());
            }
        }

        try {
            if (accessToken != null && accessTokenModel != null && !StringUtils.equals(authenticationId, this.authenticationKeyGenerator.extractKey(this.deserializeAuthentication((byte[])accessTokenModel.getAuthentication())))) {
                this.replaceToken(authentication, accessToken);
            }
        } catch (ClassCastException | IllegalArgumentException var6) {
            this.replaceToken(authentication, accessToken);
        }

        return accessToken;
    }

    protected void replaceToken(OAuth2Authentication authentication, OAuth2AccessToken accessToken) {
        this.removeAccessToken(accessToken.getValue());
        this.storeAccessToken(accessToken, authentication);
    }

    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        OAuthRefreshTokenModel refreshTokenModel = null;
        if (token.getRefreshToken() != null) {
            String refreshTokenKey = this.extractTokenKey(token.getRefreshToken().getValue());

            try {
                refreshTokenModel = this.oauthTokenService.getRefreshToken(refreshTokenKey);
            } catch (UnknownIdentifierException var6) {
                refreshTokenModel = this.oauthTokenService.saveRefreshToken(refreshTokenKey, this.serializeRefreshToken(token.getRefreshToken()), this.serializeAuthentication(authentication));
            }
        }

        if (Config.getBoolean("oauth2.accesstoken.save.retry", true)) {
            this.tryToSaveAccessTokenWithRetryOnModelSavingException(token, authentication, refreshTokenModel);
        } else {
            this.saveAccessToken(token, authentication, refreshTokenModel);
        }

    }

    private void tryToSaveAccessTokenWithRetryOnModelSavingException(OAuth2AccessToken token, OAuth2Authentication authentication, OAuthRefreshTokenModel refreshTokenModel) {
        try {
            this.saveAccessToken(token, authentication, refreshTokenModel);
        } catch (ModelSavingException var5) {
            if (this.isSpringDuplicateKeyException(var5)) {
                this.saveAccessToken(token, authentication, refreshTokenModel);
            }
        }

    }

    private void saveAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication, OAuthRefreshTokenModel refreshTokenModel) {
        this.oauthTokenService.saveAccessToken(this.extractTokenKey(token.getValue()), this.serializeAccessToken(token), this.authenticationKeyGenerator.extractKey(authentication), this.serializeAuthentication(authentication), authentication.isClientOnly() ? null : authentication.getName(), authentication.getOAuth2Request().getClientId(), refreshTokenModel);
    }

    protected boolean isSpringDuplicateKeyException(Exception e) {
        return Utilities.getRootCauseOfType(e, DuplicateKeyException.class) != null;
    }

    public OAuth2AccessToken readAccessToken(String tokenValue) {
        OAuth2AccessToken accessToken = null;

        try {
            OAuthAccessTokenModel accessTokenModel = this.oauthTokenService.getAccessToken(this.extractTokenKey(tokenValue));
            accessToken = this.deserializeAccessToken((byte[])accessTokenModel.getToken());
            LOG.info("Oauth2 token Found :- "+ tokenValue + " , Refresh Token:-  "+accessToken.getRefreshToken() +" For User :- " +accessTokenModel.getUser().getUid());

        } catch (ClassCastException | IllegalArgumentException var4) {
            LOG.warn("Failed to deserialize access token for  " + YSanitizer.sanitize(tokenValue));
            this.removeAccessToken(tokenValue);
        } catch (UnknownIdentifierException var5) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to find access token for token " + YSanitizer.sanitize(tokenValue));
            }
        }

        return accessToken;
    }

    public void removeAccessToken(OAuth2AccessToken token) {
        this.removeAccessToken(token.getValue());
    }

    public void removeAccessToken(String tokenValue) {
        this.oauthTokenService.removeAccessToken(this.extractTokenKey(tokenValue));
    }

    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return this.readAuthentication(token.getValue());
    }

    public OAuth2Authentication readAuthentication(String token) {
        OAuth2Authentication authentication = null;

        try {
            OAuthAccessTokenModel accessTokenModel = this.oauthTokenService.getAccessToken(this.extractTokenKey(token));
            authentication = this.deserializeAuthentication((byte[])accessTokenModel.getAuthentication());
        } catch (ClassCastException | IllegalArgumentException var4) {
            LOG.warn("Failed to deserialize authentication for " + YSanitizer.sanitize(token));
            this.removeAccessToken(token);
        } catch (UnknownIdentifierException var5) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to find authentication for token " + YSanitizer.sanitize(token));
            }
        }

        return authentication;
    }

    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        this.oauthTokenService.saveRefreshToken(this.extractTokenKey(refreshToken.getValue()), this.serializeRefreshToken(refreshToken), this.serializeAuthentication(authentication));
    }


    @Transactional(
            noRollbackFor = {InvalidGrantException.class}
    )
    public OAuth2RefreshToken readRefreshToken(String token){
        OAuth2RefreshToken refreshToken = null;

        try {
            OAuthRefreshTokenModel refreshTokenModel = getSclRefreshToken(this.extractTokenKey(token));
            refreshToken = this.deserializeRefreshToken((byte[])refreshTokenModel.getToken());

        } catch (ClassCastException | IllegalArgumentException var4) {
            LOG.warn("Failed to deserialize refresh token for token " + YSanitizer.sanitize(token));
            this.removeRefreshToken(token);
        } catch (UnknownIdentifierException var5) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to find refresh token for token " + YSanitizer.sanitize(token));
            }
        } catch (InvalidGrantException e){
            LOG.info("call 1 Oauth2 Refresh Token call failure with HttpErrorCode :-" + e.getHttpErrorCode());
            throw new InvalidGrantException(" Invalid/Not Found  Refresh Token ");
        }

        return refreshToken;
    }

    @Transactional(
            noRollbackFor = {InvalidGrantException.class}
    )
    private OAuthRefreshTokenModel getSclRefreshToken(final String id) {
        ServicesUtil.validateParameterNotNull(id, "Parameter 'id' must not be null!");
        return (OAuthRefreshTokenModel)sessionService.executeInLocalView(new SessionExecutionBody() {
            public Object execute() {
                searchRestrictionService.disableSearchRestrictions();
                try {
                    return oauthTokenDao.findRefreshTokenById(id);
                } catch (ModelNotFoundException var2) {
                    LOG.info("OAuthRefreshTokenModel ModelNotFoundException found then   throw new InvalidGrantException :");
                    throw new InvalidGrantException(" Invalid/Not Found  Refresh Token ");
                }
            }
        });
    }

    public void removeRefreshToken(OAuth2RefreshToken token) {
        this.removeRefreshToken(token.getValue());
    }

    public void removeRefreshToken(String token) {
        try {
            this.oauthTokenService.removeRefreshToken(this.extractTokenKey(token));
        } catch (UnknownIdentifierException var3) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Tried to remove token that is not present in the database: " + YSanitizer.sanitize(token));
            }
        }

    }

    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return this.readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String value) {
        OAuth2Authentication authentication = null;

        try {
            OAuthRefreshTokenModel refreshTokenModel = this.oauthTokenService.getRefreshToken(this.extractTokenKey(value));
            authentication = this.deserializeAuthentication((byte[])refreshTokenModel.getAuthentication());
        } catch (ClassCastException | IllegalArgumentException var9) {
            LOG.warn("Failed to deserialize authentication for refresh token " + YSanitizer.sanitize(value));
            List<OAuthAccessTokenModel> accessTokenModelList = this.oauthTokenService.getAccessTokenListForRefreshToken(this.extractTokenKey(value));
            Iterator var5 = accessTokenModelList.iterator();

            while(var5.hasNext()) {
                OAuthAccessTokenModel accessTokenModel = (OAuthAccessTokenModel)var5.next();
                LOG.warn("Trying to deserialize authentication from parent access token " + accessTokenModel);

                try {
                    authentication = this.deserializeAuthentication((byte[])accessTokenModel.getAuthentication());
                    break;
                } catch (ClassCastException | IllegalArgumentException var8) {
                    LOG.warn("Failed to deserialize authentication for access token " + accessTokenModel);
                }
            }

            if (authentication == null) {
                this.removeAccessTokenUsingRefreshToken(value);
                this.removeRefreshToken(value);
                LOG.warn("Failed to deserialize authentication for access token");
            }
        } catch (UnknownIdentifierException var10) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to find refresh token for " + YSanitizer.sanitize(value));
            }
        }

        return authentication;
    }

    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        this.removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    public void removeAccessTokenUsingRefreshToken(String refreshToken) {
        this.oauthTokenService.removeAccessTokenUsingRefreshToken(this.extractTokenKey(refreshToken));
    }

    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        List<OAuth2AccessToken> accessTokenList = new ArrayList();
        List<OAuthAccessTokenModel> accessTokenModelList = this.oauthTokenService.getAccessTokensForClient(clientId);
        Iterator var5 = accessTokenModelList.iterator();

        while(var5.hasNext()) {
            OAuthAccessTokenModel accessTokenModel = (OAuthAccessTokenModel)var5.next();

            try {
                OAuth2AccessToken accessToken = this.deserializeAccessToken((byte[])accessTokenModel.getToken());
                accessTokenList.add(accessToken);
            } catch (ClassCastException | IllegalArgumentException var8) {
                LOG.warn("Failed to deserialize access token for client : " + YSanitizer.sanitize(clientId));
                this.oauthTokenService.removeAccessToken(accessTokenModel.getTokenId());
            }
        }

        return accessTokenList;
    }

    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        if (clientId == null) {
            return this.findTokensByUserName(userName);
        } else {
            List<OAuth2AccessToken> accessTokenList = new ArrayList();
            List<OAuthAccessTokenModel> accessTokenModelList = this.oauthTokenService.getAccessTokensForClientAndUser(clientId, userName);
            Iterator var6 = accessTokenModelList.iterator();

            while(var6.hasNext()) {
                OAuthAccessTokenModel accessTokenModel = (OAuthAccessTokenModel)var6.next();

                try {
                    OAuth2AccessToken accessToken = this.deserializeAccessToken((byte[])accessTokenModel.getToken());
                    accessTokenList.add(accessToken);
                } catch (ClassCastException | IllegalArgumentException var9) {
                    LOG.warn("Failed to deserialize access token for client : " + YSanitizer.sanitize(clientId));
                    this.oauthTokenService.removeAccessToken(accessTokenModel.getTokenId());
                }
            }

            return accessTokenList;
        }
    }

    public Collection<OAuth2AccessToken> findTokensByUserName(String userName) {
        List<OAuth2AccessToken> accessTokenList = new ArrayList();
        List<OAuthAccessTokenModel> accessTokenModelList = this.oauthTokenService.getAccessTokensForUser(userName);
        Iterator var5 = accessTokenModelList.iterator();

        while(var5.hasNext()) {
            OAuthAccessTokenModel accessTokenModel = (OAuthAccessTokenModel)var5.next();

            try {
                OAuth2AccessToken accessToken = this.deserializeAccessToken((byte[])accessTokenModel.getToken());
                accessTokenList.add(accessToken);
            } catch (ClassCastException | IllegalArgumentException var8) {
                LOG.warn("Failed to deserialize access token for user : " + YSanitizer.sanitize(userName));
                this.oauthTokenService.removeAccessToken(accessTokenModel.getTokenId());
            }
        }

        return accessTokenList;
    }

    protected String extractTokenKey(String value) {
        return SHAGenerator.generateSHA256Signature(value);
    }

    protected byte[] serializeAccessToken(OAuth2AccessToken token) {
        return SerializationUtils.serialize(token);
    }

    protected byte[] serializeRefreshToken(OAuth2RefreshToken token) {
        return SerializationUtils.serialize(token);
    }

    protected byte[] serializeAuthentication(OAuth2Authentication authentication) {
        return SerializationUtils.serialize(authentication);
    }

    protected OAuth2AccessToken deserializeAccessToken(byte[] token) {
        return (OAuth2AccessToken)SerializationUtils.deserialize(token);
    }

    protected OAuth2RefreshToken deserializeRefreshToken(byte[] token) {
        return (OAuth2RefreshToken)SerializationUtils.deserialize(token);
    }

    protected OAuth2Authentication deserializeAuthentication(byte[] authentication) {
        return (OAuth2Authentication)SerializationUtils.deserialize(authentication);
    }

    public OAuthTokenService getOauthTokenService() {
        return this.oauthTokenService;
    }

    @Required
    public void setOauthTokenService(OAuthTokenService oauthTokenService) {
        this.oauthTokenService = oauthTokenService;
    }
}
