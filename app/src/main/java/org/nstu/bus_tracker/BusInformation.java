package org.nstu.bus_tracker;

import android.support.annotation.Keep;

@Keep
public class BusInformation {
    private double latitude;
    private double longitude;
    private String license;

    public BusInformation(){}

    BusInformation( double latitude, double longitude, String license) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.license = license;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    void setLicense(String license) {
        this.license = license;
    }

    double getLatitude() {
        return latitude;
    }

    double getLongitude() {
        return longitude;
    }

    String getLicense() {
        return license;
    }
}
