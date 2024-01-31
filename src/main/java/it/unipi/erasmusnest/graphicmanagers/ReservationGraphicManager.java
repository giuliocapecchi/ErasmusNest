package it.unipi.erasmusnest.graphicmanagers;

import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;
import it.unipi.erasmusnest.model.MonthlyReservations;
import it.unipi.erasmusnest.model.Reservation;
import it.unipi.erasmusnest.model.Session;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class ReservationGraphicManager {

    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button confirmButton;
    private ArrayList<Reservation> reservations;
    private final Session session;
    private final RedisConnectionManager redisConnectionManager;
    private boolean available;
    private ArrayList<LocalDate> enabledEndDates;
    private final Integer maxAccommodatesPerMonth;
    private ArrayList<MonthlyReservations> monthlyReservations;

    public ReservationGraphicManager(DatePicker startDatePicker, DatePicker endDatePicker, Button confirmButton, Session session, RedisConnectionManager redisConnectionManager, Integer maxAccommodatesPerMonth){
        this.startDatePicker = startDatePicker;
        this.endDatePicker = endDatePicker;
        this.confirmButton = confirmButton;
        this.session = session;
        this.redisConnectionManager = redisConnectionManager;
        this.maxAccommodatesPerMonth = maxAccommodatesPerMonth;
        initialize();
    }

    private void initialize(){
        startDatePicker.setShowWeekNumbers(false);
        endDatePicker.setShowWeekNumbers(false);
        startDatePicker.setEditable(false);
        endDatePicker.setEditable(false);
        endDatePicker.setDisable(true);
        confirmButton.setDisable(true);

        if(session.isLogged()){
            enabledEndDates = new ArrayList<>();
            reservations = redisConnectionManager.getReservationsForApartment(session.getApartmentId());

            computeNumberOfReservationsPerMonth();
            System.out.println(monthlyReservations);

            enableOnlyFutureFirstDayOfMonthsAvailable(startDatePicker, reservations);
            setHandlers();
        }else{
            startDatePicker.setDisable(true);
        }

    }

    private void setHandlers(){
        startDatePicker.setOnAction(event -> onStartDatePickerClick());
        endDatePicker.setOnAction(event -> onEndDatePickerClick());
    }

    private void computeNumberOfReservationsPerMonth(){
        monthlyReservations = new ArrayList<>();
        for(Reservation reservation : reservations){
            int startYear = reservation.getStartYear();
            int startMonth = reservation.getStartMonth();
            int numberOfMonths = reservation.getNumberOfMonths();
            for(int i = 0; i<numberOfMonths; i++){

                MonthlyReservations monthlyReservation = getReservationsForMonth(startYear, startMonth);
                if(monthlyReservation == null){
                    monthlyReservations.add(new MonthlyReservations(startYear, startMonth, reservation.getStudentEmail()));
                } else {
                    monthlyReservation.addReservation();
                }

                startMonth++;
                if(startMonth == 13){
                    startMonth = 1;
                    startYear++;
                }
            }
        }
    }

    private MonthlyReservations getReservationsForMonth(int year, int month){
        MonthlyReservations monthlyReservationResult = null;
        for(MonthlyReservations monthlyReservation : monthlyReservations){
            if(monthlyReservation.getYear() == year && monthlyReservation.getMonth() == month){
                monthlyReservationResult = monthlyReservation;
                break;
            }
        }
        return monthlyReservationResult;
    }

    private void enableOnlyFutureFirstDayOfMonthsAvailable(DatePicker datePicker, ArrayList<Reservation> reservations){
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                boolean available = isDateAvailable(date, reservations);
                setDisable(empty || date.getDayOfMonth() != 1 || date.isBefore(today) || !available);
                if(date.getDayOfMonth() == 1 && date.isAfter(today)) {
                    setStyle("-fx-background-color: lightgreen;");
                }
                if(!available && date.getDayOfMonth() == 1){
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    }

    private void enableOnlyFutureLastDayOfMonthsAvailable(DatePicker datePicker,  LocalDate startDate, ArrayList<Reservation> reservations) {
        available = true;

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                boolean enabled = enabledEndDates.contains(date);
                if(!enabled && available && isLastDayOfMonth(date) && date.isAfter(startDate.plusDays(1))){
                    available = isDateAvailable(date, reservations);
                    if(available) {
                        enabledEndDates.add(date);
                        enabled = true;
                    }
                }
                if(!enabled) {
                    setDisable(empty || !isLastDayOfMonth(date) || date.isBefore(startDate.plusDays(1)) || !available);
                }
                if(enabled) {
                    setStyle("-fx-background-color: lightgreen;");
                }else if(!available && isLastDayOfMonth(date) && date.isAfter(startDate.plusDays(1))){
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    }

    private boolean isDateAvailable(LocalDate date, ArrayList<Reservation> reservations){
        boolean available = true;
        for(Reservation reservation : reservations){
            int startYear = reservation.getStartYear();
            int startMonth = reservation.getStartMonth();
            int numberOfMonths = reservation.getNumberOfMonths();
            for(int i = 0; i<numberOfMonths && available; i++){
                if(date.getMonthValue() == startMonth && date.getYear() == startYear){
                    MonthlyReservations monthlyReservation = getReservationsForMonth(startYear, startMonth);
                    if(monthlyReservation != null){
                        if(Objects.equals(monthlyReservation.getNumberOfReservations(), maxAccommodatesPerMonth)){
                            available = false;
                        }else{
                            if(monthlyReservation.isReservedForStudent(session.getUser().getEmail())){
                                available = false;
                            }
                        }
                    }
                }
                startMonth++;
                if(startMonth == 13){
                    startMonth = 1;
                    startYear++;
                }
            }
        }
        return available;
    }

    private boolean isLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return false;
        }

        int currentMonth = date.getMonthValue();
        int nextMonth = date.plusDays(1).getMonthValue();

        return nextMonth != currentMonth;
    }



    public void onStartDatePickerClick() {
        enabledEndDates = new ArrayList<>();
        LocalDate startDate = startDatePicker.getValue();
        enableOnlyFutureLastDayOfMonthsAvailable(endDatePicker, startDate, reservations);
        endDatePicker.setDisable(false);
        endDatePicker.setValue(startDatePicker.getValue().plusMonths(1).minusDays(1));
    }

    public void onEndDatePickerClick() {
        confirmButton.setDisable(false);
    }

}