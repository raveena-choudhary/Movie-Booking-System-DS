package Replica3.movieTicketBookingSystem.user;

import Replica3.util.db.MovieTicketBookingDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.OutputToUser;
import Replica3.util.UDPCommunication;

import java.rmi.RemoteException;

public class Admin extends User {

    private static final Logger ATWLOGGER = LogManager.getLogger("atwater");
    private static final Logger VERLOGGER = LogManager.getLogger("verdun");
    private static final Logger OUTLOGGER = LogManager.getLogger("outremont");

//    private final String ATWATER_SERVER_PORT = "7000";
//    private final String VERDUN_SERVER_PORT = "7001";
//    private final String OUTREMONT_SERVER_PORT = "7002";

    public Admin(MovieTicketBookingDB db, String hostname, String port) throws RemoteException {
        super(db, hostname, port);
    }

    @Override
    public synchronized String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws RemoteException{

        //add pre-conditions
        //dont allow admin to add movie slots lesser than todays date
        if (bookingUtility.validateMovieDateForPastWeek(movieID))
//            return "Movie cannot be added as it is from past date";

            return OutputToUser.addMovieSlotMessages(false,"Movie cannot be added as it is from past date.");

        //dont allow admin to add movie slots for more than a week
        if (bookingUtility.validateMovieDateForNextWeek(movieID))
//            return "Movie slot can only be added for next week from today's date";
            return OutputToUser.addMovieSlotMessages(false,"Movie slot can only be added for next week from today's date.");

        //movie slot already existing with same booking capacity
        if (validateMovieSlotAlreadyExists(movieName,movieID,bookingCapacity))
//            return "Movie slot already exists!";

            return OutputToUser.addMovieSlotMessages(false,"Movie slot already exists!");

        //String message = "Some issue occured while adding a movie slot. Please check the entered data!";
        String message = OutputToUser.addMovieSlotMessages(false,"Some issue occured while adding a movie slot. Please check the entered data!");
        if (movieID != null && movieName != null && bookingCapacity > 0) {
            System.out.println("Add movies called");
            if (movieDb.addMovie(movieID, movieName, bookingCapacity)) {
                //message = "Movie slot added successfully!";
                message = OutputToUser.addMovieSlotMessages(true,"Movie slot added successfully!");
            }
        }

        return message;
    }

    @Override
    public synchronized String removeMovieSlots(String movieID, String movieName) throws RemoteException {

        String message = "Some issue occured while deleting a movie slot. Please check the entered data!";

        if (movieID != null && movieName != null) {
            if (!movieDb.isMovieExist(movieName, movieID)) {
                return "Movie with movieName :" + movieName + "does not exist in system.";
            }

            //movie exists in db and movie from past date, dont allow admin to remove it.
            if (movieDb.isMovieExist(movieName, movieID) && bookingUtility.validateMovieDateForPastWeek(movieID)) {
                return "Movie is from past date, user cannot delete it.";
            }

            //if client booked ticket for deleted slot, book next slot for client. Otherwise delete movie.
            //if movie does not exist in system, then add message "Movie slot does not exist in system."
            if (movieDb.deleteMovieAndBookNextSlot(movieID, movieName) && movieDb.deleteMovie(movieID, movieName)) {
                message = "Movie slot deleted successfully!";
            }

        }

        return message;
    }

    @Override
    public synchronized String listMovieShowsAvailability(String movieName) throws RemoteException{
        //udp communication
        String response = "";

        String listOfAvailableShows = movieDb.getAvailableShowsForMovie(movieName);

        response = (movieName + ":" + listOfAvailableShows).trim();
        String getAvailableShowsForMovieMethod = "getAvailableShowsForMovie";
        switch (port) {
            case ATWATER_SERVER_PORT: {
                ATWLOGGER.info(String.format("Sending request to get all {} shows from Verdun Server ..."), movieName);
                String responseFromVerdun = UDPCommunication.sendMessage(getAvailableShowsForMovieMethod, VERDUN_SERVER_PORT, hostname, movieName).trim();
                ATWLOGGER.info(String.format("Response from verdun : {} "), responseFromVerdun);

                ATWLOGGER.info(String.format("Sending request to get all {} shows from Outremont Server..."), movieName);
                String responseFromOutremont = UDPCommunication.sendMessage(getAvailableShowsForMovieMethod, OUTREMONT_SERVER_PORT, hostname, movieName).trim();
                ATWLOGGER.info(String.format("Response from outremont : {} "), responseFromOutremont);
                return concatNonBlankResponses(response, responseFromVerdun, responseFromOutremont);
            }
            case VERDUN_SERVER_PORT: {
                VERLOGGER.info(String.format("Sending request to get all {} shows from Atwater Server..."), movieName);
                String responseFromAtwater = UDPCommunication.sendMessage(getAvailableShowsForMovieMethod, ATWATER_SERVER_PORT, hostname, movieName).trim();

                VERLOGGER.info(String.format("Sending request to get all {} shows from Outremont Server..."), movieName);
                String responseFromOutremont = UDPCommunication.sendMessage(getAvailableShowsForMovieMethod, OUTREMONT_SERVER_PORT, hostname, movieName).trim();
                return concatNonBlankResponses(response, responseFromAtwater, responseFromOutremont);
            }
            case OUTREMONT_SERVER_PORT: {
                OUTLOGGER.info(String.format("Sending request to get all {} shows from Verdun Server..."), movieName);
                String responseFromVerdun = UDPCommunication.sendMessage(getAvailableShowsForMovieMethod, VERDUN_SERVER_PORT, hostname, movieName).trim();

                OUTLOGGER.info(String.format("Sending request to get all {} shows from Atwater Server..."), movieName);
                String responseFromAtwater = UDPCommunication.sendMessage(getAvailableShowsForMovieMethod, ATWATER_SERVER_PORT, hostname, movieName).trim();
                return concatNonBlankResponses(response, responseFromVerdun, responseFromAtwater);
            }
        }

        return response;
    }

    private boolean validateMovieSlotAlreadyExists(String movieName,String movieID,int bookingCapacity)
    {
        return movieDb.isMovieSlotExist(movieName,movieID,bookingCapacity);
    }



}
