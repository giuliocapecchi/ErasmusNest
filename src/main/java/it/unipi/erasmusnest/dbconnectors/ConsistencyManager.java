package it.unipi.erasmusnest.dbconnectors;

import it.unipi.erasmusnest.model.Apartment;

import java.util.PriorityQueue;
import java.util.Queue;

public class ConsistencyManager {

    private final MongoConnectionManager mongoConnectionManager;
    private final Neo4jConnectionManager neo4jConnectionManager;

    private static final Queue<Thread> operationsQueue = new PriorityQueue<>();

    public ConsistencyManager(MongoConnectionManager mongoConnectionManager, Neo4jConnectionManager neo4jConnectionManager) {
        this.mongoConnectionManager = mongoConnectionManager;
        this.neo4jConnectionManager = neo4jConnectionManager;
    }

    public void addApartmentOnNeo4J(Apartment apartment) {
        Thread thread = new Thread(() -> {
            // sleep for 10 seconds
            /*try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            System.out.println("\t\t\t\t\t\t*** THREAD addApartmentOnNeo STARTED ***");
            boolean added = neo4jConnectionManager.addApartment(apartment);
            if (added)
                System.out.println("\t\t\t\t\t\t*** APARTMENT ADDED ON NEO4J ***");
            else
                System.out.println("\t\t\t\t\t\t*** APARTMENT NOT ADDED ON NEO4J ***");

            if(!added){
                System.out.println("\t\t\t\t\t\t*** -> ROLLBACK ON MONGO ***");
                mongoConnectionManager.removeApartment(apartment.getId(), apartment.getHostName());
            }
            System.out.println("\t\t\t\t\t\t*** THREAD addApartmentOnNeo ENDED ***");
        });

        thread.start();

        //operationsQueue.add(thread);
    }

    public void updateApartmentImageOnNeo4J(String apartmentId, String pictureUrl) {
        Thread thread = new Thread(() -> {
            System.out.println("\t\t\t\t\t\t*** THREAD updateApartmentImageOnNeo STARTED ***");
            boolean updated = neo4jConnectionManager.updateApartment(apartmentId, pictureUrl);
            if (updated)
                System.out.println("\t\t\t\t\t\t*** APARTMENT IMAGE UPDATED ON NEO4J ***");
            else
                System.out.println("\t\t\t\t\t\t*** APARTMENT IMAGE NOT UPDATED ON NEO4J ***");
            // no rollback needed
            System.out.println("\t\t\t\t\t\t*** THREAD updateApartmentImageOnNeo ENDED ***");
        });

        thread.start();

        //operationsQueue.add(thread);
    }

    public void removeApartmentFromNeo4J(String apartmentId) {
        Thread thread = new Thread(() -> {
            System.out.println("\t\t\t\t\t\t*** THREAD removeApartmentFromNeo4J STARTED ***");
            boolean removed = neo4jConnectionManager.removeApartment(apartmentId);
            if (removed)
                System.out.println("\t\t\t\t\t\t*** APARTMENT REMOVED FROM NEO4J ***");
            else
                System.out.println("\t\t\t\t\t\t*** APARTMENT NOT REMOVED FROM NEO4J ***");
            // no rollback needed
            System.out.println("\t\t\t\t\t\t*** THREAD removeApartmentFromNeo4J ENDED ***");
        });

        thread.start();

        //operationsQueue.add(thread);
    }

}
