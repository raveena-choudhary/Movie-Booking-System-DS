package Replica2.movieTicketBookingSystem.user;

import Replica2.movieTicketBookingSystem.MovieTicketBookingInterface;
import Replica2.util.UDPCommunication;
import Replica2.util.booking.Booking;
import Replica2.util.booking.Movie;
import Replica2.util.db.LoginDBMovieTicketSystem;
import Replica2.util.db.MovieTicketBookingDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Enums.ServerEnum;
import util.Enums.SlotEnum;
import Replica1.util.booking.BookingUtility;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class User extends UnicastRemoteObject implements MovieTicketBookingInterface {

    protected MovieTicketBookingDB movieDb;
    private LoginDBMovieTicketSystem loginDb = new LoginDBMovieTicketSystem();

    protected String hostname = "";
    protected String port = "";
    protected static final String ATWATER_SERVER_PORT = "6000";
    protected static final String VERDUN_SERVER_PORT = "6001";
    protected static final String OUTREMONT_SERVER_PORT = "6002";

    private static final int ACCEPTED_MOVIE_COUNT = 3;

    private static final Logger ATWLOGGER = LogManager.getLogger("atwater");
    private static final Logger VERLOGGER = LogManager.getLogger("verdun");
    private static final Logger OUTLOGGER = LogManager.getLogger("outremont");

    protected BookingUtility bookingUtility = new BookingUtility();

    public User(MovieTicketBookingDB db, String hostname, String port) throws RemoteException{
        super();
        this.movieDb = db;
//        this.loginDb = loginDb;
        this.hostname = hostname;
        this.port = port;
        //init user
    }

    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws RemoteException {
        return null;
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) throws RemoteException
    {
        return null;
    }

    @Override
    public String listMovieShowsAvailability(String movieName) throws RemoteException {
       return null;
    }

    @Override
    public synchronized String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException {
        String response = "";
        //based on customerID, implement udp to call that server for ticket booking
        String movieTheatreLocationForBooking = ServerEnum.getEnumNameForValue(movieID.substring(0, 3)).toLowerCase();
        String customerLocation = ServerEnum.getEnumNameForValue(customerID.substring(0, 3)).toLowerCase();

        //check if movie already booked at any location.
        if(!checkMovieBookingAtAllLocations(customerID,movieID,movieName).equals("Movie not booked at any location"))
        {
            return "Movie already booked for same slot or with same name";
        }

        //if customer location and movie where it is booking are different
        if (!customerLocation.equals(movieTheatreLocationForBooking)) {
            //verify if user exists in system
            if (!verifyUserOnAllServers(customerID)) {
                return "Please check customerID, it does not exist in system.";
            }

            switch (movieTheatreLocationForBooking) {
                case "atwater": {
                    ATWLOGGER.info(String.format("Sending request to book ticket for movie {} on Atwater Server ..."), movieName);
                    String responseFromAtwater = UDPCommunication.sendMessageToBookTicket(ATWATER_SERVER_PORT, hostname, customerID, movieID, movieName, String.valueOf(numberOfTickets)).trim();
                    ATWLOGGER.info(String.format("Response from atwater : {} "), responseFromAtwater);
                    return responseFromAtwater;
                }
                case "verdun": {
                    VERLOGGER.info(String.format("Sending request to book ticket for movie {} on Verdun Server ..."), movieName);
                    String responseFromVerdun = UDPCommunication.sendMessageToBookTicket(VERDUN_SERVER_PORT, hostname, customerID, movieID, movieName, String.valueOf(numberOfTickets)).trim();
                    VERLOGGER.info(String.format("Response from verdun : {} "), responseFromVerdun);
                    return responseFromVerdun;
                }
                case "outremont": {
                    OUTLOGGER.info(String.format("Sending request to book ticket for movie {} on Outremont Server ..."), movieName);
                    String responseFromOutremont = UDPCommunication.sendMessageToBookTicket(OUTREMONT_SERVER_PORT, hostname, customerID, movieID, movieName, String.valueOf(numberOfTickets)).trim();
                    OUTLOGGER.info(String.format("Response from outremont : {} "), responseFromOutremont);
                    return responseFromOutremont;
                }

            }
        } else {
            //check in server , which customers belong
            return bookMovieTicketForCustomer(customerID, movieID, movieName, numberOfTickets);
        }


        return response;

    }

    public synchronized String bookMovieTicketForCustomer(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException{
        String response = "";
//        boolean movieBooked = true;
        String message = "Some issue occured while booking a movie slot. Please check the entered data Or All seats are full";

        int availableNumberOfTickets = movieDb.getBookingCapactiyForAMovie(movieName, movieID);
        System.out.println("availableNumberOfTickets:" + availableNumberOfTickets);

        if (customerID != null && movieID != null && movieName != null && numberOfTickets>=0) {
            //check if movie is available in theater
            if (!movieDb.isMovieExist(movieName, movieID)) {
                return "Movie is not available in Theater";
            }

            //user cannot book movie with previous date from today's date
            if (bookingUtility.validateMovieDateForPastWeek(movieID))
                return "Movie is no longer available for booking as it is from past date";

            //user cannot book movie with movie date more than a week
            if (bookingUtility.validateMovieDateForNextWeek(movieID))
                return "Movie can only be booked for next week from today's date";

            // if customer booking to other region than check booking db if already out of quota for week then throw exception
            String getAllMoviesBookedByCustomer = null;
            getAllMoviesBookedByCustomer=getBookingSchedule(customerID);

            int numberOfTicketsAlreadyBookedByCustomer = 0;

            if(!getAllMoviesBookedByCustomer.equals("") && getAllMoviesBookedByCustomer!=null) {
                String[] listOfMoviesBooked = getAllMoviesBookedByCustomer.split(",");
                long movieCount = 0;
                String movieNameFromBookingSchedule = "";

                //A:ATWATER MORNING 080223 4,B:VERDUN AFTERNOON 080223 2,D:OUTREMONT AFTERNOON 080223 2,F:OUTREMONT MORNING 080223 2
                for (int index = 0; index < listOfMoviesBooked.length; index++) {
                    if (!listOfMoviesBooked[index].contains(customerID.substring(0, 3)))
                        movieCount++;
                }

                if (movieCount >= ACCEPTED_MOVIE_COUNT) {
                    return "You have exhausted booking quota for this week";
                }

                //get number of tickets for movie already booked
                Movie movie = new Movie(movieID);
                for (int i = 0; i < listOfMoviesBooked.length; i++) {
                    String[] dataForMovie = listOfMoviesBooked[i].split(" ");
                    movieNameFromBookingSchedule = dataForMovie[0].substring(0, dataForMovie[0].indexOf(":"));
                    String slotFromBookingSchedule = String.valueOf(SlotEnum.valueOf(dataForMovie[1]));
                    String dateFromBookingSchedule = dataForMovie[2];
                    if (movieNameFromBookingSchedule.equals(movieName) && slotFromBookingSchedule.equals(SlotEnum.getEnumNameForValue(movie.getSlot())) && dateFromBookingSchedule.equals(movie.getDate())) {
                        numberOfTicketsAlreadyBookedByCustomer = Integer.parseInt(dataForMovie[3]);
                        break;
                    }
                }
            }
            //now update movie database to update capacity
            //if successful update booking database (customer details)

            if (numberOfTickets>0 && numberOfTickets <= availableNumberOfTickets) {
                //before booking
                //check movie booked in local theatre to get already booked tickets number
                //int numberOfTicketsForBooking = 0;
                //if (checkMovieBookingAtLocalTheatre(customerID, movieID, movieName)) {
               // numberOfTicketsForBooking = movieDb.getNumberOfTicketsForCustomerBooking(customerID, movieID,movieName);
                //}

                //if movie is not booked at local theatre, number of tickets for booking=0
                int totalTicketsToBeBooked = numberOfTicketsAlreadyBookedByCustomer + numberOfTickets;

                Booking booking = movieDb.createBooking(movieID, movieName, totalTicketsToBeBooked);
                boolean booked = movieDb.bookTickets(customerID, movieID,booking,numberOfTickets);



                if (booked)
                    message = "Movie booked successfully!";
            }
        }

        return message;
    }

    @Override
    public synchronized String getBookingSchedule(String customerID) throws RemoteException {
        //udp communication
        String response = "";

        //add getAllMovies for the server which customer belongs
        response =movieDb.getAllMoviesBookedByCustomer(customerID);
        if(response.endsWith(","))
        {
            response = response.substring(0,response.length()-1).trim();
        }
        String getAllMoviesBookedByCustomerMethod = "getAllMoviesBookedByCustomer";

        switch (port) {
            case ATWATER_SERVER_PORT: {
                ATWLOGGER.info(String.format("Sending request to get all movies booked by customer {} on Verdun Server ..."), customerID);
                String responseFromVerdun = UDPCommunication.sendMessage(getAllMoviesBookedByCustomerMethod, VERDUN_SERVER_PORT, hostname, customerID).trim();
                ATWLOGGER.info(String.format("Response received from Verdun server {} ..."), responseFromVerdun);
                ATWLOGGER.info(String.format("Sending request to get all movies booked by customer {} on Outremont Server ..."), customerID);
                String responseFromOutremont = UDPCommunication.sendMessage(getAllMoviesBookedByCustomerMethod, OUTREMONT_SERVER_PORT, hostname, customerID).trim();
                ATWLOGGER.info(String.format("Response received from Outremont server {} ..."), responseFromOutremont);

                if(responseFromVerdun.endsWith(","))
                {
                    responseFromVerdun = responseFromVerdun.substring(0,responseFromVerdun.length()-1).trim();
                }
                if(responseFromOutremont.endsWith(","))
                {
                    responseFromOutremont = responseFromOutremont.substring(0,responseFromOutremont.length()-1).trim();
                }

                return concatNonBlankResponses(response, responseFromVerdun, responseFromOutremont);
            }
            case VERDUN_SERVER_PORT: {
                VERLOGGER.info(String.format("Sending request to get all movies booked by customer {} on Atwater Server ..."), customerID);
                String responseFromAtwater = UDPCommunication.sendMessage(getAllMoviesBookedByCustomerMethod, ATWATER_SERVER_PORT, hostname, customerID).trim();
                VERLOGGER.info(String.format("Response received from Atwater server {} ..."), customerID);
                VERLOGGER.info(String.format("Sending request to get all movies booked by customer {} on Outremont Server ..."), customerID);
                String responseFromOutremont = UDPCommunication.sendMessage(getAllMoviesBookedByCustomerMethod, OUTREMONT_SERVER_PORT, hostname, customerID).trim();
                VERLOGGER.info(String.format("Response received from Outremont Server ..."), customerID);

                if(responseFromAtwater.endsWith(","))
                {
                    responseFromAtwater = responseFromAtwater.substring(0,responseFromAtwater.length()-1).trim();
                }
                if(responseFromOutremont.endsWith(","))
                {
                    responseFromOutremont = responseFromOutremont.substring(0,responseFromOutremont.length()-1).trim();
                }

                return concatNonBlankResponses(response, responseFromAtwater, responseFromOutremont);
            }
            case OUTREMONT_SERVER_PORT: {
                OUTLOGGER.info(String.format("Sending request to get all movies booked by customer {} on Verdun Server ..."), customerID);
                String responseFromVerdun = UDPCommunication.sendMessage(getAllMoviesBookedByCustomerMethod, VERDUN_SERVER_PORT, hostname, customerID).trim();
                OUTLOGGER.info(String.format("Response received from Verdun server {} ..."), responseFromVerdun);
                OUTLOGGER.info(String.format("Sending request to get all movies booked by customer {} on Atwater Server ..."), customerID);
                String responseFromAtwater = UDPCommunication.sendMessage(getAllMoviesBookedByCustomerMethod, ATWATER_SERVER_PORT, hostname, customerID).trim();
                OUTLOGGER.info(String.format("Response received from Atwater server {} ..."), customerID);

                if(responseFromAtwater.endsWith(","))
                {
                    responseFromAtwater = responseFromAtwater.substring(0,responseFromAtwater.length()-1).trim();
                }
                if(responseFromVerdun.endsWith(","))
                {
                    responseFromVerdun = responseFromVerdun.substring(0,responseFromVerdun.length()-1).trim();
                }

                return concatNonBlankResponses(response, responseFromVerdun, responseFromAtwater);
            }
        }

        return response;
    }

    @Override
    public synchronized String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets)  throws RemoteException {
        String response = "";

        //based on customerID, implement udp to call that server for ticket booking
        String movieTheatreLocationForBooking = ServerEnum.getEnumNameForValue(movieID.substring(0, 3)).toLowerCase();
        String customerLocation = ServerEnum.getEnumNameForValue(customerID.substring(0, 3)).toLowerCase();

        //if customer location and movie where it is booking are different
        if (!customerLocation.equals(movieTheatreLocationForBooking)) {
            switch (movieTheatreLocationForBooking) {
                case "atwater": {
                    ATWLOGGER.info(String.format("Sending request to cancel ticket for movie {} on Atwater Server ..."), movieName);
                    String responseFromAtwater = UDPCommunication.sendMessageToCancelTicket(ATWATER_SERVER_PORT, hostname, customerID, movieID, movieName, String.valueOf(numberOfTickets)).trim();
                    ATWLOGGER.info(String.format("Response received from Atwater Server ..."), responseFromAtwater);
                    return responseFromAtwater;
//				break;
                }
                case "verdun": {
                    VERLOGGER.info(String.format("Sending request to cancel ticket for movie {} on Verdun Server ..."), movieName);
                    String responseFromVerdun = UDPCommunication.sendMessageToCancelTicket(VERDUN_SERVER_PORT, hostname, customerID, movieID, movieName, String.valueOf(numberOfTickets)).trim();
                    VERLOGGER.info(String.format("Response received from Verdun Server ..."), responseFromVerdun);
                    return responseFromVerdun;
                }
                case "outremont": {
                    OUTLOGGER.info(String.format("Sending request to cancel ticket for movie {} on Outremont Server ..."), movieName);
                    String responseFromOutremont = UDPCommunication.sendMessageToCancelTicket(OUTREMONT_SERVER_PORT, hostname, customerID, movieID, movieName, String.valueOf(numberOfTickets)).trim();
                    OUTLOGGER.info(String.format("Response received from Outremont Server ..."), movieName);
                    return responseFromOutremont;
                }

            }
        } else {

            //check in server , which customers belong
            return cancelMovieTicketsForCustomer(customerID, movieID, movieName, numberOfTickets);
        }


        return response;
    }

    public String cancelMovieTicketsForCustomer(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException {

        String message = "Some issue occured while cancelling a movie slot. Please check the entered data!";

        if (customerID != null && movieID != null && movieName != null && numberOfTickets > 0) {
            if (movieDb.cancelBookingAndUpdateBookingCapacity(customerID, movieID, movieName, numberOfTickets))
                message = "Booking cancelled successfully!";
        }

        return message;
    }

    public synchronized MovieTicketBookingDB getMovieTicketBookingDB() {
        return movieDb;
    }

    //renaming method to check movie booking at all locations - based on assignment 2
    //private synchronized String checkMovieBookingAtOtherLocations(String customerID, String movieID, String movieName) {
//    private synchronized String checkMovieBookingAtAllLocations(String customerID, String movieID, String movieName) {
//
//        String areaCode = movieID.substring(0, 3);
//
////        String customerLocation = customerID.substring(0, 3);
//
////        if (!customerLocation.equals(areaCode)) {
//        switch (areaCode) {
//            //check movie at atwater
//            case "ATW": {
//                if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//                    return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";
//                }
//            }
//
//            //check movie at outremont
//            case "OUT": {
//                if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//                    return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";
//                }
//            }
//
//            //check movie at Verdun
//            case "VER": {
//                if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//                    return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";
//                }
//            }
//        }
//
//        return "Movie not booked at any location";
//
//    }

    public synchronized String checkMovieBookingAtAllLocations(String customerID, String movieID, String movieName) throws RemoteException {

        String response = "";

        String areaCode = movieID.substring(0, 3);

        String customerLocation = customerID.substring(0, 3);

        String methodName ="isMovieAlreadyBookedByCustomer";

        String moviesBookedByCustomer = null;
        moviesBookedByCustomer=getBookingSchedule(customerID);

//        // check movie for the server which customer belongs
//        if(movieDb.isMovieAlreadyBookedByCustomer(customerID,movieID,movieName))
//        {
//            return "Movie already booked at " + ServerEnum.getEnumNameForValue(customerLocation) + " for same slot or with same movieName";
//        }

//        if (!customerLocation.equals(areaCode)) {

//            switch (areaCode) {
//            //check movie at atwater
//            case "ATW": {
                ATWLOGGER.info(String.format("Sending request to check movie Booked with {} on Atwater Server ..."), movieName);
                String responseFromAtwater = UDPCommunication.sendMessageToIsMovieAlreadyBookedByCustomer(methodName,ATWATER_SERVER_PORT, hostname, customerID, movieID, movieName,moviesBookedByCustomer).trim();
                ATWLOGGER.info(String.format("Response received from Atwater Server ..."), responseFromAtwater);
                if (Boolean.parseBoolean(responseFromAtwater)) {
                    return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";

                }
                ATWLOGGER.info(String.format("Response received from Atwater Server ..."), "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName");
//            }

            //check movie at outremont
//            case "OUT": {
                OUTLOGGER.info(String.format("Sending request to check movie Booked with {} on Outremont Server ..."), movieName);
                String responseFromOutremont = UDPCommunication.sendMessageToIsMovieAlreadyBookedByCustomer(methodName,OUTREMONT_SERVER_PORT, hostname, customerID, movieID, movieName,moviesBookedByCustomer).trim();
                OUTLOGGER.info(String.format("Response received from Outremont Server ..."), responseFromAtwater);
                if (Boolean.parseBoolean(responseFromOutremont)) {
                    return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";

                }
                OUTLOGGER.info(String.format("Response received from Outremont Server ..."), "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName");
//
//            }

            //check movie at Verdun
//            case "VER": {
                VERLOGGER.info(String.format("Sending request to check movie Booked with {} on Verdun Server ..."), movieName);
                String responseFromVerdun = UDPCommunication.sendMessageToIsMovieAlreadyBookedByCustomer(methodName,VERDUN_SERVER_PORT, hostname, customerID, movieID, movieName,moviesBookedByCustomer).trim();
                VERLOGGER.info(String.format("Response received from Verdun Server ..."), responseFromAtwater);
                if (Boolean.parseBoolean(responseFromVerdun)) {
                    return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";
                }
                VERLOGGER.info(String.format("Response received from Verdun Server ..."), "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName");
//
//            }
//        }
//    }
        return "Movie not booked at any location";

}

    //done for assignment -2

    public synchronized String checkBookingAtAllLocations(String customerID, String movieID, String movieName) throws RemoteException {

        String response = "";

        String areaCode = movieID.substring(0, 3);

        String customerLocation = customerID.substring(0, 3);

        String methodName ="isMovieAlreadyBooked";

        String moviesBookedByCustomer = null;
        moviesBookedByCustomer=getBookingSchedule(customerID);


//        // check movie for the server which customer belongs
//        if(movieDb.isMovieAlreadyBookedByCustomer(customerID,movieID,movieName))
//        {
//            return "Movie already booked at " + ServerEnum.getEnumNameForValue(customerLocation) + " for same slot or with same movieName";
//        }

//        if (!customerLocation.equals(areaCode)) {

//            switch (areaCode) {
//            //check movie at atwater
//            case "ATW": {
        ATWLOGGER.info(String.format("Sending request to check movie Booked with {} on Atwater Server ..."), movieName);
        String responseFromAtwater = UDPCommunication.sendMessageToIsMovieAlreadyBookedByCustomer(methodName,ATWATER_SERVER_PORT, hostname, customerID, movieID, movieName,moviesBookedByCustomer).trim();
        ATWLOGGER.info(String.format("Response received from Atwater Server ..."), responseFromAtwater);
        if (Boolean.parseBoolean(responseFromAtwater)) {
            return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";

        }
        ATWLOGGER.info(String.format("Response received from Atwater Server ..."), "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName");
//            }

        //check movie at outremont
//            case "OUT": {
        OUTLOGGER.info(String.format("Sending request to check movie Booked with {} on Outremont Server ..."), movieName);
        String responseFromOutremont = UDPCommunication.sendMessageToIsMovieAlreadyBookedByCustomer(methodName,OUTREMONT_SERVER_PORT, hostname, customerID, movieID, movieName,moviesBookedByCustomer).trim();
        OUTLOGGER.info(String.format("Response received from Outremont Server ..."), responseFromAtwater);
        if (Boolean.parseBoolean(responseFromOutremont)) {
            return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";

        }
        OUTLOGGER.info(String.format("Response received from Outremont Server ..."), "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName");
//
//            }

        //check movie at Verdun
//            case "VER": {
        VERLOGGER.info(String.format("Sending request to check movie Booked with {} on Verdun Server ..."), movieName);
        String responseFromVerdun = UDPCommunication.sendMessageToIsMovieAlreadyBookedByCustomer(methodName,VERDUN_SERVER_PORT, hostname, customerID, movieID, movieName,moviesBookedByCustomer).trim();
        VERLOGGER.info(String.format("Response received from Verdun Server ..."), responseFromAtwater);
        if (Boolean.parseBoolean(responseFromVerdun)) {
            return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName";
        }
        VERLOGGER.info(String.format("Response received from Verdun Server ..."), "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode) + " for same slot or with same movieName");
//
//            }
//        }
//    }
        return "Movie not booked at any location";

    }

    @Override
    public synchronized boolean validateUser(String username, String password) throws RemoteException {
        return loginDb.verifyUser(username, password);
    }

//    private synchronized String checkMovieBookingAtOtherLocations(String customerID, String movieID, String movieName) {
//
//        String areaCode = movieID.substring(0, 3);
//        String customerLocation = customerID.substring(0, 3);
//
//        if (!customerLocation.equals(areaCode)) {
//            switch (areaCode) {
//                //check movie at atwater
//                case "ATW": {
//                    if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//                        return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode);
//                    }
//                }
//
//                //check movie at outremont
//                case "OUT": {
//                    if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//                        return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode);
//                    }
//                }
//
//                //check movie at Verdun
//                case "VER": {
//                    if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//                        return "Movie already booked at " + ServerEnum.getEnumNameForValue(areaCode);
//                    }
//                }
//            }
//        }
//        return "Movie not booked at other locations";
//    }
//
//    //check movie booked at local theatre
//    private synchronized boolean checkMovieBookingAtLocalTheatre(String customerID, String movieID, String movieName) {
//        if (movieDb.isMovieAlreadyBookedByCustomer(customerID, movieID, movieName)) {
//            return true;
//        }
//
//        return false;
//
//    }

    private synchronized List<String> getMovieIdsForAWeek(String customerID, String movieID) {
        List<String> movieIds = new ArrayList<>();
        switch (customerID.substring(0, 3)) {
            case "ATW": {
                movieIds.addAll(getMovieIdsForWeekByArea(movieID, "OUT"));
                movieIds.addAll(getMovieIdsForWeekByArea(movieID, "VER"));
                break;
            }
            case "OUT": {
                movieIds.addAll(getMovieIdsForWeekByArea(movieID, "ATW"));
                movieIds.addAll(getMovieIdsForWeekByArea(movieID, "VER"));
                break;
            }
            case "VER": {
                movieIds.addAll(getMovieIdsForWeekByArea(movieID, "OUT"));
                movieIds.addAll(getMovieIdsForWeekByArea(movieID, "ATW"));
                break;
            }
            default:
                System.out.println("Customer ID " + customerID + " does not exist.");

        }
        return movieIds;


    }

    private synchronized List<String> getMovieIdsForWeekByArea(String movieID, String area) {
        List<String> movieIds = new ArrayList<>();

        Movie movie = new Movie(movieID);
        movie.setAreaCode(area);
        movie.setSlot("M"); //ids generated for morning slot
        movieIds.addAll(bookingUtility.getMovieIdsForWeek(movie.getMovieId()));
        movie.setSlot("A");  //ids generated for afternoon slot
        movieIds.addAll(bookingUtility.getMovieIdsForWeek(movie.getMovieId()));
        movie.setSlot("E"); //ids generated for evening slot
        movieIds.addAll(bookingUtility.getMovieIdsForWeek(movie.getMovieId()));

        return movieIds;
    }

    @Override
    public void setPortAndHost(String hostname, String port) {
        this.hostname = hostname;
        this.port = port;
    }

    public synchronized static String concatNonBlankResponses(String... response) {
        return Stream.of(response).filter((val) -> val != null && !val.equals(" ") && !val.trim().isEmpty() && !val.equals("No data found")).collect(Collectors.joining(","));
    }

    @Override
    public String getAllMovieNames() throws RemoteException {
        String method = "getAllMovieNames";
        String responseFromLocal = movieDb.getAllMovieNames().stream().collect(Collectors.joining(","));

        switch (port) {
            case ATWATER_SERVER_PORT: {
                ATWLOGGER.info(String.format("Sending request to get all movie names on Verdun Server ..."));
                String responseFromVerdun = UDPCommunication.sendMessageToGetAllMovies(method, VERDUN_SERVER_PORT, hostname).trim();
                ATWLOGGER.info(String.format("Response received from Verdun Server {}..."),responseFromVerdun);
                ATWLOGGER.info(String.format("Sending request to get all movie names on Outremont Server ..."));
                String responseFromOutremont = UDPCommunication.sendMessageToGetAllMovies(method, OUTREMONT_SERVER_PORT, hostname).trim();
                ATWLOGGER.info(String.format("Response received from Outremont Server {}..."),responseFromOutremont);
                return concatNonBlankResponses(responseFromLocal, responseFromVerdun, responseFromOutremont);
            }
            case VERDUN_SERVER_PORT: {
                VERLOGGER.info(String.format("Sending request to get all movie names on Atwater Server ..."));
                String responseFromAtwater = UDPCommunication.sendMessageToGetAllMovies(method, ATWATER_SERVER_PORT, hostname).trim();
                VERLOGGER.info(String.format("Response received from Atwater Server {}..."),responseFromAtwater);
                VERLOGGER.info(String.format("Sending request to get all movie names on Outremont Server ..."));
                String responseFromOutremont = UDPCommunication.sendMessageToGetAllMovies(method, OUTREMONT_SERVER_PORT, hostname).trim();
                VERLOGGER.info(String.format("Response received from Outremont Server {}..."),responseFromOutremont);
                return concatNonBlankResponses(responseFromLocal, responseFromAtwater, responseFromOutremont);
            }
            case OUTREMONT_SERVER_PORT: {
                OUTLOGGER.info(String.format("Sending request to get all movie names on Verdun Server ..."));
                String responseFromVerdun = UDPCommunication.sendMessageToGetAllMovies(method, VERDUN_SERVER_PORT, hostname).trim();
                OUTLOGGER.info(String.format("Response received from Verdun Server {}..."),responseFromVerdun);
                OUTLOGGER.info(String.format("Sending request to get all movie names on Atwater Server ..."));
                String responseFromAtwater = UDPCommunication.sendMessageToGetAllMovies(method, ATWATER_SERVER_PORT, hostname).trim();
                OUTLOGGER.info(String.format("Response received from Atwater Server {}..."),responseFromAtwater);
                return concatNonBlankResponses(responseFromLocal, responseFromVerdun, responseFromAtwater);
            }
        }


        return responseFromLocal;

    }

    @Override
    public String getAllMovieIds(String movieName) throws RemoteException{

        String method = "getAllMovieIds";
        String responseFromLocal = "";

        if (movieName != null) {
            responseFromLocal = movieDb.getAllMovieIdsForMovie(movieName).stream().collect(Collectors.joining(","));

            switch (port) {
                case ATWATER_SERVER_PORT: {
                    ATWLOGGER.info(String.format("Sending request to get all movie ids on Verdun Server ..."));
                    String responseFromVerdun = UDPCommunication.sendMessageToGetAllMovies(method, VERDUN_SERVER_PORT, hostname, movieName).trim();
                    ATWLOGGER.info(String.format("Response received from Verdun server {}.."),responseFromVerdun);
                    ATWLOGGER.info(String.format("Sending request to get all movie ids on Outremont Server ..."));
                    String responseFromOutremont = UDPCommunication.sendMessageToGetAllMovies(method, OUTREMONT_SERVER_PORT, hostname, movieName).trim();
                    ATWLOGGER.info(String.format("SResponse received from Outremont server {}.."),responseFromOutremont);
                   return concatNonBlankResponses(responseFromLocal, responseFromVerdun, responseFromOutremont);
                }
                case VERDUN_SERVER_PORT: {
                    VERLOGGER.info(String.format("Sending request to get all movie ids on Atwater Server ..."));
                    String responseFromAtwater = UDPCommunication.sendMessageToGetAllMovies(method, ATWATER_SERVER_PORT, hostname, movieName).trim();
                    VERLOGGER.info(String.format("Response received from Atwater server {}.."),responseFromAtwater);
                    VERLOGGER.info(String.format("Sending request to get all movie ids on Outremont Server ..."));
                    String responseFromOutremont = UDPCommunication.sendMessageToGetAllMovies(method, OUTREMONT_SERVER_PORT, hostname, movieName).trim();
                    VERLOGGER.info(String.format("Response received from Outremont Server {}..."),responseFromOutremont);
                    return concatNonBlankResponses(responseFromLocal, responseFromAtwater, responseFromOutremont);
                }
                case OUTREMONT_SERVER_PORT: {
                    OUTLOGGER.info(String.format("Sending request to get all movie ids on verdun Server ..."));
                    String responseFromVerdun = UDPCommunication.sendMessageToGetAllMovies(method, VERDUN_SERVER_PORT, hostname, movieName).trim();
                    OUTLOGGER.info(String.format("Response received from Verdun server {}.."),responseFromVerdun);
                    OUTLOGGER.info(String.format("Sending request to get all movie ids on Atwater Server ..."));
                    String responseFromAtwater = UDPCommunication.sendMessageToGetAllMovies(method, ATWATER_SERVER_PORT, hostname, movieName).trim();
                    OUTLOGGER.info(String.format("Response received from Verdun server {}.."),responseFromAtwater);
                    return concatNonBlankResponses(responseFromLocal, responseFromVerdun, responseFromAtwater);
                }
            }

        }
        return responseFromLocal;
    }

    @Override
    public String exchangeTickets(String customerID, String old_movieName, String old_movieID, String new_movieID, String new_movieName, int numberOfTickets) throws RemoteException {

        String responseFromBookTicket = "";
        String responseFromCancelTicket = "";
        String response = "Error exchanging movie Tickets";

        //checkMovieAlreadyBookedByCustomer
        if(!checkMovieBookingAtAllLocations(customerID,old_movieID,old_movieName).equals("Movie not booked at any location")) {

            String getAllMoviesBookedByCustomer = getBookingSchedule(customerID);
            String[] listOfMoviesBooked = getAllMoviesBookedByCustomer.split(",");

            //checking other locations get number of tickets for old movie
            int numberOfTicketsForOldMovie = 0;

            Movie old_movie = new Movie(old_movieID);
            String date = old_movie.getDate();
            String slot = SlotEnum.getEnumNameForValue(old_movie.getSlot());
            String loc = ServerEnum.getEnumNameForValue(old_movie.getAreaCode());

            //A:ATWATER MORNING 080223 4
            for(int index = 0; index<listOfMoviesBooked.length;index++)
            {
                if(listOfMoviesBooked[index].contains(old_movieName) &&  listOfMoviesBooked[index].contains(date) && listOfMoviesBooked[index].contains(slot) && listOfMoviesBooked[index].contains(loc))
                {
                    numberOfTicketsForOldMovie = Integer.parseInt(listOfMoviesBooked[index].substring(listOfMoviesBooked[index].length()-1).trim());
                    break;
                }
            }

            //cancel old movie, if old movie already booked- should not be greater more than already booked tickets- as in cancellation
            //we can cancel only number of tickets booked by customer.
            //if number of tickets are not same for both old movie and new movie
//            if (numberOfTickets > numberOfTicketsForOldMovie) {
//                responseFromCancelTicket = cancelMovieTickets(customerID, old_movieID, old_movieName, numberOfTicketsForOldMovie);
//            }

            //if numberOfTickets to be exchanged are greater than number of tickets previously booked.
            if(numberOfTickets>numberOfTicketsForOldMovie)
            {
                return "You cannot exchange tickets more than already booked tickets for old movie.";
            }
            else if (numberOfTickets == numberOfTicketsForOldMovie) {
                responseFromCancelTicket = cancelMovieTickets(customerID, old_movieID, old_movieName, numberOfTickets);
//                numberOfTickets = numberOfTickets + numberOfTicketsForOldMovie;
            }
            else if(numberOfTickets<numberOfTicketsForOldMovie) {
                //int ticketDiff = numberOfTicketsForOldMovie - numberOfTickets;
                responseFromCancelTicket = cancelMovieTickets(customerID, old_movieID, old_movieName, numberOfTickets);
            }

            if (responseFromCancelTicket.equals("Booking cancelled successfully!")) {
                    //book new movie for customer by checking capacity and then cancel old movie id booking
                    String new_movieTheatreLocationForBooking = ServerEnum.getEnumNameForValue(new_movieID.substring(0, 3)).toLowerCase();
                    String customerLocation = ServerEnum.getEnumNameForValue(customerID.substring(0, 3)).toLowerCase();

                if(checkMovieBookingAtAllLocations(customerID,new_movieID,new_movieName).equals("Movie not booked at any location")) {
                    //if customer location and movie where it is booking are different
                    if (!customerLocation.equals(new_movieTheatreLocationForBooking)) {
                        //verify if user exists in system
                        if (!verifyUserOnAllServers(customerID)) {
                            return "Please check customerID, it does not exist in system.";
                        }


                        switch (new_movieTheatreLocationForBooking) {
                            case "atwater": {
                                ATWLOGGER.info(String.format("Sending request to book ticket for movie {} on Atwater Server ..."), new_movieName);
                                String responseFromAtwater = UDPCommunication.sendMessageToBookTicket(ATWATER_SERVER_PORT, hostname, customerID, new_movieID, new_movieName, String.valueOf(numberOfTickets)).trim();
                                ATWLOGGER.info(String.format("Response from atwater : {} "), responseFromAtwater);
                                responseFromBookTicket = responseFromAtwater;
                                break;
                            }
                            case "verdun": {
                                VERLOGGER.info(String.format("Sending request to book ticket for movie {} on Verdun Server ..."), new_movieName);
                                String responseFromVerdun = UDPCommunication.sendMessageToBookTicket(VERDUN_SERVER_PORT, hostname, customerID, new_movieID, new_movieName, String.valueOf(numberOfTickets)).trim();
                                VERLOGGER.info(String.format("Response from verdun : {} "), responseFromVerdun);
                                responseFromBookTicket = responseFromVerdun;
                                break;
                            }
                            case "outremont": {
                                OUTLOGGER.info(String.format("Sending request to book ticket for movie {} on Outremont Server ..."), new_movieName);
                                String responseFromOutremont = UDPCommunication.sendMessageToBookTicket(OUTREMONT_SERVER_PORT, hostname, customerID, new_movieID, new_movieName, String.valueOf(numberOfTickets)).trim();
                                OUTLOGGER.info(String.format("Response from outremont : {} "), responseFromOutremont);
                                responseFromBookTicket = responseFromOutremont;
                                break;
                            }

                        }

                    } else {
                        //check in server , which customers belong
                        responseFromBookTicket = bookMovieTicketForCustomer(customerID, new_movieID, new_movieName, numberOfTickets);
                    }
                }

                    //if new movie booked successfully
                    if (responseFromBookTicket.equals("Movie booked successfully!")) {

                        response = "Movie exchanged successfully";
                    } else //rollback operation if new booking fails
                    {
                        responseFromBookTicket = rollBackExchangeOperation(customerID, old_movieName, old_movieID, numberOfTickets);
                        if (responseFromBookTicket.equals("Movie booked successfully!")) {
                            System.out.println("Rollback Success!");
                        }
                    }
                }

            }

        return response;

    }

    private synchronized String rollBackExchangeOperation(String customerID, String old_movieName, String old_movieID,int numberOfTicketsForOldMovie) throws RemoteException
    {
        //rollback cancel
        String responseFromBookTicket = "";
        String old_movieTheatreLocationForBooking = ServerEnum.getEnumNameForValue(old_movieID.substring(0, 3)).toLowerCase();
        String customerLocation = ServerEnum.getEnumNameForValue(customerID.substring(0, 3)).toLowerCase();

        //if customer location and movie where it is booking are different
        if (!customerLocation.equals(old_movieTheatreLocationForBooking)) {
            //verify if user exists in system
            if (!verifyUserOnAllServers(customerID)) {
                return "Please check customerID, it does not exist in system.";
            }

            switch (old_movieTheatreLocationForBooking) {
                case "atwater": {
                    ATWLOGGER.info(String.format("Sending request to book ticket for movie {} on Atwater Server ..."), old_movieName);
                    String responseFromAtwater = UDPCommunication.sendMessageToBookTicket(ATWATER_SERVER_PORT, hostname, customerID, old_movieID, old_movieName, String.valueOf(numberOfTicketsForOldMovie)).trim();
                    ATWLOGGER.info(String.format("Response from atwater : {} "), responseFromAtwater);
                    responseFromBookTicket = responseFromAtwater;
                    break;
                }
                case "verdun": {
                    VERLOGGER.info(String.format("Sending request to book ticket for movie {} on Verdun Server ..."), old_movieName);
                    String responseFromVerdun = UDPCommunication.sendMessageToBookTicket(VERDUN_SERVER_PORT, hostname, customerID, old_movieID, old_movieName, String.valueOf(numberOfTicketsForOldMovie)).trim();
                    VERLOGGER.info(String.format("Response from verdun : {} "), responseFromVerdun);
                    responseFromBookTicket = responseFromVerdun;
                    break;
                }
                case "outremont": {
                    OUTLOGGER.info(String.format("Sending request to book ticket for movie {} on Outremont Server ..."), old_movieName);
                    String responseFromOutremont = UDPCommunication.sendMessageToBookTicket(OUTREMONT_SERVER_PORT, hostname, customerID, old_movieID, old_movieName, String.valueOf(numberOfTicketsForOldMovie)).trim();
                    OUTLOGGER.info(String.format("Response from outremont : {} "), responseFromOutremont);
                    responseFromBookTicket = responseFromOutremont;
                    break;
                }

            }
        }
        else {
            //check in server , which customers belong
            responseFromBookTicket = bookMovieTicketForCustomer(customerID, old_movieID, old_movieName, numberOfTicketsForOldMovie);
        }

        return responseFromBookTicket;
    }

    private synchronized List<String> getListFromCommaSeparatedString(String response) {
        return Stream.of(response.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private synchronized String[] getArrayFromCommaSeparatedString(String response) {
        return Stream.of(response.split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }

    private synchronized boolean verifyUserOnAllServers(String userID){


        ATWLOGGER.info(String.format("Sending Request to Atwater server for verifying user id {}.."),userID);
        String responseFromAtwater = UDPCommunication.sendMessage("verifyUserID",ATWATER_SERVER_PORT, hostname,userID).trim();
        ATWLOGGER.info(String.format("Response received from Atwater server {}.."),responseFromAtwater);
        VERLOGGER.info(String.format("Sending Request to Verdun server for verifying user id {}.."),userID);
        String responseFromVerdun = UDPCommunication.sendMessage("verifyUserID",VERDUN_SERVER_PORT, hostname,userID).trim();
        VERLOGGER.info(String.format("Response received from Verdun server {}.."),responseFromVerdun);
        OUTLOGGER.info(String.format("Sending Request to Outremont server for verifying user id {}.."),userID);
        String responseFromOutremont = UDPCommunication.sendMessage("verifyUserID",OUTREMONT_SERVER_PORT, hostname,userID).trim();
        OUTLOGGER.info(String.format("Response received from Outremont server {}.."),responseFromOutremont);
        //return true if user exist on any of server
        return Boolean.parseBoolean(responseFromAtwater) || Boolean.parseBoolean(responseFromVerdun) || Boolean.parseBoolean(responseFromOutremont);
    }

    @Override
    public synchronized String shutDown() throws RemoteException
    {
        movieDb=new MovieTicketBookingDB();
        loginDb = new LoginDBMovieTicketSystem();

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
}
