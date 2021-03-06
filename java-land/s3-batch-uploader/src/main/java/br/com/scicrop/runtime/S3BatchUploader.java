package br.com.scicrop.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.google.gson.Gson;
import com.scicrop.agroapi.commons.exceptions.SciCropAgroApiException;

import br.com.scicrop.commons.Constants;
import br.com.scicrop.commons.ManageProperties;
import br.com.scicrop.commons.Utils;
import br.com.scicrop.components.S3Component;
import br.com.scicrop.entities.AppProperties;
import br.com.scicrop.entities.FileEntity;



public class S3BatchUploader {

	public static long totalFiles;
	public static long uploadedFiles;
	public static long uploadedSize;
	public static long totalSize;
	public static long skippedFiles;

	public static void main(String[] args) {
		
		
		long initTime = new Date().getTime(); 
		
		FileWriter writer = null;
		String jsonFileName = "/tmp/s3-uploader.json";
		File jsonFile = null;
		FileInputStream fis = null;

		String propertiesFilePath = Constants.POSIX_DEFAULT_PROPERTIES_FILE_PATH;
		String logPath = Constants.LOG_PATH; 

		if(Utils.getInstance().isWindows()){
			propertiesFilePath = Utils.getInstance().getWorkingDir()+"\\"+Constants.WIN_DEFAULT_PROPERTIES_FILE_PATH;
			jsonFileName = Utils.getInstance().getWorkingDir()+"\\s3-uploader.json";
			logPath = Utils.getInstance().getWorkingDir()+"\\"+Constants.LOG_FILE_NAME;
		}

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
		rootLogger.addAppender(new ConsoleAppender(layout));
		try {

			RollingFileAppender fileAppender = new RollingFileAppender(layout, logPath);
			rootLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.err.println("Failed to find/access "+logPath+" !");
		}

		if(args != null && args.length > 0){
			propertiesFilePath = args[0];
		}

		AppProperties appProperties = null;
		try {
			appProperties = ManageProperties.getInstance().getAppProperties(propertiesFilePath);
		} catch (SciCropAgroApiException e) {
			System.err.println("********************************************************************************************");
			System.err.println("Unable to find properties file: "+propertiesFilePath);
			System.err.println("********************************************************************************************");
			System.exit(1);
		}

		try {

			File folder = new File(appProperties.getFolder());
			if(!folder.isDirectory()) throw new Exception("Folder property is not a directory!");
			else{

				String ext = appProperties.getFileextension().trim();


				String[] exts = null;

				if(null == ext || ext.equals("") || ext.equals("*") || ext.equals(";") || ext.equals("*;")) ext = null;
				else exts = appProperties.getFileextension().split(";");

				StringBuffer sb = new StringBuffer();
				for (String e : exts) {
					sb.append(e.toLowerCase()+" ");
				}

				Utils.getInstance().handleVerboseLog(appProperties, 'i', Constants.APP_NAME+" | "+Constants.APP_VERSION);
				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Root Folder: "+folder.getAbsolutePath());
				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Extensions to upload: "+sb.toString()+"\n\n");
				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Inspecting files...\n\n");

				
				Collection<File> fileCollection = FileUtils.listFiles(folder, exts, true);
				
				totalFiles = fileCollection.size();
				
				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Files inspected: "+totalFiles+"\n\n");
				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Calculating total size of files...\n\n");
				for (File file : fileCollection) {
					totalSize=totalSize+file.length();
				}
				
				
				File[] filesArray = fileCollection.toArray (new File[fileCollection.size ()]);

				Comparator<File> comparator = null;

				if(appProperties.getOrderBy().equals("orderbydesc")) comparator = LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
				else comparator = LastModifiedFileComparator.LASTMODIFIED_REVERSE;

				boolean md5Name = false;
				if(appProperties.getMd5name().equals("yes")) md5Name = true;


				Arrays.sort(filesArray, comparator);
				String aggregator = appProperties.getFileaggregator().trim();
				String[] aggregators = null;
				if(null == aggregator || aggregator.equals("") || aggregator.equals("*") || aggregator.equals(";") || aggregator.equals("*;")) aggregator = null;
				else aggregators = aggregator.split(";");

				Map<String, String> repetidos = new HashMap<String,String>();
				Map<String, List<FileEntity>> jsons = new HashMap<String, List<FileEntity>>();
				for (int i = 0; i < filesArray.length; i++) {
					File file = filesArray[i];

					if(!file.isDirectory() && file.exists()){
						String fileName  = file.getName().substring(0,file.getName().length() - file.getName().substring(file.getName().lastIndexOf(".") + 1).length() - 1);
						String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
						String md5 = null;

						try{
							fis = new FileInputStream(file);
							System.out.println("Calculating md5... ("+file.getName()+")");
							md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
							System.out.println("Finished md5 calculation... ("+file.getName()+": "+md5+")\n\n");
						}catch(IOException ioe){
							Utils.getInstance().handleVerboseLog(appProperties, 'e', ioe.getMessage());
							continue;
						}finally {
							if(fis != null) fis.close();
						}


						for (String ag : aggregators) {
							if(extension.equals(ag)) {
								if(repetidos.containsKey(fileName)) {
									md5 = repetidos.get(fileName);
								}else {
									repetidos.put(fileName, md5);
								}
							}
						}

						List<FileEntity> listaJson = null;
						if(jsons.containsKey(md5)) {
							listaJson =  jsons.get(md5);
						}else {
							listaJson = new ArrayList<FileEntity>();
						}
						FileEntity json = new FileEntity(file, extension);
						listaJson.add(json);
						jsons.put(md5, listaJson);

						checkOverwriteUpload(appProperties, file, fileName, md5, false);
						Utils.getInstance().handleVerboseLog(appProperties, 'i', "\n\nUploaded/Skipped/Total files: "+uploadedFiles+"/"+skippedFiles+"/"+totalFiles+" | Uploaded/Total size: "+Utils.getInstance().formatBytes(uploadedSize)+"/"+Utils.getInstance().formatBytes(totalSize)+" ("+Utils.getInstance().formatTransferedProgress(totalSize, uploadedSize)+" - "+Utils.getInstance().formatInterval(new Date().getTime() - initTime)+") \n\n");
					}else Utils.getInstance().handleVerboseLog(appProperties, 'e', file.getName()+": is a directory, or file does not exist. Skipped!");
				
				}

				Iterator iter = jsons.entrySet().iterator();

				while (iter.hasNext()) {
					Map.Entry mEntry = (Map.Entry) iter.next();
					List<FileEntity> listaJson = null;
					listaJson =  jsons.get(mEntry.getKey());


					Gson gson = new Gson();
					String jsonString = gson.toJson(listaJson);

					try{
						writer = new FileWriter(jsonFileName);
						writer.write(jsonString);

						jsonFile = new File(jsonFileName);

						//S3Component.getInstance().upload(appProperties, jsonFile, jsons.get(mEntry.getKey()).get(0).getFileName(),mEntry.getKey().toString());

					} catch (IOException e) {
						Utils.getInstance().handleVerboseLog(appProperties, 'e', e.getMessage());
					}finally {
						if(writer != null){
							try {
								writer.close();
							} catch (IOException ioe) {
								Utils.getInstance().handleVerboseLog(appProperties, 'e', ioe.getMessage());
							}
						}
						if(jsonFile != null && jsonFile.exists() && jsonFile.isFile()) {
							checkOverwriteUpload(appProperties, jsonFile, jsons.get(mEntry.getKey()).get(0).getFileName(), mEntry.getKey().toString(), true);
							jsonFile.delete();
						}
					}

				}

			}
		} catch (Exception e) {
			Utils.getInstance().handleVerboseLog(appProperties, 'e', e.getMessage());
		}


	}

	private static void checkOverwriteUpload(AppProperties appProperties, File file, String fileName, String md5, boolean isMetadataUpload) {

		try {

			if(!isMetadataUpload) {
				uploadedSize= uploadedSize + file.length();
			}
			
			if(appProperties.getOverwrite().equalsIgnoreCase("yes")) {
				S3Component.getInstance().upload(appProperties, file, fileName,md5);

			}else {

				if(appProperties.getMd5name().equalsIgnoreCase("yes")) {
					String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
					fileName = md5+"."+ext;
				}

				if(!S3Component.getInstance().isValidFile(appProperties, fileName)) {
					S3Component.getInstance().upload(appProperties, file, fileName, md5);
				}else {
					skippedFiles++;
					Utils.getInstance().handleVerboseLog(appProperties, 'e', file.getName()+": already uploaded. Skipped!\n");
					
				}
			}

		}catch(Exception e) {
			Utils.getInstance().handleVerboseLog(appProperties, 'e', e.getMessage());
		}
	}

}
