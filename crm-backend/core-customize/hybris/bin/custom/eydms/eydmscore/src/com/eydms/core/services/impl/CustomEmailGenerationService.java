package com.eydms.core.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;

public class CustomEmailGenerationService extends DefaultEmailGenerationService{

	@Autowired
	ModelService modelService;
	
	@Override
	public EmailMessageModel generate(final BusinessProcessModel businessProcessModel, final EmailPageModel emailPageModel)
	{
		EmailMessageModel emailMessageModel = super.generate(businessProcessModel, emailPageModel);
		
		List<EmailAttachmentModel> emailAttachments = businessProcessModel.getEmailAttachments();
		emailMessageModel.setAttachments(emailAttachments);
		
		modelService.saveAll(emailMessageModel);
		
		return emailMessageModel;
	}
}
