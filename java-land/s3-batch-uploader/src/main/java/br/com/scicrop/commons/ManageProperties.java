package br.com.scicrop.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.scicrop.agroapi.commons.exceptions.SciCropAgroApiException;

import br.com.scicrop.entities.AppProperties;

public class ManageProperties {
	
	private ManageProperties(){}
	
	private static ManageProperties INSTANCE = null;
	
	public static ManageProperties getInstance(){
		if(INSTANCE == null) INSTANCE = new ManageProperties();
		return INSTANCE;
	}
	
	public AppProperties getAppProperties(String propertiesFilePath) throws SciCropAgroApiException {
		
		AppProperties appProperties = null;
		
		
			appProperties = new AppProperties(
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[0]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[1]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[2]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[3]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[4]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[5]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[6]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[7]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[8]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[9]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[10]),
					getPropertyByName(propertiesFilePath, AppProperties.KEY_NAMES[11])
					);
		
		
		
		
		return appProperties;
		
	}
	
	public String getPropertyByName(String propertiesFilePath, String propertyName) throws SciCropAgroApiException{
		
		String propertyValue = null;
		Properties prop = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propertiesFilePath);
			
			if (inputStream != null) {
				prop.load(inputStream);
				propertyValue = prop.getProperty(propertyName);
				if(propertyValue == null || propertyValue.equals("")) throw new SciCropAgroApiException("Impossible to read property \""+propertyName+"\" file at: "+propertiesFilePath); 
			}
		} catch (IOException e) {
			throw new SciCropAgroApiException(e);
		} finally {
			if(inputStream != null) try {
				inputStream.close();
			} catch (Exception e) {
				throw new SciCropAgroApiException(e);
			}
		}
		
		
		
		return propertyValue;
	}

}
