import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import java.util.Scanner;

public class P2PServer // extends Thread
{
	// public void run() {}
	public static void main(String argv[]) throws Exception {
		String path = "/Users/tsk/Documents/School/3rdyear/CPS706/src/imgsrc/";
		int portNum = 20151;
		String filename;
		BufferedImage image;
		byte[] imgBytes;
		byte[] imgSize;

		// Create Welcoming socket at port 20151
		ServerSocket welcomeSocket = new ServerSocket(portNum);

		while (true) {
			// create new thread here probably
			// wait, on welcoming socket for contact by client
			Socket connectionSocket = welcomeSocket.accept();

			// create input stream, attached to socket
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			// create output stream, attached to socket
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			// read in name of file from client
			filename = inFromClient.readLine();
			System.out.println(filename);

			// read the file given the file name
			image = ImageIO.read(new File(path + filename));

			// convert the image to a byte array
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", byteArrayOutputStream);
			imgSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
			imgBytes = byteArrayOutputStream.toByteArray();

			// write out image and image size as byte arrays to client
			outToClient.write(imgSize);
			outToClient.write(imgBytes);
			outToClient.flush();
		} // loop ends, loop back wait for another connection
	}

	// Utility method to hash filename into server #
	static int hashFilename(String filename) {
		char[] letters = filename.toCharArray();
		int sum = 0;
		for (int i = 0; i < filename.length(); i++) sum += (int) letters[i];
		int result = (sum % 4) + 1;

		return result;
	}
}
