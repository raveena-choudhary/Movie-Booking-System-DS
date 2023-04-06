package Replica2.movieTicketBookingSystem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

//Remote interface
public interface MovieTicketBookingInterface extends Remote{

	//admin
	public String addMovieSlots (String movieID, String movieName, int bookingCapacity) throws RemoteException;
	public String removeMovieSlots (String movieID, String movieName) throws RemoteException;
	public String listMovieShowsAvailability (String movieName) throws RemoteException;
	
	//customer & admin
	public String bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException;
	public String getBookingSchedule (String customerID) throws RemoteException;
	public String cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException;
	public boolean validateUser(String username, String password) throws RemoteException;
	public void setPortAndHost(String hostname, String port) throws RemoteException;
//	public List<String> getAllMovieNames() throws RemoteException;
//	public List<String> getAllMovieIds(String movieName) throws RemoteException;

	public String getAllMovieNames() throws RemoteException;
	public String getAllMovieIds(String movieName) throws RemoteException;

	public String shutDown() throws RemoteException;
	public String exchangeTickets (String customerID, String old_movieName, String movieID,String new_movieID,String new_movieName,int numberOfTickets) throws RemoteException;

}
