package com.mercatus.main;

import java.util.concurrent.BlockingQueue;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.log4j.Logger;

import com.mercatus.Util.ApplicationContextUtil;
import com.mercatus.Util.MercatusConstant;
import com.mercatus.service.DocumentService;
import com.mercatus.service.DocumentServiceImpl;

public class Consumer implements Runnable
{

	private static final Logger log = Logger.getLogger(Consumer.class);

	private BlockingQueue<MapMessage> queue = null;
	private DocumentService documentService = null;
	private Connection connection         	= null;

   
	public Consumer(BlockingQueue<MapMessage> queue, Connection connection) 
	{
		this.queue 		= queue;
		this.connection = connection;
		documentService = (DocumentServiceImpl) ApplicationContextUtil.getApplicationContext().getBean(DocumentServiceImpl.class);
	}

	public void run() 
	{
		while(true)
		{
			try
			{
				Session session 		 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination destination  = session.createQueue(MercatusConstant.CONSUMER_QUEUE);
				MessageConsumer consumer = session.createConsumer(destination);

				Message message = consumer.receive(1000);

				if (message != null && (message instanceof MapMessage))
				{
					MapMessage mapMessage 	= (MapMessage) message;
					
					log.info(" Consumer.run() :  message received from MASS_DOWNLOAD_REQ_QUEUE.");
					
					String downloadedPath = documentService.downloadDocuments(mapMessage);
					
					log.info(" Consumer.run() : created zip file and got shared path to send back.");
					
					//creating mapMessage to send back response on MASS_DOWNLOAD_RES_QUEUE
					MapMessage outputMessage = session.createMapMessage();
					outputMessage.setString("messageType", mapMessage.getString("messageType"));
					outputMessage.setString("userInfo", mapMessage.getString("userInfo"));
					outputMessage.setString("jsonMessage", downloadedPath);
			       
					log.info(" Consumer.run() :  reponse message prepared to put on MASS_DOWNLOAD_RES_QUEUE.");
					//adding message in blocking deQueue to send it to producer for sending back response queue
					queue.put(outputMessage);
					Thread.sleep(1000);
				}

				consumer.close();
				session.close();
			} 
			catch (Exception e)
			{
				log.error("Consumer.run() : Exception occured while processing request message from queue.",e);
			}
		}
	}
}