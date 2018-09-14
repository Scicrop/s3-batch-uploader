package br.com.scicrop.components;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import br.com.scicrop.commons.TopLevelException;
import br.com.scicrop.commons.Utils;
import br.com.scicrop.entities.AppProperties;

public class S3Component {

	private static S3Component INSTANCE = null;

	private static long transfered = 0;

	private S3Component(){}

	public AmazonS3 getS3Client(AppProperties appProperties){
		AWSCredentials credentials = new BasicAWSCredentials(appProperties.getAws_access_key_id(), appProperties.getAws_secret_access_key());

		AmazonS3 client = AmazonS3Client.builder().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(appProperties.getRegion()).build();


		return client;
	}

	public static S3Component getInstance(){
		if(INSTANCE == null) INSTANCE = new S3Component();
		return INSTANCE;
	}

	public void upload(AppProperties appProperties, File uploadFile, String keyName,String md5, boolean md5Name) throws TopLevelException{
		AmazonS3 s3client = getS3Client(appProperties);


		try {

			String ext = uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1);
			if(md5Name == true) {
				keyName = md5;
			}
			keyName += "."+ext;

			uploadFileWithListener(keyName, appProperties.getBucketnName(), uploadFile, s3client);

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


	public void uploadFileWithListener(String key_name, String bucket_name, File uploadFile, AmazonS3 s3client) {

		TransferManager xfer_mgr = TransferManagerBuilder.standard().withS3Client(s3client).build();

		transfered = 0;

		try {
			Upload u = xfer_mgr.upload(bucket_name, key_name, uploadFile);			

			u.addProgressListener(new ProgressListener() {
				public void progressChanged(ProgressEvent e) {

					long tSum = transfered = transfered + e.getBytesTransferred();

					if(tSum < uploadFile.length()) transfered = tSum;
					else transfered = uploadFile.length();

					//System.out.println(uploadFile.getName() + ": "+uploadFile.length()+ " | " +transfered);

					Utils.getInstance().printTransferProgress(uploadFile.getName(), uploadFile.length(), transfered);

				}
			});
			waitForCompletion(u);
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}
		xfer_mgr.shutdownNow();
	}


	private void waitForCompletion(Upload xfer)
    {
        try {
            xfer.waitForCompletion();
        } catch (AmazonServiceException e) {
            System.err.println("Amazon service error: " + e.getMessage());
            System.exit(1);
        } catch (AmazonClientException e) {
            System.err.println("Amazon client error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Transfer interrupted: " + e.getMessage());
            System.exit(1);
        }
    }

}
