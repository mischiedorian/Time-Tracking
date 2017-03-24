package com.dorian.licenta.Location;

/**
 * Created by Dorian on 22/03/2017.
 */

public interface UtilsLocations {
   double distanceBetween2Locations(MyLocation location1);
   void deleteLocation();
   void updateLocation(String oraSfarsit);
   void insertLocation();
   int minutesLocation();
}
