package com.dorian.licenta.NearbyPlace;

public class NearbyPlace {
    private String placeName;
    private String vicinity;
    private double lat;
    private double lng;
    private String reference;
    private double rating;

    public NearbyPlace(String placeName, String vicinity, double lat, double lng, String reference, double rating) {
        this.placeName = placeName;
        this.vicinity = vicinity;
        this.lat = lat;
        this.lng = lng;
        this.reference = reference;
        this.rating = rating;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "NearbyPlace{" +
                "placeName='" + placeName + '\'' +
                ", vicinity='" + vicinity + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", reference='" + reference + '\'' +
                ", rating=" + rating +
                '}';
    }
}
