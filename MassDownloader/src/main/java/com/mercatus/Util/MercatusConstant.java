package com.mercatus.Util;

import org.apache.activemq.ActiveMQConnectionFactory;

public class MercatusConstant {

	public static final String ALFRESCOPASSWORD = "synerzip";
	public static final String ALFRESCOUSER = "synerzip";
	
	public static final String ALFRESCOURL = "http://172.25.33.99:8080/alfresco/cmisatom";
	public static final String ALFRESCODOCUMENTPATH = "/mercatus-docs";
	
	
	public static final String MQ_BROKER_URL 	= ActiveMQConnectionFactory.DEFAULT_BROKER_URL;
	public static final String CONSUMER_QUEUE 	= "MASS_DOWNLOAD_REQ_QUEUE";
	public static final String PRODUCER_QUEUE 	= "MASS_DOWNLOAD_RES_QUEUE";
 
}
 