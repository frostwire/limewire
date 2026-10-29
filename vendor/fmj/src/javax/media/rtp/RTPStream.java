package javax.media.rtp;

import javax.media.protocol.DataSource;
import javax.media.rtp.rtcp.SenderReport;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/RTPStream.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface RTPStream
{

	public Participant getParticipant();
	public SenderReport getSenderReport();
	public long getSSRC();
	public DataSource getDataSource();
	
}
