package net.sf.fmj.media.protocol.rtp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Time;
import javax.media.format.VideoFormat;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;

import net.sf.fmj.media.datasink.rtp.RTPBonusFormatsMgr;
import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.SynchronizedBoolean;


/**
 * RTP protocol handler DataSource.  Very basic; makes the connection, gets the underlying DataSource, and delegates
 * all calls to the underlying DataSource.
 * TODO: support the full URL format:
 * "rtp://address:port[:ssrc]/content-type/[ttl]"
 * 
 *  address is an IP address, port is an integer port number, and content-type is a string such as video or audio. The SSRC (Synchronizing Source) and TTL (Time to Live) fields are optional. 
 *  
 *  TODO: the dimensions of the format (if video) are not known until a frame arrives.
 *  We probably need to wrap ((PushBufferDataSource) dataSource).getStreams(); to somehow
 *  get the first frame if it is video, and determine the format, and have our wrapper return the 
 *  full format when queried.
 *  
 * @author Ken Larson
 *
 */
public class DataSource extends PushBufferDataSource
{
    private static final Logger logger = LoggerSingleton.logger;

	private RTPManager rtpManager;
	private javax.media.protocol.DataSource dataSource;	// has to be a PushBufferDataSource actually.
	
	private static final int DATA_RECEIVED_TIMEOUT = 20 * 1000; 	// 20 seconds
	
	private PushBufferStream[] streams;
	
	@Override
	public void connect() throws IOException
	{
		URI uri;
		try {
			uri = new URI(getLocator().toExternalForm());
		}
		catch (URISyntaxException e) {
			throw new IOException("Malformed RTP URI: " + getLocator().toExternalForm());			
		}
		
		final String proto = uri.getScheme();
		if (!"rtp".equalsIgnoreCase(proto)) {
			throw new IOException("Invalid protocol: expected \"rtp\"");
		}
		
		final String addr = uri.getHost();
		final int port = uri.getPort();
		
		rtpManager = RTPManager.newInstance();
		RTPBonusFormatsMgr.addBonusFormats(rtpManager);
		rtpManager.addReceiveStreamListener(new MyReceiveStreamListener());

	    final SessionAddress localSessionAddress = new SessionAddress(InetAddress.getLocalHost(), port);
	    final SessionAddress remoteSessionAddress = new SessionAddress(InetAddress.getByName(addr), port);
		
	    try
		{
			rtpManager.initialize(localSessionAddress);
			rtpManager.addTarget(remoteSessionAddress);
		} catch (InvalidSessionAddressException e)
		{
			throw new IOException("" + e);
		}
		
		
		try
		{
			dataSourceSet.waitUntil(true, DATA_RECEIVED_TIMEOUT);
		} catch (InterruptedException e)
		{
			throw new InterruptedIOException("" + e);
		}
		
		if (!dataSourceSet.getValue())
		{	throw new IOException("Timeout");
		}
		
		if (!(dataSource instanceof PushBufferDataSource))
			throw new IOException("Expected dataSource to be instanceof PushBufferDataSource");
		
		dataSource.connect();
		logger.fine("Connected datasource");
	}
	
	private class MyReceiveStreamListener implements ReceiveStreamListener
	{

		public void update(ReceiveStreamEvent event)
		{
			if (event instanceof NewReceiveStreamEvent)
			{
				logger.fine("NewReceiveStreamEvent: " + event);
				final NewReceiveStreamEvent eCast = (NewReceiveStreamEvent) event;
				dataSource = eCast.getReceiveStream().getDataSource();
				final PushBufferStream[] originalStreams = ((PushBufferDataSource) dataSource).getStreams();
				streams = new PushBufferStream[originalStreams.length];
				for (int i = 0; i < streams.length; ++i)
				{
					// For video formats, look ahead to first buffer, and get video size.
					if (originalStreams[i].getFormat() instanceof VideoFormat)
					{	final LookAheadPushBufferStream s = new LookAheadPushBufferStream(originalStreams[i]);
						try
						{
							s.lookAheadAndFindFullFormat();
						} catch (InterruptedException e)
						{
							logger.log(Level.WARNING, "" + e, e);
							return;
						}
						streams[i] = s;
					}
					else
					{	streams[i] = originalStreams[i];
					}
				}
				dataSourceSet.setValue(true);
				

			}
		}
	}
	
	
	private final SynchronizedBoolean dataSourceSet = new SynchronizedBoolean();
	

	@Override
	public PushBufferStream[] getStreams()
	{
		return streams;
	}
	
	@Override
	public void disconnect()
	{
		dataSource.disconnect();
		rtpManager.dispose();
		
	}

	@Override
	public String getContentType()
	{
		return dataSource.getContentType();
	}

	@Override
	public Object getControl(String controlType)
	{
		return dataSource.getControl(controlType);
	}

	@Override
	public Object[] getControls()
	{
		return dataSource.getControls();
	}

	@Override
	public Time getDuration()
	{
		return dataSource.getDuration();
	}

	@Override
	public void start() throws IOException
	{
		dataSource.start();
		
	}

	@Override
	public void stop() throws IOException
	{
		dataSource.stop();
		
	}
	
}

