package it.unipi.erasmusnest.graphicmanagers;

import javafx.geometry.Point2D;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class MapGraphicManager {

    private final Point2D location;
    private final WebView webView;

    public MapGraphicManager(WebView webView, Point2D location){
        this.webView = webView;
        this.location = location;
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

}
