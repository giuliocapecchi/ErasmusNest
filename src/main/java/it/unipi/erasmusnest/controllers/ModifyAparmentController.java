package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;

import java.io.File;
import java.util.ArrayList;

public class ModifyAparmentController extends Controller{

    @FXML
    private VBox apartmentVBox;
    @FXML
    private VBox bathroomsVBox;
    @FXML
    private VBox priceVBox;
    @FXML
    private VBox neighborhoodVBox;
    private Spinner<Integer> inputAccommodates;
    private Spinner<Integer> inputBathrooms;
    private Spinner<Integer> inputPrice;
    @FXML
    private VBox imageVBox;
    private TextField textField;
    private TextArea descriptionTextArea;
    @FXML
    private Button updateHouse;
    @FXML
    private Button removeHouse;

    public ModifyAparmentController()
    {

    }

    @FXML
    private void initialize()
    {
        String apartmentId = getSession().getApartmentId();
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);
        System.out.println("\n\n\n"+apartment.toString());

        inputAccommodates = new Spinner<>();
        inputBathrooms = new Spinner<>();
        inputPrice = new Spinner<>();

        SpinnerValueFactory<Integer> valoriAccommodates = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,apartment.getMaxAccommodates());
        SpinnerValueFactory<Integer> valoriBathrooms = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,apartment.getBathrooms());
        SpinnerValueFactory<Integer> valoriPrice = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000,apartment.getDollarPriceMonth());
        // setting suitable values for the spinners
        inputAccommodates.setValueFactory(valoriAccommodates);
        inputBathrooms.setValueFactory(valoriBathrooms);
        inputPrice.setValueFactory(valoriPrice);
        priceVBox.getChildren().add(inputPrice);
        apartmentVBox.getChildren().add(inputAccommodates);
        bathroomsVBox.getChildren().add(inputBathrooms);
        // pulsante.setOnAction(e->calcola());
        descriptionTextArea = new TextArea();
        descriptionTextArea.setText(apartment.getDescription());
        descriptionTextArea.setWrapText(true); // Abilita il word wrapping per l'area di testo
        neighborhoodVBox.getChildren().add(descriptionTextArea);
        //Aggiunta foto:
        // Creazione di un campo di input di testo
        textField = new TextField();
        // textField.setText(apartment.getImageURL());
        textField.setText("");
        imageVBox.getChildren().add(textField);
    }

    public void onUpdateHouseButtonClick(ActionEvent actionEvent)
    {
        //Qua deve sparare la query su MONGO per aggiornare i dati dell'appartamento
        String apartmentId = getSession().getApartmentId();
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);
        apartment.setMaxAccommodates(inputAccommodates.getValue());
        Integer bathrooms = inputBathrooms.getValue();
        apartment.setBathrooms(bathrooms);
        apartment.setDollarPriceMonth(inputPrice.getValue());
        apartment.setDescription(descriptionTextArea.getText());
        // String imageUrl = textField.getText()==null || textField.getText().isBlank() || textField.getText().isEmpty() ? "" : textField.getText();
        ArrayList<String> imageUrl = new ArrayList<>();
        imageUrl.add(textField.getText());
        apartment.setImageURL(imageUrl);
        apartment.setId(getSession().getApartmentId());

        if(getMongoConnectionManager().updateApartment(apartment))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("House correctly modified.");

            // Aggiungi un pulsante "OK"
            ButtonType okButton = new ButtonType("OK");
            alert.getButtonTypes().setAll(okButton);

            // Gestisci l'azione del pulsante "OK"
            alert.setOnCloseRequest(event -> {
                // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
                super.changeWindow("modifyApartment","myProfile");
            });

            // Mostra la finestra di dialogo
            alert.showAndWait();
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Impossible to correctly modify the house.");

            // Aggiungi un pulsante "OK"
            ButtonType okButton = new ButtonType("OK");
            alert.getButtonTypes().setAll(okButton);

            // Gestisci l'azione del pulsante "OK"
            alert.setOnCloseRequest(event -> {
                // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
                super.refreshWindow();
            });

            // Mostra la finestra di dialogo
            alert.showAndWait();
        }

    }

    private void showConfirmationMessage(String message, Button button) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 15px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        // Mostra il messaggio di conferma
        popOver.show(button);
    }

    public void onGoBackButtonClick(ActionEvent actionEvent)
    {
        super.changeWindow("modifyApartment","myProfile");
    }

    @FXML
    private void onRemoveHouseButtonClick(ActionEvent actionEvent)
    {
        String apartmentId = getSession().getApartmentId();
        boolean remove = new AlertDialogGraphicManager("Delete confirmation","Are you sure you want to remove this apartment?\n",
                "You will not be able to recover it","confirmation").showAndGetConfirmation();
        if(remove)
        {
            if(getRedisConnectionManager().isApartmentReserved(apartmentId))
            {
                // non ci sono prenotazioni per questo appartamento
                if(getMongoConnectionManager().removeApartment(apartmentId))
                {
                    // Apartment removed from MongoDB
                    // Apartment is still available on Neo4j, apartments view
                    // While someone try to find out more information on apartment view, this'll be removed
                    alertDialog("House correctly removed");
                }
                else
                {
                    alertDialog("Impossible to remove house");
                }
            }
            else
            {
                showConfirmationMessage("Remove failed", removeHouse);
            }

        }
    }

    private void alertDialog(String s)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Impossible to correctly modify the house.");

        ButtonType okButton = new ButtonType("OK");
        alert.getButtonTypes().setAll(okButton);

        alert.setOnCloseRequest(event -> {
            // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
            super.refreshWindow();
        });

        // Mostra la finestra di dialogo
        alert.showAndWait();
    }

}
