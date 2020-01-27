
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Client {

	public static byte[] receive = new byte[65535];
	public static DatagramSocket ds;
	public static DatagramPacket DpReceive;
	public static Scanner sc;

	public static HashMap<Integer, InetAddress> servers;

	public static void main(String args[]) throws IOException {

		servers = new HashMap<Integer, InetAddress>();
		InetAddress firstServerIP = InetAddress.getLocalHost();
		int firstServerPort = 20152;
		sc = new Scanner(System.in);

		// retrieve IP/ports of all servers in pool
		init(firstServerIP, firstServerPort);

		int choice = 0;
		do{
			System.out.println("Query for content? (1) \nInform and Update? (2)\nExit? (0)");
			choice = sc.nextInt();
			if (choice == 1) queryForContent();
			else if (choice == 2) informAndUpdate();
		} while(choice!=0);

		exit();
	}

	//TODO: Implement once you have 4 servers
	static void exit(){
		System.out.println("Exiting, goodbye!");
	}

	static void queryForContent() throws SocketException, IOException{
		sc = new Scanner(System.in);
		ds = new DatagramSocket();

		while (true) {

			System.out.println("Please enter file to be fetched or type exit: ");
			String filename = sc.nextLine();

			if (filename.equals("exit"))
				break; //TODO: Send the exit msg to server 1

			// int serverNum = hashFilename(filename);
			int serverNum = 1; //TODO: Remove this once you have all 4 servers
			InetAddress destIP = servers.get(serverNum);
			int destPort = 20151+serverNum;
			byte[] requestInfo = filename.getBytes();
			DatagramPacket DpSend = new DatagramPacket(requestInfo, requestInfo.length, destIP, destPort);

			System.out.println("Requesting file " + filename);
			ds.send(DpSend);

			//Wait for server to return the user IP, then:
			InetAddress userIP = getUserIP();
			getImageFromUser(userIP, filename);
		}
	}

	//Listen for sever response for userIP with certain filename;
	static InetAddress getUserIP() throws IOException {
		while (true) {
			// Receive server response
			DpReceive = new DatagramPacket(receive, receive.length);
			ds.receive(DpReceive);
			InetAddress userIP;

			if (receive != null) {
				userIP = InetAddress.getByAddress(Arrays.copyOfRange(receive, 0, 4));
				return userIP;
			}
		}
	}

	//Nicks code, retrieving filename from user with userIP
	//Will be transfered to multithread
	//TODO: wait for Nick to do the multithread
	static void getImageFromUser(InetAddress userIP, String filename) throws IOException {
		String path = "/Users/tsk/Documents/School/3rdyear/CPS706/src/imgdstn/";
		int userPort = 20151;
		byte[] imageBytes;
		int imageSize;
		BufferedImage image;

		filename = filename + ".jpg";

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket = new Socket(userIP, userPort);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

		outToServer.writeBytes(filename + "\n");

		// read image size as byte array and image as byte array
		byte[] imgSizeBytes = new byte[4];
		inFromServer.read(imgSizeBytes);
		imageSize = ByteBuffer.wrap(imgSizeBytes).asIntBuffer().get();
		imageBytes = new byte[imageSize];
		inFromServer.read(imageBytes);

		// convert byte array to image and save image
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
		image = ImageIO.read(byteArrayInputStream);
		ImageIO.write(image, "jpg", new File(path + filename));

		System.out.println("Received Image!");
		clientSocket.close();
	}

	// Method contacts server #1 and retrieves all server IPs from DHS pool
	static void init(InetAddress destIP, int destPort) throws IOException {

		InetAddress serverIP;
		int serverPort;
		ds = new DatagramSocket(20150);

		//Construct the init query
		DatagramPacket query;
		String initStr = "init";
		byte[] initBytes = initStr.getBytes();
		byte[] myIP = InetAddress.getLocalHost().getAddress();
  	byte[] myPort = Integer.toString(20150).getBytes();
  	byte[] queryBytes = new byte[myIP.length + myPort.length + initBytes.length + 1];
  	System.arraycopy(initBytes, 0, queryBytes, 0, initBytes.length);
  	System.arraycopy(myIP, 0, queryBytes, initBytes.length, myIP.length);
  	System.arraycopy(myPort, 0, queryBytes, myIP.length+initBytes.length, myPort.length);

		//Send the init query
  	query = new DatagramPacket(queryBytes, queryBytes.length, destIP, destPort);
		ds.send(query);

		//wait for 4 servers
		receiveServerIPs();
	}

	//Listen for server response for serverIPs
	static void receiveServerIPs() throws IOException, UnknownHostException{
		int serverCounter = 1;
		InetAddress serverIP;
		int serverPort;
		while (serverCounter<5) {

			// Receive server response
			DpReceive = new DatagramPacket(receive, receive.length);
			ds.receive(DpReceive);

			if (receive != null) {
				// Extract first 4 bytes as IP
				serverIP = InetAddress.getByAddress(Arrays.copyOfRange(receive, 0, 4));
				// Extract remainder as port #
				serverPort = Integer.parseInt(data(Arrays.copyOfRange(receive, 4, receive.length)).toString());

				// Put into servers table
				servers.put(serverCounter, serverIP);

				serverCounter++;
			}
		}
	}

	//Upload
	static void informAndUpdate() throws IOException {

		File uploadFile;
		String filename;
		//Checking if file is in the src folder
		do{
			System.out.println("What is the filename of the file you wish to upload?");
			sc = new Scanner(System.in);
			filename = sc.nextLine();
			System.out.println("Uploading file " + filename);

			String path = "/Users/tsk/Documents/School/3rdyear/CPS706/src/imgsrc/";
			uploadFile = new File(path + filename + ".jpg");

			if(!uploadFile.exists()) System.out.println("File not found, please try again");

		} while (!uploadFile.exists());



		// int serverNum = hashFilename(filename);
		int serverNum = 1; //TODO: Remove this once you have all 4 servers
		InetAddress serverIP = servers.get(serverNum);
		int serverPort = 20151 + serverNum;

		DatagramSocket ds = new DatagramSocket(20151);
		byte[] uploadBytes = "upload".getBytes();
		byte[] filenameBytes = filename.getBytes();
		byte[] uploadInfo = new byte[uploadBytes.length + filenameBytes.length];
		System.arraycopy(uploadBytes, 0, uploadInfo, 0, uploadBytes.length);
		System.arraycopy(filenameBytes, 0, uploadInfo, uploadBytes.length, filenameBytes.length);
		DatagramPacket upload = new DatagramPacket(uploadInfo, uploadInfo.length, serverIP, serverPort);
		ds.send(upload);
		ds.close();
	}

	// Utility method to hash filename into server #
	static int hashFilename(String filename) {
		char[] letters = filename.toCharArray();
		int sum = 0;
		for (int i = 0; i < filename.length(); i++) sum += (int) letters[i];
		int result = (sum % 4) + 1;

		return result;
	}

	// A utility method to convert the byte array
	// data into a string representation.
	public static StringBuilder data(byte[] a) {
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}

}
