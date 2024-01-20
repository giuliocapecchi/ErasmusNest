package it.unipi.erasmusnest.graphicmanagers;

import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;
import it.unipi.erasmusnest.model.Reservation;
import it.unipi.erasmusnest.model.Session;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class ReservationGraphicManager {

    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button confirmButton;
    private ArrayList<Reservation> reservations;
    private final Session session;
    private final RedisConnectionManager redisConnectionManager;
    private boolean available;
    private ArrayList<LocalDate> enabledEndDates;

    public ReservationGraphicManager(DatePicker startDatePicker, DatePicker endDatePicker, Button confirmButton, Session session, RedisConnectionManager redisConnectionManager){
        this.startDatePicker = startDatePicker;
        this.endDatePicker = endDatePicker;
        this.confirmButton = confirmButton;
        this.session = session;
        this.redisConnectionManager = redisConnectionManager;
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
            enableOnlyFutureFirstDayOfMonthsAvailable(startDatePicker, reservations);
            setHandlers();
        }else{
            startDatePicker.setDisable(true);
        }

    }

    private void setHandlers(){
        startDatePicker.setOnAction(event -> onStartDatePickerClick());
        endDatePicker.setOnAction(event -> onEndDatePickerClick());
        confirmButton.setOnAction(event -> onConfirmButtonClick());
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
        boolean isIn = false;
        for(Reservation reservation : reservations){
            int startYear = reservation.getStartYear();
            int startMonth = reservation.getStartMonth();
            int numberOfMonths = reservation.getNumberOfMonths();
            for(int i = 0; i<numberOfMonths && !isIn; i++){
                if(date.getMonthValue() == startMonth && date.getYear() == startYear){
                    isIn = true;
                }
                startMonth++;
                if(startMonth == 13){
                    startMonth = 1;
                    startYear++;
                }
            }
        }
        return !isIn;
    }

    private boolean isLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return false;
        }

        int currentMonth = date.getMonthValue();
        int nextMonth = date.plusDays(1).getMonthValue();

        return nextMonth != currentMonth;
    }

    public void onConfirmButtonClick(){
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if(startDate != null && endDate != null) {

            int startYear = startDate.getYear();
            int startMonth = startDate.getMonthValue();

            Period period = Period.between(startDate, endDate);
            int numberOfMonths = (period.getMonths() + period.getYears() * 12) + 1;

            String userEmail = session.getUser().getEmail();
            String houseId = String.valueOf(session.getApartmentId());
            redisConnectionManager.addReservation(userEmail, houseId, String.valueOf(startYear), String.valueOf(startMonth), String.valueOf(numberOfMonths));

        }
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
