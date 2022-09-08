
package Project;


import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Server4 {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    static int weight;
    static DatagramSocket serverSocket;

    static DatagramPacket receivePacket = null;
    static DatagramPacket sendPacket = null;

    static String client_message = "";
    static String server_response = "";

    static byte[] receiveData = new byte[2048];
    static byte[] sendData = new byte[2048];

    static String username = "";
    static InetAddress client_ip = null;
    static int client_port = -1;

    static Random rnd = new Random();

    //Load Balancer İnformation
    static byte[] sendLoad = new byte[2048];
    static String feedback = "";
    static DatagramPacket sendPacketLoad = null;

    static Date prevTime = new Date();

    public static void main(String[] args) throws SocketException, IOException, InterruptedException, UnknownHostException {
        serverSocket = new DatagramSocket(9880);
        System.out.println("Server4 is running...");

        while (true) {
            receiveData = new byte[2048];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            client_message = new String(receivePacket.getData());
            client_ip = receivePacket.getAddress();
            client_port = receivePacket.getPort();
            String[] message_parts = client_message.split("#");
            if (message_parts[0].equals("JOIN")) {
                accept_user(message_parts);
            } else if (message_parts[0].equals("MESSAGE")) {
                chat(message_parts);
            } else if (message_parts[0].equals("QUIT")) {
                quit();
            }else if (message_parts[0].equals("CHECK")) {
                check();
            }
        }
    }

    public static boolean accept_user(String[] message_parts) throws IOException {

        System.out.println("User " + message_parts[1] + " wants to connect to Server.");
        System.out.println("Accepting the connection.");
        username = message_parts[1];
        server_response = "ACCEPT#I can\n1) Send File'File' File Names:File1-File2-File3-deneme\n2) Make Computation'Computation'\n3) Send Video'Video'#"+client_port;

        send_response();
        return true;
    }
    public static boolean check() throws IOException {

        System.out.println("LoadBalancer check");
        server_response = "cheked";

        send_response();
        return true;
    }

    public static void processCompleted(int weight) throws IOException {

        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getByName("localhost");

        server_response = "Your Process Completed ";
        int negative = -weight;
        feedback = "" + negative;
        feedback = "SERVER#" + feedback + "#3#";
        sendData = new byte[2048];
        sendData = feedback.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);
        clientSocket.send(sendPacket);

    }

    public static void quit() throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getByName("localhost");
        System.out.println("User " + username + " wants to end Server connection.");
        server_response = "So you want to quit. Bye :( ";
        send_response();

        username = "";
        client_ip = null;
        client_port = -1;

    }

    public static void chat(String[] message_parts) throws IOException, InterruptedException {
        //server_response = possible_messages[rnd.nextInt(possible_messages.length)];
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getByName("localhost");

        if (message_parts[1].equals("File")) {
            weight = 1;
            feedback = "1";
            feedback = "SERVER#" + feedback + "#3#";
            sendData = new byte[2048];
            sendData = feedback.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);
            clientSocket.send(sendPacket);

            Socket socket = new Socket("localhost", client_port);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            int bytes = 0;
            
            String a= message_parts[2]+".pdf";
            File file = new File(a);
            FileInputStream fileInputStream = new FileInputStream(file);

            // send file size
            dataOutputStream.writeLong(file.length());
            // break file into chunks
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }
            fileInputStream.close();

            server_response = "File Send";
        } else if (message_parts[1].equals("Computation")) {
            weight = 5;
            feedback = "5";
            feedback = "SERVER#" + feedback + "#3#";
            sendData = new byte[2048];
            sendData = feedback.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);
            clientSocket.send(sendPacket);
            TimeUnit.SECONDS.sleep(5);
            server_response = "Computed";
        } else if (message_parts[1].equals("Video")) {

            int counter = 32;
            weight = 10;
            feedback = "10";
            feedback = "SERVER#" + feedback + "#3#";
            sendData = new byte[2048];
            sendData = feedback.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);
            clientSocket.send(sendPacket);

            int i = 0;

            while (i < 32) {

                Date currentTime = new Date();
                if (currentTime.getTime() - prevTime.getTime() >= 500) {

                    i++;

                    boolean randomTrueFalse = rnd.nextBoolean();

                    if (randomTrueFalse) {
                        server_response = "10100101";
                        send_response();
                    } else {
                        server_response = "01011001";
                        send_response();
                    }

                    prevTime = new Date();

                }
            }

            //TimeUnit.SECONDS.sleep(10);
            server_response = "Video Sent";
        } else {
            server_response = "Hay allah";
        }
        send_response();
        processCompleted(weight);
    }

    public static void send_response() throws IOException {
        sendData = new byte[2048];
        sendData = server_response.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, client_ip, client_port);
        serverSocket.send(sendPacket);
    }
}

