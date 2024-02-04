package it.unipi.erasmusnest.model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Arrays;

public class Apartment {

    private String id;              // embedded in User (host)
    private String name;            // embedded in User (host)
    private String description;
    private Point2D location;
    private ArrayList<String> studyFields;
    private Integer dollarPriceMonth;
    private Integer maxAccommodates;
    private String hostEmail;
    private ArrayList<String> imageUrls;        // embedded in User (host)
    private Double averageRating;    // embedded in User (host)

    private int numberOfReviews = 0; // in neo4j
    private Integer bathrooms; // in MONGO
    private String hostName;
    private String hostSurname;



    //Complete constructor
    /*
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
    */

    // COSTRUTTORE COMPLETO CORRETTO CON BAGNI IN INTEGER

    // TODO: DELETE THIS ONE
    /*
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
    */

    // TODO: DELETE THIS ONE
    /*
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
    */

    public Apartment(String apartmentId, String apartmentName, String imageURL, Double averageReviewScores, Integer numberOfReviews) {
        this.id = apartmentId;
        this.name = apartmentName;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = averageReviewScores;
        this.numberOfReviews = numberOfReviews;
    }

    // TODO: DELETE THIS ONE
    /*
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
    */

    public Apartment(String id, String houseName, String imageURL, Double reviewScoresRating)
    {
        this.id = id;
        this.name = houseName;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
        this.averageRating = reviewScoresRating;
    }

    // TODO: DELETE THIS ONE
    /*
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
    */

    // TODO: DELETE THIS ONE
    /*
    public Apartment(Long id, String name, String imageURL) {
        this.id=id;
        this.name=name;
        this.imageUrls = new ArrayList<>();
        this.imageUrls.add(imageURL);
    }
    */


    public Apartment(String id, String houseName, String description, Point2D point2D, Integer price, Integer accommodates, String hostEmail, String imageURL, Integer bathroomsNumber)
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

    public Apartment(String id, String houseName, String description, Point2D point2D, Integer price, Integer accommodates, String hostEmail, ArrayList<String> imageUrls, Integer bathroomsNumber)
    {
        this.id = id;
        this.name = houseName;
        this.description = description;
        this.location = point2D;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = hostEmail;
        this.imageUrls = imageUrls;
        this.bathrooms = bathroomsNumber;
    }



    //TODO : costruttore di Cape dove usiamo un'arrayList di stringhe per gli url invece che una stringa singola
    /*
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
    */

    // TODO COSTRUTTORE DI CAPE MODIFICATO METTENDO BATHROOMS A INTEGER E USANDO ARRAYLIST DI STRINGHE PER GLI URL
    public Apartment(String id, String houseName, String neighborhood, Point2D location, Integer price, Integer accommodates, String userEmail, ArrayList<String> imagesURL, double averageRating, int numberOfReviews, Integer bathrooms, String name, String surname) {
        this.id = id;
        this.name = houseName;
        this.description = neighborhood;
        this.location = location;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = userEmail;
        this.imageUrls = new ArrayList<>();
        // this.imageUrls.addAll(Arrays.asList(imageURLArray));
        this.imageUrls = imagesURL;
        this.averageRating = averageRating;
        this.numberOfReviews = numberOfReviews;
        this.bathrooms = bathrooms;
        this.hostName = name;
        this.hostSurname = surname;
    }

    // TODO NUOVO COSTRUTTORE SENZA ID
    public Apartment(String houseName, String houseDescription, Point2D location, Integer price, Integer accommodates, String userEmail, ArrayList<String> arrayList, double v, int i, Integer bathrooms, String name, String surname) {
        this.name = houseName;
        this.description = houseDescription;
        this.location = location;
        this.dollarPriceMonth = price;
        this.maxAccommodates = accommodates;
        this.hostEmail = userEmail;
        this.imageUrls = arrayList;
        this.averageRating = v;
        this.numberOfReviews = i;
        this.bathrooms = bathrooms;
        this.hostName = name;
        this.hostSurname = surname;
    }

    public Apartment(String id, String houseName) {
        this.id = id;
        this.name = houseName;
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

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public ArrayList<String> getImageURL() {
        return imageUrls;
    }

    public void setImageURL(ArrayList<String> imageURL) {
        this.imageUrls=imageURL;
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

    public Integer getDollarPriceMonth() {
        return dollarPriceMonth;
    }

    public Integer getMaxAccommodates() {
        return maxAccommodates;
    }

    public void setMaxAccommodates(Integer maxAccommodates) {
        this.maxAccommodates = maxAccommodates;
    }

    public void setDollarPriceMonth(Integer dollarPriceMonth) {
        this.dollarPriceMonth = dollarPriceMonth;
    }

    @Override
    public String toString() {
        return "Apartment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", position=" + location +
                ", studyFields=" + studyFields +
                ", dollarPriceMonth=" + dollarPriceMonth +
                ", maxAccommodates=" + maxAccommodates +
                ", bathrooms=" + bathrooms +
                ", email='" + hostEmail + '\'' +
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
