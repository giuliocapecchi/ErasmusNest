module it.unipi.erasmusnest {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.dlsc.gemsfx;
    requires redis.clients.jedis;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires opencsv;
    requires javafx.web;
    requires org.json;
    requires org.neo4j.driver;
    requires org.controlsfx.controls;


    opens it.unipi.erasmusnest to javafx.fxml;
    exports it.unipi.erasmusnest;
    exports it.unipi.erasmusnest.controllers;
    opens it.unipi.erasmusnest.controllers to javafx.fxml;
}