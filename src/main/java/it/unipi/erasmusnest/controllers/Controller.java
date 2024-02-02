package it.unipi.erasmusnest.controllers;

import com.dlsc.gemsfx.EmailField;
import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.dbconnectors.Neo4jConnectionManager;
import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;
import it.unipi.erasmusnest.model.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.util.Objects;

public class Controller implements Validator{

    // note that this is the mandatory root pane name for all the fxml files
    // managed by controllers that extend this class
    @FXML
    private StackPane rootPane;
    private static Session session = new Session();
    private static String actualWindowName;
    private static final RedisConnectionManager redisConnectionManager = new RedisConnectionManager();
    private static final MongoConnectionManager mongoConnectionManager = new MongoConnectionManager();
    private static final Neo4jConnectionManager neo4jConnectionManager = new Neo4jConnectionManager();

    public Controller() {
    }

    // TODO add actualWindowName that becomes the previous window name
    protected void changeWindow(String nextWindowName) {

        try {
            actualWindowName = nextWindowName;
            StackPane pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/it/unipi/erasmusnest/" + nextWindowName + "-view.fxml")));
            rootPane.getChildren().setAll(pane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected void setActualWindowName(String actualWindowName) {
        Controller.actualWindowName = actualWindowName;
    }

    protected String getActualWindowName() {
        return actualWindowName;
    }

    RedisConnectionManager getRedisConnectionManager() {
        return redisConnectionManager;
    }

    MongoConnectionManager getMongoConnectionManager() {
        return mongoConnectionManager;
    }

    Neo4jConnectionManager getNeo4jConnectionManager() {
        return neo4jConnectionManager;
    }

    static Session getSession() {
        return session;
    }

    protected StackPane getRootPane() {
        return rootPane;
    }

    @Override
    public boolean isTextFieldValid(TextField textField) {
        boolean textValid = true;
        String password = textField.getText();
        if(password.length() < 4 || password.length() > 20){
            textValid = false;
        }
        return textValid;
    }

    @Override
    public boolean isEmailFieldValid(EmailField emailField) {
        boolean emailValid = true;
        String email = emailField.getEmailAddress();
        if(!emailField.isValid()) {
            emailValid = false;
        }
        if(email != null) {
            if(email.isBlank()) {
                emailValid = false;
            }
        }else {
            emailValid = false;
        }
        return emailValid;
    }

    protected void showErrorMessage(String message, TextFlow errorTextFlow) {
        errorTextFlow.getChildren().clear();
        Text text = new Text(message);
        text.setFill(Color.RED);
        text.setStyle("-fx-font-style: italic");
        errorTextFlow.getChildren().add(text);
    }

}
