
package com.savemaster.pklib;

import android.os.Handler;
import android.os.HandlerThread;

public class BackgroundThread extends HandlerThread {
    private static BackgroundThread sInstance;
    private static Handler sHandler;

    public BackgroundThread() {
        super("WorkerThread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new BackgroundThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static BackgroundThread get() {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }

    public static void post(final Runnable runnable) {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            sHandler.post(runnable);
        }
    }
    
    public static void postDelayed(final Runnable runnable, long nDelay) {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            sHandler.postDelayed(runnable, nDelay);
        }
    }
    
    public static void removeTask(final Runnable runnable) {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            sHandler.removeCallbacks(runnable);
        }
    }
	
	public static void changePriority(boolean background) {
    	synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            if (background) {
            	sHandler.postAtFrontOfQueue(new Runnable() {
					
					@Override
					public void run() {
						android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					}
				});	
            } else {
            	sHandler.postAtFrontOfQueue(new Runnable() {
					
					@Override
					public void run() {
						android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
					}
				});	
            }
        }
    }

}
