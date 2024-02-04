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
import org.controlsfx.control.PopOver;

import java.util.ArrayList;
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
    @FXML
    private VBox suggestedOuterBox;
    @FXML
    private VBox suggestedVBox;
    @FXML
    private Button followButton;

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

            followButton.setDisable(!getSession().isLogged());
            suggestedOuterBox.prefWidthProperty().bind(super.getRootPane().widthProperty());
            suggestedOuterBox.setVisible(false);

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

                    String imageUrl = apartment.getImageURL().get(0);
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = "https://hips.hearstapps.com/hmg-prod/images/lago-di-montagna-cervinia-1628008263.jpg";
                    }

                    System.out.println("houseName: " + apartment.getName());
                    System.out.println("immmagine: " + imageUrl);
                    Image image = new Image(imageUrl, true);
                    apartmentImage.setImage(image);

                    Button apartmentButton = new Button();
                    apartmentButton.setText(apartment.getName());
                    apartmentButton.setOnAction(event -> {
                        // Handle the apartment button click
                        System.out.println("Apartment button clicked");
                        // Setto l'id dell'appartamento nella sessione
                        getSession().setApartmentId(apartment.getId());
                        super.changeWindow("profile","apartment");
                    });

                    //Now add the apartment image and button to the HBox
                    apartmentBox.getChildren().addAll(apartmentImage, apartmentButton);
                    housesContainer.getChildren().add(apartmentBox); // This should add the apartment to the UI
                    if(Objects.equals(getPreviousWindowName(), "homepage")
                            || Objects.equals(getPreviousWindowName(), "profile")) {
                        toolBar.getItems().remove(backToSearch);
                    }
                }
            } else {
                housesContainer.getChildren().clear();
            }
        }
    }


    @FXML
    protected void backToBrowse() {
        super.changeWindow("profile","apartment");
    }

    @FXML
    protected void backToHomepage() {
        super.changeWindow("profile","homepage");
    }

    @FXML
    protected void seeSuggested(ActionEvent actionEvent)
    {
        String otherEmail = getSession().getOtherProfileMail();
        String email = getSession().getUser().getEmail();
        getNeo4jConnectionManager().addFollow(email, otherEmail);
        List<String> suggestedUsers =  getNeo4jConnectionManager().seeSuggestedUsers(email, otherEmail);
        // Show pop up with suggested users
        suggestedVBox.setVisible(true);
        suggestedOuterBox.setVisible(true);
        if (suggestedUsers != null && !suggestedUsers.isEmpty()) {
            for (String suggestedUser : suggestedUsers) {
                Button suggestedUserButton = new Button(suggestedUser);
                suggestedVBox.getChildren().add(suggestedUserButton);
                suggestedUserButton.setOnAction(event -> {
                    getSession().setOtherProfileMail(suggestedUser);
                    super.refreshWindow();
                });
            }
            showConfirmationMessage("Started follow", followButton);
            followButton.setDisable(true);
        } else {
            suggestedOuterBox.getChildren().addAll(new Label("No suggested users"));
            followButton.setDisable(true);
        }
    }

    private void showConfirmationMessage(String message, Button likeButton) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 10px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        popOver.show(likeButton);
    }

    public void showReviews() {
        super.changeWindow("profile","reviews");
    }
}
