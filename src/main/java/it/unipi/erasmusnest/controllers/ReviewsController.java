package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.model.Review;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class ReviewsController extends Controller{

    @FXML
    HBox hboxTitle;

    @FXML
    ScrollPane scrollPane;

    @FXML
    Button previousPageButton;

    @FXML
    TextField pageNumber;

    @FXML
    Button nextPageButton;

    @FXML
    VBox reviewsVBox;

    @FXML
    MenuButton changeFiltersButton;

    @FXML
    TextField title;
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
            System.out.println("Session:apartment = " + getSession().getApartmentId());
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
        if(Objects.equals(getPreviousWindowName(), "profile")) {
            reviews = getNeo4jConnectionManager().getReviewsForUser(getSession().getOtherProfileMail(),page, elementsPerPage); //TODO ; aggiungiamo un filtro anche qui o no?
        }else{
            reviews = getNeo4jConnectionManager().getReviewsForApartment(getSession().getApartmentId(),page, elementsPerPage,selectedFilter);
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
            double ratingWidthRatio = 0.2; // 20% of the width
            double commentsWidthRatio = 0.6; // 60% of the width
            double timestampWidthRatio = 0.2; // 20% of the width

            // Apartment name
            Label emailLabel = new Label(review.getUserEmail());
            emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            emailLabel.setAlignment(Pos.CENTER_LEFT);
            emailLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(emailWidthRatio));
            emailLabel.setWrapText(true);

            // RatingLabel
            Label ratingLabel = new Label("Rating: " + review.getRating());
            ratingLabel.setStyle("-fx-font-size: 18px;");
            ratingLabel.setAlignment(Pos.CENTER);
            ratingLabel.setMaxWidth(Double.MAX_VALUE);
            ratingLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(ratingWidthRatio));
            ratingLabel.setWrapText(true);

            // Comments
            Label commentsLabel = new Label(review.getComments());
            commentsLabel.setStyle("-fx-font-size: 18px;");
            commentsLabel.setAlignment(Pos.CENTER);
            commentsLabel.setMaxWidth(Double.MAX_VALUE);
            commentsLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(commentsWidthRatio));
            commentsLabel.setWrapText(true);

            // Timestamp
            Label timestampLabel = new Label(review.getTimestamp().toString());
            System.out.println("Timestamp: "+review.getTimestamp().toString());
            timestampLabel.setStyle("-fx-font-size: 12px;");
            timestampLabel.setAlignment(Pos.CENTER);
            timestampLabel.setMaxWidth(Double.MAX_VALUE);
            timestampLabel.prefWidthProperty().bind(reviewHBox.widthProperty().multiply(timestampWidthRatio));
            timestampLabel.setWrapText(true);

            // Adding elements to the horizontal box
            reviewHBox.getChildren().addAll(emailLabel, ratingLabel, commentsLabel, timestampLabel);
            reviewHBox.setAlignment(Pos.CENTER);
            HBox.setMargin(emailLabel, new Insets(5.0, 5.0, 5.0, 5.0));
            HBox.setMargin(ratingLabel, new Insets(5.0, 5.0, 5.0, 5.0));
            HBox.setMargin(commentsLabel, new Insets(5.0, 5.0, 5.0, 5.0));
            HBox.setMargin(timestampLabel, new Insets(2.0, 2.0, 2.0, 2.0));

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
