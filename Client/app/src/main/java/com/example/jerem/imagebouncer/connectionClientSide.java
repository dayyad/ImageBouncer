package com.example.jerem.imagebouncer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class connectionClientSide extends Thread {
    private String serverIP = "128.199.236.107";
    private int serverPort = 9696;

    serverConnection connection;
    MainActivity client;
    int currentImage = 0;

    public  void run(){

        try {
            Socket socket = new Socket("128.199.236.107", 9696);
            System.out.println("Connection to server established. !!!");
            connection = new serverConnection((socket));
            connection.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public connectionClientSide(MainActivity client) {
        this.client = client;
    }

    public boolean getNewImage() {
            if(connection!=null){
                connection.getNewImage();
            } else {
                System.out.println("Trying to get new image, connection not established");
            }
        return true;
    }

    public boolean submitImage(byte[] image) {
        if (connection != null) {
            connection.submitImage(image);
        } else {
            System.out.println("Trying to submit image, connection not established");
        }
        return true;
    }


    private class serverConnection extends Thread { //Listen to specific client while socket is open, deals with any client input.
        Socket serverSocket;
        ObjectOutputStream oos;
        ObjectInputStream ois;

        public boolean getNewImage() {
            if (connection != null) {

                try {
                    connection.sendToServer("getImage");
                    InetAddress ip = InetAddress.getLocalHost();
                    NetworkInterface macAddress = NetworkInterface.getByInetAddress(ip);
                    byte[] macBytes = macAddress.getHardwareAddress();
                    oos.writeObject(macBytes);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return true;
        }

        public serverConnection(Socket serverSocket) {
            this.serverSocket = serverSocket;

        }

        void submitImage(byte[] image){
            if (!serverSocket.isClosed()){
                sendToServer("submitImage");
                try {
                    oos.writeObject(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void processInputLine(String line) {
            Scanner lineScanner = new Scanner(line);
            String key = lineScanner.next();

            System.out.println("Server received " + key);

            if (key.equals("voteSuccess")) {
                client.voteSuccess(lineScanner.nextInt() == 1);

            } else if (key.equals("newImage")) {
                try {
                    client.setByte_array((byte[]) ois.readObject());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        void sendToServer(String message) { //Sends any string to the client while connection exists
            if (!serverSocket.isClosed()) {
                try {
                    oos.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void run() {
            try {
                oos = new ObjectOutputStream(serverSocket.getOutputStream());
                ois = new ObjectInputStream(serverSocket.getInputStream());

                while (true) {
                    System.out.println("read one line.");
                    processInputLine((String) ois.readObject());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}