package javax.media;

/**
 * Standard JMF class -- see <a href="http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/ExtendedCachingControl.html" target="_blank">this class in the JMF Javadoc</a>.
 * Complete.
 * @author Ken Larson
 *
 */
public interface ExtendedCachingControl extends CachingControl
{
	public void setBufferSize(Time t);
	public Time getBufferSize();
	public void pauseDownload();
	public void resumeDownload();
	public long getStartOffset();
	public long getEndOffset();
	public void addDownloadProgressListener(DownloadProgressListener l, int numKiloBytes);
	public void removeDownloadProgressListener(DownloadProgressListener l);
	
}
