package Replica2.servers;

import Replica2.movieTicketBookingSystem.user.Admin;
import Replica2.util.db.LoginDBMovieTicketSystem;
import Replica2.util.db.MovieTicketBookingDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.login.UserInfo;

import java.net.MalformedURLException;
import java.rmi.RemoteException;


//Server program
public class AtwaterServer implements ServerInterface {

	private static final Logger LOGGER = LogManager.getLogger("atwater");
	private static final int portNum = 6000;
	private MovieTicketBookingDB movieDB = new MovieTicketBookingDB();
	private LoginDBMovieTicketSystem userDB = new LoginDBMovieTicketSystem();
	private static final String registryURL = "rmi://localhost:" + portNum + "/atwater";

	public static Admin user = null;

	public static void main(String[] args) throws NumberFormatException, RemoteException {

//		InputStreamReader is = new InputStreamReader(System.in);
//		BufferedReader br = new BufferedReader(is);

		AtwaterServer atwaterServer = new AtwaterServer();
		user = new Admin(atwaterServer.movieDB,"localhost",String.valueOf(portNum)); // this is created to access method in User class

		LOGGER.info("Setup user started");
		LOGGER.info("Pre-defined users setup started for server");
		atwaterServer.setupUsers();
		System.out.println("Setup user done");
		LOGGER.info("Pre-defined users setup done for server");

		try {
			LOGGER.info("Atwater server started at port: " + portNum);
			Runnable task = () -> {
				try {
					atwaterServer.register(portNum, registryURL,atwaterServer.movieDB);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			};
			Thread thread = new Thread(task);
			thread.start();


			atwaterServer.receive(portNum, LOGGER, atwaterServer.movieDB, atwaterServer.userDB, user);


		} catch (Exception re) {
			System.out.println("Exception in Atwater.main: " + re);
		}
	}

	private void setupUsers(){
			UserInfo user1 = new UserInfo();
			user1.setUsername("ATWA1234");
			user1.setPassword("xyz");
			user1.setType("A");
			UserInfo user4 = new UserInfo();
			user4.setUsername("ATWC7075");
			user4.setPassword("xyz");
			user4.setType("C");
			userDB.addUser(user1);
			userDB.addUser(user4);

	}

	//move to interface
//	public void receive() {
//		DatagramSocket aSocket = null;
//		try {
//			aSocket = new DatagramSocket(portNum);
//
//			while(true) {
//				byte[] buffer = new byte[1000]; //clear buffer for every request
//				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
//				aSocket.receive(request);
//				LOGGER.info("Request received");
//				String response = new String(request.getData()).trim();
//				// Choice of the method to be invoked, and param if any
//				String responseMethodName = response.substring(0,response.indexOf(":")).trim();
//				String responseParam =  new String(request.getData()).substring(response.indexOf(":")+1).trim();
//				String responseParameters = new String(request.getData()).substring(response.indexOf(":")+1).trim();
//
//				String responseString = "";
//
//				if(responseMethodName.equalsIgnoreCase("getAvailableShowsForMovie") && responseParam!=null) {
////					String responseString = "";
//					responseString = movieDB.getAvailableShowsForMovie(responseParam);//to get all shows available for a movie
//					LOGGER.info("Available shows for movie " + responseParam + " are: " + responseString);
//
//					LOGGER.info("Response sent for available shows for movie: " + responseParam);
//				}
//				else if(responseMethodName.equalsIgnoreCase("getAllMoviesBookedByCustomer") && responseParam!=null) {
//
//					responseString = movieDB.getAllMoviesBookedByCustomer(responseParam);//to get all movies booked by Customer
//
//					LOGGER.info("Movies booked by customer " + responseParam + " are: " + responseString);
//					LOGGER.info("Response sent for all movies booked by customer with id: " + responseParam);
//				}
//				else if(responseMethodName.equalsIgnoreCase("bookMovieTicketForCustomer") && responseParameters!=null) {
//
//					//get all parameters after removing , from parameters.
//					List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
//					responseString = user.bookMovieTicketForCustomer(param.get(0),param.get(1),param.get(2),Integer.parseInt(param.get(3)));//to book movie
//
//					LOGGER.info("Movie booked for customer " + param.get(0) + "? : " + responseString);
//					LOGGER.info("Response sent after booking movie "+param.get(2)+ "for customer: " + param.get(0));
//				}
//				else if(responseMethodName.equalsIgnoreCase("cancelMovieTicketForCustomer") && responseParameters!=null) {
//
////					System.out.print(responseParam + ": ");
//					//get all parameters after removing , from parameters.
//					List<String> param = Arrays.asList(responseParameters.split("\\s*,\\s*"));
//					responseString = user.cancelMovieTicketsForCustomer(param.get(0),param.get(1),param.get(2),Integer.parseInt(param.get(3)));//to cancel movie
////					DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());
////					aSocket.send(reply);
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
//						LOGGER.info("No user found with user id: "+responseParam+" in Atwater");
//					}else{
//						LOGGER.info("User "+ responseParam +" found in Atwater");
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
//		} catch (IOException e) {
//			System.out.println("IO: " + e.getMessage());
//		} finally {
//			if (aSocket != null)
//				aSocket.close();
//		}
//	}
}
