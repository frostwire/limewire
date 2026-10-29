package net.sf.fmj.media.parser;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.BadHeaderException;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Owned;
import javax.media.Track;
import javax.media.control.FrameRateControl;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

import net.sf.fmj.media.AbstractDemultiplexer;
import net.sf.fmj.media.AbstractTrack;
import net.sf.fmj.utility.LoggerSingleton;

/**
 * Raw Demultiplexer that reads from a PushBufferDataSource and passes the data unmodified to the
 * output.
 * This Demux doesn't really do much - since the stream does all the work of providing the buffers.
 * It may be that similar sun classes actually do something meaningful.
 * Very similar code could be used for PullBufferDataSource.
 * 
 * @author Ken Larson
 *
 */
public class RawPushBufferParser extends AbstractDemultiplexer 
{
	private static final Logger logger = LoggerSingleton.logger;

	private ContentDescriptor[] supportedInputContentDescriptors = new ContentDescriptor[] {new ContentDescriptor(ContentDescriptor.RAW)};

	private PushBufferDataSource source;
	
	private PushBufferStreamTrack[] tracks;
	
	private float frameRate = -1;
	private int sleepTime;
	private long lastSleepTime = -1;
	
	public RawPushBufferParser()
	{
		addControl(new FRC());
	}
	
	@Override
	public ContentDescriptor[] getSupportedInputContentDescriptors()
	{
		return supportedInputContentDescriptors;
	}
	
	@Override
	public Track[] getTracks() throws IOException, BadHeaderException
	{
		return tracks;
	}
	
	@Override
	public void setSource(DataSource source) throws IOException, IncompatibleSourceException
	{
		if (!(source instanceof PushBufferDataSource))
			throw new IncompatibleSourceException();
		
		this.source = (PushBufferDataSource) source;
		
	}
	
	@Override
	public void start() throws IOException
	{
		source.start();
		if (tracks == null)
		{
			// we assume that streams do not change if we stop and restart.  TODO: is this true?
			
			final PushBufferStream[] streams = source.getStreams();
			
			tracks = new PushBufferStreamTrack[streams.length];
			for (int i = 0; i < streams.length; ++i)
			{
				tracks[i] = new PushBufferStreamTrack(streams[i]);
			}
		}
	}
	
	@Override
	public void stop()
	{
		try 
		{
			source.stop();
		} catch (IOException e) 
		{
			logger.log(Level.WARNING, "" + e, e);
		}
	}
	
	private class FRC implements FrameRateControl, Owned
    {
        public Object getOwner()
		{
			return RawPushBufferParser.this;
		}
        
		public float getFrameRate()
		{
			return frameRate;
		}
		
		public float setFrameRate(float newFrameRate)
		{
			frameRate = newFrameRate;
			
			sleepTime = (int)(1000.0f / frameRate);
			
			return frameRate;
		}
		
		public float getMaxSupportedFrameRate()
		{
			return -1;
		}
		
		public float getPreferredFrameRate()
		{
			return -1;
		}
		
		public java.awt.Component getControlComponent()
		{
			return null;
		}
	}
	
	private class PushBufferStreamTrack extends AbstractTrack implements BufferTransferHandler
	{
		private final PushBufferStream stream;
		
		public PushBufferStreamTrack(PushBufferStream stream)
		{
			super();
			this.stream = stream;
			stream.setTransferHandler(this);
		}
		
		@Override
		public Format getFormat()
		{
			return stream.getFormat();
		}
		
		private Object dataAvailable = new Object();
		private int framesAvailable = 0;
		
		public void transferData(PushBufferStream stream)
		{
            synchronized ( dataAvailable )
            {
                framesAvailable++;
                dataAvailable.notifyAll();
            }
		}
		
		@Override
		public void readFrame(Buffer buffer)
		{
			// the transfer handler is not as useful as it might seem, since
			// read is (I think) a blocking method, and the reading all occurs within
			// its own thread anyway.  KAL.
			
			try
			{
                synchronized ( dataAvailable )
                {
                    while ( framesAvailable == 0 )
                    {
                        dataAvailable.wait();
                    }
                    
                    stream.read(buffer);
                    framesAvailable--;
                }
                
                adaptiveSleep(sleepTime);
			}
            catch (InterruptedException e)
            {
            }
            catch (Exception e)
			{
				buffer.setEOM(true);	// TODO
				buffer.setDiscard(true);	// TODO
				buffer.setLength(0);
				if (!(e instanceof InterruptedIOException))	// logging interruptions is noisy.
					logger.log(Level.FINE, "" + e, e);
			}
		}
		
		// uses the high resolution timer
		private void adaptiveSleep(long sleepTime)
		{
			if ( sleepTime > 0 )
			{
				long currentTime = (System.nanoTime() / 1000000L);
				if ( lastSleepTime == -1 )
				{
					lastSleepTime = currentTime;
				}
				else
				{
					lastSleepTime += sleepTime;
				}
				
				try
				{
					long deltaSleep = lastSleepTime - currentTime;
					if ( deltaSleep >= 2 )
					{
						Thread.sleep(deltaSleep);
					}
					else
					{
						lastSleepTime = currentTime;
					}
				}
				catch ( Exception dontcare )
				{
				}
			}
		}
	}
}
