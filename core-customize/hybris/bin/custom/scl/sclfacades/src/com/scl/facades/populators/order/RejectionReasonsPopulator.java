package com.scl.facades.populators.order;

import com.scl.core.model.RejectionReasonModel;
import com.scl.facades.data.RejectionReasonData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

public class RejectionReasonsPopulator implements Populator<RejectionReasonModel, RejectionReasonData> {

    /**
     * Populate the target instance with values from the source instance.
     *
     * @param source the source object
     * @param target  the target to fill
     * @throws ConversionException if an error occurs
     */
    @Override
    public void populate(RejectionReasonModel source, RejectionReasonData target) throws ConversionException {
        if (StringUtils.isNotBlank(source.getCode())) {
            target.setCode(source.getCode());
        }
        if (StringUtils.isNotBlank(source.getName())) {
            target.setName(source.getName());
        }
    }
}
