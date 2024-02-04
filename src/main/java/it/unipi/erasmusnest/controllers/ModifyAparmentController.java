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
import java.util.Objects;

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
    private TextField textField;
    private TextArea descriptionTextArea;
    @FXML
    private Button updateHouse;
    @FXML
    private Button removeHouse;
    @FXML
    VBox pictureUrlsVBox;
    @FXML
    Button morePictureButton;
    @FXML
    Button lessPictureButton;
    private ArrayList<TextField> pictureUrlsTextField;

    public ModifyAparmentController()
    {

    }

    @FXML
    private void initialize()
    {
        String apartmentId = getSession().getApartmentId();
        System.out.println("\n\n\nL'ID dell'appartamento è: "+apartmentId);
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

        TextField pictureUrlTextField = new TextField();
        pictureUrlTextField.setPromptText("Insert picture URL");
        // pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
        pictureUrlsVBox.getChildren().add(pictureUrlTextField);
        pictureUrlsTextField = new ArrayList<>();
        pictureUrlsTextField.add(pictureUrlTextField);
    }

    public void onUpdateHouseButtonClick(ActionEvent actionEvent)
    {
        //Qua deve sparare la query su MONGO per aggiornare i dati dell'appartamento
        String apartmentId = getSession().getApartmentId();
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);
        apartment.setMaxAccommodates(inputAccommodates.getValue());
        apartment.setBathrooms(inputBathrooms.getValue());
        apartment.setDollarPriceMonth(inputPrice.getValue());
        apartment.setDescription(descriptionTextArea.getText());
        // String imageUrl = textField.getText()==null || textField.getText().isBlank() || textField.getText().isEmpty() ? "" : textField.getText();
        ArrayList<String> pictureUrls = new ArrayList<>();
        for (TextField pictureUrlTextField : pictureUrlsTextField) {
            if(pictureUrlTextField.getText() != null && !pictureUrlTextField.getText().isEmpty() && !pictureUrlTextField.getText().isBlank())
                pictureUrls.add(pictureUrlTextField.getText());
        }
        apartment.setImageURL(pictureUrls);
        apartment.setId(getSession().getApartmentId());

        if(getMongoConnectionManager().updateApartment(apartment))
        {
            System.out.println("\n\n\nCasa modificata correttamente"+apartment.toString());
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
            if(!getRedisConnectionManager().isApartmentReserved(apartmentId))
            {
                // non ci sono prenotazioni attive, si puo eliminare la casa
                if(getMongoConnectionManager().removeApartment(apartmentId, getSession().getUser().getEmail()))
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
        alert.setContentText(s);

        ButtonType okButton = new ButtonType("OK");
        alert.getButtonTypes().setAll(okButton);

        alert.setOnCloseRequest(event -> {
            // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
            //super.refreshWindow();
        });

        // Mostra la finestra di dialogo
        alert.showAndWait();
    }

    @FXML
    protected void onMorePictureButtonClick() {
        if(pictureUrlsTextField.size() <= 5) {
            TextField pictureUrlTextField = new TextField();
            //pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
            pictureUrlTextField.setPromptText("Insert picture URL");
            pictureUrlsVBox.getChildren().add(pictureUrlTextField);
            pictureUrlsTextField.add(pictureUrlTextField);
        }
        if(pictureUrlsTextField.size() == 5) {
            morePictureButton.setDisable(true);
        }
        if(!pictureUrlsTextField.isEmpty()) {
            lessPictureButton.setDisable(false);
        }
    }

    @FXML
    protected void onLessPictureButtonClick(){
        if(!pictureUrlsTextField.isEmpty()) {
            pictureUrlsVBox.getChildren().remove(pictureUrlsTextField.get(pictureUrlsTextField.size() - 1));
            pictureUrlsTextField.remove(pictureUrlsTextField.size() - 1);
        }
        if(pictureUrlsTextField.size() < 5) {
            morePictureButton.setDisable(false);
        }
        if(pictureUrlsTextField.isEmpty()) {
            lessPictureButton.setDisable(true);
        }
        //checkFields();
    }

    @FXML
    private void checkFields() {
        //uploadButton.setDisable(Objects.equals(houseNameTextField.getText(), "") || wrongPictureUrls() ||(mapGraphicManager.getLocation() == null));
    }

}
