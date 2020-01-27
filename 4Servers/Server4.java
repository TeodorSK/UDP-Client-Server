// Java program to illustrate Server side
// Implementation using DatagramSocket
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

public class Server4
{
    public static DatagramSocket ds;

    public static byte[] receive;
    public static InetAddress clientIP;
    public static int clientPort;

    public static DatagramPacket DpReceive;
    public static DatagramPacket response;

    static HashMap<String, InetAddress> localTable;
    public static void main(String[] args) throws IOException
    {
    		localTable = new HashMap();
        ds = new DatagramSocket(20155);

        receive = new byte[65535];

        DpReceive = null;
        response = null;

    		InetAddress someUserIP = InetAddress.getLocalHost();
    		localTable.put("bunny", someUserIP);

        serverListen();
        clientListen();
    }

    static void serverListen() throws IOException, SocketException{
      ServerSocket welcomeSocket = new ServerSocket(20155);
      while (true) {
        //Receive forwarded init
        Socket connectionSocket = welcomeSocket.accept();
        InputStream in = connectionSocket.getInputStream();
        DataInputStream dataStream = new DataInputStream(in);
        int length = dataStream.readInt();
        byte[] dataBytes = new byte[length];
        dataStream.readFully(dataBytes, 0, length);

        if(data(dataBytes).length()>4 &&
          data(dataBytes).toString().substring(0, 4).equals("init")){
          //respond to user
          clientIP = InetAddress.getByAddress(Arrays.copyOfRange(dataBytes, 4, 8));
          clientPort = Integer.parseInt(data(Arrays.copyOfRange(dataBytes, 8, dataBytes.length)).toString());

          byte[] myIP = InetAddress.getLocalHost().getAddress();
          byte[] myPort = Integer.toString(20155).getBytes();
          byte[] myInfo = new byte[myIP.length + myPort.length];
          System.arraycopy(myIP, 0, myInfo, 0, myIP.length);
          System.arraycopy(myPort, 0, myInfo, myIP.length, myPort.length);
          response = new DatagramPacket(myInfo, myInfo.length, clientIP, clientPort);
          ds.send(response);


          //No connection from server 4 to 1
          // Socket clientSocket = new Socket(InetAddress.getLocalHost(), 20156);
          // DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
          // outToServer.write(dataBytes, 0, dataBytes.length);
        }


      }
    }

    static void clientListen() throws IOException, SocketException{

      ds = new DatagramSocket(20155);

      receive = new byte[65535];


      DpReceive = null;
      response = null;
      while (true)
      {
          DpReceive = new DatagramPacket(receive, receive.length);

          ds.receive(DpReceive);
          clientPort = DpReceive.getPort();
          clientIP = DpReceive.getAddress();

          System.out.println("Client message:- " + data(receive) + " Client Port:-" + clientPort);

          //Exit the server if the client sends "bye"
          if (data(receive).toString().equals("exit")) {
              //Tell other servers to exit over TCP

              //Exit myself

              break;
          }
          //return all server IPs to user
          else  if (data(receive).length()>4 &&
                    data(receive).toString().substring(0, 4).equals("init")) {
              //Unreachable code. Init is received from previous server over TCP
          }
          //Uploading new image into directory
          else if (data(receive).length()>6 &&
                   data(receive).toString().substring(0, 6).equals("upload")) {
              String filename = data(Arrays.copyOfRange(receive, 6, receive.length)).toString();
              System.out.println("Uploading file " + filename);
              localTable.put(filename, clientIP);
              System.out.println("Local table is now \n" + localTable);
          }
          //Retrieving image and sending IP of user who has that image to the Client
          else if (localTable.containsKey(data(receive).toString())) {
              System.out.println("Fetching image " + data(receive).toString());
              byte[] requestIP = localTable.get(data(receive).toString()).getAddress();
              response = new DatagramPacket(requestIP, requestIP.length, clientIP, clientPort);
              ds.send(response);
          }
          else {
              System.out.println("Sorry, unknown command");
          }
          // Clear the buffer after every message.
          receive = new byte[65535];
      }
    }
    // A utility method to convert the byte array data into a string representation.
    public static StringBuilder data(byte[] a){
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
