package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.Session;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class ProfileController extends Controller{

    @FXML
    ToolBar toolBar;
    @FXML
    Button backToSearch;
    @FXML
    private Label pageTitle;
    @FXML
    private Label emailLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label surnameLabel;
    @FXML
    private Label studyFieldLabel;
    @FXML
    private Label citiesLabel;
    @FXML
    public VBox housesContainer;

    public ProfileController() {

    }


    @FXML
    private void initialize() {

        Session session = getSession();

        if (session.getOtherProfileMail() != null) {

            User utente = getMongoConnectionManager().findUser(session.getOtherProfileMail());

            pageTitle.setText("Profile of " + utente.getName() + " " + utente.getSurname());

            emailLabel.setText(utente.getEmail());
            nameLabel.setText(utente.getName());
            surnameLabel.setText(utente.getSurname());

            if(utente.getStudyField().isEmpty() || utente.getStudyField().isBlank())
                studyFieldLabel.setText("not specified");
            else
                studyFieldLabel.setText(utente.getStudyField());

            List<String> userCities = utente.getPreferredCities();

            String cities = "";
            if (userCities != null && !userCities.isEmpty()) {
                for (String city : userCities) {
                    cities += city;
                    if (userCities.indexOf(city) != userCities.size() - 1) {
                        cities += ", ";
                    }
                }
            } else {
                cities = "not specified";
            }
            citiesLabel.setText(cities);

            //Adesso si deve popolare la vbox per le case dell'utente
            List<Apartment> userHouses = utente.getHouses();
            System.out.println("userHouses: " + userHouses);
            // Recupera gli appartamenti dell'utente e li aggiunge al VBox apartmentsContainer
            if (userHouses != null && !userHouses.isEmpty()) {
                for (Apartment apartment : userHouses) {
                    //QUI TUTTO CORRETTO
                    // HBox apartmentBox = new HBox(10);
                    HBox apartmentBox = new HBox();

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
                    if(Objects.equals(getSession().getNextWindowName(), "homepage"))
                        toolBar.getItems().remove(backToSearch);
                }
            } else {
                housesContainer.getChildren().clear();
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

    @FXML
    protected void seeSuggested(ActionEvent actionEvent)
    {
        String otherEmail = getSession().getOtherProfileMail();
        String email = getSession().getUser().getEmail();
        boolean suggestedUsers =  getNeo4jConnectionManager().seeSuggested(email, otherEmail);
    }
}
