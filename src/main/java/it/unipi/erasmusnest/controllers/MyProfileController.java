package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox; // Import per il banner/pop-up
import org.controlsfx.control.PopOver; // Import per il banner/pop-up
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyProfileController extends Controller {

    @FXML
    VBox personalInfoVbox;
    public VBox cityVBox;
    public  TitledPane cityTidlePane;
    @FXML
    VBox apartmentsContainerVBox;
    @FXML
    Button updateCitiesButton;
    @FXML
    VBox apartmentsContainer;

    @FXML
    VBox adminContainer;

    @FXML
    Label emailLabel;
    @FXML
    Label nameLabel;
    @FXML
    Label lastNameLabel;
    @FXML
    Button uploadHouseButton;
    @FXML
    PasswordField passwordField; // Campo password
    @FXML
    Button modifyPasswordButton; // Bottone per la modifica della password
    @FXML
    VBox passwordChangeBox; // Banner/pop-up per la modifica della password
    @FXML
    private VBox passwordChangeOuterBox;
    @FXML
    PasswordField newPasswordField; // Campo per la nuova password
    @FXML
    PasswordField confirmNewPasswordField; // Campo per la conferma della nuova password
    boolean isEditingPassword = false; // Aggiunto per gestire la modifica della password
    @FXML
    Label passwordErrorLabel; // Etichetta per visualizzare gli errori
    @FXML
    private VBox citiesOfInterestBox; // Contenitore per le CheckBoxes delle città di interesse
    @FXML
    private ComboBox<String> studyFieldComboBox;
    @FXML
    private ComboBox<String> citiesOfInterestComboBox;

    private String selectedStudyField;
    private List<String> selectedCities = new ArrayList<>();
    private List<String> CITIES = new ArrayList<>();


    // Metodo per impostare il testo dell'etichetta dell'errore
    private void setPasswordErrorText() {
        passwordErrorLabel.setText("The passwords don't match or the new password is invalid.");
    }

    public MyProfileController() {}

    @FXML
    private void initialize() {

        personalInfoVbox.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        apartmentsContainerVBox.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        passwordChangeOuterBox.prefWidthProperty().bind(super.getRootPane().widthProperty());

        CITIES = getSession().getCities();
        String userEmail = getSession().getUser().getEmail();

        User utente = getMongoConnectionManager().findUser(userEmail);
        passwordField.setText("******"); // Set password field to 6 asterisks
        List<String> userCities = utente.getPreferredCities();
        cityTidlePane = createTitledPane(userCities);
        cityVBox.getChildren().add(cityTidlePane);

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
        passwordChangeOuterBox.getChildren().clear();

        // Inizializza il ComboBox per il campo "Study Field" (SF)

        studyFieldComboBox.getItems().addAll(getSession().getStudyFields());
        // Inizializza il ComboBox per il campo "Cities of Interest" (CoI)
        // Estrai il campo "Study Field" (SF) e "Cities of Interest" (CoI) dal documento dell'utente
        // selectedStudyField = userDocument.getString("SF");
        selectedStudyField = utente.getStudyField();
        // selectedCityOfInterest = userDocument.getString("CoI");

        // Imposta i valori iniziali nei ComboBox
        studyFieldComboBox.setValue(selectedStudyField);
        // citiesOfInterestComboBox.setValue(selectedCityOfInterest);

        // Assumi che "house" possa essere un Document o una List<Document>
        //Object houseObject = userDocument.get("house");

        nameLabel.setText(utente.getName());
        lastNameLabel.setText(utente.getSurname());
        emailLabel.setText(utente.getEmail());

        //apartmentsContainer = new VBox(10); // Assicurati che questo VBox sia definito nel FXML con fx:id="apartmentsContainer"

            if(utente.getHouses() != null  && !utente.getHouses().isEmpty())
            {
                System.out.println("casa: " + utente.getHouses().get(0).getName());

                // Recupera gli appartamenti dell'utente e li aggiunge al VBox apartmentsContainer
                for (Apartment apartment : utente.getHouses())
                {
                    //QUI TUTTO CORRETTO
                    HBox apartmentBox = new HBox(10);
                    apartmentBox.setAlignment(Pos.CENTER_LEFT);

                    ImageView apartmentImage = new ImageView();
                    apartmentImage.setPreserveRatio(true);

                    String imageUrl = apartment.getImageURL();
                    System.out.println("\n\n\nimage url: " + imageUrl);
                    if(imageUrl.isEmpty())
                    {
                        String imagePath = "/media/no_photo_available.png"; // Path inside the classpath
                        apartmentImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
                    } else {
                        Image image = new Image(imageUrl);
                        apartmentImage.setImage(image);
                    }

                    apartmentImage.setSmooth(true);
                    apartmentImage.fitWidthProperty().bind(apartmentBox.widthProperty().multiply(0.4));
                    Button apartmentButton = new Button();
                    apartmentButton.setText("Modify");
                    apartmentButton.setId(apartment.getId().toString());
                    //Now add the apartment image and button to the HBox
                    apartmentButton.setOnAction(event -> {
                        // Chiamare il metodo desiderato quando il bottone viene premuto
                        onApartmentView(apartmentButton.getId());
                    });
                    Button viewButton = new Button(apartment.getName());
                    viewButton.setOnAction(event -> {
                        onChangeView(apartmentButton.getId());
                    });

                    apartmentBox.getChildren().addAll(apartmentImage, viewButton, apartmentButton);
                    apartmentsContainer.getChildren().add(apartmentBox); // This should add the apartment to the UI
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

    private void onChangeView(String apartmentId)
    {
        getSession().setApartmentId(Long.parseLong(apartmentId));
        super.changeWindow("apartment");
    }

    public TitledPane createTitledPane(List<String> userCities) {
        GridPane gridPane = new GridPane();
        List<String> cities = getNeo4jConnectionManager().getAllCities();
        for(String city : cities)
        {
            CheckBox mainCheckBox = new CheckBox(city);
            mainCheckBox.setText(city);
            mainCheckBox.setOnAction(e -> {
                if (mainCheckBox.isSelected()) {
                    selectedCities.add(mainCheckBox.getText());
                } else {
                    selectedCities.remove(mainCheckBox.getText());
                }
            });
            if(userCities!=null && userCities.contains(city)){
                mainCheckBox.setSelected(true);
                selectedCities.add(city);
            }
            gridPane.add(mainCheckBox, 0, cities.indexOf(city));
        }

        TitledPane titledPane = new TitledPane("Cities interested in", gridPane);
        // titledPane.setStyle("-fx-pref-width: 10px;"); // Imposta la larghezza preferita inline
        // titledPane.setPadding(new Insets(2, 2, 2, 2));

        titledPane.setExpanded(false);

        return titledPane;
    }

    @FXML
    protected void logoutButtonClick(){
        getSession().reset();
        super.changeWindow("login");
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
            passwordChangeOuterBox.setVisible(false);
            passwordChangeOuterBox.getChildren().clear();
            isEditingPassword = false;
        }
        else
        {
            // Mostra il banner/pop-up per la modifica della password
            passwordChangeBox.setVisible(true);
            passwordChangeOuterBox.setVisible(true);
            if(!passwordChangeOuterBox.getChildren().contains(passwordChangeBox))
            {
                passwordChangeOuterBox.getChildren().add(passwordChangeBox);
            }
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
            if(getRedisConnectionManager().updateUserPassword(getSession().getUser().getEmail(), newPassword)){
                System.out.println("Password aggiornata su Redis");
                passwordChangeOuterBox.setVisible(false);
                showConfirmationMessagePassword("Password aggiornata con successo!");
            }else{ // aggiorno su Mongo e basta / TODO : eventual consistency da gestire qui! La password rimane solo su Redis per ora
                System.out.println("Password non aggiornata su Redis perchè non trovata la chiave. La aggiorno su MongoDB");
                // per come è ora il codice, su mongo la password viene aggiornata a priori (errore???)
                if(getMongoConnectionManager().updatePassword(getSession().getUser().getEmail(), newPassword)){
                    // Nascondi il banner/pop-up e mostra un messaggio di conferma
                    //passwordChangeBox.setVisible(false);
                    passwordChangeOuterBox.setVisible(false);
                    showConfirmationMessagePassword("Password aggiornata con successo!");
                }else{
                    // Mostra un messaggio di errore se la nuova password non è valida o la vecchia password non coincide
                    setPasswordErrorText();
                }
            }
        }
        else
        {
            // Mostra un messaggio di errore se la nuova password non è valida o la vecchia password non coincide
            setPasswordErrorText();
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
        } else {
            showConfirmationMessageCities("Errore nell'aggiornamento delle città di interesse!");
        }
    }

    private boolean updateCitiesInDatabase(List<String> cities) {
        // Ottieni l'ID dell'utente corrente
        String mail = getSession().getUser().getEmail();
        User user = getMongoConnectionManager().findUser(mail);
        return getMongoConnectionManager().updatePreferredCities(user.getEmail(), cities);
    }


    public void onUploadHouseButtonClick(ActionEvent actionEvent) {
        super.changeWindow("uploadHouse");
    }

    public void onApartmentView(String apartmentId) {
        getSession().setApartmentId(Long.parseLong(apartmentId));
        System.out.println("\n\n\nApartment ID: " + apartmentId);
        super.changeWindow("modifyApartment");
    }

    public void onBack(ActionEvent actionEvent) {
        super.changeWindow("login");
    }

    @FXML
    protected void onReservationsButtonClick() {
        super.changeWindow("myreservations");
    }

    public void onFollowersButtonClick(ActionEvent actionEvent) {
        super.changeWindow("followers");
    }
}
