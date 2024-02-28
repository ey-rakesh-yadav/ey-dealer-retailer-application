package com.eydms.occ.security;

import groovy.util.logging.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Spring Web filter for logging request and response.
 * @see org.springframework.web.filter.AbstractRequestLoggingFilter
 * @see ContentCachingRequestWrapper
 * @see ContentCachingResponseWrapper
 */
@Slf4j
public class EYDMSOCCRequestAndResponseLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = Logger.getLogger( EYDMSOCCRequestAndResponseLoggingFilter.class.getName());

    private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
    );

    /**
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }
    }

    /**
     * Do filter wrapped.
     *
     * @param request     the request
     * @param response    the response
     * @param filterChain the filter chain
     * @throws ServletException the servlet exception
     * @throws IOException      the io exception
     */
    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain) throws ServletException, IOException {
        try {
            beforeRequest(request, response);
            filterChain.doFilter(request, response);
        }
        finally {
            afterRequest(request, response);
            response.copyBodyToResponse();
        }
    }

    /**
     * Before request.
     *
     * @param request  the request
     * @param response the response
     */
    protected void beforeRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (log.isInfoEnabled()) {
            logRequestHeader(request, request.getRemoteAddr() + "|>");
        }
    }

    /**
     * After request.
     *
     * @param request  the request
     * @param response the response
     */
    protected void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (log.isInfoEnabled()) {
            logRequestBody(request, request.getRemoteAddr() + "|>");
            logResponse(response, request.getRemoteAddr() + "|<");
        }
    }

    /**
     *
     * @param request
     * @param prefix
     */
    private static void logRequestHeader(ContentCachingRequestWrapper request, String prefix) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            log.info(String.format("prefix:{%s}, requestMethod:{%s}, requestURI:{%s}", prefix, request.getMethod(), request.getRequestURI()));
        } else {
            log.info(String.format("prefix:{%s}, requestMethod:{%s}, requestURI:{%s},queryString:{%s}", prefix, request.getMethod(), request.getRequestURI(), queryString));
        }
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                Collections.list(request.getHeaders(headerName)).forEach(headerValue ->
                        log.info(String.format("prefix:{%s}, headerName:{%s}: headerValue:{%s}", prefix, headerName, headerValue))));

    }

    /**
     *
     * @param request
     * @param prefix
     */
    private static void logRequestBody(ContentCachingRequestWrapper request, String prefix) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            logContent(content, "request",request.getContentType(), request.getCharacterEncoding(), prefix);
        }
    }

    /**
     *
     * @param response
     * @param prefix
     */
    private static void logResponse(ContentCachingResponseWrapper response, String prefix) {
        int status = response.getStatus();
        log.info(String.format("prefix:{%s} ,status:{%s}, reason:{%s}", prefix, status, HttpStatus.valueOf(status).getReasonPhrase()));
        response.getHeaderNames().forEach(headerName ->
                response.getHeaders(headerName).forEach(headerValue ->
                        log.info(String.format("prefix:{%s}, headerName{%s}: ,headerValue:{%s}", prefix, headerName, headerValue))));
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            logContent(content,"response", response.getContentType(), response.getCharacterEncoding(), prefix);
        }
    }

    /**
     *
     * @param content
     * @param contentType
     * @param contentEncoding
     * @param prefix
     */
    private static void logContent(final byte[] content,final String type, final String contentType, final String contentEncoding, final String prefix) {
		 final MediaType mediaType = MediaType.valueOf(contentType);
		 final boolean visible = VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
		 if (visible)
		 {
			 try
			 {
				 final String contentString = new String(content, contentEncoding);

				 log.info(String.format(type.equalsIgnoreCase("request")?"Request Body :: %s ":"Response Body :: %s ", contentString));
				 // Stream.of(contentString.split("\r\n|\r|\n")).forEach(line -> log.info(String.format("prefix:{%s} ,line:{%s}", prefix, line)));
			 }
			 catch (final UnsupportedEncodingException e)
			 {
				 log.info(String.format("prefix:{%s}, contentLength:[{%s} bytes content]", prefix, content.length));
			 }
		 }
		 else
		 {
			 log.info(String.format("prefix:{%s}, contentLength:[{%s} bytes content]", prefix, content.length));
		 }
	 }


    /**
     *
     * @param request
     * @return
     */
    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    /**
     *
     * @param response
     * @return
     */
    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}
