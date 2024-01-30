package it.unipi.erasmusnest.model;

import it.unipi.erasmusnest.utils.StudyFieldsLoader;

import java.util.ArrayList;
import java.util.List;

public class Session {
    private User user;
    private String city;
    private boolean logged;
    private Long apartmentId;

    private Double apartmentAverageRating;

    private boolean connectionError;

    private Integer current_page = 1;

    private Integer current_filter = 0;

    private String otherProfileMail;

    private String nextWindowName;

    private List<String> studyFields;

    private List<String> cities;

    public Session() {
        this.user = new User();
        this.logged = false;
        this.connectionError = false;
        studyFields = new ArrayList<>();
        studyFields = StudyFieldsLoader.getStudyFields();
    }

    public void reset() {
        this.user = new User();
        this.logged = false;
        this.connectionError = false;
    }

    public String getOtherProfileMail() {
        return otherProfileMail;
    }

    public void setOtherProfileMail(String otherProfileMail) {
        this.otherProfileMail = otherProfileMail;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Double getApartmentAverageRating() {
        return apartmentAverageRating;
    }

    public void setApartmentAverageRating(Double apartmentAverageRating) {
        this.apartmentAverageRating = apartmentAverageRating;
    }

    public boolean getConnectionError() {
        return connectionError;
    }

    public void setConnectionError(boolean connectionError) {
        this.connectionError = connectionError;
    }

    public boolean isConnectionError() {
        return connectionError;
    }

    public Integer getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(Integer current_page) {
        this.current_page = current_page;
    }

    public Integer getCurrent_filter() {
        return current_filter;
    }

    public void setCurrent_filter(Integer current_filter) {
        this.current_filter = current_filter;
    }

    public String getNextWindowName() {
        return nextWindowName;
    }

    public void setNextWindowName(String nextWindowName) {
        this.nextWindowName = nextWindowName;
    }

    public List<String> getStudyFields() {
        return studyFields;
    }

    public void setStudyFields(List<String> studyFields) {
        this.studyFields = studyFields;
    }

    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

}
