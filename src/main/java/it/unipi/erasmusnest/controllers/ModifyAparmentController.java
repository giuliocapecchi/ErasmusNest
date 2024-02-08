package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

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
    private Apartment apartment;

    public ModifyAparmentController(){
    }

    @FXML
    private void initialize() {
        String apartmentId = getSession().getApartmentId();
        System.out.println("\n\n\nL'ID dell'appartamento è: " + apartmentId);
        apartment = getMongoConnectionManager().getApartment(apartmentId);
        System.out.println("\n\n\n" + apartment.toString());
        inputAccommodates = new Spinner<>();
        inputBathrooms = new Spinner<>();
        inputPrice = new Spinner<>();

        SpinnerValueFactory<Integer> valoriAccommodates = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, apartment.getMaxAccommodates());
        SpinnerValueFactory<Integer> valoriBathrooms = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, apartment.getBathrooms());
        SpinnerValueFactory<Integer> valoriPrice = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, apartment.getDollarPriceMonth());
        // setting suitable values for the spinners
        inputAccommodates.setValueFactory(valoriAccommodates);
        inputBathrooms.setValueFactory(valoriBathrooms);
        inputPrice.setValueFactory(valoriPrice);
        inputPrice.valueProperty().addListener((obs, oldValue, newValue) -> checkFields());
        inputBathrooms.valueProperty().addListener((obs, oldValue, newValue) -> checkFields());
        inputAccommodates.valueProperty().addListener((obs, oldValue, newValue) -> checkFields());
        priceVBox.getChildren().add(inputPrice);
        apartmentVBox.getChildren().add(inputAccommodates);
        bathroomsVBox.getChildren().add(inputBathrooms);
        // Aggiunta del messaggio di errore per la lunghezza massima della descrizione
        Label maxDescriptionLengthReached = new Label("Maximum description length reached");
        maxDescriptionLengthReached.setStyle("-fx-text-fill: red");
        maxDescriptionLengthReached.setVisible(false);
        // Aggiunta della descrizione
        descriptionTextArea = new TextArea();
        // rendo la text area ad altezza dinamic
        descriptionTextArea.minHeightProperty().bind(super.getRootPane().heightProperty().multiply(0.2));
        descriptionTextArea.setText(apartment.getDescription());
        descriptionTextArea.setWrapText(true); // Abilita il word wrapping per l'area di testo
        descriptionTextArea.onKeyReleasedProperty().set(event -> checkFields());
        descriptionTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1000) {
                descriptionTextArea.setText(oldValue); // Revert al valore precedente se supera 1000 caratteri
                descriptionTextArea.setScrollTop(Double.MAX_VALUE); // rimette lo scroll in fondo
                maxDescriptionLengthReached.setVisible(true);
            } else {
                maxDescriptionLengthReached.setVisible(false);
            }
        });
        neighborhoodVBox.getChildren().add(descriptionTextArea);
        neighborhoodVBox.getChildren().add(maxDescriptionLengthReached);

        //Aggiunta foto:
        pictureUrlsTextField = new ArrayList<>();
        pictureUrlsVBox.setSpacing(5);
        if (apartment.getImageURLs() != null) {
            for (String url : apartment.getImageURLs()) {
                TextField pictureUrlTextField = new TextField();
                pictureUrlTextField.setText(url);
                pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
                pictureUrlsVBox.getChildren().add(pictureUrlTextField);
                pictureUrlsTextField.add(pictureUrlTextField);
            }
        }

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
        //boolean change = false;
        //if(!pictureUrls.get(0).equals(apartment.getImageURLs().get(0)) || apartment.getImageURLs()==null || apartment.getImageURLs().isEmpty())
        //    change = true;
        apartment.setImageURL(pictureUrls);
        System.out.println("\n\n\nIMMAGINI DENTRO ALL'APPARTAMENTO:");
        for (String s : apartment.getImageURLs()) {
            System.out.println(s);
        }
        apartment.setId(getSession().getApartmentId());

        if(getMongoConnectionManager().updateApartment(apartment))
        {
            //if(change)
                if(getNeo4jConnectionManager().updateApartment(apartment.getId(), apartment.getImageURLs().get(0)))
                    System.out.println("\n\n\nCasa modificata correttamente"+apartment.toString());
                else
                    System.out.println("\n\n\nCasa non modificata correttamente"+apartment.toString());
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
                super.changeWindow("myProfile");
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
        super.changeWindow("myProfile");
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
                    super.changeWindow("myProfile");
                }
                else
                {
                    alertDialog("Impossible to remove house");
                }
            }
            else
            {
                showConfirmationMessage("Remove failed. This apartment has active reservations.", removeHouse);
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
            pictureUrlTextField.setPromptText("Insert picture URL");
            pictureUrlTextField.onKeyReleasedProperty().set(event -> checkFields());
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
        checkFields();
    }

    @FXML
    private void checkFields() {
        Integer accommodates = apartment.getMaxAccommodates();
        Integer bathrooms = apartment.getBathrooms();
        Integer price = apartment.getDollarPriceMonth();
        String description = apartment.getDescription();
        ArrayList<String> pictureUrls = apartment.getImageURLs();
        boolean fieldsModified = !(Objects.equals((inputAccommodates).getValue(), accommodates)
                && Objects.equals(inputBathrooms.getValue(), bathrooms)
                && Objects.equals(inputPrice.getValue(), price)
                && Objects.equals(descriptionTextArea.getText(), description));
        if(pictureUrls.size() != pictureUrlsTextField.size()) {
            fieldsModified = true;
        }
        for (int i = 0; i < pictureUrlsTextField.size() && !fieldsModified; i++) {
            if(!pictureUrlsTextField.get(i).getText().isBlank() && !pictureUrlsTextField.get(i).getText().isEmpty()) {
                if(pictureUrls.size() > i) {
                    if(!pictureUrlsTextField.get(i).getText().equals(pictureUrls.get(i))) {
                        fieldsModified = true;
                    }
                } else {
                    fieldsModified = true;
                }
            }

        }
        updateHouse.setDisable(!fieldsModified);
    }

}
