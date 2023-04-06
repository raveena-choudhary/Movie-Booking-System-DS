package Replica5.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BookingSystemInterface extends Remote {

    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) throws RemoteException;
    public String removeMovieSlots (String movieID,String movieName) throws RemoteException;
    public String listMovieShowsAvailability (String movieName) throws RemoteException;
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException;

    public String getBookingSchedule(String customerID) throws RemoteException;

    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException;

    public String exchangeTickets(String customerID, String movieID, String newMovieID, String oldMovieName, String movieName, int numberOfTickets) throws RemoteException;

    public String shutDown() throws RemoteException;


    public String getAllMovieNames() throws RemoteException;
    public String getAllMovieIds(String movieName) throws RemoteException;
}
