package com.scl.occ.security;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import de.hybris.platform.oauth2.services.impl.DefaultHybrisOpenIDTokenServices;
import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.transaction.annotation.Transactional;

public class SclHybrisOpenIDTokenServices extends DefaultHybrisOpenIDTokenServices {


    private static final Logger LOG = Logger.getLogger(SclHybrisOpenIDTokenServices.class);


    public SclHybrisOpenIDTokenServices() {
    }



    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) {

            OAuth2AccessToken accessToken = super.createAccessToken(authentication);
            LOG.info("New Oauth2 token :- "+ accessToken + " Refresh Token:-  "+accessToken.getRefreshToken() +" For User :- " +authentication.getUserAuthentication().getPrincipal().toString());
           return accessToken;
    }

    @Override
    @Transactional(
            noRollbackFor = {InvalidTokenException.class, InvalidGrantException.class}
    )
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
       try {
           OAuth2AccessToken accessToken = super.refreshAccessToken(refreshTokenValue,tokenRequest);
           LOG.info("Oauth2 token :- "+ accessToken + " old Refresh Token:-  " + refreshTokenValue + " New Refresh Token:-  "+accessToken.getRefreshToken() +" For User :- "+ tokenRequest.getRequestParameters().get("username") );
           return accessToken;
       }catch (InvalidGrantException e){
           LOG.info("call 2 Oauth2 Refresh Token call failure with HttpErrorCode :-" + e.getHttpErrorCode());
           throw new UsernameNotFoundException(" Invalid/Not Found  Refresh Token ");
       }

    }


}
