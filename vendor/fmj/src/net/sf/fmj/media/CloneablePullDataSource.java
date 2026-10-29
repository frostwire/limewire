package net.sf.fmj.media;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceCloneable;

import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.ProducerConsumerQueue;

/**
 * Cloneable {@link PullDataSource}. 
 * TODO: test.
 * @author Ken Larson
 *
 */
public class CloneablePullDataSource extends PullDataSource implements SourceCloneable
{
	private static final Logger logger = LoggerSingleton.logger;
	
	private final PullDataSource source;
	private PullSourceStream[] streams;
	private final ClonedDataSource firstClonedDataSource;

	public CloneablePullDataSource(PullDataSource source)
	{
		super();
		this.source = source;
		firstClonedDataSource = (ClonedDataSource) createClone();
	}

	@Override
	public PullSourceStream[] getStreams()
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
	
	
	
	
	private class ClonedDataSource extends PullDataSource
	{

		private ClonedPullSourceStream[] clonedStreams;
		private boolean cloneConnected;
		private boolean cloneStarted;
		
		
		@Override
		public PullSourceStream[] getStreams()
		{
			synchronized (CloneablePullDataSource.this)
			{
				if (clonedStreams == null)
				{	
					clonedStreams = new ClonedPullSourceStream[streams.length];
					for (int i = 0; i < streams.length; ++i)
					{
						clonedStreams[i] = new ClonedPullSourceStream(i, streams[i]);
					}
				}
				return clonedStreams;
			}
		}

		@Override
		public void connect() throws IOException
		{
			
			synchronized (CloneablePullDataSource.this)
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
			synchronized (CloneablePullDataSource.this)
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
			synchronized (CloneablePullDataSource.this)
			{	return source.getContentType();
			}
		}

		@Override
		public Object getControl(String controlType)
		{
			synchronized (CloneablePullDataSource.this)
			{	return source.getControl(controlType);
			}
		}

		@Override
		public Object[] getControls()
		{
			synchronized (CloneablePullDataSource.this)
			{	return source.getControls();
			}
		}

		@Override
		public Time getDuration()
		{
			synchronized (CloneablePullDataSource.this)
			{	return source.getDuration();
			}
		}

		@Override
		public void start() throws IOException
		{
			// TODO: only start this data source?
			synchronized (CloneablePullDataSource.this)
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
			synchronized (CloneablePullDataSource.this)
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
		
		private static final int READ_BUFFER_SIZE = 2000; // TODO: how to determine? // TODO: take into account minimu transfer size?
		
		class ClonedPullSourceStream implements PullSourceStream
		{
			private final int streamIndex;
			private final PullSourceStream stream;
			private final ProducerConsumerQueue bufferQueue = new ProducerConsumerQueue();		// TODO: limit size?
			private final BufferQueueInputStream bufferQueueInputStream = new BufferQueueInputStream(bufferQueue);
			private boolean eos = false;
			
			public ClonedPullSourceStream(int streamIndex, PullSourceStream stream)
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
				synchronized (CloneablePullDataSource.this)
				{	return stream.getContentDescriptor();
				}
			}

			public long getContentLength()
			{
				synchronized (CloneablePullDataSource.this)
				{	return stream.getContentLength();
				}
			}

			public Object getControl(String controlType)
			{
				synchronized (CloneablePullDataSource.this)
				{	return stream.getControl(controlType);
				}
			}

			public Object[] getControls()
			{
				synchronized (CloneablePullDataSource.this)
				{	return stream.getControls();
				}
			}



			public boolean willReadBlock()
			{
				return bufferQueue.isEmpty(); // TODO: is this true?
			}

			public int read(byte[] buffer, int offset, int length) throws IOException
			{
				synchronized (CloneablePullDataSource.this)
				{
					// if our queue is empty, we have to read the next
					// underlying buffer, and queue it to ourself and all other cloned streams of the same
					// underlying stream (same stream index):
					
					if (bufferQueue.isEmpty())
					{
						final Buffer originalBuffer = new Buffer();	// TODO: find a way to reuse buffers/avoid allocating new memory each time
						final byte[] originalBufferData = new byte[READ_BUFFER_SIZE];
						originalBuffer.setData(originalBufferData);
						int numRead = stream.read(originalBufferData, 0, originalBufferData.length);
						if (numRead < 0)
							originalBuffer.setEOM(true);
						else
							originalBuffer.setLength(numRead);
						
						try
						{
							for (ClonedDataSource clone : clones)
							{
								final ClonedDataSource.ClonedPullSourceStream clonedStream = (ClonedDataSource.ClonedPullSourceStream) clone.getStreams()[streamIndex];
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
				
				return bufferQueueInputStream.read(buffer, offset, length);
			}
			
		
			
		}
		
	}
	

	
}
