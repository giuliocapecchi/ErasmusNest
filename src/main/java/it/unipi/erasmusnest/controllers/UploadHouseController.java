package it.unipi.erasmusnest.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.bson.Document;
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
import java.util.List;

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
    private TextField latitudeTextField;

    @FXML
    private TextField longitudeTextField;

    public Button updateButton;

    public UploadHouseController() {}

    private void initialize() {

    }

    @FXML
    void onUploadButtonClick(ActionEvent event) {
        // Ottieni i dati inseriti dall'utente
        String houseName = houseNameTextField.getText();
        String pictureUrl = pictureUrlTextField.getText();

        if(pictureUrl.isBlank() || pictureUrl.isEmpty())
        {
            pictureUrl = "https://www.altabadia.org/media/titelbilder/arrivo-coppa-del-mondo-by-freddy-planinschekjpg-3-1.jpg";
        }

        String accommodatesStr = accommodatesTextField.getText();
        String bathroomsStr = bathroomsTextField.getText();
        String bedsStr = bedsTextField.getText();
        String priceStr = priceTextField.getText();
        String latitudeStr = latitudeTextField.getText();
        String longitudeStr = longitudeTextField.getText();
        String neighborhood = neighborhoodTextField.getText();

        try {
            int accommodates = Integer.parseInt(accommodatesStr);
            int bathrooms = Integer.parseInt(bathroomsStr);
            int beds = Integer.parseInt(bedsStr);
            double price = Double.parseDouble(priceStr);
            double latitude = Double.parseDouble(latitudeStr);
            double longitude = Double.parseDouble(longitudeStr);

            String check = checkFields(houseName, accommodates, bathrooms, beds, price, latitude, longitude);
            // carica casa
            // prima bisogna recuperare le credenziali dell'utente dal db utenti
            String userEmail = getSession().getUser().getEmail();

            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("users");
            Document userDocument = collection.find(Filters.eq("email", userEmail)).first();

            if (userDocument != null) {
                // user_id di utenti è host_id di case
                int userId = userDocument.getInteger("user_id");
                // host_name e host_surname di case è first_name e last_name di utenti
                String host_name = userDocument.getString("first_name");
                String host_surname = userDocument.getString("last_name");
                // host_email di case è email di utenti
                String host_email = userDocument.getString("email");
                // Creare un nuovo documento per la casa
                Document houseDocument = new Document()
                        .append("house_id", 7) // Genera un nuovo ID per la casa
                        .append("house_name", houseName)
                        .append("picture_url", pictureUrl)
                        .append("host_id", userId)
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
        popOver.show(updateButton); // rootPane è il tuo StackPane principale
    }

    private String checkFields(String houseName, int accommodates, int bathrooms, int beds,
                               double price, double latitude, double longitude) {
        if (houseName.isBlank() || houseName.isEmpty())
            return "houseName";
        if (accommodates <= 0)
            return "accommodates";
        if (bathrooms <= 0)
            return "bathrooms";
        if (beds <= 0)
            return "beds";
        if (price <= 0)
            return "price";
        if (latitude == 0)
            return "latitude";
        if (longitude == 0)
            return "longitude";
        return "OK";
    }

    public void onBackButtonClick(ActionEvent actionEvent)
    {
        super.changeWindow("myProfile");
    }
}
