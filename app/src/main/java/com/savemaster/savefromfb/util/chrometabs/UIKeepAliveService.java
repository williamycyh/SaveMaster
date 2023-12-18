package com.savemaster.savefromfb.util.chrometabs;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class UIKeepAliveService extends Service {
	
	private static final Binder sBinder = new Binder();
	
	@Override
	public IBinder onBind(Intent intent) {
		
		return sBinder;
	}
}