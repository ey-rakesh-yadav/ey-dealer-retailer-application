/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.occ.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.eydms.core.exceptions.UserNotAssignedException;
import com.eydms.facades.exception.EyDmsException;

import de.hybris.platform.cmsfacades.factory.ErrorFactory;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.search.pagedata.PaginationData;
import de.hybris.platform.commercewebservicescommons.dto.search.pagedata.PaginationWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.PasswordPolicyViolation;
import de.hybris.platform.servicelayer.user.exceptions.PasswordPolicyViolationException;
import de.hybris.platform.util.Config;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import de.hybris.platform.webservicescommons.util.YSanitizer;


/**
 * Base Controller. It defines the exception handler to be used by all controllers. Extending controllers can add or
 * overwrite the exception handler if needed.
 *
 * Duplicate of de.hybris.platform.commercewebservices.core.v2.controller.BaseController as b2bocc does not depend on ycommercewebservice
 */
@Controller
public class EyDmsBaseController
{
    protected static final String DEFAULT_PAGE_SIZE = "20";
    protected static final String DEFAULT_CURRENT_PAGE = "0";
    protected static final String BASIC_FIELD_SET = FieldSetLevelHelper.BASIC_LEVEL;
    protected static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    protected static final String FULL_FIELD_SET = FieldSetLevelHelper.FULL_LEVEL;
    protected static final String HEADER_TOTAL_COUNT = "X-Total-Count";
    protected static final String INVALID_REQUEST_BODY_ERROR_MESSAGE = "Invalid request body";
    protected static final String FILE_EMPTY_ERROR =  "File can not be empty";
    protected static final String INVALID_FILE_TYPE_ERROR =  "Please upload valid .png/.jpeg /.jpg /.pdf file only";
    protected static final String DOC_SIZE_MAX_UPLOAD_SIZE_ERROR  = "Please upload a file with size less than 10 MB";
    protected static final String PNG_MIME_TYPE    =  "image/png";
    protected static final String JPEG_MIME_TYPE   =  "image/jpeg";
    protected static final String JPG_MIME_TYPE    =  "image/jpg";
    protected static final String PDF_MIME_TYPE    =  "application/pdf";
    protected static final String DOCX_MIME_TYPE    =  "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    protected static final String DOC_MIME_TYPE    =  "application/msword";
    protected static final long   FIVE_MB_IN_BYTES =   5242880;

    private static final Logger LOG = Logger.getLogger(EyDmsBaseController.class);

	@Resource(name = "commerceWebServicesCartFacade2")
	private CartFacade commerceCartFacade;
	
    @Resource(name = "dataMapper")
    private DataMapper dataMapper;

    @Resource(name = "validatorErrorFactory")
    private ErrorFactory validatorErrorFactory;


    private static final String MAX_PAGE_SIZE_KEY = "webservicescommons.pagination.maxPageSize";

    @Autowired
    WebPaginationUtils webPaginationUtils;
    
    
    protected static String logParam(final String paramName, final long paramValue)
    {
        return paramName + " = " + paramValue;
    }

    protected static String logParam(final String paramName, final Long paramValue)
    {
        return paramName + " = " + paramValue;
    }

    protected static String logParam(final String paramName, final String paramValue)
    {
        return paramName + " = " + logValue(paramValue);
    }

    protected static String logValue(final String paramValue)
    {
        return "'" + sanitize(paramValue) + "'";
    }

    protected static String sanitize(final String input)
    {
        return YSanitizer.sanitize(input);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ ModelNotFoundException.class })
    public ErrorListWsDTO handleModelNotFoundException(final Exception ex)
    {
        LOG.info("Handling Exception for this request - " + ex.getClass().getSimpleName() + " - " + sanitize(ex.getMessage()));
        if (LOG.isDebugEnabled())
        {
            LOG.debug(ex);
        }

        return handleErrorInternal(UnknownIdentifierException.class.getSimpleName(), ex.getMessage());
    }

    protected ErrorListWsDTO handleErrorInternal(final String type, final String message)
    {
        final ErrorListWsDTO errorListDto = new ErrorListWsDTO();
        final ErrorWsDTO error = new ErrorWsDTO();
        error.setType(type.replace("Exception", "Error"));
        error.setMessage(sanitize(message));
        errorListDto.setErrors(Lists.newArrayList(error));
        return errorListDto;
    }

    protected void validate(final Object object, final String objectName, final Validator validator)
    {
        final Errors errors = new BeanPropertyBindingResult(object, objectName);
        validator.validate(object, errors);
        if (errors.hasErrors())
        {
            throw new WebserviceValidationException(errors);
        }
    }
    protected void validateDocument(final MultipartFile file)
    {
        final Map<String, String> params = new HashMap<>();
        params.put("FILE_EMPTY_ERROR",FILE_EMPTY_ERROR);
        params.put("INVALID_FILE_TYPE_ERROR",INVALID_FILE_TYPE_ERROR);
        params.put("DOC_SIZE_MAX_UPLOAD_SIZE_ERROR",DOC_SIZE_MAX_UPLOAD_SIZE_ERROR);

        final Errors errors = new MapBindingResult(params, "params");

        if(file.isEmpty()){
            errors.rejectValue(params.get("FILE_EMPTY_ERROR"),FILE_EMPTY_ERROR);
        }
        else if(!(PNG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                ||  JPEG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                || JPG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                || DOC_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                || DOCX_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                || PDF_MIME_TYPE.equalsIgnoreCase(file.getContentType()))){

            errors.rejectValue(params.get("INVALID_FILE_TYPE_ERROR"),INVALID_FILE_TYPE_ERROR);

        }
        else if(file.getSize() > FIVE_MB_IN_BYTES){
            errors.rejectValue(params.get("DOC_SIZE_MAX_UPLOAD_SIZE_ERROR"),DOC_SIZE_MAX_UPLOAD_SIZE_ERROR);
            }
        if(errors.hasErrors()){
            throw new WebserviceValidationException(errors);
        }
    }

    /**
     * Adds pagination field to the 'fields' parameter
     *
     * @param fields
     * @return fields with pagination
     */
    protected String addPaginationField(final String fields)
    {
        String fieldsWithPagination = fields;

        if (StringUtils.isNotBlank(fieldsWithPagination))
        {
            fieldsWithPagination += ",";
        }
        fieldsWithPagination += "pagination";

        return fieldsWithPagination;
    }

    protected void setTotalCountHeader(final HttpServletResponse response, final PaginationWsDTO paginationDto)
    {
        if (paginationDto != null && paginationDto.getTotalResults() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(paginationDto.getTotalResults()));
        }
    }

    protected void setTotalCountHeader(final HttpServletResponse response, final PaginationData paginationDto)
    {
        if (paginationDto != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(paginationDto.getTotalNumberOfResults()));
        }
    }
    protected void setTotalCountHeaderNew(final HttpServletResponse response, final de.hybris.platform.core.servicelayer.data.PaginationData paginationData)
    {
        if (paginationData != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(paginationData.getTotalNumberOfResults()));
        }
    }

    protected DataMapper getDataMapper()
    {
        return dataMapper;
    }

    protected void setDataMapper(final DataMapper dataMapper)
    {
        this.dataMapper = dataMapper;
    }

    public ErrorFactory getValidatorErrorFactory() {
        return validatorErrorFactory;
    }

    public void setValidatorErrorFactory(ErrorFactory validatorErrorFactory) {
        this.validatorErrorFactory = validatorErrorFactory;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ DuplicateUidException.class })
    public ErrorListWsDTO handleDuplicateUidException(final DuplicateUidException ex)
    {
        LOG.debug("DuplicateUidException", ex);
        return handleErrorInternal("DuplicateUidException", ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ HttpMessageNotReadableException.class })
    public ErrorListWsDTO handleHttpMessageNotReadableException(final Exception ex)
    {
        LOG.debug(INVALID_REQUEST_BODY_ERROR_MESSAGE, ex);
        return handleErrorInternal(HttpMessageNotReadableException.class.getSimpleName(), INVALID_REQUEST_BODY_ERROR_MESSAGE);
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ AmbiguousIdentifierException.class })
    public ErrorListWsDTO handleAmbiguosIdentifierException(final AmbiguousIdentifierException ex)
    {
        LOG.debug("AmbiguousIdentifierException", ex);
        return handleErrorInternal("AmbiguousIdentifierException", ex.getMessage());
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ ClassCastException.class })
    public ErrorListWsDTO handleClassCastException(final ClassCastException ex)
    {
        LOG.debug("ClassCastException", ex);
        return handleErrorInternal("ClassCastException", ex.getMessage());
    }
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ UsernameNotFoundException.class })
    public ErrorListWsDTO handleUsernameNotFoundException(final UsernameNotFoundException ex)
    {
        LOG.debug("UsernameNotFoundException", ex);
        return handleErrorInternal("UsernameNotFoundException", ex.getMessage());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ UserNotAssignedException.class })
    public ErrorListWsDTO handleUserNotAssignedException(final UserNotAssignedException ex)
    {
        LOG.debug("UserNotAssignedException", ex);
        return handleErrorInternal("UserNotAssignedException", ex.getMessage());
    }
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ PasswordPolicyViolationException.class })
    public ErrorListWsDTO handlePasswordPolicyViolationException(final PasswordPolicyViolationException ex)
    {
        LOG.debug("PasswordPolicyViolationException", ex);
        List<PasswordPolicyViolation> passwordPolicyViolationList = ex.getPolicyViolations();
        return handleErrorInternal("PasswordMismatchException", passwordPolicyViolationList.size()>0? passwordPolicyViolationList.get(0).getLocalizedMessage():ex.getMessage());
    }

	public CartFacade getCommerceCartFacade() {
		return commerceCartFacade;
	}

	public void setCommerceCartFacade(CartFacade commerceCartFacade) {
		this.commerceCartFacade = commerceCartFacade;
	}
    
	protected CartData getSessionCart()
	{
		return commerceCartFacade.getSessionCart();
	}
    
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler({ EyDmsException.class })
    public ErrorListWsDTO handleEyDmsException(final EyDmsException ex)
    {
        LOG.debug("EyDmsException", ex);
        String type = "RuntimeException";
        if(ex.getMessage()!=null)
        	type = ex.getMessage();
        return handleErrorInternal(type, ExceptionUtils.getStackTrace(ex));
    }

    protected ErrorListWsDTO getEmptyRequestErrorMessage(){
        ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        List<ErrorWsDTO> errors = new ArrayList<>();
        ErrorWsDTO errorWsDTO = new ErrorWsDTO();
        errorWsDTO.setReason("Request is Invalid");
        errors.add(errorWsDTO);
        errorListWsDTO.setErrors(errors);
        return errorListWsDTO;
    }

    
    protected void recalculatePageSize(final SearchPageData searchPageData)
    {
        int pageSize = searchPageData.getPagination().getPageSize();
        if (pageSize <= 0)
        {
            final int maxPageSize = Config.getInt(MAX_PAGE_SIZE_KEY, 1000);
            pageSize = webPaginationUtils.getDefaultPageSize();
            pageSize = pageSize > maxPageSize ? maxPageSize : pageSize;
            searchPageData.getPagination().setPageSize(pageSize);
        }
    }

}
