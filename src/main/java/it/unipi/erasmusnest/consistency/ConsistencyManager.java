package it.unipi.erasmusnest.consistency;

import it.unipi.erasmusnest.dbconnectors.MongoConnectionManager;
import it.unipi.erasmusnest.dbconnectors.Neo4jConnectionManager;
import it.unipi.erasmusnest.dbconnectors.RedisConnectionManager;
import it.unipi.erasmusnest.model.Apartment;

import java.util.PriorityQueue;
import java.util.Queue;

public class ConsistencyManager {

    protected final RedisConnectionManager redisConnectionManager;
    protected final MongoConnectionManager mongoConnectionManager;
    protected final Neo4jConnectionManager neo4jConnectionManager;

    // private static final Queue<Thread> operationsQueue = new PriorityQueue<>();

    public ConsistencyManager(RedisConnectionManager redisConnectionManager, MongoConnectionManager mongoConnectionManager, Neo4jConnectionManager neo4jConnectionManager) {
        this.redisConnectionManager = redisConnectionManager;
        this.mongoConnectionManager = mongoConnectionManager;
        this.neo4jConnectionManager = neo4jConnectionManager;
    }

    public void executeOperation(Thread operation) {
        if(operation != null)
            operation.start();
    }


}
