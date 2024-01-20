package it.unipi.erasmusnest.graphicmanagers;

import javafx.scene.control.Alert;

public class AlertDialogGraphicManager {

    private final Alert alert;

    public AlertDialogGraphicManager(String header) {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
    }

    public void show() {
        alert.showAndWait();
    }

}
