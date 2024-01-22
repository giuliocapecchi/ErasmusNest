package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Apartment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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

    public ModifyAparmentController()
    {

    }

    @FXML
    private void initialize()
    {
        Long apartmentId = getSession().getApartmentId();
        System.out.println("\n\n\nApartmentId: "+apartmentId);
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);

        inputAccommodates = new Spinner<>();
        inputBathrooms = new Spinner<>();
        inputPrice = new Spinner<>();

        System.out.println("\n\n\nI VALORI ATTUALI SONO (ACCOM,BATH,PRICE): "+apartment.getMaxAccommodates()+" "+apartment.getBathrooms()+" "+apartment.getDollarPriceMonth()+"\n\n\n");

        SpinnerValueFactory<Integer> valoriAccommodates = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,10,apartment.getMaxAccommodates());
        SpinnerValueFactory<Double> valoriBathrooms = new SpinnerValueFactory.DoubleSpinnerValueFactory(1,10,Double.parseDouble(apartment.getBathrooms()),0.5);
        SpinnerValueFactory<Double> valoriPrice = new SpinnerValueFactory.DoubleSpinnerValueFactory(1,1000,apartment.getDollarPriceMonth(),0.5);
        // imposto il generatore di valori possibili
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
        System.out.println("\n\n\n STO ANDANDO A MODIFICARE L'APPARTAMENTO: "+apartmentId+"\n\n\n");
        Apartment apartment = getMongoConnectionManager().getApartment(apartmentId);
        apartment.setMaxAccommodates(inputAccommodates.getValue());
        apartment.setBathrooms(String.valueOf(inputBathrooms.getValue()));
        apartment.setDollarPriceMonth(inputPrice.getValue());
        apartment.setDescription(descriptionTextArea.getText());
        apartment.setImageURL(textField.getText());
        apartment.setId(getSession().getApartmentId());

        if(getMongoConnectionManager().updateApartment(apartment))
        {
            //Print OK message
        }
        else
        {
            //Print error message
        }

    }

    public void onGoBackButtonClick(ActionEvent actionEvent)
    {
        super.changeWindow("myProfile");
    }
}
