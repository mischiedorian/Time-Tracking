package com.dorian.licenta.FirebaseCloudMessaging;

public class NotificationData {

    private String imageName;
    private double rating;
    private String title;
    private String textMessage;
    private String sound;

    public NotificationData() {}

    public NotificationData(String imageName,double rating, String title, String textMessage, String sound) {
        this.imageName = imageName;
        this.rating = rating;
        this.title = title;
        this.textMessage = textMessage;
        this.sound = sound;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
