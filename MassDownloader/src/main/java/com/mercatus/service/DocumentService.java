package com.mercatus.service;

import javax.jms.MapMessage;

public interface DocumentService 
{
	/**
	 * This API is used to download multiple documents 
	 * by creating zip file on shared path  
	 * @param mapMessage
	 * @return shared path of zip file to download
	 */
	String downloadDocuments(MapMessage message);

}
