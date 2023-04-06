/** @Author: Raveena Choudhary, 40232370 **/
package Replica1.servers;

import Replica1.movieTicketBookingSystem.user.Admin;
import Replica1.util.db.LoginDBMovieTicketSystem;
import Replica1.util.db.MovieTicketBookingDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.login.UserInfo;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class VerdunServer implements ServerInterface {

	private static final int portNum = 5001;
	private static final String registryURL = "rmi://localhost:" + portNum + "/verdun";
	private MovieTicketBookingDB movieDB = new MovieTicketBookingDB();
	private LoginDBMovieTicketSystem userDB = new LoginDBMovieTicketSystem();

	private static final Logger LOGGER = LogManager.getLogger("verdun");

	public static Admin user = null;
	public static void main(String[] args) throws RemoteException {
		VerdunServer verdunServer = new VerdunServer();
		user = new Admin(verdunServer.movieDB,"localhost",String.valueOf(portNum));
		LOGGER.info("Pre-defined users setup started for server");
		verdunServer.setupUsers();
		LOGGER.info("Pre-defined users setup done for server");
		try {
			LOGGER.info("Starting Verdun Server ...");
			Runnable task = () -> { // to handle concurrency
				try {
					verdunServer.register(portNum, registryURL, verdunServer.movieDB);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			};
			Thread thread = new Thread(task);
			thread.start();
			verdunServer.receive(portNum,LOGGER,verdunServer.movieDB,verdunServer.userDB,user);
		} catch (Exception re) {
			System.out.println("Exception in Verdun.main: " + re);
		}

		LOGGER.info("Verdun Server started");
	}

	private void setupUsers(){

		UserInfo user2 = new UserInfo();
		user2.setUsername("VERA1234");
		user2.setPassword("xyz");
		user2.setType("A");

		UserInfo user6 = new UserInfo();
		user6.setUsername("VERC7075");
		user6.setPassword("xyz");
		user6.setType("C");

		userDB.addUser(user2);
		userDB.addUser(user6);

//		LOGGER.info("Users setup for verdun Server");
	}

//	public void receive() {
//		DatagramSocket aSocket = null;
//		try {
//			aSocket = new DatagramSocket(portNum);
//			while(true) {
//				byte[] buffer = new byte[1000];
//				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
//				aSocket.receive(request);
//				LOGGER.info("Request received");
//				String response = new String(request.getData()).trim();
//				// Choice of the method to be invoked, and param if any
//				String responseMethodName = response.substring(0,response.indexOf(":")).trim();
//				String responseParam =  new String(request.getData()).substring(response.indexOf(":")+1).trim();
//				String responseString = "";
//				String responseParameters = new String(request.getData()).substring(response.indexOf(":")+1).trim();
//
//				if(responseMethodName.equalsIgnoreCase("getAvailableShowsForMovie") && responseParam!=null) {
////					String responseString = "";
//					responseString = movieDB.getAvailableShowsForMovie(responseParam);//to get all shows available for a movie
////					DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
////					aSocket.send(reply);
//					LOGGER.info("Available shows for movie " + responseParam + " are: " + responseString);
//					LOGGER.info("Response sent for available shows for movie: " + responseParam);
//				}
//				else if(responseMethodName.equalsIgnoreCase("getAllMoviesBookedByCustomer") && responseParam!=null) {
//
////					System.out.print(responseParam + ": ");
//					responseString = movieDB.getAllMoviesBookedByCustomer(responseParam);//to get all movies booked by Customer
//
//					LOGGER.info("Movies booked by customer " + responseParam + " are: " + responseString);
//					LOGGER.info("Response sent for all movies booked by customer with id: " + responseParam);
//				}
//				else if(responseMethodName.equalsIgnoreCase("bookMovieTicketForCustomer") && responseParameters!=null) {
//
////					System.out.print(responseParam + ": ");
//					//todo get all parameters after removing , from parameters.
//					List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
//					responseString = user.bookMovieTicketForCustomer(param.get(0),param.get(1),param.get(2),Integer.parseInt(param.get(3)));//to book movie
//
//					LOGGER.info("Movie booked for customer " + param.get(0) + "? : " + responseString);
//					LOGGER.info("Response sent after booking movie "+param.get(2)+ "for customer: " + param.get(0));
//				}
//				else if(responseMethodName.equalsIgnoreCase("cancelMovieTicketForCustomer") && responseParameters!=null) {
//
//					//get all parameters after removing , from parameters.
//					List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
//					responseString = user.cancelMovieTicketsForCustomer(param.get(0),param.get(1),param.get(2),Integer.parseInt(param.get(3)));//to cancel movie
//
//					LOGGER.info("Movie cancelled for customer " + param.get(0) + "? : " + responseString);
//					LOGGER.info("Response sent after cancelling movie "+param.get(2)+ "for customer: " + param.get(0));
//				}
//				else if(responseMethodName.equalsIgnoreCase("getAllMovieNames")) {
//
//					responseString = movieDB.getAllMovieNames().stream().collect(Collectors.joining(","));
//
//					LOGGER.info("Movies available on all locations: " + responseString);
//					LOGGER.info("Response sent after getting movies from all locations: "+ responseString);
//				}
//				else if(responseMethodName.equalsIgnoreCase("getAllMovieIds") && responseParam!=null) {
//
//					responseString = movieDB.getAllMovieIdsForMovie(responseParam).stream().collect(Collectors.joining(","));
//
//					LOGGER.info("Movie Ids available for a movie: " + responseString);
//					LOGGER.info("Response sent after getting movieIds for a movie: "+ responseString);
//				}
//				else if(responseMethodName.equalsIgnoreCase("verifyUserID") && responseParam!=null) {
//					LOGGER.info("Verifying user "+responseParam+" exists on server...");
//					boolean isCustomerExist = userDB.verifyUserID(responseParam);
//					responseString = String.valueOf(isCustomerExist);
//					if(!isCustomerExist){
//						LOGGER.info("No user found with user id: "+responseParam+" in Verdun");
//					}else{
//						LOGGER.info("User "+ responseParam +" found in Verdun");
//					}
//
//				}
//
//				if(responseString!=null)
//				{
//					DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
//					aSocket.send(reply);
//				}
//				else {
//
//					responseString = "No data found";
//					DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
//					aSocket.send(reply);
//					LOGGER.info("Response sent when No data found");
//				}
//			}
//		}catch (SocketException e) {
//			System.out.println("Socket: " + e.getMessage());
//			LOGGER.info("Socket: " + e.getMessage());
//		} catch (IOException e) {
//			System.out.println("IO: " + e.getMessage());
//			LOGGER.info("IO: " + e.getMessage());
//		} finally {
//			if (aSocket != null)
//				aSocket.close();
//			LOGGER.info("Closing socket!");
//		}
//	}
}
