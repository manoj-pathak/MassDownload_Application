package com.mercatus.service.cms;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


@Service("s3Service")
public class S3ServiceImpl implements ContentManagementService 
{

	private static final Logger LOG = Logger.getLogger(S3ServiceImpl.class);


	@Value("${s3.bucket.name}")
	private String s3BucketName;

	@Value("${s3.access.key}")
	private String accessKey;

	@Value("${s3.secret.key}")
	private String secretKey;

	@Value("${s3.download.folder}")
	private String s3DownloadFolder;

	private static AmazonS3 client;

	protected AmazonS3 getClient() 
	{
		if (client == null) {
			AWSCredentials myCredentials = new BasicAWSCredentials(accessKey, secretKey);
			client = new AmazonS3Client(myCredentials);
		}
		return client;
	}

	@Override
	public void deleteDocument(String path) 
	{
		try {
			getClient().deleteObject(new DeleteObjectRequest(s3BucketName, path));
		} catch (AmazonServiceException ase) {
			LOG.error("Caught an AmazonServiceException.", ase);

		} catch (AmazonClientException ace) {
			LOG.error("Caught an AmazonClientException.", ace);
		} catch (Exception e) {
			LOG.error("Can't delete document", e);
		}
	}

	@Override
	public InputStream getDocumentStream(String repoPath) 
	{
		InputStream objectData = null;
		try {
			S3Object object = getClient().getObject(new GetObjectRequest(s3BucketName, repoPath));
			objectData = object.getObjectContent();

			return objectData;

		} catch (Exception e) {
			LOG.error("Error getDocumentStream:" + repoPath, e);
		}

		return null;
	}

	@Override
	public String createDocument(String repoPath, String folder, String file, String docType, InputStream stream) 
	{
		String docPath = repoPath + "/" + folder + "/" + file;
		try 
		{
			PutObjectRequest putObjectReq = new PutObjectRequest(s3BucketName, docPath, stream, new ObjectMetadata());
			putObjectReq.setCannedAcl(CannedAccessControlList.PublicRead);

			getClient().putObject(putObjectReq);

		} catch (AmazonServiceException ase) {
			LOG.error("Caught an AmazonServiceException.", ase);

		} catch (AmazonClientException ace) {
			LOG.error("Caught an AmazonClientException.", ace);
		} catch (Exception e) {
			LOG.error("Can't create document", e);
		}

		return docPath;

	}




	/**
	 * @return the s3BucketName
	 */
	public String getS3BucketName() {
		return s3BucketName;
	}

	/**
	 * @param s3BucketName the s3BucketName to set
	 */
	public void setS3BucketName(String s3BucketName) {
		this.s3BucketName = s3BucketName;
	}

	/**
	 * @return the accessKey
	 */
	public String getAccessKey() {
		return accessKey;
	}

	/**
	 * @param accessKey the accessKey to set
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	/**
	 * @return the secretKey
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * @param secretKey the secretKey to set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	/**
	 * @return the s3DownloadFolder
	 */
	public String getS3DownloadFolder() {
		return s3DownloadFolder;
	}

	/**
	 * @param s3DownloadFolder the s3DownloadFolder to set
	 */
	public void setS3DownloadFolder(String s3DownloadFolder) {
		this.s3DownloadFolder = s3DownloadFolder;
	}

	/**
	 * @param client the client to set
	 */
	public static void setClient(AmazonS3 client) {
		S3ServiceImpl.client = client;
	}

}
