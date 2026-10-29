package javax.media.rtp;

import java.util.Vector;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/Participant.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface Participant
{
	public Vector getStreams();
	public Vector getReports();
	public String getCNAME();
	public Vector getSourceDescription();
}
