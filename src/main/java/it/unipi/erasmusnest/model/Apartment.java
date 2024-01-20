package it.unipi.erasmusnest.model;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class Apartment {

    private Long id;              // embedded in User (host)
    private String name;            // embedded in User (host)
    private String description;
    private Point2D location;
    private ArrayList<String> studyFields;
    private Double dollarPriceMonth;
    private Integer maxAccommodates;
    private String hostEmail;
    private String imageURL;        // embedded in User (host)

    private Double averageRating;    // embedded in User (host)

    private int numberOfReviews = 0; // in neo4j

    public Apartment(Long id, String name, String description, Point2D location, ArrayList<String> studyFields, String hostEmail, String imageURL, Double averageRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.studyFields = studyFields;
        this.hostEmail = hostEmail;
        this.imageURL = imageURL;
        this.averageRating = averageRating;
    }

    public Apartment(Long id, String name, String description, Point2D location, String studyField, String hostEmail, String imageURL, Double averageRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.studyFields = new ArrayList<>();
        this.studyFields.add(studyField);
        this.hostEmail = hostEmail;
        this.imageURL = imageURL;
        this.averageRating = averageRating;
    }

    public Apartment(Long apartmentId, String apartmentName, String pictureUrl, Double averageReviewScores, Integer numberOfReviews) {
        this.id = apartmentId;
        this.name = apartmentName;
        this.imageURL = pictureUrl;
        this.averageRating = averageReviewScores;
        this.numberOfReviews = numberOfReviews;
    }

    public Apartment(Long id, String name, String description, Point2D point2D, Double price, Integer accommodates, String hostEmail, String pictureUrl, Double reviewScoresRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = point2D;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = hostEmail;
        this.imageURL = pictureUrl;
        this.averageRating = reviewScoresRating;
    }

    public Apartment(Long id, String houseName, String pictureUrl, Double reviewScoresRating)
    {
        this.id = id;
        this.name = houseName;
        this.imageURL = pictureUrl;
        this.averageRating = reviewScoresRating;
    }

    public Apartment(Long id, String houseName, String description, Point2D point2D, double v, Integer accommodates, String hostEmail, String pictureUrl) {
        this.id = id;
        this.name = houseName;
        this.description = description;
        this.location = point2D;
        this.dollarPriceMonth = v;
        this.maxAccommodates = accommodates;
        this.hostEmail = hostEmail;
        this.imageURL = pictureUrl;
    }

    public Apartment(Long id, String name, String pictureUrl) {
        this.id=id; this.name=name; this.imageURL=pictureUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public ArrayList<String> getStudyFields() {
        return studyFields;
    }

    public void setStudyFields(ArrayList<String> studyFields) {
        this.studyFields = studyFields;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
    }

    public Double getDollarPriceMonth() {
        return dollarPriceMonth;
    }

    public Integer getMaxAccommodates() {
        return maxAccommodates;
    }

    @Override
    public String toString() {
        return "Apartment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", location=" + location +
                ", studyFields=" + studyFields +
                ", dollarPriceMonth=" + dollarPriceMonth +
                ", maxAccommodates=" + maxAccommodates +
                ", hostEmail='" + hostEmail + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", averageRating=" + averageRating +
                '}';

    }

    public Object getNumberOfReviews() {
        return numberOfReviews;
    }

    public void setNumberOfReviews(int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }
}
