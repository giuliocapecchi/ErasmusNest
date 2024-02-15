package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnalyticsController extends Controller {
    @FXML
    Label valueLabel;
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
    VBox vboxQueryHeatmap;
    @FXML
    VBox vboxMapOutput;
    @FXML
    ComboBox<String> citySplitComboBox2;
    @FXML
    WebView heatmapWebView;
    @FXML
    HBox hboxHeatmapButton;
    @FXML
    Button HeatmapAnalyticButton;

    @FXML
    private void initialize() {
        System.out.println("Analytics controller initialize");
        title.prefWidthProperty().bind(super.getRootPane().widthProperty());
        vboxQueryPrice.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        vboxQueryPosition.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));
        vboxQueryHeatmap.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.3));

        vboxPriceOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxPositionOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));
        vboxMapOutput.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.5));

        hboxPriceButton.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        hboxPositionButton.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        hboxHeatmapButton.prefWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        citySplitComboBox.getItems().addAll(getSession().getCities());
        citySplitComboBox.getItems().add("None");
        distanceSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                valueLabel.setText("Chosen distance from center: "+String.format("%.2f", newValue.doubleValue())+" km"));
        citySplitComboBox2.getItems().addAll(getSession().getCities());
        citySplitComboBox2.getItems().add("None");
        citySplitComboBox2.setOnAction(event -> {
            String selectedItem = citySplitComboBox2.getValue();
            HeatmapAnalyticButton.setDisable(selectedItem == null || selectedItem.equals("None"));
        });
        HeatmapAnalyticButton.setDisable(true);

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
        Integer lowestAverageApartmensNumber = -1;
        Integer highestAverageApartmensNumber = -1;
        double lowestAveragePrice =-1; double highestAveragePrice =-1;
        String result = getMongoConnectionManager().getPriceAnalytics(accommodates, bathrooms, priceMin, priceMax);
        try {
            JSONObject jsonObject = new JSONObject(result);
            lowestAveragePriceCity = jsonObject.getString("lowestAveragePriceCity");
            lowestAveragePrice = Math.round(jsonObject.getDouble("lowestAveragePrice") * 100.0) / 100.0;
            lowestAverageApartmensNumber = jsonObject.getInt("lowestAveragePriceCount");
            highestAveragePriceCity = jsonObject.getString("highestAveragePriceCity");
            highestAveragePrice = Math.round(jsonObject.getDouble("highestAveragePrice") * 100.0) / 100.0;
            highestAverageApartmensNumber = jsonObject.getInt("highestAveragePriceCount");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(highestAveragePrice==-1 || highestAveragePriceCity.isEmpty() || highestAverageApartmensNumber==-1){
            cityHighestPrice.setText("No data available");
        } else {
            cityHighestPrice.setText(highestAveragePriceCity + " with " + highestAveragePrice + "$ for " + highestAverageApartmensNumber + " apartments");
        }
        if(lowestAveragePrice==-1 && lowestAveragePriceCity.isEmpty() || highestAverageApartmensNumber==-1){
            cityLowestPrice.setText("No data available");
        } else {
            cityLowestPrice.setText(lowestAveragePriceCity + " with " + lowestAveragePrice + "$ for " + lowestAverageApartmensNumber + " apartments");
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

    public void onHeatmapButton() throws InterruptedException {
        System.out.println("\n>>>Heatmap button pressed\n");
        String city = citySplitComboBox2.getValue();
        HashMap<Point2D, Integer> heatmapTiles =  getMongoConnectionManager().getHeatmap(city);
        MapGraphicManager mapGraphicManager = new MapGraphicManager(heatmapWebView, heatmapTiles);
        mapGraphicManager.prepareHeatmap();
        mapGraphicManager.loadMap("heatmap");
    }
}
