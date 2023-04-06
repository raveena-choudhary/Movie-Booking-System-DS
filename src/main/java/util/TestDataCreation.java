//package util;
//
//import Replica1.movieTicketBookingSystem.MovieTicketBookingInterface;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import util.Enums.ServerEnum;
//import util.Enums.SlotEnum;
//
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//
//public class TestDataCreation {
//
//    private static final Logger LOGGER = LogManager.getLogger("admin");
//    private static String userName = "";
//    private static String password = "";
//    private static final String HOSTNAME= "localhost";
//
//    //threatre for admin
//    private static String tLocation ="";
//
//    private static String PORT = "";
//
//    private static final String ATWATER_SERVER_PORT = "5000";
//    private static final String VERDUN_SERVER_PORT = "5001";
//    private static final String OUTREMONT_SERVER_PORT = "5002";
//
//    public static MovieTicketBookingInterface admin = null;
//
//    private static void addMovieSlots(String[] args) {
//
//        List<Movie> movies = new ArrayList<>();
//        userName = "ATWA1234";
//
//
//        connectToServer(args);
//        Movie pink = new Movie("pink", ServerEnum.valueOf("ATWATER"), SlotEnum.valueOf("MORNING"), "310323");
//        Movie abc = new Movie("abc", ServerEnum.valueOf("ATWATER"), SlotEnum.valueOf("AFTERNOON"), "310323");
//        Movie pinkE = new Movie("pink", ServerEnum.valueOf("ATWATER"), SlotEnum.valueOf("EVENING"), "310323");
//        movies.add(pink);
//        movies.add(abc);
//        movies.add(pinkE);
//        addMovies(movies);
//        movies.clear();
//        userName = "VERA1234";
//        connectToServer(args);
//        Movie avengers = new Movie("avengers", ServerEnum.valueOf("VERDUN"), SlotEnum.valueOf("MORNING"), "310323");
//        Movie avengersA = new Movie("avengers", ServerEnum.valueOf("VERDUN"), SlotEnum.valueOf("AFTERNOON"), "310323");
//        Movie avengersE = new Movie("avengers", ServerEnum.valueOf("VERDUN"), SlotEnum.valueOf("EVENING"), "310323");
//        movies.add(avengers);
//        movies.add(avengersA);
//        movies.add(avengersE);
//        addMovies(movies);
//        movies.clear();
//        userName = "OUTA1234";
//        connectToServer(args);
//        Movie avatar = new Movie("avatar", ServerEnum.valueOf("OUTREMONT"), SlotEnum.valueOf("MORNING"), "310323");
//
//        Movie avatarA = new Movie("avatar", ServerEnum.valueOf("OUTREMONT"), SlotEnum.valueOf("AFTERNOON"), "310323");
//
//        Movie avatarE = new Movie("avatar", ServerEnum.valueOf("OUTREMONT"), SlotEnum.valueOf("EVENING"), "310323");
//        movies.add(avatar);
//        movies.add(avatarA);
//        movies.add(avatarE);
//        addMovies(movies);
//        movies.clear();
//
//    }
//
//
//    private static void addMovies(List<Movie> movies){
//        for (Movie movie : movies) {
//            String response = null;
//            try {
//                //response = AdminClient.admin.bookMovieTickets(admin, movie.getMovieId(), movie.getName(), 10);
//                response = TestDataCreation.admin.addMovieSlots(movie.getMovieId(), movie.getName(), 10);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println(response);
//        }
//    }
//
//    private static void connectToServer(String[] args) {
//        try
//        {
//            tLocation = ServerEnum.getEnumNameForValue(userName.substring(0,3)).toLowerCase();
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
//            String registryURL = "rmi://" + HOSTNAME+ ":" + PORT + "/" + tLocation +"/admin";
//            admin = (MovieTicketBookingInterface) Naming.lookup(registryURL);
//            admin.setPortAndHost(HOSTNAME,PORT);
//            LOGGER.info(userName + "connection to server open...");
//        }
//        catch (RemoteException | MalformedURLException | NotBoundException e) {
//            e.printStackTrace();
//            LOGGER.info(userName + "Issue connecting with Server...");
//        }
//    }
//
//
//    public static void main(String args[])
//    {
//        addMovieSlots(args);
//    }
//}
