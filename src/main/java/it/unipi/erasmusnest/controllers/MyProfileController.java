package it.unipi.erasmusnest.controllers;

import com.dlsc.gemsfx.EmailField;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox; // Import per il banner/pop-up
import org.controlsfx.control.PopOver; // Import per il banner/pop-up
import java.util.ArrayList;
import java.util.List;

public class MyProfileController extends Controller {

    public Button updateCitiesButton;
    @FXML
    public VBox apartmentsContainer;
    @FXML
    public Button backButton;
    public VBox adminContainer;
    @FXML
    private Label emailLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Button uploadHouseButton;
    @FXML
    private ImageView houseImageView;

    @FXML
    private EmailField emailField; // Aggiungi questo campo per l'indirizzo email
    @FXML
    private Button modifyButton; // Aggiungi questo campo per il bottone Modify/Update

    @FXML
    private PasswordField passwordField; // Campo password
    @FXML
    private Button modifyPasswordButton; // Bottone per la modifica della password

    @FXML
    private boolean isEditingEmail = false;

    @FXML
    private VBox passwordChangeBox; // Banner/pop-up per la modifica della password

    @FXML
    private VBox passwordChangeOuterBox;

    @FXML
    private PasswordField oldPasswordField; // Aggiunto campo per la vecchia password
    @FXML
    private PasswordField newPasswordField; // Campo per la nuova password
    @FXML
    private PasswordField confirmNewPasswordField; // Campo per la conferma della nuova password

    private boolean isEditingPassword = false; // Aggiunto per gestire la modifica della password

    @FXML
    private Label passwordErrorLabel; // Etichetta per visualizzare gli errori

    private String currentPassword;

    private boolean changedEmail = false;

    private boolean changePassword = false;

    @FXML
    private Label studyFieldLabel; // Etichetta per il campo Study Field

    @FXML
    private Label citiesOfInterestLabel; // Etichetta per il campo Cities of Interest

    @FXML
    private VBox citiesOfInterestBox; // Contenitore per le CheckBoxes delle città di interesse

    private List<String> selectedCitiesOfInterest; // Lista delle città di interesse selezionate

    @FXML
    private ComboBox<String> studyFieldComboBox;

    @FXML
    private ComboBox<String> citiesOfInterestComboBox;

    private String selectedStudyField;
    private String selectedCityOfInterest;

    private List<String> selectedCities = new ArrayList<>();

    private List<String> CITIES = new ArrayList<>();


    // Metodo per impostare il testo dell'etichetta dell'errore
    private void setPasswordErrorText(String errorMessage) {
        passwordErrorLabel.setText(errorMessage);
    }


    public MyProfileController() {}

    @FXML
    private void initialize() {

        CITIES = getNeo4jConnectionManager().getAllCities();
        User utente = new User();
        String userEmail = getSession().getUser().getEmail();

        emailField.setPromptText(userEmail);

        utente = getMongoConnectionManager().findUser(userEmail);
        passwordField.setText("******"); // Set password field to 6 asterisks
        List<String> userCities = utente.getPreferredCities();

        for (String city : CITIES) {
            CheckBox comboCity = new CheckBox();
            comboCity.setText(city);
            citiesOfInterestBox.getChildren().add(comboCity);
            // Check if the user has already selected this city
            if(userCities!=null && userCities.contains(city)){
                comboCity.setSelected(true);
                selectedCities.add(city);
            }
            comboCity.setOnAction(event -> {
                if (comboCity.isSelected()) {
                    selectedCities.add(city);
                } else {
                    selectedCities.remove(city);
                }
            });
        }
        // Update preferred cities
        updateCitiesButton.setOnAction(event -> {
            if (updateCitiesInDatabase(selectedCities)) {
                showConfirmationMessageCities("Preferred cities updated successfully!");
            } else {
                showConfirmationMessageCities("Error while updating preferred cities!");
            }
        });

        // Nascondi il banner/pop-up per la modifica della password all'inizio
        passwordChangeBox.setVisible(false);
        passwordChangeOuterBox.setVisible(false);

        // Inizializza il ComboBox per il campo "Study Field" (SF)

        studyFieldComboBox.getItems().addAll(getSession().getStudyFields());

        // Inizializza il ComboBox per il campo "Cities of Interest" (CoI)
        // citiesOfInterestComboBox.getItems().addAll("Florence", "Padua", "Bologna", "Amsterdam", "Other");

        // Estrai il campo "Study Field" (SF) e "Cities of Interest" (CoI) dal documento dell'utente
        // selectedStudyField = userDocument.getString("SF");
        selectedStudyField = utente.getStudyField();
        // selectedCityOfInterest = userDocument.getString("CoI");

        // Imposta i valori iniziali nei ComboBox
        studyFieldComboBox.setValue(selectedStudyField);
        // citiesOfInterestComboBox.setValue(selectedCityOfInterest);

        // Assumi che "house" possa essere un Document o una List<Document>
        //Object houseObject = userDocument.get("house");

        currentPassword = utente.getPassword();
        nameLabel.setText(utente.getName());
        lastNameLabel.setText(utente.getSurname());

        //apartmentsContainer = new VBox(10); // Assicurati che questo VBox sia definito nel FXML con fx:id="apartmentsContainer"

        if(utente.getHouses() != null ){
            if(!utente.getHouses().isEmpty()){
                System.out.println("casa: " + utente.getHouses().get(0).getName());

            /*
            for(Apartment a : utente.getHouses())
            {
                String houseName = a.getName();
                String pictureUrl = a.getImageURL();

                if(pictureUrl== null || pictureUrl.isEmpty())
                {
                    pictureUrl = "https://hips.hearstapps.com/hmg-prod/images/lago-di-montagna-cervinia-1628008263.jpg";
                }
                System.out.println("houseName: " + houseName);
                System.out.println("immmagine: " + pictureUrl);

                HBox apartmentBox = new HBox(10);
                apartmentBox.setAlignment(Pos.CENTER_LEFT);

                ImageView apartmentImage = new ImageView();
                apartmentImage.setFitHeight(100);
                apartmentImage.setFitWidth(100);
                apartmentImage.setPreserveRatio(true);

                Image houseImage = new Image(pictureUrl);
                //houseImageView.setImage(houseImage);
                apartmentImage.setImage(houseImage);
                // houseButton.setText(houseName);

                Label apartmentName = new Label(a.getName());
                apartmentName.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6200;");

                apartmentBox.getChildren().addAll(apartmentImage, apartmentName);
                apartmentsContainer.getChildren().add(apartmentBox);


                /*
                if (pictureUrl != null || !pictureUrl.isEmpty()) {
                    Image houseImage = new Image(pictureUrl);
                    houseImageView.setImage(houseImage);
                }
                else
                {
                    String defaultImageUrl = "https://www.liveinup.it/thumbs/luoghi/esperienze-in-montagna-copertina.webp";
                    Image defaultImage = new Image(defaultImageUrl);
                    houseImageView.setImage(defaultImage);
                }
                */
                // Recupera gli appartamenti dell'utente e li aggiunge al VBox apartmentsContainer
                for (Apartment apartment : utente.getHouses())
                {
                    //QUI TUTTO CORRETTO
                    HBox apartmentBox = new HBox(10);
                    apartmentBox.setAlignment(Pos.CENTER_LEFT);

                    ImageView apartmentImage = new ImageView();
                    apartmentImage.setFitHeight(100);
                    apartmentImage.setFitWidth(100);
                    apartmentImage.setPreserveRatio(true);
                    //String imageUrl = apartment.getImageURL() != null && !apartment.getImageURL().isEmpty() ? apartment.getImageURL() : "https://hips.hearstapps.com/hmg-prod/images/lago-di-montagna-cervinia-1628008263.jpg";

                    String imageUrl = apartment.getImageURL();
                    if(imageUrl== null || imageUrl.isEmpty())
                    {
                        //imageUrl = "/media/no_photo_available.png"; // Path inside the classpath
                        imageUrl = "https://www.altabadia.org/media/titelbilder/arrivo-coppa-del-mondo-by-freddy-planinschekjpg-3-1.jpg";
                    }

                    Image image = new Image(imageUrl, true);
                    apartmentImage.setImage(image);
                    Button apartmentButton = new Button();
                    apartmentButton.setText(apartment.getName());
                    //Now add the apartment image and button to the HBox
                    apartmentButton.setOnAction(event -> {
                        // Chiamare il metodo desiderato quando il bottone viene premuto
                        onApartmentView();
                    });
                    apartmentBox.getChildren().addAll(apartmentImage, apartmentButton);
                    apartmentsContainer.getChildren().add(apartmentBox); // This should add the apartment to the UI
                }
            }
        }
        else
        {
            apartmentsContainer.getChildren().add(new Label("No apartments available."));
        }
        // Parte riservata all'ADMIN
        // Se sono admin, allora mi appare un bottone per accedere alla vista analitiche
        if (utente.isAdmin(utente.getEmail()))
        {
            Button analyticsButton = new Button();
            analyticsButton.setText("Analytics");
            analyticsButton.setStyle("-fx-background-color: orange; -fx-border-color: red; -fx-border-width: 1px;");
            analyticsButton.setOnAction(event -> {
                // Handle the analytics button click
                System.out.println("Analytics button clicked");
                super.changeWindow("analytics");
            });

            adminContainer.getChildren().add(analyticsButton);
        }
        getSession().setUser(utente);
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
            emailField.setEmailAddress("");
            passwordChangeOuterBox.setVisible(false);
            isEditingPassword = false;
        }
        else
        {
            // Mostra il banner/pop-up per la modifica della password
            passwordChangeBox.setVisible(true);
            passwordChangeOuterBox.setVisible(true);
            isEditingPassword = true;
        }
    }

    @FXML
    private void onUpdatePasswordClick() {
        String newPassword = newPasswordField.getText();
        String confirmNewPassword = confirmNewPasswordField.getText();

        // Aggiungi la logica per verificare la vecchia password
        // String currentPassword = getCurrentUserPassword(); // Sostituisci con la tua logica per ottenere la password corrente

        if (newPassword.length() >= 4 && newPassword.length() <= 20 && newPassword.equals(confirmNewPassword))
        {
            if(getMongoConnectionManager().updatePassword(getSession().getUser().getEmail(), newPassword))
            {
                // Nascondi il banner/pop-up e mostra un messaggio di conferma
                //passwordChangeBox.setVisible(false);
                passwordChangeOuterBox.setVisible(false);
                showConfirmationMessagePassword("Password aggiornata con successo!");
            }
            else
            {
                // Mostra un messaggio di errore se la nuova password non è valida o la vecchia password non coincide
                setPasswordErrorText("Impossibile modificare la password. Verifica i dati inseriti.");
            }
        }
        else
        {
            // Mostra un messaggio di errore se la nuova password non è valida o la vecchia password non coincide
            setPasswordErrorText("Impossibile modificare la password. Verifica i dati inseriti.");
        }
    }

    // Metodo per mostrare un messaggio di conferma
    private void showConfirmationMessagePassword(String message) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 10px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        popOver.show(modifyPasswordButton);
    }

    private void showConfirmationMessageCities(String message) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 10px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        popOver.show(updateCitiesButton);
    }

    private void showConfirmationMessageSF(String message) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 10px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        popOver.show(studyFieldComboBox);
    }

    private void showConfirmationMessageEmail(String message) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 10px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        popOver.show(emailField);
    }

    // Metodo per mostrare un messaggio di errore
    private void showErrorMessage(String message) {
        // Implementa la visualizzazione dell'errore a tua scelta (ad esempio, un messaggio di errore sullo schermo)
    }

    @FXML
    private void onStudyFieldSelectionChanged(ActionEvent event)
    {
        String newStudyField = studyFieldComboBox.getValue();
        if (!newStudyField.equals(selectedStudyField))
        {
            if(getMongoConnectionManager().updateStudyField(getSession().getUser().getEmail(),newStudyField))
            {
                // Aggiorna la variabile con il nuovo valore
                selectedStudyField = newStudyField;
                showConfirmationMessageSF("Study Field aggiornato con successo!");
            }
            else
            {
                showConfirmationMessageSF("Errore nell'aggiornamento di study field!");
            }
        }
    }

    @FXML
    private void onUpdateButtonClick() {
        // Aggiorna le città di interesse dell'utente nel database con selectedCities
        updateCitiesInDatabase(selectedCities);
        if(updateCitiesInDatabase(selectedCities))
        {
            showConfirmationMessageCities("Città di interesse aggiornate con successo!");
        }
        else
        {
            showConfirmationMessageCities("Errore nell'aggiornamento delle città di interesse!");
        }
    }

    private boolean updateCitiesInDatabase(List<String> cities) {
        // Ottieni l'ID dell'utente corrente
        String mail = getSession().getUser().getEmail();
        User user = getMongoConnectionManager().findUser(mail);
        if(getMongoConnectionManager().updatePreferredCities(user.getEmail(), (ArrayList<String>) cities))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public void onUploadHouseButtonClick(ActionEvent actionEvent) {
        super.changeWindow("uploadHouse");
    }

    public void onApartmentView() {
        // Non funziona perche c'è un errore nel RatingGraphicManager
        getSession().setApartmentId(getSession().getUser().getHouses().get(0).getId());
        super.changeWindow("apartment");
    }

    public void onBack(ActionEvent actionEvent) {
        super.changeWindow("login");
    }

    @FXML
    private void checkEmailField(){
        modifyButton.setDisable(!isEmailFieldValid(emailField));
    }

    public void onModifyButtonClick(ActionEvent actionEvent) {
        if(!getMongoConnectionManager().availableEmail(emailField.getEmailAddress())){
            showConfirmationMessageEmail("Email already in use: change it.");
        }else{
            getMongoConnectionManager().updateEmail(getSession().getUser().getEmail(), emailField.getEmailAddress());
            getSession().getUser().setEmail(emailField.getEmailAddress());
            showConfirmationMessageEmail("Email updated successfully!");
            emailField.setPromptText(getSession().getUser().getEmail());
            emailField.setEmailAddress("");
        }
        modifyButton.setDisable(true);
    }
}
