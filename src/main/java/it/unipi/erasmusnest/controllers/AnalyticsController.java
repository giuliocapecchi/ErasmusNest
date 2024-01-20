package it.unipi.erasmusnest.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AnalyticsController extends Controller {

    @FXML
    TextArea outputTextArea;

    @FXML
    VBox vboxQuery;

    @FXML
    VBox vboxOutput;

    @FXML
    HBox hboxButton;

    @FXML
    Label title;

    @FXML
    Button analyticsButton1;

    @FXML
    Button analyticsButton2;

    @FXML
    Button analyticsButton3;

    @FXML
    Button goToHomepageButton;

    @FXML
    Button goBackButton;

    @FXML
    private void initialize() {
        System.out.println("Analytics controller initialize");
        title.prefWidthProperty().bind(super.getRootPane().widthProperty());
        vboxQuery.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        hboxButton.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.1));
    }

    @FXML void goBack(ActionEvent actionEvent) {
        super.changeWindow("myProfile");
    }

    public void goToTheHomePage(ActionEvent actionEvent) {
        super.changeWindow("homepage");
    }

    public void analytics1(ActionEvent actionEvent) {
        System.out.println("implements analytics one");
    }

    public void analytics2(ActionEvent actionEvent) {
        System.out.println("implements analytics two");
    }

    public void analytics3(ActionEvent actionEvent) {
        System.out.println("implements analytics three");
    }
}
