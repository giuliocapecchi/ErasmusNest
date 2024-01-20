package it.unipi.erasmusnest.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class HomepageController extends Controller{

    @FXML
    ImageView logoImageView;

    @FXML
    Label title;

    @FXML
    Button signUpButton;

    @FXML
    Button loginButton;

    @FXML
    Label welcomeText;

    @FXML
    Button profileButton;

    @FXML
    Button logoutButton;

    @FXML
    TextField cityTextField;

    @FXML
    VBox resultsBox;

    private List<String> CITIES = new ArrayList<>();

    public HomepageController() {
        System.out.println("HomepageController constructor");
        getSession().setCurrent_filter(1);
        getSession().setCurrent_page(1);
    }

    @FXML
    private void initialize() {
        cityTextField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        resultsBox.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        title.maxWidthProperty().bind(super.getRootPane().widthProperty());
        logoImageView.fitWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        CITIES = getNeo4jConnectionManager().getAllCities();
        if(CITIES == null){
            getSession().reset();
            getSession().setConnectionError(true);
            super.changeWindow("login");
        }

        if(getSession().isLogged()) {
            System.out.println("User logged");
            welcomeText.setText("Welcome " + getSession().getUser().getEmail());
            welcomeText.setVisible(true);
            welcomeText.setManaged(true);
            profileButton.setVisible(true);
            profileButton.setManaged(true);
            logoutButton.setVisible(true);
            logoutButton.setManaged(true);
            signUpButton.setVisible(false);
            signUpButton.setManaged(false);
            loginButton.setVisible(false);
            loginButton.setManaged(false);

        } else {
            welcomeText.setVisible(false);
            welcomeText.setManaged(false);
            profileButton.setVisible(false);
            profileButton.setManaged(false);
            logoutButton.setVisible(false);
            logoutButton.setManaged(false);
            signUpButton.setVisible(true);
            signUpButton.setManaged(true);
            loginButton.setVisible(true);
            loginButton.setManaged(true);
        }


    }

    @FXML
    protected void handleSearchAction() {
        String searchText = cityTextField.getText().toLowerCase();
        if(searchText.isEmpty()) {
            resultsBox.getChildren().clear();
            return;
        }
        updateSearchResults(searchText);
    }

    private void updateSearchResults(String searchText) {
        resultsBox.getChildren().clear();

        for (String city : CITIES) {
            if (city.toLowerCase().contains(searchText)) {
                Hyperlink cityLink = new Hyperlink(city);
                cityLink.setOnAction(e -> handleCitySelection(city));
                resultsBox.getChildren().add(cityLink);
            }
        }
    }

    private void handleCitySelection(String city) {
        getSession().setCity(city);
        super.changeWindow("apartments");
    }

    @FXML void handleLogoutAction() {
        getSession().reset();
        super.changeWindow("login");
    }

    @FXML void handleSignupAction() {
        super.changeWindow("signup");
    }

    @FXML void handleLoginAction() {
        super.changeWindow("login");
    }

    @FXML void handleProfileAction()
    {
        System.out.println("Profile");
        super.changeWindow("myProfile");
    }
}
