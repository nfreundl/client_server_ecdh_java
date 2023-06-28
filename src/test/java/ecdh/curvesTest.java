
package ecdh;
import java.math.BigInteger;
import java.security.spec.ECPoint;

import org.junit.Assert;
import org.junit.Test;





public class curvesTest {
  
  @Test
  public  void ComparePower(){

    curves curve =new curves();
    ECPoint generator =curve.getGenerator();

    ECPoint res1 = curve.power(generator,new BigInteger("100"));
    ECPoint res2 = curve.power2(generator,new BigInteger("100"));

    boolean isEqual = res1.equals(res2);


    Assert.assertEquals(true,isEqual);
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
