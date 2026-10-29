package javax.media;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/ControllerEvent.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public class ControllerEvent extends MediaEvent
{
	
	Controller eventSrc;
	
	public ControllerEvent(Controller from)
	{
		super(from);
		eventSrc = from;
	}
	
	public Controller getSourceController()
	{
		return eventSrc;
	}
	
	public Object getSource()
	{	return eventSrc;
	}
	
	public String toString()
	{
		return getClass().getName() + "[source=" + eventSrc + "]";
	}
}