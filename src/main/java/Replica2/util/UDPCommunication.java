package Replica2.util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//server will send and receive data as per the requirements.
public class UDPCommunication {

    public static String sendMessage(String method, String serverPort, String hostName, String paramName) {

        String response = null;
        DatagramSocket socket = null;
        //Here , parameter can be movieName, customerID for these 2 cases, might add up in future.
        try {
            InetAddress aHost = InetAddress.getByName(hostName); //getIPaddress
            socket = new DatagramSocket(); //it will bind to any arbitary port
            //String methodCalled = "getAvailableMoviesForAdmin";
            String methodCalledWithParam = "";


            switch (method) {
                case "getAvailableShowsForMovie": {
                    methodCalledWithParam = "getAvailableShowsForMovie" + ":" + paramName; // here parameter is movieName
                    byte[] message = methodCalledWithParam.trim().getBytes();
                    DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                    socket.send(request);
                    break;
                }
                case "getAllMoviesBookedByCustomer": {
                    methodCalledWithParam = "getAllMoviesBookedByCustomer" + ":" + paramName; // here parameter is customerID
                    byte[] message = methodCalledWithParam.trim().getBytes();
                    DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                    socket.send(request);
                    break;
                }
                case "verifyUserID": {
                    methodCalledWithParam = "verifyUserID" + ":" + paramName; // here parameter is customerID
                    byte[] message = methodCalledWithParam.trim().getBytes();
                    DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                    socket.send(request);
                    break;
                }
            }
            //receive reply from server with port number
//                response = receiveReplyFromServerWithPort(serverPort);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            socket.receive(reply);
            System.out.println("Reply received from the server with port number " + serverPort + " is: "
                    + new String(reply.getData()));
            response = new String(reply.getData());

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }

        return response;

    }

    //sendMessage to book ticket on server requested by customer
    public static String sendMessageToBookTicket(String serverPort, String hostName, String... parameters) {

        String response = null;
        DatagramSocket socket = null;
        //Here , parameter can be movieName, customerID,movieName, number of tickets.
        List<String> listOfParams = new ArrayList<String>();

        for(String param : parameters)
        {
            listOfParams.add(param);
        }

        String allParameters = listOfParams.stream().collect(Collectors.joining(","));

        try {
            InetAddress aHost = InetAddress.getByName(hostName); //getIPaddress
            socket = new DatagramSocket(); //it will bind to any arbitary port
            //String methodCalled = "getAvailableMoviesForAdmin";
            String methodCalledWithParam = "";

            //todo call booking method, modify booking
            methodCalledWithParam = "bookMovieTicketForCustomer" + ":" + allParameters;
            byte[] message = methodCalledWithParam.trim().getBytes();
            DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
            socket.send(request);

            //receive reply from server with port number
            //response = receiveReplyFromServerWithPort(serverPort);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            socket.receive(reply);
            System.out.println("Reply received from the server with port number " + serverPort + " is: "
                    + new String(reply.getData()));
            response = new String(reply.getData());

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }

        return response;

    }

    //sendMessage to cancel ticket on server requested by customer
    public static String sendMessageToCancelTicket(String serverPort, String hostName, String... parameters) {

        String response = null;
        DatagramSocket socket = null;
        //Here , parameter can be movieName, customerID,movieName, number of tickets.
        List<String> listOfParams = new ArrayList<String>();

        for(String param : parameters)
        {
            listOfParams.add(param);
        }

        String allParameters = listOfParams.stream().collect(Collectors.joining(","));

        try {
            InetAddress aHost = InetAddress.getByName(hostName); //getIPaddress
            socket = new DatagramSocket(); //it will bind to any arbitary port
            //String methodCalled = "getAvailableMoviesForAdmin";
            String methodCalledWithParam = "";

            methodCalledWithParam = "cancelMovieTicketForCustomer" + ":" + allParameters;
            byte[] message = methodCalledWithParam.trim().getBytes();
            DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
            socket.send(request);

            //receive reply from server with port number
            //response = receiveReplyFromServerWithPort(serverPort);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            socket.receive(reply);
            System.out.println("Reply received from the server with port number " + serverPort + " is: "
                    + new String(reply.getData()));
            response = new String(reply.getData());

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }

        return response;

    }

    public static String sendMessageToGetAllMovies(String method,String serverPort, String hostName, String... paramName)
    {
        String response = null;
        DatagramSocket socket = null;
        try {
            InetAddress aHost = InetAddress.getByName(hostName); //getIPaddress
            socket = new DatagramSocket(); //it will bind to any arbitary port

            String methodCalledWithParam = "";

            switch (method) {
                case "getAllMovieNames": {
                    methodCalledWithParam = "getAllMovieNames:";
                    byte[] message = methodCalledWithParam.trim().getBytes();
                    DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                    socket.send(request);
                    break;
                }
                case "getAllMovieIds": {
                    if(paramName.length>0)
                    {
                        methodCalledWithParam = "getAllMovieIds:" + paramName[0]; // here parameter is movieName
                        byte[] message = methodCalledWithParam.trim().getBytes();
                        DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                        socket.send(request);
                        break;
                    }
                }
            }

            //receive reply from server with port number
            //response = receiveReplyFromServerWithPort(serverPort);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            socket.receive(reply);
            System.out.println("Reply received from the server with port number " + serverPort + " is: "
                    + new String(reply.getData()));
            response = new String(reply.getData());

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }

        return response;
    }

    public static String sendMessageToIsMovieAlreadyBookedByCustomer(String method,String serverPort, String hostName, String... paramName)
    {
        String response = null;
        DatagramSocket socket = null;
        try {
            InetAddress aHost = InetAddress.getByName(hostName); //getIPaddress
            socket = new DatagramSocket(); //it will bind to any arbitary port

            //Here , parameter can be movieName, customerID,movieID, movieName
            List<String> listOfParams = new ArrayList<String>();

            for(String param : paramName)
            {
                    listOfParams.add(param);
            }

            String allParameters = listOfParams.stream().collect(Collectors.joining("-"));

            String methodCalledWithParam = "";

            switch (method) {
                case "isMovieAlreadyBookedByCustomer": {
                    methodCalledWithParam = "isMovieAlreadyBookedByCustomer:" + allParameters;
                    byte[] message = methodCalledWithParam.trim().getBytes();
                    DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                    socket.send(request);
                    break;
                }
                case "isMovieAlreadyBooked": {
                    methodCalledWithParam = "isMovieAlreadyBooked:" + allParameters;
                    byte[] message = methodCalledWithParam.trim().getBytes();
                    DatagramPacket request = new DatagramPacket(message, methodCalledWithParam.length(), aHost, Integer.parseInt(serverPort));
                    socket.send(request);
                    break;
                }
            }

            //receive reply from server with port number
            //response = receiveReplyFromServerWithPort(serverPort);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            socket.receive(reply);
            System.out.println("Reply received from the server with port number " + serverPort + " is: "
                    + new String(reply.getData()));
            response = new String(reply.getData());

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }

        return response;
    }


}
