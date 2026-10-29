package net.sf.fmj.media.protocol.rtp;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;

import net.sf.fmj.media.codec.video.jpeg.JpegRTPHeader;
import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.ProducerConsumerQueue;


/**
 * Looks ahead in the RTP PushBufferStream to fill in details of the VideoFormat, particularly the resolution.
 * Only supports JPEG/RTP.
 * @author Ken Larson
 *
 */
public class LookAheadPushBufferStream implements PushBufferStream
{
	private static final Logger logger = LoggerSingleton.logger;
	
	private final PushBufferStream s;
	private final ProducerConsumerQueue q = new ProducerConsumerQueue(); // Buffer or IOException
	private Format fullFormat;

	public LookAheadPushBufferStream(PushBufferStream s)
	{
		super();
		this.s = s;
	}
	
	public void lookAheadAndFindFullFormat() throws InterruptedException
	{
		s.setTransferHandler(new MyBufferTransferHandler());
		
		final Object o;
		synchronized (q)
		{
			q.waitUntilNotEmpty();
			o = q.get();
			q.put(o); // put back
		}
		if (o instanceof IOException)
		{
			fullFormat = s.getFormat();
			return;
		}
		final Buffer input = (Buffer) o;
		
		
		final Format f = s.getFormat();	 // TODO
		if (f.getEncoding().equals(VideoFormat.JPEG_RTP) && f.getClass() == VideoFormat.class)
		{	final VideoFormat vf = (VideoFormat) f;
			final JpegRTPHeader jpegRtpHeader = input.getLength() >= JpegRTPHeader.HEADER_SIZE ? JpegRTPHeader.parse((byte[]) input.getData(), input.getOffset()) : null;
			if (jpegRtpHeader== null)
			{	logger.warning("Expected buffer to be large enough for JPEG RTP Header");	// TODO: we could read buffers until we get one big enough.
				fullFormat = f;
			}
			fullFormat = new VideoFormat(vf.getEncoding(), new Dimension(jpegRtpHeader.getWidthInPixels(), jpegRtpHeader.getHeightInPixels()), vf.getMaxDataLength(), vf.getDataType(), vf.getFrameRate());
		}
		else
			fullFormat = f;	// TODO: others
		
		logger.info("Full format: " + fullFormat);
		
		if (transferHandler != null)
			transferHandler.transferData(this);
	}
	
	private class MyBufferTransferHandler implements BufferTransferHandler
	{
		public void transferData(PushBufferStream stream)
		{
			if (fullFormat == null)
			{
				try
				{	final Buffer b = new Buffer();
					
					try
					{
						s.read(b);
					}
					catch (IOException e)
					{	q.put(e);
					}
					q.put(b);
				}

				catch (InterruptedException e)
				{	return;
				}
			}
			else
			{	if (transferHandler != null)
					transferHandler.transferData(LookAheadPushBufferStream.this);
			}
		}
	}

	public boolean endOfStream()
	{
		return s.endOfStream();
	}

	public ContentDescriptor getContentDescriptor()
	{
		return s.getContentDescriptor();
	}

	public long getContentLength()
	{
		return s.getContentLength();
	}

	public Object getControl(String controlType)
	{
		return s.getControl(controlType);
	}

	public Object[] getControls()
	{
		return s.getControls();
	}

	public Format getFormat()
	{
		if (fullFormat != null)
			return fullFormat;
		return s.getFormat();
	}

	public void read(Buffer buffer) throws IOException
	{
		// any lookahead data gets returned first.
		if (!q.isEmpty())
		{	try
			{
				final Object o = q.get();
				if (o instanceof IOException)
					throw (IOException) o;
				final Buffer b = (Buffer) o;
				buffer.copy(b);
				return;
			}
			catch (InterruptedException e)
			{	throw new InterruptedIOException("" + e);
			}
		}
		s.read(buffer);
	}

	private BufferTransferHandler transferHandler;
	public void setTransferHandler(BufferTransferHandler transferHandler)
	{
		this.transferHandler = transferHandler;
		s.setTransferHandler(transferHandler);
	}
	
}
