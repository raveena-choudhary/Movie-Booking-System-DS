package Replica2.movieTicketBookingSystem.user;

import Replica2.util.db.MovieTicketBookingDB;

import java.rmi.RemoteException;

public class Customer extends User {
    public Customer(MovieTicketBookingDB db, String hostname, String portNum) throws RemoteException {
        super(db,hostname,portNum);
        // TODO Auto-generated constructor stub
    }
}
