package it.unipi.erasmusnest.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONException;
import org.json.JSONObject;

public class AnalyticsController extends Controller {

    @FXML
    TextArea outputPriceTextArea;
    @FXML
    TextArea outputPositionTextArea;
    @FXML
    VBox vboxQueryPrice;
    @FXML
    VBox vboxQueryPosition;
    @FXML
    VBox vboxPriceOutput;
    @FXML
    VBox vboxPositionOutput;
    @FXML
    HBox hboxPriceButton;
    @FXML
    HBox hboxPositionButton;
    @FXML
    Label title;
    @FXML
    Button priceAnalyticButton;
    @FXML
    Button PositionAnalyticButton;
    @FXML
    Button goToHomepageButton;
    @FXML
    Button goBackButton;
    @FXML
    Spinner<Integer> inputAccommodates;
    @FXML
    Spinner<Integer> inputBathrooms;
    @FXML
    Spinner<Integer> inputPrice;

    @FXML
    private void initialize() {
        System.out.println("Analytics controller initialize");
        title.prefWidthProperty().bind(super.getRootPane().widthProperty());
        vboxQueryPrice.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxQueryPosition.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxPriceOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        vboxPositionOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        hboxPriceButton.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.1));

    }

    @FXML
    void goBack() {
        super.changeWindow("myProfile");
    }

    public void goToTheHomePage() {
        super.changeWindow("homepage");
    }

    @FXML
    void onPriceAnalyticButton(ActionEvent actionEvent) {
        System.out.println("\n>>>Price analytics button pressed\n");
        Integer accommodates = inputAccommodates.getValue();
        Integer bathrooms = inputBathrooms.getValue();
        Integer price = inputPrice.getValue();
        String result = getMongoConnectionManager().getPriceAnalytics(accommodates, bathrooms, price);
        try {
            JSONObject jsonObject = new JSONObject(result);

            String lowestAveragePriceCity = jsonObject.getString("lowestAveragePriceCity");
            double lowestAveragePrice = Math.round(jsonObject.getDouble("lowestAveragePrice") * 100.0) / 100.0;
            String highestAveragePriceCity = jsonObject.getString("highestAveragePriceCity");
            double highestAveragePrice = Math.round(jsonObject.getDouble("highestAveragePrice"));
            result = "Lowest average price city: " + lowestAveragePriceCity + "\nLowest average price: " + lowestAveragePrice + "\nHighest average price city: " + highestAveragePriceCity + "\nHighest average price: " + highestAveragePrice;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        outputPriceTextArea.setText(result);
    }

    @FXML
    void onPositionAnalyticButton(ActionEvent actionEvent) {
        System.out.println("\n>>>Position analytics button pressed\n");
    }


}
