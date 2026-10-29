package javax.media;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/Multiplexer.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface Multiplexer extends PlugIn
{
	public ContentDescriptor[] getSupportedOutputContentDescriptors(Format[] inputs);

	public Format[] getSupportedInputFormats();

	public int setNumTracks(int numTracks);

	public Format setInputFormat(Format format, int trackID);

	public int process(Buffer buffer, int trackID);

	public DataSource getDataOutput();

	public ContentDescriptor setContentDescriptor(ContentDescriptor outputContentDescriptor);
}
