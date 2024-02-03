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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class UploadHouseController extends Controller {


    @FXML
    TextField houseNameTextField;
    @FXML
    VBox pictureUrlsVBox;
    @FXML
    Button morePictureButton;
    @FXML
    Button lessPictureButton;
    @FXML
    Spinner<Integer> inputAccommodates;
    @FXML
    Spinner<Integer> inputBathrooms;
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

    private ArrayList<TextField> pictureUrlsTextField;

    public UploadHouseController() {
    }

    @FXML
    private void initialize() {
        mapGraphicManager = new MapGraphicManager();
        uploadButton.setDisable(true);

        TextField pictureUrlTextField = new TextField();
        pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
        pictureUrlTextField.setPromptText("Insert picture URL");
        pictureUrlsVBox.getChildren().add(pictureUrlTextField);
        pictureUrlsTextField = new ArrayList<>();
        pictureUrlsTextField.add(pictureUrlTextField);
        pictureUrlsVBox.setSpacing(5);
        lessPictureButton.setDisable(true);

    }

    @FXML
    protected void onMorePictureButtonClick() {
        if(pictureUrlsTextField.size() <= 5) {
            TextField pictureUrlTextField = new TextField();
            pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
            pictureUrlTextField.setPromptText("Insert picture URL");
            pictureUrlsVBox.getChildren().add(pictureUrlTextField);
            pictureUrlsTextField.add(pictureUrlTextField);
        }
        if(pictureUrlsTextField.size() == 5) {
            morePictureButton.setDisable(true);
        }
        if(!pictureUrlsTextField.isEmpty()) {
            lessPictureButton.setDisable(false);
        }
    }

    @FXML
    protected void onLessPictureButtonClick(){
        if(!pictureUrlsTextField.isEmpty()) {
            pictureUrlsVBox.getChildren().remove(pictureUrlsTextField.get(pictureUrlsTextField.size() - 1));
            pictureUrlsTextField.remove(pictureUrlsTextField.size() - 1);
        }
        if(pictureUrlsTextField.size() < 5) {
            morePictureButton.setDisable(false);
        }
        if(pictureUrlsTextField.isEmpty()) {
            lessPictureButton.setDisable(true);
        }
    }

    @FXML
    void onUploadButtonClick() {
        String houseName = houseNameTextField.getText();
        // String [] pictureUrlArray = pictureUrlTextField.getText().split(";");
        //ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(pictureUrlTextField.getText().split(";")));
        ArrayList<String> pictureUrls = new ArrayList<>();
        for (TextField pictureUrlTextField : pictureUrlsTextField) {
            if(pictureUrlTextField.getText() != null && !pictureUrlTextField.getText().isEmpty() && !pictureUrlTextField.getText().isBlank())
                pictureUrls.add(pictureUrlTextField.getText());
        }
        Integer accommodates = inputAccommodates.getValue();
        Integer bathrooms = inputBathrooms.getValue();
        // String bathrooms = String.valueOf(inputBathrooms.getValue()); //TODO: I BAGNI DOVREBBERO ESSERE INTERI, NON STRINGHE
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
                    pictureUrls, 0.0, 0, bathrooms, user.getName(), user.getSurname());

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
        uploadButton.setDisable(Objects.equals(houseNameTextField.getText(), "") || wrongPictureUrls() || Objects.equals(houseDescriptionTextField.getText(),"")||(mapGraphicManager.getLocation() == null));
    }

    private boolean wrongPictureUrls() {
        for (TextField pictureUrlTextField : pictureUrlsTextField) {
            if(pictureUrlTextField.getText() == null || pictureUrlTextField.getText().isEmpty() || pictureUrlTextField.getText().isBlank())
                return true;
        }
        return false;
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
