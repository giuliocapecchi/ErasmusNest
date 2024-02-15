package it.unipi.erasmusnest.dbconnectors;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.User;
import javafx.geometry.Point2D;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

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
            if (userDocument == null) {
                return null;
            }
            user.setEmail(userDocument.getString("email"));
            user.setName(userDocument.getString("first_name"));
            user.setSurname(userDocument.getString("last_name"));
            if(userDocument.containsKey("study_field") && userDocument.get("study_field")!=null)
                user.setStudyField(userDocument.getString("study_field"));
            if(userDocument.containsKey("houses") && userDocument.get("houses")!=null)
            {
                ArrayList<Document> houseArray = (ArrayList<Document>) userDocument.get("houses");
                if(houseArray!=null && !houseArray.isEmpty()) {
                    ArrayList<Apartment> houses = new ArrayList<>();
                    for(Document d : houseArray)
                    {
                        String id = d.getObjectId("object_id").toHexString();
                        Apartment casa = new Apartment(id, d.getString("house_name"));
                        String imageURL = d.getString("picture_url");
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Error in findUser: " + e.getMessage());
        }
        return user;
    }


//TODO: da LASCIARE, PER CAPE
    /*public void averagePriceNearCityCenter(String cityName, Point2D cityPosition, int maxDistance) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
            AggregateIterable<Document> result = apartmentsCollection.aggregate(
                    Arrays.asList(
                            new Document("$geoNear", new Document()
                                    .append("near", new Document()
                                            .append("type", "Point")
                                            .append("coordinates", Arrays.asList(cityPosition.getX(), cityPosition.getY())))
                                    .append("distanceField", "distance")
                                    .append("maxDistance", maxDistance)
                                    .append("spherical", true)),
                            new Document("$match", new Document("city", cityName)),
                            new Document("$group", new Document("_id", null)
                                    .append("price", new Document("$avg", "$price"))),
                            new Document("$project", new Document("_id", 0)
                                    .append("averagePrice", new Document("$round", Arrays.asList("$price", 2))))
                    )
            );

            for (Document doc : result) {
                System.out.println(doc.toJson());
            }
        }
    }*/

    public Double averagePriceNearCityCenter(String cityName, int maxDistance) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
            AggregateIterable<Document> result = apartmentsCollection.aggregate(Arrays.asList(new Document("$match",
                            new Document("city", cityName)),
                    new Document("$group",
                            new Document("_id", "$city")
                                    .append("avgLat",
                                            new Document("$avg",
                                                    new Document("$arrayElemAt", Arrays.asList("$position", 0L))))
                                    .append("avgLon",
                                            new Document("$avg",
                                                    new Document("$arrayElemAt", Arrays.asList("$position", 1L))))
                                    .append("apartments",
                                            new Document("$push", "$$ROOT"))),
                    new Document("$unwind", "$apartments"),
                    new Document("$set",
                            new Document("apartments.lat2",
                                    new Document("$degreesToRadians", "$avgLat"))
                                    .append("apartments.lon2",
                                            new Document("$degreesToRadians", "$avgLon"))
                                    .append("apartments.lat1",
                                            new Document("$degreesToRadians",
                                                    new Document("$arrayElemAt", Arrays.asList("$apartments.position", 0L))))
                                    .append("apartments.lon1",
                                            new Document("$degreesToRadians",
                                                    new Document("$arrayElemAt", Arrays.asList("$apartments.position", 1L))))),
                    new Document("$set",
                            new Document("apartments.distance",
                                    new Document("$let",
                                            new Document("vars",
                                                    new Document("earthRadius", 6371L * 1000L)
                                                            .append("deltaLat",
                                                                    new Document("$subtract", Arrays.asList("$apartments.lat2", "$apartments.lat1")))
                                                            .append("deltaLon",
                                                                    new Document("$subtract", Arrays.asList("$apartments.lon2", "$apartments.lon1"))))
                                                    .append("in",
                                                            new Document("$multiply", Arrays.asList("$$earthRadius",
                                                                    new Document("$atan2", Arrays.asList(new Document("$sqrt",
                                                                                    new Document("$add", Arrays.asList(new Document("$pow", Arrays.asList("$$deltaLat", 2L)),
                                                                                            new Document("$multiply", Arrays.asList(new Document("$cos",
                                                                                                            new Document("$divide", Arrays.asList(new Document("$add", Arrays.asList("$apartments.lat1", "$apartments.lat2")), 2L))),
                                                                                                    new Document("$pow", Arrays.asList("$$deltaLon", 2L))))))),
                                                                            new Document("$sqrt",
                                                                                    new Document("$add", Arrays.asList(new Document("$cos", "$apartments.lat1"),
                                                                                            new Document("$cos", "$apartments.lat2"),
                                                                                            new Document("$multiply", Arrays.asList(new Document("$cos", "$$deltaLon"),
                                                                                                    new Document("$cos",
                                                                                                            new Document("$divide", Arrays.asList(new Document("$add", Arrays.asList("$apartments.lat1", "$apartments.lat2")), 2L)))))))))))))))),
                    new Document("$project",
                            new Document("apartments.distance", 1L)
                                    .append("apartments.price", 1L)
                                    .append("apartments.position", 1L)
                                    .append("apartments.name", 1L)
                                    .append("apartments.city", 1L)),
                    new Document("$match",
                            new Document("apartments.distance",
                                    new Document("$lte", maxDistance))),
                    new Document("$group",
                            new Document("_id", "$_id")
                                    .append("avgPrice",
                                            new Document("$avg", "$apartments.price"))),
                    new Document("$project",
                            new Document("_id", 1L)
                                    .append("avgPrice",
                                            new Document("$round", Arrays.asList("$avgPrice", 2L))))));


            for (Document doc : result) {
                System.out.println(doc.toJson());
                return doc.getDouble("avgPrice");
            }
        }
        return null;
    }



    public List<Map<String, Object>> averagePriceNearCityCenterForEachCity(int distance) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
            AggregateIterable<Document> result = apartmentsCollection.aggregate(Arrays.asList(new Document("$group",
                            new Document("_id", "$city")
                                    .append("avgLat",
                                            new Document("$avg",
                                                    new Document("$arrayElemAt", Arrays.asList("$position", 0L))))
                                    .append("avgLon",
                                            new Document("$avg",
                                                    new Document("$arrayElemAt", Arrays.asList("$position", 1L))))
                                    .append("apartments",
                                            new Document("$push", "$$ROOT"))),
                    new Document("$unwind", "$apartments"),
                    new Document("$set",
                            new Document("apartments.lat2",
                                    new Document("$degreesToRadians", "$avgLat"))
                                    .append("apartments.lon2",
                                            new Document("$degreesToRadians", "$avgLon"))
                                    .append("apartments.lat1",
                                            new Document("$degreesToRadians",
                                                    new Document("$arrayElemAt", Arrays.asList("$apartments.position", 0L))))
                                    .append("apartments.lon1",
                                            new Document("$degreesToRadians",
                                                    new Document("$arrayElemAt", Arrays.asList("$apartments.position", 1L))))),
                    new Document("$set",
                            new Document("apartments.distance",
                                    new Document("$let",
                                            new Document("vars",
                                                    new Document("earthRadius", 6371L * 1000L)
                                                            .append("deltaLat",
                                                                    new Document("$subtract", Arrays.asList("$apartments.lat2", "$apartments.lat1")))
                                                            .append("deltaLon",
                                                                    new Document("$subtract", Arrays.asList("$apartments.lon2", "$apartments.lon1"))))
                                                    .append("in",
                                                            new Document("$multiply", Arrays.asList("$$earthRadius",
                                                                    new Document("$atan2", Arrays.asList(new Document("$sqrt",
                                                                                    new Document("$add", Arrays.asList(new Document("$pow", Arrays.asList("$$deltaLat", 2L)),
                                                                                            new Document("$multiply", Arrays.asList(new Document("$cos",
                                                                                                            new Document("$divide", Arrays.asList(new Document("$add", Arrays.asList("$apartments.lat1", "$apartments.lat2")), 2L))),
                                                                                                    new Document("$pow", Arrays.asList("$$deltaLon", 2L))))))),
                                                                            new Document("$sqrt",
                                                                                    new Document("$add", Arrays.asList(new Document("$cos", "$apartments.lat1"),
                                                                                            new Document("$cos", "$apartments.lat2"),
                                                                                            new Document("$multiply", Arrays.asList(new Document("$cos", "$$deltaLon"),
                                                                                                    new Document("$cos",
                                                                                                            new Document("$divide", Arrays.asList(new Document("$add", Arrays.asList("$apartments.lat1", "$apartments.lat2")), 2L)))))))))))))))),
                    new Document("$project",
                            new Document("apartments.distance", 1L)
                                    .append("apartments.price", 1L)
                                    .append("apartments.position", 1L)
                                    .append("apartments.name", 1L)
                                    .append("apartments.city", 1L)),
                    new Document("$match",
                            new Document("apartments.distance",
                                    new Document("$lte", distance))),
                    new Document("$group",
                            new Document("_id", "$_id")
                                    .append("avgPrice",
                                            new Document("$avg", "$apartments.price"))),
                    new Document("$project",
                            new Document("_id", 1L)
                                    .append("avgPrice",
                                            new Document("$round", Arrays.asList("$avgPrice", 2L)))))

            );

            List<Map<String, Object>> cityPrices = new ArrayList<>();
            for (Document doc : result) {
                System.out.println(doc.toJson());
                String city = doc.getString("_id");
                double avgPrice = doc.getDouble("avgPrice");
                Map<String, Object> cityPriceMap = new HashMap<>();
                cityPriceMap.put("city", city);
                cityPriceMap.put("avgPrice", avgPrice);
                cityPrices.add(cityPriceMap);
            }
            return cityPrices;
        }catch (Exception e){
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB connection failed").show();
            System.out.println("Error in averagePriceNearCityCenterForEachCity: " + e.getMessage());
            return null;
        }
    }


    public Apartment uploadApartment(Apartment apartment)
    {
        Apartment insertedApartment = null;
        try(MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort()))
        {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> userCollection = database.getCollection("users");
            Document userDocument = userCollection.find(Filters.eq("email", apartment.getHostEmail())).first();
            if (userDocument != null) { // procedo solo se l'utente che tenta il caricamento viene trovato
                MongoCollection<Document> collection = database.getCollection("apartments");
                Document newApartment = new Document("house_name", apartment.getName())
                        .append("host_name",apartment.getHostName())
                        .append("host_surname",apartment.getHostSurname())
                        .append("email", apartment.getHostEmail())
                        .append("accommodates", apartment.getMaxAccommodates())
                        .append("bathrooms", apartment.getBathrooms())
                        .append("price", apartment.getDollarPriceMonth())
                        .append("position", Arrays.asList(apartment.getLocation().getX(), apartment.getLocation().getY()))
                        .append("city", apartment.getCity());
                // OPTIONAL: DESCRIPTION E PICTUREURL
                String description = apartment.getDescription();
                if(description!=null && !description.isEmpty() && !description.isBlank()) {
                    newApartment.append("description", apartment.getDescription());
                }
                if(apartment.getImageURLs()!=null && !apartment.getImageURLs().isEmpty()) {
                    newApartment.append("picture_url", apartment.getImageURLs());
                }
                collection.insertOne(newApartment);
                ObjectId objectId = newApartment.getObjectId("_id");
                System.out.println("\n\n\nObjectId: " + objectId);
                String insertedObjectId = newApartment.getObjectId("_id").toHexString();
                // Update the user's house list
                Document houseDocument = new Document()
                        .append("object_id", objectId)
                        .append("house_name", apartment.getName());
                if(apartment.getImageURLs()!=null && !apartment.getImageURLs().isEmpty()) {
                    // Taking only the first image of the list
                    houseDocument.append("picture_url", apartment.getImageURLs().get(0));
                }
                ArrayList<Document> houseArray = new ArrayList<>();
                // Controlla se il campo "house" non esiste o è vuoto
                if (userDocument.containsKey("houses") && userDocument.get("houses") != null)
                    houseArray = (ArrayList<Document>) userDocument.get("houses");
                houseArray.add(houseDocument);
                userDocument.put("houses", houseArray);
                userCollection.replaceOne(Filters.eq("email", apartment.getHostEmail()), userDocument);
                System.out.println("Casa aggiunta o creata con successo.");
                insertedApartment = apartment;
                insertedApartment.setId(insertedObjectId);
            } else {
                System.out.println("Documento utente non trovato.");
            }

        }catch (Exception e){
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB UPLOAD failed").show();
            System.out.println("Error in uploadApartment: " + e.getMessage());
        }
        return insertedApartment;
    }

    public Apartment getApartment(String apartmentId){

        Apartment resultApartment = null;

        try (MongoClient mongoClient = MongoClients.create("mongodb://"+super.getHost()+":"+super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> collection = database.getCollection("apartments");

            // find one document with new Document that have the object id field equal to the apartmentId
            ObjectId id = new ObjectId(apartmentId);
            Document apartment = collection.find(eq("_id", id)).first();
            if(apartment!=null) {
               // String coordinates = apartment.getString("position");
                List<Double> coordinates = apartment.getList("position", Double.class);
                Point2D coordinatesPoint = new Point2D(coordinates.get(0), coordinates.get(1));

                ArrayList<String> picURLs = new ArrayList<>();
                if(apartment.get("picture_url")!=null)
                    picURLs = apartment.get("picture_url",ArrayList.class);
                System.out.println("\n\n\nPICURLS: " + picURLs);

                resultApartment = new Apartment(
                        apartmentId,
                        apartment.getString("house_name"),
                        //description,
                        apartment.getString("description"),
                        coordinatesPoint,
                        apartment.getInteger("price"),
                        apartment.getInteger("accommodates"),
                        apartment.getString("email"),
                        picURLs,
                        apartment.getInteger("bathrooms")
                );
            }
            else {
                new AlertDialogGraphicManager("Apartment not found in MongoDB").show();
            }

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
            collection.updateOne(Filters.eq("email", email), new Document("$set", new Document("study_field", newStudyField)));
        }
        else {
            collection.updateOne(Filters.eq("email", email),new Document("$unset", new Document("study_field", ""))
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
                    newUser.append("study_field", utente.getStudyField());
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

    public boolean updateApartment(Apartment oldHouse, Apartment updatedHouse) {
        boolean updated = false;
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
            MongoCollection<Document> usersCollection = database.getCollection("users");
            // Preparing document for $set operation
            Document setDocument = new Document();
            if (!Objects.equals(updatedHouse.getDollarPriceMonth(), oldHouse.getDollarPriceMonth())) {
                setDocument.append("price", updatedHouse.getDollarPriceMonth());
            }
            if (!Objects.equals(updatedHouse.getMaxAccommodates(), oldHouse.getMaxAccommodates())) {
                setDocument.append("accommodates", updatedHouse.getMaxAccommodates());
            }
            if(updatedHouse.getImageURLs()!=null && !updatedHouse.getImageURLs().isEmpty() && !updatedHouse.getImageURLs().equals(oldHouse.getImageURLs())) {
                setDocument.append("picture_url", updatedHouse.getImageURLs());
            }
            if (!Objects.equals(updatedHouse.getBathrooms(), oldHouse.getBathrooms())) {
                setDocument.append("bathrooms", updatedHouse.getBathrooms());
            }
            if ((!Objects.equals(updatedHouse.getDescription(), oldHouse.getDescription())) && updatedHouse.getDescription() != null && !updatedHouse.getDescription().isBlank()) {
                setDocument.append("description", updatedHouse.getDescription());
            }

            // Documento per l'operazione $unset
            Document unsetDocument = new Document();
            if (updatedHouse.getDescription() == null || updatedHouse.getDescription().isEmpty() || updatedHouse.getDescription().isBlank()) {
                unsetDocument.append("description", "");
            }

            // Preparazione del documento di aggiornamento completo
            Document updatedHouseDocument = new Document();
            if (!setDocument.isEmpty()) {
                updatedHouseDocument.append("$set", setDocument);
            }
            if (!unsetDocument.isEmpty()) {
                updatedHouseDocument.append("$unset", unsetDocument);
            }

            System.out.println("UpdatedHouseDocument: " + updatedHouseDocument.toJson());

            // Applica l'operazione di aggiornamento per l'appartamento
            apartmentsCollection.updateOne(Filters.eq("_id", new ObjectId(updatedHouse.getId())), updatedHouseDocument);

            // Controlla se la lista delle immagini è vuota prima di tentare di accedere al primo elemento
            Document pictureOperation = new Document();
            if (updatedHouse.getImageURLs() != null && !updatedHouse.getImageURLs().isEmpty()) {
                String firstImageUrl = updatedHouse.getImageURLs().get(0);
                pictureOperation.append("$set", new Document("houses.$[elem].picture_url", firstImageUrl));
            } else {
                pictureOperation.append("$unset", new Document("houses.$[elem].picture_url", ""));
            }

            // Creazione del filtro per l'utente e dell'array filter per l'elemento specifico nell'array houses
            Bson userFilter = Filters.eq("email", updatedHouse.getHostEmail());
            Bson houseFilter = Filters.eq("elem.object_id", new ObjectId(updatedHouse.getId()));

            // Applica l'operazione di aggiornamento
            usersCollection.updateOne(
                    userFilter,
                    pictureOperation,
                    new UpdateOptions().arrayFilters(List.of(houseFilter))
            );

            updated = true;
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB UPDATE failed").show();
            System.out.println("Error in updateApartment: " + e.getMessage());
        }
        return updated;
    }

    public boolean removeApartment(String objectIdToRemove, String userEmail) {
        boolean res = false;
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            // Cerca e rimuovi l'appartamento dalla collezione "apartments" utilizzando l'ObjectID
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
            ObjectId apartmentObjectId = new ObjectId(objectIdToRemove);
            Document apartmentFilter = new Document("_id", apartmentObjectId);
            boolean apartmentRemoved = apartmentsCollection.deleteOne(apartmentFilter).getDeletedCount() > 0;

            if (!apartmentRemoved) {
                throw new RuntimeException("L'appartamento con l'ObjectID specificato non è stato trovato o non può essere rimosso.");
            } else {
                System.out.println("\n\nAppartamento rimosso con successo.\n\n");
            }

            // Cerca l'utente nella collezione "users" utilizzando l'email
            MongoCollection<Document> usersCollection = database.getCollection("users");
            Document userFilter = new Document("email", userEmail);
            Document userDocument = usersCollection.find(userFilter).first();

            if (userDocument == null) {
                throw new RuntimeException("L'utente con l'email specificata non è stato trovato.");
            } else {
                System.out.println("\n\nUtente trovato con successo.\n\n"+userDocument.toJson());
            }

            // Ottieni l'array "houses" dal documento utente
            ArrayList housesEmbeddedDocument = userDocument.get("houses", ArrayList.class);

            if (housesEmbeddedDocument == null) {
                throw new RuntimeException("L'utente non ha un documento 'houses'.");
            } else {
                System.out.println("\n\nDocumento 'houses' trovato con successo.\n\n");
            }

            // Rimuovi la casa con lo stesso ObjectID dall'array
            ObjectId apartmentToRemoveObjectId = new ObjectId(objectIdToRemove);

            // Se l'array ha un solo elemento, elimina tutto il documento "houses"
            if ( housesEmbeddedDocument.size() == 1) {
                usersCollection.updateOne(userFilter, new Document("$unset", new Document("houses", "")));
            } else if(housesEmbeddedDocument.size()>1){
                // Altrimenti, aggiorna l'array "houses" nel documento utente
                usersCollection.updateOne(userFilter, new Document("$pull", new Document("houses", new Document("object_id", apartmentToRemoveObjectId))));
            }
            else {
                throw new RuntimeException("L'array 'houses' è vuoto.");
            }
            System.out.println("Operazione completata con successo.");
            res = true;
        } catch (Exception e) {
            System.err.println("Errore durante l'operazione: " + e.getMessage());
            res = false;
        }
        return res;
    }

    public void removeUser(String email) {
        // Method should remove user from users collection and all apartments from apartments collection associated with the user
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> usersCollection = database.getCollection("users");
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");

            // Remove user from users collection
            usersCollection.deleteOne(Filters.eq("email", email));

            // Remove all apartments associated with the user from apartments collection
            apartmentsCollection.deleteMany(Filters.eq("email", email));
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB REMOVE failed").show();
            System.out.println("Error in removeUser: " + e.getMessage());
        }
    }

    public String getPriceAnalytics(Integer accommodates, Integer bathrooms, Integer priceMin, Integer priceMax) {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("ErasmusNest");
        MongoCollection<Document> collection = database.getCollection("apartments");

        List<Bson> matchFilters = new ArrayList<>();

        // Aggiungi filtri solo se il valore non è zero
        if (accommodates != 0) {
            matchFilters.add(Filters.gte("accommodates", accommodates));
        }
        if (bathrooms != 0) {
            matchFilters.add(Filters.gte("bathrooms", bathrooms));
        }
        if (priceMin != 0) {
            matchFilters.add(Filters.gte("price", priceMin));
        }
        if (priceMax != 0) {
            matchFilters.add(Filters.lte("price", priceMax));
        }
        Bson matchStage = matchFilters.isEmpty() ? match(new Document()) : match(Filters.and(matchFilters));

        // Define the aggregation pipeline
        AggregateIterable<Document> result = collection.aggregate(Arrays.asList(
                matchStage,
                new Document("$group",
                        new Document("_id", "$city")
                                .append("averagePrice",
                                        new Document("$avg", "$price"))
                                .append("count",
                                        new Document("$sum", 1L))),
                new Document("$sort",
                        new Document("averagePrice", 1L)),
                new Document("$group",
                        new Document("_id",
                                new BsonNull())
                                .append("lowestAveragePrice",
                                        new Document("$first", "$$ROOT"))
                                .append("highestAveragePrice",
                                        new Document("$last", "$$ROOT"))),
                new Document("$project",
                        new Document("_id", 0L)
                                .append("lowestAveragePriceCity", "$lowestAveragePrice._id")
                                .append("lowestAveragePrice", "$lowestAveragePrice.averagePrice")
                                .append("lowestAveragePriceCount", "$lowestAveragePrice.count")
                                .append("highestAveragePriceCity", "$highestAveragePrice._id")
                                .append("highestAveragePrice", "$highestAveragePrice.averagePrice")
                                .append("highestAveragePriceCount", "$highestAveragePrice.count"))));
        StringBuilder resultString = new StringBuilder();
        for (Document doc : result) {
            resultString.append(doc.toJson()).append("\n");
        }
        String res = !resultString.toString().isEmpty() ? resultString.toString() : "No result found";
        client.close();
        return res;
    }

    public HashMap<Point2D, Integer> getHeatmap(String city) {

        long multiplier = 80L;
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + super.getHost() + ":" + super.getPort())) {
            MongoDatabase database = mongoClient.getDatabase("ErasmusNest");
            MongoCollection<Document> apartmentsCollection = database.getCollection("apartments");
            AggregateIterable<Document> result = apartmentsCollection.aggregate(Arrays.asList(new Document("$match",
                            new Document("city", city)),
                    new Document("$project",
                            new Document("position", 1L)),
                    new Document("$addFields",
                            new Document("cell",
                                    new Document("$concat", Arrays.asList(new Document("$toString",
                                                    new Document("$trunc",
                                                            new Document("$multiply", Arrays.asList(multiplier,
                                                                    new Document("$arrayElemAt", Arrays.asList("$position", 0L)))))), "_",
                                            new Document("$toString",
                                                    new Document("$trunc",
                                                            new Document("$multiply", Arrays.asList(multiplier,
                                                                    new Document("$arrayElemAt", Arrays.asList("$position", 1L)))))))))),
                    new Document("$group",
                            new Document("_id", "$cell")
                                    .append("count",
                                            new Document("$sum", 1L)))));


            HashMap<Point2D, Integer> heatmap = new HashMap<>();
            for (Document doc : result) {
                String cell = doc.getString("_id");
                String[] coordinates = cell.split("_");
                double lat = Double.parseDouble(coordinates[0]);
                double lon = Double.parseDouble(coordinates[1]);
                Point2D point = new Point2D(lat/multiplier, lon/multiplier);
                Integer count = doc.getLong("count").intValue();
                heatmap.put(point, count);
            }
            return heatmap;
        }catch (Exception e){
            e.printStackTrace();
            new AlertDialogGraphicManager("MongoDB connection failed").show();
            System.out.println("Error in getHeatmap: " + e.getMessage());
        }
        return null;
    }

}
