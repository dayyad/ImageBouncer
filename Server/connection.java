import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class connection {
	ArrayList<serverConnection> connections = new ArrayList<serverConnection>();

	public connection(Database DB){ //Waits for a new connection then creates a clientConnection Object
		ServerSocket serverSocket;
		System.out.println("Opening connection...");
		try {
			serverSocket = new ServerSocket(9696);
			
			
			while(true){
				Socket newConnectionSocket = serverSocket.accept();
				System.out.println("trying something...");
				serverConnection newConnection = new serverConnection(newConnectionSocket,DB);
				connections.add(newConnection);
				newConnection.start();
				System.out.println("New Connection started!!!");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private class serverConnection extends Thread { //Listen to specific client while socket is open, deals with any client input.
		Socket clientSocket;
		Database DB;
		Scanner inputScanner;
		
		public serverConnection(Socket clientSocket,Database DB){
			this.clientSocket = clientSocket;
			this.DB = DB;
		}

		void sendToClient(String message){ //Sends any string to the client while connection exists
			if(clientSocket!=null){
				try {
					PrintWriter PW = new PrintWriter(clientSocket.getOutputStream());
					PW.write(message);
					PW.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void run(){
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(clientSocket.getInputStream());
				
				while(true){ //Always grabs next "key" string of incoming data
					try {
						String key = (String) (ois.readObject());
						
						System.out.println("Received key: " + key);

						if(key!=null){
							if(key.equals("upVote")){
								DB.voteImage(true,(Integer) ois.readObject());

							} else if (key.equals("downVote")){

								if(DB.voteImage(false,(Integer) ois.readObject())){
									sendToClient("voteSuccess 1");
								} else {
									sendToClient("voteSuccess 0");
								}

							} else if (key.equals("submitImage")){
								//Receives in format "submitImage SIZE_IN_BYTES" BYTE[]
								
								byte[] image = (byte[]) ois.readObject();
								DB.addImage(image);
								
							} else if (key.equals("getImage")){
								
								byte[] macAddress = (byte[]) (ois.readObject());
								int imageLoc = DB.getImage(macAddress);
								sendToClient("newImage " + imageLoc);
							}

						}
						
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
	}

	public static void main(String[] args) {

	}

}
