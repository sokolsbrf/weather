package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.RemoteViews;

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

public class WeatherService extends Service implements LocationListener {

    //минимально необходимое время location - 30 минут, location с большим временем не подходят
    private static final long MIN_LAST_TIME = 1000 * 60 * 30;
    //минимально необходимая точность для location - 200 метров, location с меньшей точностью не подходят
    private static final long MIN_LAST_ACCURACY = 200;

    private static final float LOCATION_ACCURACY_NEEDED = 100f; // 100 метров точность


    private WeatherClient.WeatherEventListener weatherListener = new WeatherClient.WeatherEventListener() {
        @Override
        public void onWeatherRetrieved(CurrentWeather weather) {
            AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());

            int[] ids = manager.getAppWidgetIds(new ComponentName(getApplicationContext(), WeatherWidget.class));

            for (int id : ids) {

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.weather_widget);

                if (weather != null) {
                    views.setTextViewText(R.id.tvDefinition,
                            getString(R.string.temperature_format, Math.round((weather.weather.temperature.getTemp()))));
                    views.setTextViewText(R.id.tvCity, weather.weather.location.getCity());
                    views.setTextViewText(R.id.tvCondition, weather.weather.currentCondition.getCondition());

                    if (weather.weather.currentCondition.getIcon().equals("01d")) {
                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z01d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("01n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z01n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("02d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z02d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("02n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z02n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("03d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z03d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("03n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z03n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("04d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z04d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("04n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z04n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("09d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z09d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("09n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z09n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("10d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z10d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("10n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z10n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("11d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z11d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("11n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z11n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("13d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z13d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("13n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z13n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("50d")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z50d);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("50n")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.z50n);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("r")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.zr);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("sn50")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.zsn50);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("t50")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.zt50);
                    }
                    if (weather.weather.currentCondition.getIcon().equals("w50")) {

                        views.setImageViewResource(R.id.ivWeatherIcon, R.drawable.zw50);
                    }
                }

                manager.updateAppWidget(id, views);
            }
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

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastLocation = getLastBestLocation(lm.getAllProviders(), lm);

        if (lastLocation == null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,
                    LOCATION_ACCURACY_NEEDED, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,
                    LOCATION_ACCURACY_NEEDED, this);
        } else {
            requestWeather(lastLocation.getLatitude(), lastLocation.getLongitude());
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
                    .httpClient(com.survivingwithandroid.weather.lib.WeatherClient.class)
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

    @Override
    public void onLocationChanged(Location location) {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.removeUpdates(this);
        requestWeather(location.getLatitude(), location.getLongitude());
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
