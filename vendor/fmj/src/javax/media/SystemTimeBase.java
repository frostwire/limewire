package javax.media;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/SystemTimeBase.html" target="_blank">this class in the JMF Javadoc</a>.
 * 
 * Complete.
 * @author Ken Larson
 *
 */
public final class SystemTimeBase implements TimeBase
{
	private static long milliTimestamp = 0; 
	private static long nanoTimestamp = 0; 
	
	public long getNanoseconds()
	{
		// modehardt: System.currentTimeMillis has on Win9x a resolution of ~54ms, WinNT 10ms, WinXP 15ms, linux and Max 1ms
		// with nanoTime it has at least a resolution of 1ms on all systems
		
		// mgodehardt nanoTime is relative to currentTimeMillis ( both are synced )
		if (milliTimestamp == 0)
		{
			milliTimestamp = System.currentTimeMillis() * 1000000L;
			nanoTimestamp = System.nanoTime();
		}
		
		return (System.nanoTime() - nanoTimestamp) + milliTimestamp;
	}

	public Time getTime()
	{
		return new Time(getNanoseconds());
	}

}
