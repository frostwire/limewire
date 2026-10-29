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
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.protocol.SourceCloneable;

import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.ProducerConsumerQueue;

/**
 * Cloneable {@link PushBufferDataSource}. 
 * @author Ken Larson
 *
 */
public class CloneablePushBufferDataSource extends PushBufferDataSource implements SourceCloneable
{
	// TODO: we should be more efficient in our use of buffers.  We currently clone each buffer
	// once for each clone.  Perhaps the buffers could be simply stored in a multi-consumer queue?
	// one of the problems with this is that I don't believe a Buffer is considered to be 
	// immutable by the filter graph process.  This is because a codec can choose to consume
	// only part of the input, and then update the offset/length to reflect what it has consumed.
	// also data swapping between buffers can occur.
	private static final Logger logger = LoggerSingleton.logger;
	
	private final PushBufferDataSource source;
	private PushBufferStream[] streams;
	private final ClonedDataSource firstClonedDataSource;

	public CloneablePushBufferDataSource(PushBufferDataSource source)
	{
		super();
		this.source = source;
		firstClonedDataSource = (ClonedDataSource) createClone();
	}

	@Override
	public PushBufferStream[] getStreams()
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
	
	
	private class MyBufferTransferHandler implements BufferTransferHandler
	{
		
		private final int streamIndex;
		
		public MyBufferTransferHandler(int streamIndex)
		{
			super();
			this.streamIndex = streamIndex;
		}

		public void transferData(PushBufferStream stream)
		{
			final Buffer originalBuffer = new Buffer(); // TODO: find a way to reuse buffers/avoid allocating new memory each time
			try
			{
				stream.read(originalBuffer);
			} catch (IOException e)
			{
				originalBuffer.setEOM(true);	// TODO
				originalBuffer.setDiscard(true);	// TODO
				originalBuffer.setLength(0);
				if (!(e instanceof InterruptedIOException))	// logging interruptions is noisy.
					logger.log(Level.WARNING, "" + e, e);
			}
			
			final List<ClonedDataSource.ClonedPushBufferStream> clonedStreams = new ArrayList<ClonedDataSource.ClonedPushBufferStream>();
			synchronized (CloneablePushBufferDataSource.this)
			{
				for (ClonedDataSource clone : clones)
				{
					final ClonedDataSource.ClonedPushBufferStream clonedStream = (ClonedDataSource.ClonedPushBufferStream) clone.getStreams()[streamIndex];

					// mgodehardt: Only started cloned streams get data (this caused OutOfMemory Bugs)
					if ( clone.cloneStarted )
					{
						clonedStreams.add(clonedStream);
					}
				}
			}
			
			// TODO: additional synchronization?
			try
			{
				// put a clone of the buffer in each stream's buffer queue
				for (ClonedDataSource.ClonedPushBufferStream clonedStream : clonedStreams)
				{
					clonedStream.getBufferQueue().put(originalBuffer.clone());
					
				}
				
				// notify their transfer handlers asynchronously:
				for (ClonedDataSource.ClonedPushBufferStream clonedStream : clonedStreams)
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
	
	private class ClonedDataSource extends PushBufferDataSource
	{

		private ClonedPushBufferStream[] clonedStreams;
		private boolean cloneConnected;
		public boolean cloneStarted;
		
		
		@Override
		public PushBufferStream[] getStreams()
		{
			synchronized (CloneablePushBufferDataSource.this)
			{
				if (clonedStreams == null)
				{	
					clonedStreams = new ClonedPushBufferStream[streams.length];
					for (int i = 0; i < streams.length; ++i)
					{
						clonedStreams[i] = new ClonedPushBufferStream(streams[i]);
					}
				}
				return clonedStreams;
			}
		}

		@Override
		public void connect() throws IOException
		{
			
			synchronized (CloneablePushBufferDataSource.this)
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
			
			synchronized (CloneablePushBufferDataSource.this)
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
			synchronized (CloneablePushBufferDataSource.this)
			{	return source.getContentType();
			}
		}

		@Override
		public Object getControl(String controlType)
		{
			synchronized (CloneablePushBufferDataSource.this)
			{	return source.getControl(controlType);
			}
		}

		@Override
		public Object[] getControls()
		{
			synchronized (CloneablePushBufferDataSource.this)
			{	return source.getControls();
			}
		}

		@Override
		public Time getDuration()
		{
			synchronized (CloneablePushBufferDataSource.this)
			{	return source.getDuration();
			}
		}

		@Override
		public void start() throws IOException
		{
			// TODO: only start this data source?
			synchronized (CloneablePushBufferDataSource.this)
			{
				if (cloneStarted)
					return;
				if (!sourceStarted)
				{	
					streams = source.getStreams();
					for (int i = 0; i < streams.length; ++i)
					{
						streams[i].setTransferHandler(new MyBufferTransferHandler(i));
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
			synchronized (CloneablePushBufferDataSource.this)
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
			for (ClonedPushBufferStream clonedStream : clonedStreams)
			{	clonedStream.disposeAsync();
			}
		}
		
		class ClonedPushBufferStream implements PushBufferStream
		{
			private final PushBufferStream stream;
			private final ProducerConsumerQueue bufferQueue = new ProducerConsumerQueue();		// TODO: limit size?
			private boolean eos = false;
			
			public ClonedPushBufferStream(PushBufferStream stream)
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
				synchronized (CloneablePushBufferDataSource.this)
				{	return stream.getContentDescriptor();
				}
			}

			public long getContentLength()
			{
				synchronized (CloneablePushBufferDataSource.this)
				{	return stream.getContentLength();
				}
			}

			public Object getControl(String controlType)
			{
				synchronized (CloneablePushBufferDataSource.this)
				{	return stream.getControl(controlType);
				}
			}

			public Object[] getControls()
			{
				synchronized (CloneablePushBufferDataSource.this)
				{	return stream.getControls();
				}
			}

			public Format getFormat()
			{
				synchronized (CloneablePushBufferDataSource.this)
				{	return stream.getFormat();
				}
			}

			public void read(Buffer buffer) throws IOException
			{
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
			
			private final AsyncBufferTransferHandlerNotifier asyncBufferTransferHandlerNotifier = new AsyncBufferTransferHandlerNotifier(this);

			public void setTransferHandler(BufferTransferHandler transferHandler)
			{
				asyncBufferTransferHandlerNotifier.setTransferHandler(transferHandler);
			}

			public void notifyTransferHandlerAsync() throws InterruptedException
			{
				// TODO: looser synchronization?
				synchronized (CloneablePushBufferDataSource.this)
				{
					if (!cloneStarted)
						return;
					asyncBufferTransferHandlerNotifier.notifyTransferHandlerAsync();
				}
			}

			public void dispose()
			{
				asyncBufferTransferHandlerNotifier.dispose();
			}
			
			public void disposeAsync()
			{
				asyncBufferTransferHandlerNotifier.disposeAsync();
			}

		
			
		}
		
	}
	

	
}
