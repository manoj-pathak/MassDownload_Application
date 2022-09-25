package com.mercatus.Util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextUtil {

	private static ApplicationContext context = null;
	
	public static ApplicationContext getApplicationContext()
	{
		if(context == null)
		{
			context = new ClassPathXmlApplicationContext("applicationContext.xml");

//			context = new AnnotationConfigApplicationContext();		
		}
		
		return context;
	}
}
