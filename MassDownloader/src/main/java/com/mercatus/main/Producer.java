package com.mercatus.main;

import java.util.concurrent.BlockingQueue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.log4j.Logger;

import com.mercatus.Util.MercatusConstant;

public class Producer implements Runnable
{
	private static final Logger logger = Logger.getLogger(Producer.class);

	
	private  BlockingQueue<MapMessage> queue = null;
	private Connection connection	= null;

	public Producer(BlockingQueue<MapMessage> queue,Connection connection) 
	{
		this.queue 		= queue;
		this.connection = connection;		
	}

	public void run() 
	{
		while(true)
		{
			try 
			{
				if(!queue.isEmpty())
				{

					MapMessage mapMessage 	 = queue.take();
					logger.info("Producer.run() : got response message : "+mapMessage);
					
					Session session 		 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					Destination destination  = session.createQueue(MercatusConstant.PRODUCER_QUEUE);
					MessageProducer producer = session.createProducer(destination);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					
					//sending message to response queue
					producer.send(mapMessage);
					logger.info("Producer.run() : send response back to MASS_DOWNLOAD_RES_QUEUE successfully");

					producer.close();
					session.close();
				}
			}
			catch (Exception e) 
			{
				logger.error(" Producer.run() : Exception occured while putting message on response queue.", e);
			}
		}
	}
}