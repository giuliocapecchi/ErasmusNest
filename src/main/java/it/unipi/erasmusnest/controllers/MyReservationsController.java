package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Reservation;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class MyReservationsController extends Controller {

    @FXML
    private VBox centerVBox;

    ArrayList<Reservation> reservations;

    public MyReservationsController() {

        reservations = getRedisConnectionManager().getReservationsForUser(getSession().getUser().getEmail());

    }

    @FXML
    protected void initialize() {

        for (Reservation reservation : reservations) {
            add(reservation);
        }

    }

    private void add(Reservation reservation) {

        HBox reservationHBox = new HBox();
        /*ImageView
        reservationHBox.getChildren().add();*/

    }
}
