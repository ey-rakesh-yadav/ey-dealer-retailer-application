package com.scl.facades.populators;
import com.scl.core.model.CompetitorProductModel;
import com.scl.facades.data.CompetitorProductData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class CompetitorProductPopulator implements Populator<CompetitorProductModel,CompetitorProductData> {


    /**
     * Populate the target instance with values from the source instance.
     *
     * @param competitorProductModel the source object
     * @param competitorProductData  the target to fill
     * @throws ConversionException if an error occurs
     */
    @Override
    public void populate(CompetitorProductModel competitorProductModel, CompetitorProductData competitorProductData) throws ConversionException {
        int retailsale= 0;
        int wholesale= 0;
        competitorProductData.setCode(competitorProductModel.getCode());
        competitorProductData.setName(competitorProductModel.getName());
        competitorProductData.setGrade(competitorProductModel.getGrade());
        competitorProductData.setPackaging(competitorProductModel.getPackaging());
        competitorProductData.setState(competitorProductModel.getState());
        competitorProductData.setActive(competitorProductModel.getActive());
        competitorProductData.setBrand(competitorProductModel.getBrand().getIsocode());
        competitorProductData.setWholeSale(wholesale);
        competitorProductData.setRetailSale(retailsale);

    }
}
