package org.ohdsi.utilities;

import java.io.IOException;
import java.util.zip.ZipEntry;

public class ZipOutputStreamEntry {
    private String name;
    private String content;
    
    public ZipOutputStreamEntry(ZipEntry ze, ZipInputStreamWrapper zisw) throws IOException {
        this.name = ze.getName();
        this.content = zisw.readCurrentFileToString();
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String value) {
        this.name = value;
    }
    
    public String getContent() {
        return this.content;
    }
    
    public void setContent(String value) {
        this.content = value;
    }
}
