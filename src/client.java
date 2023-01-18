import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.spec.ECPoint;
import java.util.Random;

import javax.lang.model.type.ErrorType;



class Client {
  
  private BigInteger randomSecret;
  private curves curve = new curves();
  private ECPoint sharedSecret;


  public Client(){
    this.randomSecret = new BigInteger(curves.size(), new Random(0));
    this.randomSecret = this.randomSecret.mod(curves.n());

  }

  public ECPoint ComputePublic(){
    return this.curve.GetPoint(this.randomSecret);
  }

  public ECPoint ComputeSharedSecret() throws Exception{
    throw new UnsupportedOperationException("To implement");
  }


  public static void main(String[]args) throws IOException{
    
    
    
    //InetSocketAddress address = new InetSocketAddress("127.0.0.1", 5000);
    Socket socket = new Socket("127.0.0.1",5000);
    //socket.bind(address);
    //socket.connect(address);
    OutputStream x=socket.getOutputStream();
    byte[] buffer ="ping".getBytes();
    System.out.println("send data");
    x.write(buffer);
    System.out.println(new String(buffer));

    InputStream y = socket.getInputStream();
    
    buffer = new byte[12];
    System.out.println("read data");
    y.read(buffer);
    System.out.println(new String(buffer));
    socket.close();
    
  }
}
