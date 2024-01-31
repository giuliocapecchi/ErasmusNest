package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Reservation;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyReservationsController extends Controller {

    @FXML
    private VBox centerVBox;

    @FXML
    private ScrollPane scrollPane;

    ArrayList<Reservation> reservations;


    @FXML
    protected void initialize() {

        System.out.println("MyReservations initialize");

        if(getSession().getApartmentsId() == null){
            reservations = getRedisConnectionManager().getReservationsForUser(getSession().getUser().getEmail());
            if(!reservations.isEmpty()){
                for (Reservation reservation : reservations) {
                    add(reservation, "student");
                }
            } else {
                noReservation();
            }
        } else {
            // get the reservations for the apartments
            List<Long> apartmentsId = getSession().getApartmentsId();
            reservations = getRedisConnectionManager().getReservationsForApartments(apartmentsId);

            // ordering the reservations by timestamp
            sortReservationsByTimestampAsc();

            for(Reservation reservation : reservations){
                System.out.println(">>> "+reservation.getApartmentId()+" "+reservation.getTimestamp());
                add(reservation, "host");
            }

        }

        scrollPane.setVvalue(0.0);
    }

    private void sortReservationsByTimestampAsc(){
        reservations.sort((o1, o2) -> {
            if(o1.getTimestamp().isBefore(o2.getTimestamp())){
                return -1;
            } else if(o1.getTimestamp().isAfter(o2.getTimestamp())){
                return 1;
            } else {
                return 0;
            }
        });
    }

    private void noReservation(){

        HBox reservationHBox = new HBox();
        reservationHBox.setAlignment(Pos.BOTTOM_CENTER);

        Label msg = new Label("No reservations found");

        // change the style of the label
        msg.setStyle("-fx-font-size: 20px; -fx-text-fill: #019fe1; -fx-font-style: italic;");

        reservationHBox.getChildren().add(msg);
        centerVBox.getChildren().add(reservationHBox);

    }

    private void add(Reservation reservation, String userType) {

        // userType = "host" | "student"

        HBox reservationHBox = new HBox();
        reservationHBox.setAlignment(Pos.CENTER_LEFT);
        // color the reservationHBox border
        reservationHBox.setStyle("-fx-border-color: #ff6f00; -fx-border-width: 1px; -fx-border-radius: 0px; -fx-background-radius: 5px; -fx-background-color: #ffffff;");

        VBox imageVBox = new VBox();
        imageVBox.setAlignment(Pos.CENTER_LEFT);
        VBox cityVBox = new VBox();
        cityVBox.setAlignment(javafx.geometry.Pos.CENTER);
        VBox periodVBox = new VBox();
        periodVBox.setAlignment(javafx.geometry.Pos.CENTER);
        VBox stateVBox = new VBox();
        stateVBox.setAlignment(javafx.geometry.Pos.CENTER);
        VBox buttonsVBox = new VBox();
        buttonsVBox.setAlignment(javafx.geometry.Pos.CENTER);

        ImageView imageView = new ImageView();
        try {
            // get the pat of reservation
            Path path = Path.of(reservation.getApartmentImage());

            Image image = new Image(path.toUri().toURL().toExternalForm(), 800, 0, true, true, true);
            //Image image = new Image(reservation.getApartmentImage(), true); //true let the application continue without waiting for the image to fully load
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
        } catch (Exception e) {
            String imagePath = "/media/no_photo_available.png"; // Path inside the classpath
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        }

        imageView.fitWidthProperty().bind(reservationHBox.widthProperty().multiply(0.2));
        imageView.setPreserveRatio(true);

        Label city = new Label(reservation.getCity());

        // change the style of the label
        city.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #019fe1;");

        LocalDate lastDayDate = LocalDate.of(reservation.getStartYear(), reservation.getStartMonth(), 1);
        lastDayDate = lastDayDate.plusMonths(reservation.getNumberOfMonths());
        lastDayDate = lastDayDate.minusDays(1);

        int startDay = 1;
        int lastDay = lastDayDate.getDayOfMonth();
        int startMonth = reservation.getStartMonth();
        int startYear = reservation.getStartYear();
        int numberOfMonths = reservation.getNumberOfMonths();
        int endMonth = (startMonth + numberOfMonths) % 12 - 1;
        int endYear = startYear + (startMonth + numberOfMonths) / 12;

        String periodFromTo = "from " + startDay + "/" + startMonth + "/" + startYear + "\nto " + lastDay + "/" + endMonth + "/" + endYear;
        Label period = new Label(periodFromTo);
        period.setStyle("-fx-font-size: 15px; -fx-text-fill: #ff6f00;");

        String state = "state: "+reservation.getState();
        Label stateLabel = new Label(state);
        stateLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #019fe1;");

        // remove '\n' from periodFromTo
        String msgPeriod = periodFromTo.replace("\n", " ");

        buildButtons(buttonsVBox, reservation, userType, msgPeriod);

        imageVBox.getChildren().add(imageView);
        cityVBox.getChildren().add(city);
        periodVBox.getChildren().add(period);
        stateVBox.getChildren().add(stateLabel);

        imageVBox.prefWidthProperty().bind(reservationHBox.widthProperty().multiply(0.5));
        cityVBox.prefWidthProperty().bind(reservationHBox.widthProperty().multiply(0.5));
        periodVBox.prefWidthProperty().bind(reservationHBox.widthProperty().multiply(0.5));
        stateVBox.prefWidthProperty().bind(reservationHBox.widthProperty().multiply(0.5));
        buttonsVBox.prefWidthProperty().bind(reservationHBox.widthProperty().multiply(0.5));
        reservationHBox.getChildren().addAll(imageVBox, cityVBox, periodVBox, stateVBox, buttonsVBox);
        centerVBox.getChildren().add(reservationHBox);

    }

    private void buildButtons(VBox buttonsVBox, Reservation reservation, String userType, String msgPeriod){
        if(userType.equals("student")) {
            Button deleteButton = new Button("Delete reservation");
            deleteButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 5px;");
            deleteButton.setOnAction(event -> {
                boolean remove = new AlertDialogGraphicManager("Are you sure you want to delete this reservation\n"
                        + msgPeriod + " in " + reservation.getCity()
                        + "?", "You will not be able to recover it").showAndGetConfirmation();
                if (remove) {
                    getRedisConnectionManager().deleteReservation(reservation);
                    super.changeWindow("myreservations");
                }
            });
            buttonsVBox.getChildren().add(deleteButton);
        } else if(reservation.getState().equals("pending")){
            Button approveButton = new Button("Approve reservation");
            approveButton.setStyle("-fx-background-color: #63d27f; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 5px;");
            approveButton.setOnAction(event -> {
                boolean approve = new AlertDialogGraphicManager("Are you sure you want to approve this reservation\n"
                        + msgPeriod + " in " + reservation.getCity()
                        + "?", "You will not be able to reject it later").showAndGetConfirmation();
                if (approve) {
                    // TODO getRedisConnectionManager().approveReservation(reservation);
                    super.changeWindow("myreservations");
                }
            });
            Button rejectButton = new Button("Reject reservation");
            rejectButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: #ffffff; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 5px;");
            rejectButton.setOnAction(event -> {
                boolean approve = new AlertDialogGraphicManager("Are you sure you want to reject this reservation\n"
                        + msgPeriod + " in " + reservation.getCity()
                        + "?", "You will not be able to approve it later").showAndGetConfirmation();
                if (approve) {
                    // TODO getRedisConnectionManager().rejectReservation(reservation);
                    super.changeWindow("myreservations");
                }
            });
            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(10);
            vBox.getChildren().addAll(approveButton, rejectButton);
            buttonsVBox.getChildren().add(vBox);
        }
    }

    @FXML
    protected void logoutButtonClick(){
        getSession().reset();
        super.changeWindow("login");
    }

    @FXML
    protected void homepageButtonClick(){
        super.changeWindow("homepage");
    }

    @FXML
    protected void profileButtonClick(){
        super.changeWindow("myProfile");
    }
}
