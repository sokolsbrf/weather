package ru.sberbank.learning.weather;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Ponomarev on 02.06.2017.
 */
class LocationHelper {

    private static final long LAST_FIX_2_HOURS_MILLIS = 2 * 60 * 60 * 1000;

    private Context context;
    private Comparator<Location> locationComparator = new Comparator<Location>() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public int compare(Location o1, Location o2) {
            int relevance = Long.compare(o2.getElapsedRealtimeNanos(), o1.getElapsedRealtimeNanos());
            int accuracy = Float.compare(o1.getAccuracy(), o2.getAccuracy());

            return relevance == -1 ?
                    relevance :
                    accuracy == -1 ?
                            accuracy :
                            0;
        }
    };

    public LocationHelper(Context context) {
        this.context = context;
    }

    Location getBestLastKnownLocation() {
        Location location = null;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            List<Location> locationList = getAllAvailableLocations(lm);
            location = findBestLocation(locationList);
        }

        return location;
    }

    @SuppressWarnings("Since15")
    private Location findBestLocation(List<Location> list) {
        if (list.size() > 0) {
            list.sort(locationComparator);
            return checkLocationRelevance(list.get(0));
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Nullable
    private Location checkLocationRelevance(Location location) {
        return location != null &&
                SystemClock.elapsedRealtime() - location.getElapsedRealtimeNanos()/1000000 < LAST_FIX_2_HOURS_MILLIS ?
                location :
                null;
    }

    @SuppressWarnings("MissingPermission")
    private List<Location> getAllAvailableLocations(LocationManager lm) {
        List<Location> list = new ArrayList<Location>();

        for (String provider : lm.getAllProviders()) {
            Location location = lm.getLastKnownLocation(provider);
            if (location != null) {
                list.add(location);
            }
        }
        return list;
    }
}
