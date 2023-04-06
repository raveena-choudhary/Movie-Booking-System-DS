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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerClient {

    private static final Logger LOGGER = LogManager.getLogger("customer");
    private static String userName = null;
    private static String password = null;
    private static final String HOSTNAME = "localhost";

    //threatre for customer
    private static String tLocation = "";

    private static String PORT = "";

//    private static final String ATWATER_SERVER_PORT = "5000";
//    private static final String VERDUN_SERVER_PORT = "5001";
//    private static final String OUTREMONT_SERVER_PORT = "5002";

    public static MovieTicketBooking.MovieTicketBookingInterface customer = null;

    //method displaying main menu
    private void displayMainMenu() {
        System.out.println("What would you like to do today?");
        System.out.println("1: Book Movie Tickets");
        System.out.println("2: Get Booking schedule");
        System.out.println("3: Cancel movie Tickets");
        System.out.println("4: Exchange movie Tickets");
        System.out.println("5. Quit");
        System.out.println("Please enter your choice >");
    }

    private boolean getInputFromUser(String userName, int input){

        Scanner sc = new Scanner(System.in);

        switch (input) {
            //book movie tickets
            case 1: {
//				System.out.println("Please enter movie theatre(Atwater, Verdun, Outremont): ");
//				String movieThreatre = sc.nextLine().toUpperCase().trim();
                System.out.println("Please choose movieName: ");
                List<String> movieNames = getListFromCommaSeparatedString(customer.getAllMovieNames());
                Set<String> distinctMovieNamesSet = movieNames.stream().collect(Collectors.toSet());
                List<String> distinctMovieNamesList = new ArrayList<String>(distinctMovieNamesSet);

//				String movieName = sc.nextLine().trim();

                for (int i = 0; i < distinctMovieNamesList.size(); i++) {
                    System.out.println((i + 1) + "." + distinctMovieNamesList.get(i));
                }

                int index = Integer.parseInt(sc.nextLine());
                String movieName = distinctMovieNamesList.get(index - 1);
                System.out.println("Selected movie name: " + movieName);
                System.out.println("Please choose movie theatre: ");
                List<String> movieIDs = getListFromCommaSeparatedString(customer.getAllMovieIds(movieName));

                // print distinct values for theatre
                Set<String> distinctMovieIdsSet = movieIDs.stream().map(movieId -> ServerEnum.getEnumNameForValue(movieId.substring(0, 3))).collect(Collectors.toSet());
                List<String> distinctMovieIdsList = new ArrayList<String>(distinctMovieIdsSet);
                for (int i = 0; i < distinctMovieIdsList.size(); i++) {
                    System.out.println((i + 1) + "." + distinctMovieIdsList.get(i));
                }

                int tIndex = Integer.parseInt(sc.nextLine());
                String movieThreatre = distinctMovieIdsList.get(tIndex - 1);

                // System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
//                String slot = sc.nextLine().toUpperCase().trim();

                System.out.println("Please choose movie slot: ");
                // print distinct values for slots
                Set<String> distinctSlotSet = movieIDs.stream().map(movieId -> SlotEnum.getEnumNameForValue(movieId.substring(3, 4))).collect(Collectors.toSet());
                List<String> distinctSlotList = new ArrayList<String>(distinctSlotSet);
                for (int i = 0; i < distinctSlotList.size(); i++) {
                    System.out.println((i + 1) + "." + distinctSlotList.get(i));
                }

                int sIndex = Integer.parseInt(sc.nextLine());
                String slot = distinctSlotList.get(sIndex - 1);

                System.out.println("Please enter date(DDMMYY): ");
                String date = sc.nextLine().trim();
                System.out.println("Please enter number of tickets: ");
                int numberOfTickets = Integer.parseInt(sc.nextLine().trim());

                Movie movie = new Movie(movieName, ServerEnum.valueOf(movieThreatre).value(), SlotEnum.valueOf(slot), date);
                String response = customer.bookMovieTickets(userName, movie.getMovieId(), movie.getName(), numberOfTickets);
                System.out.println(response);
                LOGGER.info("request to bookMovieTickets: " + response);
                break;
            }

            //get booking schedule for customer
            case 2: {
                //implemented udp for this method
                String response = customer.getBookingSchedule(userName);
                if (response.equals("")) {
                    response = "No data found";
                }
                System.out.println(response);
                LOGGER.info("request to getBookingSchedule: " + response);
                break;
            }

            //cancel movie tickets
            case 3: {
//                System.out.println("Please enter movie theatre(Atwater, Verdun, Outremont): ");
//                String movieThreatre = sc.nextLine().toUpperCase().trim();
//                System.out.println("Please enter movieName: ");
//                String movieName = sc.nextLine().trim();
//                System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
//                String slot = sc.nextLine().toUpperCase().trim();
                String response = customer.getBookingSchedule(userName);
                if (response.equals("")) {
                    response = "No data found";
                } else {
                    List<String> moivesBookedByCustomer = Stream.of(response.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    System.out.println("Please choose movie:");
//				String movieName = sc.nextLine().trim();
                    for (int i = 0; i < moivesBookedByCustomer.size(); i++) {
                        System.out.println((i + 1) + "." + moivesBookedByCustomer.get(i));
                    }
                    int index = Integer.parseInt(sc.nextLine());

                    int indexOfDelimiterColon = moivesBookedByCustomer.get(index - 1).indexOf(":");
                    String[] movieDetails = moivesBookedByCustomer.get(index - 1).split(" ");
                    String movieName = movieDetails[0].substring(0,indexOfDelimiterColon);
                    System.out.println(movieName);
                    String movieThreatre = movieDetails[0].substring(indexOfDelimiterColon+1);
                    System.out.println(movieThreatre);
                    String slot = movieDetails[1];
                    System.out.println(slot);
                    String date = movieDetails[2];
                    System.out.println(date);
//                System.out.println("Please enter date(DDMMYY): ");
//                String date = sc.nextLine().trim();
                    System.out.println("Please enter number of tickets: ");
                    int numberOfTickets = Integer.parseInt(sc.nextLine().trim());

                    Movie movie = new Movie(movieName, ServerEnum.valueOf(movieThreatre).value(), SlotEnum.valueOf(slot), date);
                    response = customer.cancelMovieTickets(userName, movie.getMovieId(), movie.getName(), numberOfTickets);
//                    System.out.println(response);
//                    LOGGER.info("request to cancelMovieTickets: " + response);
                }
                System.out.println(response);
                LOGGER.info("request to cancelMovieTickets: " + response);
                break;

            }

            case 4:
            {
                String response = customer.getBookingSchedule(userName);
                if (response.equals("")) {
                    response = "No data found";
                } else {
                    //booking that exists for customer
                    List<String> moivesBookedByCustomer = Stream.of(response.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    System.out.println("Please choose movie:");
//				String movieName = sc.nextLine().trim();
                    for (int i = 0; i < moivesBookedByCustomer.size(); i++) {
                        System.out.println((i + 1) + "." + moivesBookedByCustomer.get(i));
                    }
                    int index = Integer.parseInt(sc.nextLine());

                    int indexOfDelimiterColon = moivesBookedByCustomer.get(index - 1).indexOf(":");
                    String[] movieDetails = moivesBookedByCustomer.get(index - 1).split(" ");
                    String old_movieName = movieDetails[0].substring(0,indexOfDelimiterColon);
                    System.out.println(old_movieName);

                    String old_movieThreatre = movieDetails[0].substring(indexOfDelimiterColon+1);
                    System.out.println(old_movieThreatre);
                    String old_movieSlot = movieDetails[1];
                    System.out.println(old_movieSlot);
                    String old_movieDate = movieDetails[2];
                    System.out.println(old_movieDate);

//                    System.out.println("Please enter number of tickets booked for old movie: ");
//                    int numberOfTicketsAlreadyBookedForOldMovie = Integer.parseInt(sc.nextLine().trim());

                    //todo create a single method
                    System.out.println("Please choose new movieName: ");
                    List<String> movieNames = getListFromCommaSeparatedString(customer.getAllMovieNames());
                    Set<String> distinctMovieNamesSet = movieNames.stream().collect(Collectors.toSet());
                    List<String> distinctMovieNamesList = new ArrayList<String>(distinctMovieNamesSet);

//				String movieName = sc.nextLine().trim();

                    for (int i = 0; i < distinctMovieNamesList.size(); i++) {
                        System.out.println((i + 1) + "." + distinctMovieNamesList.get(i));
                    }

                    int indexForNewMovie = Integer.parseInt(sc.nextLine());
                    String new_movieName = distinctMovieNamesList.get(indexForNewMovie - 1);

                    System.out.println("Please choose movie theatre: ");
                    List<String> movieIDs = getListFromCommaSeparatedString(customer.getAllMovieIds(new_movieName));

                    // print distinct values for theatre
                    Set<String> distinctMovieIdsSet = movieIDs.stream().map(movieId -> ServerEnum.getEnumNameForValue(movieId.substring(0, 3))).collect(Collectors.toSet());
                    List<String> distinctMovieIdsList = new ArrayList<String>(distinctMovieIdsSet);
                    for (int i = 0; i < distinctMovieIdsList.size(); i++) {
                        System.out.println((i + 1) + "." + distinctMovieIdsList.get(i));
                    }

                    int tIndex = Integer.parseInt(sc.nextLine());
                    String new_movieThreatre = distinctMovieIdsList.get(tIndex - 1);

                    // System.out.println("Please enter slot(Morning, Afternoon, Evening): ");
//                String slot = sc.nextLine().toUpperCase().trim();

                    System.out.println("Please choose movie slot: ");
                    // print distinct values for slots
                    Set<String> distinctSlotSet = movieIDs.stream().map(movieId -> SlotEnum.getEnumNameForValue(movieId.substring(3, 4))).collect(Collectors.toSet());
                    List<String> distinctSlotList = new ArrayList<String>(distinctSlotSet);
                    for (int i = 0; i < distinctSlotList.size(); i++) {
                        System.out.println((i + 1) + "." + distinctSlotList.get(i));
                    }

                    int sIndex = Integer.parseInt(sc.nextLine());
                    String new_movieSlot = distinctSlotList.get(sIndex - 1);

                    System.out.println("Please enter date(DDMMYY): ");
                    String new_movieDate = sc.nextLine().trim();

                    System.out.println("Please enter number of tickets to be exchanged for new movie: ");
                    int numberOfTickets = Integer.parseInt(sc.nextLine().trim());

                    Movie old_movie = new Movie(old_movieName, String.valueOf(ServerEnum.valueOf(old_movieThreatre)), SlotEnum.valueOf(old_movieSlot), old_movieDate);
                    Movie new_movie = new Movie(new_movieName, String.valueOf(ServerEnum.valueOf(new_movieThreatre)), SlotEnum.valueOf(new_movieSlot), new_movieDate);
                    response = customer.exchangeTickets(userName,old_movieName,old_movie.getMovieId(),new_movie.getMovieId(), new_movieName, numberOfTickets);
                }
                System.out.println(response);
                LOGGER.info("request to exchangeMovieTickets: " + response);
                break;
            }

            case 5: {
                System.out.println("Thank you! Visit again..");
                LOGGER.info("User: " + userName + " is no longer active");
                return false;
            }

            default: {
                System.out.println("Please enter a valid choice as shown in menu");
            }
        }
        return true;
    }

    private static void connectToServer(String[] args) {
        try {
            tLocation = ServerEnum.getEnumNameForValue(userName.substring(0, 3)).toLowerCase();
//            String portNum = "";
//            switch (tLocation) {
//                case "atwater":
//                    PORT = ATWATER_SERVER_PORT;
//                    break;
//                case "verdun":
//                    PORT = VERDUN_SERVER_PORT;
//                    break;
//                case "outremont":
//                    PORT = OUTREMONT_SERVER_PORT;
//                    break;
//            }
//            String registryURL = "rmi://" + HOSTNAME + ":" + PORT + "/" + tLocation + "/customer";
//            customer = (MovieTicketBookingInterface) Naming.lookup(registryURL);

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
            customer = MovieTicketBooking.MovieTicketBookingInterfaceHelper.narrow(ncRef.resolve_str("FrontEnd"));

            LOGGER.info(userName + "connection to server open...");

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(userName + "Issue connecting with Server...");
        }
    }

    public static void main(String[] args) {

        CustomerClient client = new CustomerClient();
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Ticket booking System");
        System.out.println("Enter your username : ");
        userName = sc.nextLine();
        System.out.println("Enter password: ");
        password = sc.nextLine();

        connectToServer(args);

        if (userName.substring(3,4).equals("C") && customer.validateUser(userName, password)) {
            System.out.println("Login Successfull!");
            LOGGER.info(userName + "Login Success...");
            boolean flag = true;
            do {
                client.displayMainMenu();
                flag = client.getInputFromUser(userName, Integer.parseInt(sc.nextLine()));

            } while (flag);


        } else {
            System.out.println("Invalid Credentials, please check username and password.");
            LOGGER.info(userName + "Invalid Credentials, please check username and password.");
        }

    }

    private synchronized List<String> getListFromCommaSeparatedString(String response) {
        return Stream.of(response.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

}
