package it.unipi.erasmusnest.controllers;

import com.dlsc.gemsfx.EmailField;
import it.unipi.erasmusnest.model.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class LoginController extends Controller{

    @FXML
    Label title;

    @FXML
    Button continueButton;

    @FXML
    EmailField emailField;

    @FXML
    PasswordField passwordField;

    @FXML
    TextFlow errorTextFlow;

    @FXML
    Button loginButton;

    @FXML
    Button signupButton;

    @FXML
    ImageView logoImageView;

    public LoginController() {
    }

    @FXML
    private void initialize() {

        if(getSession().getConnectionError()) {
            showErrorMessage("Connection error", errorTextFlow);
            getSession().setConnectionError(false);
            //printDialogPopup();
        }

        if(getSession().isLogged()){
            super.changeWindow("login","homepage");
        }

        if(super.getActualWindowName() == null) {
           super.setActualWindowName("login");
        }

        if(super.getActualWindowName().equals("login")) {
            loginButton.setDisable(true);
            emailField.emailAddressProperty().addListener((observable, oldValue, newValue) -> checkFields());
            passwordField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
            // Adjusting the width of the text fields and buttons relative to the StackPane's width
            title.maxWidthProperty().bind(super.getRootPane().widthProperty());
            emailField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
            passwordField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
            errorTextFlow.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
            loginButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.1));
            signupButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.1));
            continueButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
            // Doing the same also for the logo
            logoImageView.fitWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
            logoImageView.setPreserveRatio(true);

            errorTextFlow.getChildren().clear();

        }
    }

    @FXML
    protected void onSignupButtonClick(){
        super.changeWindow("login","signup");
    }

    @FXML
    protected void onLoginButtonClick() {
        boolean logged = false;
        if(isEmailFieldValid(emailField) && isTextFieldValid(passwordField)) {
            String password = super.getRedisConnectionManager().getPassword(emailField.getEmailAddress());
            System.out.println("\n\n\nREDIS Password: " + password);
            if(password != null && password.equals(passwordField.getText())) {
                getSession().setLogged(true);
                getSession().getUser().setEmail(emailField.getEmailAddress());
                logged = true;
            }
            else if(password != null && !password.equals(passwordField.getText()))
            {
                // Password taken different from real pw
                showErrorMessage("Invalid email or password", errorTextFlow);
            }
            else
            {
                System.out.println("Credentials not found in Redis. Let's check in MongoDB");
                String mongoPassword = super.getMongoConnectionManager().getPassword(emailField.getEmailAddress());
                System.out.println("\n\n\nREDIS Password: " + mongoPassword);
                if(mongoPassword != null && mongoPassword.equals(passwordField.getText())) {
                    getSession().setLogged(true);
                    getSession().getUser().setEmail(emailField.getEmailAddress());
                    super.getRedisConnectionManager().addUser(emailField.getEmailAddress(), passwordField.getText());
                    logged = true;
                }else{
                    showErrorMessage("Invalid email or password", errorTextFlow);
                }
            }
            if(logged){
                if(getPreviousWindowName() != null && !getPreviousWindowName().equals("signup")) {
                    super.backToPreviousWindow();
                } else {
                    super.changeWindow("login","homepage");
                }
            }
        }

    }

    @FXML
    protected void onContinueButtonClick(){
        getSession().setLogged(false);
        if(getPreviousWindowName() != null) {
            super.backToPreviousWindow();
        } else {
            super.changeWindow("login","homepage");
        }
    }

    @FXML
    protected void checkFields() {
        loginButton.setDisable(!isEmailFieldValid(emailField) || !isTextFieldValid(passwordField));
        if(!isEmailFieldValid(emailField)) {
            showErrorMessage("Invalid email address format", errorTextFlow);
        } else if (!isTextFieldValid(passwordField)) {
            showErrorMessage("Invalid password format (4-20 characters required)", errorTextFlow);
        } else {
            errorTextFlow.getChildren().clear();
        }
    }



}
