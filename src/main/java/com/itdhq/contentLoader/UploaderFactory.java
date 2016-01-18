package com.itdhq.contentLoader;

import org.apache.log4j.Logger;

/**
 * Future uploader fabric/pool
 * 
 * @author Derek Hulley
 */
public class UploaderFactory
{
	private Logger logger = Logger.getLogger(UploaderFactory.class);
	public void init()
	{
		logger.debug("I'm a potential uploaders factory/pool");
	}
}
