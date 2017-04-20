package com.dorian.licenta.Location;

import java.util.ArrayList;

public interface UtilsLocations {
   double distanceBetween2Locations(MyLocation location1);
   void deleteLocation();
   void updateLocation(String oraSfarsit);
   void insertLocation();
   int minutesLocation();
}
