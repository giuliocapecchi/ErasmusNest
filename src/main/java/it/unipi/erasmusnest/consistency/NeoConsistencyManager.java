package it.unipi.erasmusnest.consistency;

import it.unipi.erasmusnest.dbconnectors.Neo4jConnectionManager;

public class NeoConsistencyManager extends ConsistencyManager{

    public NeoConsistencyManager(Neo4jConnectionManager neo4jConnectionManager) {
        super(null, null, neo4jConnectionManager);
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

        executeOperation(thread);

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
            if(removed)

                System.out.println("\t\t\t\t\t\t*** THREAD removeApartmentFromNeo4J ENDED ***");
        });

        executeOperation(thread);

        //operationsQueue.add(thread);
    }

}

