import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class Client extends Thread{
	Socket socket;
	Scanner scanner;
	Server server;
	PrintWriter PW;
	
	public Client(Server server,Socket socket){
		this.server = server;
		this.socket = socket;
		this.start();
	}
	
	private void sendMessage(String line){
		//System.out.println("trying to send: "+line);
		if(socket!=null){
			try {
				PW = new PrintWriter(socket.getOutputStream());
				
				PW.write(line);
				System.out.println("Sent a message");
				
				PW.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run(){
		try {
			scanner = new Scanner(socket.getInputStream());
			PW = new PrintWriter(socket.getOutputStream());
			
			while (scanner.hasNext()){
				processLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void processLine(){
		if(scanner.hasNext()){
			String key = scanner.next();
			System.out.println("Server Received key: " + key);
			
			
			if(key.equals("getImage")){
				Map <String,String> image = server.getRandomImage();
				if (image!=null){
					
					// newImage id title score imageString
					sendMessage("newImage " + " " + image.get("id") + " " + image.get("title") + " " + image.get("score")+ " " + image.get("image"));
					sendMessage(" endOfImageStream ");
					System.out.println("Sent client image with EOS");
				} else {
					System.out.println("Image selected was null :(");
				}
			} else if(key.equals("newImage")){
				String buffer ="";
				String name = scanner.next();
				boolean done = false;
				while(!done && scanner.hasNext()){
					String next = scanner.next();
					if(next.equals("endOfImageStream")){
						System.out.println("reached end of image stream.");
						done = true;
					} else {
						buffer = buffer + next;
						//System.out.println("Adding line to buffer");
					}
				}
				System.out.println("Final Size of image buffer: " + buffer.length());
				server.addImageAsString(name,buffer);
			}
		}
	}
}
