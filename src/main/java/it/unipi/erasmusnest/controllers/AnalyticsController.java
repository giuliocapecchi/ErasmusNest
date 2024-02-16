package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsController extends Controller {

    @FXML
    private Label valueLabel;
    @FXML
    private TextArea outputPositionTextArea;
    @FXML
    private VBox vboxQueryPrice;
    @FXML
    private VBox vboxQueryPosition;
    @FXML
    private VBox vboxPriceOutput;
    @FXML
    private VBox vboxPositionOutput;
    @FXML
    private HBox hboxPriceButton;
    @FXML
    private HBox hboxPositionButton;
    @FXML
    private Label title;
    @FXML
    private Spinner<Integer> inputAccommodates;
    @FXML
    private Spinner<Integer> inputBathrooms;
    @FXML
    private Spinner<Integer> minInputPrice;
    @FXML
    private Spinner<Integer> maxInputPrice;
    @FXML
    private Label cityHighestPrice;
    @FXML
    private Label cityLowestPrice;
    @FXML
    private Slider distanceSlider;
    @FXML
    private ComboBox<String> citySplitComboBox;
    @FXML
    private VBox vboxQueryHeatmap;
    @FXML
    private VBox vboxMapOutput;
    @FXML
    private ComboBox<String> citySplitComboBox2;
    @FXML
    private WebView heatmapWebView;
    @FXML
    private HBox hboxHeatmapButton;
    @FXML
    private Button HeatmapAnalyticButton;

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
        Integer accommodates = inputAccommodates.getValue();
        Integer bathrooms = inputBathrooms.getValue();
        Integer priceMin = minInputPrice.getValue();
        Integer priceMax = maxInputPrice.getValue();
        String lowestAveragePriceCity = ""; String highestAveragePriceCity = "";
        int lowestAverageApartmentsNumber = -1;
        int highestAverageApartmentsNumber = -1;
        double lowestAveragePrice =-1; double highestAveragePrice =-1;
        String result = getMongoConnectionManager().getPriceAnalytics(accommodates, bathrooms, priceMin, priceMax);
        if(result!=null) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject object = jsonObject.getJSONObject("lower");
                lowestAveragePriceCity = object.getString("name");
                lowestAveragePrice = Math.round(object.getDouble("price") * 100.0) / 100.0;
                lowestAverageApartmentsNumber = object.getInt("count");
                object = jsonObject.getJSONObject("higher");
                highestAveragePriceCity = object.getString("name");
                highestAveragePrice = Math.round(object.getDouble("price") * 100.0) / 100.0;
                highestAverageApartmentsNumber = object.getInt("count");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cityLowestPrice.setText(lowestAveragePriceCity + " with " + lowestAveragePrice + "$ for " + lowestAverageApartmentsNumber + " apartments");
            cityHighestPrice.setText(highestAveragePriceCity + " with " + highestAveragePrice + "$ for " + highestAverageApartmentsNumber + " apartments");
        }
        else {
            cityLowestPrice.setText("No results found");
            cityHighestPrice.setText("No results found");
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
