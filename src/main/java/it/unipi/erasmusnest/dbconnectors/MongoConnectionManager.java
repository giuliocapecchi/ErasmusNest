package it.unipi.erasmusnest.dbconnectors;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.geometry.Point2D;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
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

            if (userDocument == null)
                return null;

            // Object houseObject = userDocument.get("house");

            // Set the user's email, password, name and surname
            user.setEmail(userDocument.getString("email"));
            user.setPassword(userDocument.getString("password"));
            user.setName(userDocument.getString("first_name"));
            user.setSurname(userDocument.getString("last_name"));
            user.setStudyField(userDocument.getString("SF"));

            // Retrieve the list of preferred cities
            /*
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
            */
            //LOgica per controllare se document o list
            ArrayList<Document> houseArray = (ArrayList<Document>) userDocument.get("house");
            if(houseArray!=null && !houseArray.isEmpty()) {
                ArrayList<Apartment> houses = new ArrayList<>();
                for(Document d : houseArray)
                {
                    String id = d.get("object_id").toString();
                    System.out.println("\n\n\nID: " + id);
                    Apartment casa = new Apartment(id, d.getString("house_name"));
                    String imageURL = d.get("picture_url").toString();
                    if(imageURL!=null && !imageURL.isEmpty()) {
                        ArrayList<String> urlList = new ArrayList<>();
                        urlList.add(imageURL);
                        casa.setImageURL(urlList);
                    }
                    houses.add(casa);
                }
                user.setHouses(houses);
            }
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
            Document newApartment = new Document("house_name", apartment.getName())
                    .append("host_name",apartment.getHostName())
                    .append("host_surname",apartment.getHostSurname())
                    .append("email", apartment.getHostEmail())
                    .append("accommodates", apartment.getMaxAccommodates())
                    .append("bathrooms", apartment.getBathrooms())
                    .append("price", apartment.getDollarPriceMonth())
                    .append("position", apartment.getLocation().getX() + ", " + apartment.getLocation().getY());
            // OPTIONAL: DESCRIPTION E PICTUREURL
            String description = apartment.getDescription();
            if(description!=null && !description.isEmpty() && !description.isBlank()) {
                newApartment.append("description", apartment.getDescription());
            }
            if(apartment.getImageURL()!=null && !apartment.getImageURL().isEmpty()) {
                newApartment.append("picture_url", apartment.getImageURL());
            }
            collection.insertOne(newApartment);
            String insertedObjectId = newApartment.getObjectId("_id").toHexString();
            // Update the user's house list
            MongoCollection<Document> userCollection = database.getCollection("users");
            Document userDocument = userCollection.find(Filters.eq("email", apartment.getHostEmail())).first();
            if (userDocument != null) {
                Document houseDocument = new Document()
                        .append("object_id", insertedObjectId)
                        .append("house_name", apartment.getName());
                if(apartment.getImageURL()!=null && !apartment.getImageURL().isEmpty()) {
                    // Taking only the first image of the list
                    houseDocument.append("picture_url", apartment.getImageURL().get(0));
                }
                ArrayList<Document> houseArray = new ArrayList<>();
                // Controlla se il campo "house" non esiste o è vuoto
                if (userDocument.containsKey("house") && userDocument.get("house") != null)
                    houseArray = (ArrayList<Document>) userDocument.get("house");
                houseArray.add(houseDocument);
                userDocument.put("house", houseArray);
                userCollection.replaceOne(Filters.eq("email", apartment.getHostEmail()), userDocument);

                System.out.println("Casa aggiunta o creata con successo.");
            } else {
                System.out.println("Documento utente non trovato.");
            }
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

    public Apartment getApartment(String apartmentId){

        Apartment resultApartment = null;

        try (MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");

            // find one document with new Document
            Document apartment = collection.find(eq("_id", apartmentId)).first();
            //System.out.println("Apartment: " + apartment.toJson());

            String coordinates = apartment.getString("position");
            // remove space from coordinates
            coordinates = coordinates.replaceAll("\\s","");
            // split coordinates in latitude and longitude that are separated by ','
            String[] latLong = coordinates.split(",");

            /*
            String[] descriptionList = apartment.getString("description").split("\\s*,\\s*|(?<=\\s)-(?=\\s)");

            // create a string with the neighborhood elements separated by a new line
            String description = "";
            for (String s : descriptionList) {
                s = s.strip();
            	description += s + "\n";
            }
            */

            // description += "Bathrooms: "+apartment.getString("bathrooms_text");

            String id = apartment.get("_id").toString();
            // if apartment.get("id") return an integer, it is necessary to cast it to Long
            /*
            if(apartment.get("id") instanceof Integer)
                id = ((Integer) apartment.get("id")).longValue();
            else
                id = (Long) apartment.get("id");
            */

            // to retrieve also studyFields
            resultApartment = new Apartment(
                    apartmentId,
                    apartment.getString("house_name"),
                    //description,
                    apartment.getString("description"),
                    new Point2D(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1])),
                    apartment.getDouble("price"),
                    apartment.getInteger("accommodates"),
                    apartment.getString("email"),
                    apartment.getString("picture_url"),
                    apartment.getInteger("bathrooms")
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
        // If new studyField != None, update the user's studyField
        // else remove the studyField from the user's document
        if(!newStudyField.equals("None")) {
            collection.updateOne(Filters.eq("email", email), new Document("$set", new Document("SF", newStudyField)));
        }
        else {
            collection.updateOne(Filters.eq("email", email),new Document("$unset", new Document("SF", ""))
            );
        }
        mongoClient.close();

        return true;
    }


    public boolean updatePreferredCities(String email, List<String> newPreferredCities)
    {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
        MongoCollection<Document> userCollection = database.getCollection("users");
        if(!newPreferredCities.contains("None"))
        {
            userCollection.updateOne(Filters.eq("email", email), new Document("$set", new Document("CoI", newPreferredCities)));
        }
        else
        {
            userCollection.updateOne(Filters.eq("email", email), new Document("$unset", new Document("CoI", "")));
        }
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
                        .append("last_name", utente.getSurname());
                if(!utente.getStudyField().equals("None"))
                    newUser.append("SF", utente.getStudyField());
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
            Document updatedHouseDocument = new Document("house_name", updatedHouse.getName())
                    .append("price", updatedHouse.getDollarPriceMonth())
                    .append("accommodates", updatedHouse.getMaxAccommodates())
                    .append("email", updatedHouse.getHostEmail())
                    .append("position", updatedHouse.getLocation().getX() + ", " + updatedHouse.getLocation().getY())
                    .append("bathrooms", updatedHouse.getBathrooms());
            if(updatedHouse.getImageURL()!=null) {
                if(updatedHouse.getImageURL().isEmpty() || updatedHouse.getDescription().equals(" "))
                    updatedHouseDocument.remove("picture_url");
                else
                    updatedHouseDocument.append("picture_url", updatedHouse.getImageURL());
            }
            if(updatedHouse.getDescription()!=null) {
                if(updatedHouse.getDescription().isEmpty() || updatedHouse.getDescription().isBlank() || updatedHouse.getDescription().equals(" "))
                    updatedHouseDocument.remove("description");
                else
                    updatedHouseDocument.append("description", updatedHouse.getDescription());
            }
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

    public boolean removeApartment(String apartmentId)
    {
        boolean removed = false;
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");
            // Converti l'apartmentId in un ObjectId
            ObjectId objectId = new ObjectId(apartmentId);

            // Verifica se la casa da eliminare è l'ultima associata all'utente
            MongoCollection<Document> userCollection = database.getCollection("users");
            long apartmentsCount = userCollection.countDocuments(Filters.eq("house._id", objectId));

            if (apartmentsCount > 1) {
                collection.deleteOne(Filters.eq("_id", objectId));
                userCollection.updateOne(Filters.eq("house._id", objectId), Updates.pull("house", new Document("_id", objectId)));
                removed = true;
            } else if (apartmentsCount == 1) {
                collection.deleteOne(Filters.eq("_id", objectId));
                userCollection.deleteOne(Filters.eq("house._id", objectId));
                removed = true;
            } else {
                System.out.println("L'appartamento con l'ObjectId specificato non è stato trovato negli utenti.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB REMOVE failed").show();
            System.out.println("Error in removeApartment: " + e.getMessage());
        }
        return removed;
    }
}
