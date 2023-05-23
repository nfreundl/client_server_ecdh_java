package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECPoint;

import javax.print.event.PrintEvent;


class Server extends Communicator {

  
  public Server() throws NoSuchAlgorithmException{

  }

  public void receiveKey() throws Exception{

    ServerSocket serverSocket = new ServerSocket(5000, 1, InetAddress.getByName("127.0.0.1"));
    Socket socket = serverSocket.accept();
    InputStream inputStream = socket.getInputStream();
    byte[] allBytesRead = inputStream.readAllBytes();
    ECPoint ecPoint = curves.readPointToString(new String(allBytesRead));
    socket.close();
    serverSocket.close();
    this.ComputeSharedSecret(ecPoint);

  }

  public static void main(String[]args) throws Exception{


    while (true) {
      Server server =new Server();

      server.receiveKey();
      
    }

  }
  
}