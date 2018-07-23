package br.com.scicrop.runtime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import br.com.scicrop.commons.Constants;
import br.com.scicrop.commons.ManageProperties;
import br.com.scicrop.commons.TopLevelException;
import br.com.scicrop.commons.Utils;
import br.com.scicrop.components.S3Component;
import br.com.scicrop.entities.AppProperties;

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
				
				

				
				Arrays.sort(filesArray, comparator);
				for (int i = 0; i < filesArray.length; i++) {
					File file = filesArray[i];

					if(!file.isDirectory()){

						if(appProperties.getOverwrite().equalsIgnoreCase("yes"))
							S3Component.getInstance().upload(appProperties, file, file.getName());
						else
							if(!S3Component.getInstance().isValidFile(appProperties, file.getName())) S3Component.getInstance().upload(appProperties, file, file.getName());

						Utils.getInstance().handleVerboseLog(appProperties, 'i', "File: "+file.getName()+" - " + new Date(file.lastModified()));
					}else Utils.getInstance().handleVerboseLog(appProperties, 'e', file.getName()+": is a directory. Skipped!");
				}

			}
		} catch (Exception e) {
			Utils.getInstance().handleVerboseLog(appProperties, 'e', e.getMessage());
		}



	}
}
