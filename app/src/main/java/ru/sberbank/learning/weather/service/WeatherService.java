package ru.sberbank.learning.weather.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.PermissionChecker;

public class WeatherService extends Service {
    public WeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PermissionChecker.PERMISSION_GRANTED){
            stopSelf();
            return START_NOT_STICKY;
        }


        return START_STICKY;
    }
}
