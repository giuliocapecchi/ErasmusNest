package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.RatingGraphicManager;
import it.unipi.erasmusnest.model.Review;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReviewsController extends Controller{

    @FXML
    private HBox averageReviewScoreHBox;
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
    private HBox hboxTitle;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button previousPageButton;
    @FXML
    private TextField pageNumber;
    @FXML
    private Button nextPageButton;
    @FXML
    private VBox reviewsVBox;
    @FXML
    private MenuButton changeFiltersButton;
    @FXML
    private TextField title;
    private Integer selectedFilter = 0;

    private Integer page = 1;


    @FXML
    private void initialize() {
        System.out.println("ReviewsController initialize");
        if(Objects.equals(getPreviousWindowName(), "profile")){
            System.out.println("Session:User = " + getSession().getOtherProfileMail());
            title.setText("Reviews for the selected User");
            hboxTitle.getChildren().remove(changeFiltersButton);
        }else{
            System.out.println("Session:apartment = " + getSession().getApartment().getId());
            title.setText("Reviews for the apartment selected");
        }
        previousPageButton.setVisible(false);
        previousPageButton.setManaged(false);
        reviewsVBox.maxWidthProperty().bind(super.getRootPane().widthProperty());
        printReviews();

    }

    public void printReviews() {

        int elementsPerPage = 10;
        List <Review> reviews;
        if(Objects.equals(getPreviousWindowName(), "profile")) { // prendo le recensioni che ha scritto un utente
            reviews = getNeo4jConnectionManager().getReviewsForUser(getSession().getOtherProfileMail(),page, elementsPerPage);
            averageReviewScoreHBox.getChildren().clear();
        }else{ // prendo le recensioni di un appartamento
            reviews = getNeo4jConnectionManager().getReviewsForApartment(getSession().getApartment().getId(),page, elementsPerPage,selectedFilter);
            Double averageRating = getNeo4jConnectionManager().getAverageReviewScore(getSession().getApartment().getId());
            if(averageRating != null){
                ArrayList<ImageView> ratingImages = new ArrayList<>();
                ratingImages.add(ratingImage1);
                ratingImages.add(ratingImage2);
                ratingImages.add(ratingImage3);
                ratingImages.add(ratingImage4);
                ratingImages.add(ratingImage5);
                RatingGraphicManager ratingGraphicManager = new RatingGraphicManager(ratingImages, ratingImages.size());
                ratingGraphicManager.showRating(averageRating);
                getSession().getApartment().setAverageRating(averageRating);
            }
        }

        scrollPane.setVvalue(0.0);
        reviewsVBox.getChildren().clear();
        if(reviews == null || reviews.isEmpty()){
            System.out.println("No reviews found for the desired entity");
            printNoMoreReviewsMessage();
            nextPageButton.setVisible(false);
            nextPageButton.setManaged(false);
            return;
        }

        for (Review review : reviews){
            HBox reviewHBox = new HBox();
            if (review.getRating()<= 2){
                reviewHBox.setStyle("-fx-border-color: #950000; -fx-border-width: 4;"); // Add border to each HBox
            }else if (review.getRating()< 4){
                reviewHBox.setStyle("-fx-border-color: #FFD700; -fx-border-width: 4;");
            }else {
                reviewHBox.setStyle("-fx-border-color: #008000; -fx-border-width: 4;");
            }
            reviewHBox.prefHeightProperty().bind(super.getRootPane().heightProperty().multiply(0.25));


            // Calculate width proportions for each cell
            double emailWidthRatio = 0.2; // 20% of the width
            double ratingWidthRatio = 0.1; // 20% of the width
            double commentsWidthRatio = 0.55; // 60% of the width
            double viewApartmentVBoxWidthRatio = 0.15; // 20% of the width

            String dividerColor = "-fx-border-color: #aeb000;";

            // Apartment name
            VBox apartmentNameVBox = new VBox();
            apartmentNameVBox.setStyle(dividerColor+" -fx-border-width: 0 2px 0 0;");
            apartmentNameVBox.setAlignment(Pos.CENTER);
            Label emailLabel = new Label(review.getUserEmail());
            emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            emailLabel.setAlignment(Pos.CENTER_LEFT);
            emailLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(emailWidthRatio));
            emailLabel.setWrapText(true);
            apartmentNameVBox.getChildren().add(emailLabel);

            // RatingLabel
            Label ratingLabel = new Label("Rating: " + review.getRating());
            ratingLabel.setStyle("-fx-font-size: 16px;");
            ratingLabel.setAlignment(Pos.CENTER);
            ratingLabel.setMaxWidth(Double.MAX_VALUE);
            ratingLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(ratingWidthRatio));
            ratingLabel.setWrapText(true);

            // Comments
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setStyle(dividerColor+" -fx-border-width: 0 2px 0 2px;");
            Label commentsLabel = new Label(review.getComments());
            commentsLabel.setStyle("-fx-font-size: 18px;");
            commentsLabel.setAlignment(Pos.CENTER);
            commentsLabel.setMaxWidth(Double.MAX_VALUE);
            commentsLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(commentsWidthRatio));
            commentsLabel.setWrapText(true);
            scrollPane.setContent(commentsLabel);
            scrollPane.setFitToWidth(true);

            // Timestamp & ViewApartmentButton
            VBox viewApartmentVBox = new VBox();
            Button viewApartmentButton = new Button("View Apartment");
            viewApartmentButton.prefWidthProperty().bind(viewApartmentVBox.widthProperty().multiply(0.9));
            viewApartmentButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
            viewApartmentButton.setOnAction(e -> {
                getSession().getApartment().setId(review.getApartmentId());
                super.changeWindow("apartment");
            });
            Label timestampLabel = new Label(review.getTimestamp().toString());
            timestampLabel.setStyle("-fx-font-size: 12px;");
            timestampLabel.setAlignment(Pos.CENTER);
            timestampLabel.setMaxWidth(Double.MAX_VALUE);
            timestampLabel.setWrapText(true);

            if(getSession().getUser().isAdmin()){
                System.out.println("User is admin");
                Button removeButton = new Button("Remove Review");
                removeButton.setOnAction(e -> {
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("Confirm Review Removal");
                    confirmationAlert.setHeaderText("Confirm Review Removal");
                    confirmationAlert.setContentText("Are you sure you want to remove this review?");
                    Optional<ButtonType> result = confirmationAlert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) { // admin confirmed the removal of the review
                        getNeo4jConnectionManager().removeReview(review);
                        getNeo4jConnectionManager().updateApartmentAverageReviewScore(review.getApartmentId());
                        printReviews();
                    } else { // admin cancelled the removal of the review
                        System.out.println("Review removal declined by admin.");
                    }
                });
                viewApartmentVBox.getChildren().addAll(timestampLabel, viewApartmentButton, removeButton);
            }else{
                System.out.println("User is not admin");
                viewApartmentVBox.getChildren().addAll(timestampLabel, viewApartmentButton);
            }
            viewApartmentVBox.setAlignment(Pos.CENTER);
            viewApartmentVBox.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(viewApartmentVBoxWidthRatio));

            // Adding elements to the horizontal box
            reviewHBox.getChildren().addAll(apartmentNameVBox, ratingLabel, scrollPane, viewApartmentVBox);
            reviewHBox.setAlignment(Pos.CENTER);
            VBox.setMargin(emailLabel, new Insets(5.0, 5.0, 5.0, 5.0));
            HBox.setMargin(ratingLabel, new Insets(5.0, 5.0, 5.0, 5.0));
            VBox.setMargin(commentsLabel, new Insets(5.0, 5.0, 5.0, 5.0));
            VBox.setMargin(viewApartmentButton, new Insets(5.0, 5.0, 5.0, 5.0));
            VBox.setMargin(timestampLabel, new Insets(5.0, 5.0, 5.0, 5.0));

            // Adding the apartment entry to the main VBox
            reviewsVBox.getChildren().add(reviewHBox);
            pageNumber.setText("Page Number: "+page.toString());
        }
        if(reviews.size() < elementsPerPage){
            printNoMoreReviewsMessage();
            nextPageButton.setVisible(false);
            nextPageButton.setManaged(false);;
        }else{
            nextPageButton.setVisible(true);
            nextPageButton.setManaged(true);
        }
    }

    private void printNoMoreReviewsMessage() {
        TextArea textArea = new TextArea("Apologies, there aren't other reviews available.");
        textArea.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        textArea.setEditable(false); // Per evitare che l'utente modifichi il testo
        textArea.setWrapText(true);
        textArea.setMaxHeight(50);
        reviewsVBox.getChildren().add(textArea);
        VBox.setMargin(textArea, new Insets(5.0, 5.0, 5.0, 5.0));
        ImageView logoImageView = new ImageView();
        logoImageView.fitWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.1));
        logoImageView.setPickOnBounds(true);
        logoImageView.setPreserveRatio(true);
        logoImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/media/logo.png"))));
        VBox.setMargin(logoImageView, new Insets(5.0, 5.0, 5.0, 5.0));
        logoImageView.setSmooth(true); // Per migliorare la qualità dell'immagine ridimensionata
        reviewsVBox.getChildren().add(logoImageView);
    }


    public void handleOption1() {
        selectedFilter = 1;
        page = 1;
        System.out.println("Option 1");
        printReviews();
    }

    public void handleOption2() {
        selectedFilter = 2;
        page = 1;
        System.out.println("Option 2");
        printReviews();
    }

    public void handleOption3() {
        selectedFilter = 3;
        page = 1;
        System.out.println("Option 3");
        printReviews();
    }

    public void handleOption4() {
        selectedFilter = 4;
        page = 1;
        System.out.println("Option 4");
        printReviews();
    }

    public void handleDefaultOption(ActionEvent actionEvent) {
        selectedFilter = 0;
        page = 1;
        System.out.println("Default Option");
        printReviews();
    }

    public void onGoBackButtonPressed() {
        super.backToPreviousWindow();
    }

    @FXML
    void goToNextPage(ActionEvent actionEvent) {
        System.out.println("goToNextPage");
        page++;
        if(page==2){
            previousPageButton.setVisible(true);
            previousPageButton.setManaged(true);
        }
        printReviews();
    }

    @FXML
    void goToPreviousPage() {
        System.out.println("goToPreviousPage");
        page--;
        if(page==1){
            previousPageButton.setVisible(false);
            previousPageButton.setManaged(false);
        }
        printReviews();
    }
}
