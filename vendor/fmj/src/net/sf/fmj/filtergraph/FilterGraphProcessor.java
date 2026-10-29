package net.sf.fmj.filtergraph;

import java.io.IOException;
import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.PlugIn;
import javax.media.ResourceUnavailableException;

import net.sf.fmj.utility.LoggerSingleton;

/**
 * Execute functions on filter graphs.  Does not actually represent the graph itself, that 
 * would be simply a FilterGraphNode being used as a root.
 * TODO: if a demux for example sets both discard and eom, what are the semantics?  It appears that
 * we will discard the EOM.
 * @author Ken Larson
 *
 */
final class FilterGraphProcessor
{
	private static final Logger logger = LoggerSingleton.logger;

	private static final boolean TRACE = true;

	private FilterGraphProcessor()
	{	super();
	}

	public static Node getTail(Node n)
	{
		if (n == null) {
			return n;
		}
		while (n.getNumDestLinks() > 0)
			n = n.getDestLink(0).getDestNode();	// TODO: this doesn't handle demuxs
		return n;
	}
	
	/**
	 * Finds a penultimate node - one right before a mux or renderer.
	 */
	public static Node getBeforeTail(Node n)
	{
		// TODO: this always follows the first link from any node.
		while (n.getNumDestLinks() > 0 && n.getDestLink(0).getDestNode().getNumDestLinks() > 0)
		{	n = n.getDestLink(0).getDestNode();	// TODO: this doesn't handle demuxs
		}
		return n;
	}	
	
	public static void start(Node n) throws IOException
	{
		n.start();
		
		for (int i = 0; i < n.getNumDestLinks(); ++i)
		{	final Link destLink = n.getDestLink(i);
			if (destLink != null)
				start(destLink.getDestNode());	// track doesn't matter for this.  TODO: prevent starting same node twice.
		}
	

	}
	
	public static void stop(Node n) throws IOException
	{
		n.stop();

		for (int i = 0; i < n.getNumDestLinks(); ++i)
		{	final Link destLink = n.getDestLink(i);
			if (destLink != null)
				stop(destLink.getDestNode());	// track doesn't matter for this.  TODO: prevent starting same node twice.
		}
	

	}
	
	
	public static void open(Node n) throws ResourceUnavailableException
	{
		n.open();
		
		for (int i = 0; i < n.getNumDestLinks(); ++i)
		{	final Link destLink = n.getDestLink(i);
			if (destLink != null)
				open(destLink.getDestNode());	// track doesn't matter for this.  TODO: prevent opening same node twice.
		}
	

	}
	
	// TODO: call this method during cleanup.
	public static void close(Node n) throws ResourceUnavailableException
	{
		n.close();
		
		for (int i = 0; i < n.getNumDestLinks(); ++i)
		{	final Link destLink = n.getDestLink(i);
			if (destLink != null)
				close(destLink.getDestNode());	// track doesn't matter for this.  TODO: prevent closing same node twice.
		}
	

	}
	


	// flags for process
	public static final int PROCESS_DEFAULT 		= 0;
	public static final int SUPPRESS_TRACK_READ 	= 0x0001;	// used to be able to re-display a frame.  For example, when we first open a movie, we usually want to get the first frame to see how big it is
	/**
	 * Process a graph starting with n
	 * @param n
	 * @param input the source buffer
	 * sourceTrackNumber only used for demux, and destTrackNumber only used for mux
	 */
	public static int process(final Node n, final Buffer input, final int sourceTrackNumber, final int destTrackNumber, final int flags)
	{
		//if (sourceTrackNumber < 0)
		//	throw new IllegalArgumentException();
		// EOM propagation needs to go all the way to renderer, that's what JMF does.
		
		int result = PlugIn.BUFFER_PROCESSED_OK;
		
		do
		{
//			if (result == PlugIn.INPUT_BUFFER_NOT_CONSUMED)
//			{
//				try
//				{
//					Thread.sleep(20);
//				} catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			result = n.process(input, sourceTrackNumber, destTrackNumber, flags);
			// TODO: sleep on retry??
			
			if (result != PlugIn.BUFFER_PROCESSED_OK && result != PlugIn.INPUT_BUFFER_NOT_CONSUMED) // TODO
			{	if (result == PlugIn.BUFFER_PROCESSED_FAILED)
					logger.warning("BUFFER_PROCESSED_FAILED: " + n.getPlugIn());
				return result;	// TODO: error code return?
			}
			
			for (int i = 0; i < n.getNumDestLinks(); ++i)
			{	
				if (n instanceof DemuxNode && sourceTrackNumber >= 0 && i != sourceTrackNumber)
				{	continue;
				}	
				
				final Link linkDest = n.getDestLink(i);
				if (linkDest != null)
				{	if (n.getOutputBuffer(i) == null)
						throw new NullPointerException("Buffer " + i + " is null, trackNumber=" + sourceTrackNumber + ", flags=" + flags);
					final Buffer b = n.getOutputBuffer(i);
					// TODO: what is the proper place to check for and/or propagate EOM/discard?
//					if (b.isEOM())
//						continue;
					if (b.isDiscard())
					{	
//						try
//						{
//							Thread.sleep(20);
//						} catch (InterruptedException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						continue;
					}
					//linkDest.getDestNode().process(b, -1, linkDest.getDestTrack(), flags);
					if (process(linkDest.getDestNode(), b, -1, linkDest.getDestPin().getTrack(), flags) == PlugIn.BUFFER_PROCESSED_FAILED)
					{	return PlugIn.BUFFER_PROCESSED_FAILED;
					}
				}
			}
		}
		while (result == PlugIn.INPUT_BUFFER_NOT_CONSUMED);
		

		return result;
		
	}
	

	
}
