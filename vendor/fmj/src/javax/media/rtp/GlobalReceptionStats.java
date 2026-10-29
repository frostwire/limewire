package javax.media.rtp;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/GlobalReceptionStats.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface GlobalReceptionStats
{
	public int getPacketsRecd();
	public int getBytesRecd();
	public int getBadRTPkts();
	public int getLocalColls();
	public int getRemoteColls();
	public int getPacketsLooped();
	public int getTransmitFailed();
	public int getRTCPRecd();
	public int getSRRecd();
	public int getBadRTCPPkts();
	public int getUnknownTypes();
	public int getMalformedRR();
	public int getMalformedSDES();
	public int getMalformedBye();
	public int getMalformedSR();
}
