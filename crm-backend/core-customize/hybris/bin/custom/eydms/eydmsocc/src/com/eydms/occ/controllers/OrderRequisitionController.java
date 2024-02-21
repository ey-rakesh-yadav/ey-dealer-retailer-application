package com.eydms.occ.controllers;

import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.facades.OrderRequisitionFacade;
import com.eydms.facades.data.OrderRequisitionData;
import com.eydms.facades.data.OrderRequisitionListData;
import com.eydms.occ.dto.OrderRequisitionListWsDTO;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservices.core.user.data.AddressDataList;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressListWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/eydmsInfluencer")
@ApiVersion("v2")
@Tag(name = "Order Requisition Controller")
    public class OrderRequisitionController extends EyDmsBaseController {

    @Autowired
    OrderRequisitionFacade orderRequisitionFacade;

	@Autowired
	EyDmsCustomerService eydmsCustomerService;
	
	@Resource(name = "userFacade")
	private UserFacade userFacade;
	
	public UserFacade getUserFacade() {
		return userFacade;
	}

	public void setUserFacade(UserFacade userFacade) {
		this.userFacade = userFacade;
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/saveOrderRequisitionDetails", method = RequestMethod.POST)
    @Operation(operationId = "saveOrderRequisitionDetails", summary = "Save Order Requisition Details")
    @ResponseBody
    @ApiBaseSiteIdParam
    public boolean saveOrderRequisitionDetails(@RequestBody OrderRequisitionData orderRequisitionData) throws CMSItemNotFoundException, ConversionException, ParseException {
        boolean result =  orderRequisitionFacade.saveOrderRequisitionDetails(orderRequisitionData);
        if(orderRequisitionData.getDeliveryAddress().getId()!=null) {
        	final AddressData addressData = orderRequisitionFacade.getAddressDataFromAddressModel(orderRequisitionData.getDeliveryAddress().getId(), orderRequisitionData.getFromCustomerUid());
        	if(addressData!=null) {
        		addressData.setShippingAddress(true);
        		addressData.setVisibleInAddressBook(true);

        		getUserFacade().addAddress(addressData);

        		eydmsCustomerService.triggerShipToPartyAddress(addressData.getId());
        	}
        }
		return result;
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getOrderRequisitionDetails", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public OrderRequisitionListWsDTO getOrderRequisitionDetails(@RequestParam(required = false) List<String> statuses, @RequestParam final String submitType
            , @RequestParam(required = false, defaultValue = "0") Integer fromMonth, @RequestParam(required = false, defaultValue = "0") Integer fromYear, @RequestParam(required = false, defaultValue = "0") Integer toMonth
            , @RequestParam(required = false, defaultValue = "0") Integer toYear, @RequestParam(required = false) String productCode, @RequestParam(required = false) String searchKey
            , @RequestParam(required = false) String requisitionId
            , @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
            , @Parameter(description = "Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
            , @Parameter(description = "Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
            , final HttpServletResponse response)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        OrderRequisitionListData listData = new OrderRequisitionListData();
        SearchPageData<OrderRequisitionData> respone = orderRequisitionFacade.getOrderRequisitionDetails(statuses, submitType, fromMonth, fromYear, toMonth, toYear, productCode, fields, searchPageData, requisitionId, searchKey);
        listData.setRequisitions(respone.getResults());

        if (respone.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
        }
        return getDataMapper().map(listData, OrderRequisitionListWsDTO.class, fields);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/updateOrderRequistionStatus", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public Boolean updateOrderRequistionStatus(@RequestParam String requisitionId, @RequestParam String status
            , @RequestParam(required = false, defaultValue = "0") Double receivedQty, @RequestParam(required = false) String cancelReason)
    {
        return orderRequisitionFacade.updateOrderRequistionStatus(requisitionId,status,receivedQty,cancelReason);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getAddressListForRetailer", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public AddressListWsDTO getAddressListForRetailer(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {
        final List<AddressData> addressList = orderRequisitionFacade.getAddressListForRetailer();
        final AddressDataList addressDataList = new AddressDataList();
        addressDataList.setAddresses(addressList);
        return getDataMapper().map(addressDataList, AddressListWsDTO.class, fields);
    }

}
