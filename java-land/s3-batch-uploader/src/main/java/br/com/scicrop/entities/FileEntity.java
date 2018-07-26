package br.com.scicrop.entities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class FileEntity implements Serializable{
	
	private static final long serialVersionUID = 5220249832560290162L;
	
	private String fileName;
	private String md5;
	private String fileExtension;
	private Date dtCreation;
	private Date dtCreationFile;
	private String sourcePath;
	private String sourceIp;
	
	public FileEntity(File file, String md5, String extension) {
		try {
		this.dtCreationFile = new Date(file.lastModified());
		this.dtCreation = new Date();
		this.fileName = file.getName();
		this.sourcePath = file.getAbsolutePath();
		this.md5 = md5;
		this.fileExtension = extension;
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("google.com", 80));
		this.sourceIp = socket.getLocalAddress().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FileEntity( String fileName, String md5, String fileExtension, Date dtCreation, Date dtCreationFile,
			String sourcePath, String sourceIp) {
		super();
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
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	
	
}
