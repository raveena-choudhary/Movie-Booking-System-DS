package Replica1;

import Replica1.movieTicketBookingSystem.MovieTicketBookingInterface;
import Replica1.servers.AtwaterServer;
import Replica1.servers.OutremontServer;
import Replica1.servers.VerdunServer;
import util.Enums.ServerEnum;
import util.RMServersDownException;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RM1 {

    protected static final int ATWATER_SERVER_PORT = 5000;
    protected static final int VERDUN_SERVER_PORT = 5001;
    protected static final int OUTREMONT_SERVER_PORT = 5002;

    private static final String HOSTNAME = "localhost";
    private static int PORT = 0;

    public static MovieTicketBookingInterface admin = null;
    public static MovieTicketBookingInterface customer = null;

    private static final int multicast_socket_port = 1234;
    private static final String multicast_Addr = "230.1.1.10";

    private static final int FE_port = 1999;
    private static final int port_to_reply_Fe = 2021;
    public static int lastSequenceID = 1;

    //Map to store sequence id and message to respond front end with sequence id, response--> Format<Sequence Id, Request>
    public static ConcurrentHashMap<Integer, Message> message_with_sequenceId_map = new ConcurrentHashMap<>();

    //Message queue to get the messages from Sequencer, stores
    public static Queue<Message> message_queue = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;

    public static void main(String[] args) throws Exception {
        Runnable task = () -> {
            try {
                System.out.println("Receive Method Called.");
                System.out.println("Thread name: " + Thread.currentThread().getName());
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private static void receive() throws Exception {
        MulticastSocket socket = null;
        try {

            socket = new MulticastSocket(multicast_socket_port);
            socket.joinGroup(InetAddress.getByName(multicast_Addr));
            System.out.println("RM1 Started.");

            //To execute all messages in queue
            Runnable task = () -> {
                try {
                    System.out.println("ExecuteAll Message Called.");
                    System.out.println("Thread name: " + Thread.currentThread().getName());
                    executeAllMessagesInQueue();
                } catch (RMServersDownException e) {
                    System.out.println("removing key: " + e.getSequenceId());
                    message_with_sequenceId_map.remove(e.getSequenceId());
                    for (Map.Entry<Integer, Message> m : message_with_sequenceId_map.entrySet()) {
                        System.out.println("Get values:" + m.getKey());
                    }
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(task);
            thread.start();

            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String msgData = new String(request.getData(), 0, request.getLength());
                String[] parts = msgData.split(";");
//                Message messageFromRequest = null;
//                try {
//                    messageFromRequest = new ObjectMapper().readValue(msgData, Message.class);
//                    parts = messageFromRequest.toString().split(";");
//                    System.out.println("not in exception " + parts);
//                }catch (Exception e){
//                    System.out.println(e.getMessage());
//                    System.out.println(Arrays.toString(e.getStackTrace()));
//                    parts = msgData.split(";");
//                    System.out.println("in exception " + parts);
//                }

                /*
                Message Types:
                    00- Simple message
                    01- Sync request between the RMs
                    03 - send message to RM1 on request
                    21-Rm1 is down, crashed
                */
                System.out.println("RM1 recieved message with msgData: " + msgData);
                String messageTypeFromRequest = parts[2];
                if (msgData.contains("{")) {
                    messageTypeFromRequest = parts[parts.length - 9];
                }
                System.out.println("Message type from Request received:" + messageTypeFromRequest);
                switch (messageTypeFromRequest) {
                    case "00": {
                        System.out.println("Create Mesaage Called.");
                        System.out.println("Thread name: " + Thread.currentThread().getName());
                        Message message = createMessage(msgData); //create message object

                        //check when sequence id is not matching with seq id expected by RM
                        //requesting all RMs to send list of messages
                        System.out.println("Difference:" + (message.sequenceId - lastSequenceID));
                        if (message.sequenceId - lastSequenceID > 1) {
                            Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "Null", "Null", "Null", "Null", 0, 0);
                            System.out.println("Sending request to all RMs for message list..");
                            sendMulticastToRM(initial_message);
                        }
                        System.out.println("message to be added in queue:" + message);
                        System.out.println("message to be added in map: " + message + "with sequence id:" + message.sequenceId);
                        message_queue.add(message);
                        message_with_sequenceId_map.put(message.sequenceId, message);
                        break;
                    }
                    case "01":
                    {
                        Message message = createMessage(msgData);
                        if (!message_with_sequenceId_map.contains(message.sequenceId))
                            message_with_sequenceId_map.put(message.sequenceId, message);
                        break;
                    }
                    case "03": {
                        System.out.println("Received message with 03 request");
//                        if (messageFromRequest == null) break;
//
//                        ConcurrentHashMap<Integer, Message> concurrentHashMap = new ObjectMapper().readValue(messageFromRequest.FrontEndIpAddress, new TypeReference<ConcurrentHashMap<Integer, Message>>() {
//                        });
//                        message_with_sequenceId_map.putAll(concurrentHashMap);
//                        message_with_sequenceId_map.forEach((integer, message) -> System.err.println("RM1 Merged map to " + integer + " : " + message.toString()));

                        int indexof_start = msgData.indexOf("{");
                        int indexof_end = msgData.indexOf("}");

                        if(updateMessageList(msgData.substring(indexof_start+1,indexof_end)))
                        {
                            System.out.println("Reload Servers Called.");
                            System.out.println("Thread name: " + Thread.currentThread().getName());
                            reloadServers();
                            Runnable executeTask = () -> {
                                try {
                                    System.out.println("ExecuteAll Message Called.");
                                    System.out.println("Thread name: " + Thread.currentThread().getName());
                                    executeAllMessagesInQueue();
                                } catch (RMServersDownException e) {
                                    System.out.println("removing key: " + e.getSequenceId());
                                    message_with_sequenceId_map.remove(e.getSequenceId());
                                    for (Map.Entry<Integer, Message> m : message_with_sequenceId_map.entrySet()) {
                                        System.out.println("Get values:" + m.getKey());
                                    }
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            };
                            Thread thread2 = new Thread(executeTask);
                            thread2.start();
                        }

                        break;
                    }
                    case "21": {
                        Runnable crash_task = () -> {
                            try {
                                serversFlag = false;
                                final String[] registryURL = {""};
                                //suspend the execution of messages untill all servers are up. (serversFlag=false)
                                Runnable atw = () -> {
                                    try {
                                        AtwaterServer.main(new String[0]);
                                        //reboot Atwater Server
                                        Registry atwater_registry = LocateRegistry.getRegistry(ATWATER_SERVER_PORT);
                                        registryURL[0] = "rmi://" + HOSTNAME + ":" + ATWATER_SERVER_PORT + "/atwater";
                                        admin = (MovieTicketBookingInterface) atwater_registry.lookup(registryURL[0] + "/admin");
                                        customer = (MovieTicketBookingInterface) atwater_registry.lookup(registryURL[0] + "/customer");
                                        System.out.println("Atwater Server running");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                };
                                Thread thread1 = new Thread(atw);
                                thread1.start();

                                Runnable ver = () -> {
                                    try {
                                        VerdunServer.main(new String[0]);
                                        //reboot Verdun Server
                                        Registry verdun_registry = LocateRegistry.getRegistry(VERDUN_SERVER_PORT);
                                        registryURL[0] = "rmi://" + HOSTNAME + ":" + VERDUN_SERVER_PORT + "/verdun";
                                        admin = (MovieTicketBookingInterface) verdun_registry.lookup(registryURL[0] + "/admin");
                                        customer = (MovieTicketBookingInterface) verdun_registry.lookup(registryURL[0] + "/customer");
                                        System.out.println("Verdun Server running");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                };
                                Thread thread2 = new Thread(ver);
                                thread2.start();

                                Runnable out = () -> {
                                    try {
                                        OutremontServer.main(new String[0]);
                                        //reboot Outremont Server
                                        Registry outremont_registry = LocateRegistry.getRegistry(OUTREMONT_SERVER_PORT);
                                        registryURL[0] = "rmi://" + HOSTNAME + ":" + OUTREMONT_SERVER_PORT + "/outremont";
                                        admin = (MovieTicketBookingInterface) outremont_registry.lookup(registryURL[0] + "/admin");
                                        customer = (MovieTicketBookingInterface) outremont_registry.lookup(registryURL[0] + "/customer");
                                        System.out.println("Outremont Server running");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                };
                                Thread thread3 = new Thread(out);
                                thread3.start();
                                //wait untill are servers are up
                                Thread.sleep(5000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                        Thread handleThread = new Thread(crash_task);
                        handleThread.start();
                        handleThread.join();
                        System.out.println("RM1 handled the crash!");
                        serversFlag = true;
                        break;
                    }

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

    private static Message createMessage(String data) {
        String[] parts = data.split(";");
        int sequenceId = Integer.parseInt(parts[0]);
        String FrontEndIpAddress = parts[1];
        String MessageType = parts[2];
        String MethodCalled = parts[3];
        String userID = parts[4];
        String newMovieId = parts[5];
        String newMovieName = parts[6];
        String oldMovieId = parts[7];
        String oldMovieName = parts[8];
        int bookingCapacity = Integer.parseInt(parts[9]);
        int numberOfTickets = Integer.parseInt(parts[10]);
        Message message = new Message(sequenceId, FrontEndIpAddress, MessageType, MethodCalled, userID, newMovieId, newMovieName, oldMovieId, oldMovieName, bookingCapacity, numberOfTickets);
        return message;
    }

    //updating the map and adding new data to queue to be executed for recovery
    private static boolean updateMessageList(String data) {
        String[] parts = data.split("@");
        for (int i = 0; i < parts.length; ++i) {
            System.out.println("Message " + i + ":" + parts[i]);
            Message message = createMessage(parts[i]);
            if (!message_with_sequenceId_map.containsKey(message.sequenceId)) {
                System.out.println("RM1 update its message list" + message);
                message_queue.add(message);
                message_with_sequenceId_map.put(message.sequenceId, message);
                return true;
            }
        }
        return false;
    }

    private static void sendMulticastToRM(Message message) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] data = message.toString().getBytes();
            InetAddress aHost = InetAddress.getByName(multicast_Addr);
            DatagramPacket request = new DatagramPacket(data, data.length, aHost, multicast_socket_port);
            socket.send(request);
            System.out.println("Message sent to other RMs from RM1: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Execute all request from the lastSequenceID, send the response back to Front and update the counter(lastSequenceID)
    private static void executeAllMessagesInQueue() throws Exception, RMServersDownException {
        while (true) {
            synchronized (RM1.class) {
                for (Message data : message_queue) {
                    System.out.println("executing requests in queue --->" + data);
                    if (data.sequenceId == lastSequenceID && serversFlag) {
                        String response;
                        try {
                            System.out.println("RequestToServers Method Called.");
                            System.out.println("Thread name: " + Thread.currentThread().getName());
                            response = requestToServers(data);
                        } catch (RemoteException re) {
                            throw new RMServersDownException(re, data.sequenceId);
                        }
                        Message message = new Message(data.sequenceId, response, "RM1",
                                data.MethodCalled, data.userID, data.newMovieId,
                                data.newMovieName, data.oldMovieId,
                                data.oldMovieName, data.bookingCapacity, data.numberOfTickets);
                        lastSequenceID += 1;
                        System.out.println("Message to front Called.");
                        System.out.println("Thread name: " + Thread.currentThread().getName());
                        messsageToFront(message.toString(), data.FrontEndIpAddress);
                        message_queue.poll();
                    }
                }
            }
        }
    }

    //Send RMI request to server
    private static String requestToServers(Message input) throws Exception {
        serverPort(input.userID.substring(0, 3));
        Registry registry = null;
        String registryURL = "";
        switch (PORT) {
            case ATWATER_SERVER_PORT: {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                registryURL = "rmi://" + HOSTNAME + ":" + PORT + "/atwater";
                break;
            }
            case VERDUN_SERVER_PORT: {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                registryURL = "rmi://" + HOSTNAME + ":" + PORT + "/verdun";
                break;
            }
            case OUTREMONT_SERVER_PORT: {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                registryURL = "rmi://" + HOSTNAME + ":" + PORT + "/outremont";
                break;
            }

        }

        admin = (MovieTicketBookingInterface) registry.lookup(registryURL + "/admin");
        customer = (MovieTicketBookingInterface) registry.lookup(registryURL + "/customer");

        if (input.userID.substring(3, 4).equalsIgnoreCase("A")) {
            if (input.MethodCalled.equalsIgnoreCase("addMovieSlots")) {
                System.out.println("Movie name received by RM1:" + input.newMovieName);
                String response = admin.addMovieSlots(input.newMovieId, input.newMovieName, input.bookingCapacity);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("removeMovieSlots")) {
                String response = admin.removeMovieSlots(input.newMovieId, input.newMovieName);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("listMovieShowsAvailability")) {
                String response = admin.listMovieShowsAvailability(input.newMovieName);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("bookMovieTickets")) {
                String response = admin.bookMovieTickets(input.userID, input.newMovieId, input.newMovieName, input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getBookingSchedule")) {
                String response = admin.getBookingSchedule(input.userID);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("cancelMovieTickets")) {
                String response = admin.cancelMovieTickets(input.userID, input.newMovieId, input.newMovieName, input.numberOfTickets);
                System.out.println(response);
                return response;
            }
        } else if (input.userID.substring(3, 4).equalsIgnoreCase("C")) {
            if (input.MethodCalled.equalsIgnoreCase("bookMovieTickets")) {
                String response = customer.bookMovieTickets(input.userID, input.newMovieId, input.newMovieName, input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getBookingSchedule")) {
                String response = customer.getBookingSchedule(input.userID);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("cancelMovieTickets")) {
                String response = customer.cancelMovieTickets(input.userID, input.newMovieId, input.newMovieName, input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("exchangeTickets")) {
                String response = customer.exchangeTickets(input.userID, input.newMovieId, input.newMovieName, input.oldMovieId, input.oldMovieName, input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getAllMovieNames")) {
                String response = customer.getAllMovieNames();
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getAllMovieIds")) {
                String response = customer.getAllMovieIds(input.newMovieName);
                System.out.println(response);
                return response;
            }
        }
        return "Null response from server" + input.userID.substring(0, 3);
    }

    private static void serverPort(String userName) {
        String tLocation = ServerEnum.getEnumNameForValue(userName.substring(0, 3)).toLowerCase();
        switch (tLocation) {
            case "atwater":
                PORT = ATWATER_SERVER_PORT;
                break;
            case "verdun":
                PORT = VERDUN_SERVER_PORT;
                break;
            case "outremont":
                PORT = OUTREMONT_SERVER_PORT;
                break;
        }
    }

    public static void messsageToFront(String message, String FrontIpAddress) {
        System.out.println("Message to front:" + message);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port_to_reply_Fe);
            byte[] bytes = message.getBytes();
            InetAddress aHost = InetAddress.getByName(FrontIpAddress);

            System.out.println(aHost);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, aHost, FE_port);
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
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_with_sequenceId_map.entrySet()) {
            System.out.println("Recovery Mode-RM1 is executing message request:" + entry.getValue().toString());
            System.out.println("Request to servers Called.");
            System.out.println("Thread name: " + Thread.currentThread().getName());
            requestToServers(entry.getValue());
            if (entry.getValue().sequenceId >= lastSequenceID)
                lastSequenceID = entry.getValue().sequenceId + 1;
        }
        message_queue.clear();
        //enable server flag
        serversFlag = true;
    }

}
