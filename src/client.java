import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.spec.ECPoint;
import java.util.Base64;
import java.util.Random;
import java.util.Base64.Encoder;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.lang.model.type.ErrorType;



class Client {
  
  private BigInteger randomSecret;
  private curves curve = new curves();
  private ECPoint sharedSecret;
  private String key;
  private MessageDigest messageDigest;
  private Cipher cipher;


  public Client() throws NoSuchAlgorithmException{
    this.randomSecret = new BigInteger(curves.size(), new Random(0));
    this.randomSecret = this.randomSecret.mod(curves.n());
    this.messageDigest = MessageDigest.getInstance("SHA-256");
  }

  public ECPoint ComputePublic(){
    return this.curve.GetPoint(this.randomSecret);
  }

  private  void ComputeSharedSecret(ECPoint pointFromServer) throws Exception{
    this.sharedSecret = this.curve.power(pointFromServer, randomSecret);
  }

  private void DeriveKey(){
    String sharedSecretString =this.sharedSecret.toString();
    this.messageDigest.update(Byte.parseByte(sharedSecretString));
    Encoder encoder =Base64.getEncoder();
    this.key =encoder.encodeToString(this.messageDigest.digest());

  }

  private void setCipher() throws NoSuchAlgorithmException, NoSuchPaddingException{
    cipher = Cipher.getInstance("AES/CBC");
  }

  private void setKey(){
    //  https://stackoverflow.com/questions/9536827/generate-key-from-string
  }
  private String EncryptMessage(String message){
    
    cipher.init(Cipher.ENCRYPT_MODE,n);
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
