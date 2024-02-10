package it.unipi.erasmusnest.graphicmanagers;

import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class MapGraphicManager {

    private Point2D location;
    private HashMap<Point2D,Integer> heatmapTiles;
    private String city;
    private WebView webView;

    public MapGraphicManager(WebView webView, Point2D location){
        this.webView = webView;
        this.location = location;
    }

    public MapGraphicManager(WebView webView, HashMap<Point2D,Integer> heatmapTiles){
        this.webView = webView;
        this.heatmapTiles = heatmapTiles;
    }

    public MapGraphicManager(){
        this.webView = null;
        this.location = null;
    }

    public void setLocationOnMap() {
        // Get the resource URL
        Path filePath = new File(getClass().getResource("/map/location.txt").getFile()).toPath();

        // The content to be added to the top of the JavaScript file
        String content = location.getX()+","+location.getY();

        try {
            // Create a list with a single line (the new content)
            List<String> lines = Collections.singletonList(content);

            // Write the modified lines (single line) back to the file, replacing the entire content
            Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareHeatmap() {
        // Get the resource URL
        Path filePath = new File(getClass().getResource("/heatmap/locations.txt").getFile()).toPath();
        // Rimuovi il file se esiste già
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Aggiungi i punti al file
        for (Map.Entry<Point2D, Integer> entry : heatmapTiles.entrySet()) {
            // The content to be added to the top of the JavaScript file
            String content = entry.getKey().getX() + "," + entry.getKey().getY() + ";" + entry.getValue();
            try {
                // Create a list with a single line (the new content)
                List<String> lines = Collections.singletonList(content);
                // Write the modified lines (single line) back to the file, replacing the entire content
                Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void loadMap(String mapType){
        WebEngine webEngine = webView.getEngine();
        if(Objects.equals(mapType, "heatmap"))
            webEngine.load(getClass().getResource("/heatmap/heatmap.html").toExternalForm());
        else if(Objects.equals(mapType, "map"))
            webEngine.load(getClass().getResource("/map/map.html").toExternalForm());
    }


    /**
     * This method takes an address, a WebView and a Label as parameters.
     * It returns true if the address is found and the map is loaded, false otherwise.
     * It puts on the selected WebView the map for the selected address, and uses the label in case of errors.
     * @param address : string of the address to search
     * @param webView : WebView where the map will be loaded
     * @param resultLabel : Label where the error message will be loaded
     * @return true if the address is found and the map is loaded, false otherwise
     */
    public boolean geocodeAddress(String address, WebView webView, Label resultLabel) {
        try {
            System.out.println("\n\n\nADDRESS:"+address);
            String encoded_address = URLEncoder.encode(address, StandardCharsets.UTF_8);
            JSONArray results = getObjects(encoded_address);
            if (!results.isEmpty()) {
                System.out.println(results);
                resultLabel.setText("");
                JSONObject result = results.getJSONObject(0);
                this.webView = webView;
                this.location = new Point2D(result.getDouble("lat"), result.getDouble("lon"));
                setLocationOnMap();
                loadMap("map");
                return true;
            } else {
                resultLabel.setText("Address not found.Try again please.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Error during geocoding.");
        }
        return false;
    }

    private JSONArray getObjects(String address) throws IOException {
        String apiUrl = "https://nominatim.openstreetmap.org/search?format=json&q=" + address;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();
        return new JSONArray(response.toString());
    }

    public double getLatitude(){
        return location.getX();
    }

    public double getLongitude(){
        return location.getY();
    }

    public Point2D getLocation(){
        return location;
    }


    public void setLocation(Point2D location) {
        this.location = location;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}
