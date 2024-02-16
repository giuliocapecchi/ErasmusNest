package it.unipi.erasmusnest.model;

import java.time.LocalDateTime;

public class Reservation {
    private String studentEmail;
    private String apartmentId;
    private int startYear;
    private int startMonth;
    private int numberOfMonths;
    private LocalDateTime timestamp;
    private String city;
    private String apartmentImage;
    private String state;


    public Reservation(String studentEmail, String apartmentId, int startYear, int startMonth, int numberOfMonths) {
        this.studentEmail = studentEmail;
        this.apartmentId = apartmentId;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.numberOfMonths = numberOfMonths;
    }

    public Reservation(String studentEmail, String apartmentId, int startYear, int startMonth,
                       int numberOfMonths, LocalDateTime timestamp, String city,
                       String apartmentImage, String state) {
        this.studentEmail = studentEmail;
        this.apartmentId = apartmentId;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.numberOfMonths = numberOfMonths;
        this.timestamp = timestamp;
        this.city = city;
        this.apartmentImage = apartmentImage;
        this.state = state;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getNumberOfMonths() {
        return numberOfMonths;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getApartmentImage() {
        return apartmentImage;
    }

    public void setApartmentImage(String apartmentImage) {
        this.apartmentImage = apartmentImage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "studentEmail='" + studentEmail + '\'' +
                ", apartmentId='" + apartmentId + '\'' +
                ", startYear=" + startYear +
                ", startMonth=" + startMonth +
                ", numberOfMonths=" + numberOfMonths +
                ", timestamp=" + timestamp +
                ", city='" + city + '\'' +
                ", apartmentImage='" + apartmentImage + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
