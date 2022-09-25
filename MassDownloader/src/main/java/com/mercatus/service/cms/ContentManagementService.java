package com.mercatus.service.cms;

import java.io.InputStream;


/**
 * Alfresco Service methods
 * 
 * @author ashutoshk
 */

public interface ContentManagementService 
{
	
	/**
	 * This API is used to create document while uploading M-FMS file
	 * @param userInfo
	 * @param repoPath
	 * @param folder
	 * @param file
	 * @param docType
	 * @param stream
	 * @return returns document path of document
	 */
	String createDocument(String repoPath, String folder, String file, String docType, InputStream stream);
	
	 
	/**
	 * This API is used to delete documents
	 * @param userInfo
	 * @param docPath
	 */
	void deleteDocument(String docPath);
	
    /**
     * This API is used to get file while downloading FMS file
     * @param repoPath
     * @return InputStream
     */
    InputStream getDocumentStream(String repoPath);
 
}
