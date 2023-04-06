//package util;
//
//import org.apache.logging.log4j.Logger;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.rmi.RemoteException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class ClientHandler extends Thread{
//
//    private DatagramPacket request;
//    private int portNum;
//    private Logger LOGGER;
//    private MovieTicketBookingDB movieDB;
//    private LoginDBMovieTicketSystem userDB;
//    private User user;
//
//    private DatagramSocket aSocket;
//
//    public ClientHandler(DatagramSocket aSocket, DatagramPacket request, int portNum, Logger logger, MovieTicketBookingDB movieDB, LoginDBMovieTicketSystem userDB, User user) {
//
//            this.aSocket =aSocket;
//            this.request = request;
//            this.portNum=portNum;
//            this.LOGGER=logger;
//            this.movieDB=movieDB;
//            this.userDB=userDB;
//            this.user=user;
//    }
//
//    @Override
//    public void run() {
//            try {
//                String response = new String(request.getData()).trim();
//                // Choice of the method to be invoked, and param if any
//                String responseMethodName = response.substring(0, response.indexOf(":")).trim();
//                String responseParam = new String(request.getData()).substring(response.indexOf(":") + 1).trim();
//                String responseParameters = new String(request.getData()).substring(response.indexOf(":") + 1).trim();
//
//                String responseString = "";
//
//                if (responseMethodName.equalsIgnoreCase("getAvailableShowsForMovie") && responseParam != null) {
////					String responseString = "";
//                    responseString = movieDB.getAvailableShowsForMovie(responseParam);//to get all shows available for a movie
//                    LOGGER.info("Available shows for movie " + responseParam + " are: " + responseString);
//
//                    LOGGER.info("Response sent for available shows for movie: " + responseParam);
//                } else if (responseMethodName.equalsIgnoreCase("getAllMoviesBookedByCustomer") && responseParam != null) {
//
//                    responseString = movieDB.getAllMoviesBookedByCustomer(responseParam);//to get all movies booked by Customer
//
//                    LOGGER.info("Movies booked by customer " + responseParam + " are: " + responseString);
//                    LOGGER.info("Response sent for all movies booked by customer with id: " + responseParam);
//                } else if (responseMethodName.equalsIgnoreCase("bookMovieTicketForCustomer") && responseParameters != null) {
//
//                    //get all parameters after removing , from parameters.
//                    List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
//                    responseString = user.bookMovieTicketForCustomer(param.get(0), param.get(1), param.get(2), Integer.parseInt(param.get(3)));//to book movie
//
//                    LOGGER.info("Movie booked for customer " + param.get(0) + "? : " + responseString);
//                    LOGGER.info("Response sent after booking movie " + param.get(2) + "for customer: " + param.get(0));
//                } else if (responseMethodName.equalsIgnoreCase("cancelMovieTicketForCustomer") && responseParameters != null) {
//
////					System.out.print(responseParam + ": ");
//                    //get all parameters after removing , from parameters.
//                    List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
//                    responseString = user.cancelMovieTicketsForCustomer(param.get(0), param.get(1), param.get(2), Integer.parseInt(param.get(3)));//to cancel movie
////					DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
////					aSocket.send(reply);
//                    LOGGER.info("Movie cancelled for customer " + param.get(0) + "? : " + responseString);
//                    LOGGER.info("Response sent after cancelling movie " + param.get(2) + "for customer: " + param.get(0));
//                } else if (responseMethodName.equalsIgnoreCase("getAllMovieNames")) {
//
//                    responseString = movieDB.getAllMovieNames().stream().collect(Collectors.joining(","));
//
//                    LOGGER.info("Movies available on all locations: " + responseString);
//                    LOGGER.info("Response sent after getting movies from all locations: " + responseString);
//                } else if (responseMethodName.equalsIgnoreCase("getAllMovieIds") && responseParam != null) {
//
//                    responseString = movieDB.getAllMovieIdsForMovie(responseParam).stream().collect(Collectors.joining(","));
//
//                    LOGGER.info("Movie Ids available for a movie: " + responseString);
//                    LOGGER.info("Response sent after getting movieIds for a movie: " + responseString);
//                } else if (responseMethodName.equalsIgnoreCase("verifyUserID") && responseParam != null) {
//                    LOGGER.info("Verifying user " + responseParam + " exists on server...");
//                    boolean isCustomerExist = userDB.verifyUserID(responseParam);
//                    responseString = String.valueOf(isCustomerExist);
//                    if (!isCustomerExist) {
//                        LOGGER.info("No user found with user id: " + responseParam);
//                    } else {
//                        LOGGER.info("User " + responseParam);
//                    }
//
//                }
//
//                if (responseString != null) {
//                    DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
//                    aSocket.send(reply);
//                } else {
//
//                    responseString = "No data found";
//                    DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
//                    aSocket.send(reply);
//                    LOGGER.info("Response sent when No data found");
//                }
//
//
//            } catch (RemoteException e) {
//                System.out.println("Remote: " + e.getMessage());
//            } catch (IOException e1) {
//                System.out.println("IO: " + e1.getMessage());
//            }
//
//    }
//
//}
