package com.mercatus.service.cms;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service("alfrescoService")
public class AlfrescoServiceImpl implements ContentManagementService
{

    private static final Logger LOG = Logger.getLogger(AlfrescoServiceImpl.class);

    @Value("${alfresco.user}")
    private String alfrescoUser;

    @Value("${alfresco.password}")
    private String alfrescoPassword;

    @Value("${alfresco.url}")
    private String alfrescoUrl;

    @Value("${alfresco.documents.path}")
    private String alfrescoDocumentsPath;

    private SessionFactory factory;

    private Session getSession()
    {
        Map<String, String> parameter = new HashMap<String, String>();
        // user credentials
        parameter.put(SessionParameter.USER, alfrescoUser);
        parameter.put(SessionParameter.PASSWORD, alfrescoPassword);

        // connection settings
        parameter.put(SessionParameter.ATOMPUB_URL, alfrescoUrl);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        // set the alfresco object factory
        parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        if (factory == null)
        {
            factory = SessionFactoryImpl.newInstance();
        }

        // create session
        Session session = factory.getRepositories(parameter).get(0).createSession();

        return session;
    }

    @Override
    public void deleteDocument(String path)
    {
        try
        {
            Session session = getSession();
            Document doc = (Document) session.getObjectByPath(path);
            LOG.debug("Document deleted :" + doc.getName());

            doc.deleteAllVersions();
        } catch (Exception e)
        {
            LOG.error("Can't delete document", e);
        }
    }

    protected Folder createFolder(Session session, String folderName, String parent)
    {
        Folder folder = null;
        try
        {
            Folder currentFolder = (Folder) session.getObjectByPath(parent);

            Map<String, String> newFolderProps = new HashMap<String, String>();
            newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            newFolderProps.put(PropertyIds.NAME, folderName);
            folder = currentFolder.createFolder(newFolderProps, null, null, null, session.getDefaultContext());
            LOG.debug("folder created :" + parent + " - " + folderName);
        } 
        catch (Exception e)
        {
            LOG.error("Can't create folder:" + folderName, e);
        }
        return folder;
    }

    @Override
    public InputStream getDocumentStream(String repoPath)
    {
        try
        {
            Session session = getSession();
            Document doc = (Document) session.getObjectByPath(repoPath);
            return doc.getContentStream().getStream();

        } catch (Exception e)
        {
            LOG.error("Error getDocumentStream:" + repoPath, e);
        }
        return null;
    }

    @Override
    public String createDocument(String repoPath, String folder, String file, String docType, InputStream stream)
    {
        String docPath = null;
        try
        {

            if (docType == null)
            {
                docType = URLConnection.guessContentTypeFromName(file);

                if (docType == null)
                {
                    docType = "application/octet-stream";
                }
            }
            Session session = getSession();

            // first check if folder exists if not then create
            Folder currentFolder = null;
            if (folder != null && !checkFolderExists(session, repoPath + "/" + folder))
            {
                currentFolder = createFolder(session, folder, alfrescoDocumentsPath + "/" + repoPath);
            }

            String folderPath = alfrescoDocumentsPath + "/" + repoPath + (folder != null ? ("/" + folder) : "");
            currentFolder = (currentFolder == null) ? (Folder) session.getObjectByPath(folderPath) : currentFolder;

            Map<String, String> newDocProps = new HashMap<String, String>();
            newDocProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            newDocProps.put(PropertyIds.NAME, file);
            ContentStream contentStream = new ContentStreamImpl(file, null, docType, stream);
            Document doc = currentFolder.createDocument(newDocProps, contentStream, null, null, null, null, session.getDefaultContext());
            docPath = folderPath + "/" + file;
            LOG.debug("** Created Document");
            dumpProps(doc);
        }catch (Exception e)
        {
            LOG.error("Error createDocument:" + folder + ":" + file, e);
        }
        return docPath;

    }

    private void dumpProps(Document doc)
    {
        LOG.debug(" name: " + doc.getName());
        LOG.debug(" version label: " + doc.getVersionLabel());
        LOG.debug(" version series id: " + doc.getVersionSeriesId());
        LOG.debug(" checked out by: " + doc.getVersionSeriesCheckedOutBy());
        LOG.debug(" checked out id: " + doc.getVersionSeriesCheckedOutId());
        LOG.debug(" major version: " + doc.isMajorVersion());
        LOG.debug(" latest version: " + doc.isLatestVersion());
        LOG.debug(" latest major version: " + doc.isLatestMajorVersion());
        LOG.debug(" checkin comment: " + doc.getCheckinComment());
        LOG.debug(" content length: " + doc.getContentStreamLength());
    }

    protected String getFileName(String docPath)
    {
        int lastIndex = docPath.lastIndexOf('/');
        return (lastIndex != -1) ? docPath.substring(lastIndex + 1) : docPath;
    }

    protected String getFolderName(String docPath)
    {
        int lastIndex = docPath.lastIndexOf('/');
        return (lastIndex != -1) ? docPath.substring(0, lastIndex) : docPath;
    }

    protected boolean checkFolderExists(Session session, String folderName)
    {
        boolean result = false;
        try
        {
            String statement = "SELECT * FROM cmis:folder where cmis:path = '" + alfrescoDocumentsPath + "/" + folderName + "'";
            boolean searchAllVersions = false;
            ItemIterable<QueryResult> results = session.query(statement, searchAllVersions);

            if (results != null)
            {
                Iterator itr = results.iterator();
                if (itr != null && itr.hasNext())
                {
                    result = true;
                }
            }
        } catch (Exception cmie)
        {
            result = false;
        }
        return result;
    }

}
