package it.unipi.erasmusnest.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.bson.Document;
import org.controlsfx.control.PopOver;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UploadHouseController extends Controller {

    public VBox apartmentVBox;
    public VBox bathroomsVBox;
    public VBox priceVBox;
    public VBox neighborhoodVBox;
    @FXML
    private TextField houseNameTextField;

    @FXML
    private TextField pictureUrlTextField;
    public Spinner<Integer> inputAccommodates;
    public Spinner<Double> inputBathrooms;
    public Spinner<Double> inputPrice;
    public TextArea descriptionTextArea;

    @FXML
    private TextField accommodatesTextField;

    @FXML
    private TextField bathroomsTextField;

    @FXML
    private TextField bedsTextField;

    @FXML
    private TextField priceTextField;

    @FXML
    private TextField neighborhoodTextField;

    @FXML
    private TextField addressTextField;

    @FXML Button uploadButton;

    @FXML
    Button geocodeButton;

    @FXML
    Label geocodeResultLabel;

    @FXML
    VBox mapVBox;

    MapGraphicManager mapGraphicManager;

    public UploadHouseController() {}

    @FXML
    private void initialize() {
        mapGraphicManager = new MapGraphicManager();
        uploadButton.setDisable(true);

        inputAccommodates = new Spinner<>();
        inputBathrooms = new Spinner<>();
        inputPrice = new Spinner<>();

        SpinnerValueFactory<Integer> accomodatesValues = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,1);
        SpinnerValueFactory<Double> bathroomValues = new SpinnerValueFactory.DoubleSpinnerValueFactory(1,10,1,0.5);
        SpinnerValueFactory<Double> priceValues = new SpinnerValueFactory.DoubleSpinnerValueFactory(1,2000,1,0.5);

        inputAccommodates.setValueFactory(accomodatesValues);
        inputBathrooms.setValueFactory(bathroomValues);
        inputPrice.setValueFactory(priceValues);
        priceVBox.getChildren().add(inputPrice);
        apartmentVBox.getChildren().add(inputAccommodates);
        bathroomsVBox.getChildren().add(inputBathrooms);
        descriptionTextArea = new TextArea();
        descriptionTextArea.setPromptText("Insert neighbourhood description");
        descriptionTextArea.setWrapText(true);
        neighborhoodVBox.getChildren().add(descriptionTextArea);

    }

    @FXML
    void onUploadButtonClick(ActionEvent event)
    {
        String houseName = houseNameTextField.getText();
        String pictureUrl = pictureUrlTextField.getText();
        if(pictureUrl.isBlank() || pictureUrl.isEmpty())
            pictureUrl = "";
        Integer accommodates = inputAccommodates.getValue();
        Double bathrooms = inputBathrooms.getValue();
        String bathroomsStr = bathrooms==1 ? bathrooms +" bath" : bathrooms +" baths";
        Double price = inputPrice.getValue();
        String neighborhood = descriptionTextArea.getText();
        double latitude = mapGraphicManager.getLatitude();
        double longitude = mapGraphicManager.getLongitude();
        Point2D location = new Point2D(latitude, longitude);
        try
        {
            String userEmail = getSession().getUser().getEmail();
            User user = getMongoConnectionManager().findUser(userEmail);
            if (user != null)
            {
                //Create new apartment
                Apartment apartment = new Apartment(7L,houseName,neighborhood,location,price, accommodates, userEmail,
                        pictureUrl, 0.0,0,bathroomsStr,user.getName(),user.getSurname());

                // Call mongo to insert apartment
                if(getMongoConnectionManager().uploadApartment(apartment))
                {
                    alertDialog("House correctly uploaded.");
                }
                else
                {
                    alertDialog("Impossible to upload house");
                }
            }
        }
        catch (NumberFormatException e)
        {
            showConfirmationMessage("Error.");
        }
    }

    private void alertDialog(String s)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("");

        // Aggiungi un pulsante "OK"
        ButtonType okButton = new ButtonType("OK");
        alert.getButtonTypes().setAll(okButton);

        // Gestisci l'azione del pulsante "OK"
        alert.setOnCloseRequest(event -> {
            // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
            super.changeWindow("modifyApartment");
        });

        // Mostra la finestra di dialogo
        alert.showAndWait();
    }

    private void showConfirmationMessage(String message) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 15px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        // Mostra il messaggio di conferma
        popOver.show(uploadButton);
    }

    @FXML void onBackButtonClick()
    {
        super.changeWindow("myProfile");
    }

    @FXML void onGeocodeButtonClick(){
        String address = URLEncoder.encode(addressTextField.getText(), StandardCharsets.UTF_8);
        WebView mapWebView = new WebView();
        mapVBox.getChildren().clear();
        mapVBox.getChildren().add(mapWebView);
        mapWebView.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        mapWebView.maxHeightProperty().bind(super.getRootPane().heightProperty().multiply(0.5));
        if(mapGraphicManager.geocodeAddress(address, mapWebView, geocodeResultLabel)){
            uploadButton.setDisable(false);
        }
    }

    @FXML
    private void checkFields(){
        uploadButton.setDisable(Objects.equals(houseNameTextField.getText(), "") || Objects.equals(pictureUrlTextField.getText(), "") || Objects.equals(accommodatesTextField.getText(), "") || Objects.equals(bathroomsTextField.getText(), "") || Objects.equals(bedsTextField.getText(), "") || Objects.equals(priceTextField.getText(), "") || (mapGraphicManager.getLocation()==null));
    }
}
