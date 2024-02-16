package it.unipi.erasmusnest.consistency;

import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;

public class RedisConsistencyManager extends ConsistencyManager{

    public RedisConsistencyManager(RedisConnectionManager redisConnectionManager) {
        super(redisConnectionManager, null, null);
    }

    public void addUserOnRedis(String email, String password) {
        Thread thread = new Thread(() -> {
            System.out.println("\t\t\t\t\t\t*** THREAD addUserOnRedis STARTED ***");
            boolean added = redisConnectionManager.addUser(email, password);
            if (added)
                System.out.println("\t\t\t\t\t\t*** USER ADDED ON REDIS ***");
            else
                System.out.println("\t\t\t\t\t\t*** USER NOT ADDED ON REDIS ***");

            System.out.println("\t\t\t\t\t\t*** THREAD addUserOnRedis ENDED ***");
        });

        executeOperation(thread);
    }

}
