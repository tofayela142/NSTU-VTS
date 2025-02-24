package org.nstu.bus_tracker;

import android.support.compat.R.string;
import android.util.Log;
import android.util.Pair;

import static android.content.ContentValues.TAG;

import java.lang.reflect.Executable;

/**
 * Created by touhid on 4/18/18.
 * Email: islam.touhid.20@gmail.com
 */
public class Helper {

    public static final String CHILD_NAME_FIREBASE="vehicles";
    public static final String LAST_SEEN="lastseen";

    public static BusInformation formatLatLangBus(double lat,double lang, String license)
    {
        return new BusInformation(lat, lang, license);
    }

    public static Pair<Double,Double> extractLatLangBus(BusInformation bus_info)
    {
        try {
            double lat = bus_info.getLatitude();
            double lang = bus_info.getLongitude();
            return new Pair<>(lat,lang);
        } catch (Exception e) {
            Log.e(TAG, "extractLatLang: ", e);
            return null;
        }
    }

    public static String extractLicense(BusInformation bus_info)
    {
        String license = bus_info.getLicense();
        return license;
    }
    public static String formatLatLang(double lat,double lang)
    {
        return lat+","+lang;
    }

    public static Pair<Double,Double> extractLatLang(String string)
    {

        String[] dogs = string.split(",");

        double lat=Double.parseDouble(dogs[0]);
        double lang=Double.parseDouble(dogs[1]);

        return new Pair<>(lat,lang);
    }

}
