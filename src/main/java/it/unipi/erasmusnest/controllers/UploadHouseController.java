package it.unipi.erasmusnest.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

    @FXML
    private TextField houseNameTextField;

    @FXML
    private TextField pictureUrlTextField;

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
    }

    @FXML
    void onUploadButtonClick(ActionEvent event) {
        String houseName = houseNameTextField.getText();
        String pictureUrl = pictureUrlTextField.getText();
        if(pictureUrl.isBlank() || pictureUrl.isEmpty()){ //TODO : ABBIAMO IL PLACEHOLDER PER QUANDO UN'IMMAGINE NON è DISPONIBILE, si trova dentro media
            pictureUrl = "https://www.altabadia.org/media/titelbilder/arrivo-coppa-del-mondo-by-freddy-planinschekjpg-3-1.jpg";
        }

        String accommodatesStr = accommodatesTextField.getText();
        String bathroomsStr = bathroomsTextField.getText();
        String bedsStr = bedsTextField.getText();
        String priceStr = priceTextField.getText();
        String neighborhood = neighborhoodTextField.getText();
        double latitude = mapGraphicManager.getLatitude();
        double longitude = mapGraphicManager.getLongitude();

        try {
            int accommodates = Integer.parseInt(accommodatesStr);
            int beds = Integer.parseInt(bedsStr);

            // prima bisogna recuperare le credenziali dell'utente dal db utenti
            String userEmail = getSession().getUser().getEmail();


            // TODO: ANDRE C'è DA USARE STI CONNETTORI OVUNQUE NON FARE STE  MINCHIA DI CHIAMATE DIRETTE AL DB -> User user = getMongoConnectionManager().findUser(userEmail);
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("users");
            Document userDocument = collection.find(Filters.eq("email", userEmail)).first();

            User user = getMongoConnectionManager().findUser(userEmail);


            if (user != null) {
                String host_email = user.getEmail();
                String host_name = user.getName();
                String host_surname = user.getSurname();
                // host_email di case è email di utenti

                // Creare un nuovo documento per la casa
                Document houseDocument = new Document()
                        .append("house_id", 7) // Genera un nuovo ID per la casa
                        .append("house_name", houseName)
                        .append("picture_url", pictureUrl)
                      //  .append("host_id", userId) -> NON LO ABBIAMO
                        .append("host_name", host_name)
                        .append("host_surname", host_surname)
                        .append("host_email", host_email)
                        .append("accommodates", accommodates)
                        .append("bathrooms_text", bathroomsStr)
                        .append("beds", beds)
                        .append("price", priceStr)
                        .append("neighbourhood", neighborhood)
                        .append("position", latitude + "," + longitude)
                        .append("email", host_email);


                //TODO: (PARTE2) ANDRE C'è DA USARE STI CONNETTORI OVUNQUE NON FARE STE  MINCHIA DI CHIAMATE DIRETTE AL DB

                // Inserisci il nuovo documento nel database
                MongoCollection<Document> housesCollection = database.getCollection("apartments");
                housesCollection.insertOne(houseDocument);

                showConfirmationMessage("CASA caricata con successo");

                // Codice per aggiungere la casa alla collection utenti
                // Prima di inserire un nuovo documento casa, verifica se l'utente ha già una casa associata
                MongoCollection<Document> usersCollection = database.getCollection("users");
                Document existingUser = usersCollection.find(Filters.eq("email", userEmail)).first();

                if (existingUser != null) {
                    // Crea un nuovo documento casa
                    houseDocument = new Document()
                            .append("house_id", 7) // Genera un nuovo ID per la casa
                            .append("name", houseName)
                            .append("picture_url", pictureUrl)
                            .append("review_scores_rating", 4.6);

                    // Verifica se l'utente ha già un campo "house" nel documento
                    if (existingUser.containsKey("house")) {
                        // Ottieni la lista delle case dell'utente
                        List<Document> userHouses;
                        if (existingUser.get("house") instanceof Document) {
                            userHouses = new ArrayList<>();
                            userHouses.add((Document) existingUser.get("house")); // Aggiungi il documento esistente
                        } else {
                            userHouses = (List<Document>) existingUser.get("house");
                        }
                        // Aggiungi il nuovo documento casa alla lista
                        userHouses.add(houseDocument);
                        existingUser.put("house", userHouses); // Sostituisci il campo "house" con la lista di documenti
                    } else {
                        // Se l'utente non ha ancora case, crea un nuovo campo "house" con il documento casa
                        List<Document> userHouses = new ArrayList<>();
                        userHouses.add(houseDocument);
                        existingUser.put("house", userHouses);
                    }

                    //todo: PARTE 3  connettori

                    // Aggiorna il documento utente con il nuovo documento casa
                    usersCollection.replaceOne(Filters.eq("email", userEmail), existingUser);

                    showConfirmationMessage("CASA caricata con successo");
                } else {
                    showConfirmationMessage("L'utente non esiste.");
                }
            }
        }
        catch (NumberFormatException e)
        {
            showConfirmationMessage("Errore di parsing: Assicurati di inserire valori numerici validi.");
        }
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
