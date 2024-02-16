package it.unipi.erasmusnest.controllers;

import it.unipi.erasmusnest.dbconnectors.Neo4jConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class FollowersController extends Controller{

    @FXML
    private VBox leftFirstVBox;
    @FXML
    private VBox rightFirstVBox;
    @FXML
    private Label followersNumberLabel;
    @FXML
    private Label followsNumberLabel;

    @FXML
    private void initialize()
    {

        String email = getSession().getUser().getEmail();
        try
        {
            Neo4jConnectionManager neo4jConnectionManager = getNeo4jConnectionManager();
            List<String> followers = neo4jConnectionManager.getFollowersEmail(email);
            List<String> follows = neo4jConnectionManager.getFollowsEmail(email);
            followsNumberLabel.setText(follows.size() + "");
            followersNumberLabel.setText(followers.size() + "");
            for(String followerMail : followers)
            {
                Button button = new Button(followerMail);
                button.setOnAction(this::onUserPageButtonClick);
                button.setPrefWidth(150); // Imposta la larghezza preferita a 100 pixel
                button.setPrefHeight(40); // Imposta l'altezza preferita a 40 pixel
                Button followBack = new Button("Follow Back");
                followBack.setOnAction( event -> {
                    onFollowerButtonClick(followBack,followerMail);
                });
                followBack.setPrefWidth(100); // Imposta la larghezza preferita a 100 pixel
                followBack.setPrefHeight(40); // Imposta l'altezza preferita a 40 pixel
                followBack.setText(follows.contains(followerMail) ? "Stop Follow" : "Follow Back");
                HBox buttonContainer = new HBox(button, followBack);
                leftFirstVBox.getChildren().add(buttonContainer);
            }
            for(String followMail : follows)
            {
                Button button = new Button(followMail);
                button.setOnAction(this::onUserPageButtonClick);
                button.setPrefWidth(150); // Imposta la larghezza preferita a 100 pixel
                button.setPrefHeight(40); // Imposta l'altezza preferita a 40 pixel

                Button followButton = new Button("Stop Follow");
                followButton.setOnAction( event -> {
                    onFollowerButtonClick(followButton,followMail);
                });
                followButton.setPrefWidth(100); // Imposta la larghezza preferita
                followButton.setPrefHeight(40); // Imposta l'altezza preferita
                // Crea un HBox per contenere entrambi i pulsanti
                HBox buttonContainer = new HBox(button, followButton);

                rightFirstVBox.getChildren().add(buttonContainer);
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }

    }

    private void onUserPageButtonClick(ActionEvent actionEvent)
    {
        getSession().setOtherProfileMail(((Button)actionEvent.getSource()).getText());
        super.changeWindow("profile");
    }

    private void onFollowerButtonClick(Button followButton, String otherEmail)
    {
        Neo4jConnectionManager neo4jConnectionManager = getNeo4jConnectionManager();
        String email = getSession().getUser().getEmail();
        if(followButton.getText().equals("Follow Back"))
        {
            neo4jConnectionManager.addFollow(email, otherEmail);
            followButton.setText("Stop Follow");
        }
        else
        {
            neo4jConnectionManager.removeFollow(email, otherEmail);
            followButton.setText("Follow Back");
        }
        super.refreshWindow();
    }


    public void onGoBackButtonClick(ActionEvent actionEvent)
    {
        super.changeWindow("myProfile");
    }
}
