package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.PermissionChecker;
import android.widget.RemoteViews;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class WeatherService extends Service {

    private WeatherClient.WeatherEventListener weatherListener = new WeatherClient.WeatherEventListener() {
        @Override
        public void onWeatherRetrieved(CurrentWeather weather) {

            AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());

            int[] ids = manager.getAppWidgetIds(new ComponentName(getApplicationContext(), WeatherWidget.class));

            for (int id : ids) {

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.weather_widget);

                if (weather != null) {
                    views.setTextViewText(R.id.tvDefinition,
                            Math.round((weather.weather.temperature.getTemp() - 273.15)) + "°C");
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
        Location location = null;
        if (LocationHelper.isLocationEnabled(this)) {
            location = LocationHelper.getCurrentLocation(this);
        }

        if (location != null) {
            requestWeather(location.getLatitude(), location.getLongitude());
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
}