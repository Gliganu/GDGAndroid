package com.gliga.newscafe.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


/**
 * Created by gliga on 3/26/2015.
 */
public class NewsSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static NewsSyncAdapter  newsSyncAdapter = null;


    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (newsSyncAdapter == null) {
                newsSyncAdapter = new NewsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  newsSyncAdapter.getSyncAdapterBinder();
    }
}
