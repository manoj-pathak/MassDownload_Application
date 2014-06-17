package com.mercatus.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.Resource;
import javax.jms.MapMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercatus.Util.UrlShortener;
import com.mercatus.Util.ZipUtil;
import com.mercatus.service.cms.ContentManagementService;


@Service("documentService")
public class DocumentServiceImpl implements DocumentService 
{
	private static final Logger logger = Logger.getLogger(DocumentServiceImpl.class);

	@Autowired
	private ContentManagementService alfrescoService;

	@Resource(name="s3Service")
	private ContentManagementService s3Service;

	@Value("${docs.download.folder}")
	private String Download_Folder;

	@Value("${docs.zip.folder}")
	private String Zip_Folder;

	@Value("${s3.massDownload.folder}")
	private String s3MassDownloadFolder;

	@Value("${s3.download.url}")
	private String s3DownloadUrl;

	@Override
	public String downloadDocuments(MapMessage mapMessage) 
	{
		String downlodPathURL = "";
		try
		{
			String messageString  	= mapMessage.getString("jsonMessage");
			String folderCategory 	= mapMessage.getString("folderCategory");
			String zipFileName  	= mapMessage.getString("zipFileName");

			if(messageString != null && zipFileName != null)
			{
				String DATE_FORMAT 	 = "yyyyMMdd";
				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
				Calendar c1 		 = Calendar.getInstance(); 
				zipFileName 		 = zipFileName + "_" + sdf.format(c1.getTime())+".zip";

				logger.info("DocumentServiceImpl.downloadDocuments(): calling createZipFile method to download docs and create zip file.");
				
				//create zip file by downloading docs
				createZipFile(messageString, zipFileName);

				File file  		 = new File(Zip_Folder + File.separator + zipFileName);
				if(file.exists())
				{
					InputStream is = new FileInputStream(file);
					logger.info("Calling s3Service.createDocument() method to upload zip file on s3 storage.");

					//upload zip file on s3 server 
					String docPath = s3Service.createDocument(s3MassDownloadFolder, folderCategory, zipFileName, null, is);

					downlodPathURL = s3DownloadUrl +"/"+docPath;

					// shorten the downlodPathURL to share by Email
					if(downlodPathURL != null)
					{
						downlodPathURL = UrlShortener.shortenUrl(downlodPathURL);
					}
					
					logger.info("DocumentServiceImpl.downloadDocuments():  zip file has been uploaded successfully on shared path: "+downlodPathURL);
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("DocumentServiceImpl.downloadDocuments() : exception occured while downloading docs or uploading zip file.", e);
		}

		return downlodPathURL;
	}


	private void createZipFile(String messageString, String zipFileName)
	{
		try
		{
			//Cleanup download and zip folder 
			cleanUpDownloadFolder();

			String[] docPathArray = messageString.replace("[", "").replace("]","").replaceAll("\"", "").split(",");

			for(String repoPath : docPathArray)
			{
				String fileName = repoPath.substring(repoPath.lastIndexOf("/")+1);
				
				//get document from storage server  
				InputStream is 	 = alfrescoService.getDocumentStream(repoPath);

				File file  		 = new File(Download_Folder + File.separator +fileName);
				if(!file.exists())
					file.createNewFile();

				OutputStream out = new FileOutputStream(file);

				byte[] bytes = new byte[1024];
				while (is.read(bytes) != -1) 
				{
					out.write(bytes);
				}
				
				is.close();
				out.close();
			}

			//create zip file on system for uploading on S3 server
			ZipUtil.generateZip(Download_Folder, Zip_Folder + File.separator + zipFileName);
			
			logger.info("DocumentServiceImpl.createZipFile(): Zip file created successfully.");
		}
		catch (Exception e) 
		{
			logger.error("DocumentServiceImpl.createZipFile(): Exception occured while creating zip file or downloading docs.", e);
		}
	}


	private void cleanUpDownloadFolder()
	{
		try
		{
			File downloadFolder = new File(Download_Folder);
			if(downloadFolder.exists() && downloadFolder.isDirectory())
			{
				File [] files = downloadFolder.listFiles();
				for (File file : files)
				{
					boolean deleteFlag = file.delete();
				}
			}
			else
				downloadFolder.mkdir();
			
	
			//clean up zip files
			File zipFolder = new File(Zip_Folder);
			if(zipFolder.exists() && zipFolder.isDirectory())
			{
				File [] zipFiles = zipFolder.listFiles();
				for (File file : zipFiles)
				{
					boolean deleteFlag = file.delete();
				}	
			}
			else 
				zipFolder.mkdir();
			
			logger.info("DocumentServiceImpl.cleanUpDownloadFolder(): downloaded and zip folder cleanup successfully.");
		}
		catch (Exception e) 
		{
			logger.error("DocumentServiceImpl.cleanUpDownloadFolder(): Exception eccured while cleanup of download folder.", e);
		}
	}
}
