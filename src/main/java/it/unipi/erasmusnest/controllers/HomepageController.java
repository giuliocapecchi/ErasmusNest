package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class HomepageController extends Controller{

    @FXML
    private Button searchUserButton;
    @FXML
    private RadioButton radioButtonLookForCities;
    @FXML
    private RadioButton radioButtonLookForUsers;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Label title;
    @FXML
    private Button signUpButton;
    @FXML
    private Button loginButton;
    @FXML
    private Label welcomeText;
    @FXML
    private Button profileButton;
    @FXML
    private Button logoutButton;
    @FXML
    private TextField cityTextField;
    @FXML
    private VBox resultsBox;

    public HomepageController() {
        System.out.println("HomepageController constructor");
        getSession().setCurrent_filter(0);
        getSession().setCurrent_page(1);
        setFirstWindow("homepage");
    }

    @FXML
    private void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        cityTextField.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        resultsBox.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        title.maxWidthProperty().bind(super.getRootPane().widthProperty());
        logoImageView.fitWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        radioButtonLookForCities.setToggleGroup(toggleGroup);
        radioButtonLookForUsers.setToggleGroup(toggleGroup);
        radioButtonLookForCities.setSelected(true);
        searchUserButton.setVisible(false);

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

        for (String city : getSession().getCities()) {
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
        setFirstWindow("login");
        getSession().reset();
        super.changeWindow("login");
    }

    @FXML void handleSignupAction() {
        super.changeWindow("signup");
    }

    @FXML void handleLoginAction() {
        super.changeWindow("login");
    }

    @FXML void handleProfileAction() {
        super.changeWindow("myProfile");
    }

    @FXML void lookForCities() {
        radioButtonLookForUsers.setSelected(false);
        cityTextField.setText("");
        cityTextField.setOnKeyReleased(event -> handleSearchAction());
        searchUserButton.setVisible(false);
        resultsBox.getChildren().clear();
    }

    @FXML void lookForUsers() {
        radioButtonLookForCities.setSelected(false);
        cityTextField.setText("");
        cityTextField.setOnKeyReleased(event -> {});
        searchUserButton.setVisible(true);
        resultsBox.getChildren().clear();

    }

    public void handleSearchUserAction() {
        System.out.println("searching for user : "+cityTextField.getText());
        User user = getMongoConnectionManager().findUser(cityTextField.getText());
        if(user == null) {
            System.out.println("User not found in MongoDB");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("User "+cityTextField.getText()+" not found");
            alert.setContentText("The user you are looking for does not exist.");
            alert.showAndWait();
        }else if(Objects.equals(getSession().getUser().getEmail(), user.getEmail())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("You can't search yourself");
            alert.setContentText("Use the \"Profile\" button below.");
            alert.showAndWait();
        }else{ // user was found
            getSession().setOtherProfileMail(user.getEmail());
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

    public void handleHelpAction() {
        String title = "User guide";
        String header = "Welcome to the user guide";
        String content = "Welcome to the ErasmusNest application!\n\n" +
                "Are you in search of the perfect apartment? Or perhaps you're looking to share your property with students from around the globe?\n" +
                "Well, you've come to the right place!\n"+
                "For students, we offer a wide range of homes and apartments." +
                "\nWith our review system and personalized recommendations based on likes, you can discover the best destinations and\naccommodations tailored to your preferences and interests.\n"+
                "For hosts, we provide a simple but efficient way to expose your\nproperty on the market. Manage your bookings, check your\nreviews, and be ready to welcome erasmus students from\n around the globe.\n"+
                "\nYou can also connect with other users, follow their activities, and receive personalized recommendations. Whether it's advice based on your cities of interest, users to follow, or the most popular homes, we're here to help you create unforgettable experiences.\n"+
                "Check your profile to update all your personal preferences."+
                "\n\nWe hope you enjoy your stay with us!";
        String type = "information";
        AlertDialogGraphicManager alertDialogGraphicManager = new AlertDialogGraphicManager(title, header, content, type);
        alertDialogGraphicManager.showAndGetConfirmation();
    }

}
