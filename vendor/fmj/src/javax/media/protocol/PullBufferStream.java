package javax.media.protocol;

import java.io.IOException;

import javax.media.Buffer;
import javax.media.Format;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/protocol/PullBufferStream.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface PullBufferStream extends SourceStream
{

	public boolean willReadBlock();
	
	public void read(Buffer buffer) throws IOException;

	public Format getFormat();
}
