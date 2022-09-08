
package Project;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;


public class LoadBalancer {

    static DatagramSocket serverSocket;

    static DatagramSocket serverSocketForReceive;

    static DatagramPacket receivePacket = null;
    static DatagramPacket sendPacket = null;

    static DatagramPacket receivePacketClient = null;
    static DatagramPacket sendPacketClient = null;

    static String client_message = "";
    static String server_response = "";

    static byte[] receiveData = new byte[2048];
    static byte[] sendData = new byte[2048];

    static String username = "";
    static InetAddress client_ip = null;
    static int client_port = -1;

    static String[] Servers = {"9877", "9878", "9879", "9880"};
    static int[] ServersInInteger = {9877, 9878, 9879, 9880};

    static int[] ServersStatu = {0, 0, 0, 0};

    static boolean[] listOfOpenServers = {false, false, false, false};

    static Random rnd = new Random();

    static Date prevTime = new Date();

    public static void main(String[] args) throws SocketException, IOException {
        serverSocket = new DatagramSocket(9876);
        System.out.println("LoadBalancer is running...");
        while (true) {

            receiveData = new byte[2048];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            client_message = new String(receivePacket.getData());
            client_ip = receivePacket.getAddress();
            client_port = receivePacket.getPort();

            Date currentTime = new Date();

            if (currentTime.getTime() - prevTime.getTime() >= 3000) {

                check_server_status();
                prevTime = new Date();

            }

            String[] message_parts = client_message.split("#");
            if (message_parts[0].equals("JOIN")) {
                accept_user(message_parts);
            } else if (message_parts[0].equals("MESSAGE")) {
                chat();
            } else if (message_parts[0].equals("QUIT")) {
                quit();
            } else if (message_parts[0].equals("SERVER")) {
                weight(message_parts);
            }
            for (int i = 0; i < 4; i++) {
                //System.out.print("Server " + i + " " + ServersStatu[i] + " ");

            }
            
            System.out.println(Arrays.toString(ServersStatu));
            
            System.out.println("");
        }
    }

    public static boolean accept_user(String[] message_parts) throws IOException {
        System.out.println("User " + message_parts[1] + " wants to connect to LoadBalancer.");
        System.out.println("Accepting the connection.");
        username = message_parts[1];
        server_response = "ACCEPT#You connect to the load balancer.";

        send_response();
        return true; //accepted;
    }

    public static void quit() throws IOException {
        System.out.println("User " + username + " wants to end LoadBalancer connection.");
        server_response = "So you want to quit. Bye :( ";
        send_response();

        username = "";
        client_ip = null;
        client_port = -1;
    }

    public static void weight(String[] message_parts) throws IOException {

        ServersStatu[Integer.parseInt(message_parts[2])] = ServersStatu[Integer.parseInt(message_parts[2])] + Integer.parseInt(message_parts[1]);
    }

    public static void weightDecrement(String[] message_parts) throws IOException {

        ServersStatu[Integer.parseInt(message_parts[2])] = ServersStatu[Integer.parseInt(message_parts[2])] - Integer.parseInt(message_parts[1]);
    }

    public static void chat() throws IOException {
        int less = ServersStatu[0];
        int loca = 0;
        
        for (int i = 0; i < Servers.length; i++) {
            if (ServersStatu[i]>0) {
                listOfOpenServers[i]=true;
                less = ServersStatu[i];
            }
        }
        
        for (int i = 0; i < Servers.length; i++) {
            if (less >= ServersStatu[i] && listOfOpenServers[i]) {
                less = ServersStatu[i];
                loca = i;
            }
        }
        server_response = Servers[loca];

        send_response();
    }

    public static void send_response() throws IOException {
        sendData = new byte[2048];
        sendData = server_response.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, client_ip, client_port);
        serverSocket.send(sendPacket);
    }

    public static void check_server_status() throws IOException {
        serverSocketForReceive = new DatagramSocket();
        serverSocketForReceive.setSoTimeout(1000);

        InetAddress server_ip = InetAddress.getByName("localhost");
        String messageOfServers = "";
        for (int i = 0; i < Servers.length; i++) {

            messageOfServers = "CHECK#" + username + "#0#";
            sendData = new byte[2048];
            sendData = messageOfServers.getBytes();
            sendPacketClient = new DatagramPacket(sendData, sendData.length, server_ip, ServersInInteger[i]);
            serverSocketForReceive.send(sendPacketClient);

            while (true) {
                receivePacketClient = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocketForReceive.receive(receivePacketClient);
                    listOfOpenServers[i] = true;
                    break;

                } catch (SocketTimeoutException e) {
                    listOfOpenServers[i] = false;
                    serverSocketForReceive.send(sendPacketClient);
                    break;
                }
            }
            if (listOfOpenServers[i] == true) {
                System.out.println("Server" + i + " Open");
            } else {
                System.out.println("Server" + i + " Close");
            }

        }

    }
}
