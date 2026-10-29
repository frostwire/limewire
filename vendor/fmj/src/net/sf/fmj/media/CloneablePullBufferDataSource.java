package net.sf.fmj.media;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.SourceCloneable;

import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.ProducerConsumerQueue;

/**
 * Cloneable {@link PullBufferDataSource}. 
 * TODO: test.
 * @author Ken Larson
 *
 */
public class CloneablePullBufferDataSource extends PullBufferDataSource implements SourceCloneable
{
	private static final Logger logger = LoggerSingleton.logger;
	
	private final PullBufferDataSource source;
	private PullBufferStream[] streams;
	private final ClonedDataSource firstClonedDataSource;

	public CloneablePullBufferDataSource(PullBufferDataSource source)
	{
		super();
		this.source = source;
		firstClonedDataSource = (ClonedDataSource) createClone();
	}

	@Override
	public PullBufferStream[] getStreams()
	{
		return firstClonedDataSource.getStreams();
	}


	@Override
	public void connect() throws IOException
	{
		firstClonedDataSource.connect();
	}

	@Override
	public void disconnect()
	{
		firstClonedDataSource.disconnect();
	}

	@Override
	public String getContentType()
	{
		return firstClonedDataSource.getContentType();
	}

	@Override
	public Object getControl(String controlType)
	{
		return firstClonedDataSource.getControl(controlType);
	}

	@Override
	public Object[] getControls()
	{
		return firstClonedDataSource.getControls();
	}

	@Override
	public Time getDuration()
	{
		return firstClonedDataSource.getDuration();
	}

	@Override
	public void start() throws IOException
	{
		firstClonedDataSource.start();
	}

	@Override
	public void stop() throws IOException
	{
		firstClonedDataSource.stop();
	}
	
	private List<ClonedDataSource> clones = new ArrayList<ClonedDataSource>();
	public synchronized DataSource createClone()
	{
		final ClonedDataSource result = new ClonedDataSource();
		clones.add(result);
		return result;
	}
	
	private boolean sourceConnected = false;
	private boolean sourceStarted = false;
	
	
	
	
	private class ClonedDataSource extends PullBufferDataSource
	{

		private ClonedPullBufferStream[] clonedStreams;
		private boolean cloneConnected;
		private boolean cloneStarted;
		
		
		@Override
		public PullBufferStream[] getStreams()
		{
			synchronized (CloneablePullBufferDataSource.this)
			{
				if (clonedStreams == null)
				{	
					clonedStreams = new ClonedPullBufferStream[streams.length];
					for (int i = 0; i < streams.length; ++i)
					{
						clonedStreams[i] = new ClonedPullBufferStream(i, streams[i]);
					}
				}
				return clonedStreams;
			}
		}

		@Override
		public void connect() throws IOException
		{
			
			synchronized (CloneablePullBufferDataSource.this)
			{
				if (cloneConnected)
					return;
				
				if (!sourceConnected)
				{	source.connect();
					sourceConnected = true;
				}
				
				cloneConnected = true;
			}
		}

		@Override
		public void disconnect()
		{
			synchronized (CloneablePullBufferDataSource.this)
			{
				if (!cloneConnected)
					return;
				cloneConnected = false;
				
				if (sourceConnected)	// should always be true if the clone was connected...
				{
					// stop underlying source if needed:
					for (ClonedDataSource clone : clones)
					{
						if (clone.cloneConnected)
							return;		// at least one started, don't close underlying source
					}
					
					source.disconnect();
					
					sourceConnected = false;
				}
				
			}
		}

		@Override
		public String getContentType()
		{
			synchronized (CloneablePullBufferDataSource.this)
			{	return source.getContentType();
			}
		}

		@Override
		public Object getControl(String controlType)
		{
			synchronized (CloneablePullBufferDataSource.this)
			{	return source.getControl(controlType);
			}
		}

		@Override
		public Object[] getControls()
		{
			synchronized (CloneablePullBufferDataSource.this)
			{	return source.getControls();
			}
		}

		@Override
		public Time getDuration()
		{
			synchronized (CloneablePullBufferDataSource.this)
			{	return source.getDuration();
			}
		}

		@Override
		public void start() throws IOException
		{
			// TODO: only start this data source?
			synchronized (CloneablePullBufferDataSource.this)
			{
				if (cloneStarted)
					return;
				if (!sourceStarted)
				{	
					streams = source.getStreams();
					source.start();
				
					sourceStarted = true;
				}
				
				cloneStarted = true;
			}
		}

		@Override
		public void stop() throws IOException
		{
			synchronized (CloneablePullBufferDataSource.this)
			{
				if (!cloneStarted)
					return;
				cloneStarted = false;
				
				if (sourceStarted) // should always be true if the clone was started
				{
					// stop underlying source if needed:
					for (ClonedDataSource clone : clones)
					{
						if (clone.cloneStarted)
							return;		// at least one started, don't close underlying source
					}
					
					source.stop();
					
					sourceStarted = false;
				}
				
			}
		}
		
		class ClonedPullBufferStream implements PullBufferStream
		{
			private final int streamIndex;
			private final PullBufferStream stream;
			private final ProducerConsumerQueue bufferQueue = new ProducerConsumerQueue();		// TODO: limit size?
			private boolean eos = false;
			
			public ClonedPullBufferStream(int streamIndex, PullBufferStream stream)
			{
				super();
				this.streamIndex = streamIndex;
				this.stream = stream;
			}
			
			ProducerConsumerQueue getBufferQueue()
			{	return bufferQueue;
			}

			public boolean endOfStream()
			{
				return eos;
			}

			public ContentDescriptor getContentDescriptor()
			{
				synchronized (CloneablePullBufferDataSource.this)
				{	return stream.getContentDescriptor();
				}
			}

			public long getContentLength()
			{
				synchronized (CloneablePullBufferDataSource.this)
				{	return stream.getContentLength();
				}
			}

			public Object getControl(String controlType)
			{
				synchronized (CloneablePullBufferDataSource.this)
				{	return stream.getControl(controlType);
				}
			}

			public Object[] getControls()
			{
				synchronized (CloneablePullBufferDataSource.this)
				{	return stream.getControls();
				}
			}

			public Format getFormat()
			{
				synchronized (CloneablePullBufferDataSource.this)
				{	return stream.getFormat();
				}
			}

			public boolean willReadBlock()
			{
				return bufferQueue.isEmpty();
			}

			public void read(Buffer buffer) throws IOException
			{
				synchronized (CloneablePullBufferDataSource.this)
				{
					// if our queue is empty, we have to read the next
					// underlying buffer, and queue it to ourself and all other cloned streams of the same
					// underlying stream (same stream index):
					
					if (bufferQueue.isEmpty())
					{
						final Buffer originalBuffer = new Buffer();	// TODO: find a way to reuse buffers/avoid allocating new memory each time
						stream.read(originalBuffer);
						try
						{
							for (ClonedDataSource clone : clones)
							{
								final ClonedDataSource.ClonedPullBufferStream clonedStream = (ClonedDataSource.ClonedPullBufferStream) clone.getStreams()[streamIndex];
								clonedStream.getBufferQueue().put((Buffer) originalBuffer.clone());
							}
						}
						catch (InterruptedException e)
						{
							logger.log(Level.WARNING, "" + e, e);
							throw new InterruptedIOException();
						}
					}
				}
				
				Buffer nextBuffer = null;
				try
				{
					nextBuffer = (Buffer) bufferQueue.get();
				} catch (InterruptedException e)
				{
					throw new InterruptedIOException("" + e);
				}
				if (nextBuffer.isEOM())
					eos = true;
				
				buffer.copy(nextBuffer);
				
				
			}
		
			
		}
		
	}
	

	
}
