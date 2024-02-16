package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Review;
import it.unipi.erasmusnest.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;

public class WriteReviewController extends Controller{

    @FXML
    private Label pageTitle;
    @FXML
    private Button goBackButton;
    @FXML
    private Button goToHomepageButton;
    @FXML
    private Button submitButton;
    @FXML
    private TextFlow errorTextFlow;
    @FXML
    private TextArea textArea;
    @FXML
    private Slider ratingSlider;

    @FXML
    private void initialize() {

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


        Review review = getNeo4jConnectionManager().getReview(getSession().getUser().getEmail(),getSession().getApartment().getId());
        if(review != null){
            textArea.setText(review.getComments());
            textArea.positionCaret(textArea.getText().length());
            ratingSlider.setValue(review.getRating());
        }


    }

    @FXML void goBack() {
        backToPreviousWindow();
    }

    @FXML
    void goToTheHomePage() {
        System.out.println("go to the home page action");
        super.changeWindow("homepage");
    }

    @FXML
    void submit() {
        System.out.println("submit action");
        Review review = new Review(getSession().getApartment().getId(),getSession().getUser().getEmail(),textArea.getText(), (int)ratingSlider.getValue());
        getNeo4jConnectionManager().addReview(review);
        super.changeWindow("myProfile");

    }
}
