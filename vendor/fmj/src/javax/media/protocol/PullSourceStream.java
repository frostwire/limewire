package javax.media.protocol;

import java.io.IOException;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/protocol/PullSourceStream.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface PullSourceStream extends SourceStream
{
	public boolean willReadBlock();

	public int read(byte[] buffer, int offset, int length) throws IOException;
}
