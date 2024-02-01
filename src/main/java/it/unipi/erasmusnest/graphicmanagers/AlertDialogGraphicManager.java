package it.unipi.erasmusnest.graphicmanagers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Objects;

public class AlertDialogGraphicManager {

    private final Alert alert;

    public AlertDialogGraphicManager(String header) {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
    }

    public AlertDialogGraphicManager(String title, String header, String content, String type) {
        if(Objects.equals(type, "confirmation")){
            alert = new Alert(Alert.AlertType.CONFIRMATION);
        }else if(Objects.equals(type, "information")){
            alert = new Alert(Alert.AlertType.INFORMATION);
        }else{
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
        }
        alert.setTitle(title);
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
