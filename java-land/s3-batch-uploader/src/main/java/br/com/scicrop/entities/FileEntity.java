package br.com.scicrop.entities;

import java.io.Serializable;
import java.util.Date;

public class FileEntity implements Serializable{
	
	private static final long serialVersionUID = 5220249832560290162L;
	
	private long id;
	private String fileName;
	private String md5;
	private String fileExtension;
	private Date dtCreation;
	private Date dtCreationFile;
	private String sourcePath;
	private String sourceIp;
	

	public FileEntity(long id, String fileName, String md5, String fileExtension, Date dtCreation, Date dtCreationFile,
			String sourcePath, String sourceIp) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.md5 = md5;
		this.fileExtension = fileExtension;
		this.dtCreation = dtCreation;
		this.dtCreationFile = dtCreationFile;
		this.sourcePath = sourcePath;
		this.sourceIp = sourceIp;
	}


	public FileEntity() {}
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	public Date getDtCreation() {
		return dtCreation;
	}
	public void setDtCreation(Date dtCreation) {
		this.dtCreation = dtCreation;
	}
	public Date getDtCreationFile() {
		return dtCreationFile;
	}
	public void setDtCreationFile(Date dtCreationFile) {
		this.dtCreationFile = dtCreationFile;
	}
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public String getSourceIp() {
		return sourceIp;
	}
	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	
	
}
