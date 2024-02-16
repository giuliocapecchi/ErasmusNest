package it.unipi.erasmusnest.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;        // GRAPH, MONGO, REDIS
    private String name;        // MONGO
    private String surname;     // MONGO
    private ArrayList<String> preferredCities;  // GRAPH
    private String studyField;  // MONGO, GRAPH
    private List<Apartment> houses;
    private String password;


    public User(){}

    public User(String email, String name, String surname, ArrayList<String> preferredCities, String studyField) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.preferredCities = preferredCities;
        this.studyField = studyField;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public ArrayList<String> getPreferredCities() {
        return preferredCities;
    }

    public void setPreferredCities(ArrayList<String> preferredCities) {
        this.preferredCities = preferredCities;
    }

    public String getStudyField() {
        return studyField;
    }

    public void setStudyField(String studyField) {
        this.studyField = studyField;
    }

    public List<Apartment> getApartments()
    {
        return houses;
    }

    public void setApartments(List<Apartment> houses)
    {
        this.houses = houses;
    }

    public boolean isAdmin() {
        if(email == null) return false;
        //Per ora l'admin è solo l'utente con email "admin"
        //Parsing dell'email
        String[] emailParts = email.split("@");
        String emailDomain = emailParts[1];
        return emailDomain.equals("erasmusnest.com");
    }

    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", preferredCities=" + preferredCities +
                ", studyField='" + studyField + '\'' +
                ", houses=" + houses +
                ", password='" + password + '\'' +
                '}';
    }

}
