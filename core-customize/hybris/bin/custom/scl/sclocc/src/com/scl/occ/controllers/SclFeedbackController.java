package com.scl.occ.controllers;

import com.scl.facades.data.FeedbackAndComplaintsData;
import com.scl.facades.data.FeedbackAndComplaintsListData;
import com.scl.facades.feedback.SclFeedbackFacade;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commercefacades.product.data.ReviewData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservices.core.product.data.ReviewDataList;
import de.hybris.platform.commercewebservicescommons.dto.product.ReviewListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.ReviewWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Controller class for Feedback Management
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/sclfeedback")
@ApiVersion("v2")
@Tag(name = "Scl Feedback Management")
public class SclFeedbackController extends SclBaseController {

    @Resource
    private SclFeedbackFacade sclFeedbackFacade;

    @Resource(name = "reviewDTOValidator")
    private Validator reviewDTOValidator;

    /**
     * Create a new feedback for customer , and order(if order code is present as well)
     * @param orderCode
     * @param review
     * @param fields
     * @return
     */
    @Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CLIENT })
    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Operation(operationId = "createFeedback", summary = "Creates a feedback.", description = "Creates a new feedback as a user.")
    @ApiBaseSiteIdAndUserIdParam
    public ReviewWsDTO createFeeback(
            @Parameter(description = "Order identifier") @RequestParam(required = false) final String orderCode,
            @Parameter(description = "Object contains review details like : rating, alias, headline, comment", required = true) @RequestBody final ReviewWsDTO review,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        validate(review, "review", reviewDTOValidator);
        final ReviewData reviewData = getDataMapper().map(review, ReviewData.class, "rating,headline,comment");
        final ReviewData reviewDataReturn = sclFeedbackFacade.postFeedback(orderCode, reviewData);
        return getDataMapper().map(reviewDataReturn, ReviewWsDTO.class, fields);
    }

    /**
     * Get the feedback for customer , and order(if order code is present as well)
     * @param orderCode
     * @param fields
     * @return
     */
    @Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CLIENT })
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(operationId = "getFeedback", summary = "Gets Customer a feedback.", description = "Gets feedbacks for the user.")
    @ApiBaseSiteIdAndUserIdParam
    public ReviewListWsDTO getCustomerFeedback(
            @Parameter(description = "Order identifier") @RequestParam(required = false) final String orderCode,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {

        final ReviewDataList reviewDatas = sclFeedbackFacade.getCustomerFeedback(orderCode);
        return getDataMapper().map(reviewDatas, ReviewListWsDTO.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/infoDeskFeedback", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean submitInfoDeskFeedback(@RequestBody FeedbackAndComplaintsData feedbackAndComplaintsData)
    {
        return sclFeedbackFacade.submitInfoDeskFeedback(feedbackAndComplaintsData);
    }

}
