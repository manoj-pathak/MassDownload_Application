package com.mercatus.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
/**
 * Util class to create zip file
 * @author pravinr
 *
 */

public class ZipUtil
{
	private static final Logger LOG = Logger.getLogger(ZipUtil.class);
	static List<String> fileList = null;

	/**
	 * Zip all files exist in source folder
	 * @param zipFile output ZIP file location
	 */
	public static void generateZip(String sourceFolder, String zipFile)
	{
		fileList = new ArrayList<String>();

		generateFileList(new File(sourceFolder), sourceFolder);

		byte[] buffer = new byte[1024];

		try{

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			LOG.debug("Output to Zip : " + zipFile);

			for(String file : fileList){ 
				LOG.debug("File Added : " + file);
				ZipEntry ze= new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(sourceFolder + File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				} 
				in.close();
			}

			zos.closeEntry();
			//close it
			zos.close();

			LOG.debug("Zip file created successfuly");
		}catch(IOException ex){
			LOG.error("Error occurred creating Zip file",ex);

		}
	}

	/**
	 * Traverse a directory and get all files,
	 * and add the file into fileList  
	 * @param node file or directory
	 */
	public static void generateFileList(File node, String sourceFilePath)
	{ 
		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				fileList.add(filename);           
			}
		} 
	}


}


