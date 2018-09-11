package org.ohdsi.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.ZipInputStream;

public class ZipInputStreamWrapper extends ZipInputStream {

    public ZipInputStreamWrapper(InputStream in) {
        super(in);
    }

    public String readCurrentFileToString() throws IOException {
        //String outpath = outputDirectory + "/" + outputName;
        Reader reader = new BufferedReader(new InputStreamReader(this));
        StringBuilder sb = new StringBuilder();
        int c = 0;
        try {
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw e;
        }
        return sb.toString();
    }

}
