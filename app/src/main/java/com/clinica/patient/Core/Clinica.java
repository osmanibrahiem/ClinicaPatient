package com.clinica.patient.Core;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.FirebaseDatabase;

public class Clinica extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
