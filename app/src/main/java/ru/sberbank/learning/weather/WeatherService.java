package ru.sberbank.learning.weather;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.PermissionChecker;
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
        if (!isPermissionsGranted(this)) {
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

    public static boolean isPermissionsGranted(Context context) {
        return PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PermissionChecker.PERMISSION_GRANTED;

    }
}
