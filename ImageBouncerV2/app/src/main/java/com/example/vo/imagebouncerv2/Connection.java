package com.example.vo.imagebouncerv2;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Connection extends Thread{
    String IP = "128.199.236.107";
    int port = 9696;
    MainActivity client;
    Scanner scanner;
    PrintWriter PW;
    Socket socket;
    String currentImageId;



    public Connection(MainActivity mainActivity) {
        client = mainActivity;
        System.out.println("Trying to start new clinet.");
    }

    public void run(){
        try {
            socket = new Socket(IP,port);
            System.out.println("Client Connected.");
            scanner=new Scanner(socket.getInputStream());
            PW = new PrintWriter(socket.getOutputStream());

            while(scanner.hasNext()){
                processLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Process all receive signals here.
    private void processLine(){
        if(scanner.hasNext()){
            String key = scanner.next();
            System.out.println("Received Message with key: " + key);

            if(key.equals("newImage")){
                // newImage id title score imageString
                String buffer ="";
                String currentImageId = scanner.next();
                String title = scanner.next();
                String score = scanner.next();

                boolean done = false;
                while(!done && scanner.hasNext()){
                    String next = scanner.next();
                    if(next.equals("endOfImageStream")){
                        System.out.println("reached end of image stream.");
                        done = true;
                    } else {
                        buffer = buffer + next;
                        System.out.println("Adding line to buffer");
                    }
                }
                System.out.println("Total image received size: " + buffer.length());
                client.setImageFromString(title,score,buffer);

            }
        }
    }

    public boolean sendLine(String line){
        try {
            if(socket!=null) {
                PW = new PrintWriter(socket.getOutputStream());
                    PW.write(line);
                    PW.flush();
                    return true;
                } else {
                    PW.flush();
                    return false;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
