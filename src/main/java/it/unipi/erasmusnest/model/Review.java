package it.unipi.erasmusnest.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Review {
    private String apartmentId;
    private String userEmail;
    private String comments;
    private int rating;
    LocalDate timestamp;

    public Review(String apartmentId, String userEmail, String text, int rating) {
        this.apartmentId = apartmentId;
        this.userEmail = userEmail;
        this.comments = text;
        this.rating = rating;
        // quando viene creata una Review viene automaticamente aggiunta la data corrente
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timestamp = LocalDate.parse(LocalDate.now().toString(), formatter);
    }

    public Review(String apartmentId, String userEmail, String text, int rating, String timestamp) throws DateTimeParseException{
        this.apartmentId = apartmentId;
        this.userEmail = userEmail;
        this.comments = text;
        this.rating = rating;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timestamp = LocalDate.parse(timestamp, formatter);
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }


    /**
     * Imposta la data e l'ora del timestamp utilizzando una stringa nel formato "yyyy-MM-dd".
     *
     * @param timestamp La stringa che rappresenta il timestamp nel formato "yyyy-MM-dd".
     * @throws DateTimeParseException Se la stringa timestamp non è nel formato corretto.
     */
    public void setTimestamp(String timestamp) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timestamp = LocalDate.parse(timestamp, formatter);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
