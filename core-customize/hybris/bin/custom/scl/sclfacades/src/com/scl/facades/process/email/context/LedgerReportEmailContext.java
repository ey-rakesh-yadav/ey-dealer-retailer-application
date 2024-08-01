package com.scl.facades.process.email.context;



import com.scl.core.model.LedgerReportEmailProcessModel;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;

public class LedgerReportEmailContext extends CustomerEmailContext{

	private String content;
	
	@Override
	public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
	{
		super.init(storeFrontCustomerProcessModel, emailPageModel);
		if(storeFrontCustomerProcessModel instanceof LedgerReportEmailProcessModel)
		{
			setContent(((LedgerReportEmailProcessModel) storeFrontCustomerProcessModel).getContent());
		}
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
