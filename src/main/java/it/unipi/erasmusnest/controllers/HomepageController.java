package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomepageController extends Controller{

    @FXML
    Button searchUserButton;

    @FXML
    RadioButton radioButtonLookForCities;

    @FXML
    RadioButton radioButtonLookForUsers;

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
        getSession().setCurrent_filter(0);
        getSession().setCurrent_page(1);
    }

    @FXML
    private void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
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

        radioButtonLookForCities.setToggleGroup(toggleGroup);
        radioButtonLookForUsers.setToggleGroup(toggleGroup);
        radioButtonLookForCities.setSelected(true);
        searchUserButton.setVisible(false);
        getSession().setCities(CITIES);

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

    @FXML void lookForCities() {
        System.out.println("look for cities");
        radioButtonLookForUsers.setSelected(false);
        cityTextField.setText("");
        cityTextField.setOnKeyReleased(event -> handleSearchAction());
        searchUserButton.setVisible(false);
        resultsBox.getChildren().clear();
    }

    @FXML void lookForUsers() {
        System.out.println("look for users");
        radioButtonLookForCities.setSelected(false);
        cityTextField.setText("");
        cityTextField.setOnKeyReleased(event -> {});
        searchUserButton.setVisible(true);
        resultsBox.getChildren().clear();

    }

    public void handleSearchUserAction() {
        System.out.println("searching user...");
        User user = getMongoConnectionManager().findUser(cityTextField.getText());
        if(user == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("User "+cityTextField.getText()+" not found");
            alert.setContentText("The user you are looking for does not exist.");
            alert.showAndWait();
        }else{
            if(Objects.equals(getSession().getUser().getEmail(), user.getEmail())){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("You can't search yourself");
                alert.setContentText("Use the \"Profile\" button below.");
                alert.showAndWait();
                return;
            }
            getSession().setOtherProfileMail(user.getEmail());
            getSession().setNextWindowName("homepage");
            super.changeWindow("profile");
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
}
