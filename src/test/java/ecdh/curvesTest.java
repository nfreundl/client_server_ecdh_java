
package ecdh;
import java.math.BigInteger;
import java.security.spec.ECPoint;

import org.junit.Assert;
import org.junit.Test;





public class curvesTest {

  void comparePowerHelper(BigInteger exponent){
    curves curve =new curves();
    ECPoint generator =curve.getGenerator();

    ECPoint res1 = curve.power(generator,exponent);
    ECPoint res2 = curve.power2(generator,exponent);

    boolean isEqual = res1.equals(res2);


    Assert.assertEquals(true,isEqual);
  }
  
  @Test
  public  void ComparePower1(){
    this.comparePowerHelper(BigInteger.ONE);
    
  }
  @Test
  public  void ComparePower2(){
    this.comparePowerHelper(BigInteger.TWO);
    
  }
  @Test
  public  void ComparePower3(){
    this.comparePowerHelper(new BigInteger("3"));
  }
  @Test
  public  void ComparePower4(){
    this.comparePowerHelper(new BigInteger("4"));
  }
  @Test
  public  void ComparePower10(){
    this.comparePowerHelper(BigInteger.TEN);
  }

  @Test
  public void WriteReadPoint(){
    curves curves = new curves();

    ECPoint gen = curves.getGenerator();

    String serialized = ecdh.curves.serializePointToString(gen);
    ECPoint parsed;
    try {
       parsed = ecdh.curves.readPointToString(serialized);
    } catch (Exception e) {
      
      e.printStackTrace();
      Assert.fail(e.getMessage());
      return; // else the next line causes an error
    }

    Assert.assertTrue(gen.equals(parsed));

  }
}
