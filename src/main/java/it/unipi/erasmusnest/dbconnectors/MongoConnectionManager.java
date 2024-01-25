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
            e.printStackTrace();
            System.out.println("Error in findUser: " + e.getMessage());
            return null;
        }
        return user;
    }

    public boolean uploadApartment(Apartment apartment)
    {
        boolean result = false;
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");
            Document newApartment = new Document("house_id", apartment.getId())
                    .append("house_name", apartment.getName())
                    .append("picture_url", apartment.getImageURL())
                    .append("host_name",apartment.getHostName())
                    .append("host_surname",apartment.getHostSurname())
                    .append("host_email", apartment.getHostEmail())
                    .append("accommodates", apartment.getMaxAccommodates())
                    .append("bathrooms_text", apartment.getBathrooms())
                    .append("price", "$" + apartment.getDollarPriceMonth())
                    .append("position", apartment.getLocation().getX() + ", " + apartment.getLocation().getY())
                    .append("neighbourhood", apartment.getDescription());
            collection.insertOne(newApartment);
            // Update the user's house list
            MongoCollection<Document> userCollection = database.getCollection("users");
            Document userDocument = userCollection.find(Filters.eq("email", apartment.getHostEmail())).first();
            Document houseDocument = new Document()
                    .append("house_id", apartment.getId())
                    .append("name", apartment.getName())
                    .append("picture_url", apartment.getImageURL())
                    .append("review_scores_rating", 0.0);
            // Prepare houses list
            List<Document> userApartments = new ArrayList<>();
            // NEW VERSION
            if(userDocument.containsKey("house"))
            {
                if (userDocument.get("house") instanceof Document)
                {
                    userApartments.add((Document) userDocument.get("house"));
                }
                else
                {
                    userApartments = (List<Document>) userDocument.get("house");
                }
            }
            // Add houseDocument to List<Document>
            userApartments.add(houseDocument);
            // Add the updated list to the userDocument
            userDocument.put("house", userApartments);
            // update user collection inserting new house
            userCollection.replaceOne(Filters.eq("email", apartment.getHostEmail()), userDocument);
            result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB UPLOAD failed").show();
            System.out.println("Error in uploadApartment: " + e.getMessage());
            result = false;
        }
        return result;
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

            // description += "Bathrooms: "+apartment.getString("bathrooms_text");

            String bathroomsText = apartment.getString("bathrooms_text");
            String[] bathSplit = bathroomsText.split("\\s+");
            String bathroomsNumber = bathSplit[0];
            System.out.println("\n\n\nBathrooms number: "+bathroomsNumber+"\n\n\n");
            bathroomsNumber = Double.parseDouble(bathroomsNumber) == 1.0 ? bathroomsNumber + " bath" : bathroomsNumber + " baths";

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
                    apartment.getString("picture_url"),
                    bathroomsNumber
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
                //Mail already in use
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

    public boolean updateApartment(Apartment updatedHouse)
    {
        boolean updated = false;
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");
            Document updatedHouseDocument = new Document("house_id", updatedHouse.getId())
                    .append("house_name", updatedHouse.getName())
                    .append("price", "$" + updatedHouse.getDollarPriceMonth())
                    .append("accommodates", updatedHouse.getMaxAccommodates())
                    .append("host_email", updatedHouse.getHostEmail())
                    .append("picture_url", updatedHouse.getImageURL())
                    .append("position", updatedHouse.getLocation().getX() + ", " + updatedHouse.getLocation().getY())
                    .append("neighbourhood", updatedHouse.getDescription())
                    .append("bathrooms_text", updatedHouse.getBathrooms());
            collection.updateOne(Filters.eq("house_id", updatedHouse.getId()), new Document("$set", updatedHouseDocument));
            updated = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB UPDATE failed").show();
            System.out.println("Error in updateApartment: " + e.getMessage());
        }
        return updated;
    }

    public boolean removeApartment(Long apartmentId)
    {
        boolean removed = false;
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");
            collection.deleteOne(Filters.eq("house_id", apartmentId));
            // remove apartment also from users collection
            MongoCollection<Document> userCollection = database.getCollection("users");
            userCollection.updateOne(Filters.eq("house.house_id", apartmentId), new Document("$pull", new Document("house", new Document("house_id", apartmentId))));
            removed = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB REMOVE failed").show();
            System.out.println("Error in removeApartment: " + e.getMessage());
        }
        return removed;
    }
}
