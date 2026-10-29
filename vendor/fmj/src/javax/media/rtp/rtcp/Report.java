package javax.media.rtp.rtcp;

import java.util.Vector;

import javax.media.rtp.Participant;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/rtcp/Report.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface Report
{
	public Participant getParticipant();

	public long getSSRC();

	public Vector getFeedbackReports();

	public Vector getSourceDescription();
}
