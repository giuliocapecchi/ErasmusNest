package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.dbconnectors.ConsistencyManager;
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
import java.util.Objects;

public class UploadHouseController extends Controller {


    @FXML
    Label houseDescriptionMaxLengthReached;
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
    Spinner<Integer> inputPrice;
    @FXML
    TextArea houseDescriptionTextArea;
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
        houseDescriptionMaxLengthReached.setVisible(false);
        TextField pictureUrlTextField = new TextField();
        pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
        pictureUrlTextField.setPromptText("Insert picture URL");
        pictureUrlsVBox.getChildren().add(pictureUrlTextField);
        pictureUrlsTextField = new ArrayList<>();
        pictureUrlsTextField.add(pictureUrlTextField);
        pictureUrlsVBox.setSpacing(5);
        lessPictureButton.setDisable(true);
        houseDescriptionTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1000) {
                houseDescriptionTextArea.setText(oldValue); // Revert al valore precedente se supera 1000 caratteri
                houseDescriptionTextArea.setScrollTop(Double.MAX_VALUE); // rimette lo scroll in fondo
                houseDescriptionMaxLengthReached.setVisible(true);
            }else{
                houseDescriptionMaxLengthReached.setVisible(false);
            }
        });

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
        checkFields();
    }

    @FXML
    void onUploadButtonClick() {
        String houseName = houseNameTextField.getText();
        ArrayList<String> pictureUrls = new ArrayList<>();
        for (TextField pictureUrlTextField : pictureUrlsTextField) {
            if(pictureUrlTextField.getText() != null && !pictureUrlTextField.getText().isEmpty() && !pictureUrlTextField.getText().isBlank())
                pictureUrls.add(pictureUrlTextField.getText());
        }
        Integer accommodates = inputAccommodates.getValue();
        Integer bathrooms = inputBathrooms.getValue();
        Integer price = inputPrice.getValue();
        String houseDescription = houseDescriptionTextArea.getText();
        double latitude = mapGraphicManager.getLatitude();
        double longitude = mapGraphicManager.getLongitude();
        String city = mapGraphicManager.getCity();
        Point2D location = new Point2D(latitude, longitude);
        String userEmail = getSession().getUser().getEmail();
        User user = getMongoConnectionManager().findUser(userEmail);
        if (user != null) {
            Apartment apartment = new Apartment(houseName, houseDescription, location, city, price, accommodates, userEmail,
                    pictureUrls, 0.0, 0, bathrooms, user.getName(), user.getSurname());

            // Call Mongo to insert apartment
            apartment = getMongoConnectionManager().uploadApartment(apartment);
            if (apartment.getId() != null) { // se è andato a buon fine, dentro l'ID ci sarà l'object id assegnato da MongoDB

                // TODO JACOPO: qui bisogna fare il caricamento su Neo4j
                new ConsistencyManager(getMongoConnectionManager(), getNeo4jConnectionManager()).addApartmentOnNeo4J(apartment);

                new AlertDialogGraphicManager("House uploaded correctly","House correctly uploaded.","You will be redirected to your profile","information").show();
                super.changeWindow("myProfile");
                return;

            }else{
                System.out.println("Caricamento casa su MongoDB FALLITO");}
        }else{
            System.out.println("impossibile caricare casa: l'utente non esiste su Mongo.");
        }
        // in tutti i casi negativi mostro un alert di errore all'utente
        new AlertDialogGraphicManager("House NOT uploaded correctly").show();
    }

    @FXML
    void onBackButtonClick() {
        super.changeWindow("myProfile");
    }

    @FXML
    void onGeocodeButtonClick() {

        if(!cityIsValid(addressTextField.getText())){
            mapVBox.getChildren().clear();
            mapGraphicManager.setLocation(null);
            geocodeResultLabel.setText("Our service is not available in this city right now.");
            checkFields();
            return;
        }

        String address = URLEncoder.encode(addressTextField.getText(), StandardCharsets.UTF_8);
        WebView mapWebView = new WebView();
        mapVBox.getChildren().clear();
        mapVBox.getChildren().add(mapWebView);
        mapWebView.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        mapWebView.maxHeightProperty().bind(super.getRootPane().heightProperty().multiply(0.5));
        if (!mapGraphicManager.geocodeAddress(address, mapWebView, geocodeResultLabel)) {
            mapVBox.getChildren().clear();
            mapGraphicManager.setLocation(null);
        }
        checkFields();
    }


    /**
    * Verifica se la città è valida, ovvero se è presente nell'elenco di città che serviamo.
     * @param address indirizzo da verificare
     * @return true se la città è valida, false altrimenti
     *  Inoltre, setta dentro "mapGraphicManager" la città, se trovata
     */
    private boolean cityIsValid(String address) {
        // verifico se la città è nell'elenco di città che serviamo, altrimenti la scarto
        String[] splittedStrings = address.split(",");
        for (String str : splittedStrings) {
            str = str.trim().toLowerCase();
            for(String city : getSession().getCities()){
                if (str.equals(city.toLowerCase())) {
                    mapGraphicManager.setCity(city);
                    return true;
                }
            }
        }
        System.out.println("City not valid");
        return false;
    }

    @FXML
    private void checkFields() {
        uploadButton.setDisable(Objects.equals(houseNameTextField.getText(), "") || wrongPictureUrls() || (mapGraphicManager.getLocation() == null) );
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
        String url = "https://imgbb.com";
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
