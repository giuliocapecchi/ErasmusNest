package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class UploadHouseController extends Controller {


    @FXML
    TextField houseNameTextField;
    @FXML
    TextField pictureUrlTextField;
    @FXML
    Spinner<Integer> inputAccommodates;
    @FXML
    Spinner<Double> inputBathrooms;
    @FXML
    Spinner<Double> inputPrice;
    @FXML
    TextField houseDescriptionTextField;

    @FXML
    TextField addressTextField;
    @FXML
    Button geocodeButton;
    @FXML
    Label geocodeResultLabel;
    @FXML
    VBox mapVBox;
    @FXML
    Button uploadButton;
    MapGraphicManager mapGraphicManager;

    public UploadHouseController() {
    }

    @FXML
    private void initialize() {
        mapGraphicManager = new MapGraphicManager();
        uploadButton.setDisable(true);
    }

    @FXML
    void onUploadButtonClick() {
        String houseName = houseNameTextField.getText();
        String [] pictureUrlArray = pictureUrlTextField.getText().split(";"); //Todo : gli url devono essere scomposti in un array di stringhe prima di essere passati a mongoDB
        Integer accommodates = inputAccommodates.getValue();
        String bathrooms = String.valueOf(inputBathrooms.getValue()); //TODO: I BAGNI DOVREBBERO ESSERE INTERI, NON STRINGHE
        Double price = inputPrice.getValue();
        String houseDescription = houseDescriptionTextField.getText();
        double latitude = mapGraphicManager.getLatitude();
        double longitude = mapGraphicManager.getLongitude();
        Point2D location = new Point2D(latitude, longitude);
        String userEmail = getSession().getUser().getEmail();
        User user = getMongoConnectionManager().findUser(userEmail);
        if (user != null) {
            //Create new apartment //todo : perchè a tutti assegna id 7L????
            Apartment apartment = new Apartment(7L, houseName, houseDescription, location, price, accommodates, userEmail,
                    pictureUrlArray, 0.0, 0, bathrooms, user.getName(), user.getSurname());

            // Call Mongo to insert apartment
            if (getMongoConnectionManager().uploadApartment(apartment)) {
                System.out.println("sei qui");
                new AlertDialogGraphicManager("House uploaded correctly","House correctly uploaded.","You will be redirected to your profile","information").show();
                super.changeWindow("uploadHouse","myProfile");
                return;
            } else {
                System.out.println("impossibile caricare casa");}
        } else {
            System.out.println("impossibile caricare casa: l'utente non esiste su Mongo.");
        }
        // in tutti i casi negativi mostro un alert di errore all'utente
        new AlertDialogGraphicManager("House NOT uploaded correctly").show();

    }

    @FXML
    void onBackButtonClick() {
        super.changeWindow("uploadHouse","myProfile");
    }

    @FXML
    void onGeocodeButtonClick() {
        String address = URLEncoder.encode(addressTextField.getText(), StandardCharsets.UTF_8);
        WebView mapWebView = new WebView();
        mapVBox.getChildren().clear();
        mapVBox.getChildren().add(mapWebView);
        mapWebView.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        mapWebView.maxHeightProperty().bind(super.getRootPane().heightProperty().multiply(0.5));
        if (mapGraphicManager.geocodeAddress(address, mapWebView, geocodeResultLabel)) {
            uploadButton.setDisable(false);
        }
    }

    @FXML
    private void checkFields() {
        uploadButton.setDisable(Objects.equals(houseNameTextField.getText(), "") || Objects.equals(pictureUrlTextField.getText(), "") || Objects.equals(houseDescriptionTextField.getText(),"")||(mapGraphicManager.getLocation() == null));
    }

    @FXML
    private void openLinkInBrowser() {
        String url = "https://imgbb.com"; // Sostituisci con l'URL desiderato
        // Apri l'URL nel browser predefinito
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            try {
                java.net.URI uri = new java.net.URI(url);
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Cannot open user browser.");
            }
        }
    }
}
