package javax.media;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/Controller.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface Controller extends Clock, Duration
{
	public static final Time LATENCY_UNKNOWN = new Time(Long.MAX_VALUE);

	public static final int Prefetched = 500;

	public static final int Prefetching = 400;

	public static final int Realized = 300;

	public static final int Realizing = 200;

	public static final int Started = 600;

	public static final int Unrealized = 100;

	public int getState();
	public int getTargetState();
	public void realize();
	public void prefetch();
	public void deallocate();
	public void close();
	public Time getStartLatency();
	public Control[] getControls();
	public Control getControl(String forName);
	public void addControllerListener(ControllerListener listener);
	public void removeControllerListener(ControllerListener listener);
}
