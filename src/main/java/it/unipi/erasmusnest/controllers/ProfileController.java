package it.unipi.erasmusnest.controllers;

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

import java.util.List;

public class ProfileController extends Controller{

    @FXML
    private Label pageTitle;
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

    public ProfileController() {

    }


    @FXML
    private void initialize() {

        Session session = getSession();

        if (session.getOtherProfileMail() != null) {

            User utente = getMongoConnectionManager().findUser(session.getOtherProfileMail());

            System.out.println("utente: " + utente);
            pageTitle.setText("Profile of " + utente.getName() + " " + utente.getSurname());

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


    @FXML
    protected void backToBrowse(ActionEvent actionEvent)
    {
        super.changeWindow("apartments");
    }

    @FXML
    protected void backToHomepage(ActionEvent actionEvent) {
        super.changeWindow("homepage");
    }
}
