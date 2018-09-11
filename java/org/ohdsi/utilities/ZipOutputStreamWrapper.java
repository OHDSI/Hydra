package org.ohdsi.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamWrapper {
    
    private static final int DEFAULT_BUFFER_SIZE = 8912;
    private ZipOutputStream zipOutputStream;
    private int fileCount = 0;
    private final HashMap fileList = new HashMap();
    private byte[] buffer;
    
    public ZipOutputStreamWrapper(ByteArrayOutputStream baos) {
        init(baos, DEFAULT_BUFFER_SIZE);
    }
    
    public ZipOutputStreamWrapper(ByteArrayOutputStream baos, int bufferSize) {
        init(baos, bufferSize);
    }
    
    private void init(ByteArrayOutputStream baos, int bufferSize) {
        this.zipOutputStream = new ZipOutputStream(baos);
        this.buffer = new byte[bufferSize];
    }
    
    public int addZipEntry(ZipEntry zipEntry) throws IOException {
        return this.addEntry(zipEntry.getName());
    }
    
    public int addZipEntry(ZipOutputStreamEntry zipEntry) throws IOException {
        return this.addEntry(zipEntry.getName());
    }
    
    private int addEntry(String name) throws IOException {
        this.zipOutputStream.putNextEntry(new ZipEntry(name));
        this.fileList.put(name, null);
        return ++fileCount;
    }
    
    public boolean fileExists(String name) {
        return this.fileList.containsKey(name);
    }
    
    public int getFileCount() {
        return fileCount;
    }
    
    public void write(String str) throws IOException {
        this.write(str.getBytes());
    }
    
    public void write(byte[] b) throws IOException {
        this.zipOutputStream.write(b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        this.zipOutputStream.write(b, off, len);
    }
    
    public void closeEntry() throws IOException {
        this.zipOutputStream.closeEntry();
    }
    
    public void close() throws IOException {
        this.zipOutputStream.close();
    }
    
    public void writeToOutputStream(ZipInputStream zipInputStream) throws IOException {
        int length;
        while ((length = zipInputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, length);
        }
    }
}
