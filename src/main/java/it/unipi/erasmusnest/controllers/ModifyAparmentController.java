package it.unipi.erasmusnest.controllers;

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

public class ModifyAparmentController extends Controller{

    public VBox apartmentVBox;
    public VBox bathroomsVBox;
    public VBox priceVBox;
    public VBox neighborhoodVBox;
    public Spinner<Integer> inputAccommodates;
    public Spinner<Double> inputBathrooms;
    public Spinner<Double> inputPrice;
    public VBox imageVBox;
    public TextField textField;
    public TextArea descriptionTextArea;
    @FXML
    public Button updateHouse;
    @FXML
    public Button goBack;

    public ModifyAparmentController()
    {

    }

    @FXML
    private void initialize()
    {
        Long apartmentId = getSession().getApartmentId();
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);

        inputAccommodates = new Spinner<>();
        inputBathrooms = new Spinner<>();
        inputPrice = new Spinner<>();

        SpinnerValueFactory<Integer> valoriAccommodates = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,apartment.getMaxAccommodates());
        // Retrive number of bathrooms from the String field
        String bathStr = apartment.getBathrooms();
        String[] bathSplit = bathStr.split("\\s+");
        String bathsNumber = bathSplit[0];
        String bathroomsNumber = Double.parseDouble(bathsNumber) == 1.0 ? bathsNumber + " bath" : bathsNumber + " baths";
        SpinnerValueFactory<Double> valoriBathrooms = new SpinnerValueFactory.DoubleSpinnerValueFactory(1,10,Double.parseDouble(bathsNumber),0.5);
        SpinnerValueFactory<Double> valoriPrice = new SpinnerValueFactory.DoubleSpinnerValueFactory(1,1000,apartment.getDollarPriceMonth(),0.5);
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
        textField.setText(apartment.getImageURL());
        imageVBox.getChildren().add(textField);
        System.out.println("Apartment: "+apartment.toString());
    }

    public void onUpdateHouseButtonClick(ActionEvent actionEvent)
    {
        //Qua deve sparare la query su MONGO per aggiornare i dati dell'appartamento
        Long apartmentId = getSession().getApartmentId();
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);
        apartment.setMaxAccommodates(inputAccommodates.getValue());
        Double bathrooms = inputBathrooms.getValue();
        String bathroomsText = bathrooms == 1.0 ? bathrooms + " bath" : bathrooms + " baths";
        apartment.setBathrooms(bathroomsText);
        apartment.setDollarPriceMonth(inputPrice.getValue());
        apartment.setDescription(descriptionTextArea.getText());
        String imageUrl = textField.getText().isBlank() || textField.getText().isEmpty() ? "" : textField.getText();
        apartment.setImageURL(imageUrl);
        apartment.setId(getSession().getApartmentId());

        if(getMongoConnectionManager().updateApartment(apartment))
        {
            //Print OK message
            showConfirmationMessage("Succesfull update");
        }
        else
        {
            //Print error message
            showConfirmationMessage("Update failed");
        }

    }

    private void showConfirmationMessage(String message) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 15px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        // Mostra il messaggio di conferma
        popOver.show(updateHouse);
    }

    public void onGoBackButtonClick(ActionEvent actionEvent)
    {
        super.changeWindow("myProfile");
    }
}
