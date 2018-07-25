package br.com.scicrop.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;

import br.com.scicrop.commons.TopLevelException;
import br.com.scicrop.commons.Utils;
import br.com.scicrop.entities.AppProperties;
import br.com.scicrop.entities.FileEntity;

public class S3Component {
	
	private static S3Component INSTANCE = null;
	
	private S3Component(){}
	
	public AmazonS3Client getS3Client(AppProperties appProperties){
		AWSCredentials credentials = new BasicAWSCredentials(appProperties.getAws_access_key_id(), appProperties.getAws_secret_access_key());
		return new AmazonS3Client(credentials);
	}
	
	public static S3Component getInstance(){
		if(INSTANCE == null) INSTANCE = new S3Component();
		return INSTANCE;
	}
	
	public void upload(AppProperties appProperties, File uploadFile, String keyName) throws TopLevelException{
		AmazonS3 s3client = getS3Client(appProperties);
        try {
        	Utils.getInstance().handleVerboseLog(appProperties, 'i', "Uploading a new object to S3 from a file\n");
            s3client.putObject(new PutObjectRequest(appProperties.getBucketnName(), keyName, uploadFile));
            
            FileInputStream fis = new FileInputStream(uploadFile);
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            fis.close();
            
            Date dt_file = new Date(uploadFile.lastModified());
            Date dt_today = new Date();
            String ip;
          
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            ip = socket.getLocalAddress().toString();
            
            	  
            FileEntity json = new FileEntity(0, uploadFile.getName(), md5, uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1), dt_file, dt_today,uploadFile.getAbsolutePath() ,ip);
            
            Gson gson = new Gson();
            String jsonString = gson.toJson(json);
             FileWriter writer = new FileWriter("/tmp/s3-uploader.json");
             writer.write(jsonString);
             writer.close();
            
            
            Utils.getInstance().handleVerboseLog(appProperties, 'i', "Uploading a new JSON object to S3 from a file\n");
            s3client.putObject(new PutObjectRequest(appProperties.getBucketnName(), uploadFile.getName()+".json", new File("/tmp/s3-uploader.json") ));
         } catch (AmazonServiceException ase) {
        	Utils.getInstance().handleVerboseLog(appProperties, 'e', "Caught an AmazonServiceException, which " +
            		"means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
        	Utils.getInstance().handleVerboseLog(appProperties, 'e', "Error Message:    " + ase.getMessage());
        	Utils.getInstance().handleVerboseLog(appProperties, 'e', "HTTP Status Code: " + ase.getStatusCode());
        	Utils.getInstance().handleVerboseLog(appProperties, 'e', "AWS Error Code:   " + ase.getErrorCode());
        	Utils.getInstance().handleVerboseLog(appProperties, 'e', "Error Type:       " + ase.getErrorType());
        	Utils.getInstance().handleVerboseLog(appProperties, 'e', "Request ID:       " + ase.getRequestId());
        	
        	throw new TopLevelException(appProperties, ase);
            
        } catch (AmazonClientException ace) {
        	throw new TopLevelException(appProperties, "Caught an AmazonClientException, which " +
            		"means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network. Error Message: " + ace.getMessage());

        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isValidFile(AppProperties appProperties, String keyName) throws TopLevelException {
		
		AmazonS3 s3client = getS3Client(appProperties);

		boolean isValidFile = true;
	    try {

	    	S3Object object = s3client.getObject(new GetObjectRequest(appProperties.getBucketnName(), keyName));
	    	ObjectMetadata objectMetadata = object.getObjectMetadata();
	    } catch (AmazonS3Exception s3e) {
	        if (s3e.getStatusCode() == 404) {
	            isValidFile = false;
	        }
	        else {
	            throw new TopLevelException(appProperties, s3e); 
	        }
	    }

	    return isValidFile;
	}

}
