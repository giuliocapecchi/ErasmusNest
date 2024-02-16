package it.unipi.erasmusnest;

import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceEvaluator {

    private final MongoConnectionManager mongoConnectionManager;
    private final RedisConnectionManager redisConnectionManager;

    private PerformanceEvaluator() {
        this.mongoConnectionManager = new MongoConnectionManager();
        this.redisConnectionManager = new RedisConnectionManager();
    }

    private void loginTiming(String type){

        // test

        Thread thread = new Thread(() -> {

            AtomicLong totalTime = new AtomicLong();
            int iterations = 200;

            for (int i = 0; i < iterations; i++) {

                String email = "adriana33@gmail.com";

                long startTime = System.nanoTime();

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
        pe.loginTiming("redislocal");
        pe.passwordUpdateTiming("redislocal");
    }

}
