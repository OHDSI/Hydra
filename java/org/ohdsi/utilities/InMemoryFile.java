package org.ohdsi.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InMemoryFile {
    private String name;
    private byte[] content;
    private boolean deleted = false;
    private boolean isDirectory;
    
    public InMemoryFile(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
    	this.name = zipEntry.getName();
    	isDirectory = zipEntry.isDirectory();
    	if (!isDirectory) {
    		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    		int nRead;
    		byte[] data = new byte[16384];
    		while ((nRead = zipInputStream.read(data, 0, data.length)) != -1) {
    			byteArrayOutputStream.write(data, 0, nRead);
    		}
    		byteArrayOutputStream.flush();
    		content = byteArrayOutputStream.toByteArray();
    	}
    }
    
    public InMemoryFile(String name, byte[] content) {
    	this.name = name;
    	this.content = content;
    }
    
    public InMemoryFile(String name, String content) {
    	this.name = name;
    	this.content = content.getBytes();
	}

	public String getName() {
        return this.name;
    }
    
    public void setName(String value) {
        this.name = value;
    }
    
    public byte[] getContent() {
        return this.content;
    }
    
    public String getContentAsString() {
    	if (content == null)
    		return "";
    	else
    		return(new String(content));
    }
    
    public void setContent(byte[] value) {
        this.content = value;
    }
    
    public void setContent(String value) {
        this.content = value.getBytes();
    }

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDirectory() {
		return isDirectory;
	}
}
