package com.awss3.s3library;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.ld.sharedlibrary.lib.ErrorManager;

public class AWSAuthentication
{
	private AWSCredentials credentials = null;									//[~/.aws/credentials || C:\Users\<user>\.aws\credentials]
	private AWSSecurityTokenServiceClient awsSecurityTokenService = null;		//TEMP TOKEN FOR AUTH
	BasicSessionCredentials authCredentials = null;								//TEMP SESSION CREDENTIALS
	private ErrorManager errorManager = null;
	
	public AWSAuthentication()
	{
		this.errorManager = new ErrorManager();
	}
	
	public ErrorManager getErrorInformation()
	{
		return this.errorManager;
	}
	
	/*
	 * This function will set our Auth Credentials using our local ~.aws/credentials file.
	 * 
	 * @return	Boolean
	 */
	public boolean setCredentials()
	{
        try
        {
            this.credentials = new ProfileCredentialsProvider().getCredentials();
            this.awsSecurityTokenService = new AWSSecurityTokenServiceClient(this.credentials);
            return true;
        }
        catch (Exception e)
        {
        	this.errorManager.setErrorMessage("Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.");
        	return false;
        }
	}
	
	/*
	 * This function will set our Auth Credentials using our access_key_id and access_key for AWS.
	 * 
	 * @return	Boolean
	 */
	public boolean setCredentials(String key_id, String access_key)
	{
        //SET CREDENTIALS [IAM USER]
		try
		{
	        this.credentials = new BasicAWSCredentials(key_id, access_key);					//CHANGED
	        this.awsSecurityTokenService = new AWSSecurityTokenServiceClient(this.credentials);
	        return true;
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("Exception Authentication::setCredentials: " + e.getMessage() );
			return false;
		}
	}
	
	/*
	 * This function will process our Authentication Information so that we can then call our getAuthCredentials() to return our Auth Info.
	 * 
	 * @param	access_key_id		The access_key_id for AWS
	 * @param	access_key			The access_key for AWS
	 */
	public boolean processAuthentication(String access_key_id, String access_key)
	{
		boolean credentialStatus;
		
		//SET CREDENTIALS
		if(access_key_id == null || access_key == null)
			credentialStatus = this.setCredentials();
		else
			credentialStatus = this.setCredentials(access_key_id, access_key);
		
		//CHECK STATUS OF CREDENTIALS
		if(!credentialStatus)
			return false;

		this.authCredentials = this.getTemporarySessionCredentials();
        if(this.authCredentials == null)
        	return false;
        
        return true;
	}
	
	/*
	 * This function is used to return our Temporary Session Credentials
	 * 
	 * @return	BasicSessionCredentials
	 */
	public BasicSessionCredentials getAuthCredentials()
	{
		return this.authCredentials;
	}
	
	/*
	 * This function will generate a session token and return a basic session credential object to be used to make AWS Requests.
	 * 
	 * @return	BasicSessionCredentials
	 */
	public BasicSessionCredentials getTemporarySessionCredentials()
	{
		BasicSessionCredentials basic_session_creds = null;
		
		try
		{
	        //DETERMINE LENGTH OF VALID CREDENTIALS
	        GetSessionTokenRequest session_token_request = new GetSessionTokenRequest();
	        session_token_request.setDurationSeconds(3600);
	        
	        //GET SESSION TOKEN + SESSION CREDENTIALS
	        GetSessionTokenResult session_token_result = this.awsSecurityTokenService.getSessionToken(session_token_request);
	        Credentials session_creds = session_token_result.getCredentials();
	        
	        basic_session_creds = new BasicSessionCredentials(
	        		   session_creds.getAccessKeyId(),
	        		   session_creds.getSecretAccessKey(),
	        		   session_creds.getSessionToken());
		}
		catch(AmazonServiceException e)
		{
			if(e.getStatusCode() == 403)
				this.errorManager.setErrorMessage("Exception Authentication::getTemporarySessionCredentials(): Invalid Access Credentials!" );
			else
				this.errorManager.setErrorMessage("Exception Authentication::getTemporarySessionCredentials(): " + e.getLocalizedMessage() );
			return null;
		}
		catch(Exception e)
		{
			this.errorManager.setErrorMessage("Exception Authentication::getTemporarySessionCredentials() Error: " + e.getMessage() );
			return null;
		}
        return basic_session_creds;
	}
	
	
}
