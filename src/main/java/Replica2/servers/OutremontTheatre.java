package Replica5.servers;


import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Replica5.utils.UDPSendRecieve.recieveUDPMessages;
import static Replica5.utils.UDPSendRecieve.sendUDPMessages;

public class OutremontTheatre extends TheatreImplementation {
    private static final String LOGS_DIR = System.getProperty("user.dir") + "\\src\\main\\java\\Replica2\\servers\\Outremont";
    private static Logger logger;

    public OutremontTheatre() throws RemoteException {
        super(logger);

    }

    public static void main(String[] args) {

        Registry registry;
        OutremontTheatre outremontTheatre;
        try {
            addLogger();
            // create the (local) object registry
            registry = LocateRegistry.createRegistry(6002);
            outremontTheatre = new OutremontTheatre();
            outremontTheatre.addTestData();
            // bind the object to the name "server"
            registry.rebind("outremontTheatre", outremontTheatre);
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    recieveUDPMessages(outremontTheatre, 6002, logger);
                }
            })).start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void addLogger() {
        logger = Logger.getLogger(AtwaterTheatre.class.getName());
        logger.setUseParentHandlers(false);
        try {
            System.out.println(LOGS_DIR + "\\logs.txt");
            File directory = new File(LOGS_DIR);
            if (!directory.exists()) {
                directory.mkdir();
            }
            directory = new File(LOGS_DIR + "\\logs.txt");
            directory.createNewFile();

            FileHandler fh = new FileHandler(directory.getAbsolutePath());
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addTestData() {
        addMovieSlotInHashMap("Avatar", "OUTE0100223", 15);
        addMovieSlotInHashMap("Avengers", "OUTM140223", 45);
        addMovieSlotInHashMap("Avengers", "OUTM150223", 35);
        addMovieSlotInHashMap("Avatar", "OUTA150223", 65);
        addMovieSlotInHashMap("Avatar", "OUTE160223", 85);
        addMovieSlotInHashMap("Titanic", "OUTE160223", 45);
        addMovieSlotInHashMap("Titanic", "OUTE150223", 55);


    }

    @Override
    public String listMovieShowsAvailability(String movieName) throws RemoteException {
        String result = "";
        String resultFromATW = sendUDPMessages(6000, "GET_SHOWS-" + movieName);
        String resultFromVer = sendUDPMessages(6001, "GET_SHOWS-" + movieName);
        String resultLocal = super.listMovieShowsAvailability(movieName);
        String joined =
                Stream.of(resultLocal, resultFromATW, resultFromVer)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(","));
        if (joined.isEmpty()) result = "";
        else result = movieName + ":" + joined;

        utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("listMovieShowsAvailability", "movieName : " + movieName, "Operation Sucessful", result);

        logger.log(Level.INFO, msg.toString());

        return result;
    }

    @Override
    public synchronized String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String res = super.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
        String movieInServer = movieID.substring(0, 3);
        if (res.equals("SEND_TO_SERVERS")) {
            String propToSend = "BOOK_TICKETS-" + customerID + "," + movieID + "," + movieName + "," + numberOfTickets;
            switch (movieInServer) {
                case "VER":
                    res = sendUDPMessages(6001, propToSend);
                    break;
                case "ATW":
                    res = sendUDPMessages(6000, propToSend);
                    break;
            }

            if (res.equals("Booking successful")) {
//                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("bookMovieTickets", "customerID : " + customerID + ", movieID: " + movieID + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Operation Sucessful", res);
//                logger.log(Level.INFO, msg.toString());
                addCustomerMovieCount(customerID, movieName, movieID, numberOfTickets);
            }
        }
        return res;

    }

//    public String getCustomerTicketsInOtherTheatres(String customerID) {
//        String result = "";
//        String resultFromATW = sendUDPMessages(6000, "GET_TICKETS-" + customerID);
//        String resultFromVer = sendUDPMessages(6002, "GET_TICKETS-" + customerID);
//        String joined =
//                Stream.of(resultFromATW, resultFromVer)
//                        .filter(s -> s != null && !s.isEmpty())
//                        .collect(Collectors.joining(","));
//        if (joined.isEmpty()) result = "";
//        else result = joined;
//
//        utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getCustomerTicketsInOtherTheatres", "customerID : " + customerID, "Operation Sucessful", result);
//
//        logger.log(Level.INFO, msg.toString());
//
//        return result;
//    }
//
//    @Override
//    public synchronized String getBookingSchedule(String customerID) {
//        String result = Stream.of(getCustomerTicketsInCurrentTheatre(customerID), getCustomerTicketsInOtherTheatres(customerID))
//                .filter(s -> s != null && !s.isEmpty())
//                .collect(Collectors.joining(","));
//        utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getBookingSchedule", "customerID : " + customerID, "Operation Sucessful", result);
//
//        logger.log(Level.INFO, msg.toString());
//        return result;
//    }
}
