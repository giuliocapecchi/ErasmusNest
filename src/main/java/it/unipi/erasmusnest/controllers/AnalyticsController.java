package it.unipi.erasmusnest.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;

public class AnalyticsController extends Controller {

    @FXML
    Label valueLabel;
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
    Spinner<Integer> minInputPrice;
    @FXML
    Spinner<Integer> maxInputPrice;
    @FXML
    Label cityHighestPrice;
    @FXML
    Label cityLowestPrice;
    @FXML
    Slider distanceSlider;
    @FXML
    ComboBox<String> citySplitComboBox;

    @FXML
    private void initialize() {
        System.out.println("Analytics controller initialize");
        title.prefWidthProperty().bind(super.getRootPane().widthProperty());
        vboxQueryPrice.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxQueryPosition.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxPriceOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        vboxPositionOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.4));
        hboxPriceButton.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.1));
        citySplitComboBox.getItems().addAll(getSession().getCities());
        citySplitComboBox.getItems().add("None");
        distanceSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                valueLabel.setText("Chosen distance from center: "+String.format("%.2f", newValue.doubleValue())+" km"));

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
        Integer priceMin = minInputPrice.getValue();
        Integer priceMax = maxInputPrice.getValue();
        String lowestAveragePriceCity = ""; String highestAveragePriceCity = "";
        double lowestAveragePrice =-1; double highestAveragePrice =-1;
        String result = getMongoConnectionManager().getPriceAnalytics(accommodates, bathrooms, priceMin, priceMax);
        try {
            JSONObject jsonObject = new JSONObject(result);
            lowestAveragePriceCity = jsonObject.getString("lowestAveragePriceCity");
            lowestAveragePrice = Math.round(jsonObject.getDouble("lowestAveragePrice") * 100.0) / 100.0;
            highestAveragePriceCity = jsonObject.getString("highestAveragePriceCity");
            highestAveragePrice = Math.round(jsonObject.getDouble("highestAveragePrice"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(highestAveragePrice==-1 && highestAveragePriceCity.isEmpty()){
            cityHighestPrice.setText("No data available");
        } else {
            cityHighestPrice.setText(highestAveragePriceCity + " with " + highestAveragePrice + "$");
        }
        if(lowestAveragePrice==-1 && lowestAveragePriceCity.isEmpty()){
            cityLowestPrice.setText("No data available");
        } else {
            cityLowestPrice.setText(lowestAveragePriceCity + " with " + lowestAveragePrice + "$");
        }
        cityHighestPrice.setVisible(true); cityLowestPrice.setVisible(true);
    }

    @FXML
    void onPositionAnalyticButton() {
        System.out.println("\n>>>Distance from center average price analytics button pressed\n");
        int distance = (int)(distanceSlider.getValue()*1000);
        String city = citySplitComboBox.getValue();
        if(city != null && !city.equals("None")){
            System.out.println("City selected: " + city);
            double result = getMongoConnectionManager().averagePriceNearCityCenter(city, distance);
            outputPositionTextArea.setText("City: "+city +"\nDistance from center: "+String.format("%.2f", distanceSlider.getValue())+" km\nAverage price: "+ result);
        }else{
            System.out.println("Distance: " + distance);
            outputPositionTextArea.setText("");
            List<Map<String, Object>> result =  getMongoConnectionManager().averagePriceNearCityCenterForEachCity(distance);
            for (Map<String, Object> map : result) {
                if(map.get("city") != null && map.get("avgPrice") != null)
                    outputPositionTextArea.appendText(map.get("city") + ": \t" + map.get("avgPrice") + "\n");
            }
        }
    }
}
