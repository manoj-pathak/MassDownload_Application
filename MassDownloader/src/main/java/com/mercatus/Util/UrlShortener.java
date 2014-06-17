package com.mercatus.Util;
/**
 * Util class to shorten long URL
 * @author manojP
 *
 */

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.log4j.Logger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public final class UrlShortener 
{
	private static final Logger LOG = Logger.getLogger(UrlShortener.class);

	public static String shortenUrl(String longUrl)
	{
		String shortPath = longUrl;
		try
		{
			@SuppressWarnings("unused")
			OAuthService oAuthService = new ServiceBuilder().provider(GoogleApi.class).apiKey("anonymous").apiSecret("anonymous").scope("https://www.googleapis.com/auth/urlshortener") .build();
	
			OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, "https://www.googleapis.com/urlshortener/v1/url");
			oAuthRequest.addHeader("Content-Type", "application/json");
			String json = "{\"longUrl\": \""+longUrl+"\"}";
			oAuthRequest.addPayload(json);
			
			Response response 	= oAuthRequest.send();
			Type typeOfMap 		= new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> responseMap = new GsonBuilder().create().fromJson(response.getBody(), typeOfMap);
			
			shortPath	= responseMap.get("id");
			
			LOG.info("UrlShortener.shortenUrl(): short path created....");
		}
		catch(Exception e)
		{
			LOG.error("UrlShortener.shortenUrl(): Exception occured while shortening path.", e);
		}
		
		return shortPath;
	}

}