package javax.media;

import java.io.IOException;

import javax.media.protocol.DataSource;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/MediaHandler.html" target="_blank">this class in the JMF Javadoc</a>.
 * 
 * Complete.
 * @author Ken Larson
 *
 */
public interface MediaHandler
{
	public void setSource(DataSource source) throws IOException, IncompatibleSourceException;
}
