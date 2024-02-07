package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.graphicmanagers.MapGraphicManager;
import it.unipi.erasmusnest.graphicmanagers.RatingGraphicManager;
import it.unipi.erasmusnest.graphicmanagers.ReservationGraphicManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import org.controlsfx.control.InfoOverlay;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApartmentController extends Controller{

    @FXML
    HBox reviewHBox;
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
    private BorderPane leftFirstBorderPane;
    @FXML
    private VBox centerVBox;
    @FXML
    private Label nameLabel;
    @FXML
    private InfoOverlay imageOverlay;
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
    @FXML
    private Button likeButton;
    @FXML
    private VBox suggestedApartmentsVBox;
    @FXML
    private Button removeButton;
    @FXML
    private Label suggestionsLabel;

    private Apartment apartment;
    private ReservationGraphicManager reservationGraphicManager;
    private boolean reservationsLoaded;

    private int imageIndex = 0;

    @FXML
    private void initialize() {
        likeButton.setDisable(!getSession().isLogged());
        apartment = getMongoConnectionManager().getApartment(getSession().getApartmentId());
        System.out.println("Apartment: "+apartment);
        if(apartment==null){
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
            removeButton.setVisible(getSession().getUser().isAdmin(getSession().getUser().getEmail()));
            apartment.setAverageRating(getSession().getApartmentAverageRating());

            MapGraphicManager mapGraphicManager = new MapGraphicManager(webView, apartment.getLocation());
            mapGraphicManager.setLocationOnMap();
            mapGraphicManager.loadMap();
            double ratio = 0.5; // 50% of the width
            firstHBox.prefHeightProperty().bind(centerVBox.heightProperty().multiply(ratio));
            secondHBox.prefHeightProperty().bind(centerVBox.heightProperty().multiply(ratio));

            rightFirstVBox.prefWidthProperty().bind(firstHBox.widthProperty().multiply(ratio));
            leftFirstBorderPane.prefWidthProperty().bind(firstHBox.widthProperty().multiply(ratio));
            leftFirstBorderPane.minHeightProperty().bind(super.getRootPane().heightProperty().multiply(ratio));
            leftFirstBorderPane.maxHeightProperty().bind(super.getRootPane().heightProperty().multiply(ratio));
            rightVBox.prefWidthProperty().bind(secondHBox.widthProperty().multiply(ratio));
            leftVBox.prefWidthProperty().bind(secondHBox.widthProperty().multiply(ratio));
            nameLabel.setText(apartment.getName());
            buildImage();
            imageView.fitWidthProperty().bind(leftFirstBorderPane.widthProperty().multiply(0.8));
            String information;
            if(apartment.getDescription()==null || apartment.getDescription().isEmpty()) {
                information = "Accommodates: " + apartment.getMaxAccommodates() + "\n" +
                        "Price per month: " + apartment.getDollarPriceMonth() + "$\n";
            } else {
                information = apartment.getDescription() + "\n" +
                        "Accommodates: " + apartment.getMaxAccommodates() + "\n" +
                        "Price per month: " + apartment.getDollarPriceMonth() + "$\n";
            }

            infoText.setText(information);
            if(apartment.getDescription()!=null && apartment.getDescription().length() > 100){
                infoText.setTextAlignment(TextAlignment.JUSTIFY);
                infoText.wrappingWidthProperty().bind(leftFirstBorderPane.widthProperty().multiply(0.8));
            }else{
                infoText.setTextAlignment(TextAlignment.CENTER);
            }

            hostEmail.setText(apartment.getHostEmail());

            if(apartment.getAverageRating() != null) {
                ratingImage1.fitHeightProperty().bind(leftFirstBorderPane.heightProperty());
                ratingImage2.fitHeightProperty().bind(leftFirstBorderPane.heightProperty());
                ratingImage3.fitHeightProperty().bind(leftFirstBorderPane.heightProperty());
                ratingImage4.fitHeightProperty().bind(leftFirstBorderPane.heightProperty());
                ratingImage5.fitHeightProperty().bind(leftFirstBorderPane.heightProperty());

                ArrayList<ImageView> ratingImages = new ArrayList<>();
                ratingImages.add(ratingImage1);
                ratingImages.add(ratingImage2);
                ratingImages.add(ratingImage3);
                ratingImages.add(ratingImage4);
                ratingImages.add(ratingImage5);

                RatingGraphicManager ratingGraphicManager = new RatingGraphicManager(ratingImages, ratingImages.size());
                ratingGraphicManager.showRating(apartment.getAverageRating());
            }else{
                reviewHBox.getChildren().clear();
                reviewHBox.getChildren().add(reviewsButton);
            }

            reservationGraphicManager = new ReservationGraphicManager(startDatePicker, endDatePicker, confirmButton,
                    getSession(), getRedisConnectionManager(), apartment.getMaxAccommodates());
            reservationsLoaded = false;
            startDatePicker.setOnMousePressed(event -> onStartDatePickerFirstClick());

            if(!getSession().isLogged()){
                showErrorMessage("Login required", loginMessage);
                startDatePicker.setDisable(true);
            }else{
                loginButton.setVisible(false);
                startDatePicker.setDisable(false);
            }
        }
        suggestedApartmentsVBox.setVisible(false);
    }

    private void buildImage(){
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        slideImage();

        imageView.fitHeightProperty().bind(leftFirstBorderPane.heightProperty().multiply(0.9));
        imageView.fitHeightProperty().bind(leftFirstBorderPane.heightProperty().multiply(0.9));

        imageOverlay.setContent(imageView);
        if(apartment.getImageURLs().size() > 1){
            imageOverlay.setText("Click on the image\nto see more images");
            imageView.setOnMouseClicked(event -> {
                slideImage();
            });
            imageView.hoverProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue){
                    imageView.setStyle("-fx-cursor: hand;");
                }else{
                    imageView.setStyle("-fx-cursor: default;");
                }
            });
        }else if(apartment.getImageURLs().size() == 1){
            imageOverlay.setText("Only image available.\n There are no other images for this apartment");
        }else {
            imageOverlay.setText("No images available.\n There are no images for this apartment");
        }

        imageView.setCache(true);
        imageView.setSmooth(true);

    }

    private void slideImage() {
        System.out.println("SLIDE IMAGE "+ imageIndex);
        String noImageAvailablePath = "/media/no_photo_available.png";
        Image image;
        if(!apartment.getImageURLs().isEmpty()) {
            try {
                image = new Image(apartment.getImageURLs().get(imageIndex), true);
            }catch (IllegalArgumentException e){
                image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(noImageAvailablePath)));
            }
            imageIndex = (imageIndex + 1) % apartment.getImageURLs().size();
        } else {
            image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(noImageAvailablePath)));
        }
        imageView.setImage(image);
    }

    @FXML
    private void onLikeButtonClicked(){
        if (getNeo4jConnectionManager().likeApartment(getSession().getApartmentId(), getSession().getUser().getEmail())) {
            seeSuggestedApartments();
            showConfirmationMessage("Like added", likeButton);
        } else {
            seeSuggestedApartments();
            showConfirmationMessage("Already liked, go to My Profile section to delete", likeButton);
        }
        likeButton.setText("Already liked");
        likeButton.setDisable(true);
    }

    private void seeSuggestedApartments() {
        List<Apartment> suggestedApartments = getNeo4jConnectionManager().getSuggestedApartments(getSession().getUser().getEmail(),getSession().getCity());
        if(suggestedApartments != null && !suggestedApartments.isEmpty()) {
            suggestionsLabel.setVisible(true);
            suggestionsLabel.setText("Suggested apartments:");
            for (Apartment apartment : suggestedApartments) {
                System.out.println(apartment.getName());
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.setSpacing(10);
                vBox.setPrefWidth(100);
                vBox.setPrefHeight(100);
                vBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-color: white; -fx-padding: 10px;");
                ImageView imageView = new ImageView();
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setImage(new Image(apartment.getImageURLs().get(0)));
                suggestedApartmentsVBox.getChildren().add(imageView);
                Button button = new Button(apartment.getName());
                button.setOnAction(event -> {
                    getSession().setApartmentId(apartment.getId());
                    cleanAverageRatingInSession();
                    super.changeWindow("apartment");
                });
                suggestedApartmentsVBox.getChildren().add(button);
            }
        } else {
            suggestionsLabel.setText("No Suggested apartments found");
        }
        suggestedApartmentsVBox.getChildren().add(suggestionsLabel);
    }

    private void onStartDatePickerFirstClick(){
        if(!reservationsLoaded){
            reservationGraphicManager.loadReservations();
            reservationsLoaded = true;
        }
    }

    @FXML
    protected void onShowReviewsButtonClick() {
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

            getRedisConnectionManager().addReservation(userEmail, houseId, String.valueOf(startYear), String.valueOf(startMonth), String.valueOf(numberOfMonths), getSession().getCity(), apartment.getImageURLs().get(0));

            cleanAverageRatingInSession();
            super.changeWindow("myreservations");
        }
    }

    public void onGoBackButtonClick() {
        cleanAverageRatingInSession();
        System.out.println(getPreviousWindowName());
        super.backToPreviousWindow();
        //super.changeWindow("apartment", "apartments");

    }

    private void cleanAverageRatingInSession(){
        getSession().setApartmentAverageRating(null);
    }

    private void showConfirmationMessage(String message, Button likeButton) {
        PopOver popOver = new PopOver();
        Label label = new Label(message);
        label.setStyle("-fx-padding: 10px;");
        popOver.setContentNode(label);
        popOver.setDetachable(false);
        popOver.setAutoHide(true);
        popOver.show(likeButton);
    }

    @FXML
    protected void onRemoveButtonClicked(ActionEvent actionEvent) {
        boolean remove = new AlertDialogGraphicManager("Delete confirmation","Are you sure you want to remove this apartment?\n",
                "You will not be able to recover it","confirmation").showAndGetConfirmation();
        if(remove)
        {
            if(!getRedisConnectionManager().isApartmentReserved(apartment.getId()))
            {
                // non ci sono prenotazioni attive, si puo eliminare la casa
                if(getMongoConnectionManager().removeApartment(apartment.getId(), getSession().getUser().getEmail()))
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
                showConfirmationMessage("Remove failed", removeButton);
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
}
