package it.unipi.erasmusnest.consistency;

import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;

public class RedisMongoConsistencyManager extends ConsistencyManager{

    public RedisMongoConsistencyManager(RedisConnectionManager redisConnectionManager, MongoConnectionManager mongoConnectionManager) {
        super(redisConnectionManager, mongoConnectionManager, null);
    }

    public void updateUserPasswordOnMongo(String email, String newPassword) {
        Thread thread = new Thread(() -> {
            System.out.println("\t\t\t\t\t\t*** THREAD updateUserPasswordOnMongo STARTED ***");
            boolean updated = mongoConnectionManager.updatePassword(email, newPassword);
            if (updated)
                System.out.println("\t\t\t\t\t\t*** PASSWORD UPDATED ON MONGO ***");
            else
                System.out.println("\t\t\t\t\t\t*** PASSWORD NOT UPDATED ON MONGO ***");

            if(updated)
                redisConnectionManager.updateExpirationTimeOnUser(email);

            System.out.println("\t\t\t\t\t\t*** THREAD updateUserPasswordOnMongo ENDED ***");
        });

        executeOperation(thread);

        //operationsQueue.add(thread);
    }

}
