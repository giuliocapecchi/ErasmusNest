package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.Session;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProfileController extends Controller{

    @FXML
    public Label emailLabel;
    @FXML
    public Label nameLabel;
    @FXML
    public Label surnameLabel;
    @FXML
    public Label studyFieldLabel;
    @FXML
    public VBox citiesOfInterestBox;
    @FXML
    public VBox housesContainer;
    @FXML
    public HBox apartmentBox;
    @FXML
    public Button backBrowseButton;

    public ProfileController() {

    }


    @FXML
    private void initialize() {

        Session session = getSession();
        if (session == null)
            System.out.println("Session is null\n\n\n");
        else
            System.out.println("Session is not null\n\n\n");
        System.out.println("EMAIL DELL'UTENTE CHE VUOI VEDERE:" + session.getOtherProfileMail() + "\n\n\n");
        if (session.getOtherProfileMail() != null) {
            // MongoConnectionManager per recuperare tutti i dati necessari sull'utente
            User utente = getMongoConnectionManager().findUser(session.getOtherProfileMail());
            //if utente == null ...
            // Setto i dati dell'utente nella pagina
            emailLabel.setText(utente.getEmail());
            nameLabel.setText(utente.getName());
            surnameLabel.setText(utente.getSurname());
            studyFieldLabel.setText(utente.getStudyField());

            List<String> userCities = utente.getPreferredCities();

            if (userCities != null && !userCities.isEmpty())
            {
                if (userCities.size() == 1)
                {
                    Label cityLabel = new Label(userCities.get(0));
                    citiesOfInterestBox.getChildren().add(cityLabel);
                }
                else
                {
                    for (String city : userCities)
                    {
                        Label cityLabel = new Label(city);
                        citiesOfInterestBox.getChildren().add(cityLabel);
                    }
                }
            }
            else
            {
                // Se non ci sono città di interesse, mostra un messaggio
                Label noCitiesLabel = new Label("Nessuna città di interesse");
                citiesOfInterestBox.getChildren().add(noCitiesLabel);

                System.out.println("userCities is null\n\n\n");
            }
            //Adesso si deve popolare la vbox per le case dell'utente
            List<Apartment> userHouses = utente.getHouses();
            System.out.println("userHouses: " + userHouses);
            // Recupera gli appartamenti dell'utente e li aggiunge al VBox apartmentsContainer
            if (userHouses != null && !userHouses.isEmpty()) {
                for (Apartment apartment : userHouses) {
                    //QUI TUTTO CORRETTO
                    // HBox apartmentBox = new HBox(10);
                    apartmentBox = new HBox(10);
                    apartmentBox.setAlignment(Pos.CENTER_LEFT);

                    ImageView apartmentImage = new ImageView();
                    apartmentImage.setFitHeight(100);
                    apartmentImage.setFitWidth(100);
                    apartmentImage.setPreserveRatio(true);
                    // apartmentImage.setPreserveRatio(false);
                    //String imageUrl = apartment.getImageURL() != null && !apartment.getImageURL().isEmpty() ? apartment.getImageURL() : "https://hips.hearstapps.com/hmg-prod/images/lago-di-montagna-cervinia-1628008263.jpg";

                    String imageUrl = apartment.getImageURL();
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = "https://hips.hearstapps.com/hmg-prod/images/lago-di-montagna-cervinia-1628008263.jpg";
                    }

                    System.out.println("houseName: " + apartment.getName());
                    System.out.println("immmagine: " + imageUrl);
                    Image image = new Image(imageUrl, true);
                    apartmentImage.setImage(image);

                    Button apartmentButton = new Button();
                    apartmentButton.setText(apartment.getName());
                    System.out.println("Testo bottone:" + apartmentButton.getText());
                    apartmentButton.setOnAction(event -> {
                        // Handle the apartment button click
                        System.out.println("Apartment button clicked");
                        // Setto l'id dell'appartamento nella sessione
                        getSession().setApartmentId(apartment.getId());
                        super.changeWindow("apartment");
                    });

                    //Now add the apartment image and button to the HBox
                    apartmentBox.getChildren().addAll(apartmentImage, apartmentButton);
                    housesContainer.getChildren().add(apartmentBox); // This should add the apartment to the UI
                }
            }
        }
    }


    /*
    @FXML
    private void onModifyButtonClick()
    {
        if (isEditingEmail)
        {
            // Qui gestisci l'azione di Update per l'email
            changedEmail = true;
            User utente = getMongoConnectionManager().findUser(getSession().getUser().getEmail());
            updateEmail(utente); // Chiama la funzione per aggiornare l'email
            modifyButton.setText("Modify");
            emailField.setEditable(false);
            isEditingEmail = false;
        } else {
            // Qui gestisci l'azione di Modify per l'email
            modifyButton.setText("Update");
            emailField.setEditable(true);
            isEditingEmail = true;
        }
    }

    @FXML
    private void updateEmail(User utente)
    {
        if (changedEmail)
        {
            String newEmail = emailField.getText();
            String oldEmail = utente.getEmail();

            if (!newEmail.equals(oldEmail))
            {
                if(getMongoConnectionManager().updateEmail(oldEmail, newEmail))
                {
                    getSession().getUser().setEmail(newEmail);
                    showConfirmationMessage("Email aggiornata con successo!");
                }
                else
                {
                    showErrorMessage("Impossibile aggiornare l'email. Verifica i dati inseriti.");
                }
            }
            changedEmail = false;
        }
    }


    @FXML
    private void onBackButtonClick() {
        passwordChangeOuterBox.setVisible(false);
        // Puoi anche reimpostare i campi della password se lo desideri, ad esempio:
        //  oldPasswordField.clear();
        newPasswordField.clear();
        confirmNewPasswordField.clear();
    }

    @FXML
    private void onModifyPasswordButtonClick() {
        // Mostra il banner/pop-up per la modifica della password
        if (isEditingPassword) {
            // Chiudi la modifica della password se il banner è già aperto
            //passwordChangeBox.setVisible(false);
            passwordChangeOuterBox.setVisible(false);
            isEditingPassword = false;
        }
        else
        {
            System.out.println("Session.getOtherProfileMail() is null\n\n\n");
        }
    }

    @FXML
    private void onUpdateButtonClick() {
        // Aggiorna le città di interesse dell'utente nel database con selectedCities
        updateCitiesInDatabase(selectedCities);
        if(updateCitiesInDatabase(selectedCities))
        {
            showConfirmationMessage("Città di interesse aggiornate con successo!");
        }
        else
        {
            showConfirmationMessage("Errore nell'aggiornamento delle città di interesse!");
        }
    }
    */

    public void backToBrowse(ActionEvent actionEvent)
    {
        super.changeWindow("apartments");
    }
}
