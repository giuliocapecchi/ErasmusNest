package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import it.unipi.erasmusnest.graphicmanagers.RatingGraphicManager;
import it.unipi.erasmusnest.graphicmanagers.ReservationGraphicManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class ApartmentController extends Controller{

    @FXML
    private WebView webView;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button confirmButton;
    @FXML
    private HBox firstHBox;
    @FXML
    private HBox secondHBox;
    @FXML
    private VBox rightVBox;
    @FXML
    private VBox leftVBox;
    @FXML
    private VBox rightFirstVBox;
    @FXML
    private VBox leftFirstVBox;
    @FXML
    private VBox centerVBox;
    @FXML
    private Label nameLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Text infoText;
    @FXML
    private Text hostEmail;
    @FXML
    private ImageView ratingImage1;
    @FXML
    private ImageView ratingImage2;
    @FXML
    private ImageView ratingImage3;
    @FXML
    private ImageView ratingImage4;
    @FXML
    private ImageView ratingImage5;
    @FXML
    private Button reviewsButton;
    @FXML
    private Button loginButton;
    @FXML
    private TextFlow loginMessage;

    private Apartment apartment;
    private ReservationGraphicManager reservationGraphicManager;
    private boolean reservationsLoaded;

    @FXML
    private void initialize() {

        apartment = getMongoConnectionManager().getApartment(getSession().getApartmentId());
        if(apartment==null)
        {
            // If apartment is null seems that the apartment has been removed from Mongo
            // and so need to be removed also from Neo4j
            if(getNeo4jConnectionManager().removeApartment(getSession().getApartmentId()))
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Apartment not available");

                // Aggiungi un pulsante "OK"
                ButtonType okButton = new ButtonType("Back to search");
                alert.getButtonTypes().setAll(okButton);

                // Gestisci l'azione del pulsante "OK"
                alert.setOnCloseRequest(event -> {
                    // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
                    cleanAverageRatingInSession();
                    super.changeWindow("apartments");
                });

                // Mostra la finestra di dialogo
                alert.showAndWait();
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Impossible to remove the apartment from Neo4j");

                // Aggiungi un pulsante "OK"
                ButtonType okButton = new ButtonType("Back to search");
                alert.getButtonTypes().setAll(okButton);

                // Gestisci l'azione del pulsante "OK"
                alert.setOnCloseRequest(event -> {
                    // Qui puoi aggiungere il codice per reindirizzare a un'altra pagina
                    cleanAverageRatingInSession();
                    super.changeWindow("apartments");
                });

                // Mostra la finestra di dialogo
                alert.showAndWait();
            }
        }
        else
        {
            apartment.setAverageRating(getSession().getApartmentAverageRating());

            MapGraphicManager mapGraphicManager = new MapGraphicManager(webView, apartment.getLocation());
            mapGraphicManager.setLocationOnMap();
            mapGraphicManager.loadMap();
            double ratio = 0.5; // 50% of the width
            firstHBox.prefHeightProperty().bind(centerVBox.heightProperty().multiply(ratio));
            secondHBox.prefHeightProperty().bind(centerVBox.heightProperty().multiply(ratio));

            rightFirstVBox.prefWidthProperty().bind(firstHBox.widthProperty().multiply(ratio));
            leftFirstVBox.prefWidthProperty().bind(firstHBox.widthProperty().multiply(ratio));

            rightVBox.prefWidthProperty().bind(secondHBox.widthProperty().multiply(ratio));
            leftVBox.prefWidthProperty().bind(secondHBox.widthProperty().multiply(ratio));

            nameLabel.setText(apartment.getName());
            Image image = new Image(apartment.getImageURL(), true);

            imageView.setImage(image);
            imageView.setSmooth(true);
            // make the image view always fit the height of the parent
            imageView.fitWidthProperty().bind(leftFirstVBox.widthProperty().multiply(0.8));
            String information = apartment.getDescription() + "\n" +
                    "Accommodates: " + apartment.getMaxAccommodates() + "\n" +
                    "Price per month: " + apartment.getDollarPriceMonth() + "$\n";
            infoText.setText(information);
            hostEmail.setText(apartment.getHostEmail());


            System.out.println(">>> "+getSession().getApartmentId());

            if(apartment.getAverageRating() != null) {
                ratingImage1.fitHeightProperty().bind(leftFirstVBox.heightProperty());
                ratingImage2.fitHeightProperty().bind(leftFirstVBox.heightProperty());
                ratingImage3.fitHeightProperty().bind(leftFirstVBox.heightProperty());
                ratingImage4.fitHeightProperty().bind(leftFirstVBox.heightProperty());
                ratingImage5.fitHeightProperty().bind(leftFirstVBox.heightProperty());

                ArrayList<ImageView> ratingImages = new ArrayList<>();
                ratingImages.add(ratingImage1);
                ratingImages.add(ratingImage2);
                ratingImages.add(ratingImage3);
                ratingImages.add(ratingImage4);
                ratingImages.add(ratingImage5);

                RatingGraphicManager ratingGraphicManager = new RatingGraphicManager(ratingImages, ratingImages.size());
                ratingGraphicManager.showRating(apartment.getAverageRating());
                if(apartment.getAverageRating() == 0){
                    reviewsButton.setVisible(false);
                }
            }

            reservationGraphicManager = new ReservationGraphicManager(startDatePicker, endDatePicker, confirmButton,
                    getSession(), getRedisConnectionManager(), apartment.getMaxAccommodates());
            reservationsLoaded = false;
            startDatePicker.setOnMousePressed(event -> onStartDatePickerFirstClick());

            if(!getSession().isLogged()){
                showErrorMessage("Login required", loginMessage);
            }else{
                loginButton.setVisible(false);
            }
        }
    }

    private void onStartDatePickerFirstClick(){
        if(!reservationsLoaded){
            reservationGraphicManager.loadReservations();
            reservationsLoaded = true;
        }
    }

    @FXML
    protected void onShowReviewsButtonClick() {
        getSession().setNextWindowName("apartment");
        cleanAverageRatingInSession();
        super.changeWindow("reviews");
    }

    @FXML
    protected void onContactHostButtonClick() {
        getSession().setOtherProfileMail(hostEmail.getText());
        cleanAverageRatingInSession();
        super.changeWindow("profile");
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {
        getSession().setNextWindowName("apartment");
        cleanAverageRatingInSession();
        super.changeWindow("login");
    }

    @FXML
    protected void onConfirmButtonClick(){
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if(startDate != null && endDate != null) {

            int startYear = startDate.getYear();
            int startMonth = startDate.getMonthValue();

            Period period = Period.between(startDate, endDate);
            int numberOfMonths = (period.getMonths() + period.getYears() * 12) + 1;

            String userEmail = getSession().getUser().getEmail();
            String houseId = String.valueOf(getSession().getApartmentId());

            getRedisConnectionManager().addReservation(userEmail, houseId, String.valueOf(startYear), String.valueOf(startMonth), String.valueOf(numberOfMonths), getSession().getCity(), apartment.getImageURL());

            cleanAverageRatingInSession();
            super.changeWindow("myreservations");
        }
    }

    public void onGoBackButtonClick() {
        cleanAverageRatingInSession();
        changeWindow(getSession().getNextWindowName());
    }

    private void cleanAverageRatingInSession(){
        getSession().setApartmentAverageRating(null);
    }
}
