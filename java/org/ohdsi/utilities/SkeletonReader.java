package org.ohdsi.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SkeletonReader implements Iterable<InMemoryFile> {

	private InputStream inputStream;

	public SkeletonReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public Iterator<InMemoryFile> iterator() {
		return new InMemoryFileIterator(inputStream);
	}

	private class InMemoryFileIterator implements Iterator<InMemoryFile> {

		private ZipInputStream	zipInputStream;
		private ZipEntry		zipEntry;

		public InMemoryFileIterator(InputStream inputStream) {
			zipInputStream = new ZipInputStream(inputStream);
			try {
				zipEntry = zipInputStream.getNextEntry();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public boolean hasNext() {
			return (zipEntry != null);
		}

		public InMemoryFile next() {
			try {
				InMemoryFile next = new InMemoryFile(zipEntry, zipInputStream);
				zipEntry = zipInputStream.getNextEntry();
				return next;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public void close() {
		try {
			inputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
