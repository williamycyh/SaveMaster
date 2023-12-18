package com.savemaster.download.helper;

import com.savemaster.savefromfb.streams.io.SharpStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.channels.ClosedByInterruptException;

import com.savemaster.download.helper.DownloadMission.Block;
import com.savemaster.download.helper.DownloadMission.HttpError;

import static com.savemaster.download.helper.DownloadMission.ERROR_HTTP_FORBIDDEN;

/**
 * Runnable to download blocks of a file until the file is completely downloaded,
 * an error occurs or the process is stopped.
 */
public class DownloadRunnable extends Thread {
	
	private final DownloadMission mMission;
	private final int mId;
	
	private HttpURLConnection mConn;
	
	DownloadRunnable(DownloadMission mission, int id) {
		if (mission == null) throw new NullPointerException("mission is null");
		mMission = mission;
		mId = id;
	}
	
	private void releaseBlock(Block block, long remain) {
		// set the block offset to -1 if it is completed
		mMission.releaseBlock(block.position, remain < 0 ? -1 : block.done);
	}
	
	@Override
	public void run() {
		boolean retry = false;
		Block block = null;
		int retryCount = 0;
		SharpStream f;
		
		try {
			f = mMission.storage.getStream();
		}
		catch (IOException e) {
			mMission.notifyError(e);// this never should happen
			return;
		}
		
		while (mMission.running && mMission.errCode == DownloadMission.ERROR_NOTHING) {
			if (!retry) {
				block = mMission.acquireBlock();
			}
			
			if (block == null) {
				break;
			}
			
			long start = (long) block.position * DownloadMission.BLOCK_SIZE;
			long end = start + DownloadMission.BLOCK_SIZE - 1;
			
			start += block.done;
			
			if (end >= mMission.length) {
				end = mMission.length - 1;
			}
			
			try {
				mConn = mMission.openConnection(false, start, end);
				mMission.establishConnection(mId, mConn);
				
				// check if the download can be resumed
				if (mConn.getResponseCode() == 416) {
					if (block.done > 0) {
						// try again from the start (of the block)
						mMission.notifyProgress(-block.done);
						block.done = 0;
						retry = true;
						mConn.disconnect();
						continue;
					}
					
					throw new DownloadMission.HttpError(416);
				}
				
				retry = false;
				
				// The server may be ignoring the range request
				if (mConn.getResponseCode() != 206) {
					mMission.notifyError(new DownloadMission.HttpError(mConn.getResponseCode()));
					break;
				}
				
				f.seek(mMission.offsets[mMission.current] + start);
				
				try (InputStream is = mConn.getInputStream()) {
					byte[] buf = new byte[DownloadMission.BUFFER_SIZE];
					int len;
					
					// use always start <= end
					// fixes a deadlock because in some videos, tube is sending one byte alone
					while (start <= end && mMission.running && (len = is.read(buf, 0, buf.length)) != -1) {
						f.write(buf, 0, len);
						start += len;
						block.done += len;
						mMission.notifyProgress(len);
					}
				}
			}
			catch (Exception e) {
				if (!mMission.running || e instanceof ClosedByInterruptException) break;
				
				if (e instanceof HttpError && ((HttpError) e).statusCode == ERROR_HTTP_FORBIDDEN) {
					// for youtube streams. The url has expired, recover
					f.close();
					
					if (mId == 1) {
						// only the first thread will execute the recovery procedure
						mMission.doRecover(ERROR_HTTP_FORBIDDEN);
					}
					return;
				}
				
				if (retryCount++ >= mMission.maxRetry) {
					mMission.notifyError(e);
					break;
				}
				
				retry = true;
			}
			finally {
				if (!retry) releaseBlock(block, end - start);
			}
		}
		f.close();
		
		if (mMission.errCode == DownloadMission.ERROR_NOTHING && mMission.running) {
			mMission.notifyFinished();
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		
		try {
			if (mConn != null) mConn.disconnect();
		}
		catch (Exception e) {
			// nothing to do
		}
	}
	
}
