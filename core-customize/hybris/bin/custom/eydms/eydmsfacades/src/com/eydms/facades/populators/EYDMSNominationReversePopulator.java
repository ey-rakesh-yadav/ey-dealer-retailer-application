package com.eydms.facades.populators;

import com.eydms.core.model.NominationModel;
import com.eydms.core.model.UidMediaModel;
import com.eydms.facades.data.EYDMSImageData;
import com.eydms.facades.data.UIDData;
import com.eydms.facades.prosdealer.data.NominationData;
import com.eydms.facades.util.GenericMediaUtil;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Objects;

public class EYDMSNominationReversePopulator implements Populator<NominationData, NominationModel> {
	@Resource
	private Converter<MultipartFile, MediaModel> eydmsMediaReverseConverter;
	@Resource
	private Converter<UIDData, UidMediaModel> uidMediaReverseConverter;
	@Resource
	private KeyGenerator customCodeGenerator;
	@Resource
	private GenericMediaUtil genericMediaUtil;

	@Override
	public void populate(NominationData source, NominationModel target) throws ConversionException {
		target.setNomineeID(customCodeGenerator.generate().toString());
		target.setName(source.getName());
		target.setFathersName(source.getFathersName());
		target.setAddressLine1(source.getLine1());
		target.setAddressLine2(source.getLine2());
		target.setState(source.getState());
		target.setCity(source.getCity());
		target.setDistrict(source.getDistrict());
		target.setTaluka(source.getTaluka());
		target.setAadharCard(source.getAadharCard());
		target.setPanCard(source.getPanCard());
		target.setIdProofDoc(source.getIdProof());
		EYDMSImageData idProofDoc = source.getPanDoc();
		if (null != idProofDoc && null != idProofDoc.getByteStream() && null != idProofDoc.getFileName()
				&& !(idProofDoc.getFileName().isEmpty())) {
			target.setIdProof(eydmsMediaReverseConverter
				.convert(genericMediaUtil.getMultipartFile(idProofDoc.getByteStream(), idProofDoc.getFileName())));
		} /*else {
			target.setIdProof(new MediaModel());
		}*/
		if (Objects.nonNull(source.getUids())) {
			target.setUidMedias(uidMediaReverseConverter.convertAll(source.getUids()));
		}
	}

}
