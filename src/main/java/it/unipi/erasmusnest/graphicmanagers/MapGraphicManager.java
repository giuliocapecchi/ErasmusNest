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
import java.util.Collections;
import java.util.List;

public class MapGraphicManager {

    private Point2D location;
    private WebView webView;

    public MapGraphicManager(WebView webView, Point2D location){
        this.webView = webView;
        this.location = location;
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

            /*harset charset = StandardCharsets.UTF_8;
            String actualContent = Files.readString(filePath, charset);
            actualContent = actualContent.replaceAll(content);
            Files.write(filePath, actualContent.getBytes(charset));*/

            // Create a list with a single line (the new content)
            List<String> lines = Collections.singletonList(content);

            // Write the modified lines (single line) back to the file, replacing the entire content
            Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(){
        WebEngine webEngine = webView.getEngine();
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
            String encoded_address = URLEncoder.encode(address, StandardCharsets.UTF_8);
            JSONArray results = getObjects(encoded_address);
            if (!results.isEmpty()) {
                System.out.println(results);
                resultLabel.setText("");
                JSONObject result = results.getJSONObject(0);
                this.webView = webView;
                this.location = new Point2D(result.getDouble("lat"), result.getDouble("lon"));
                setLocationOnMap();
                loadMap();
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


}
