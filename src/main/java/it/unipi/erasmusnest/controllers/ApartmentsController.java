package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.graphicmanagers.RatingGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApartmentsController extends Controller{

    @FXML
    ScrollPane scrollPane;

    @FXML
    MenuButton changeFiltersButton;

    @FXML
    TextField title;

    @FXML
    TextField pageNumber;

    @FXML
    VBox apartmentsVBox = new VBox();

    @FXML
    Button nextPageButton;

    @FXML
    Button previousPageButton;

    private Integer page;

    private Integer selectedFilter;

    public ApartmentsController() {}

    @FXML
    private void initialize() {
        selectedFilter = getSession().getCurrent_filter();
        page = getSession().getCurrent_page();
        System.out.println("ApartmentsController initialize");
        System.out.println("Session:city = " + getSession().getCity());
        title.setText("Apartments found in " + getSession().getCity());
        if(page==1) {
            previousPageButton.setVisible(false);
            previousPageButton.setManaged(false);
        }
        apartmentsVBox.maxWidthProperty().bind(super.getRootPane().widthProperty());
        printApartments();
    }

    @FXML
    protected void onBackButtonPressed() {
        changeWindow("homepage");
    }

    @FXML
    void goToNextPage() {
        System.out.println("Going to next page...");
        page++;
        if(page==2){
            previousPageButton.setVisible(true);
            previousPageButton.setManaged(true);
        }
        printApartments();
    }

    @FXML
    void goToPreviousPage() {
        System.out.println("goToPreviousPage");
        page--;
        if(page==1){
            previousPageButton.setVisible(false);
            previousPageButton.setManaged(false);
        }
        printApartments();
    }


    /**
     * print the apartments with the selected filter. If no filter is selected, print all the apartments. Uses global variables page, elementsPerPage and selectedFilter
     */
    private void printApartments(){
        System.out.println("filter : "+selectedFilter);
        int elementsPerPage = 10;
        if(selectedFilter==0){
            changeFiltersButton.setText("Filter by");
        }
        if(selectedFilter==1){
            changeFiltersButton.setText("Positive reviews first");
        }else if(selectedFilter==2) {
            changeFiltersButton.setText("Most reviews first");
        }
        List <Apartment> apartments =  getNeo4jConnectionManager().getApartmentsInCity(getSession().getCity(),page, elementsPerPage,selectedFilter);
        scrollPane.setVvalue(0.0);
        cancelImageLoading();
        apartmentsVBox.getChildren().clear();
        if(apartments == null || apartments.isEmpty()){
            System.out.println("No apartments found in :"+getSession().getCity());
            printNoMoreApartmentsMessage();
            nextPageButton.setVisible(false);
            nextPageButton.setManaged(false);
            return;
        }

        for (Apartment apartment : apartments){
            HBox apartmentHBox = new HBox();
            apartmentHBox.setStyle("-fx-border-color: #956800; -fx-border-width: 4;"); // Add border to each HBox

            // HBox height
            double HBoxHeightRatio = 0.3;
            // Calculate width proportions for each cell inside the HBox
            double nameWidthRatio = 0.4; // 40% of the width
            double ratingWidthRatio = 0.2; // 20% of the width
            double imageWidthRatio = 0.4; // 40% of the width

            // In questo modo rendo gli HBox ad altezza fissa, pari al 30% dell'altezza della finestra //TODO: @jacopoNiccolai + ho usato un Vbox per metterci dentro la imageView
            apartmentHBox.minHeightProperty().bind(super.getRootPane().heightProperty().multiply(HBoxHeightRatio));
            apartmentHBox.maxHeightProperty().bind(super.getRootPane().heightProperty().multiply(HBoxHeightRatio));

            // Apartment name
            Label nameLabel = new Label(apartment.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            nameLabel.setAlignment(Pos.CENTER_LEFT);
            nameLabel.prefWidthProperty().bind(apartmentHBox.widthProperty().multiply(nameWidthRatio));
            nameLabel.setWrapText(true);

            // Apartment page button
            Button apartmentPageButton = new Button("View apartment page");
            apartmentPageButton.setOnAction(event -> {
                cancelImageLoading();
                getSession().setApartmentId(apartment.getId());
                System.out.println("Apartment id: "+apartment.getId());
                getSession().setApartmentAverageRating(apartment.getAverageRating());
                getSession().setCurrent_filter(selectedFilter);
                getSession().setCurrent_page(page);
                changeWindow("apartment");
            });

            apartmentPageButton.setTextFill(Color.web("#03612a"));
            apartmentPageButton.setMaxWidth(Double.MAX_VALUE);
            BorderPane nameBorderPane= new BorderPane();
            nameBorderPane.setCenter(nameLabel);
            nameBorderPane.setBottom(apartmentPageButton);
            BorderPane.setMargin(nameLabel, new Insets(10, 10, 10, 10));
            BorderPane.setMargin(apartmentPageButton, new Insets(10, 10, 10, 10));
            BorderPane.setAlignment(nameLabel, Pos.CENTER);
            BorderPane.setAlignment(apartmentPageButton, Pos.CENTER);

            // Average rating Box
            VBox ratingVBox = new VBox();
            ratingVBox.setAlignment(Pos.CENTER);

            Label ratingLabel = new Label( "Average: " + apartment.getAverageRating().toString()+"\nReviews: "+apartment.getNumberOfReviews().toString());
            ratingLabel.setStyle("-fx-font-size: 18px;");
            ratingLabel.setAlignment(Pos.CENTER);
            ratingLabel.setMaxWidth(Double.MAX_VALUE);
            ratingLabel.prefWidthProperty().bind(apartmentHBox.widthProperty().multiply(ratingWidthRatio));
            ratingLabel.setWrapText(true);

            HBox ratingHBox = new HBox();
            HBox.setMargin(ratingHBox, new Insets(5));

            if(apartment.getAverageRating() != null){
                ratingHBox.prefWidthProperty().bind(apartmentHBox.widthProperty().multiply(ratingWidthRatio));
                ratingHBox.setAlignment(Pos.CENTER);
                ArrayList<ImageView> ratingImages = new ArrayList<>();
                for(int i=0;i<5;i++){
                    ImageView ratingImage = new ImageView();
                    ratingImage.setFitHeight(20);
                    ratingImage.setFitWidth(20);
                    ratingImage.setPreserveRatio(true);
                    ratingImages.add(ratingImage);
                    ratingHBox.getChildren().add(ratingImage);
                }
                RatingGraphicManager ratingGraphicManager = new RatingGraphicManager(ratingImages, ratingImages.size());
                ratingGraphicManager.showRating(apartment.getAverageRating());
            }
            ratingVBox.getChildren().addAll(ratingLabel,ratingHBox);


            // Image Box
            VBox imageVBox = new VBox();
            ImageView imageView = new ImageView();
            try {
                Image image = new Image(apartment.getImageURLs().get(0), true); //true let the application continue without waiting for the image to fully load
                imageView.setImage(image);
            } catch (Exception e) {
                String imagePath = "/media/no_photo_available.png"; // Path inside the classpath
                imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            }
            imageView.fitHeightProperty().bind(apartmentHBox.heightProperty().multiply(0.9));
            imageView.setPreserveRatio(true);
            // aggiungo l'immagine al VBox
            imageVBox.getChildren().add(imageView);
            imageVBox.setAlignment(Pos.CENTER);
            imageVBox.prefWidthProperty().bind(apartmentHBox.widthProperty().multiply(imageWidthRatio));
            HBox.setMargin(imageVBox, new Insets(5));

            // Adding elements to the horizontal box
            apartmentHBox.getChildren().addAll(nameBorderPane, ratingVBox, imageVBox);

            // Adding the apartment entry to the main VBox
            apartmentsVBox.getChildren().add(apartmentHBox);
            pageNumber.setText("Page Number: "+page.toString());
        }
        if(apartments.size() < elementsPerPage){
            // non ci sono appartamenti
            printNoMoreApartmentsMessage();
            nextPageButton.setVisible(false);
            nextPageButton.setManaged(false);
        }else{
            nextPageButton.setVisible(true);
            nextPageButton.setManaged(true);
        }
    }

    private void printNoMoreApartmentsMessage() {
        TextArea textArea = new TextArea("Apologies, there aren't other apartments available in " + getSession().getCity() + ".");
        textArea.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");
        textArea.setEditable(false); // Per evitare che l'utente modifichi il testo
        textArea.setWrapText(true);
        textArea.setMaxHeight(50);
        apartmentsVBox.getChildren().add(textArea);
        VBox.setMargin(textArea, new javafx.geometry.Insets(5.0, 5.0, 5.0, 5.0));
        ImageView logoImageView = new ImageView();
        logoImageView.fitWidthProperty().bind(super.getRootPane().widthProperty().multiply(0.2));
        logoImageView.setPickOnBounds(true);
        logoImageView.setPreserveRatio(true);
        logoImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/media/logo.png"))));
        VBox.setMargin(logoImageView, new javafx.geometry.Insets(5.0, 5.0, 5.0, 5.0));
        logoImageView.setSmooth(true); // Per migliorare la qualità dell'immagine ridimensionata
        apartmentsVBox.getChildren().add(logoImageView);
    }


    /**
     * Cancels the asynchronous loading of images in all ImageView elements
     * within HBox containers inside the VBox container.
     * This method interrupts the background loading of images
     * if they are still in the process of loading.
     */
    private void cancelImageLoading() {
        ObservableList<Node> apartmentHBoxList = apartmentsVBox.getChildren();
        for (Node node : apartmentHBoxList) {
            if (node instanceof HBox) {
                HBox apartmentHBox = (HBox) node;
                ObservableList<Node> imageViewList = apartmentHBox.getChildren();

                for (Node imageViewNode : imageViewList) {
                    if (imageViewNode instanceof ImageView) {
                        ImageView imageView = (ImageView) imageViewNode;
                        imageView.getImage().cancel();
                    }
                }
            }
        }
    }


    public void handleOption1() {
        selectedFilter = 1;
        page = 1;
        System.out.println(selectedFilter);
        printApartments();
    }

    public void handleOption2() {
        selectedFilter = 2;
        page = 1;
        System.out.println(selectedFilter);
        printApartments();
    }

    public void handleDefaultOption() {
        selectedFilter=0;
        page = 1;
        System.out.println(selectedFilter);
        printApartments();
    }
}
