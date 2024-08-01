package com.scl.core.translator;

import de.hybris.platform.impex.jalo.translators.AbstractValueTranslator;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class SclCustomerTranslator extends AbstractValueTranslator {

    @Resource
    KeyGenerator customCodeGenerator;

    @Override
    public Object importValue(String valueExpr, Item item) throws JaloInvalidParameterException {

        return String.valueOf(getCustomCodeGenerator().generate());

    }

    @Override
    public String exportValue(Object o) throws JaloInvalidParameterException {

        return o == null ? "" : o.toString();
    }

    public KeyGenerator getCustomCodeGenerator() {
        return customCodeGenerator;
    }

    public void setCustomCodeGenerator(KeyGenerator customCodeGenerator) {
        this.customCodeGenerator = customCodeGenerator;
    }

}
