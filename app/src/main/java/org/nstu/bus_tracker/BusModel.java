package org.nstu.bus_tracker;

/**
 * Created by touhid on 3/25/18.
 * Email: islam.touhid.20@gmail.com
 */

public class BusModel {



    private double latitude,longitue;

   public BusModel()
   {

   }

    public BusModel(double latitude,double longitue)
    {

        this.latitude=latitude;
        this.longitue=longitue;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitue()
    {
        return longitue;
    }


}
