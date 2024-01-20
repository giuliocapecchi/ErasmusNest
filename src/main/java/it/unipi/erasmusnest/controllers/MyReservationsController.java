package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

public class MyReservationsController extends Controller {

    @FXML
    private VBox centerVBox;

    ArrayList<Reservation> reservations;


    @FXML
    protected void initialize() {

        System.out.println("MyReservations initialize");

        reservations = getRedisConnectionManager().getReservationsForUser(getSession().getUser().getEmail());
        for (Reservation reservation : reservations) {
            System.out.println(reservation.toString());
            add(reservation);
        }

    }

    private void add(Reservation reservation) {

        HBox reservationHBox = new HBox();
        reservationHBox.setAlignment(javafx.geometry.Pos.CENTER);
        // color the reservationHBox border
        reservationHBox.setStyle("-fx-border-color: #000000; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #ffffff;");


        ImageView imageView = new ImageView();
        try {
            Image image = new Image(reservation.getApartmentImage(), true); //true let the application continue without waiting for the image to fully load
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
        } catch (Exception e) {
            String imagePath = "/media/no_photo_available.png"; // Path inside the classpath
            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        }

        imageView.fitWidthProperty().bind(reservationHBox.widthProperty().multiply(0.4));
        imageView.setPreserveRatio(true);

        Label city = new Label(reservation.getCity());

        int startMonth = reservation.getStartMonth();
        int startYear = reservation.getStartYear();
        int numberOfMonths = reservation.getNumberOfMonths();
        int endMonth = (startMonth + numberOfMonths) % 12;
        int endYear = startYear + (startMonth + numberOfMonths) / 12;

        Label period = new Label("From " + startMonth + "/" + startYear + " to " + endMonth + "/" + endYear);

        reservationHBox.getChildren().addAll(imageView, city, period);
        centerVBox.getChildren().add(reservationHBox);

    }

    @FXML
    protected void loginButtonClick(){
        super.changeWindow("login");
    }
}
