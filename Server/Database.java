import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;


public class Database {

	public static final String IP = "128.199.236.207";
	public static final String port = "9696";
	
	private static int totalvotes = 0;
	
	private ArrayList<User> users;
	private ArrayList<Picture> pictures;
	private connection connection;
	
	public Database() {
		// TODO Auto-generated constructor stub
		System.out.println("Starting Server...");
		users = new ArrayList<User>();
		pictures = new ArrayList<Picture>();
		connection = new connection(this);
		
		
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param MacAddress The mac address of the user who needs the image
	 * @return The image ID/Location as an integer
	 */
	
	public int getImage(byte[] macaddress){ 			
			Picture img =  getNextImg(); 
			User user = findMacAddress(macaddress);
			
			assert img != null;
			
			if(user==null){
				User newuser = new User(macaddress);
				newuser.changeImage(img);
			}
			user.changeImage(img);//TODO needs some kind of confirmation from the user
			
			
			return img.getID();
		
	}
	/**
	 * 
	 * @param A byte array of the image data
	 * @return True if the image was added sucessfully
	 */
	public boolean addImage(byte[] data){
		
		System.out.println("Adding new image to: ");
		printPicsInfo();
		
		BufferedImage img = null;
		
		
		try{
			 img = ImageIO.read(new ByteArrayInputStream(data));
				}catch(IOException e){System.out.println("Image creation failed");}
		
		
		assert img != null;
		if(img ==null){System.out.println("Picture not added"); return false;}
		
		Picture pic = new Picture(img);
		
		//Jonah added this
		try {
		    // retrieve image
		    File outputfile = new File("saved.png");
		    ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		//Jonah added this
		
		pictures.add(pic);
		//TODO Insert proper checks and assert statements
		
		System.out.println("New data:");
		printPicsInfo();
		
		return true;
			}
	
	/**
	 * 
	 * @param vote Boolean true = up. false = down
	 * @param ImageLocation
	 * @return
	 */
	public boolean voteImage(boolean vote, int ID){
		for(Picture img :pictures){
			if(img.getID()==ID){
				img.addVote(vote);
				return true;
			}
			
		}
		return false;
		
	}
	
	
	/**
	 * 
	 * @param mac Mac address in byte[] for you want to search
	 * @return User matching the mac address, if notbody matches, return null
	 */
	private User findMacAddress(byte[] mac){ //TODO horribly inefficient
		//Checks if it is a known user
		for(User usr:users){
			ArrayList<byte[]>usraddresses = usr.addresses;
			
			for(byte[] address:usraddresses){
				if( mac==address){
					return usr;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Assumes there are images in Pictures
	 * @return The next image to be given to the next user, given a uniform distrubution wieghted by the votes each image has
	 */
	private Picture getNextImg(){
		int imgnumber = (int) (Math.random()*totalvotes); 
		double votecurrent = 0;
		
		for(Picture pic: pictures){
			votecurrent += pic.getVotes();
			if(votecurrent>=imgnumber){
				return pic;
			}
		}
		return null;
		
	}
		
	
	private void printPicsInfo(){
		
		System.out.println("Number of pictures" + pictures.size());
		
		int currentvotes = 0;
		int currentposition = 0;
		System.out.println("Counting votes and showing pictrue info:");
		
		for(Picture pic:pictures){
			currentposition++;
			
			System.out.println("current array position: " + currentposition);
			
			System.out.println("ID: " + pic.getID() + "votes: " + pic.getVotes() + "FilePath: " + pic.getLocation().toString());
			currentvotes += pic.getVotes();
		}
		System.out.println("Votes counted: " + currentvotes);
		System.out.println("Expected total: " + totalvotes);
		
		
	}
	
	
	private void printUserInfo(){
		
		System.out.println("Current number of registered users: " + users.size());
		
		
		for(User user:users){
			System.out.println("ID: " + user.getID());
			System.out.println("CurrentImage: " + user.getCurrentImage());
			
		}
	}

static class User {//TODO Stop identifying users through mac addresses, inefficient
	
	public static int UID = 1;
	
	private ArrayList<byte[]> addresses;
	private Picture currentimage;
	private int ID;

	public User(byte[] mac) {

		addresses = new ArrayList<byte[]>();
		addresses.add(mac);
		
		ID = UID; UID++;

	}
	
	
	/**
	 * 
	 * @param change the img the client is viewing 
	 */
	public void changeImage(Picture img){
		currentimage = img;
	}
	
	
	
	/**
	 * 
	 * @return the image the client is viewing
	 */
	
	public Picture getCurrentImage(){
		return currentimage;
	}
	
	
	/**
	 * 
	 * @param newmac bye array of an additional mac address the user has
	 */
	public void addAddress(byte[] newmac){
		addresses.add(newmac);
	}
	
	/**
	 * 
	 * @returns an ArrayList of byte[]'s containing the known Mac addresses for the user
	 */
	
	public ArrayList<byte[]> getAddresses(){
		return addresses;
	}
	
	public int getID(){
			return ID;
	}

}

static class Picture {
	public static int UID = 0;

	private int ID;
	private File imagelocation;
	private BufferedImage img;
	private int votes;
	
	public Picture(BufferedImage imag) {
		UID++;
		votes++;
		totalvotes++;
		ID = UID;
		img = imag;
		
		imagelocation = new File(ID + ".jpg");
		try {
			
			ImageIO.write(imag, "jpg", imagelocation);
		
		} catch (IOException e) {
			System.out.println("Failed to save file to Server");
		}
		
	}
	
	
	public int getID(){
		return ID;
	}
	
	public File getLocation(){
		return imagelocation;
	}

	public void addVote(boolean vote){
		if(vote){
		votes++;
		totalvotes++;
		return;}
		votes--;
		totalvotes--;
		return;
	}
	
	public int getVotes(){
		return votes;
	}
	
	public BufferedImage getImg(){
		return img;
	}
	
	public String toString(){
		System.out.println("UID: " + UID + "ID" + ID + "Imagelocation: " + imagelocation.toString() + "votes: " + votes);
		return ""; //ADDED THIS LINE TO MAKE COMPILE
	}
}


public static void main(String[] args) { new Database();}


}