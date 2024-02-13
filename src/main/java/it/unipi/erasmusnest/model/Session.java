package it.unipi.erasmusnest.model;

import it.unipi.erasmusnest.utils.StudyFieldsLoader;

import java.util.ArrayList;
import java.util.List;

public class Session {
    private User user;
    private String city;
    private boolean logged;

    private Apartment apartment; // appartamento visualizzato

    private boolean connectionError;

    private Integer current_page = 1;

    private Integer current_filter = 0;

    private String otherProfileMail;

    private List<String> studyFieldsOptions; // list of possible study fields

    private List<String> cities; // sono tutte le città presenti nel db, non quelle di interesse!

    private List<String> myApartmentsIds; // contengono gli id degli appartamenti posseduti dall'utente

    private ArrayList <String> reservationsApartmentIds; // lista degli appartamenti su cui ho una prenotazione attiva

    public Session() {
        this.user = new User();
        this.logged = false;
        this.connectionError = false;
        studyFieldsOptions = new ArrayList<>();
        studyFieldsOptions = StudyFieldsLoader.getStudyFields();
        cities = new ArrayList<>();
        myApartmentsIds = new ArrayList<>();
        reservationsApartmentIds = new ArrayList<>();
        apartment = new Apartment();
    }

    public void reset() {
        // svuoto la sessione
        this.user = new User();
        this.logged = false;
        this.connectionError = false;
        myApartmentsIds = new ArrayList<>();
        reservationsApartmentIds = new ArrayList<>();
        apartment = new Apartment();
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

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public boolean getConnectionError() {
        return connectionError;
    }

    public void setConnectionError(boolean connectionError) {
        this.connectionError = connectionError;
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

    public List<String> getStudyFieldsOptions() {
        return studyFieldsOptions;
    }

    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    public List<String> getMyApartmentsIds() {
        return myApartmentsIds;
    }

    public void setMyApartmentsIds(List<String> myApartmentsIds) {
        this.myApartmentsIds = myApartmentsIds;
    }

    public ArrayList<String> getReservationsApartmentIds() {
        return reservationsApartmentIds;
    }

    public void setReservationsApartmentIds(ArrayList<String> reservationsApartmentIds) {
        this.reservationsApartmentIds = reservationsApartmentIds;
    }


}
