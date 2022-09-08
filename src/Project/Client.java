
package Project;


import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Scanner;


public class Client {

    static Date prevTime = new Date();

    public static void main(String[] args) throws SocketException, UnknownHostException, IOException {
        Scanner scn = new Scanner(System.in);

        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getByName("localhost");

        byte[] sendData = new byte[2048];
        byte[] receiveData = new byte[2048];
        String message = ""; //client sent
        String server_response = "";

        String loadBalancerResponse = "";

        DatagramPacket sendPacket = null;
        DatagramPacket receivePacket = null;

        System.out.println("Enter your username:  ");
        String username = scn.nextLine();

        int count = 0;

        System.out.println("Connecting... ");
        message = "JOIN#" + username;
        sendData = message.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);

        clientSocket.send(sendPacket);

        receivePacket = new DatagramPacket(receiveData, receiveData.length);

        clientSocket.receive(receivePacket);

        String[] response_parts = loadBalancerResponse.split("#");

        if (response_parts[0].equals("ACCEPT")) {
            System.out.println(response_parts[1]);
        }

        message = "MESSAGE#";

        sendData = new byte[2048];
        sendData = message.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);
        clientSocket.send(sendPacket);

        receiveData = new byte[2048];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        loadBalancerResponse = new String(receivePacket.getData());

        System.out.println("Avaliable server port: " + loadBalancerResponse);

        sendData = new byte[2048];

        message = "QUITE#";
        sendData = message.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, 9876);
        clientSocket.send(sendPacket);

        clientSocket.close();
        DatagramSocket clientSocketServer = new DatagramSocket();
        message = "JOIN#" + username;
        int a = ConvertInt(loadBalancerResponse);
        //System.out.println(a);
        sendData = new byte[2048];
        sendData = message.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, a);
        clientSocketServer.send(sendPacket);

        receiveData = new byte[2048];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocketServer.receive(receivePacket);
        server_response = new String(receivePacket.getData());

        String[] serverRes = server_response.split("#");
        System.out.println("Server says: " + serverRes[1]);

        System.out.println("Enter Y to continue or N to finish");
        char option = scn.nextLine().charAt(0);

        
        
        String type = "";
        while (option == 'Y') {
            System.out.println("Enter transection type.");

            message = scn.nextLine();

            if(message.contains("File")){
                System.out.println("Enter file name.");
                type = scn.nextLine();
                message = "MESSAGE#" + message + "#"+type+"#";
            }else{
                message = "MESSAGE#" + message + "#";
            }
            
            sendData = new byte[2048];
            sendData = message.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, a);
            clientSocketServer.send(sendPacket);

            if (message.contains("File")) {
                
                ServerSocket serverSocket = new ServerSocket(ConvertInt(serverRes[2]));
                Socket fileSocket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(fileSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(fileSocket.getOutputStream());
                int bytes = 0;

                FileOutputStream fileOutputStream = new FileOutputStream("geldi" + (++count) + ".pdf");

                long size = dataInputStream.readLong();     // read file size
                byte[] buffer = new byte[4 * 1024];
                while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes;      // read upto file size
                }
                fileOutputStream.close();

                serverSocket.close();
            }
            if (message.contains("Video") && !server_response.contains("Video Sent")) {

                receiveData = new byte[2048];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocketServer.receive(receivePacket);
                server_response = new String(receivePacket.getData());
                //System.out.println("Server says: " + server_response);
                for (int i = 0; i < 32;) {
                    Date currentTime = new Date();

                    if (currentTime.getTime() - prevTime.getTime() >= 500) {
                        receiveData = new byte[2048];
                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientSocketServer.receive(receivePacket);
                        server_response = new String(receivePacket.getData());
                        if (i == 0) {
                            System.out.println("Server sending video... \n" + server_response);
                        } else {
                            System.out.println(server_response);
                        }

                        i++;
                        prevTime = new Date();
                    }
                }

            } else {
                receiveData = new byte[2048];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocketServer.receive(receivePacket);
                server_response = new String(receivePacket.getData());
                System.out.println("Server says: " + server_response);
            }
            System.out.println("Press Y to continue or N to finish");
            option = scn.nextLine().charAt(0);

        }

        message = "Bitti";
        message = "QUIT#" + message + "#";
        sendData = new byte[2048];
        sendData = message.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, server_ip, a);
        clientSocketServer.send(sendPacket);

        receiveData = new byte[2048];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocketServer.receive(receivePacket);
        server_response = new String(receivePacket.getData());
        System.out.println("Server says: " + server_response);

    }

    public static int ConvertInt(String arraySt) {
        String s = "";
        for (int i = 0; i < arraySt.length(); i++) {
            if (arraySt.charAt(i) == '0') {
                s = s + 0;
            }
            if (arraySt.charAt(i) == '1') {
                s = s + 1;
            }
            if (arraySt.charAt(i) == '2') {
                s = s + 2;
            }
            if (arraySt.charAt(i) == '3') {
                s = s + 3;
            }
            if (arraySt.charAt(i) == '4') {
                s = s + 4;
            }
            if (arraySt.charAt(i) == '5') {
                s = s + 5;
            }
            if (arraySt.charAt(i) == '6') {
                s = s + 6;
            }
            if (arraySt.charAt(i) == '7') {
                s = s + 7;
            }
            if (arraySt.charAt(i) == '8') {
                s = s + 8;
            }
            if (arraySt.charAt(i) == '9') {
                s = s + 9;
            }

        }
        return Integer.parseInt(s);
    }
}
