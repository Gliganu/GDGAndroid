package com.gliga.newscafe.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by gliga on 3/26/2015.
 */
public class NewsAuthenticatorService  extends Service {
    // Instance field that stores the authenticator object
    private NewsAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new NewsAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
