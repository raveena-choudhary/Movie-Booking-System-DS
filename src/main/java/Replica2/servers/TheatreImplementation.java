package Replica5.servers;

import Replica5.interfaces.BookingSystemInterface;
import Replica5.movieTicketBookingSystem.booking.Booking;
import Replica5.utils.UDPSendRecieve;
import util.OutputToUser;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TheatreImplementation extends UnicastRemoteObject implements BookingSystemInterface {

    private final String ATWATER_SERVER_PORT = "6000";
    private final String VERDUN_SERVER_PORT = "6001";
    private final String OUTREMONT_SERVER_PORT = "6002";

    protected String hostname = "localhost";
    protected String port = "";

    private static final Map<String, Integer> slotsMapPriority = new HashMap<String,Integer>(){{
                put("M", 0);
                put("A", 1);
                put("E", 2);
    }};

    private static Logger logger;
    private Map<String, Map<String, Integer>> movieData;
    //customerID -> { movieID:count* }
    private Map<String, List<Booking>> customerBookingData;

    public TheatreImplementation(Logger logger) throws RemoteException {
        super();
        movieData = new HashMap<>();
        customerBookingData = new HashMap<>();
        TheatreImplementation.logger = logger;

    }

    private static boolean isMovieInFuture(String movieID) {
        LocalDateTime now = LocalDateTime.now();
        String movieDateTime = movieID.substring(4);

        int month = Integer.parseInt(movieDateTime.substring(2, 4));
        int day = Integer.parseInt(movieDateTime.substring(0, 2));
        int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));

        LocalDate movieTime = LocalDate.of(year, month, day);

        return movieTime.isAfter(now.toLocalDate());
    }

    private static boolean isMovieInPast(String movieID) {
        LocalDateTime now = LocalDateTime.now();
        String movieDateTime = movieID.substring(4);

        int month = Integer.parseInt(movieDateTime.substring(2, 4));
        int day = Integer.parseInt(movieDateTime.substring(0, 2));
        int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));

        LocalDate movieTime = LocalDate.of(year, month, day);

        return movieTime.isBefore(now.toLocalDate());
    }

    private static boolean isMovieToday(String movieID) {
        LocalDateTime now = LocalDateTime.now();
        String movieDateTime = movieID.substring(4);

        int month = Integer.parseInt(movieDateTime.substring(2, 4));
        int day = Integer.parseInt(movieDateTime.substring(0, 2));
        int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));

        LocalDate movieTime = LocalDate.of(year, month, day);

        return movieTime.isEqual(now.toLocalDate());
    }

    private static boolean isMovieAfterGivenDate(String movieID, String newMovieID) {
//        LocalDateTime now = LocalDateTime.now();
        String movieDateTime = movieID.substring(4);

        int month = Integer.parseInt(movieDateTime.substring(2, 4));
        int day = Integer.parseInt(movieDateTime.substring(0, 2));
        int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));
        LocalDate movieTime = LocalDate.of(year, month, day);


        String movieDateTimeNew = newMovieID.substring(4);

        int month2 = Integer.parseInt(movieDateTimeNew.substring(2, 4));
        int day2 = Integer.parseInt(movieDateTimeNew.substring(0, 2));
        int year2 = Integer.parseInt("20" + movieDateTimeNew.substring(4, 6));

        LocalDate movieTimeNew = LocalDate.of(year2, month2, day2);

        return movieTimeNew.isAfter(movieTime);
    }

    private static boolean isMovieOnGivenDate(String movieID, String newMovieID) {
//        LocalDateTime now = LocalDateTime.now();
        String movieDateTime = movieID.substring(4);

        int month = Integer.parseInt(movieDateTime.substring(2, 4));
        int day = Integer.parseInt(movieDateTime.substring(0, 2));
        int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));
        LocalDate movieTime = LocalDate.of(year, month, day);


        String movieDateTimeNew = newMovieID.substring(4);

        int month2 = Integer.parseInt(movieDateTimeNew.substring(2, 4));
        int day2 = Integer.parseInt(movieDateTimeNew.substring(0, 2));
        int year2 = Integer.parseInt("20" + movieDateTimeNew.substring(4, 6));

        LocalDate movieTimeNew = LocalDate.of(year2, month2, day2);

        return movieTimeNew.isEqual(movieTime);
    }

    public void updateMovieCount(String movieName, String movieID, int noOfBookedTickets, boolean isAdd) {
        Map<String, Integer> idCountMap = movieData.get(movieName);
        int oldCount = idCountMap.get(movieID);
        idCountMap.put(movieID, isAdd ? oldCount + noOfBookedTickets : oldCount - noOfBookedTickets);
        movieData.put(movieName, idCountMap);
    }

    public void addCustomerMovieCount(String customerID, String movieName, String movieID, int capacity) {
        List<Booking> bookings = customerBookingData.get(customerID);
        if (bookings == null) {
            customerBookingData.put(customerID, new ArrayList<Booking>());
            bookings = customerBookingData.get(customerID);
        }
        bookings.add(new Booking(movieName, movieID, capacity));
        customerBookingData.put(customerID, bookings);
        System.out.println(customerBookingData);
    }

    public boolean addMovieSlotInHashMap(String movieName, String movieID, int bookingCapacity) {
        Boolean isNewSlot = true;
        Map<String, Integer> idCountMap;
        if (movieData.containsKey(movieName)) {
            idCountMap = movieData.get(movieName);
            isNewSlot = false;
//            int oldCount=idCountMap.get(movieID);
        } else {
            idCountMap = new HashMap<String, Integer>();
        }
        idCountMap.put(movieID, bookingCapacity);
        movieData.put(movieName, idCountMap);
        return isNewSlot;
    }

    @Override
    public synchronized String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        String res = "";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime afterSevenDaya = now.plusDays(7);
        String movieDateTime = movieID.substring(4);
        //System.out.println(movieDateTime);
        int month = Integer.parseInt(movieDateTime.substring(2, 4));
        int day = Integer.parseInt(movieDateTime.substring(0, 2));
        int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));

        LocalDate movieTime = LocalDate.of(year, month, day);
        // System.out.println(movieTime);
        if (movieTime.isAfter(afterSevenDaya.toLocalDate())) {
            //res = "Movie slot can only be added for next week from today's date";
            res = OutputToUser.addMovieSlotMessages(false,"Movie slot can only be added for next week from today's date.");

            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("addMovieSlots", "movieID : " + movieID + ", movieName : " + movieName + ", bookingCapacity : " + bookingCapacity, "Error", res);
            logger.log(Level.SEVERE, msg.toString());

            return res;
        } else if (movieTime.isBefore(now.toLocalDate())) {
           // res = "Movie cannot be added as it is from past date";

            res = OutputToUser.addMovieSlotMessages(false,"Movie cannot be added as it is from past date.");
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("addMovieSlots", "movieID : " + movieID + ", movieName : " + movieName + ", bookingCapacity : " + bookingCapacity, "Error", res);
            logger.log(Level.SEVERE, msg.toString());

            return res;
        }

        //
        Boolean isNewSlot = addMovieSlotInHashMap(movieName, movieID, bookingCapacity);
//        res = isNewSlot ? "Movie slot added successfully!" : "Movie slot updated successfully!";
        res = isNewSlot? OutputToUser.addMovieSlotMessages(false,"Movie slot added successfully!"):
                OutputToUser.addMovieSlotMessages(false,"Movie slot updated successfully!");
        System.out.println(movieData);

        utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("addMovieSlots", "movieID : " + movieID + ", movieName : " + movieName + ", bookingCapacity : " + bookingCapacity, "Success", res);
        logger.log(Level.INFO, msg.toString());

        return res;
    }

    public synchronized void findNextAvailableSlotAndReturnSlot(String customerID, String movieName, String movieID, int oldTicketCount) {

        String res = null;
        String oldSlot = movieID.substring(3, 4);
        Map<String, Integer> idCountMap;
        idCountMap = movieData.get(movieName);

        List<String> listOfMovieID = new ArrayList<String>(idCountMap.keySet());
        System.out.println(listOfMovieID);
        Collections.sort(listOfMovieID, (o1, o2) -> {
            String slot1 = o1.substring(3, 4);
            String slot2 = o2.substring(3, 4);


            LocalDate movieTime1 = utils.Utils.getLocalDateObFromMovieID(o1);

            LocalDate movieTime2 = utils.Utils.getLocalDateObFromMovieID(o2);

            if (movieTime1.isEqual(movieTime2)) {
                return slotsMapPriority.get(slot1) - slotsMapPriority.get(slot2);
            } else {
                return movieTime1.compareTo(movieTime2);
            }
        });
        System.out.println(listOfMovieID);
        if (idCountMap != null) {

            for (int i = 0; i < listOfMovieID.size(); i++) {
                String curMovieId = listOfMovieID.get(i);
                if (isMovieOnGivenDate(movieID, curMovieId) || isMovieAfterGivenDate(movieID, curMovieId)) {
                    String curSlot = curMovieId.substring(3, 4);
                    if (slotsMapPriority.get(curSlot) > slotsMapPriority.get(oldSlot)) {
                        res = bookMovieTickets(customerID, curMovieId, movieName, oldTicketCount);

                        if (res.equals("Booking successful")) {
                            return;
                        }
                    }
                }
            }
//                Map.Entry<String, Integer> entry = entries.next();

            //int capacity=entry.getValue();

//                String movieDateTime = curMovieId.substring(4);

//                int month = Integer.parseInt(movieDateTime.substring(2, 4));
//                int day = Integer.parseInt(movieDateTime.substring(0, 2));
//                int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));
//
//                LocalDate movieTime = LocalDate.of(year, month, day);

            //same day or next days


        }
        //  LocalDateTime now = LocalDateTime.now();
//            idCountMap.
//                    idCountMap.forEach((curMovieId, capacity) -> {


//                    });


//        return res;
    }

//    public String getCustomerTicketsInCurrentTheatre(String customerID) {
//
//        if (customerBookingData.containsKey(customerID)) {
//            StringBuilder sb = new StringBuilder();
//            AtomicInteger count = new AtomicInteger();
//
//            List<Booking> bookingData = customerBookingData.get(customerID);
//            if (bookingData != null && bookingData.size() != 0) {
//                bookingData.forEach((booking) -> {
//
//                    sb.append(booking.getMovieName() + " " + booking.getMovieID() + " " + booking.getCapacity() + (count.get() == bookingData.size() - 1 ? "" : ","));
//                    count.getAndIncrement();
//                });
//            }
//            //mID 10,MID 20
//            return sb.toString();
//
//        }
//        return "";
//
//    }

    public void findCustomersAndAdjustTimings(String movieName, String movieID) {
        String res = null;
        if (customerBookingData != null) {
            customerBookingData.forEach((customerID, bookingArrayList) -> {
//                    bookingArrayList.forEach((booking) -> {
//                        if (booking.getMovieID().equals(movieID)) {
//                            //find next available_slot
//
//                        }
//                    });
                Booking oldBooking = bookingArrayList.stream().filter(booking -> booking.getMovieID().equals(movieID)).findFirst().orElse(null);
                //  List<Booking> newBookingListAfterDeleting = bookingArrayList.stream().filter(booking -> !booking.getMovieID().equals(movieID)).toList();
                if (oldBooking != null) {
                    bookingArrayList.remove(oldBooking);
                    //find next
                    findNextAvailableSlotAndReturnSlot(customerID, movieName, movieID, oldBooking.getCapacity());


                }

            });
        }
    }

    @Override
    public synchronized String removeMovieSlots(String movieID, String movieName) throws RemoteException {
        Map<String, Integer> idCountMap;
        String res = "";
        if (movieData.containsKey(movieName) && movieData.get(movieName).containsKey(movieID)) {
//
//            LocalDateTime now = LocalDateTime.now();
//            String movieDateTime = movieID.substring(4);
//
//            int month = Integer.parseInt(movieDateTime.substring(2, 4));
//            int day = Integer.parseInt(movieDateTime.substring(0, 2));
//            int year = Integer.parseInt("20" + movieDateTime.substring(4, 6));
//
//            LocalDate movieTime = LocalDate.of(year, month, day);
            // System.out.println(movieTime);
            if (isMovieInPast(movieID)) {

                res = "Movie is from past date, user cannot delete it.";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("removeMovieSlots", "movieID : " + movieID + ", movieName : " + movieName, "Error", res);
                logger.log(Level.SEVERE, msg.toString());
                return res;
            }
            idCountMap = movieData.get(movieName);
            idCountMap.remove(movieID);
            movieData.put(movieName, idCountMap);

            //find customers and adjust timings()
            findCustomersAndAdjustTimings(movieName, movieID);
            System.out.println(movieData);
            System.out.println(customerBookingData);
            //------
            //sucess
            res = "Movie slot deleted successfully!";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("removeMovieSlots", "movieID : " + movieID + ", movieName : " + movieName, "Success", res);
            logger.log(Level.INFO, msg.toString());

            return res;
        } else {
            //unsucess
            res = "Movie with movieName :" + movieName + "does not exist in system.";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("removeMovieSlots", "movieID : " + movieID + ", movieName : " + movieName, "Error", res);
            logger.log(Level.SEVERE, msg.toString());

            return res;
        }


    }

    @Override
    public String listMovieShowsAvailability(String movieName) throws RemoteException {
        return getLocalMoviesShowsAvailability(movieName);
    }

    public String getLocalMoviesShowsAvailability(String movieName) {
        StringBuilder sb = new StringBuilder();
        AtomicInteger count = new AtomicInteger();
        // sb.append(movieName + ":");
        Map<String, Integer> currentMovieData = movieData.get(movieName);
        if (currentMovieData != null) {
            currentMovieData.forEach((k, v) -> {
                sb.append(k + " " + v + (count.get() == currentMovieData.size() - 1 ? "" : ","));
                count.getAndIncrement();
            });
        }

        return sb.toString();
    }

    private boolean checkIfSameSlotInOtherTheatre(String customerID, String movieID, String movieName) {
        String movieSlotWithDate = movieID.substring(3);

        if (!customerBookingData.containsKey(customerID) || customerBookingData.get(customerID).size() == 0)
            return false;
        List<Booking> customerBookingList = customerBookingData.get(customerID);

        for (int i = 0; i < customerBookingList.size(); i++) {
            Booking booking = customerBookingList.get(i);
            if (booking.getMovieID().substring(3).equals(movieSlotWithDate) && movieName.equals(booking.getMovieName()))
                return true;
        }
        return false;
    }

    @Override
    public synchronized String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String res = null;
        if (isMovieInPast(movieID)) {
            res = "Cannot book the movie show that occurred from the before the current date";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);
            logger.log(Level.SEVERE, msg.toString());

            return res;
        }

        String clientFromServer = customerID.substring(0, 3);
        String movieInServer = movieID.substring(0, 3);


        if (clientFromServer.equals(movieInServer)) {
            res = bookMovieTicketsInLocalServer(customerID, movieID, movieName, numberOfTickets);
            if (res.equals("Booking successful")) {
                addCustomerMovieCount(customerID, movieName, movieID, numberOfTickets);
            }

            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Operation Sucessful", res);
            logger.log(Level.INFO, msg.toString());
        } else {

            if (checkIfSameSlotInOtherTheatre(customerID, movieID, movieName)) {
                res = "Cannot book the movie show as the same movie with same slot is booked in another theatre";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);
                logger.log(Level.SEVERE, msg.toString());

                return res;
            }

            //getCount tckets other servers
            int countOfTicketFromOtherTheatres = getCountOfTicketFromOtherTheatres(customerID);
            if (countOfTicketFromOtherTheatres + numberOfTickets <= 3) {
                //at most 3 movies from other areas overall
                res = "SEND_TO_SERVERS";
            } else if (numberOfTickets > 3) {
                res = "You can not book more than 3 tickets in other theatres for a week";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);
                logger.log(Level.SEVERE, msg.toString());
            } else {
                res = "You have already booked 3 or more tickets in other theatres. So cannot be book more tickets for a week";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);
                logger.log(Level.SEVERE, msg.toString());

            }


        }
        return res;
    }

    public synchronized String bookMovieTicketsInLocalServer(String customerID, String movieID, String movieName, int numberOfTickets) {

        String res = "";
        //search movie
        if (!movieData.containsKey(movieName)) {
            res = movieName + " is not avaialble in the current theatre.";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

            logger.log(Level.SEVERE, msg.toString());
            return res;
        }
        if (!movieData.get(movieName).containsKey(movieID)) {
            res = movieID + " movie slot is not available in the current theatre.";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);
            logger.log(Level.SEVERE, msg.toString());
            return res;
        }
        int movieCapacity = movieData.get(movieName).get(movieID);
        //see capacity
        if (numberOfTickets > movieCapacity) {
            res = movieID + " movie slot does not have enough capacity to book " + numberOfTickets + " tickets the current theatre.";

            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);
            logger.log(Level.SEVERE, msg.toString());
            return res;
        }


        //if sucess
        updateMovieCount(movieName, movieID, numberOfTickets, false);

        res = "Booking successful";

        utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Operation Sucessful", res);

        logger.log(Level.INFO, msg.toString());

        return res;


        //diff server -> check customer other server tickets
//        return null;

    }

    public int getCountOfTicketFromOtherTheatres(String customerID) {
        if (!customerBookingData.containsKey(customerID))
            return 0;


        // System.out.println(movieTime);

        String clientFromServer = customerID.substring(0, 3);
        List<Booking> bookings = customerBookingData.get(customerID);
//        return bookings.stream().filter(booking -> booking. ).map(booking -> booking.getCapacity()) reduce(0, (integer, booking) -> !booking.getMovieID().substring(0, 3).equals(clientFromServer) ? integer + booking.getCapacity() : integer);
        int otherTheatreBookingCount = bookings.stream().filter(booking -> isMovieInFuture(booking.getMovieID()) && !booking.getMovieID().substring(0, 3).equals(clientFromServer)).mapToInt(Booking::getCapacity).sum();
        return otherTheatreBookingCount;
    }


    @Override
    public synchronized String getBookingSchedule(String customerID) {
        String res = "";
        if (!customerBookingData.containsKey(customerID) || customerBookingData.get(customerID).size() == 0) {
            res = "Customer has not booked any tickets yet";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getBookingSchedule", "customerID : " + customerID, "Error", res);

            logger.log(Level.SEVERE, msg.toString());
        } else {

            List<Booking> bookings = customerBookingData.get(customerID);
            res = bookings.toString();
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getBookingSchedule", "customerID : " + customerID, "Operation Sucessful", res);

            logger.log(Level.INFO, msg.toString());

        }

//        String result = Stream.of(getCustomerTicketsInCurrentTheatre(customerID), getCustomerTicketsInOtherTheatres(customerID))
//                .filter(s -> s != null && !s.isEmpty())
//                .collect(Collectors.joining(","));
//        utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getBookingSchedule", "customerID : " + customerID, "Operation Sucessful", result);
//
//        logger.log(Level.INFO, msg.toString());
//        return result;
        return res;
    }

    @Override
    public String exchangeTickets(String customerID, String movieID, String newMovieID, String oldMovieName, String movieName, int numberOfTickets) throws RemoteException {
        //check customer booking exists
        String res = "";
        if (!checkBookingExists(customerID, movieID)) {
            res = "The customer does not have the " + movieID + " slot booked";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("exchangeTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

            logger.log(Level.SEVERE, msg.toString());
        }

        return res;
        //if yes
        // same theatre exchange


        // other theatre exchange
        //book in other theatre if success then cancelMovieTickets
        //if no


    }

//    @Override
//    public synchronized String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException {
//        return null;
//    }


    public boolean checkBookingExists(String customerID, String movieID) {

        if (customerBookingData.containsKey(customerID) && customerBookingData.get(customerID).size() > 0) {
            List<Booking> bookings = customerBookingData.get(customerID);


            Booking oldBooking = bookings.stream().filter(booking -> booking.getMovieID().equals(movieID)).findFirst().orElse(null);

            return oldBooking != null;
        }

        return false;
    }

    public synchronized String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String res = "";
        if (customerBookingData.containsKey(customerID) && customerBookingData.get(customerID).size() > 0) {
            List<Booking> bookings = customerBookingData.get(customerID);


            Booking oldBooking = bookings.stream().filter(booking -> booking.getMovieID().equals(movieID)).findFirst().orElse(null);

            if (oldBooking == null) {
                res = "The customer does not have the " + movieID + " slot booked";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("cancelMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

                logger.log(Level.SEVERE, msg.toString());
            } else if (oldBooking.getCapacity() < numberOfTickets) {
                res = "The customer does not have " + numberOfTickets + " tickets booked";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("cancelMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

                logger.log(Level.SEVERE, msg.toString());
            } else if (oldBooking.getCapacity() > numberOfTickets) {
                oldBooking.setCapacity(oldBooking.getCapacity() - numberOfTickets);
                bookings = bookings.stream().filter(booking -> !booking.getMovieID().equals(movieID)).collect(Collectors.toList());
                bookings.add(oldBooking);
                customerBookingData.put(customerID, bookings);
//                updateMovieCount(movieName, movieID, numberOfTickets, true);
                res = "tickets cancelled successfully";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("cancelMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "OPERATION_SUCCESSFUL", res);

                logger.log(Level.INFO, msg.toString());
            } else {
                res = "tickets cancelled successfully";
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("cancelMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "OPERATION_SUCCESSFUL", res);

                logger.log(Level.INFO, msg.toString());
                bookings = bookings.stream().filter(booking -> !booking.getMovieID().equals(movieID)).collect(Collectors.toList());
//                updateMovieCount(movieName, movieID, numberOfTickets, true);
                customerBookingData.put(customerID, bookings);
            }


        } else {
            res = "The customer does not have any tickets booked";
            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("cancelMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

            logger.log(Level.SEVERE, msg.toString());

        }
        System.out.println(customerBookingData);
        System.out.println(movieData);
        return res;
    }

    @Override
    public synchronized String shutDown() throws RemoteException
    {
        movieData = new HashMap<>();
        customerBookingData = new HashMap<>();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignored
                }
                System.exit(1);
            }
        });
        return "Shutting down";
    }

    @Override
    public String getAllMovieNames() throws RemoteException {
        String method = "getAllMovieNamesFromTheatres";
//        String responseFromLocal = "";
        String responseFromAtwater = UDPSendRecieve.sendMessageToGetAllMovies(method, ATWATER_SERVER_PORT, hostname).trim();
        String responseFromVerdun = UDPSendRecieve.sendMessageToGetAllMovies(method, VERDUN_SERVER_PORT, hostname).trim();
        String responseFromOutremont = UDPSendRecieve.sendMessageToGetAllMovies(method, OUTREMONT_SERVER_PORT, hostname).trim();
        return concatNonBlankResponses(responseFromAtwater, responseFromVerdun, responseFromOutremont);
    }

    public String getAllMovieNamesFromTheatres()
    {
        return getAllMovieNamesFromMap().stream().collect(Collectors.joining(","));
    }

    public String getAllMovieIdsFromTheatres(String movieName)
    {
        return getAllMovieIdsForMovie(movieName).stream().collect(Collectors.joining(","));
    }

    @Override
    public String getAllMovieIds(String movieName) throws RemoteException{

        String method = "getAllMovieIdsFromTheatres";
//        String responseFromLocal = "";
        String responseFromAtwater="";
        String responseFromVerdun="";
        String responseFromOutremont="";

        if (movieName != null) {
            //responseFromLocal = getAllMovieIdsForMovie(movieName).stream().collect(Collectors.joining(","));
            responseFromAtwater = UDPSendRecieve.sendMessageToGetAllMovies(method, ATWATER_SERVER_PORT, hostname).trim();
            responseFromVerdun = UDPSendRecieve.sendMessageToGetAllMovies(method, VERDUN_SERVER_PORT, hostname).trim();
            responseFromOutremont = UDPSendRecieve.sendMessageToGetAllMovies(method, OUTREMONT_SERVER_PORT, hostname).trim();
        }

        return concatNonBlankResponses(responseFromAtwater, responseFromVerdun, responseFromOutremont);
    }

    private synchronized List<String> getAllMovieNamesFromMap() {
        List<String> moviesList = new ArrayList<String>();

        if (movieData.keySet() != null) {
            for (String movieName : movieData.keySet()) {
                moviesList.add(movieName);
            }
        }


        return moviesList;
    }

    private synchronized List<String> getAllMovieIdsForMovie(String movieName) {
        List<String> movieIdsList = new ArrayList<String>();

        Map<String, Integer> movies = movieData.get(movieName);

        if (movies != null) {
            for (String movieId : movies.keySet()) {
                movieIdsList.add(movieId);
            }
        }


        return movieIdsList;
    }

    public synchronized static String concatNonBlankResponses(String... response) {
        return Stream.of(response).filter((val) -> val != null && !val.equals(" ") && !val.trim().isEmpty() && !val.equals("No data found")).collect(Collectors.joining(","));
    }

}
