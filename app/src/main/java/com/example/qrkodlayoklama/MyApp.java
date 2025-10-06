package com.example.qrkodlayoklama;

import android.app.Application;
import com.example.qrkodlayoklama.data.local.SessionManager;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
    }
}
