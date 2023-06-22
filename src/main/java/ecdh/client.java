package ecdh;

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

class Client extends Communicator {

  public Client() throws NoSuchAlgorithmException {

  }

  private String SendPublicKey() throws IOException {
    ECPoint publicPoint = this.ComputePublic();
    byte[] buffer = curves.serializePointToString(publicPoint).getBytes();
    Socket socket = new Socket("127.0.0.1", 5000);
    OutputStream outputStream = socket.getOutputStream();
    outputStream.write(buffer);

    InputStream inputStream = socket.getInputStream();

    buffer = inputStream.readAllBytes();
    socket.close();
    return new String(buffer);
  }

  private void SendMessage(byte[] cipher) throws Exception {
    Callable<Void> callable = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        Socket socket;
        while (true) {
          try {
            socket = new Socket("127.0.0.1", 5000);
          } catch (IOException e) {
            continue;
          }
          break;
        }
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(cipher);
        socket.close();
        return null;
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Void>  ret = executor.submit(callable);
    try{
      ret.get(5, TimeUnit.SECONDS);

    } catch (TimeoutException e){
      e.printStackTrace();
      return;
    } catch (Exception e){
      throw e;
    }

  }

  private String ExchangeKeyWithTimeOut() throws Exception {
    System.out.println("computing public key");
    ECPoint publicPoint = this.ComputePublic();
    System.out.println("public key computed");
    byte[] buffer = curves.serializePointToString(publicPoint).getBytes();

    // https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

    // ooo this is how you make an anonymous class

    Callable<String> task = new Callable<String>() {

      @Override
      public String call() throws Exception {

        Socket socket = new Socket("127.0.0.1", 5000);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(buffer);
        socket.close();

        while (true) {
          try {
            socket = new Socket("127.0.0.1", 5000);
          } catch (IOException e) {
            continue;
          }
          break;
        }
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
    } catch (Exception e) {
      throw e;
    }

    return toReturn;

  }

  public static void main(String[] args) throws Exception {

    Client main = new Client();

    main.ExchangeKeyWithTimeOut();

    byte[] cipher = main.EncryptMessage("ping");
    main.SendMessage(cipher);

  }
}
