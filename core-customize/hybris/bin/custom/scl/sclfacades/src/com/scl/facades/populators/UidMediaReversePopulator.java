package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.UidMediaModel;
import com.scl.facades.data.UIDData;
import com.scl.facades.util.GenericMediaUtil;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

public class UidMediaReversePopulator implements Populator<UIDData, UidMediaModel> {
    Logger LOG=Logger.getLogger(UidMediaReversePopulator.class);
    @Resource
    private MediaService mediaService;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Resource
    private ModelService modelService;

    @Autowired
    CatalogVersionService catalogVersionService;

    @Autowired
    BaseSiteService baseSiteService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public void populate(UIDData uidData, UidMediaModel uidMediaModel) throws ConversionException {
        uidMediaModel.setCode("uid_"+uidData.getUidNumber());
        uidMediaModel.setUidNumber(uidData.getUidNumber());
        var imageMediaFolder = mediaService.getFolder(SclCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        uidMediaModel.setFolder(imageMediaFolder);
        var image=genericMediaUtil.getMultipartFile(uidData.getImage().getByteStream(),uidData.getImage().getFileName());
        uidMediaModel.setMime(image.getContentType());
        uidMediaModel.setRealFileName(image.getName());
        CatalogUnawareMediaModel documentMedia = null;
        try {
            documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(uidMediaModel.getCode());
        } catch (AmbiguousIdentifierException ex) {
            LOG.error("More than one media found with code : " + uidMediaModel.getCode());
            LOG.error("Removing duplicate media : " +  uidMediaModel.getCode());
            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
            duplicateMedia.setCode( uidMediaModel.getCode());
            List<CatalogUnawareMediaModel> duplicateMedias = flexibleSearchService.getModelsByExample(duplicateMedia);
            modelService.removeAll(duplicateMedias);
        } catch (UnknownIdentifierException uie) {
            if (LOG.isDebugEnabled()) {
                LOG.error("No Media found with code : " + uidMediaModel.getCode());
            }
        } finally {
            if (null == documentMedia) {
                documentMedia = modelService.create(CatalogUnawareMediaModel.class);
                documentMedia.setCode(uidMediaModel.getCode());
            }
        }

        modelService.save(uidMediaModel);
        try {
            mediaService.setStreamForMedia(uidMediaModel, image.getInputStream());
        } catch (IOException ioe) {
            LOG.error(ioe);
        }

    }
}
