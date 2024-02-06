package it.unipi.erasmusnest.controllers;

import com.dlsc.gemsfx.EmailField;
import it.unipi.erasmusnest.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.List;

public class SignupController extends Controller{

    @FXML
    private GridPane gridPane;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private EmailField emailField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    TextFlow errorTextFlow;

    @FXML
    private Button signupButton;

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<String> studiesComboBox;

    private final ArrayList<String> selectedCities = new ArrayList<>();

    @FXML
    private VBox cityVBox;


    @FXML
    private void initialize() {
        if(getSession().getCities()==null || getSession().getCities().isEmpty()){
            getSession().setCities(getNeo4jConnectionManager().getAllCities());
        }
        signupButton.setDisable(true);
        emailField.emailAddressProperty().addListener((observable, oldValue, newValue) -> checkFields());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        surnameField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        studiesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> checkFields());

        TitledPane preferredCitiesPane = createTitledPane();

        cityVBox.getChildren().add(preferredCitiesPane);

        for(String studyField : getSession().getStudyFields()){
            studiesComboBox.getItems().add(studyField);
        }

        gridPane.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        passwordField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        confirmPasswordField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        emailField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        signupButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        backButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        errorTextFlow.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        studiesComboBox.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        preferredCitiesPane.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        preferredCitiesPane.setPadding(new Insets(10, 10, 10, 10));
    }

    public TitledPane createTitledPane() {
        GridPane gridPane = new GridPane();
        List<String> cities = getSession().getCities();

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
            gridPane.add(mainCheckBox, 0, cities.indexOf(city));
        }

        TitledPane titledPane = new TitledPane("Cities interested in", gridPane);
        titledPane.setExpanded(false);

        return titledPane;
    }


    @FXML
    protected void checkFields() {
        // check username disponibile si fa live o @btn pressed?
        signupButton.setDisable(!isTextFieldValid(passwordField) || !isEmailFieldValid(emailField)
                || !isTextFieldValid(nameField) || !isTextFieldValid(surnameField)
                || studiesComboBox.getValue() == null || !passwordField.getText().equals(confirmPasswordField.getText()));

        if(!isEmailFieldValid(emailField)) {
            showErrorMessage("Invalid email address format", errorTextFlow);
        } else if (!isTextFieldValid(passwordField)) {
            showErrorMessage("Invalid password format (4-20 characters required)", errorTextFlow);
        } else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showErrorMessage("Confirmation password doesn't match", errorTextFlow);
        } else if (!isTextFieldValid(nameField)) {
            showErrorMessage("Invalid name format (4-20 characters required)", errorTextFlow);
        } else if (!isTextFieldValid(surnameField)) {
            showErrorMessage("Invalid surname format (4-20 characters required)", errorTextFlow);
        }
        else if (studiesComboBox.getValue() == null) {
            showErrorMessage("Please select your study field", errorTextFlow);
        } else {
            errorTextFlow.getChildren().clear();
        }

    }

    @FXML
    protected void onBackButtonClick(){
        super.changeWindow("login");
    }

    @FXML
    protected void onSignupButtonClick(){
        User utente = new User();
        utente.setEmail(emailField.getEmailAddress());
        utente.setName(nameField.getText());
        utente.setSurname(surnameField.getText());
        String hashedPassword = BCrypt.hashpw(passwordField.getText().trim(), BCrypt.gensalt(12));
        utente.setPassword(hashedPassword);
        utente.setStudyField(studiesComboBox.getValue());
        // Aggiungere citta di interesse
        utente.setPreferredCities(selectedCities);
        boolean emailAvailable = super.getMongoConnectionManager().addUser(utente);
        if(emailAvailable) {
            super.changeWindow("login");
        } else {
            signupButton.setDisable(true);
            showErrorMessage("Email not available", errorTextFlow);
        }

    }

}

