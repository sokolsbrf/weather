package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherService extends Service implements LocationListener{

    private Location myLocation;


    private WeatherClient.WeatherEventListener weatherListener = new WeatherClient.WeatherEventListener() {
        @Override
        public void onWeatherRetrieved(CurrentWeather weather) {

        }

        @Override
        public void onWeatherError(WeatherLibException wle) {

        }

        @Override
        public void onConnectionError(Throwable t) {

        }
    };

    public WeatherService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PermissionChecker.PERMISSION_GRANTED) {
            stopSelf();
            return START_NOT_STICKY;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null || locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            // если хоть один не нулл - выбираем кого из них брать
            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null && locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                // если оба не нулл, выбираем лучшего по времени
                if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime() >= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime()) {
                    myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else {
                    myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                // если кто-то нулл, то выбираем другого
            } else if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            // сделай из итогого варианта запрос
            Log.e("myWheather", " Latit:" + myLocation.getLatitude() + ", Longit:" + myLocation.getLongitude());
            requestWeather(myLocation.getLatitude(), myLocation.getLongitude());

        }
        // если же оба - нулл, запроси
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 100, this);
            Log.e("myWheather", "requesting some info");
        }
        // запрашивай раз в 100 минут (вроде minTime - минуты)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 100, this);





        return START_STICKY;
    }



    private void requestWeather(double latitude, double longitude) {
        WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
        WeatherConfig config = new WeatherConfig();
        config.ApiKey = BuildConfig.OWM_API_KEY;
        config.lang = "ru";
        try {
            WeatherClient client = builder
                    .attach(this)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.WeatherClient.class)
                    // без httpClient выдавало ошибку при попытке поднять ссылку на нулевой объект класса, что внутри этого метода
                    .config(config)
                    .build();
            client.getCurrentCondition(new WeatherRequest(longitude, latitude), weatherListener);
        } catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        Location tempLocation = myLocation;

        myLocation = whoIsBetter(tempLocation, location);

    }

    private Location whoIsBetter(Location locOld, Location locNew){
        if (locOld.getAccuracy() < locNew.getAccuracy()){ // у кого число меньше - тот круче. вроде
            return locOld;
        } else {
            return  locNew;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
