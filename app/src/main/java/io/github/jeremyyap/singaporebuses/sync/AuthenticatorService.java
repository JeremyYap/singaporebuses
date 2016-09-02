package io.github.jeremyyap.singaporebuses.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jeremyy on 9/2/2016.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}