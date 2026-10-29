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
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceCloneable;
import javax.media.protocol.SourceTransferHandler;

import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.ProducerConsumerQueue;

/**
 * Cloneable {@link PushDataSource}. 
 * @author Ken Larson
 *
 */
public class CloneablePushDataSource extends PushDataSource implements SourceCloneable
{
	// TODO: we should be more efficient in our use of buffers.  We currently clone each buffer
	// once for each clone.  Perhaps the buffers could be simply stored in a multi-consumer queue?
	// one of the problems with this is that I don't believe a Buffer is considered to be 
	// immutable by the filter graph process.  This is because a codec can choose to consume
	// only part of the input, and then update the offset/length to reflect what it has consumed.
	// also data swapping between buffers can occur.
	private static final Logger logger = LoggerSingleton.logger;
	
	private final PushDataSource source;
	private PushSourceStream[] streams;
	private final ClonedDataSource firstClonedDataSource;

	public CloneablePushDataSource(PushDataSource source)
	{
		super();
		this.source = source;
		firstClonedDataSource = (ClonedDataSource) createClone();
	}

	@Override
	public PushSourceStream[] getStreams()
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
	
	private static final int READ_BUFFER_SIZE = 2000; // TODO: how to determine? // TODO: take into account minimu transfer size?

	private class MySourceTransferHandler implements SourceTransferHandler
	{
		
		private final int streamIndex;
		
		public MySourceTransferHandler(int streamIndex)
		{
			super();
			this.streamIndex = streamIndex;
		}

		public void transferData(PushSourceStream stream)
		{
			final Buffer originalBuffer = new Buffer();	// TODO: find a way to reuse buffers/avoid allocating new memory each time
			
			try
			{
				final byte[] originalBufferData = new byte[READ_BUFFER_SIZE];
				originalBuffer.setData(originalBufferData);
				int numRead = stream.read(originalBufferData, 0, originalBufferData.length);
				if (numRead < 0)
					originalBuffer.setEOM(true);
				else
					originalBuffer.setLength(numRead);				
			} catch (IOException e)
			{
				originalBuffer.setEOM(true);	// TODO
				originalBuffer.setDiscard(true);	// TODO
				originalBuffer.setLength(0);
				if (!(e instanceof InterruptedIOException))	// logging interruptions is noisy.
					logger.log(Level.WARNING, "" + e, e);
			}
			
			final List<ClonedDataSource.ClonedPushSourceStream> clonedStreams = new ArrayList<ClonedDataSource.ClonedPushSourceStream>();
			synchronized (CloneablePushDataSource.this)
			{
				for (ClonedDataSource clone : clones)
				{
					final ClonedDataSource.ClonedPushSourceStream clonedStream = (ClonedDataSource.ClonedPushSourceStream) clone.getStreams()[streamIndex];
					clonedStreams.add(clonedStream);
					
				}
			}
			
			// TODO: additional synchronization?
			try
			{
				// put a clone of the buffer in each stream's buffer queue
				for (ClonedDataSource.ClonedPushSourceStream clonedStream : clonedStreams)
				{
					clonedStream.getBufferQueue().put(originalBuffer.clone());
					
				}
				
				// notify their transfer handlers asynchronously:
				for (ClonedDataSource.ClonedPushSourceStream clonedStream : clonedStreams)
				{
					clonedStream.notifyTransferHandlerAsync();
					
				}
				
				
			}
			catch (InterruptedException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return;
			}
			
			
		}
	}
	
	private class ClonedDataSource extends PushDataSource
	{

		private ClonedPushSourceStream[] clonedStreams;
		private boolean cloneConnected;
		private boolean cloneStarted;
		
		
		@Override
		public PushSourceStream[] getStreams()
		{
			synchronized (CloneablePushDataSource.this)
			{
				if (clonedStreams == null)
				{	
					clonedStreams = new ClonedPushSourceStream[streams.length];
					for (int i = 0; i < streams.length; ++i)
					{
						clonedStreams[i] = new ClonedPushSourceStream(streams[i]);
					}
				}
				return clonedStreams;
			}
		}

		@Override
		public void connect() throws IOException
		{
			
			synchronized (CloneablePushDataSource.this)
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
			boolean disposeAllClones = false;
			
			synchronized (CloneablePushDataSource.this)
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
					
					for (ClonedDataSource clone : clones)
						clone.disposeAsync();
					
					sourceConnected = false;
				}
				
			}
			
			if (disposeAllClones)
			{
			}
		}

		@Override
		public String getContentType()
		{
			synchronized (CloneablePushDataSource.this)
			{	return source.getContentType();
			}
		}

		@Override
		public Object getControl(String controlType)
		{
			synchronized (CloneablePushDataSource.this)
			{	return source.getControl(controlType);
			}
		}

		@Override
		public Object[] getControls()
		{
			synchronized (CloneablePushDataSource.this)
			{	return source.getControls();
			}
		}

		@Override
		public Time getDuration()
		{
			synchronized (CloneablePushDataSource.this)
			{	return source.getDuration();
			}
		}

		@Override
		public void start() throws IOException
		{
			// TODO: only start this data source?
			synchronized (CloneablePushDataSource.this)
			{
				if (cloneStarted)
					return;
				if (!sourceStarted)
				{	
					streams = source.getStreams();
					for (int i = 0; i < streams.length; ++i)
					{
						streams[i].setTransferHandler(new MySourceTransferHandler(i));
					}
					source.start();
				
					sourceStarted = true;
				}
				
				cloneStarted = true;
			}
		}

		@Override
		public void stop() throws IOException
		{
			synchronized (CloneablePushDataSource.this)
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
		
		public void disposeAsync()
		{
			for (ClonedPushSourceStream clonedStream : clonedStreams)
			{	clonedStream.disposeAsync();
			}
		}
		
		class ClonedPushSourceStream implements PushSourceStream
		{
			private final PushSourceStream stream;
			private final ProducerConsumerQueue bufferQueue = new ProducerConsumerQueue();		// TODO: limit size?
			private final BufferQueueInputStream bufferQueueInputStream = new BufferQueueInputStream(bufferQueue);
			
			private boolean eos = false;
			
			public ClonedPushSourceStream(PushSourceStream stream)
			{
				super();
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
				synchronized (CloneablePushDataSource.this)
				{	return stream.getContentDescriptor();
				}
			}

			public long getContentLength()
			{
				synchronized (CloneablePushDataSource.this)
				{	return stream.getContentLength();
				}
			}

			public int getMinimumTransferSize()
			{
				synchronized (CloneablePushDataSource.this)
				{	return stream.getMinimumTransferSize();
				}
			}

			public Object getControl(String controlType)
			{
				synchronized (CloneablePushDataSource.this)
				{	return stream.getControl(controlType);
				}
			}

			public Object[] getControls()
			{
				synchronized (CloneablePushDataSource.this)
				{	return stream.getControls();
				}
			}

			public int read(byte[] buffer, int offset, int length) throws IOException
			{
				if (bufferQueue.isEmpty())
					return 0;
				return bufferQueueInputStream.read(buffer, offset, length);
			}
			
			private final AsyncSourceTransferHandlerNotifier asyncSourceTransferHandlerNotifier = new AsyncSourceTransferHandlerNotifier(this);

			public void setTransferHandler(SourceTransferHandler transferHandler)
			{
				asyncSourceTransferHandlerNotifier.setTransferHandler(transferHandler);
			}

			public void notifyTransferHandlerAsync() throws InterruptedException
			{
				// TODO: looser synchronization?
				synchronized (CloneablePushDataSource.this)
				{
					if (!cloneStarted)
						return;
					asyncSourceTransferHandlerNotifier.notifyTransferHandlerAsync();
				}
			}

			public void dispose()
			{
				asyncSourceTransferHandlerNotifier.dispose();
			}
			
			public void disposeAsync()
			{
				asyncSourceTransferHandlerNotifier.disposeAsync();
			}

		
			
		}
		
	}
	

	
}
