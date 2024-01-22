package it.unipi.erasmusnest.dbconnectors;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.geometry.Point2D;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

public class MongoConnectionManager extends ConnectionManager{

    public MongoConnectionManager() {
        super("localhost", 27017);
    }

    public User findUser(String email)
    {
        User user = new User();
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {

            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("users");
            Document userDocument = collection.find(Filters.eq("email", email)).first();

            Object houseObject = userDocument.get("house");

            // Set the user's email, password, name and surname
            user.setEmail(userDocument.getString("email"));
            user.setPassword(userDocument.getString("password"));
            user.setName(userDocument.getString("first_name"));
            user.setSurname(userDocument.getString("last_name"));
            user.setStudyField(userDocument.getString("SF"));

            // Retrieve the list of preferred cities
            ArrayList<String> preferredCities = new ArrayList<>();
            Object preferredCitiesObject = userDocument.get("CoI");
            if(preferredCitiesObject instanceof List<?>)
            {
                for(Object o : (List<?>) preferredCitiesObject)
                {
                    preferredCities.add((String) o);
                }
            }
            user.setPreferredCities(preferredCities);

            List<Document> houseDocuments = new ArrayList<>();

            //LOgica per controllare se document o list
            if (houseObject instanceof Document)
            {
                houseDocuments.add((Document) houseObject);
            }
            else if(houseObject instanceof List<?>)
            {
                for(Object o : (List<?>) houseObject)
                {
                    houseDocuments.add((Document) o);
                }
            }
            //Adesso che ho ottenuto i documenti
            // Devo ottenere una lista di Apartment
            List<Apartment> houses = new ArrayList<>();
            for(Document d : houseDocuments)
            {
                Long id;
                if(d.get("house_id") instanceof Integer)
                    id = ((Integer) d.get("house_id")).longValue();
                else
                    id = (Long) d.get("house_id");
                Double rating;
                if(d.get("review_scores_rating") instanceof Integer)
                    rating = ((Integer) d.get("review_scores_rating")).doubleValue();
                else
                    rating = (Double) d.get("review_scores_rating");
                Apartment casa = new Apartment(id, d.getString("name"), d.getString("picture_url"), rating);
                houses.add(casa);
            }
            user.setHouses(houses);
        }
        catch (Exception e)
        {
            System.out.println("Error in findUser: " + e.getMessage());
            return null;
        }
        return user;
    }

    public Apartment getApartment(Long apartmentId){

        Apartment resultApartment = null;

        try (MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");

            // find one document with new Document
            Document apartment = collection.find(eq("house_id", apartmentId)).first();
            //System.out.println("Apartment: " + apartment.toJson());

            String coordinates = apartment.getString("position");
            // remove space from coordinates
            coordinates = coordinates.replaceAll("\\s","");
            // split coordinates in latitude and longitude that are separated by ','
            String[] latLong = coordinates.split(",");

            String[] neighborhood = apartment.getString("neighbourhood").split("\\s*,\\s*|(?<=\\s)-(?=\\s)");

            // create a string with the neighborhood elements separated by a new line
            String description = "";
            for (String s : neighborhood) {
                s = s.strip();
            	description += s + "\n";
            }

            description += "Bathrooms: "+apartment.getString("bathrooms_text");

            Long id = null;
            // if apartment.get("id") return an integer, it is necessary to cast it to Long
            if(apartment.get("id") instanceof Integer)
                id = ((Integer) apartment.get("id")).longValue();
            else
                id = (Long) apartment.get("id");

            // to retrieve also studyFields
            resultApartment = new Apartment(
                    id,
                    apartment.getString("house_name"),
                    description,
                    new Point2D(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1])),
                    Double.parseDouble(apartment.getString("price").replace("$", "")),
                    apartment.getInteger("accommodates"),
                    apartment.getString("host_email"),
                    apartment.getString("picture_url")
            );

        }catch (Exception e){
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB connection failed").show();
        }
        return resultApartment;
    }

    public void queryApartmentsCollection(){

        /*MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort());
        MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
        MongoCollection<Document> collection = database.getCollection("apartments");

        ArrayList<Apartment> apartments = new ArrayList<>();

        int i = 0;
        try (MongoCursor<Document> cursor = collection.find().iterator())
        {
            while (cursor.hasNext() && i<10)
            {
                Apartment apartment = new Apartment();

                // qui recupero un po' di roba che è in mongo (mongo sarà da cambiare)
                apartment.setId(cursor.next().getString("id"));


                apartments.add(apartment);
                System.out.println(cursor.next().getDouble("review_scores_rating"));
                i++;
            }
        }

        mongoClient.close();*/

    }

    public boolean updatePassword(String email, String newPassword)
    {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
        MongoCollection<Document> collection = database.getCollection("users");
        collection.updateOne(Filters.eq("email", email), new Document("$set", new Document("password", newPassword)));

        mongoClient.close();
        return true;
    }

    public boolean updateStudyField(String email, String newStudyField)
    {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
        MongoCollection<Document> collection = database.getCollection("users");
        // Aggiorna il campo "fieldOfStudy" dell'utente
        collection.updateOne(Filters.eq("email", email), new Document("$set", new Document("SF", newStudyField)));
        mongoClient.close();

        return true;
    }


    public boolean updatePreferredCities(String email, ArrayList<String> newPreferredCities)
    {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
        MongoCollection<Document> userCollection = database.getCollection("users");
        // Esegui l'aggiornamento nel database
        userCollection.updateOne(
                Filters.eq("email", email),
                new Document("$set", new Document("CoI", newPreferredCities))
        );

        mongoClient.close();

        return true;
    }

    public String getPassword(String emailAddress)
    {
        String password = "";
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("users");
            Document userDocument = collection.find(Filters.eq("email", emailAddress)).first();
            if(userDocument==null)
                return null;
            password = userDocument.getString("password");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB LOGIN failed").show();
            System.out.println("Error in getPassword: " + e.getMessage());
        }
        return password;
    }

    public boolean addUser(User utente) {
        boolean availableEmail = true;
        //System.out.println("EMAIL: " + emailAddress);
        System.out.println("PASSWORD: " + utente.getPassword());
        System.out.println("MAIL: " + utente.getEmail());
        System.out.println("CITIES: " + utente.getPreferredCities());
        System.out.println("STUDY: " + utente.getStudyField());

        if(availableEmail(utente.getEmail()))
        {
            // Il nome è disponibile
            // Add user to mongodb
            try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
            {
                MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
                MongoCollection<Document> collection = database.getCollection("users");
                Document newUser = new Document("email", utente.getEmail())
                        .append("password", utente.getPassword())
                        .append("first_name", utente.getName())
                        .append("last_name", utente.getSurname())
                        .append("id", 1)
                        .append("SF", utente.getStudyField())
                        .append("CoI", utente.getPreferredCities())
                        .append("house", new ArrayList<Document>());
                collection.insertOne(newUser);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                new AlertDialogGraphicManager("MongoDB SIGNUP failed").show();
                System.out.println("Error in addUser: " + e.getMessage());
            }
        } else {
            availableEmail = false;
        }

        return availableEmail;

        /*
        if(getPassword(emailAddress) != null)
        {
            System.out.println("USERNAME NON DISPONIBILE\n\n\n\n\n\n");
            availableEmail = false;
        }
        else
        {
            try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
            {
                MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
                MongoCollection<Document> collection = database.getCollection("users");
                Document newUser = new Document("email", emailAddress)
                        .append("password", text)
                        .append("first_name", "")
                        .append("last_name", "")
                        .append("SF", "")
                        .append("CoI", new ArrayList<String>())
                        .append("house", new ArrayList<Document>());
                collection.insertOne(newUser);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                new AlertDialogGraphicManager("MongoDB SIGNUP failed").show();
                System.out.println("Error in addUser: " + e.getMessage());
            }
        }
        */

    }



    public boolean availableEmail(String emailAddress){
        boolean availableEmail = true;
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("users");
            Document userDocument = collection.find(Filters.eq("email", emailAddress)).first();
            if(userDocument != null)
            {
                //Vuol dire che la mail è gia stata presa, ce gia un account
                availableEmail = false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB LOGIN failed").show();
            System.out.println("Error in getPassword: " + e.getMessage());
        }
        return availableEmail;
    }
}
