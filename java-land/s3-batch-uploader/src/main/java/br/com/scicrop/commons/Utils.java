package br.com.scicrop.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.com.scicrop.entities.AppProperties;

public class Utils {

	private Utils(){}



	private static Log log = LogFactory.getLog(Utils.class);

	private static Utils INSTANCE = null;

	public static Utils getInstance(){
		if(INSTANCE == null) INSTANCE = new Utils();
		return INSTANCE;
	}

	public void handleVerboseLog(AppProperties appProperties, char type, String data){

		if(appProperties != null){

			if(appProperties.getLog().equalsIgnoreCase("yes")){
				log(data, type);
			}
			if(appProperties.getVerbose().equalsIgnoreCase("yes")){
				verbose(data, type);
			}
		}else verbose(data, type);
	}

	public void log(String data, char type){



		switch (type) {
		case 'i':
			log.info(data);
			break;

		case 'w':
			log.warn(data);
			break;

		case 'e':
			log.error(data);
			break;

		default:
			log.info(data);
			break;
		}


	}

	public void verbose(String data, char type){
		switch (type) {


		case 'e':
			System.err.println(data);
			break;

		default:
			System.out.println(data);
			break;
		}

	}
}