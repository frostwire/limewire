package javax.media.rtp;

import java.io.IOException;

import javax.media.protocol.PushSourceStream;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/RTPConnector.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface RTPConnector
{
	public PushSourceStream getDataInputStream() throws IOException;

	public OutputDataStream getDataOutputStream() throws IOException;

	public PushSourceStream getControlInputStream() throws IOException;

	public OutputDataStream getControlOutputStream() throws IOException;

	public void close();

	public void setReceiveBufferSize(int size) throws IOException;

	public int getReceiveBufferSize();

	public void setSendBufferSize(int size) throws IOException;

	public int getSendBufferSize();

	public double getRTCPBandwidthFraction();

	public double getRTCPSenderBandwidthFraction();

}
