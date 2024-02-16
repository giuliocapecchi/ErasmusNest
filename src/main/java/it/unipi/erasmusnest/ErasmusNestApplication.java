package it.unipi.erasmusnest;

import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ErasmusNestApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ErasmusNestApplication.class.getResource("login-view.fxml"));
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

    public static class PerformanceEvaluator {

        private final MongoConnectionManager mongoConnectionManager;
        private final RedisConnectionManager redisConnectionManager;

        private PerformanceEvaluator() {
            this.mongoConnectionManager = new MongoConnectionManager();
            this.redisConnectionManager = new RedisConnectionManager();
        }

        private void loginTiming(String type){

            Thread thread = new Thread(() -> {

                AtomicLong totalTime = new AtomicLong();
                int iterations = 200;

                for (int i = 0; i < iterations; i++) {

                    String email = "adriana33@gmail.com";

                    long startTime = System.nanoTime();
                    //User user = getMongoConnectionManager().findUser(randomString);
                    // prendo un numero randomico tra 1 e 10000
                    //getMongoConnectionManager().averagePriceNearCityCenter(randomCity, random.nextInt(10000));
                    //String redisPwd = redisConnectionManager.getPassword(email);
                    // getRedisConnectionManager().getReservedApartments(email);
                    //String mongoPassword = mongoConnectionManager.getPassword(email);

                    if(Objects.equals(type, "redislocal")) {
                        redisConnectionManager.getPasswordForPerformanceEvaluation(email);
                    }else if(Objects.equals(type, "rediscluster")){
                        redisConnectionManager.getPassword(email);
                    }else if(Objects.equals(type, "mongo")){
                        mongoConnectionManager.getPassword(email);
                    }

                    long endTime = System.nanoTime();
                    totalTime.addAndGet((endTime - startTime));
                }

                double averageTime = totalTime.get() / (double) iterations;
                // Converti in millisecondi
                double averageTimeInMs = averageTime / 1_000_000.0;
                System.out.println(type + "> Tempo medio per chiamata: " + averageTimeInMs + " ms");
            });

            thread.start();

        }

        private void passwordUpdateTiming(String type){

            Thread thread = new Thread(() -> {

                AtomicLong totalTime = new AtomicLong();
                int iterations = 200;

                for (int i = 0; i < iterations; i++) {

                    String email = "a@b.cd";
                    String newPassword = "aaaa";

                    long startTime = System.nanoTime();
                    //User user = getMongoConnectionManager().findUser(randomString);
                    // prendo un numero randomico tra 1 e 10000
                    //getMongoConnectionManager().averagePriceNearCityCenter(randomCity, random.nextInt(10000));
                    //String redisPwd = redisConnectionManager.getPassword(email);
                    // getRedisConnectionManager().getReservedApartments(email);
                    //String mongoPassword = mongoConnectionManager.getPassword(email);

                    if(Objects.equals(type, "redislocal")) {
                        redisConnectionManager.updateUserPasswordForPerformanceEvaluation(email, newPassword);
                    }else if(Objects.equals(type, "rediscluster")){
                        //redisConnectionManager.getPassword(email);
                    }else if(Objects.equals(type, "mongo")){
                        mongoConnectionManager.updatePassword(email, newPassword);
                    }

                    long endTime = System.nanoTime();
                    totalTime.addAndGet((endTime - startTime));
                }

                double averageTime = totalTime.get() / (double) iterations;
                // Converti in millisecondi
                double averageTimeInMs = averageTime / 1_000_000.0;
                System.out.println(type + "> Tempo medio per chiamata: " + averageTimeInMs + " ms");
            });

            thread.start();

        }


        public static void main(String[] args) {
            PerformanceEvaluator pe = new PerformanceEvaluator();
            /*pe.loginTiming("rediscluster");
            pe.loginTiming("mongo");
            pe.loginTiming("redislocal");*/
            pe.passwordUpdateTiming("mongo");
            pe.passwordUpdateTiming("redislocal");
        }

    }
}