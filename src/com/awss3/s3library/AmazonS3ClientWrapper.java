package com.awss3.s3library;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class AmazonS3ClientWrapper
{
	private static AmazonS3 s3 = null;
	public static Region currentRegion = null;
	
	public AmazonS3ClientWrapper()
	{

	}

	/*
	 * This function will set our AmazonClient Instance to make use of BasicSessionCredentials for Authentication.
	 * This function will allow us to call getInstance(), instance of creating new Object Instances.
	 * 
	 * @param	credentials		The credentials to use for Authentication.
	 */
	public static void setInstance(BasicSessionCredentials credentials)
	{
		if(s3 == null)
		{
			s3 = new AmazonS3Client(credentials);
			s3.setRegion(Region.getRegion(Regions.US_EAST_1));			//THIS IS THE DEFAULT [http://docs.amazonaws.cn/en_us/AWSSdkDocsJava/latest/DeveloperGuide/init-ec2-client.html]
		}
	}
	
	/*
	 * This function will set our AWS Region, and store the information locally.
	 * 
	 * @param	myRegion		The region to make use of.
	 */
	public static void setRegion(Regions myRegion)
	{
		currentRegion = Region.getRegion(myRegion);
		s3.setRegion(Region.getRegion(myRegion));
	}
	
	/*
	 * This function will return our AmazonClient Instance, preventing the need for new instantiation.
	 * 
	 * @return	AmazonEC2Client
	 */
	public static AmazonS3 getInstance()
	{
		return s3;
	}
	
	/*
	 * This function will return the Region we are using.
	 */
	public static Region getRegion()
	{
		return currentRegion;
	}
}
