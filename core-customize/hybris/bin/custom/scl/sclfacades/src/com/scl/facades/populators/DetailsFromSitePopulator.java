package com.scl.facades.populators;


import com.scl.core.model.DetailsFromSiteModel;
import com.scl.facades.data.DetailsFromSiteData;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public class DetailsFromSitePopulator implements Populator<DetailsFromSiteModel, DetailsFromSiteData> {



    private Converter<MediaModel, ImageData> imageConverter;

    @Override
    public void populate(DetailsFromSiteModel source, DetailsFromSiteData target) throws ConversionException {
        target.setId(source.getId());
        target.setAreaOfTheSite(source.getAreaOfTheSite());
        target.setAdditionalComments(source.getAdditionalComments());
        target.setTeComment(source.getTeComment());
        target.setIsCallHappenedWithTE(source.getIsCallHappenedWithTE());
        target.setProductSampleAvailable(source.getProductSampleAvailable());

        List<MediaModel> list =  source.getUploadSitePicture();
        List<ImageData> imageList = new ArrayList<>();
        for (MediaModel mediaModel : list) {
            final ImageData img = getImageConverter().convert(mediaModel);
            imageList.add(img);
        }
        target.setSitePictures(imageList);
        //target.setUploadSitePicture();


    }

    public Converter<MediaModel, ImageData> getImageConverter() {
        return imageConverter;
    }

    public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
        this.imageConverter = imageConverter;
    }

}
