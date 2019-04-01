package org.ohdsi.utilities;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;

public class InMemoryFile {
    private String name;
    private byte[] content;
    private boolean deleted = false;
    private boolean isDirectory;
    
    public InMemoryFile(ZipEntry zipEntry, ZipInputStream zipInputStream) throws IOException {
    	this.name = zipEntry.getName();
    	isDirectory = zipEntry.isDirectory();
    	if (!isDirectory) {
    		content = IOUtils.toByteArray(zipInputStream);
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
