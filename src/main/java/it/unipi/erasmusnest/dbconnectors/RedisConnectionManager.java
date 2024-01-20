package it.unipi.erasmusnest.dbconnectors;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Reservation;
import redis.clients.jedis.Jedis;
import java.util.Set;
import java.util.ArrayList;

public class RedisConnectionManager extends ConnectionManager{

    public RedisConnectionManager() {
        super("localhost", 6379);
    }

    public String getPassword(String email) {
        String value = null;
        try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {

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

    public boolean addUser(String email, String password) {

        boolean availableUsername = true;

        if(getPassword(email) != null) {
            availableUsername = false;
        } else {
            try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {

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
                return false;
            }
        }

        return availableUsername;

    }


    public void addReservation(String userEmail, String houseId, String startYear, String startMonth, String numberOfMonths) {

        try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {

            String attribute = "timestamp";

            // string with the current date and time
            String dateTime = java.time.LocalDateTime.now().toString();

            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>
            String key = "reservation:" + userEmail + ":" + houseId + ":" + startYear + ":" + startMonth + ":" + numberOfMonths + ":" + attribute;

            // set the key
            jedis.set(key, dateTime);

            // jedis.close(); // not needed with try-with-resources

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }

        //return true; // to modify in order to return the operation esit
    }

    public void addReservationWithAttributes(String userEmail, String houseId, String startYear, String startMonth, String numberOfMonths, String city, String apartmentImage) {

        try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {

            String attribute = "timestamp";
            String dateTime = java.time.LocalDateTime.now().toString();

            String subKey = "reservation:" + userEmail + ":" + houseId + ":" + startYear + ":" + startMonth + ":" + numberOfMonths + ":";

            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>
            String key = subKey + attribute;
            jedis.set(key, dateTime);

            attribute = "city";
            key = subKey + attribute;
            jedis.set(key, city);

            attribute = "apartmentImage";
            key = subKey + attribute;
            jedis.set(key, apartmentImage);

            // jedis.close(); // not needed with try-with-resources

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }

        //return true; // to modify in order to return the operation esit
    }

    public ArrayList<Reservation> getReservationsForApartment(Long houseId) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {
            // Specify the houseId you want to search for
            String houseIdToSearch = houseId.toString();

            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>

            // Use the KEYS command to get all keys matching the pattern
            Set<String> keys = jedis.keys("reservation:*:" + houseIdToSearch + ":*:*:*:timestamp");

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

    public ArrayList<Reservation> getReservationsForUser(String  userEmail) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {

            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>

            // Use the KEYS command to get all keys matching the pattern
            String subKey = "reservation:" + userEmail + ":*:*:*:*:";
            Set<String> keys = jedis.keys(subKey + "timestamp");

            for (String key : keys) {

                // remove "timestamp" at the end of the key
                String subK = key.substring(0, key.length() - 9);

                ArrayList<String> attributesValues = getAttributesForReservation(subK);

                String[] keyParts = key.split(":");
                reservations.add(new Reservation(keyParts[1], keyParts[2], Integer.parseInt(keyParts[3]),
                        Integer.parseInt(keyParts[4]), Integer.parseInt(keyParts[5]),
                        java.time.LocalDateTime.parse(attributesValues.get(0)),
                        attributesValues.get(1), attributesValues.get(2)));

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }

        return reservations;
    }

    public ArrayList<String> getAttributesForReservation(String subKey) {

        ArrayList<String> attributes = new ArrayList<>();
        attributes.add("timestamp");
        attributes.add("city");
        attributes.add("apartmentImage");

        ArrayList<String> values = new ArrayList<>();

        try(Jedis jedis = new Jedis(super.getHost(), super.getPort())) {

            for (String attribute : attributes) {
                Set<String> keys = jedis.keys(subKey + attribute);
                String key = keys.iterator().next();
                String value = jedis.get(key);
                values.add(value);
            }

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }

        return values;
    }

}
