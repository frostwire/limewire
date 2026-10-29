package net.sf.fmj.media.datasink.file;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.IncompatibleSourceException;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceTransferHandler;

import net.sf.fmj.media.AbstractDataSink;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.URLUtils;

import com.lti.utils.synchronization.CloseableThread;

/**
 * File DataSink.
 * @author Ken Larson
 *
 */
public class Handler extends AbstractDataSink
{
	private static final Logger logger = LoggerSingleton.logger;

	private PushDataSource source;
	private WriterThread writerThread;
	
	// TODO: additional listener notifications?
	
	public Object getControl(String controlType)
	{
		logger.warning("TODO: getControl " + controlType);
		return null;
	}

	public Object[] getControls()
	{
		logger.warning("TODO: getControls");
		return new Object[0];
	}

	public void close()
	{
        if (writerThread != null)
        {
            writerThread.close();
            
            try
            {
                writerThread.waitUntilClosed();
            }
            catch (InterruptedException e)
            {
            }
            finally
            {
                writerThread = null;
            }
        }
        
        try
		{
			stop();
		} catch (IOException e)
		{
			logger.log(Level.WARNING, "" + e, e);
		}
		
//		 TODO: disconnect source?
        if (source != null)
            source.disconnect();
    }

	public String getContentType()
	{
		// TODO: do we get this from the source, or the outputLocator?
		if (source != null)
			return source.getContentType();
		else
			return null;
	}

	
	public void open() throws IOException, SecurityException
	{
		if (getOutputLocator() == null)
			throw new IOException("Output locator not set");
		
		final String path = URLUtils.extractValidNewFilePathFromFileUrl(getOutputLocator().toExternalForm());
		if (path == null)
			throw new IOException("Cannot determine path from URL: " + getOutputLocator().toExternalForm());

		final File f = new File(path);
		// delete file if it is there:
		if (f.exists())
		{	
			logger.fine("Deleting " + f.getAbsolutePath());
			if (!f.delete())
				throw new IOException("Unable to delete: " + f.getAbsolutePath());
		}
		final RandomAccessFile raf = new RandomAccessFile(f, "rw");	// TODO: ensure closed even if start/stop never called.
		
		// TODO: check that there is at least 1 stream.
		// TODO: move this code to start() ?
		PushSourceStream[] streams = source.getStreams();
		
		source.connect();
		
		writerThread = new WriterThread(source.getStreams()[0], raf);	// TODO other tracks?
		writerThread.setName("WriterThread for " + raf);
		writerThread.setDaemon(true);
		
	}

	public void start() throws IOException
	{
		source.start();

		writerThread.start();
	}

	public void stop() throws IOException
	{
		if (source != null)
			source.stop();
	}

	
	public void setSource(DataSource source) throws IOException, IncompatibleSourceException
	{
		logger.finer("setSource: " + source);
		if (!(source instanceof PushDataSource))
			throw new IncompatibleSourceException();
		this.source = (PushDataSource) source;
	}
	
	// if we don't implement Seekable, Sun's code will throw a class cast exception.
	// very strange that we need to be able to seek just because we call setTransferHandler.
	private class WriterThread extends CloseableThread implements SourceTransferHandler, Seekable
	{
		public boolean isRandomAccess()
		{
			return true;
		}

		public long seek(long where)
		{
			try
			{
				raf.seek(where);
				
				return raf.getFilePointer();
			} catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);	// TODO: what to return
				throw new RuntimeException(e);
			}
		}

		public long tell()
		{
			try
			{
				return raf.getFilePointer();
			} catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);	// TODO: what to return
				throw new RuntimeException(e);
			}
		}

		private final PushSourceStream sourceStream;
		private final RandomAccessFile raf;
		
		private static final boolean USE_TRANSFER_HANDLER = true;
		private static final int DEFAULT_BUFFER_SIZE = 10000;

		public WriterThread(final PushSourceStream sourceStream, RandomAccessFile raf)
		{
			super();
			this.sourceStream = sourceStream;
			this.raf = raf;
			if (USE_TRANSFER_HANDLER)
				sourceStream.setTransferHandler(this);
		}
        private Object dataAvailable = new Object();
        
		public void transferData(PushSourceStream stream)
		{
            synchronized ( dataAvailable )
            {
                dataAvailable.notifyAll();
            }
        }
		
        @Override
		public void run()
		{
            logger.fine("getMinimumTransferSize: " + sourceStream.getMinimumTransferSize());
            final byte[] buffer = new byte[sourceStream.getMinimumTransferSize() > DEFAULT_BUFFER_SIZE ? sourceStream.getMinimumTransferSize() : DEFAULT_BUFFER_SIZE];

            boolean eos = false;
            while ( !isClosing() && !eos )
            {
                try
    			{
                    synchronized ( dataAvailable )
                    {
                        dataAvailable.wait();
                    
						int read = sourceStream.read(buffer, 0, buffer.length);
                        
						if (read == 0)
						{	
						    //mgodehardt: should not happend because PipedInputStream throws an Exception if we read and it has no data
						    break;	// TODO: what to do here?
						}
						else if (read < 0)
						{
							eos = true;
                            raf.close();
							logger.fine("EOS");
							notifyDataSinkListeners(new EndOfStreamEvent(Handler.this, "EOS"));		// TODO: needed?
							break;
						}
						else
						{	
                            raf.write(buffer, 0, read);
                        }
					}
    			}
                catch (InterruptedException e)
                {
                    break;
                }
                catch (InterruptedIOException e)
    			{
                    break;
                }
    			catch (IOException e)
    			{
    			    // mgodehardt: do we need this, read end dead may be thrown, its a 0 read, and we continue streaming
    				//logger.log(Level.FINE, "" + e, e);
    				//notifyDataSinkListeners(new DataSinkErrorEvent(Handler.this, "" + e));
    			}
            }
            
            setClosed();
            
			try
			{
                raf.close();
                
                // mgodehardt: do we need this ?
                logger.fine("EOS");
                notifyDataSinkListeners(new EndOfStreamEvent(Handler.this, "EOS"));     // TODO: needed?
            }
            catch ( Exception dontcare )
            {
            }
		}
	}
}
