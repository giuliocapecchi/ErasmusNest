package it.unipi.erasmusnest.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;

public class WriteReviewController extends Controller{
    @FXML
    Label pageTitle;

    @FXML
    Button goBackButton;

    @FXML
    Button goToHomepageButton;

    @FXML
    Button submitButton;

    @FXML
    TextFlow errorTextFlow;

    @FXML
    TextArea textArea;

    @FXML
    Slider ratingSlider;

    @FXML
    private void initialize() {
        System.out.println("makeReviewController initialize");
        ratingSlider.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        ratingSlider.setValue(3);
        goBackButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        submitButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        goToHomepageButton.maxWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        submitButton.setDisable(true);
        pageTitle.setText("Write a review for the apartment in "+getSession().getCity()+".");

        int maxCharacters = 999;
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= maxCharacters) {
                textArea.setText(newValue.substring(0, maxCharacters));
                showErrorMessage("The review must be less than "+(maxCharacters+1)+" characters.",errorTextFlow);
            }else{
                errorTextFlow.getChildren().clear();
            }
            submitButton.setDisable(newValue.isEmpty());
        });

    }

    @FXML void goBack(ActionEvent actionEvent) {
        System.out.println("go back action. IMPLEMENT USER PROFILE TO DO THIS");
       // super.changeWindow("profile");
    }

    @FXML void goToTheHomePage(ActionEvent actionEvent) {
        System.out.println("go to the home page action");
        super.changeWindow("homepage");
    }

    @FXML void submit(ActionEvent actionEvent) {
        System.out.println("submit action");
        System.out.println("MANCA IL REDIRECT AL PROFILE DELL'UTENTE");
        //super.changeWindow("profile");
        getNeo4jConnectionManager().addReview(getSession().getUser().getEmail(), getSession().getApartmentId(),textArea.getText(), (int)ratingSlider.getValue());
        super.changeWindow("homepage");
    }
}
