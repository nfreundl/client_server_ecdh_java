package ecdh;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public abstract class Communicator {
  protected BigInteger randomSecret;
  protected curves curve = new curves();
  private ECPoint sharedSecret;
  private String key;
  private MessageDigest messageDigest;
  private Cipher cipher;

  public ECPoint ComputePublic() {
    return this.curve.GetPoint(this.randomSecret);
  }

  protected void ComputeSharedSecret(ECPoint pointFromServer) throws Exception {
    this.sharedSecret = this.curve.power2(pointFromServer, randomSecret);
  }

  private void DeriveKey() {
    String sharedSecretString = this.sharedSecret.toString();
    this.messageDigest.update(Byte.parseByte(sharedSecretString));
    Encoder encoder = Base64.getEncoder();
    this.key = encoder.encodeToString(this.messageDigest.digest());

  }

  public Communicator() throws NoSuchAlgorithmException {
    System.out.println("setting the random secret");
    this.randomSecret = new BigInteger(curves.size(), new Random());
    System.out.printf("random secret %s\n", randomSecret.toString(10));
    while (this.randomSecret.equals(BigInteger.ZERO) || this.randomSecret.signum() < 0
        || this.randomSecret.compareTo(curves.n()) >= 0) {

      this.randomSecret = new BigInteger(curves.size(), new Random());
      System.out.printf("random secret %s\n", randomSecret.toString(10));
    }
    System.out.println("random secret set");

    this.messageDigest = MessageDigest.getInstance("SHA-256");
  }

  private void setCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
    cipher = Cipher.getInstance("AES/CBC");
  }

  private SecretKey setKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
    // https://stackoverflow.com/questions/9536827/generate-key-from-string
    // PBE stands for password-based encryption
    SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
    KeySpec keySpec = new PBEKeySpec(this.key.toCharArray());
    SecretKey secretKey = factory.generateSecret(keySpec);
    return secretKey;
  }

  private byte[] EncryptMessage(String message) throws InvalidKeySpecException, NoSuchAlgorithmException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    SecretKey secretKey = this.setKey();
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] encrypted = cipher.doFinal(message.getBytes());
    System.out.println(Base64.getEncoder().encodeToString(encrypted));
    return encrypted;
  }

  private String DecryptMessage(byte[] message) throws InvalidKeyException, InvalidKeySpecException,
      NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
    SecretKey secretKey = this.setKey();
    cipher.init(Cipher.DECRYPT_MODE, secretKey);

    byte[] decrypted = cipher.doFinal(message);
    System.out.println(Base64.getEncoder().encodeToString(message));
    return decrypted.toString();

  }
}
