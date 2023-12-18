package com.savemaster.savefromfb.util;

import android.content.Context;

import savemaster.save.master.pipd.NewPipe;
import savemaster.save.master.pipd.ServiceList;
import savemaster.save.master.pipd.StreamingService;
import savemaster.save.master.pipd.exceptions.ExtractionException;

import java.util.concurrent.TimeUnit;

import androidx.preference.PreferenceManager;
import com.savemaster.savefromfb.R;

public class ServiceHelper {
    
    private static final StreamingService DEFAULT_FALLBACK_SERVICE = ServiceList.YouTube;

    public static int getSelectedServiceId(Context context) {

        final String serviceName = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.current_service_key), context.getString(R.string.default_service_value));

        int serviceId;
        try {
            serviceId = NewPipe.getService(serviceName).getServiceId();
        } catch (ExtractionException e) {
            serviceId = DEFAULT_FALLBACK_SERVICE.getServiceId();
        }

        return serviceId;
    }

    public static long getCacheExpirationMillis() {
        
        return TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
    }
}
