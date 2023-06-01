package src;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.spec.*;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



// https://neuromancer.sk/std/brainpool/brainpoolP256r1

public class curves {

  private static BigInteger p = new BigInteger("a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377",16);
  private static BigInteger a = new BigInteger("7d5a0975fc2c3057eef67530417affe7fb8055c126dc5c6ce94a4b44f330b5d9",16);
  private static BigInteger b = new BigInteger("26dc5c6ce94a4b44f330b5d9bbd77cbf958416295cf7e1ce6bccdc18ff8c07b6",16);
  private static BigInteger gx = new BigInteger("8bd2aeb9cb7e57cb2c4b482ffc81b7afb9de27e1e3bd23c23a4453bd9ace3262",16);
  private static BigInteger gy = new BigInteger("547ef835c3dac4fd97f8461a14611dc9c27745132ded8e545c1d54c72f046997",16);
  private static BigInteger n = new BigInteger("a9fb57dba1eea9bc3e660a909d838d718c397aa3b561a6f7901e0e82974856a7",16);
  private static int size = curves.n.bitLength(); 
  
  public static int size(){
    return curves.size;
  }

  public static BigInteger n(){
    return new BigInteger(curves.n.toByteArray());
  }

  public static String serializePointToString(ECPoint ecPoint){
    if (ecPoint.equals(ECPoint.POINT_INFINITY)){
      return "INFINITY";
    }else{
      return MessageFormat.format("AFFINEX{0}AFFINEY{1}", ecPoint.getAffineX().toString(16),ecPoint.getAffineY().toString(16));
    }

  }

  public static ECPoint readPointToString(String ecPoint) throws Exception{
    if (ecPoint.equals("INFINITY")){
      return ECPoint.POINT_INFINITY;
    }else{
      Pattern pattern = Pattern.compile("^AFFINEX(.*)AFFINEY(.*)$");
      Matcher matcher = pattern.matcher((String)ecPoint);
      if (matcher.find()){
        String xString = matcher.group(0);
        String yString = matcher.group(1);

        BigInteger x = new BigInteger(xString, 16);
        BigInteger y = new BigInteger(yString, 16);

        return new ECPoint(x, y);
        
      }else{
        throw new Exception("pattern not find");
      }

      
    }

  }
  
  
  private  ECFieldFp  ecFieldFp ;

  private ECPoint gen;
  
  public ECPoint getGenerator(){
    ECPoint newPoint = new ECPoint(this.gen.getAffineX(), this.gen.getAffineY());
    return newPoint;
  }

  private  ECPoint power(BigInteger i){

    
    return this.power(this.gen,i);
  }

  public ECPoint power(ECPoint point, BigInteger i){
    // for info, this is power in the sense of group theory, in other context you could call that a scalar multiplication
    if( point.equals(ECPoint.POINT_INFINITY)){
      return ECPoint.POINT_INFINITY;
    }
    ECPoint newPoint = new ECPoint(point.getAffineX(), point.getAffineY());
    for (BigInteger j = BigInteger.ZERO; j.compareTo(i) == -1; j = j.add(BigInteger.ONE)) {
      newPoint =curves.addTwoPoints(newPoint, this.gen);
    }
    return newPoint;
  }

  public ECPoint power2(ECPoint point, BigInteger exponent){
    ECPoint res = ECPoint.POINT_INFINITY;
    // big endian
    byte[] e = exponent.toByteArray();
    // or is it 128
    byte mask = (byte)-128;
    for (byte b : e) {
      for (int i = 0; i < 8; i++) {
        // double 
        res = addTwoPoints(res, res);
        
        // if the leftmost bit is set to  one
        if ((b & mask) == mask){
          //add
          res = addTwoPoints(res, point);
        };
        
      } 
    }

    return res;
  }

  // https://en.wikipedia.org/wiki/Elliptic_curve_point_multiplication
  private static ECPoint addTwoPoints(ECPoint a, ECPoint b){
    
    // == does not work, use .equals (maybe because it compares two different instances of a class, not caring about members' values)
    if(a.equals(ECPoint.POINT_INFINITY)){
      return b;
    }

    if (b.equals(ECPoint.POINT_INFINITY)){
      return a;
    }
    
    if (a.getAffineX().mod(curves.p).equals(b.getAffineX().mod(curves.p))){
      

      if(a.getAffineY().mod(curves.p).equals(b.getAffineY().mod(curves.p))){
        
        BigInteger dividendum = ((a.getAffineX().modPow(curves.p, BigInteger.TWO)).multiply(new BigInteger("3",16)).add(curves.a)).mod(curves.p);
        BigInteger divisor = (a.getAffineY().multiply(BigInteger.TWO)).mod(curves.p);
        BigInteger lambda = dividendum.multiply(divisor.modInverse(curves.p));
        
        BigInteger cx = lambda.modPow(curves.p, BigInteger.TWO).subtract(a.getAffineX().multiply(BigInteger.TWO));
        BigInteger cy =  a.getAffineY().subtract(lambda.multiply(cx.subtract(a.getAffineX())));

        return new ECPoint(cx.mod(curves.p), cy.mod(curves.p));
      }
      // if the xs  are the same, but not the ys, it means we have the inverse of the point (in the context of an additive group, we should call this opposite)
      else{
        return ECPoint.POINT_INFINITY;
      }
    }

    BigInteger ax = a.getAffineX();
    BigInteger ay = a.getAffineY();
    BigInteger bx = b.getAffineX();
    BigInteger by = b.getAffineY();

    BigInteger divisor = (bx.subtract(ax)).modInverse(curves.p);

    BigInteger lambda = (by.subtract(ay)).multiply(divisor);
    lambda = lambda.mod(curves.p);

    BigInteger cx = (lambda.modPow(curves.p,BigInteger.TWO).subtract(ax)).subtract(bx);
    cx = cx.mod(curves.p);

    BigInteger cy = (lambda.multiply(ax.subtract(cx))).subtract(ay);
    cy = cy.mod(curves.p);

    return new ECPoint(cx, cy);
  }

  private static boolean twoPointsEquality(ECPoint a, ECPoint b){
    // TODO I don't know if it is overloaded correctly
    return(a == b);
  }
  // modInverse already exists
/*
  private static BigInteger inverseModulo(BigInteger mod, BigInteger elm){
    if (mod.equals(BigInteger.ZERO)){
      throw  new ArithmeticException("Z/0Z doesn't contain any invertible element");
    }
    if (elm.equals(BigInteger.ZERO)){
      throw  new ArithmeticException("0 is not an invertible element");
    }

    BigInteger r0 = mod;
    BigInteger s0 = BigInteger.ONE;
    BigInteger t0 = BigInteger.ZERO;
    BigInteger r1 = elm;
    BigInteger s1 = BigInteger.ZERO;
    BigInteger t1 = BigInteger.ONE;

    while (r1.equals(BigInteger.ZERO)) {
      
    }
    if (!(r0.equals(BigInteger.ONE))){
      throw  new ArithmeticException("given elm is not an invertible element");
    }

    return t0;
  }
   */
  
  private EllipticCurve ellipticCurve;
  public curves(){
    this.ecFieldFp = new ECFieldFp(p);
    this.ellipticCurve = new EllipticCurve(ecFieldFp,a,b);
    this.gen = new ECPoint(gx, gy);
  }




  public ECPoint GetPoint(BigInteger i){
    BigInteger remainder = i.mod(curves.n);
    return this.power2(this.gen,remainder);
  }
/*
  public ECPoint scalarMultiplication(){

  }
*/
}

