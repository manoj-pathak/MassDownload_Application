package com.mercatus.main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MapMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.mercatus.Util.MercatusConstant;

public class MassDownload
{

	private static final Logger log = Logger.getLogger(MassDownload.class);

	public static void main(String[] args) 
	{
		try
		{
//			loadProperties();
			
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(MercatusConstant.MQ_BROKER_URL);
			Connection connection  = connectionFactory.createConnection();
			connection.start();

			BlockingQueue<MapMessage> queue = new ArrayBlockingQueue<MapMessage>(1024);
			
			Consumer consumer = new Consumer(queue, connection);
			Producer producer = new Producer(queue, connection);

			new Thread(producer).start();
			new Thread(consumer).start();

			Thread.sleep(4000);
			
			log.info("MassDownloadApp.main(): application started listening on MASS_DOWNLOAD_REQ_QUEUE");
		}
		catch (Exception e) 
		{
			log.error("Failled while downloading documents.", e);
			e.printStackTrace();
		}
	}
	
/*	public static void loadProperties() throws IOException
	{
		Properties prop 	= new Properties();
    	InputStream input 	= MassDownloadApp.class.getClassLoader().getResourceAsStream("mercatus.properties");
    	
    	prop.load(input);
    	
    	MQ_BROKER_URL 	= prop.getProperty("activemq.url");
    	CONSUMER_QUEUE 	= prop.getProperty("activemq.consumerQueue"); 
    	PRODUCER_QUEUE 	= prop.getProperty("activemq.producerQueue");
    	
        //get the property value and print it out
        System.out.println(prop.getProperty("activemq.url"));
    	System.out.println(prop.getProperty("activemq.consumerQueue"));
	}*/
}



