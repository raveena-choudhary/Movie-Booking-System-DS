package Replica4;

import Replica4.interfaces.BookingSystemInterface;
import Replica4.servers.AtwaterTheatre;
import Replica4.servers.OutremontTheatre;
import Replica4.servers.VerdunTheatre;
import util.Enums.ServerEnum;

import java.io.IOException;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RM4 {

    private static final String ATWATER_SERVER_PORT = "6000";
    private static final String VERDUN_SERVER_PORT = "6001";
    private static final String OUTREMONT_SERVER_PORT = "6002";

    private static final String HOSTNAME= "localhost";
    private static int PORT=0;

    public static BookingSystemInterface theatre = null;

    public static int lastSequenceID = 1;
    public static ConcurrentHashMap<Integer, Message> message_list = new ConcurrentHashMap<>();
    public static Queue<Message> message_q = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;

    public static void main(String[] args) throws Exception {
        Run();
    }

    private static void Run() throws Exception {
        Runnable task = () -> {
            try {
                receive();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private static void receive() throws Exception {
        MulticastSocket socket = null;
        try {

            socket = new MulticastSocket(1234);

            socket.joinGroup(InetAddress.getByName("230.1.1.10"));

            byte[] buffer = new byte[1000];
            System.out.println("RM2 UDP Server Started(port=1234)............");

            //Run thread for executing all messages in queue
            Runnable task = () -> {
                try {
                    executeAllRequests();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(task);
            thread.start();

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String data = new String(request.getData(), 0, request.getLength());
                String[] parts = data.split(";");

                /*
                Message Types:
                    00- Simple message
                    01- Sync request between the RMs
                    02- Initialing RM
                    11-Rm1 has bug
                    12-RM2 has bug
                    13-Rm3 has bug
                    21-Rm1 is down
                    22-RM2 is down
                    23-Rm3 is down
                */
                System.out.println("RM2 recieved message. Detail:" + data);
                if (parts[2].equalsIgnoreCase("00")) {
                    Message message = message_obj_create(data);
                    Message message_To_RMs = message_obj_create(data);
                    message_To_RMs.MessageType = "01";
                    send_multicast_toRM(message_To_RMs);
                    if (message.sequenceId - lastSequenceID > 1) {
                        Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "RM2", "Null", "Null", "Null", 0,0);
                        System.out.println("RM2 send request to update its message list. from:" + lastSequenceID + "To:" + message.sequenceId);
                        // Request all RMs to send back list of messages
                        send_multicast_toRM(initial_message);
                    }
                    System.out.println("is adding queue:" + message);
                    message_q.add(message);
                    message_list.put(message.sequenceId, message);
                } else if (parts[2].equalsIgnoreCase("01")) {
                    Message message = message_obj_create(data);
                    if (!message_list.contains(message.sequenceId))
                        message_list.put(message.sequenceId, message);
                } else if (parts[2].equalsIgnoreCase("02")) {
                    initial_send_list(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5]);
                } else if (parts[2].equalsIgnoreCase("03") && parts[5].equalsIgnoreCase("RM2")) {
                    update_message_list(parts[1]);
                } else if (parts[2].equalsIgnoreCase("11")) {
                    Message message = message_obj_create(data);
                    System.out.println("RM1 has bug:" + message.toString());
                } else if (parts[2].equalsIgnoreCase("12")) {
                    Message message = message_obj_create(data);
                    System.out.println("RM2 has bug:" + message.toString());
                } else if (parts[2].equalsIgnoreCase("13")) {
                    Message message = message_obj_create(data);
                    System.out.println("RM3 has bug:" + message.toString());
                } else if (parts[2].equalsIgnoreCase("22")) {
                    Runnable crash_task = () -> {
                        try {
                            //suspend the execution of messages untill all servers are up. (serversFlag=false)
                            serversFlag = false;
                            String registryURL="";
                            //reboot Atwater Server
                            Registry atwater_registry = LocateRegistry.getRegistry(ATWATER_SERVER_PORT);
                            theatre = (BookingSystemInterface) atwater_registry.lookup("atwaterTheatre");
                            //registryURL = "rmi://" + HOSTNAME+ ":" + ATWATER_SERVER_PORT + "/atwater";
//                            admin = (MovieTicketBookingInterface) atwater_registry.lookup(registryURL+"/admin");
//                            customer = (MovieTicketBookingInterface) atwater_registry.lookup(registryURL+"/customer");
//                            admin.shutDown();
                            theatre.shutDown();
                            System.out.println("RM2 shutdown Atwater Server");

                            //reboot Verdun Server
                            Registry verdun_registry = LocateRegistry.getRegistry(VERDUN_SERVER_PORT);
                            theatre = (BookingSystemInterface) atwater_registry.lookup("verdunTheatre");
//                            registryURL = "rmi://" + HOSTNAME+ ":" + ATWATER_SERVER_PORT + "/verdun";
//                            admin = (MovieTicketBookingInterface) verdun_registry.lookup(registryURL+"/admin");
//                            customer = (MovieTicketBookingInterface) verdun_registry.lookup(registryURL+"/customer");
//                            admin.shutDown();
                            theatre.shutDown();
                            System.out.println("RM2 shutdown Verdun Server");

                            //reboot Outremont Server
                            Registry outremont_registry = LocateRegistry.getRegistry(OUTREMONT_SERVER_PORT);
                            theatre = (BookingSystemInterface) atwater_registry.lookup("outremontTheatre");
//                            registryURL = "rmi://" + HOSTNAME+ ":" + ATWATER_SERVER_PORT + "/outremont";
//                            admin = (MovieTicketBookingInterface) outremont_registry.lookup(registryURL+"/admin");
//                            customer = (MovieTicketBookingInterface) outremont_registry.lookup(registryURL+"/customer");
//                            admin.shutDown();
                            theatre.shutDown();
                            System.out.println("RM2 shutdown Outremont Server");

                            //This is going to start all the servers for this implementation
                            AtwaterTheatre.main(new String[0]);
                            Thread.sleep(500);
                            VerdunTheatre.main(new String[0]);
                            Thread.sleep(500);
                            OutremontTheatre.main(new String[0]);

                            //wait untill are servers are up
                            Thread.sleep(5000);

                            System.out.println("RM2 is reloading servers hashmap");
                            reloadServers();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    Thread handleThread = new Thread(crash_task);
                    handleThread.start();
                    System.out.println("RM2 handled the crash!");
                    serversFlag = true;
                }
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private static Message message_obj_create(String data) {
        String[] parts = data.split(";");
        int sequenceId = Integer.parseInt(parts[0]);
        String FrontIpAddress = parts[1];
        String MessageType = parts[2];
        String Function = parts[3];
        String userID = parts[4];
        String newEventID = parts[5];
        String newEventType = parts[6];
        String oldEventID = parts[7];
        String oldEventType = parts[8];
        int bookingCapacity = Integer.parseInt(parts[9]);
        int numberOfTickets = Integer.parseInt(parts[10]);
        Message message = new Message(sequenceId, FrontIpAddress, MessageType, Function, userID, newEventID, newEventType, oldEventID, oldEventType, bookingCapacity,numberOfTickets);
        return message;
    }

    // Create a list of messsages, seperating them with @ and send it back to RM
    private static void initial_send_list(Integer begin, Integer end, String RmNumber) {
        String list = "";
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_list.entrySet()) {
            if (entry.getValue().sequenceId > begin && entry.getValue().sequenceId < end) {
                list += entry.getValue().toString() + "@";
            }
        }
        // Remove the last @ character
        if (list.length() > 2)
            list.substring(list.length() - 1);
        Message message = new Message(0, list, "03", begin.toString(), end.toString(), RmNumber, "Null", "Null", "Null", 0,0);
        System.out.println("RM2 sending its list of messages for initialization. list of messages:" + list);
        send_multicast_toRM(message);
    }

    //update the hasmap and and new data to queue to be execited
    private static void update_message_list(String data) {
        String[] parts = data.split("@");
        for (int i = 0; i < parts.length; ++i) {
            Message message = message_obj_create(parts[i]);
            //we get the list from 2 other RMs and will ensure that there will be no duplication
            if (!message_list.containsKey(message.sequenceId)) {
                System.out.println("RM2 update its message list" + message);
                message_q.add(message);
                message_list.put(message.sequenceId, message);
            }
        }
    }

    private static void send_multicast_toRM(Message message) {
        int port = 1234;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] data = message.toString().getBytes();
            InetAddress aHost = InetAddress.getByName("230.1.1.10");
//            InetAddress aHost = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(data, data.length, aHost, port);
            socket.send(request);
            System.out.println("Message multicasted from RM2 to other RMs. Detail:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Execute all request from the lastSequenceID, send the response back to Front and update the counter(lastSequenceID)
    private static void executeAllRequests() throws Exception {
        System.out.println("before while true");
        while (true) {
            synchronized (Replica4.RM4.class) {
                Iterator<Message> itr = message_q.iterator();
                while (itr.hasNext()) {
                    Message data = itr.next();
                    System.out.println("RM2 is executing message request. Detail:" + data);
                    //when the servers are down serversFlag is False therefore, no execution untill all servers are up.
                    if (data.sequenceId == lastSequenceID && serversFlag) {
                        System.out.println("RM2 is executing message request. Detail:" + data);
                        String response = requestToServers(data);
                        System.out.println("Data from RM2 for movieName: " +  data.newMovieName);
                        Message message = new Message(data.sequenceId, response, "RM2",
                                data.MethodCalled, data.userID, data.newMovieId,
                                data.newMovieName, data.oldMovieId,
                                data.oldMovieName, data.bookingCapacity,data.numberOfTickets);
                        lastSequenceID += 1;
                        messsageToFront(message.toString(), data.FrontEndIpAddress);
                        message_q.poll();
                        //break;
                    }
                }
//                message_q.clear();
            }
        }
    }

    //Send RMI request to server
    private static String requestToServers(Message input) throws Exception {
        serverPort(input.userID.substring(0, 3));
        Registry registry = null;
        String registryURL = "";
        switch(PORT)
        {
            case 6000:
            {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                theatre = (BookingSystemInterface) registry.lookup("atwaterTheatre");
                break;
            }
            case 6001:
            {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                theatre = (BookingSystemInterface) registry.lookup("verdunTheatre");
                break;
            }
            case 6002:
            {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                theatre = (BookingSystemInterface) registry.lookup("outremontTheatre");
                break;
            }

        }

//        admin = (MovieTicketBookingInterface) registry.lookup(registryURL+"/admin");
//        customer = (MovieTicketBookingInterface) registry.lookup(registryURL+"/customer");

        if (input.userID.substring(3, 4).equalsIgnoreCase("A")) {
            if (input.MethodCalled.equalsIgnoreCase("addMovieSlots")) {
                System.out.println("Movie name received by RM2:" + input.newMovieName);
                String response = theatre.addMovieSlots(input.newMovieId, input.newMovieName, input.bookingCapacity);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("removeMovieSlots")) {
                String response = theatre.removeMovieSlots(input.newMovieId, input.newMovieName);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("listMovieShowsAvailability")) {
                String response = theatre.listMovieShowsAvailability(input.newMovieName);
                System.out.println(response);
                return response;
            }else if (input.MethodCalled.equalsIgnoreCase("bookMovieTickets")) {
                    String response = theatre.bookMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                    System.out.println(response);
                    return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getBookingSchedule")) {
                    String response = theatre.getBookingSchedule(input.userID);
                    System.out.println(response);
                    return response;
            } else if (input.MethodCalled.equalsIgnoreCase("cancelMovieTickets")) {
                    String response = theatre.cancelMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                    System.out.println(response);
                    return response;
            }
        } else if (input.userID.substring(3, 4).equalsIgnoreCase("C")) {
            if (input.MethodCalled.equalsIgnoreCase("bookMovieTickets")) {
                String response = theatre.bookMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getBookingSchedule")) {
                String response = theatre.getBookingSchedule(input.userID);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("cancelMovieTickets")) {
                String response = theatre.cancelMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("exchangeTickets")) {
                String response = theatre.exchangeTickets(input.userID, input.newMovieId, input.newMovieName, input.oldMovieId, input.oldMovieName,input.numberOfTickets);
                System.out.println(response);
                return response;
            }
            else if (input.MethodCalled.equalsIgnoreCase("getAllMovieNames")) {
                String response = theatre.getAllMovieNames();
                System.out.println(response);
                return response;
            }
            else if (input.MethodCalled.equalsIgnoreCase("getAllMovieIds")) {
                String response = theatre.getAllMovieIds(input.newMovieName);
                System.out.println(response);
                return response;
            }
        }
        return "Null response from server" + input.userID.substring(0, 3);
    }

    private static void serverPort(String userName) {
        String tLocation = ServerEnum.getEnumNameForValue(userName.substring(0,3)).toLowerCase();
        switch(tLocation) {
            case "atwater":
                PORT = Integer.parseInt(ATWATER_SERVER_PORT);
                break;
            case "verdun":
                PORT = Integer.parseInt(VERDUN_SERVER_PORT);
                break;
            case "outremont":
                PORT = Integer.parseInt(OUTREMONT_SERVER_PORT);
                break;
        }
    }

    public static void messsageToFront(String message, String FrontIpAddress) {
        System.out.println("Message to front:" + message);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(4322);
            byte[] bytes = message.getBytes();
            InetAddress aHost = InetAddress.getByName(FrontIpAddress);

            System.out.println(aHost);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, aHost, 1999);
            socket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    public static void reloadServers() throws Exception {
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_list.entrySet()) {
            if (entry.getValue().sequenceId < lastSequenceID)
                requestToServers(entry.getValue());
        }
    }
}
