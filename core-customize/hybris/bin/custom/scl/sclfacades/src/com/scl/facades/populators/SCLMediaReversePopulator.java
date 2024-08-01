package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

public class SCLMediaReversePopulator implements Populator<MultipartFile, CatalogUnawareMediaModel> {
    Logger LOG=Logger.getLogger(SCLMediaReversePopulator.class);
    @Resource
    private KeyGenerator customCodeGenerator;
    @Resource
    private MediaService mediaService;
    @Resource
    private ModelService modelService;
    @Override
    public void populate(MultipartFile source, CatalogUnawareMediaModel target) throws ConversionException {
        target.setCode(customCodeGenerator.generate().toString());
        var imageMediaFolder = mediaService.getFolder(SclCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        target.setFolder(imageMediaFolder);
        target.setMime(source.getContentType());
        target.setRealFileName(source.getName());
        modelService.save(target);
        try {
            mediaService.setStreamForMedia(target, source.getInputStream());
        } catch (IOException ioe) {
            LOG.error(ioe);
        }
    }
}
