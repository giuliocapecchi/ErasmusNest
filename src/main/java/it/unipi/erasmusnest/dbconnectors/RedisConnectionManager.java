package it.unipi.erasmusnest.dbconnectors;

import it.unipi.erasmusnest.graphicmanagers.AlertDialogGraphicManager;
import it.unipi.erasmusnest.model.Reservation;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.args.ExpiryOption;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RedisConnectionManager extends ConnectionManager{

    private static final int trashWeeksInterval = 1; // TODO period "x"
    private static final int reservationMonthsInterval = 2; // #month to keep a reservation alive after the expiration date

    public RedisConnectionManager() {
        super("localhost", 6379);
    }

    //CRUD OPERATIONS

    // READ
    public long getUserTTL(String email){
        long ttl = -1;
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {
            String key = "user:" + email + ":password";
            ttl = jedis.ttl(key);
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return ttl;
    }

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

    // DID: get only the reservations that are not in the trash period
    public ArrayList<Reservation> getReservationsForApartment(String houseIdToSearch) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {
            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>
            // Use the KEYS command to get all keys matching the pattern
            Set<String> keys = jedis.keys("reservation:*:" + houseIdToSearch + ":*:*:*");

            for (String key : keys) {
                if(!isReservationInTrashPeriod(key)) {
                    String[] keyParts = key.split(":");
                    reservations.add(new Reservation(keyParts[1], keyParts[2], Integer.parseInt(keyParts[3]), Integer.parseInt(keyParts[4]), Integer.parseInt(keyParts[5])));
                }
            }
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return reservations;
    }

    // DID: get only the reservations that are not in the trash period (already did in the used method)
    public ArrayList<Reservation> getReservationsForApartments(List<String> houseIds) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        for(String houseId : houseIds) {
            ArrayList<Reservation> reservationsForApartment = getReservationsForApartment(houseId);
            for(Reservation reservation : reservationsForApartment){
                ArrayList<String> attributesValues = getReservationAttributesValues(getSubKey(reservation));
                reservation.setTimestamp(java.time.LocalDateTime.parse(attributesValues.get(0)));
                reservation.setCity(attributesValues.get(1));
                reservation.setApartmentImage(attributesValues.get(2));
                reservation.setState(attributesValues.get(3));
            }
            reservations.addAll(reservationsForApartment);
        }

        return reservations;
    }

    public boolean isApartmentReserved(String houseIdToSearch) {
        boolean isReserved = false;

        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            // key design: reservation:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>
            Set<String> keys = jedis.keys("reservation:*:" + houseIdToSearch + ":*:*:*");

            for (String key : keys) {
                String[] keyParts = key.split(":");

                // if !keyParts[5].equals("0") && the reservation is not expired then set isReserved = true
                // to know if a reservation is expired check if the current date is after the reservation date
                // the reservation date is computed using the startYear and startMonth
                if(!keyParts[5].equals("0")) {
                    int startYear = Integer.parseInt(keyParts[3]);
                    int startMonth = Integer.parseInt(keyParts[4]);
                    LocalDateTime reservationDate = java.time.LocalDateTime.of(startYear, startMonth, 1, 0, 0);
                    if(LocalDateTime.now().isBefore(reservationDate)) {
                        isReserved = true;
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return isReserved;
    }

    // DID: get only the reservations that are not in the trash period
    public ArrayList<Reservation> getReservationsForUser(String  userEmail) {
        ArrayList<Reservation> reservations = new ArrayList<>();

        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            // key design: <entity>:<userEmail>:<houseId>:<startYear>:<startMonth>:<numberOfMonths>:<dateTime>
            String subKey = "reservation:" + userEmail + ":*:*:*:*";
            Set<String> keys = jedis.keys(subKey);

            for (String key : keys) {
                if (!isReservationInTrashPeriod(key)) {
                    ArrayList<String> attributesValues = getReservationAttributesValues(key);
                    String[] keyParts = key.split(":");
                    reservations.add(new Reservation(keyParts[1], keyParts[2], Integer.parseInt(keyParts[3]),
                            Integer.parseInt(keyParts[4]), Integer.parseInt(keyParts[5]),
                            java.time.LocalDateTime.parse(attributesValues.get(0)),
                            attributesValues.get(1), attributesValues.get(2), attributesValues.get(3)));

                }
            }
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return reservations;
    }

    private ArrayList<String> getReservationAttributesValues(String subKey) {

        ArrayList<String> attributesValues = new ArrayList<>();
        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            Map<String, String>hash = jedis.hgetAll(subKey);
            attributesValues.add(hash.get("timestamp"));
            attributesValues.add(hash.get("city"));
            attributesValues.add(hash.get("apartmentImage"));
            attributesValues.add(hash.get("state"));

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return attributesValues;
    }


    // CREATE

    // TODO
    public void addUser(String email, String password) {

        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            // key design: <entity>:<email>:<attribute>
            // entity: user
            // attribute: password
            String attribute = "password";

            String key = "user:" + email + ":" + attribute;

            // set the key
            jedis.set(key, password);

            // compute the seconds between now and trashWeeksInterval
            long seconds = LocalDateTime.now().until(LocalDateTime.now().plusWeeks(trashWeeksInterval), ChronoUnit.SECONDS);
            // set expiration time on the key equal to the seconds
            jedis.expire(key, seconds);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            // Commentoo altrimenti crasha
            // new AlertDialogGraphicManager("Redis connection failed").show();
        }

    }


    // OKAY
    public void addReservation(String userEmail, String houseId, String startYear, String startMonth, String numberOfMonths, String city, String apartmentImage) {

        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String dateTime = LocalDateTime.now().toString();
            String subKey = "reservation:" + userEmail + ":" + houseId + ":" + startYear + ":" + startMonth + ":" + numberOfMonths;

            Map<String, String> hash = new HashMap<>();;
            hash.put("timestamp", dateTime);
            hash.put("city", city);
            hash.put("apartmentImage", apartmentImage);
            hash.put("state", "pending"); // pending | approved | rejected | expired | reviewed
            jedis.hset(subKey, hash);

            // get the first day after the whole reservation period is expired
            LocalDateTime expirationDate = LocalDateTime.of(Integer.parseInt(startYear), Integer.parseInt(startMonth), 1, 0, 0).plusMonths(Long.parseLong(numberOfMonths));
            // add the "still alive months" and the "trash week" to the expiration date
            expirationDate = expirationDate.plusMonths(reservationMonthsInterval).plusWeeks(trashWeeksInterval);
            // compute the seconds between the dateTime timestamp and the end of the reservation
            long seconds = LocalDateTime.now().until(expirationDate, ChronoUnit.SECONDS);
            // set expiration time on the key equal to the seconds
            jedis.expire(subKey, seconds);

            setExpirationTimeOnUser(userEmail, seconds, ExpiryOption.GT);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        System.out.println("Reservation added");
    }

    private void setExpirationTimeOnUser(String email, long seconds, ExpiryOption option) {

        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String key = "user:" + email + ":password";

            // LT option set expiry only when the new expiry is less than current one
            // GT option set expiry only when the new expiry is greater than current one
            jedis.expire(key, seconds, option);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }

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

            // set the ttl to -1 to remove the expiration time
            jedis.persist(key);

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

    // OKAY
    public void deleteReservation(Reservation reservation){
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String subKey = getSubKey(reservation);
            jedis.del(subKey);

            updateExpirationTimeOnUser(reservation.getStudentEmail());

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
    }

    public void updateExpirationTimeOnUser(String email) {

        ArrayList<Reservation> reservations = getReservationsForUser(email);

        try (JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            long maxSeconds = LocalDateTime.now().until(LocalDateTime.now().plusWeeks(trashWeeksInterval), ChronoUnit.SECONDS);
            System.out.println("MAX SECONDS: " + maxSeconds);

            for(Reservation reservation : reservations) {
                String subKey = getSubKey(reservation);
                long seconds = jedis.ttl(subKey);
                if (seconds > maxSeconds) {
                    maxSeconds = seconds;
                    System.out.println("MAX SECONDS: " + maxSeconds);
                    System.out.println("SECONDS: " + seconds);
                }
            }

            setExpirationTimeOnUser(email, maxSeconds, ExpiryOption.LT);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
    }

    public void approveReservation(Reservation reservation) {
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String subKey = getSubKey(reservation);
            Map<String, String> hash = new HashMap<>();;
            hash.put("timestamp", reservation.getTimestamp().toString());
            hash.put("city", reservation.getCity());
            hash.put("apartmentImage", reservation.getApartmentImage());
            hash.put("state", "approved"); // pending | approved | rejected | expired | reviewed
            jedis.hset(subKey, hash);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
    }

    public void rejectReservation(Reservation reservation) {
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {

            String subKey = "reservation:" + reservation.getStudentEmail()
                    + ":" + reservation.getApartmentId()
                    + ":" + reservation.getStartYear()
                    + ":" + reservation.getStartMonth()
                    + ":" + 0;  // note that a rejected reservation has numberOfMonths = 0

            Map<String, String> hash = new HashMap<>();;
            hash.put("timestamp", reservation.getTimestamp().toString());
            hash.put("city", reservation.getCity());
            hash.put("apartmentImage", reservation.getApartmentImage());
            hash.put("state", "rejected"); // pending | approved | rejected | expired | reviewed
            jedis.hset(subKey, hash);

            long seconds = LocalDateTime.now().until(LocalDateTime.now().plusWeeks(trashWeeksInterval), ChronoUnit.SECONDS);
            jedis.expire(subKey, seconds);

            updateExpirationTimeOnUser(reservation.getStudentEmail());

            deleteReservation(reservation);

        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
    }

    private static String getSubKey(Reservation reservation) {
        return "reservation:" + reservation.getStudentEmail()
                + ":" + reservation.getApartmentId()
                + ":" + reservation.getStartYear()
                + ":" + reservation.getStartMonth()
                + ":" + reservation.getNumberOfMonths();
    }

    private boolean isReservationInTrashPeriod(String reservationKey){
        boolean isInTrash = false;
        try(JedisPooled jedis = new JedisPooled(super.getHost(), super.getPort())) {
            long ttl = jedis.ttl(reservationKey);
            // if the ttl of the key is less than the trashWeeksInterval then set isInTrash = true
            if(ttl > 0 && ttl < (long) trashWeeksInterval * 7 * 24 * 60 * 60) {
                isInTrash = true;
            }
        } catch (Exception e) {
            System.out.println("Connection problem: " + e.getMessage());
            new AlertDialogGraphicManager("Redis connection failed").show();
        }
        return isInTrash;
    }



}
