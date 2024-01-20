package it.unipi.erasmusnest.model;

public class Review {
    private Long apartmentId;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    private String userEmail;
    private String comments;
    private float rating;

    public Review(Long apartmentId, String userEmail, String text, float rating) {
        this.apartmentId = apartmentId;
        this.userEmail = userEmail;
        this.comments = text;
        this.rating = rating;
    }


    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
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
}
