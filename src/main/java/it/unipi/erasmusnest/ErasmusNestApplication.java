package it.unipi.erasmusnest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ErasmusNestApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //FXMLLoader fxmlLoader = new FXMLLoader(ErasmusNestApplication.class.getResource("login-view.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(ErasmusNestApplication.class.getResource("heatmap.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700,550);
        stage.setMinWidth(700);
        stage.setMinHeight(550);
        stage.setTitle("Erasmus Nest");
        stage.getIcons().add(new Image("file:src/main/resources/media/logo.png"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}