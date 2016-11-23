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

        public boolean getNewImage() {
            if (connection != null) {
                connection.sendToServer("getImage");
                try {
                    InetAddress ip = InetAddress.getLocalHost();
                    NetworkInterface macAddress = NetworkInterface.getByInetAddress(ip);
                    byte[] macBytes = macAddress.getHardwareAddress();
                    oos.writeObject(macBytes);
                    oos.close();

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

            try {
                oos = new ObjectOutputStream(serverSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                currentImage = lineScanner.nextInt();
                InputStream is = null;
                URL url = null;
                System.out.println("Trying to download new image from URL.");
                try {
                    url = new URL(serverIP + "/" + Integer.toString(currentImage));
                    is = url.openStream();
                    byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
                    int n;

                    while ((n = is.read(byteChunk)) > 0) {
                        baos.write(byteChunk, 0, n);
                    }
                } catch (IOException e) {
                    System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
                    e.printStackTrace();
                    // Perform any other exception handling that's appropriate.
                } finally {
                    if (is != null) try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                client.setByte_array(baos.toByteArray());

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
                Scanner inputScanner = new Scanner(serverSocket.getInputStream());

                while (!serverSocket.isClosed()) {
                    if (inputScanner.hasNextLine()) {
                        processInputLine(inputScanner.nextLine());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}