package it.unipi.erasmusnest.consistency;

import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.dbconnectors.Neo4jConnectionManager;
import it.unipi.erasmusnest.model.Apartment;

public class MongoNeoConsistencyManager extends ConsistencyManager{

    public MongoNeoConsistencyManager(MongoConnectionManager mongoConnectionManager, Neo4jConnectionManager neo4jConnectionManager) {
        super(null, mongoConnectionManager, neo4jConnectionManager);
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

        executeOperation(thread);

        //operationsQueue.add(thread);
    }

}
