package it.unipi.erasmusnest.dbconnectors;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Reservation;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class RedisConnectionManager extends ConnectionManager{

    public RedisConnectionManager() {
        super("localhost", 6379);
    }

    //CRUD OPERATIONS

    // READ

    // TODO
    public String getPassword(String email) {

        String value = null;

        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            // key design: <entity>:<email>:<attribute>
            // entity: user
            // attribute: password
            String attribute = "password";

            String key = "user:" + email + ":" + attribute;
            value = jedis.get(key);
            // jedis.close(); // not needed with try-with-resources

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return value;
    }

    // OKAY
    public ArrayList<Reservation> getReservationsForApartment(Long houseId) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String houseIdToSearch = houseId.toString();
            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>
            // Use the KEYS command to get all keys matching the pattern
            Set<String> keys = jedis.keys("reservation:*:" + houseIdToSearch + ":*:*:*");

            for (String key : keys) {
                String[] keyParts = key.split(":");
                reservations.add(new Reservation(keyParts[1], keyParts[2], Integer.parseInt(keyParts[3]), Integer.parseInt(keyParts[4]), Integer.parseInt(keyParts[5])));
            }
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return reservations;
    }

    // OKAY
    public ArrayList<Reservation> getReservationsForUser(String  userEmail) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>
            String subKey = "reservation:" + userEmail + ":*:*:*:*";
            Set<String> keys = jedis.keys(subKey);

            for (String key : keys) {

                ArrayList<String> attributesValues = getReservationAttributesValues(key);

                String[] keyParts = key.split(":");
                reservations.add(new Reservation(keyParts[1], keyParts[2], Integer.parseInt(keyParts[3]),
                        Integer.parseInt(keyParts[4]), Integer.parseInt(keyParts[5]),
                        java.time.LocalDateTime.parse(attributesValues.get(0)),
                        attributesValues.get(1), attributesValues.get(2)));
            }
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return reservations;
    }

    // OKAY
    private ArrayList<String> getReservationAttributesValues(String subKey) {

        ArrayList<String> attributesValues = new ArrayList<>();
        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            Map<String, String>hash = jedis.hgetAll(subKey);
            attributesValues.add(hash.get("timestamp"));
            attributesValues.add(hash.get("city"));
            attributesValues.add(hash.get("apartmentImage"));

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return attributesValues;
    }


    // CREATE

    // TODO
    public void addUser(String email, String password) {

        if(getPassword(email) == null) {
            try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

                // key design: <entity>:<email>:<attribute>
                // entity: user
                // attribute: password
                String attribute = "password";

                String key = "user:" + email + ":" + attribute;

                // set the key
                jedis.set(key, password);

                // get the key
                String value = jedis.get(key);

                // jedis.close(); // not needed with try-with-resources

            } catch (Exception e) {
                System.out.println("Connection problem: " + e.getMessage());
                // Commentoo altrimenti crasha
                // new AlertDialogGraphicManager("Redis connection failed").show();
            }
        }

    }


    // OKAY
    public void addReservation(String userEmail, String houseId, String startYear, String startMonth, String numberOfMonths, String city, String apartmentImage) {

        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String dateTime = java.time.LocalDateTime.now().toString();
            String subKey = "reservation:" + userEmail + ":" + houseId + ":" + startYear + ":" + startMonth + ":" + numberOfMonths;

            Map<String, String> hash = new HashMap<>();;
            hash.put("timestamp", dateTime);
            hash.put("city", city);
            hash.put("apartmentImage", apartmentImage);
            jedis.hset(subKey, hash);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        System.out.println("Reservation added");
    }

    // UPDATE

    // TODO
    public boolean updateUserPassword(String email, String password) {
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {
            // key design: <entity>:<email>:<attribute>
            // entity: user
            // attribute: password
            String attribute = "password";
            String key = "user:" + email + ":" + attribute;
            // set the key
            jedis.set(key, password);
            // jedis.close(); // not needed with try-with-resources
            return true;
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return false;
    }

    // DELETE

    // TODO
    public void deleteUser(String email) {
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {
            // key design: <entity>:<email>:<attribute>
            // entity: user
            // attribute: password
            String attribute = "password";
            String key = "user:" + email + ":" + attribute;
            // delete the key
            jedis.del(key);
            // jedis.close(); // not needed with try-with-resources
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
    }

    // OKAYy
    public void deleteReservation(Reservation reservation){
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String subKey = getSubKey(reservation);
            jedis.del(subKey);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
    }

    // OKAY
    private static String getSubKey(Reservation reservation) {
        return "reservation:" + reservation.getStudentEmail()
                + ":" + reservation.getApartmentId()
                + ":" + reservation.getStartYear()
                + ":" + reservation.getStartMonth()
                + ":" + reservation.getNumberOfMonths();
    }

}
