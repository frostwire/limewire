/*
 * SSRCGenerator.java
 *
 * Created on 27 juillet 2007, 23:36
 *
 */

package net.sf.fmj.media.rtp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.fmj.utility.FmjStartup;
import net.sf.fmj.utility.LoggerSingleton;

/**
 * This class was written to generate different kinds of pseudo-random numbers,
 * especially Synchronization SouRCes numbers (SSRC). 
 * 
 * An SSRC is a 32-bit numeric SSRC identifier carried in the RTP header so as not 
 * to be dependent upon the network address, and identifies the source of a stream 
 * of RTP packets. All packets from a synchronization source form part of the same
 * timing and sequence number space.
 * 
 * The RFC requests those numbers to be random enough to avoid synchronization of
 * the PRNGs and a consequent collision of SSRCs. To avoid this problem, we generate
 * the SSRC with an MD5 hash of information about the local participant.
 * 
 * This class is also used as a normal pseudo-random number generator (PRNG) and
 * uses SecureRandom to provide secure seeds to generate good series of random
 * numbers.
 * @author Christian Vincenot
 */

public class SSRCGenerator {
        
		private static final Logger logger = LoggerSingleton.logger;

        // Secure PRNG
        static private SecureRandom securePRNG = new SecureRandom();
    
        // Start Time to be used to compute a seed
        static private long startTime = System.currentTimeMillis();
        
        StringBuffer meat;         
        private MessageDigest md5 = null;
        private Random PRNG = null;
        static private int counter=0;
        static private Object counterLock = new Object();     
        
    /**
     * Creates an instance of SSRC Generator.
     */
        public SSRCGenerator()
        {
            String user = "username";
            if ( !FmjStartup.isApplet )
            {
                user = System.getProperty("user.name");
            }
            
            int objHash = System.identityHashCode(this);
            
            String host= "localhost";
            if ( !FmjStartup.isApplet )
            {
                try
                {
                    host = InetAddress.getLocalHost().getHostAddress();
                }
                catch (UnknownHostException ex)
                {
                    logger.log(Level.WARNING, "" + ex, ex);
                }
            }
             
            try
            {
                this.md5 = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e)
            {
                logger.log(Level.WARNING, "" + e, e);
            }
             
            //System.out.println("ObjHash : "+objHash);
             
            /* Create the buffer containing the first part of the data 
             * to be hashed */
            this.meat = new StringBuffer();
            meat.append(user);
            meat.append("@");
            meat.append(host);
            meat.append(":");
            meat.append(Integer.toString(objHash));
            meat.append(":");
        }
        
    /**
     * Generates a random integer using Math.random() to get a fast generation,
     * but SecureRandom to give a good seed to Math.random() and garanties a good
     * randomness.
     * @return a random integer
     */
        public int generate() {
            int result;
            
            /* We synchronize that part to have an exclusive access to the
             * counter and avoid passing the same value to generate().
             */
            synchronized(counterLock) {
                counter++;
                result = generate(counter);
            }
            return result;
        }
        
       
    /**
     * Generates a random integer using Math.random() to get a fast generation,
     * but SecureRandom to give a good seed to Math.random() and garanties a good
     * randomness.
     * 
     * Use this version when generating close successions of number.
     * @param salt The "salt" argument is used if we have to generate several SSRCs in
     * a very close succession (the hashs could be the same depending since
     * the standard Random PRNG is time-based). This situation is very
     * unlikely to happen.
     * @return a random integer
     */
        public int generate(int salt) {
             long millis = System.currentTimeMillis();
             long rand;
             int pos = meat.length();
             byte[] hashed;
             int ssrc=0;
             
             /* We use secureRandom here to provide a seed to Random(), but 
              * since SecureRandom is known to be very slow and considering 
              * that we don't care about predictability to generate the SSRC, 
              * we could use the normal Random PRNG with a seed provided by
              * the "uptime" of the object (+type to add some "enthropy" when
              * the method is called successively). If SecureRandom is too slow, 
              * uncomment the following line.
              */
              
             if (PRNG == null) 
                 PRNG = new Random(getSecuredSeed());
                 //PRNG = new Random(millis-start+type);
 
             rand = PRNG.nextLong();
             //System.out.println("RAND "+rand);
             meat.append(Long.toString(millis));
             meat.append(":");
             meat.append(Long.toString(rand));
             meat.append(":");
             meat.append(Integer.toString(salt));
             
             md5.update(meat.toString().getBytes());
             
             hashed = md5.digest();
             
             //System.out.println("MEAT : "+meat+ "\nHASH :"+new String(hashed));
             
             /* We reset the meat which should only contain the common part to
              * all hashes (user, host, objHash) when the method returns.
              */
             meat.delete(pos,meat.length());
             
             
             /* To generate a 32-bit integer with the 128 bits hash, we xor
              * the 4 integers composing the hash.
              */
             for (int i=0; i+3<hashed.length; i+=4) {
                ssrc ^= hashed[i]   << 24
                      | hashed[i+1] << 16
                      | hashed[i+2] << 8 
                      | hashed[i+3];
             }
             
             //System.out.println("SSRC "+ssrc);
             return ssrc;           
        }
          
    
    /**
     * Generates a secured seed that can be used to initialize a PRNG (like Math.random).
     * @return a good seed
     */
        static public long getSecuredSeed() {
            byte[] gen = securePRNG.generateSeed(8);
            long genl = 0;
            int shift = 56;
          
            for (int i=0; i < 8; i++) {
                genl |= (long)gen[i] << shift;
                shift -= 8;
            }
       
            return genl;
        }
    
    /**
     * Generates a secured seed that can be used to initialize a PRNG (like Math.random).
     * This version returns a seed generated on a space of n bytes.
     * @param n number of bytes on which to generate the seed
     * @return a good seed generated on a space of n bytes
     */
        static public byte[] getSecuredSeed(int n) {
            return SecureRandom.getSeed(n);
        }
        
    /**
     * Returns a long generated in a very secure way.
     * @return a long generated in a very secure way
     */
        static public long nextSecuredRandomLong() {
            return securePRNG.nextLong();
        }
        
        
    /**
     * Returns an integer generated in a very secure way.
     * @return an integer generated in a very secure way
     */
        static public int nextSecuredRandomInt() {
            return securePRNG.nextInt();
        }
        
    /**
     * Returns a short integer generated in a very secure way.
     * @return a short integer generated in a very secure way
     */
        static public short nextSecuredRandomShort() {
            return (short)(securePRNG.nextInt() & 0xffff);
        }
        
        
        /*
        public double random(int n) throws IllegalArgumentException {
            if (n < 1 | n>8) throw new IllegalArgumentException("Invalid Random Space (should be between 1 and 8 bits)");
            
            byte gen[] = new byte[n];
            
            //securePRNG.nextBytes(gen);
            if (PRNG == null) 
                 PRNG = new Random(getSecuredSeed());
            
            
            PRNG.nextBytes(gen);
            
            long genl = 0;
            int shift = 8*(n-1);
            
            for (int i=0; i < n; i++) {
                genl |= (long)gen[i] << shift;
                shift -= 8;
            }
            */
            /* IEEE-754: To get a positive value, we ignore the sign bit,
             * and to avoid getting an NaN or Infinite value, we
             * ignore the lower bit of the exponent (because 0x7ff
             * as exponent is the common part for all those special cases)
             */
            /* genl = genl & 0x7fefffffffffffffL;
            
            return Double.longBitsToDouble(genl);
            
        }*/
        
    /**
     * Generates a random double between 0 and 1.
     * @return a random double between 0 and 1
     */
        public double random() {
            if (PRNG == null) 
                 PRNG = new Random(getSecuredSeed());
            
            return PRNG.nextDouble();
        }
        
        

}
