package FrontEnd;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;

public class FE {
    private static final int sequencerPort = 1333;
    //private static final String sequencerIP = "192.168.2.17";
    private static final String sequencerIP = "localhost";
    private static final String RM_Multicast_group_address = "230.1.1.10";
//    private static final String RM_Multicast_group_address = "localhost";
    private static final int FE_SQ_PORT = 1414;
    private static final int FE_PORT = 1999;
    private static final int RM_Multicast_Port = 1234;
    //public static String FE_IP_Address = "192.168.2.11";
    public static String FE_IP_Address = "localhost";

    public static void main(String[] args) {
        try {
            FEInterface inter = new FEInterface() {
                @Override
                public void informRmHasBug(int RmNumber) {
//                    String errorMessage = new MyRequest(RmNumber, "1").toString();
                    MyRequest errorMessage = new MyRequest(RmNumber, "1");
                    System.out.println("Rm:" + RmNumber + "has bug");
//                    sendMulticastFaultMessageToRms(errorMessage);
                    sendUnicastToSequencer(errorMessage);
                }

                @Override
                public void informRmIsDown(int RmNumber) {
//                    String errorMessage = new MyRequest(RmNumber, "2").toString();
                    MyRequest errorMessage = new MyRequest(RmNumber, "2");
                    System.out.println("Rm:" + RmNumber + "is down");
//                    sendMulticastFaultMessageToRms(errorMessage);
                    sendUnicastToSequencer(errorMessage);
                }

                @Override
                public int sendRequestToSequencer(MyRequest myRequest) {
                    return sendUnicastToSequencer(myRequest);
                }

                @Override
                public void retryRequest(MyRequest myRequest) {
                    System.out.println("No response from all Rms, Retrying request...");
                    sendUnicastToSequencer(myRequest);
                }
            };
            FrontEndImplementation servant = new FrontEndImplementation(inter);
            Runnable task = () -> {
                listenForUDPResponses(servant);
            };
            Thread thread = new Thread(task);
            thread.start();
            // create and initialize the ORB //// get reference to rootpoa &amp; activate
            // the POAManager

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", String.valueOf(FE_PORT));
            props.put("org.omg.CORBA.ORBInitialHost", FE_IP_Address);

            ORB orb = ORB.init(args, props);

            //tnameserv -ORBInitialPort 1234

            // -ORBInitialPort 1050 -ORBInitialHost localhost
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            servant.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            MovieTicketBooking.MovieTicketBookingInterface href = MovieTicketBooking.MovieTicketBookingInterfaceHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent[] path = ncRef.to_name("FrontEnd");
            ncRef.rebind(path, href);

            System.out.println("FrontEnd Server is Up & Running");
//            Logger.serverLog(serverID, " Server is Up & Running");

//            addTestData(servant);
            // wait for invocations from clients
            while (true) {
                orb.run();
            }
        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
//            Logger.serverLog(serverID, "Exception: " + e);
        }

//        System.out.println("FrontEnd Server Shutting down");
//        Logger.serverLog(serverID, " Server Shutting down");

    }

    private static int sendUnicastToSequencer(MyRequest requestFromClient) {
        DatagramSocket aSocket = null;
        String dataFromClient = requestFromClient.toString();
        System.out.println("FE:sendUnicastToSequencer>>>" + dataFromClient);
        int sequenceID = 0;
        try {
            aSocket = new DatagramSocket(FE_SQ_PORT);
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName(sequencerIP);
            DatagramPacket requestToSequencer = new DatagramPacket(message, dataFromClient.length(), aHost, sequencerPort);

            aSocket.send(requestToSequencer);

            aSocket.setSoTimeout(1000);
            // Set up an UPD packet for recieving
            byte[] buffer = new byte[1000];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            // Try to receive the response from the ping
            aSocket.receive(response);
            String sentence = new String(response.getData(), 0,
                    response.getLength());
            System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>" + sentence);
            sequenceID = Integer.parseInt(sentence.trim());
            System.out.println("FE:sendUnicastToSequencer/ResponseFromSequencer>>>SequenceID:" + sequenceID);
        } catch (SocketException e) {
            System.out.println("Failed: " + requestFromClient.noRequestSendError());
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed: " + requestFromClient.noRequestSendError());
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        return sequenceID;
    }

    public static void sendMulticastFaultMessageToRms(String errorMessage) {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] messages = errorMessage.getBytes();
            InetAddress aHost = InetAddress.getByName(RM_Multicast_group_address);

            DatagramPacket request = new DatagramPacket(messages, messages.length, aHost, RM_Multicast_Port);
            System.out.println("FE:sendMulticastFaultMessageToRms>>>" + errorMessage);
            aSocket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void listenForUDPResponses(FrontEndImplementation servant) {
        DatagramSocket aSocket = null;
        try {

//            aSocket = new MulticastSocket(1413);
//            InetAddress[] allAddresses = Inet4Address.getAllByName("SepJ-ROG");
            InetAddress desiredAddress = InetAddress.getByName(FE_IP_Address);
//            //In order to find the desired Ip to be routed by other modules (WiFi adapter)
//            for (InetAddress address :
//                    allAddresses) {
//                if (address.getHostAddress().startsWith("192.168.2")) {
//                    desiredAddress = address;
//                }
//            }
//            aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));
            aSocket = new DatagramSocket(FE_PORT, desiredAddress);
            byte[] buffer = new byte[1000];
            System.out.println("FE Server Started on " + desiredAddress + ":" + FE_PORT + "............");

            while (true) {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(response);
                String sentence = new String(response.getData(), 0,
                        response.getLength()).trim();
                System.out.println("FE:Response received from Rm>>>" + sentence);
                RmResponse rmResponse = new RmResponse(sentence);
//                String[] parts = sentence.split(";");

                System.out.println("Adding response to FrontEndImplementation:");
                servant.addReceivedResponse(rmResponse);
//                DatagramPacket reply = new DatagramPacket(response.getData(), response.getLength(), response.getAddress(),
//                        response.getPort());
//                aSocket.send(reply);
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
//            if (aSocket != null)
//                aSocket.close();
        }
    }
}
