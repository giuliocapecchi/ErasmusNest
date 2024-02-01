package it.unipi.erasmusnest.dbconnectors;
import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Apartment;
import it.unipi.erasmusnest.model.Review;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.NULL;
import static org.neo4j.driver.Values.parameters;

public class Neo4jConnectionManager extends ConnectionManager implements AutoCloseable {
    ////////////////////////////////////// DRIVER OBJECT //////////////////////////////////////
    private final Driver driver;


    ////////////////////////////////////// CONNECTION URI //////////////////////////////////////
    private static final String PROTOCOL = "bolt://";
    private static final String NEO4J_HOST = "localhost";
    private static final String NEO4J_PORT = "7687";
    private static final String NEO4J_USER = "neo4j";
    private static final String NEO4J_PSW = "adminadmin";

    ////////////////////////////////////// CONSTRUCTOR //////////////////////////////////////
    public Neo4jConnectionManager()
    {
        super(NEO4J_HOST, Integer.parseInt(NEO4J_PORT));
        String connectionString = String.format("%s%s:%s", PROTOCOL, NEO4J_HOST, NEO4J_PORT);
        driver = GraphDatabase.driver(connectionString, AuthTokens.basic(NEO4J_USER, NEO4J_PSW));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    ////////////////////////////////////// CRUD OPERATION  //////////////////////////////////////

    //CREATE

    public void addUser( final String email, final int id )
    {
        try (Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (p:Person {email: $email, userID: $id})",
                        parameters( "email", email, "userID", id ) );
                return null;
            });
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }

    public void addCity( final String name)
    {
        try (Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (c:City {name: $name})",
                        parameters( "name", name ) );
                return null;
            });
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }


    public void addApartment( final Integer apartmentId, final String name, final String pictureUrl, final Double averageReviewScore )
    {
        try (Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (a:Apartment {apartmentId: $apartmentId, name: $name, pictureUrl: $pictureUrl, averageReviewScore: $averageReviewScore})",
                        parameters( "apartmentId", apartmentId, "name", name, "pictureUrl", pictureUrl, "averageReviewScore", averageReviewScore ) );
                return null;
            });
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }

    public void addReview(final String email, Long apartmentId, String comment,Integer score) {
        System.out.println("email:" + email + " apartmentId:" + apartmentId + " comment:" + comment + " score:" + score);
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {email: $email}) " +
                                "MATCH (a:Apartment {apartmentId: $apartmentId}) " +
                                "CREATE (u)-[r:REVIEW]->(a) " +
                                "SET r.comment = $comment, r.score = $score "+
                                "RETURN r",
                        parameters("email", email, "apartmentId", apartmentId, "comment", comment, "score", score));
                System.out.println("Review added");
                updateApartmentAverageReviewScore(apartmentId);
                System.out.println("Average review score updated");
                return null;
            });
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }



    //READ

    public List<Apartment> getApartmentsInCity(String cityName, int page, int elementsPerPage, int filter) {
        try (Session session = driver.session()) {
            int elementsToSkip =  (page-1)*elementsPerPage;
            List<Apartment> apartments = session.readTransaction((TransactionWork<List<Apartment>>) tx -> {
                Result result = null;
                if(filter==0){ // default query without filter
                    result = tx.run("MATCH (a:Apartment)-[:LOCATED]->(c:City {name:$cityName}) " +
                                    "OPTIONAL MATCH (a)<-[r:REVIEW]-() " +
                                    "WITH a, COUNT(r) AS reviewCount " +
                                    "RETURN a, reviewCount " +
                                    "SKIP $elementsToSkip LIMIT $elementsPerPage",
                            parameters("cityName", cityName, "elementsToSkip", elementsToSkip, "elementsPerPage", elementsPerPage));
                }else if(filter==1){ // query that orders the houses based on the positiveness of the reviews
                    result = tx.run("MATCH (a:Apartment)-[:LOCATED]->(c:City {name:$cityName}) " +
                                    "OPTIONAL MATCH (a)<-[r:REVIEW]-() " +
                                    "WITH a, COUNT(r) AS reviewCount " +
                                    "RETURN a, reviewCount " +
                                    "ORDER BY CASE WHEN a.averageReviewScore IS NULL THEN 1 ELSE 0 END, a.averageReviewScore DESC " +
                                    "SKIP $elementsToSkip LIMIT $elementsPerPage",
                            parameters("cityName", cityName, "elementsToSkip", elementsToSkip, "elementsPerPage", elementsPerPage));
                }else if(filter==2){ // query that orders the houses based on the number of reviews
                    result = tx.run("MATCH (a:Apartment)-[:LOCATED]->(c:City {name:$cityName}) " +
                                        "OPTIONAL MATCH (a)<-[r:REVIEW]-() " +
                                        "WITH a, COUNT(r) AS reviewCount " +
                                        "RETURN a, reviewCount " +
                                        "ORDER BY CASE WHEN a.averageReviewScore IS NULL THEN 1 ELSE 0 END, reviewCount DESC " +
                                        "SKIP $elementsToSkip " +
                                        "LIMIT $elementsPerPage",
                            parameters("cityName", cityName, "elementsToSkip", elementsToSkip, "elementsPerPage",elementsPerPage));
                }

                List<Apartment> apartmentList = new ArrayList<>();
                while (Objects.requireNonNull(result).hasNext()) {
                    Record record = result.next();
                    Node apartmentNode = record.get("a").asNode();
                    Integer reviewCount = record.get("reviewCount").asInt();
                    Long apartmentId = apartmentNode.get("apartmentId").asLong();
                    String apartmentName = apartmentNode.get("name").asString();
                    String pictureUrl = apartmentNode.get("pictureUrl").asString();
                    double averageReviewScore = 0.0;
                    Value propertyValue = apartmentNode.get("averageReviewScore");
                    if (propertyValue != NULL) {
                        averageReviewScore = propertyValue.asDouble();
                    }
                    Apartment apartment = new Apartment(apartmentId, apartmentName, pictureUrl, averageReviewScore, reviewCount);
                    apartmentList.add(apartment);
                }
                return apartmentList;
            });
            return apartments;
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return null;
        }
    }

    public List<String> getAllCities() {
        try (Session session = driver.session()) {
            return session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run("MATCH (c:City) RETURN c.name");
                return getStrings(result);
            });
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return null;
        }
    }

    private List<String> getStrings(Result result) {
        List<String> cityNamesList = new ArrayList<>();
        while (result.hasNext()) {
            Record record = result.next();
            String cityNameFromDB = record.get("c.name").asString();
            cityNamesList.add(cityNameFromDB);
        }
        return cityNamesList;
    }


    public List<Review> getReviewsForApartment(Long apartmentId, Integer page, Integer elementsPerPage, Integer filter) {
        try (Session session = driver.session()) {
            int elementsToSkip =  (page-1)*elementsPerPage;
            List<Review> reviews = session.readTransaction((TransactionWork<List<Review>>) tx -> {
                Result result = null;
                if(filter==0){
                    result = tx.run("MATCH (u:User)-[r:REVIEW]->(a:Apartment {apartmentId: $apartmentId}) RETURN u,r "+
                                    "SKIP $elementsToSkip LIMIT $elementsPerPage",
                            parameters("apartmentId", apartmentId, "elementsToSkip", elementsToSkip, "elementsPerPage", elementsPerPage));
                }else if(filter==1){
                    result = tx.run("MATCH (u:User)-[r:REVIEW]->(a:Apartment {apartmentId: $apartmentId}) " +
                                    "RETURN u,r " +
                                    "ORDER BY r.score DESC "+
                                    "SKIP $elementsToSkip LIMIT $elementsPerPage",
                            parameters("apartmentId", apartmentId, "elementsToSkip", elementsToSkip, "elementsPerPage", elementsPerPage));

                }else if(filter==2){
                    result = tx.run("MATCH (u:User)-[r:REVIEW]->(a:Apartment {apartmentId: $apartmentId}) " +
                                    "RETURN u,r " +
                                    "ORDER BY r.score ASC "+
                                    "SKIP $elementsToSkip LIMIT $elementsPerPage",
                            parameters("apartmentId", apartmentId, "elementsToSkip", elementsToSkip, "elementsPerPage", elementsPerPage));

                }
                if(result == null)
                    return null;
                List<Review> reviewList = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    Node userNode = record.get("u").asNode();
                    Relationship reviewRel = record.get("r").asRelationship();
                    String userEmail = userNode.get("email").asString();
                    String comment = reviewRel.get("comment").asString();
                    float rating = reviewRel.get("score").asFloat();
                    Review review = new Review(apartmentId,userEmail, comment,rating);
                    reviewList.add(review);
                }
                return reviewList;
            });
            return reviews;
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return null;
        }
    }

    public List<Review> getReviewsForUser(String email, Integer page, Integer elementsPerPage) {
        try (Session session = driver.session()) {
            int elementsToSkip =  (page-1)*elementsPerPage;
            List<Review> reviews = session.readTransaction((TransactionWork<List<Review>>) tx -> {
                Result result;
                //result = tx.run("MATCH (u:User {email:'"+email+"'}) return u");
                result = tx.run("MATCH (u:User {email: '"+email+"'})-[r:REVIEW]->() RETURN r SKIP $elementsToSkip LIMIT $elementsPerPage;",
                        parameters( "elementsToSkip", elementsToSkip, "elementsPerPage", elementsPerPage));
                System.out.println("MATCH (u:User {email: '"+email+"'})-[r:REVIEW]->() RETURN r SKIP 0 LIMIT 10;");
                List<Review> reviewList = new ArrayList<>();
                while (result.hasNext()) {
                    System.out.println("sei qui");
                    Record record = result.next();
                    Relationship reviewRel = record.get("r").asRelationship();
                    String comment = reviewRel.get("comment").asString();
                    float rating = reviewRel.get("score").asFloat();
                    Review review = new Review(null,email, comment,rating);
                    reviewList.add(review);
                }
                return reviewList;
            });
            return reviews;
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return null;
        }
    }

    //UPDATE
    public void updateApartmentAverageReviewScore(Long apartmentId) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH ()-[r:REVIEW]->(a:Apartment {apartmentId:$apartmentId}) " +
                                "WITH a, AVG(r.score) AS averageScore " +
                                "SET a.averageReviewScore = ROUND(averageScore * 100) / 100",
                        parameters("apartmentId", apartmentId));
                return null;
            });
        }catch (Exception e){
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }

    // Method to get list of email of people followed by a user
    public List<String> getFollowsEmail(String email)
    {
        List<String> followsEmail = new ArrayList<>();
        try (Session session = driver.session())
        {
            return session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run("MATCH (u:User {email: $email})-[f:FOLLOWS]->(u2:User) RETURN u2.email",
                        parameters("email", email));
                while(result.hasNext())
                {
                    Record record = result.next();
                    String emailFromDB = record.get("u2.email").asString();
                    followsEmail.add(emailFromDB);
                }
                System.out.println("\n\n\n Follows email: " + followsEmail + "\n\n\n");
                return followsEmail;
            });
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return null;
        }
    }

    // Method to get list of email of people following a user
    public List<String> getFollowersEmail(String email)
    {
        List<String> followersEmail = new ArrayList<>();
        try (Session session = driver.session())
        {
            return session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run("MATCH (u:User {email: $email})<-[f:FOLLOWS]-(u2:User) RETURN u2.email",
                        parameters("email", email));
                while(result.hasNext())
                {
                    Record record = result.next();
                    String emailFromDB = record.get("u2.email").asString();
                    followersEmail.add(emailFromDB);
                }
                System.out.println("\n\n\n Followers email: " + followersEmail + "\n\n\n");
                return followersEmail;
            });
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return null;
        }
    }

    public void addFollow(String email, String otherEmail) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {email: $email}), (u2:User {email: $otherEmail}) " +
                                "MERGE (u)-[:FOLLOWS]->(u2)",
                        parameters("email", email, "otherEmail", otherEmail));
                return null;
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }

    public void removeFollow(String email, String otherEmail) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {email: $email})-[f:FOLLOWS]->(u2:User {email: $otherEmail}) " +
                                "DELETE f",
                        parameters("email", email, "otherEmail", otherEmail));
                return null;
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
        }
    }

    public boolean removeApartment(Long apartmentId)
    {
        try (Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Apartment {apartmentId: $apartmentId}) " +
                                "DETACH DELETE a",
                        parameters("apartmentId", apartmentId));
                return null;
            });
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return false;
        }
    }

    // Method to update apartment information
    public boolean updateApartment(Long apartmentId, String name, String pictureUrl)
    {
        try (Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Apartment {apartmentId: $apartmentId}) " +
                                "SET a.name = $name, a.pictureUrl = $pictureUrl",
                        parameters("apartmentId", apartmentId, "name", name, "pictureUrl", pictureUrl));
                return null;
            });
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            return false;
        }
    }

    public List<String> seeSuggested(String emailA, String emailB) {
        try (Session session = driver.session())
        {
            return session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run("MATCH (a:User {email: $emailA}) " +
                                "MATCH (b:User {email: $emailB}) " +
                                "MATCH (b)-[:FOLLOWS]->(suggested:User) " +
                                "WHERE NOT (a)-[:FOLLOWS]->(suggested) " +
                                "MATCH (a)-[:INTERESTS]->(city:City) " +
                                "MATCH (suggested)-[:INTERESTS]->(city) " +
                                "RETURN DISTINCT suggested.email AS suggestedEmail",
                        parameters("emailA", emailA, "emailB", emailB));
                List<String> suggestedEmails = new ArrayList<>();
                while (result.hasNext())
                {
                    suggestedEmails.add(result.next().get("suggestedEmail").asString());
                }
                return suggestedEmails;
            });
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            // Gestisci l'errore come preferisci o ritorna una lista vuota
            return Collections.emptyList();
        }
    }

    public List<String> vediSuggeriti(String mail, String otherMail) {
        try (Session session = driver.session()) {
            List<String> suggestedAccounts = session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run("MATCH (mailUser:User {email: $mail}) " +
                                "MATCH (otherUser:User {email: $otherMail}) " +
                                "MATCH (otherUser)-[:FOLLOWS]->(followed:User)-[:FOLLOWS]->(suggested:User) " +
                                "WHERE NOT (mailUser)-[:FOLLOWS]->(suggested) " +
                                "MATCH (suggested)-[:REVIEW]->(:Apartment)-[:LOCATED]->(:City)<-[:INTERESTS]-(mailUser) " +
                                "RETURN DISTINCT suggested.email AS suggestedEmail",
                        parameters("mail", mail, "otherMail", otherMail));

                List<String> suggestedEmails = new ArrayList<>();
                while (result.hasNext()) {
                    suggestedEmails.add(result.next().get("suggestedEmail").asString());
                }

                return suggestedEmails;
            });

            return suggestedAccounts;
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            new AlertDialogGraphicManager("Neo4j connection failed").show();
            // Gestisci l'errore come preferisci o ritorna una lista vuota
            return Collections.emptyList();
        }
    }





}



