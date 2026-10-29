package javax.media.rtp.event;

import javax.media.MediaEvent;
import javax.media.rtp.SessionManager;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/rtp/event/RTPEvent.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public class RTPEvent extends MediaEvent
{
	private SessionManager eventSrc;
	public RTPEvent(SessionManager from)
	{
		super(from);
		this.eventSrc = from;
	}
	
	
	public Object getSource()
	{	return eventSrc;
	}
	
	public SessionManager getSessionManager()
	{	return eventSrc;
	}
	
	public String toString()
	{	
		return getClass().getName() + "[source = " + eventSrc + "]";
	}
}
