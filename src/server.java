import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

import javax.print.event.PrintEvent;


class Server {

  public static void main(String[]args) throws IOException{
    

    ServerSocket server = new ServerSocket(5000);
    
    Socket socket = server.accept();
    

    InputStream x=socket.getInputStream();
    byte[] buffer = new byte[12];
    System.out.println("wait on data");
    x.read(buffer);
    System.out.println(new String(buffer));

    // response
    OutputStream y = socket.getOutputStream();
    System.out.println("send response");
    y.write("pong".getBytes());
    y.flush();

    socket.close();
    server.close();
    
  }
}