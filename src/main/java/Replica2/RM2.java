package Replica2;

import Replica2.movieTicketBookingSystem.MovieTicketBookingInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Enums.ServerEnum;
import util.RMServersDownException;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RM2 {

    protected static final int ATWATER_SERVER_PORT = 6000;
    protected static final int VERDUN_SERVER_PORT = 6001;
    protected static final int OUTREMONT_SERVER_PORT = 6002;

    private static final String HOSTNAME= "localhost";
    private static int PORT=0;

    public static MovieTicketBookingInterface admin = null;
    public static MovieTicketBookingInterface customer = null;

    private static final String multicast_Addr = "230.1.1.10";
    private static final int multicast_socket_port = 1234;

    private static final int FE_port = 1999;
    private static final int port_to_reply_Fe = 2022;

    public static int lastSequenceID = 1;

    //Map to store sequence id and message to respond front end with sequence id, response--> Format<Sequence Id, Request>
    public static ConcurrentHashMap<Integer, Message> message_with_sequenceId_map = new ConcurrentHashMap<>();

    //Message queue to get the messages from Sequencer, stores
    public static Queue<Message> message_queue = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;

    public static void main(String[] args) throws Exception {
        Runnable task = () -> {
            try {
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

            byte[] buffer = new byte[1000];
            System.out.println("RM2 Server Started.");

            //Run thread for executing all messages in queue
            //To execute all messages in queue
            Runnable task = () -> {
                try {
                    executeAllMessagesInQueue();
                } catch (RMServersDownException e) {
                    message_with_sequenceId_map.remove(e.getSequenceId());
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(task);
            thread.start();

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String msgData = new String(request.getData(), 0, request.getLength());
                System.out.println("total length received " + request.getLength());
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
                System.out.println("RM2 recieved message with msgData: " + msgData);
                String messageTypeFromRequest = parts[2];
                switch(messageTypeFromRequest)
                {
                    case "00": {
                        Message message = createMessage(msgData); //create message object

                        //check when sequence id is not matching with seq id expected by RM
                        //requesting all RMs to send list of messages
                        if (message.sequenceId - lastSequenceID > 1) {
                            Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "Null", "Null", "Null", "Null", 0,0);
                            System.out.println("Sending request to all RMs for message list..");
                            sendMulticastToRM(initial_message);
                        }
                        System.out.println("message to be added in queue:" + message);
                        System.out.println("message to be added in map: " + message+ "with sequence id:" + message.sequenceId);
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
                    case "02": {
                        initial_send_list(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5]);
                        break;
                    }
                    case "03":
                         {
                            updateMessageList(parts[1]);
                            break;
                       }
                    case "21": {
                        System.out.println("RM1 is down.... send list of messages");
                        initial_send_list(1,lastSequenceID,"RM1");
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

//    private static void initial_send_list_all(int i, int lastSequenceID, String rm1) throws JsonProcessingException {
//        String asString = new ObjectMapper().writeValueAsString(message_with_sequenceId_map);
//        Message message = new Message(0, asString, "03", null, null, "Null", "Null", "Null", "Null", 0,0);
//        System.out.println("RM2 sending its list of messages for initialization. list of messages:" + asString);
//        send_multicast_toRM_updated(message);
//    }

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

    // Create a list of messsages, seperating them with @ and send it back to RM
    private static void initial_send_list(Integer begin, Integer end, String RmNumber) {
        String list = "";
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_with_sequenceId_map.entrySet()) {
            System.out.println(entry.getKey());
            if (entry.getValue().sequenceId >= begin && entry.getValue().sequenceId <= end) {
                list += entry.getValue().toString() + "@";
            }
        }
        if(list.contains("@"))
        {
            list=list.substring(0,list.length() - 1);
        }

        Message message = new Message(0, "{"+list+"}", "03", begin.toString(), end.toString(), "Null", "Null", "Null", "Null", 0,0);
        System.out.println("RM2 sending its list of messages for initialization. list of messages:" + list);
        sendMulticastToRM(message);
    }

    //update the hashmap and and new msgData to queue to be executed
    private static void updateMessageList(String msgData) {
        String[] parts = msgData.split("@");
        for (int i = 0; i < parts.length; ++i) {
            Message message = createMessage(parts[i]);
            if (!message_with_sequenceId_map.containsKey(message.sequenceId)) {
                System.out.println("RM2 update its message list" + message);
                message_queue.add(message);
                message_with_sequenceId_map.put(message.sequenceId, message);
            }
        }
    }

    private static void sendMulticastToRM(Message message) {
        int port = 1234;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] msgData = message.toString().getBytes();
            InetAddress aHost = InetAddress.getByName(multicast_Addr);
            DatagramPacket request = new DatagramPacket(msgData, msgData.length, aHost, port);
            socket.send(request);
            System.out.println("Message multicasted from RM2 to other RMs. Detail:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
//    private static void send_multicast_toRM_updated(Message message) {
//        int port = 1234;
//        DatagramSocket socket = null;
//        try {
//            socket = new DatagramSocket();
//            byte[] msgData = new ObjectMapper().writeValueAsString(message).getBytes();
//            InetAddress aHost = InetAddress.getByName(multicast_Addr);
//            DatagramPacket request = new DatagramPacket(msgData, msgData.length, aHost, port);
//            System.out.println(" total lenght is: " + msgData.length);
//            System.out.println("message: " + new ObjectMapper().readValue(new String(msgData, 0, msgData.length), Message.class));
//            socket.send(request);
//            System.out.println("Message multicasted from RM2 to other RMs. Detail:" + message);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //Execute all request from the lastSequenceID, send the response back to Front and update the counter(lastSequenceID)
    private static void executeAllMessagesInQueue() throws Exception, RMServersDownException {
        while (true) {
            synchronized (RM2.class) {
                Iterator<Message> itr = message_queue.iterator();
                while (itr.hasNext()) {
                    Message msgData = itr.next();
                    System.out.println("executing requests in queue --->" + msgData);
                    if (msgData.sequenceId == lastSequenceID && serversFlag) {
                        String response;
                        try {
                            response = requestToServers(msgData);
                        }
                        catch (RemoteException re){
                            throw new RMServersDownException(re, msgData.sequenceId);
                        }
                        Message message = new Message(msgData.sequenceId, response, "RM2",
                                msgData.MethodCalled, msgData.userID, msgData.newMovieId,
                                msgData.newMovieName, msgData.oldMovieId,
                                msgData.oldMovieName, msgData.bookingCapacity,msgData.numberOfTickets);
                        lastSequenceID += 1;
                        messsageToFront(message.toString(), msgData.FrontEndIpAddress);
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
        switch(PORT)
        {
            case ATWATER_SERVER_PORT:
            {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                registryURL = "rmi://" + HOSTNAME+ ":" + PORT + "/atwater";
                break;
            }
            case VERDUN_SERVER_PORT:
            {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                registryURL = "rmi://" + HOSTNAME+ ":" + PORT + "/verdun";
                break;
            }
            case OUTREMONT_SERVER_PORT:
            {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Registry: " + registry.toString());
                registryURL = "rmi://" + HOSTNAME+ ":" + PORT + "/outremont";
                break;
            }

        }

        admin = (MovieTicketBookingInterface) registry.lookup(registryURL+"/admin");
        customer = (MovieTicketBookingInterface) registry.lookup(registryURL+"/customer");

        if (input.userID.substring(3, 4).equalsIgnoreCase("A")) {
            if (input.MethodCalled.equalsIgnoreCase("addMovieSlots")) {
                System.out.println("Movie name received by RM:" + input.newMovieName);
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
            }else if (input.MethodCalled.equalsIgnoreCase("bookMovieTickets")) {
                    String response = admin.bookMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                    System.out.println(response);
                    return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getBookingSchedule")) {
                    String response = admin.getBookingSchedule(input.userID);
                    System.out.println(response);
                    return response;
            } else if (input.MethodCalled.equalsIgnoreCase("cancelMovieTickets")) {
                    String response = admin.cancelMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                    System.out.println(response);
                    return response;
            }
        } else if (input.userID.substring(3, 4).equalsIgnoreCase("C")) {
            if (input.MethodCalled.equalsIgnoreCase("bookMovieTickets")) {
                String response = customer.bookMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("getBookingSchedule")) {
                String response = customer.getBookingSchedule(input.userID);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("cancelMovieTickets")) {
                String response = customer.cancelMovieTickets(input.userID, input.newMovieId, input.newMovieName,input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.MethodCalled.equalsIgnoreCase("exchangeTickets")) {
                String response = customer.exchangeTickets(input.userID, input.newMovieId, input.newMovieName, input.oldMovieId, input.oldMovieName,input.numberOfTickets);
                System.out.println(response);
                return response;
            }
            else if (input.MethodCalled.equalsIgnoreCase("getAllMovieNames")) {
                String response = customer.getAllMovieNames();
                System.out.println(response);
                return response;
            }
            else if (input.MethodCalled.equalsIgnoreCase("getAllMovieIds")) {
                String response = customer.getAllMovieIds(input.newMovieName);
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
}
