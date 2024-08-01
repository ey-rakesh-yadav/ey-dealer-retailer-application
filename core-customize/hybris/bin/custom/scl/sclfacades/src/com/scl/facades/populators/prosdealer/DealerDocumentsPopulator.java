package com.scl.facades.populators.prosdealer;

import com.scl.core.model.NominationModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.facades.prosdealer.data.NominationData;
import com.scl.facades.prosdealer.data.ProsDealerData;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public class DealerDocumentsPopulator implements Populator<ProspectiveDealerModel, ProsDealerData> {


    private Converter<MediaModel, ImageData> imageConverter;

    @Override
    public void populate(ProspectiveDealerModel source, ProsDealerData target) {
        MediaModel panCardMedia = source.getPanCardDoc();
        MediaModel gstDetailsMedia = source.getGstDetailsDoc();
        MediaModel ddDetailsMedia = source.getDdDetailsDoc();
        MediaModel bankStatementMedia = source.getBankStatementDoc();
        MediaModel letterHeadMedia = source.getLetterHeadCopyDoc();
        MediaModel blankChequeMedia = source.getBlankChequeDoc();
        MediaModel godownSpaceMedia = source.getGodownSpaceDoc();
        if(null!= panCardMedia){
            target.setPanCardDoc(getImageData(panCardMedia));
        }
        if(null!= gstDetailsMedia){
            target.setGstDetailsDoc(getImageData(gstDetailsMedia));
        }
        if(null!= ddDetailsMedia){
            target.setDdDeailsDoc(getImageData(ddDetailsMedia));
        }
        if(null!= bankStatementMedia){
            target.setBankStatementDoc(getImageData(bankStatementMedia));
        }
        if(null!= letterHeadMedia){
            target.setLetterHeadCopyDoc(getImageData(letterHeadMedia));
        }
        if(null!= blankChequeMedia){
            target.setBlankChequeDoc(getImageData(blankChequeMedia));
        }
        if(null!= godownSpaceMedia){
            target.setGodownSpaceDoc(getImageData(godownSpaceMedia));
        }
    }

    private ImageData getImageData(MediaModel mediaModel) {
        return getImageConverter().convert(mediaModel);
    }

    public Converter<MediaModel, ImageData> getImageConverter() {
        return imageConverter;
    }

    public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
        this.imageConverter = imageConverter;
    }
}
