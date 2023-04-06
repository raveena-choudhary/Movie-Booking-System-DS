package Replica4.servers;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.Logger.CustomFormatter;

import static Replica4.utils.UDPSendRecieve.recieveUDPMessages;
import static Replica4.utils.UDPSendRecieve.sendUDPMessages;

public class AtwaterTheatre extends TheatreImplementation {
    private static final String LOGS_DIR = System.getProperty("user.dir") + "\\src\\main\\java\\Replica2\\servers\\Atwater";
    private static Logger logger;

    public AtwaterTheatre() throws RemoteException {
        super(logger);

    }

    public static void main(String[] args) {
        Registry registry;
        AtwaterTheatre atwaterTheatre;

        try {
            addLogger();
            // create the (local) object registry
            registry = LocateRegistry.createRegistry(6000);
            atwaterTheatre = new AtwaterTheatre();
//            atwaterTheatre.addTestData();
            // bind the object to the name "server"
            registry.rebind("atwaterTheatre", atwaterTheatre);

            for(int i=0;i<registry.list().length;i++)
            {
                System.out.println(registry.list()[i].toString());
            }

            //concurrency
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    recieveUDPMessages(atwaterTheatre, 6000, logger);
                }
            })).start();

//            DatagramSocket ds = new DatagramSocket(6001);
//            DatagramPacket DpReceive = null;
//            byte[] receive = new byte[65535];
//            DpReceive = new DatagramPacket(receive, receive.length);
//
//            ds.receive(DpReceive);
//            System.out.println("A R:-" + DpReceive.getData().toString());
////            while (true){
////                Thread.sleep(3000);
////                System.out.println("hi");
////            }
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
            CustomFormatter formatter = new CustomFormatter();

            fh.setFormatter(formatter);

            logger.addHandler(fh);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void recieveUDPMessages(AtwaterTheatre atwaterTheatre) {
//        DatagramSocket ds = null;
//        try {
//            ds = new DatagramSocket(6000);
//
//            DatagramPacket dp = null;
//            byte[] data;
//            while (true) {
//
//                data = new byte[65535];
//                dp = new DatagramPacket(data, data.length);
//                try {
//                    ds.receive(dp);
//                    String localMovieCount = atwaterTheatre.getLocalMoviesShowsAvailability(dp.getData().toString());
//
//                    ds.send(new DatagramPacket(localMovieCount.getBytes(), localMovieCount.length(), dp.getAddress(), dp.getPort()));
//                    atwaterTheatre.logger.info("");
//                } catch (IOException e) {
//                    System.out.println("IOE: " + e.getMessage());
//                }
//
//
//            }
//        } catch (SocketException e) {
//            System.out.println("SOE: " + e.getMessage());
//        } finally {
//            ds.close();
//        }
//    }

    public void addTestData() {
        addMovieSlotInHashMap("Avatar", "ATWE090223", 10);
        addMovieSlotInHashMap("Avatar", "ATWM100223", 40);
        addMovieSlotInHashMap("Avatar", "ATWM140223", 30);
        addMovieSlotInHashMap("Avatar", "ATWA150223", 60);
        addMovieSlotInHashMap("Avatar", "ATWE140223", 80);
        addMovieSlotInHashMap("Titanic", "ATWE160223", 45);
        addMovieSlotInHashMap("Titanic", "ATWE150223", 55);


    }

    @Override
    public synchronized String listMovieShowsAvailability(String movieName) throws RemoteException {
        String result = "";
        String resultFromOUT = sendUDPMessages(6002, "GET_SHOWS-" + movieName);
        String resultFromVer = sendUDPMessages(6001, "GET_SHOWS-" + movieName);
        String resultLocal = super.listMovieShowsAvailability(movieName);
        //System.out.println("atw " + movieName + ":" + super.listMovieShowsAvailability(movieName) + resultFromVer + resultFromOUT);
        String joined =
                Stream.of(resultLocal, resultFromOUT, resultFromVer)
                        .filter(s -> s != null && !s.isEmpty())
                        .collect(Collectors.joining(","));
        if (joined.isEmpty()) result = "No movie slots found";
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
                case "OUT":
                    res = sendUDPMessages(6001, propToSend);
                    break;
                case "VER":
                    res = sendUDPMessages(6002, propToSend);
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

    @Override
    public synchronized String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String res = super.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
        if (res.equals("tickets cancelled successfully")) {
            String movieInServer = utils.Utils.getMovieTheatreFromID(movieID);
            String customerInServer = utils.Utils.getMovieTheatreFromID(customerID);
            if (movieInServer.equals(customerInServer)) {
                updateMovieCount(movieName, movieID, numberOfTickets, true);
            } else if (res.equals("REQ_MOVIE_CANCEL")) {
                String propToSend = "REQ_MOVIE_CANCEL-" + movieID + "," + movieName + "," + numberOfTickets;
                switch (movieInServer) {
                    case "OUT":
                        res = sendUDPMessages(6002, propToSend);
                        break;
                    case "VER":
                        res = sendUDPMessages(6001, propToSend);
                        break;
                }


            }
        }


        return res;
    }

    @Override
    public String exchangeTickets(String customerID, String movieID, String newMovieID, String oldMovieName, String movieName, int numberOfTickets) throws RemoteException {
        String res = super.exchangeTickets(customerID, movieID, newMovieID, oldMovieName, movieName, numberOfTickets);
        if (res.equals("")) {
            String bookingFnRes = bookMovieTickets(customerID, newMovieID, movieName, numberOfTickets);
            if (bookingFnRes.equals("Booking successful")) {
                String cancelTicketsRes = cancelMovieTickets(customerID, movieID, oldMovieName, numberOfTickets);
                if (cancelTicketsRes.equals("tickets cancelled successfully")) {

                    res = "successfully exchanged tickets";
                    utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("exchangeTickets", "customerID : " + customerID + ", movieID: " + movieID + ", newMovieID: " + newMovieID + ", oldMovieName: " + oldMovieName + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Success", res);

                    logger.log(Level.INFO, msg.toString());
                } else {
                    cancelMovieTickets(customerID, newMovieID, movieName, numberOfTickets);


                    res = "Failed to exchange tickets";
                    utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("exchangeTickets", "customerID : " + customerID + ", movieID: " + movieID + ", newMovieID: " + newMovieID + ", oldMovieName: " + oldMovieName + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

                    logger.log(Level.SEVERE, msg.toString());
                }
            } else {
                res = bookingFnRes;
                utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("exchangeTickets", "customerID : " + customerID + ", movieID: " + movieID + ", newMovieID: " + newMovieID + ", oldMovieName: " + oldMovieName + ", movieName: " + movieName + ", numberOfTickets: " + numberOfTickets, "Error", res);

                logger.log(Level.SEVERE, msg.toString());
            }
        }

        return res;
    }

    //
//    public String getCustomerTicketsInOtherTheatres(String customerID) {
//        String result = "";
//        String resultFromOUT = sendUDPMessages(6002, "GET_TICKETS-" + customerID);
//        String resultFromVer = sendUDPMessages(6001, "GET_TICKETS-" + customerID);
//        String joined =
//                Stream.of(resultFromOUT, resultFromVer)
//                        .filter(s -> s != null && !s.isEmpty())
//                        .collect(Collectors.joining(","));
//        if (joined.isEmpty()) result = "";
//            //mID 10,MID 20,mID 30
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
//        if (result == "") {
//            result = "The customer has not booked any tickets yet";
//            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getBookingSchedule", "customerID : " + customerID, "Error", result);
//
//            logger.log(Level.SEVERE, msg.toString());
//        } else {
//            utils.Logger.CustomMessage msg = new utils.Logger.CustomMessage("getBookingSchedule", "customerID : " + customerID, "Operation Sucessful", result);
//
//            logger.log(Level.INFO, msg.toString());
//        }
//
//        return result;
//    }
}
