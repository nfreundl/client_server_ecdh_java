import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;
import java.util.Base64.Encoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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

  private SecretKey setKey() throws InvalidKeySpecException, NoSuchAlgorithmException{
    //  https://stackoverflow.com/questions/9536827/generate-key-from-string
    // PBE stands for password-based encryption
    SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
    KeySpec keySpec = new PBEKeySpec(this.key.toCharArray());
    SecretKey secretKey = factory.generateSecret(keySpec);
    return secretKey;
  }
  private byte[] EncryptMessage(String message) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
    SecretKey secretKey = this.setKey();
    cipher.init(Cipher.ENCRYPT_MODE,secretKey);
    byte[] encrypted = cipher.doFinal(message.getBytes());
    System.out.println(Base64.getEncoder().encodeToString(encrypted));
    return encrypted;
  }

  private String DecryptMessage( byte[] message) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException{
    SecretKey secretKey = this.setKey();
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    
    byte[] decrypted = cipher.doFinal(message);
    System.out.println(Base64.getEncoder().encodeToString(message));
    return decrypted.toString();
    
  }


  private String SendPublicKey() throws IOException{
    ECPoint publicPoint = this.ComputePublic();
    byte[] buffer = curves.serializePointToString(publicPoint).getBytes();
    Socket socket = new Socket("127.0.0.1",5000);
    OutputStream outputStream=socket.getOutputStream();
    outputStream.write(buffer);

    InputStream inputStream = socket.getInputStream();

    buffer = inputStream.readAllBytes();
    socket.close();
    return new String(buffer);
  }

  private String ExchangeKeyWithTimeOut() throws Exception {
    ECPoint publicPoint = this.ComputePublic();
    byte[] buffer = curves.serializePointToString(publicPoint).getBytes();
    
    //https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread
    
    // ooo this is how you make an anonymous class
    
    Callable<String> task = new Callable<String>() {

      @Override
      public String call() throws Exception {
        
        Socket socket = new Socket("127.0.0.1",5000);
        OutputStream outputStream=socket.getOutputStream();
        outputStream.write(buffer);

        InputStream inputStream = socket.getInputStream();

        byte[] inputBuffer = inputStream.readAllBytes();
        socket.close();
        return new String(inputBuffer);
      }
      
    };
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<String> result = executor.submit(task);
    String toReturn = new String();
    try {
      toReturn = result.get(5, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return "";
    }catch (Exception e){
      throw e;
    }

    return toReturn;

  }



  public static void main(String[]args) throws Exception{
    Client main = new Client();

    main.ExchangeKeyWithTimeOut();
    
  }
}
