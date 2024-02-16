package it.unipi.erasmusnest.controllers;

import com.dlsc.gemsfx.EmailField;
import it.unipi.erasmusnest.consistency.RedisConsistencyManager;
import it.unipi.erasmusnest.consistency.RedisMongoConsistencyManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;

import java.util.Objects;

public class LoginController extends Controller{

    @FXML
    private Label title;
    @FXML
    private Button continueButton;
    @FXML
    private EmailField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextFlow errorTextFlow;
    @FXML
    private Button loginButton;
    @FXML
    private Button signupButton;
    @FXML
    private ImageView logoImageView;

    public LoginController() {
    }

    @FXML
    private void initialize() {

        if(getSession().getConnectionError()) {
            showErrorMessage("Connection error", errorTextFlow);
            getSession().setConnectionError(false);
        }

        if(getSession().isLogged()){
            super.changeWindow("homepage");
        }

        if(super.getActualWindowName() == null) {
           super.setFirstWindow("login");
        }

        if(getSession().getCities() == null || getSession().getCities().isEmpty()) {
            getSession().setCities(getNeo4jConnectionManager().getAllCities());
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
        super.changeWindow("signup");
    }

    @FXML
    protected void onLoginButtonClick() {
        getSession().setLogged(false);

        if(isEmailFieldValid(emailField) && isTextFieldValid(passwordField)) {
            String password = getRedisConnectionManager().getPassword(emailField.getEmailAddress());
            System.out.println("REDIS Password: " + password);
            if(password != null && password.equals(passwordField.getText())) { // user found in Redis
                getSession().setLogged(true);
                getSession().getUser().setEmail(emailField.getEmailAddress());
                getSession().setReservationsApartmentIds(getRedisConnectionManager().getReservedApartments(emailField.getEmailAddress()));

                long ttl = getRedisConnectionManager().getUserTTL(emailField.getEmailAddress());
                if(ttl == -1)
                    new RedisMongoConsistencyManager(getRedisConnectionManager(), getMongoConnectionManager())
                            .updateUserPasswordOnMongo(emailField.getEmailAddress(), passwordField.getText(), getSession().getReservationsApartmentIds());

            } else if(password != null && !password.equals(passwordField.getText())) {
                // Password taken different from real pw
                showErrorMessage("Invalid email or password", errorTextFlow);
            } else { // check credentials in MongoDB
                System.out.println("Credentials not found in Redis. Let's check in MongoDB");
                String mongoPassword = getMongoConnectionManager().getPassword(emailField.getEmailAddress());
                System.out.println("MongoDB Password: " + mongoPassword);

                // TODO qui ho fatto in modo che la W su Redis sia gestita da thread
                if(mongoPassword != null && mongoPassword.equals(passwordField.getText())) {
                    getSession().setLogged(true);
                    getSession().getUser().setEmail(emailField.getEmailAddress());
                    super.getRedisConnectionManager().addUser(emailField.getEmailAddress(), passwordField.getText());

                    new RedisConsistencyManager(getRedisConnectionManager())
                            .addUserOnRedis(emailField.getEmailAddress(), passwordField.getText());

                    getSession().setLogged(true);
                }else{
                    showErrorMessage("Invalid email or password", errorTextFlow);
                }
            }
            if(getSession().isLogged()){
                getSession().getUser().setPassword(passwordField.getText());
                if(getPreviousWindowName() != null && !getPreviousWindowName().equals("signup")) {
                    super.backToPreviousWindow();
                } else {
                    super.changeWindow("homepage");
                }
            }
        }

    }

    @FXML
    protected void onContinueButtonClick(){
        getSession().setLogged(false);
        if(Objects.equals(getPreviousWindowName(), "apartment")) {
            super.backToPreviousWindow();
        } else {
            super.changeWindow("homepage");
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
