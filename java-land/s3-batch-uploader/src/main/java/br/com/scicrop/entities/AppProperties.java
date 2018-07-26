package br.com.scicrop.entities;

public class AppProperties {
	
	private String aws_access_key_id = null;
	private String aws_secret_access_key = null;
	private String bucketnName = null;
	
	private String folder = null;
	private String orderBy = null;
	private String overwrite = null;
	private String verbose = null;
	private String log = null;
	private String md5name = null;
	private String fileextension = null;
	private String fileaggregator = null;
	
	public static final String[] KEY_NAMES = new String[]{"aws_access_key_id", "aws_secret_access_key", "bucketname", "folder", "orderby", "overwrite", "verbose", "log", "fileextension", "md5name", "fileaggregator"};
	
	public AppProperties(String aws_access_key_id, String aws_secret_access_key, String bucketnName, String folder, String orderBy, String overwrite, String verbose, String log, String fileextension, String md5name, String fileaggregator) {
		super();
		this.aws_access_key_id = aws_access_key_id;
		this.aws_secret_access_key = aws_secret_access_key;
		this.bucketnName = bucketnName;
		this.folder = folder;
		this.orderBy = orderBy;
		this.overwrite = overwrite;
		this.verbose = verbose;
		this.log = log;
		this.fileextension = fileextension;
		this.md5name = md5name;
		this.fileaggregator = fileaggregator;
	}
	public String getBucketnName() {
		return bucketnName;
	}
	public void setBucketnName(String bucketnName) {
		this.bucketnName = bucketnName;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public String getOverwrite() {
		return overwrite;
	}
	public void setOverwrite(String overwrite) {
		this.overwrite = overwrite;
	}
	public String getAws_access_key_id() {
		return aws_access_key_id;
	}
	public void setAws_access_key_id(String aws_access_key_id) {
		this.aws_access_key_id = aws_access_key_id;
	}
	public String getAws_secret_access_key() {
		return aws_secret_access_key;
	}
	public void setAws_secret_access_key(String aws_secret_access_key) {
		this.aws_secret_access_key = aws_secret_access_key;
	}
	public String getVerbose() {
		return verbose;
	}
	public void setVerbose(String verbose) {
		this.verbose = verbose;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	public String getFileextension() {
		return fileextension;
	}
	public void setFileextension(String fileextension) {
		this.fileextension = fileextension;
	}
	public String getMd5name() {
		return md5name;
	}
	public void setMd5name(String md5name ) {
		this.md5name = md5name;
	}
	public String getFileaggregator() {
		return fileaggregator;
	}
	public void setFileaggregator(String fileaggregator) {
		this.fileaggregator = fileaggregator;
	}
	
}
