import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.mysql.jdbc.util.Base64Decoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class Server {
	int port = 9696;
	ServerSocket socket;
	
	String sqlHost = "jdbc:mysql://mysql4.gear.host/images1";
	String sqlUser = "images1";
	String sqlPass = "Pu2g!A42k~B6";
	Connection con;
	Statement stmt;
	ResultSet rs; // For storing query results
	
	ArrayList<String> images;	
	
	public Server(){
		images = new ArrayList<String>();
		try {
			System.out.println("Connecting...");
			con = DriverManager.getConnection(sqlHost,sqlUser,sqlPass);
			socket = new ServerSocket(port);
			while(true){
				new Client(this,socket.accept());
				System.out.println("New Connection established.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private ResultSet sqlQuery(String query){
		try {
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			stmt = con.createStatement();
			System.out.println("SQL connected.");
			return stmt.executeQuery(query+";");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private int sqlExecute(String command){
		//Class.forName("com.mysql.jdbc.Driver").newInstance();
	
		try {
			stmt = con.createStatement();
			System.out.println("SQL connected.");
			System.out.println("Dispatching sqlExecute: " + command);
			return stmt.executeUpdate(command+";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private boolean saveImageToFile(String fname,String imageString){
		try {
			FileOutputStream fos = new FileOutputStream(new File(fname));
			fos.write(imageString.getBytes());
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}

	
	public void addImageAsString(String name,String imageString){
		try {
			ResultSet r = sqlQuery("SELECT COUNT(image_id) FROM images");
			
			r.next();
			int imageCount = r.getInt(1);
			System.out.println("ImageCount = " + imageCount);
			
			if(saveImageToFile(Integer.toString(imageCount),imageString)){
				sqlExecute("INSERT into images VALUES("+imageCount+",'"+name+"',0 )");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void voteImage(String id, int operation){
		try {
			rs = sqlQuery("SELECT * FROM images where image_id="+id);
			rs.next();
			int returnValue = rs.getInt(3) + operation;
			sqlExecute("UPDATE images SET image_score="+returnValue +" WHERE image_id="+id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String,String> getRandomImage(){
		try {
			rs = sqlQuery("SELECT COUNT(image_id) FROM images");
			rs.next();
			int imageCount = rs.getInt(1);
			if(imageCount>0){
				String selectedImage = Integer.toString((int)(Math.random()*imageCount));
				String outputImage = new Scanner(new File(selectedImage)).useDelimiter("\\Z").next();
				System.out.println("Output Image size = " + outputImage.length() + " Image Actual size: " + new File(selectedImage).length());
				Map<String,String> returnStrings = new HashMap<String,String>();
				rs = sqlQuery("SELECT * FROM images where image_id =" + selectedImage);
				rs.next();
				returnStrings.put("image", outputImage);
				returnStrings.put("title", rs.getNString("image_title"));
				returnStrings.put("score", Integer.toString(rs.getInt(3)));
				System.out.println("Random image retreived. " + rs.getNString("image_title")+ rs.getInt(3));
				return returnStrings;	
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		new Server();
	}

}
