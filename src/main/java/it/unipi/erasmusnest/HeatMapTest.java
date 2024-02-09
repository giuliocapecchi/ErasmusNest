package it.unipi.erasmusnest;

import it.unipi.erasmusnest.controllers.Controller;
import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HeatMapTest extends Controller {

    @FXML
    WebView webView;

    @FXML
    public void initialize() {


        loadMap();

    }

    public void loadMap(){
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/heatmap/heatmap.html").toExternalForm());
    }

}
