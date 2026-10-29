package javax.media.datasink;

import javax.media.DataSink;
import javax.media.MediaEvent;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/datasink/DataSinkEvent.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public class DataSinkEvent extends MediaEvent
{
	private String message;
	
	public DataSinkEvent(DataSink from)
	{	super(from);
		this.message = "";
	}
	
	public DataSinkEvent(DataSink from, String reason)
	{	super(from);
		this.message = reason;
	}
	
	public DataSink getSourceDataSink()
	{	return (DataSink) getSource();
	}
	
	public String toString()
	{	return DataSinkEvent.class.getName() + "[source=" + getSource() + "] message: " + message;
	}
}
