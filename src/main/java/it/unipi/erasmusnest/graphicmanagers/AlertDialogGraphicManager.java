package it.unipi.erasmusnest.graphicmanagers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertDialogGraphicManager {

    private final Alert alert;

    public AlertDialogGraphicManager(String header) {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
    }

    public AlertDialogGraphicManager(String header, String content) {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete confirmation");
        alert.setHeaderText(header);
        alert.setContentText(content);
    }

    public void show() {
        alert.showAndWait();
    }

    public boolean showAndGetConfirmation() {
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
}
