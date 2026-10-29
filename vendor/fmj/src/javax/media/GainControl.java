package javax.media;


/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/GainControl.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface GainControl extends Control
{
	public void setMute(boolean mute);
	public boolean getMute();
	public float setDB(float gain);
	public float getDB();
	public float setLevel(float level);
	public float getLevel();
	public void addGainChangeListener(GainChangeListener listener);
	public void removeGainChangeListener(GainChangeListener listener);
}
