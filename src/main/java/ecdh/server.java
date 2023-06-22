package ecdh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECPoint;

import javax.print.event.PrintEvent;

class Server extends Communicator {

  public Server() throws NoSuchAlgorithmException {

  }

  public void receiveKey() throws Exception {

    ServerSocket serverSocket = new ServerSocket(5000, 1, InetAddress.getByName("127.0.0.1"));
    Socket socket = serverSocket.accept();
    InputStream inputStream = socket.getInputStream();
    byte[] allBytesRead = inputStream.readAllBytes();

    socket.close();
    serverSocket.close();
    ECPoint ecPoint = curves.readPointToString(new String(allBytesRead));

    this.ComputeSharedSecret(ecPoint);

  }

  private byte[] ReceiveMessage() throws UnknownHostException, IOException{
    ServerSocket serverSocket = new ServerSocket(5000, 1, InetAddress.getByName("127.0.0.1"));
    Socket socket = serverSocket.accept();
    InputStream inputStream = socket.getInputStream();
    byte[] allBytesRead = inputStream.readAllBytes();

    socket.close();
    serverSocket.close();
    return allBytesRead;
  }

  public static void main(String[] args) throws Exception {

    while (true) {
      Server server = new Server();

      server.receiveKey();
      ECPoint ecpoint = server.ComputePublic();
      byte[] buffer = curves.serializePointToString(ecpoint).getBytes();

      Runnable runnable = new Runnable() {

        @Override
        public void run() {

          try {
            ServerSocket serverSocket = new ServerSocket(5000, 1, InetAddress.getByName("127.0.0.1"));

            Socket socket = serverSocket.accept();

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(buffer);
            outputStream.close();

            socket.close();
            serverSocket.close();

            byte[] cipher = server.ReceiveMessage();
            String message =server.DecryptMessage(cipher);
            System.out.printf("received message: %s \n", message);

          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };

      Thread thread = new Thread(runnable);
      thread.start();
    }

  }

}