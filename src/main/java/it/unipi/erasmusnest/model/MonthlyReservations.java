package it.unipi.erasmusnest.model;

import java.util.ArrayList;

public class MonthlyReservations {

    private final Integer year;
    private final Integer month;
    private Integer numberOfReservations;
    private final ArrayList<String> studentsEmails;

    public MonthlyReservations(Integer year, Integer month, String studentEmail) {
        this.year = year;
        this.month = month;
        this.numberOfReservations = 1;
        studentsEmails = new ArrayList<>();
        studentsEmails.add(studentEmail);
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getNumberOfReservations() {
        return numberOfReservations;
    }

    public void addReservation(){
        this.numberOfReservations++;
    }

    public boolean isReservedForStudent(String studentEmail){
        return studentsEmails.contains(studentEmail);
    }

    @Override
    public String toString() {
        return "MonthlyReservations{" +
                "year=" + year +
                ", month=" + month +
                ", numberOfReservations=" + numberOfReservations +
                '}';
    }
}
