package it.unipi.erasmusnest.model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Arrays;

public class Apartment {

    private Long id;              // embedded in User (host)
    private String name;            // embedded in User (host)
    private String description;
    private Point2D location;
    private ArrayList<String> studyFields;
    private Double dollarPriceMonth;
    private Integer maxAccommodates;
    private String hostEmail;
    private ArrayList<String> imageUrls;        // embedded in User (host)
    private Double averageRating;    // embedded in User (host)

    private int numberOfReviews = 0; // in neo4j
    private String bathrooms; // in MONGO
    private String hostName;
    private String hostSurname;



    //Complete constructor
    public Apartment(Long id, String name, String description, Point2D location, Double dollarPriceMonth,
                     Integer maxAccomodates, String hostEmail, String imageURL, Double averageRating,
                     int numberOfReviews, String bathrooms, String hostName, String hostSurname)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.dollarPriceMonth = dollarPriceMonth;
        this.maxAccommodates = maxAccomodates;
        this.hostEmail = hostEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = averageRating;
        this.numberOfReviews = numberOfReviews;
        this.bathrooms = bathrooms;
        this.hostName = hostName;
        this.hostSurname = hostSurname;
    }

    // TODO: DELETE THIS ONE
    public Apartment(Long id, String name, String description, Point2D location, ArrayList<String> studyFields, String hostEmail, String imageURL, Double averageRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.studyFields = studyFields;
        this.hostEmail = hostEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = averageRating;
    }

    // TODO: DELETE THIS ONE
    public Apartment(Long id, String name, String description, Point2D location, String studyField, String hostEmail, String imageURL, Double averageRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.studyFields = new ArrayList<>();
        this.studyFields.add(studyField);
        this.hostEmail = hostEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = averageRating;
    }

    public Apartment(Long apartmentId, String apartmentName, String imageURL, Double averageReviewScores, Integer numberOfReviews) {
        this.id = apartmentId;
        this.name = apartmentName;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = averageReviewScores;
        this.numberOfReviews = numberOfReviews;
    }

    // TODO: DELETE THIS ONE
    public Apartment(Long id, String name, String description, Point2D point2D, Double price, Integer accommodates, String hostEmail, String imageURL, Double reviewScoresRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = point2D;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = hostEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = reviewScoresRating;
    }

    public Apartment(Long id, String houseName, String imageURL, Double reviewScoresRating)
    {
        this.id = id;
        this.name = houseName;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = reviewScoresRating;
    }

    // TODO: DELETE THIS ONE
    public Apartment(Long id, String houseName, String description, Point2D point2D, double v, Integer accommodates, String hostEmail, String imageURL) {
        this.id = id;
        this.name = houseName;
        this.description = description;
        this.location = point2D;
        this.dollarPriceMonth = v;
        this.maxAccommodates = accommodates;
        this.hostEmail = hostEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
    }

    // TODO: DELETE THIS ONE
    public Apartment(Long id, String name, String imageURL) {
        this.id=id;
        this.name=name;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
    }

    public Apartment(Long id, String houseName, String description, Point2D point2D, double price, Integer accommodates, String hostEmail, String imageURL, String bathroomsNumber)
    {
        this.id = id;
        this.name = houseName;
        this.description = description;
        this.location = point2D;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = hostEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.bathrooms = bathroomsNumber;
    }


    //TODO : costruttore di Cape dove usiamo un'arrayList di stringhe per gli url invece che una stringa singola
    public Apartment(long id, String houseName, String neighborhood, Point2D location, Double price, Integer accommodates, String userEmail, String[] imageURLArray, double averageRating, int numberOfReviews, String bathrooms, String name, String surname) {
        this.id = id;
        this.name = houseName;
        this.description = neighborhood;
        this.location = location;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = userEmail;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.addAll(Arrays.asList(imageURLArray));
        this.averageRating = averageRating;
        this.numberOfReviews = numberOfReviews;
        this.bathrooms = bathrooms;
        this.hostName = name;
        this.hostSurname = surname;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostSurname() {
        return hostSurname;
    }

    public void setHostSurname(String hostSurname) {
        this.hostSurname = hostSurname;
    }

    public String getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(String bathrooms) {
        this.bathrooms = bathrooms;
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
        return imageUrls.get(0);
    }

    public void setImageURL(String imageURL) {
        this.imageUrls.add(imageURL);
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

    public void setMaxAccommodates(Integer maxAccommodates) {
        this.maxAccommodates = maxAccommodates;
    }

    public void setDollarPriceMonth(Double dollarPriceMonth) {
        this.dollarPriceMonth = dollarPriceMonth;
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
                ", bathrooms=" + bathrooms +
                ", hostEmail='" + hostEmail + '\'' +
                ", imageURL='" + imageUrls + '\'' +
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
