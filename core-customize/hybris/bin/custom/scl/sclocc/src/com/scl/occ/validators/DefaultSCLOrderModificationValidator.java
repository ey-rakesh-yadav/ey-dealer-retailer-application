package com.scl.occ.validators;

import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.math.MathContext;

public class DefaultSCLOrderModificationValidator implements Validator {

    private static final String FIELD_REQUIRED_ERROR_CODE = "field.required";
    private static final String TOTAL_QUANTITY_NOT_MATCHING = "order.ordermodification.total.quantity.invalid";

    @Override
    public boolean supports(Class<?> aClass) {
        return OrderWsDTO.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        final OrderWsDTO orderWsDTO = (OrderWsDTO) target;
        if (StringUtils.isBlank(orderWsDTO.getCode())){
            errors.rejectValue("code", FIELD_REQUIRED_ERROR_CODE);
        }
        if (StringUtils.isBlank(orderWsDTO.getErpCityCode())){
            errors.rejectValue("erpCityCode", FIELD_REQUIRED_ERROR_CODE);
        }
        if (null == orderWsDTO.getTotalQuantity()){
            errors.rejectValue("totalQuantity", FIELD_REQUIRED_ERROR_CODE);
        }
        if (StringUtils.isBlank(orderWsDTO.getOrderSource())){
            errors.rejectValue("orderSource", FIELD_REQUIRED_ERROR_CODE);
        }
        if (StringUtils.isBlank(orderWsDTO.getModificationReason())){
            errors.rejectValue("modificationReason", FIELD_REQUIRED_ERROR_CODE);
        }

        Double entryQuantity = 0.0;
        for(OrderEntryWsDTO entry : orderWsDTO.getEntries()){
            if(null == entry.getProduct() || StringUtils.isBlank(entry.getProduct().getCode())){
                errors.rejectValue("product", FIELD_REQUIRED_ERROR_CODE);
            }
            if(null == entry.getQuantityMT()){
                errors.rejectValue("entryQuantity_"+entry.getEntryNumber(), FIELD_REQUIRED_ERROR_CODE);
            }
            if(StringUtils.isBlank(entry.getSelectedDeliveryDate())){
                errors.rejectValue("selectedDeliveryDate", FIELD_REQUIRED_ERROR_CODE);
            }
            if(StringUtils.isBlank(entry.getSelectedDeliverySlot())){
                errors.rejectValue("selectedDeliverySlot", FIELD_REQUIRED_ERROR_CODE);
            }
            entryQuantity += entry.getQuantityMT();
        }
        BigDecimal entryQuantityCmp = new BigDecimal(entryQuantity);
        BigDecimal totalQuantityCmp = new BigDecimal(orderWsDTO.getTotalQuantity());
        if (entryQuantityCmp.compareTo(totalQuantityCmp) != 0) {
            errors.rejectValue("totalQuantity", TOTAL_QUANTITY_NOT_MATCHING);
        }
    }
}
