package javax.media.rtp;

import java.io.IOException;

import javax.media.rtp.rtcp.SourceDescription;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/SendStream.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * 
 * @author Ken Larson
 * 
 */
public interface SendStream extends RTPStream
{

	public void setSourceDescription(SourceDescription[] sourceDesc);

	public void close();

	public void stop() throws IOException;

	public void start() throws IOException;

	public int setBitRate(int bitRate);

	public TransmissionStats getSourceTransmissionStats();
}
