package javax.media.rtp;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/ReceptionStats.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface ReceptionStats
{
	public int getPDUlost();
	public int getPDUProcessed();
	public int getPDUMisOrd();
	public int getPDUInvalid();
	public int getPDUDuplicate();
	
}
