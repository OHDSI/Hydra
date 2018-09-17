package org.ohdsi.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackageZipWriter {

	private ZipOutputStream zipOutputStream;

	public PackageZipWriter(OutputStream outputStream) {
		zipOutputStream = new ZipOutputStream(outputStream);
	}

	public void write(InMemoryFile file) {
		if (!file.isDeleted())
			try {
				zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
				if (!file.isDirectory())
					zipOutputStream.write(file.getContent());
				zipOutputStream.closeEntry();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	public void close() {
		try {
			zipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
