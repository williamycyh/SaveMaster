package com.savemaster.savefromfb.streams.io;

import java.io.Closeable;
import java.io.IOException;

public abstract class SharpStream implements Closeable {
	
	public abstract int read() throws IOException;
	
	public abstract int read(byte buffer[]) throws IOException;
	
	public abstract int read(byte buffer[], int offset, int count) throws IOException;
	
	public abstract long skip(long amount) throws IOException;
	
	public abstract long available();
	
	public abstract void rewind() throws IOException;
	
	public abstract boolean isClosed();
	
	@Override
	public abstract void close();
	
	public abstract boolean canRewind();
	
	public abstract boolean canRead();
	
	public abstract boolean canWrite();
	
	public boolean canSetLength() {
		return false;
	}
	
	public boolean canSeek() {
		return false;
	}
	
	public abstract void write(byte value) throws IOException;
	
	public abstract void write(byte[] buffer) throws IOException;
	
	public abstract void write(byte[] buffer, int offset, int count) throws IOException;
	
	public void flush() throws IOException {
		// STUB
	}
	
	public void setLength(long length) throws IOException {
		throw new IOException("Not implemented");
	}
	
	public void seek(long offset) throws IOException {
		throw new IOException("Not implemented");
	}
	
	public long length() throws IOException {
		throw new UnsupportedOperationException("Unsupported operation");
	}
}
