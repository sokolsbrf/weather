package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WeatherService extends Service {

    //минимально необходимое время location - 30 минут, location с большим временем не подходят
    private static final long MIN_LAST_TIME = 1000 * 60 * 30;
    //минимально необходимая точность для location - 200 метров, location с меньшей точностью не подходят
    private static final long MIN_LAST_ACCURACY = 200;


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
                    .config(config)
                    .build();
            client.getCurrentCondition(new WeatherRequest(longitude, latitude), weatherListener);
        } catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("MissingPermission")
    private Location getLastBestLocation(List<String> providers, LocationManager lm) {
        List<Location> result = new ArrayList<>();

        //проверяем в цикле старые значения
        for (int i = 0; i < providers.size(); i++) {
            Location location = lm.getLastKnownLocation(providers.get(i));
            if (location != null) {
                //проверяем время и точность на минимально необходимые
                long time = getLocationTime(location);
                float accuracy = location.getAccuracy();
                if (time < MIN_LAST_TIME && accuracy < MIN_LAST_ACCURACY) {
                    result.add(location);
                }
            }
        }

        if (result.size() > 0) {

            //сортируем список с учетом трех основных критериев - время, точность, провайдер
            //время и точность имеют основное значение, провайдер - второстепенное
            Collections.sort(result, new Comparator<Location>() {
                @Override
                public int compare(android.location.Location location1, android.location.Location location2) {
                    return 31 * (int) getLocationTime(location1) / 60 * 1000
                            + (int) location1.getAccuracy()
                            + getProviderValue(location1) -
                            (31 * (int) getLocationTime(location2) / 60 * 1000
                                    + (int) location2.getAccuracy()
                                    + getProviderValue(location2));
                }
            });

            //лог для самопроверки
            for (int i = 0; i < result.size(); i++) {
                android.location.Location location = result.get(i);
                Log.e("sorted array", "time - " + (System.currentTimeMillis() - location.getTime()) / 60000 + " accuracy - " + location.getAccuracy() + " provider " + getProviderValue(location));
            }
            return result.get(0);
        }
        return null;
    }

    private int getProviderValue(Location location) {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) return 1;
        else return 0;
    }

    private long getLocationTime(Location location) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return SystemClock.elapsedRealtime() - location.getElapsedRealtimeNanos() / 1000000;
        } else {
            return System.currentTimeMillis() - location.getTime();
        }
    }
}
