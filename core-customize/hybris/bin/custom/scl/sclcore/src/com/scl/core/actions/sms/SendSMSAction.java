/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.core.actions.sms;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scl.core.dao.DataConstraintDao;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.user.UserModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.site.BaseSiteService;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.model.SMSProcessModel;
import org.springframework.web.client.HttpClientErrorException;


/**
 * This example action checks the order for required data in the business process. Skipping this action may result in
 * failure in one of the subsequent steps of the process. The relation between the order and the business process is
 * defined in basecommerce extension through item OrderProcess. Therefore if your business process has to access the
 * order (a typical case), it is recommended to use the OrderProcess as a parentClass instead of the plain
 * BusinessProcess.
 */
public class SendSMSAction extends AbstractSimpleDecisionAction<SMSProcessModel>
{
	private static final Logger LOG = Logger.getLogger(SendSMSAction.class);
	
	@Resource
	private ConfigurationService configurationService;
	@Autowired
	private DataConstraintDao dataConstraintDao;
	
	@Resource
	BaseSiteService baseSiteService;


	private String SMSSender(B2BCustomerModel processUser, final String mobile, final String sms) throws UnsupportedEncodingException, MalformedURLException, IOException
	{
		/*String rsp = "";
		
			// Construct The Post Data
		
			String data = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
			data += "&" + URLEncoder.encode("senderid", "UTF-8") + "=" + URLEncoder.encode(senderid, "UTF-8");
			data += "&" + URLEncoder.encode("mobiles", "UTF-8") + "=" + URLEncoder.encode(mobiles, "UTF-8");
			data += "&" + URLEncoder.encode("sms", "UTF-8") + "=" + URLEncoder.encode(sms, "UTF-8");
			
			String url1 = "http://sclerp.getonesms.com/sendsms.jsp";
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
		
		return rsp;*/
		String rsp = "";
		try {
			Map<String, Object> jsonData = new LinkedHashMap<>();
			jsonData.put("alias", "crm-udaan-connect-dealer-app");

			Map<String, Object> recipient = new LinkedHashMap<>();
			recipient.put("to", new String[]{mobile});
			jsonData.put("recipient", recipient);

			Map<String, Object> meta = new LinkedHashMap<>();
			meta.put("tags", new String[]{"tag1"});
			meta.put("service", "T");
			jsonData.put("meta", meta);

			Map<String, String> data = new LinkedHashMap<>();
			//String customerInfo = String.format("%s (%s)", processUser.getName(), processUser.getUid());
			data.put("var1", processUser.getUid());
			//data.put("var2", "number-" + mobile);
			data.put("var2", sms);
			Integer expireMinutes = dataConstraintDao.findDaysByConstraintName("SMS_CRM_EXPIRE_IN_MINUTES");
			data.put("var3", String.valueOf(expireMinutes));
			jsonData.put("data", data);

			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			String requestJson = gson.toJson(jsonData);
			LOG.info(String.format("Sending SMS Template JSON :: %s for Customer ::%s",requestJson,processUser.getUid()));

			String urlString = "http://portal.mobtexting.com/api/v2/sms/send/template";
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			connection.setRequestProperty("Accept", "application/json");
			String accessToken = dataConstraintDao.findVersionByConstraintName("SMS_CRM_DEALER_LOGIN_ACCESS_TOKEN");
			connection.setRequestProperty("Authorization", "Bearer " + accessToken);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = requestJson.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			int responseCode = connection.getResponseCode();
			StringBuilder response;
			if (responseCode == HttpURLConnection.HTTP_OK) {
				LOG.info(String.format("Generated SMS Template for Customer :%s",processUser.getUid()));
				try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
					response= new StringBuilder();
					String responseLine;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine);
					}
				}
			        rsp = connection.getResponseMessage();
				return rsp;
			}
			else {
				LOG.error("Error in Sending SMS Template. Server HTTP response: "+responseCode);
			}
		}
		catch (HttpClientErrorException.BadRequest badRequest) {
			LOG.error(String.format("Dealer request failed for SMS template",processUser.getUid(),
					badRequest.getResponseBodyAsString()));
			return  badRequest.getResponseBodyAsString();
		}
        return StringUtils.EMPTY;
    }

	@Override
	public Transition executeAction(final SMSProcessModel process) throws IOException
	{
		BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
		//String senderid = configurationService.getConfiguration().getString("sclcore.sms.senderid");
		
		//final String user = configurationService.getConfiguration().getString("sclcore.sms.user");
		//final String password = configurationService.getConfiguration().getString("sclcore.sms.password");
		
		final String response = SMSSender(process.getCustomer(), process.getNumber(), process.getOtp());
		
		process.setResponse(response);
		
//		LOG.info(response);
		return Transition.OK;

	}

}
