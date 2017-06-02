package ru.sberbank.learning.weather;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Created by Георгий on 02.06.2017.
 * gio.com
 */

public class LocationHelper {

    public static boolean isLocationEnabled(Context context){

        boolean result = false;

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            result = true;

        }
        return result;
    }

    public static Location getCurrentLocation(Context context){

        Location result = null;

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            result = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (result == null){
                result = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (result == null){
                    result = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
            }

        } catch (SecurityException e){
            e.printStackTrace();
        }
        return result;
    }
}
