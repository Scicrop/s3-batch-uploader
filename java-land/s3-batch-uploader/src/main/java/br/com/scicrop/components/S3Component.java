package br.com.scicrop.components;

import java.io.File;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
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
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.scicrop.agroapi.commons.exceptions.SciCropAgroApiException;

import br.com.scicrop.commons.Utils;
import br.com.scicrop.entities.AppProperties;

public class S3Component {

	private static S3Component INSTANCE = null;

	private static long transferred = 0;

	private S3Component(){}

	public AmazonS3 getS3Client(AppProperties appProperties){
		AWSCredentials credentials = new BasicAWSCredentials(appProperties.getAws_access_key_id(), appProperties.getAws_secret_access_key());

		AmazonS3 client = AmazonS3Client.builder().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(appProperties.getRegion()).build();


		return client;
	}


	public AmazonS3 getS3Client(String region, String access_key_id, String secret_access_key){
		AWSCredentials credentials = new BasicAWSCredentials(access_key_id, secret_access_key);

		AmazonS3 client = AmazonS3Client.builder().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();


		return client;
	}


	public static S3Component getInstance(){
		if(INSTANCE == null) INSTANCE = new S3Component();
		return INSTANCE;
	}

	public void upload(AppProperties appProperties, File uploadFile, String keyName, String md5) throws SciCropAgroApiException{
		AmazonS3 s3client = getS3Client(appProperties);


		try {

			String ext = uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1);
			if(md5 != null) {
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

			throw new SciCropAgroApiException(ase);

		} catch (AmazonClientException ace) {
			throw new SciCropAgroApiException("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network. Error Message: " + ace.getMessage());

		}
	}


	public void upload(AmazonS3 s3client, String bucketName, File uploadFile, String keyName, String md5, boolean delete) throws SciCropAgroApiException{


		if(uploadFile != null && uploadFile.exists() && uploadFile.isFile()){

			try {

				String ext = uploadFile.getName().substring(uploadFile.getName().lastIndexOf(".") + 1);
				if(md5 != null) {
					keyName = md5;
				}
				keyName += "."+ext;

				uploadFileWithListener(keyName, bucketName, uploadFile, s3client, delete);

			} catch (AmazonServiceException ase) {
				throw new SciCropAgroApiException(ase);

			} catch (AmazonClientException ace) {
				throw new SciCropAgroApiException("Caught an AmazonClientException, which " +
						"means the client encountered " +
						"an internal error while trying to " +
						"communicate with S3, " +
						"such as not being able to access the network. Error Message: " + ace.getMessage());

			}
		}else System.out.println("Invalid file: "+keyName);
	}

	public boolean isValidFile(AppProperties appProperties, String keyName) throws SciCropAgroApiException {

		AmazonS3 s3client = getS3Client(appProperties);

		return isValidFile(s3client, appProperties.getBucketnName(), keyName);
	}
	
	public boolean isValidFile(AmazonS3 s3client, String bucketName, String keyName) throws SciCropAgroApiException {

		boolean isValidFile = false;
		S3Object object = null;
		try {

			object = s3client.getObject(new GetObjectRequest(bucketName, keyName));
			ObjectMetadata objectMetadata = object.getObjectMetadata();
			if(objectMetadata.getContentLength() > 0) isValidFile = true;
			
		} catch (AmazonS3Exception s3e ) {
			
				isValidFile = false;
			
		} catch (SdkClientException s3ce){
			isValidFile = false;
		}finally {
			if(object != null)
				try {
					object.close();
				} catch (IOException e) {
					throw new SciCropAgroApiException(e);
				}
		}

		return isValidFile;
	}
	

	public void uploadFileWithListener(String key_name, String bucket_name, File uploadFile, AmazonS3 s3client) {
		uploadFileWithListener(key_name, bucket_name, uploadFile, s3client, false);
	}

	public void uploadFileWithListener(String key_name, String bucket_name, File uploadFile, AmazonS3 s3client, boolean delete) {

		TransferManager xfer_mgr = TransferManagerBuilder.standard().withS3Client(s3client).build();

		transferred = 0;
		Upload u = null;
		try {
			
			
			u = xfer_mgr.upload(bucket_name, key_name, uploadFile);			
			u.addProgressListener(new ProgressListener() {
				public void progressChanged(ProgressEvent e) {

					long tSum = transferred = transferred + e.getBytesTransferred();

					if(tSum < uploadFile.length()) transferred = tSum;
					else transferred = uploadFile.length();

					Utils.getInstance().printTransferProgress(bucket_name + "/" + uploadFile.getName(), uploadFile.length(), transferred);

				}
			});
			waitForCompletion(u);
			TransferState xfer_state = u.getState();
			if(xfer_state.compareTo(TransferState.Completed) == 0) {
				xfer_mgr.shutdownNow(false);

			}
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
		}
		
		if(u.isDone() && delete) uploadFile.delete();

	}


	private void waitForCompletion(Upload xfer)
	{
		try {
			xfer.waitForCompletion();
		} catch (AmazonServiceException e) {
			System.err.println("Amazon service error: " + e.getMessage());
			
		} catch (AmazonClientException e) {
			System.err.println("Amazon client error: " + e.getMessage());
			
		} catch (InterruptedException e) {
			System.err.println("Transfer interrupted: " + e.getMessage());
			
		}
	}

}
