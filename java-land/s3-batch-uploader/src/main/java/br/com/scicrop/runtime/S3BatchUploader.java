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

import br.com.scicrop.commons.Constants;
import br.com.scicrop.commons.ManageProperties;
import br.com.scicrop.commons.TopLevelException;
import br.com.scicrop.commons.Utils;
import br.com.scicrop.components.S3Component;
import br.com.scicrop.entities.AppProperties;
import br.com.scicrop.entities.FileEntity;

public class S3BatchUploader {


	public static void main(String[] args) {

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
		rootLogger.addAppender(new ConsoleAppender(layout));
		try {

			RollingFileAppender fileAppender = new RollingFileAppender(layout, Constants.LOG_FILE);
			rootLogger.addAppender(fileAppender);
		} catch (IOException e) {
			System.err.println("Failed to find/access "+Constants.LOG_FILE+" !");
		}


		String propertiesFilePath = Constants.DEFAULT_PROPERTIES_FILE_PATH;

		if(args != null && args.length > 0){
			propertiesFilePath = args[0];
		}

		AppProperties appProperties = null;
		try {
			appProperties = ManageProperties.getInstance().getAppProperties(propertiesFilePath);
		} catch (TopLevelException e) {
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

				Collection<File> fileCollection = FileUtils.listFiles(folder, exts, true);

				StringBuffer sb = new StringBuffer();
				for (String e : exts) {
					sb.append(e+" ");
				}

				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Root Folder: "+folder.getAbsolutePath());
				Utils.getInstance().handleVerboseLog(appProperties, 'i', "Extensions to upload: "+sb.toString());

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

					if(!file.isDirectory()){
						String fileName  = file.getName().substring(0,file.getName().length() - file.getName().substring(file.getName().lastIndexOf(".") + 1).length() - 1);
						String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
						String md5 = "";
						FileInputStream fis = new FileInputStream(file);
						md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
						fis.close();
						
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
					
						if(appProperties.getOverwrite().equalsIgnoreCase("yes")) {
							S3Component.getInstance().upload(appProperties, file, fileName,md5,md5Name);
							
						}else {
							if(!S3Component.getInstance().isValidFile(appProperties, file.getName())) {
								S3Component.getInstance().upload(appProperties, file, fileName,md5,md5Name);
							}
						}
						Utils.getInstance().handleVerboseLog(appProperties, 'i', "File: "+file.getName()+" - " + new Date(file.lastModified()));
					}else Utils.getInstance().handleVerboseLog(appProperties, 'e', file.getName()+": is a directory. Skipped!");
				}
				
				Iterator iter = jsons.entrySet().iterator();

				while (iter.hasNext()) {
					Map.Entry mEntry = (Map.Entry) iter.next();
					List<FileEntity> listaJson = null;
					listaJson =  jsons.get(mEntry.getKey());
					

					Gson gson = new Gson();
					String jsonString = gson.toJson(listaJson);
					FileWriter writer = new FileWriter("/tmp/s3-uploader.json");
					writer.write(jsonString);
					writer.close();
					S3Component.getInstance().upload(appProperties, new File("/tmp/s3-uploader.json"), jsons.get(mEntry.getKey()).get(0).getFileName(),mEntry.getKey().toString(),md5Name);
				}
				
				

			}
		} catch (Exception e) {
			Utils.getInstance().handleVerboseLog(appProperties, 'e', e.getMessage());
		}



	}
}
