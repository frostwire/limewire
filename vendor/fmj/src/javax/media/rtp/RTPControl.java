package javax.media.rtp;

import javax.media.Control;
import javax.media.Format;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/RTPControl.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface RTPControl extends Control
{

	public void addFormat(Format fmt, int payload);
	public ReceptionStats getReceptionStats();
	public GlobalReceptionStats getGlobalStats();
	public Format getFormat();
	public Format[] getFormatList();
	public Format getFormat(int payload);
}
