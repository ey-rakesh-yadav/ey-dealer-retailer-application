/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.core.actions.sms;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.task.RetryLaterException;



import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.eydms.core.model.SMSProcessModel;


/**
 * This example action checks the order for required data in the business process. Skipping this action may result in
 * failure in one of the subsequent steps of the process. The relation between the order and the business process is
 * defined in basecommerce extension through item OrderProcess. Therefore if your business process has to access the
 * order (a typical case), it is recommended to use the OrderProcess as a parentClass instead of the plain
 * BusinessProcess.
 */
public class SendSMSAction extends AbstractSimpleDecisionAction<SMSProcessModel>
{
//	private static final Logger LOG = Logger.getLogger(SendSMSAction.class);
	
	@Resource
	private ConfigurationService configurationService;
	
	@Resource
	BaseSiteService baseSiteService;


	private static String SMSSender(final String user, final String password, final String senderid,
			final String mobiles, final String sms) throws UnsupportedEncodingException, MalformedURLException, IOException
	{
		String rsp = "";
		
			// Construct The Post Data
		
			String data = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
			data += "&" + URLEncoder.encode("senderid", "UTF-8") + "=" + URLEncoder.encode(senderid, "UTF-8");
			data += "&" + URLEncoder.encode("mobiles", "UTF-8") + "=" + URLEncoder.encode(mobiles, "UTF-8");
			data += "&" + URLEncoder.encode("sms", "UTF-8") + "=" + URLEncoder.encode(sms, "UTF-8");
			
			String url1 = "http://eydmserp.getonesms.com/sendsms.jsp";
			url1 += "?" + data;

			final URL url = new URL(url1);
			final URLConnection connection = url.openConnection();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb = sb.append(line);
			}
			rsp = sb.toString();
		
		return rsp;
	}

	@Override
	public Transition executeAction(final SMSProcessModel process) throws IOException
	{
		BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
		String senderid = configurationService.getConfiguration().getString("eydmscore.sms.senderid");
		
		final String user = configurationService.getConfiguration().getString("eydmscore.sms.user");
		final String password = configurationService.getConfiguration().getString("eydmscore.sms.password");
		
		final String response = SMSSender(user, password, senderid, process.getNumber(), process.getMessageContent());
		
		process.setResponse(response);
		
//		LOG.info(response);
		return Transition.OK;

	}

}