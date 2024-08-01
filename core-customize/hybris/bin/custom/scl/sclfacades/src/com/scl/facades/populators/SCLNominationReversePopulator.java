package com.scl.facades.populators;

import com.scl.core.model.NominationModel;
import com.scl.core.model.UidMediaModel;
import com.scl.facades.data.SCLImageData;
import com.scl.facades.data.UIDData;
import com.scl.facades.prosdealer.data.NominationData;
import com.scl.facades.util.GenericMediaUtil;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Objects;

public class SCLNominationReversePopulator implements Populator<NominationData, NominationModel> {
	@Resource
	private Converter<MultipartFile, MediaModel> sclMediaReverseConverter;
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
		SCLImageData idProofDoc = source.getPanDoc();
		if (null != idProofDoc && null != idProofDoc.getByteStream() && null != idProofDoc.getFileName()
				&& !(idProofDoc.getFileName().isEmpty())) {
			target.setIdProof(sclMediaReverseConverter
				.convert(genericMediaUtil.getMultipartFile(idProofDoc.getByteStream(), idProofDoc.getFileName())));
		} /*else {
			target.setIdProof(new MediaModel());
		}*/
		if (Objects.nonNull(source.getUids())) {
			target.setUidMedias(uidMediaReverseConverter.convertAll(source.getUids()));
		}
	}

}
