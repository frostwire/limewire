package javax.media;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/Clock.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * 
 * @author Ken Larson
 * 
 */
public interface Clock
{

	public static final Time RESET = new Time(Long.MAX_VALUE);

	public void setTimeBase(TimeBase master) throws IncompatibleTimeBaseException;

	public void syncStart(Time at);

	public void stop();

	public void setStopTime(Time stopTime);

	public Time getStopTime();

	public void setMediaTime(Time now);

	public Time getMediaTime();

	public long getMediaNanoseconds();

	public Time getSyncTime();

	public TimeBase getTimeBase();

	public Time mapToTimeBase(Time t) throws ClockStoppedException;

	public float getRate();

	public float setRate(float factor);
}
