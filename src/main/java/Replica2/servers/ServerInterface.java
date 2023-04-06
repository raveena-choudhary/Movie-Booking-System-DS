package Replica2.servers;

import Replica2.movieTicketBookingSystem.MovieTicketBookingInterface;
import Replica2.movieTicketBookingSystem.user.Admin;
import Replica2.movieTicketBookingSystem.user.Customer;
import Replica2.movieTicketBookingSystem.user.User;
import Replica2.util.db.LoginDBMovieTicketSystem;
import Replica2.util.db.MovieTicketBookingDB;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public interface ServerInterface {
    default void register(int portNum, String registryURL, MovieTicketBookingDB db) throws RemoteException, MalformedURLException {

            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(portNum);
                registry.list(); // This call will throw an exception
                // if the registry does not already exist

            } catch (RemoteException e) {
                // No valid registry at that port.
//                System.out.println("RMI registry cannot be located at port " + portNum);
                registry = LocateRegistry.createRegistry(portNum);
                System.out.println("RMI registry created at port " + portNum);
            }

            System.out.println(registryURL);
            //remote object creation for admin and customer
            MovieTicketBookingInterface exportAdmin= new Admin(db,"localhost",String.valueOf(portNum));
            MovieTicketBookingInterface exportCustomer = new Customer(db,"localhost",String.valueOf(portNum));
            registry.rebind(registryURL+"/customer", exportCustomer);
            registry.rebind(registryURL+"/admin", exportAdmin);
            for(int i=0;i<registry.list().length;i++)
            {
                System.out.println(registry.list()[i].toString());
            }


    }

    default void receive(int portNum, Logger LOGGER, MovieTicketBookingDB movieDB, LoginDBMovieTicketSystem userDB, User user) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(portNum);

            while(true) {
                byte[] buffer = new byte[1000]; //clear buffer for every request
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                LOGGER.info("Request received");

                //implement thread to handle multiple client requests.
//                Thread t = new ClientHandler(aSocket,request,portNum,LOGGER,movieDB,userDB,user);
//                t.start();


                String response = new String(request.getData()).trim();
                // Choice of the method to be invoked, and param if any
                String responseMethodName = response.substring(0,response.indexOf(":")).trim();
                String responseParam =  new String(request.getData()).substring(response.indexOf(":")+1).trim();
                String responseParameters = new String(request.getData()).substring(response.indexOf(":")+1).trim();

                String responseString = "";

                if(responseMethodName.equalsIgnoreCase("getAvailableShowsForMovie") && responseParam!=null) {
//					String responseString = "";
                    responseString = movieDB.getAvailableShowsForMovie(responseParam);//to get all shows available for a movie
                    LOGGER.info("Available shows for movie " + responseParam + " are: " + responseString);

                    LOGGER.info("Response sent for available shows for movie: " + responseParam);
                }
                else if(responseMethodName.equalsIgnoreCase("getAllMoviesBookedByCustomer") && responseParam!=null) {

                    responseString = movieDB.getAllMoviesBookedByCustomer(responseParam);//to get all movies booked by Customer

                    LOGGER.info("Movies booked by customer " + responseParam + " are: " + responseString);
                    LOGGER.info("Response sent for all movies booked by customer with id: " + responseParam);
                }
                else if(responseMethodName.equalsIgnoreCase("bookMovieTicketForCustomer") && responseParameters!=null) {

                    //get all parameters after removing , from parameters.
                    List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
                    responseString = user.bookMovieTicketForCustomer(param.get(0),param.get(1),param.get(2),Integer.parseInt(param.get(3)));//to book movie

                    LOGGER.info("Movie booked for customer " + param.get(0) + "? : " + responseString);
                    LOGGER.info("Response sent after booking movie "+param.get(2)+ "for customer: " + param.get(0));
                }
                else if(responseMethodName.equalsIgnoreCase("cancelMovieTicketForCustomer") && responseParameters!=null) {

//					System.out.print(responseParam + ": ");
                    //get all parameters after removing , from parameters.
                    List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
                    responseString = user.cancelMovieTicketsForCustomer(param.get(0),param.get(1),param.get(2),Integer.parseInt(param.get(3)));//to cancel movie
//					DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
//					aSocket.send(reply);
                    LOGGER.info("Movie cancelled for customer " + param.get(0) + "? : " + responseString);
                    LOGGER.info("Response sent after cancelling movie "+param.get(2)+ "for customer: " + param.get(0));
                }
                else if(responseMethodName.equalsIgnoreCase("getAllMovieNames")) {

                    responseString = movieDB.getAllMovieNames().stream().collect(Collectors.joining(","));

                    LOGGER.info("Movies available on all locations: " + responseString);
                    LOGGER.info("Response sent after getting movies from all locations: "+ responseString);
                }
                else if(responseMethodName.equalsIgnoreCase("getAllMovieIds") && responseParam!=null) {

                    responseString = movieDB.getAllMovieIdsForMovie(responseParam).stream().collect(Collectors.joining(","));

                    LOGGER.info("Movie Ids available for a movie: " + responseString);
                    LOGGER.info("Response sent after getting movieIds for a movie: "+ responseString);
                }
                else if(responseMethodName.equalsIgnoreCase("verifyUserID") && responseParam!=null) {
                    LOGGER.info("Verifying user "+responseParam+" exists on server...");
                    boolean isCustomerExist = userDB.verifyUserID(responseParam);
                    responseString = String.valueOf(isCustomerExist);
                    if(!isCustomerExist){
                        LOGGER.info("No user found with user id: "+responseParam);
                    }else{
                        LOGGER.info("User "+ responseParam);
                    }

                }
                else if(responseMethodName.equalsIgnoreCase("isMovieAlreadyBookedByCustomer") && responseParameters!=null) {

                    //get all parameters after removing , from parameters.
                    List<String> param = Arrays.asList(responseParameters.split("\\s*-\\s*"));
                    String param4 = null;
                    if(param.size()==4)
                    {
                        param4 = param.get(3);
                    }
                    responseString = String.valueOf(movieDB.isMovieAlreadyBookedByCustomer(param.get(0),param.get(1),param.get(2),param4));
                }

                //done for assignment-2
                else if(responseMethodName.equalsIgnoreCase("isMovieAlreadyBooked") && responseParameters!=null) {

                    //get all parameters after removing , from parameters.
                    List<String> param = Arrays.asList(responseParameters.split("\\s*-\\s*"));
                    String param4 = null;
                    if(param.size()==4)
                    {
                        param4 = param.get(3);
                    }
                    responseString = String.valueOf(movieDB.isMovieAlreadyBooked(param.get(0),param.get(1),param.get(2),param4));
                }

                if(responseString!=null)
                {
                    DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
                    aSocket.send(reply);
                }
                else {

                    responseString = "No data found";
                    DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
                    aSocket.send(reply);
                    LOGGER.info("Response sent when No data found");
                }
            }
        }catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
