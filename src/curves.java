import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.spec.*;



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
  
  private  ECFieldFp  ecFieldFp ;

  private ECPoint gen;
  // for info, this is power in the sense of group theory, in other context you could call that a scalar multiplication
  private  ECPoint power(BigInteger i){
    ECPoint point = this.gen;
    
    // do double-and-add, this will save some (a lot of ?) time
    for (BigInteger j = BigInteger.ZERO; j.compareTo(i) == -1; j = j.add(BigInteger.ONE)) {
      point =curves.addTwoPoints(point, this.gen);
    }
    return point;
  }

  // https://en.wikipedia.org/wiki/Elliptic_curve_point_multiplication
  private static ECPoint addTwoPoints(ECPoint a, ECPoint b){
    
    if (a.getAffineX().mod(curves.p) == b.getAffineX().mod(curves.p)){
      if(a == ECPoint.POINT_INFINITY){
        return b;
      }

      if (b == ECPoint.POINT_INFINITY){
        return a;
      }

      if(a.getAffineY().mod(curves.p) == b.getAffineY().mod(curves.p)){
        return ECPoint.POINT_INFINITY;
      }
      else{
        BigInteger dividendum = ((a.getAffineX().modPow(curves.p, BigInteger.TWO)).multiply(new BigInteger("3",16)).add(curves.a)).mod(curves.p);
        BigInteger divisor = (a.getAffineY().multiply(BigInteger.TWO)).mod(curves.p);
        BigInteger lambda = dividendum.multiply(divisor.modInverse(curves.p));
        
        BigInteger cx = lambda.modPow(curves.p, BigInteger.TWO).subtract(a.getAffineX().multiply(BigInteger.TWO));
        BigInteger cy =  a.getAffineY().subtract(lambda.multiply(cx.subtract(a.getAffineX())));

        return new ECPoint(cx.mod(curves.p), cy.mod(curves.p));
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
    return this.power(remainder);
  }
/*
  public ECPoint scalarMultiplication(){

  }
*/
}

