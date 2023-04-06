package clients;
/** @Author: Raveena Choudhary, 40232370 **/


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import util.Enums.ServerEnum;
import util.Enums.SlotEnum;
import util.booking.Movie;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Scanner;

//client program
public class AdminClient {

    private static final Logger LOGGER = LogManager.getLogger("admin");
    private static String userName = "";
    private static String password = "";
    private static final String HOSTNAME= "localhost";

    //threatre for admin
    private static String tLocation ="";

    private static String PORT = "";

//    private static final String ATWATER_SERVER_PORT = "5000";
//    private static final String VERDUN_SERVER_PORT = "5001";
//    private static final String OUTREMONT_SERVER_PORT = "5002";

//    private static UserImplementation admin;

    public static MovieTicketBooking.MovieTicketBookingInterface admin = null;

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {

        AdminClient ac = new AdminClient();

        Scanner sc = new Scanner(System.in);
        //String username, password;

        System.out.println("Welcome to Ticket booking System");
        System.out.println("Enter your username : ");
        userName = sc.nextLine();
        System.out.println("Enter password: ");
        password = sc.nextLine();

        //make call to that server
        connectToServer(args);

        //validate admin
        if (userName.substring(3,4).equals("A") && admin.validateUser(userName, password)) {
            System.out.println("Login Successfull!");
            LOGGER.info(userName + "Login Success...");

            //add sample movie slots for testing


            //display menu to admin after successful login
            boolean flag = true;
            do {

                ac.displayMainMenu();
                flag = ac.getInputFromUser(userName, Integer.parseInt(sc.nextLine()));

            }while(flag);

        } else {
            System.out.println("Invalid Credentials, please check username and password.");
            LOGGER.info(userName + "Invalid Credentials, please check username and password.");
        }
    }

    private static void connectToServer(String[] args) {
        try
        {
            tLocation = ServerEnum.getEnumNameForValue(userName.substring(0,3)).toLowerCase();
//            switch(tLocation)
//            {
//                case "atwater":
//                    PORT=ATWATER_SERVER_PORT;
//                    break;
//                case "verdun":
//                    PORT=VERDUN_SERVER_PORT;
//                    break;
//                case "outremont":
//                    PORT=OUTREMONT_SERVER_PORT;
//                    break;
//            }

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "1999");
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");

//            String registryURL = "rmi://" + HOSTNAME+ ":" + PORT + "/" + tLocation +"/admin";
//            admin = (MovieTicketBookingInterfaceRMI) Naming.lookup(registryURL);

            ORB orb = ORB.init(args, props);
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            NamingContextExt ncRef =
                    NamingContextExtHelper.narrow(objRef);
//            admin = MovieTicketBookingInterfaceHelper.narrow(ncRef.resolve_str("admin"));
            //System.out.println(ncRef.resolve_str("FrontEnd"));
            admin = MovieTicketBooking.MovieTicketBookingInterfaceHelper.narrow(ncRef.resolve_str("FrontEnd"));

            //admin.setPortAndHost(HOSTNAME,PORT);
            LOGGER.info(userName + "connection to server open...");
        }
        catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(userName + "Issue connecting with Server...");
        }
    }

    private void displayMainMenu()
    {
        System.out.println("What would you like to do today?");
        System.out.println("1: Add Movie Slots");
        System.out.println("2: Remove Movie Slots");
        System.out.println("3: List Movie Shows Availability");
        System.out.println("4: Book Movie Tickets for a customer");
        System.out.println("5: Get Booking schedule for a customer");
        System.out.println("6: Cancel movie ticket for a customer");
        System.out.println("7. Quit");
        System.out.println("Please enter your choice >");
    }

    private boolean getInputFromUser(String userName,int input) throws RemoteException, MalformedURLException, NotBoundException {

        Scanner sc = new Scanner(System.in);

        switch(input)
        {
            //add movie slots
            case 1:
            {
                System.out.println("Please enter movieName: ");
                String movieName = sc.nextLine().trim();
                System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
                String slot = sc.nextLine().toUpperCase().trim();
                System.out.println("Please enter date(DDMMYY): ");
                String date = sc.nextLine().trim();
                System.out.println("Please enter booking capacity: ");
                int bookingCapacity = Integer.parseInt(sc.nextLine().trim());
                String movieThreatre = tLocation.toUpperCase().trim();
                Movie movie = new Movie(movieName, ServerEnum.valueOf(movieThreatre).value(), SlotEnum.valueOf(slot), date);
                String response = admin.addMovieSlots(movie.getMovieId(),movieName,bookingCapacity);
                System.out.println(response);
                LOGGER.info("request to addMovieSlots: " + response);
                break;

            }

            //remove movie slots
            case 2:
            {
                System.out.println("Please enter movieName: ");
                String movieName = sc.nextLine().trim();
                System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
                String slot = sc.nextLine().toUpperCase().trim();
                System.out.println("Please enter date(DDMMYY): ");
                String date = sc.nextLine().trim();
                String movieThreatre = tLocation.toUpperCase().trim();
                Movie movie = new Movie(movieName, ServerEnum.valueOf(movieThreatre).value(), SlotEnum.valueOf(slot), date);
                String response = admin.removeMovieSlots(movie.getMovieId(),movieName);
                System.out.println(response);
                LOGGER.info("request to removeMovieSlots: " + response);
                break;
            }
            //List movie shows availability
            case 3:
            {
                System.out.println("Please enter movieName: ");
                String movieName = sc.nextLine().trim();
                String response = admin.listMovieShowsAvailability(movieName);
                if(response.equals(movieName+":"))
                {
                    response = "No data found";
                }
                System.out.println(response);
                LOGGER.info("request for listMovieShowsAvailability(): " + response);
                break;
            }
            //book movie tickets for a customer
            case 4:
            {
                System.out.println("Please enter movieName: ");
                String movieName = sc.nextLine().trim();
                System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
                String slot = sc.nextLine().toUpperCase().trim();
                System.out.println("Please enter date(DDMMYY): ");
                String date = sc.nextLine().trim();
                System.out.println("Please enter customerId: ");
                String userNameC = sc.nextLine().trim();
                System.out.println("Please enter number of tickets: ");
                int numberOfTickets = Integer.parseInt(sc.nextLine().trim());
                String movieThreatre = tLocation.toUpperCase().trim();

                Movie movie = new Movie(movieName, ServerEnum.valueOf(movieThreatre).value(), SlotEnum.valueOf(slot), date);
                String response = admin.bookMovieTickets(userNameC, movie.getMovieId(), movie.getName(), numberOfTickets);
                System.out.println(response);
                LOGGER.info("request to bookMovieTickets for customer: " + response);
                break;
            }

            //get booking schedule for customer
            case 5:
            {
                System.out.println("Please enter customerId: ");
                String userNameC = sc.nextLine().trim();
                String response = admin.getBookingSchedule(userNameC);
                if(response.equals(""))
                {
                    response = "No data found";
                }
                System.out.println(response);
                LOGGER.info("request to getBookingSchedule for customer: " + response);
                break;
            }

            //cancel movie tickets
            case 6:
            {
                System.out.println("Please enter movieName: ");
                String movieName = sc.nextLine().trim();
                System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
                String slot = sc.nextLine().toUpperCase().trim();
                System.out.println("Please enter date(DDMMYY): ");
                String date = sc.nextLine().trim();
                System.out.println("Please enter customerId: ");
                String userNameC = sc.nextLine().trim();
                System.out.println("Please enter number of tickets: ");
                int numberOfTickets = Integer.parseInt(sc.nextLine().trim());
                String movieThreatre = tLocation.toUpperCase().trim();

                Movie movie = new Movie(movieName,ServerEnum.valueOf(movieThreatre).value(), SlotEnum.valueOf(slot), date);
                String response = admin.cancelMovieTickets(userNameC, movie.getMovieId(), movie.getName(),numberOfTickets);
                System.out.println(response);
                LOGGER.info("request to cancel Movie tickets for customer: " + response);
                break;
            }

            case 7:
            {
                System.out.println("Thank you! Visit again..");
                LOGGER.info("User: " + userName +" is no longer active");
                return false;
            }

            default:
            {
                System.out.println("Please enter a valid choice as shown in menu");
            }
        }
        return true;
    }
}
