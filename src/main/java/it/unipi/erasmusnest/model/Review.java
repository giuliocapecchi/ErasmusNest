package it.unipi.erasmusnest.model;

public class Review {
    private String apartmentId;
    private String userEmail;
    private String comments;
    private float rating;

    public Review(String apartmentId, String userEmail, String text, float rating) {
        this.apartmentId = apartmentId;
        this.userEmail = userEmail;
        this.comments = text;
        this.rating = rating;
    }


    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }


    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
