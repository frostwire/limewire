package javax.media.rtp.rtcp;

import javax.media.rtp.RTPStream;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/rtcp/SenderReport.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * 
 * @author Ken Larson
 * 
 */
public interface SenderReport extends Report
{

	public RTPStream getStream();

	public long getSenderPacketCount();

	public long getSenderByteCount();

	public long getNTPTimeStampMSW();

	public long getNTPTimeStampLSW();

	public long getRTPTimeStamp();

	public Feedback getSenderFeedback();
}
