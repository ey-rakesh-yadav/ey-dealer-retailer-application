package com.scl.facades.populators;

import com.scl.core.model.UidMediaModel;
import com.scl.facades.data.UIDData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.log4j.Logger;

public class UidMediaPopulator implements Populator<UidMediaModel,UIDData> {
    Logger LOG=Logger.getLogger(UidMediaReversePopulator.class);
    @Override
    public void populate(UidMediaModel uidMediaModel,UIDData uidData) throws ConversionException {
        uidData.setUidNumber(uidMediaModel.getUidNumber());
        uidData.setRealFileName(uidMediaModel.getRealFileName());
    }
}
