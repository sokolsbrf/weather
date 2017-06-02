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
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherService extends Service implements LocationListener {
    private static final String TAG = "WeatherService";

    private static float LOCATION_ACCURACY_NEEDED = 100; // 100 метров точность
    private static int TIME_DELTA = 60000; // обновляем данные каждый час

    private Location mLocation;
    private Location mLastKnownLocation;
    private Location mCurrentBestLocation;

    public WeatherService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            Log.e(TAG,"no permissions");
            stopSelf();
            return START_NOT_STICKY;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        // нужно оптимизировать
        mLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLastKnownLocation!=null){
                requestWeather(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                Log.e(TAG,"Current location (gps): " + mLastKnownLocation.getLongitude()+" "+ mLastKnownLocation.getLatitude());
        } else {
            mLastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(mLastKnownLocation != null){
                    requestWeather(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                    Log.e(TAG,"Current location (network): " + mLastKnownLocation.getLongitude()+" "+ mLastKnownLocation.getLatitude());
            } else {
                Log.e(TAG,"No last known location retrieved, trying to update data as soon as possible");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, LOCATION_ACCURACY_NEEDED,this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,LOCATION_ACCURACY_NEEDED,this);
            }
        }

        //просим обновлять данные геолокации один раз в час
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,TIME_DELTA,LOCATION_ACCURACY_NEEDED,this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,TIME_DELTA,LOCATION_ACCURACY_NEEDED,this);

        return START_STICKY;
    }



    /*Набор методов интерфейса LocationListener*/

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "Location changed");
        if(mCurrentBestLocation == null){
            mCurrentBestLocation = mLastKnownLocation;
        }
        if(isBetterLocation(location,mCurrentBestLocation)){
            mLocation = location;
            Log.e(TAG,"Current location: " + mLocation.getLongitude() + " " + mLocation.getLatitude());
            requestWeather(mLocation.getLatitude(),mLocation.getLongitude());
            mCurrentBestLocation = location;
        } else {
            mLocation = mCurrentBestLocation;
            Log.e(TAG,"Current location: " + mLocation.getLongitude() + " " + mLocation.getLatitude());
            requestWeather(mLocation.getLatitude(),mLocation.getLongitude());
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

    private WeatherClient.WeatherEventListener weatherListener = new WeatherClient.WeatherEventListener() {
        @Override
        public void onWeatherRetrieved(CurrentWeather weather) {
            Log.e(TAG,"weather retrieved "+ weather.weather.temperature.getTemp() + " " + weather.weather.location.getCity());
        }

        @Override
        public void onWeatherError(WeatherLibException wle) {
            Log.e(TAG,"weather error "+ wle.getMessage());

        }

        @Override
        public void onConnectionError(Throwable t) {
            Log.e(TAG,"connection error ");
            t.printStackTrace();
        }
    };

    /*метод запроса погоды у сервиса*/

    private void requestWeather(double latitude, double longitude) {
        Log.e(TAG,"Weather requested");
        WeatherClient.ClientBuilder builder = new WeatherClient.ClientBuilder();
        WeatherConfig config = new WeatherConfig();
        config.ApiKey = BuildConfig.OWM_API_KEY;
        //config.lang = "ru";
        try {
            WeatherClient client = builder
                    .attach(this)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.StandardHttpClient.class)
                    .config(config)
                    .build();
            client.getCurrentCondition(new WeatherRequest(longitude,latitude), weatherListener);
        } catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }
    }



    /** метод проверки полученных данных по геолокации, если true, то новые данные лучше, ((честно найден в документации))
     * @param location  новые данные геолокации
     * @param currentBestLocation  предыдущие известные данные геолокации
     *
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // новые данные лучше чем ничего
            return true;
        }

        // Проверяем какие данные более новые
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DELTA;
        boolean isSignificantlyOlder = timeDelta < -TIME_DELTA;
        boolean isNewer = timeDelta > 0;

        // Если данные обновлены более чем час спустя, то новые данные более подходящие
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Проверяем точность данных и сравниваем
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > LOCATION_ACCURACY_NEEDED;

        // Проверяем идентичность провайдеров данных
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Комбинированная проверка данных, по точности, времени и провайдеру
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Метод проверки провайдеров на идентичность */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}


