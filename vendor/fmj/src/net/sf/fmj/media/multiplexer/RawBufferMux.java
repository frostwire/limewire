package net.sf.fmj.media.multiplexer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.Owned;
import javax.media.ResourceUnavailableException;
import javax.media.Time;
import javax.media.control.BitRateControl;
import javax.media.control.FrameRateControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;

import net.sf.fmj.media.AbstractMultiplexer;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.LoggingStringUtils;
import net.sf.fmj.utility.RingBuffer;

/**
 * Raw buffer multiplexer.  Takes buffers as input, and provides them unmodified to the output.
 * @author Ken Larson
 *
 */
public class RawBufferMux extends AbstractMultiplexer
{
	private final boolean TRACE = false;
	private static final Logger logger = LoggerSingleton.logger;

	private RawBufferDataSource dataOutput;
	private RawBufferSourceStream[] streams;
	private RingBuffer[] jitterBuffer;
	
	private final ContentDescriptor contentDescriptor;

	// mgodehardt: for the BitRateControl, it will count the bytes processed
	private long lastTimestamp = -1;
	private int bytesProcessed;
	private int bitsPerSecond;

	private float frameRate = -1;
	private int framesProcessed;
	
	protected RawBufferMux(ContentDescriptor contentDescriptor)
	{
		super();
		this.contentDescriptor = contentDescriptor;
	}
	
	public RawBufferMux()
	{
		this(new ContentDescriptor(ContentDescriptor.RAW));
	}

	public DataSource getDataOutput()
	{
		if (dataOutput == null)
			dataOutput = new RawBufferDataSource();
		if (TRACE) System.out.println(getClass().getSimpleName() + " getDataOutput");
		return dataOutput;
	}

	public Format[] getSupportedInputFormats()
	{
		if (TRACE) System.out.println(getClass().getSimpleName() + " getSupportedInputFormats");
		return new Format[] {
			new AudioFormat(null, -1.0, -1, -1, -1, -1, -1, -1.0, Format.byteArray),
			new VideoFormat(null, null, -1, Format.byteArray, -1.0f)
		};
	}

	@Override
	public void close()
	{
		if (TRACE) System.out.println(getClass().getSimpleName() + " close");
		super.close();
		
		if (dataOutput != null)
		{	try
			{
				dataOutput.stop();
			} catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);
			}
			dataOutput.disconnect();
		}
	}

	@Override
	public void open() throws ResourceUnavailableException
	{
		if (TRACE) System.out.println("open");
		super.open();
	}

	public ContentDescriptor[] getSupportedOutputContentDescriptors(Format[] inputs)
	{
		// TODO: should this match the # of entries in inputs?
		return new ContentDescriptor[] {contentDescriptor};
	}
	
	public int process(Buffer buffer, int trackID)
	{
		if (TRACE) System.out.println("RawBufferMux process: " + LoggingStringUtils.bufferToStr(buffer));
		if (TRACE) System.out.println(getClass().getSimpleName() + " process " + buffer + " " + trackID + " length " + buffer.getLength());
		
		if ( buffer.isEOM() )
		{
			logger.fine("EOM, finished");
		}
		
		if ( null != dataOutput )
		{
		    // mgodehardt: put and get are in the same thread, so no buffer overruns will occur
			jitterBuffer[trackID].put(buffer.clone());
			dataOutput.notifyDataAvailable(trackID);
		}
		
		return BUFFER_PROCESSED_OK;
	}

	@Override
	public int setNumTracks(int numTracks)
	{
		if (TRACE) System.out.println("setNumTracks");
		
		numTracks = super.setNumTracks(numTracks);
		
		jitterBuffer = new RingBuffer[numTracks];
		for (int track = 0; track < numTracks; ++track)
		{
			jitterBuffer[track] = new RingBuffer(1);
		}
		
		streams = new RawBufferSourceStream[numTracks];
		for (int track = 0; track < numTracks; ++track)
		{
			streams[track] = new RawBufferSourceStream(jitterBuffer[track]);
		}
		
		return numTracks;
	}
	

	@Override
	public Format setInputFormat(Format format, int trackID)
	{
		final Format result = super.setInputFormat(format, trackID);
		if (result != null)
		{	if (streams != null)	// TODO: if null, make sure we set later!
			{	streams[trackID].setFormat(result);
			}
		}
		return result;
	}

	private class VideoFrameRateControl implements FrameRateControl, Owned
    {
        public Object getOwner()
		{
			return RawBufferMux.this;
		}
        
		public float getFrameRate()
		{
			return frameRate;
		}
		
		public float setFrameRate(float newFrameRate)
		{
			return -1;
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
	
	// mgodehardt: returns the current thruput, for e.g. if this drops below 64000 bps with ULAW RTP, then
	// the receiver will have have hipcups in the stream, this is a read only control
	private class RawBufferBitRateControl implements BitRateControl, Owned
    {
        public Object getOwner()
		{
			return RawBufferMux.this;
		}
        
		public int getBitRate()
		{
			return bitsPerSecond;
		}
		
		public int setBitRate(int bitrate)
		{
			return -1;
		}
		
		public int getMinSupportedBitRate()
		{
			return -1;
		}
		
		public int getMaxSupportedBitRate()
		{
			return -1;
		}
		
		public java.awt.Component getControlComponent()
		{
			return null;
		}
	}

	private class RawBufferDataSource extends PushBufferDataSource
	{
		private Object[] controls = new Object[] {new RawBufferBitRateControl(), new VideoFrameRateControl()};
		
		@Override
		public PushBufferStream[] getStreams()
		{
			if (TRACE) System.out.println("getStreams");
			return streams;
		}

		@Override
		public void connect() throws IOException
		{
			if (TRACE) System.out.println(getClass().getSimpleName() + " connect");
			
		}

		@Override
		public void disconnect()
		{
			if (TRACE) System.out.println(getClass().getSimpleName() + " disconnect");
		}

		@Override
		public String getContentType()
		{
			return outputContentDescriptor.getContentType();
		}

		@Override
		public Object getControl(String controlType)
		{
			if (TRACE) System.out.println("getControl");
			return null;
		}

		@Override
		public Object[] getControls()
		{
			return controls;
		}

		@Override
		public Time getDuration()
		{
			return DURATION_UNKNOWN;
		}

		@Override
		public void start() throws IOException
		{
			if (TRACE) System.out.println(getClass().getSimpleName() + " start");
		}

		@Override
		public void stop() throws IOException
		{
			if (TRACE) System.out.println(getClass().getSimpleName() + " stop");
			
			// mgodehardt: was stopped so its reset, should we also stop the TrackThread ????
			lastTimestamp = -1;
			bytesProcessed = 0;
			bitsPerSecond = 0;

			frameRate = -1;
			framesProcessed = 0;
		}
		
		public void notifyDataAvailable(int track)
		{
			streams[track].notifyDataAvailable();
		}
	}
	
	
	private class RawBufferSourceStream implements PushBufferStream
	{
		private final RingBuffer jitterBuffer;
		private boolean eos;
		private Format format;

		public RawBufferSourceStream(final RingBuffer jitterBuffer)
		{
			super();
			this.jitterBuffer = jitterBuffer;
		}

		public boolean endOfStream()
		{
			return eos;
		}

		public ContentDescriptor getContentDescriptor()
		{
			return outputContentDescriptor;
		}

		public long getContentLength()
		{
			return 0;
		}

		public Object getControl(String controlType)
		{
			return null;
		}

		public Object[] getControls()
		{
			return new Object[0];
		}
		
		void setFormat(Format f)
		{	format = f;
		}

		public Format getFormat()
		{
			return format;
		}

		
		public void read(Buffer buffer) throws IOException
		{
			if (TRACE) System.out.println(getClass().getSimpleName() + " read");
			
			Buffer next = null;
			
			try
			{
                next = (Buffer)jitterBuffer.get();
			}
			catch ( Exception ex )
			{
			}
			
			if ( (next == null) || next.isEOM() )
			{
				eos = true;
			}
			
			if (next != null)
			{
				if (buffer.getData() == null)
					buffer.copy(next, false);	
				else
				{
					// according to the API, if the caller sets the
					// data in the buffer, we should not allocate it.
					// See http://java.sun.com/products/java-media/jmf/2.1.1/apidocs/javax/media/protocol/PushBufferStream.html
					
					// we use the original offset in the buffer in this case, since that is what is required
					// by FMJ's RTPSendStream.transferData(PushBufferStream stream).  This feature does not appear to be
					// defined in the API.
					
					final Object originalData = buffer.getData();
					final int originalOffset = buffer.getOffset();
					final int originalLength =  arrayLength(buffer.getData());
					buffer.copy(next, false);
					buffer.setData(originalData);
					buffer.setOffset(originalOffset);
					// length is set in copy
					
					// mgodehardt: will measure thruput in bits per second for the BitRateControl
					// dont know if this the right place to measure the thruput
					long currentTimestamp = System.nanoTime();
					if ( -1 == lastTimestamp )
					{
						lastTimestamp = currentTimestamp;
					}
					
					bytesProcessed += buffer.getLength();
					
					if ( (buffer.getFlags() & Buffer.FLAG_RTP_MARKER) > 0 )
					{
						framesProcessed++;
					}
					
					if ( (currentTimestamp - lastTimestamp) > 1000000000L )
					{
						bitsPerSecond = bytesProcessed << 3;
						
						float diffTime = (float)(currentTimestamp - lastTimestamp) / 1000000L;
						frameRate = (float)framesProcessed * (1000.0f / diffTime);
						
						bytesProcessed = 0;
						framesProcessed = 0;
						lastTimestamp = currentTimestamp;
					}
					
					if (next.getLength() > 0)
					{	
						// TODO: what if original data is not big enough?
						if (next.getLength() > originalLength)
						{	
							logger.warning("Buffer passed in has length: " + originalLength + "; needs to be at least: " + next.getLength() + "; not able to re-use passed in buffer data");
							buffer.copy(next, false);	
						}
						else
						{
							System.arraycopy(next.getData(), next.getOffset(), originalData, originalOffset, next.getLength());
						}
					}
				}
			}
			else
			{	System.out.println("RawBufferMux EOS");
				System.out.flush();
				buffer.setEOM(true);
				buffer.setLength(0);
				buffer.setOffset(0);
			}
		}
		
		private int arrayLength(Object data)
		{
			if (data instanceof byte[])
				return ((byte[]) data).length;
			else if (data instanceof short[])
				return ((short[]) data).length;
			else if (data instanceof int[])
				return ((int[]) data).length;
			else
				throw new IllegalArgumentException();
		}

		private BufferTransferHandler transferHandler;
		public void setTransferHandler(BufferTransferHandler transferHandler)
		{
			this.transferHandler = transferHandler;
		}
		
		public void notifyDataAvailable()
		{
			if (transferHandler != null)
				transferHandler.transferData(this);
		}
		
	}


}
