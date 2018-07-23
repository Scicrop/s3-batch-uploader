package br.com.scicrop.commons;

import br.com.scicrop.entities.AppProperties;

public class TopLevelException extends Exception {

	private static final long serialVersionUID = -5433171273983353912L;
	
	private StackTraceElement[] stackTraceElements;
	
	private String message;
	
	public TopLevelException(AppProperties appProperties, String message){
		super(message);
		this.message = message;
		Utils.getInstance().handleVerboseLog(appProperties, 'e', message);
	}
	
	
	public TopLevelException(Exception e){
		super(e);
		Utils.getInstance().handleVerboseLog(null, 'e', getStackTraceElements(e));
	}
	
	public TopLevelException(AppProperties appProperties, Exception e){
		super(e);
		Utils.getInstance().handleVerboseLog(appProperties, 'e', getStackTraceElements(e));
	}
	
	
	private String getStackTraceElements(Exception e) {
		this.stackTraceElements = e.getStackTrace();
		StringBuffer sb = new StringBuffer();
		
		if(stackTraceElements == null){
			sb.append(message+" ");
		}else{
			sb.append(message+" ");
			for(int i = 0; i < stackTraceElements.length; i++){
				sb.append(stackTraceElements[i].getFileName()+" ("+stackTraceElements[i].getLineNumber()+")\n");
			}
		}
		
		
		return sb.toString();
	}

	public TopLevelException(){
		super();
		stackTraceElements = getStackTrace();
		Utils.getInstance().handleVerboseLog(null, 'e', getStackTraceElements());
	}
	

	


	public String getStackTraceElements(){
		StringBuffer sb = new StringBuffer();
		
		if(stackTraceElements == null){
			sb.append(message);
		}else{
			sb.append(message);
			for(int i = 0; i < stackTraceElements.length; i++){
				sb.append(stackTraceElements[i].getFileName()+"("+stackTraceElements[i].getLineNumber()+")\n");
			}
		}
		
		
		return sb.toString();
	}
	

}
