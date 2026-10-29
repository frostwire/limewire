package net.sf.fmj.filtergraph;

import java.io.IOException;

import javax.media.Buffer;
import javax.media.ResourceUnavailableException;
import javax.media.Track;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;

/**
 * Represents an entire filter graph.
 * @author Ken Larson
 *
 */
public class FilterGraph
{
	
	public static final int PROCESS_DEFAULT 		= FilterGraphProcessor.PROCESS_DEFAULT;
	public static final int SUPPRESS_TRACK_READ 	= FilterGraph.SUPPRESS_TRACK_READ;	// used to be able to re-display a frame.  For example, when we first open a movie, we usually want to get the first frame to see how big it is

	
	private final DemuxNode root;

	public FilterGraph(DemuxNode root)
	{
		super();
		this.root = root;
	}

	public DemuxNode getRoot()
	{
		return root;
	}
	
	public int getNumTracks()
	{
		return root.getNumDestLinks();
	}
	
	public Track[] getTracks()
	{
		return root.getTracks();
	}
	
	public boolean isTrackEnabled(int i)
	{
		return root.getDestLink(i) != null;
	}
	
	public void openExcludeDemux() throws ResourceUnavailableException
	{
		for (int i = 0; i < root.getNumDestLinks(); ++i)
		{	final Link link = root.getDestLink(i);
			if (link != null)
				FilterGraphProcessor.open(link.getDestNode());
		}
	}

    public void closeExcludeDemux() throws ResourceUnavailableException
    {
        for (int i = 0; i < root.getNumDestLinks(); ++i)
        {   final Link link = root.getDestLink(i);
            if (link != null)
                FilterGraphProcessor.close(link.getDestNode());
        }
    }
    
	public void stop() throws IOException
	{
		FilterGraphProcessor.stop(root);
	}
	
	public void start() throws IOException
	{
		FilterGraphProcessor.start(root);
	}
	
	public FilterGraph duplicate()
	{
		return new FilterGraph((DemuxNode) root.duplicate());
	}
	
	public int process(final Buffer input, final int sourceTrackNumber, final int destTrackNumber, final int flags)
	{
		return FilterGraphProcessor.process(root, input, sourceTrackNumber, destTrackNumber, flags);
	}
	
	// TODO: assumes only 1 track of each type
	public int getVideoTrackIndex()
	{
		
		int trackIndex = -1;
		
		for (int i = 0; i < root.getTracks().length; ++i)
		{
			if (!root.getTracks()[i].isEnabled())
				continue;
			if (root.getTracks()[i].getFormat() instanceof VideoFormat)
			{	trackIndex = i;
				break;
			}
		} 
		return trackIndex;
	}
	
	public int getAudioTrackIndex()
	{
		
		int trackIndex = -1;
		for (int i = 0; i < root.getTracks().length; ++i)
		{
			if (!root.getTracks()[i].isEnabled())
				continue;
			if (root.getTracks()[i].getFormat() instanceof AudioFormat)
			{	trackIndex = i;
				break;
			}
		} 
		return trackIndex;
	}
	
	public DataSource getDataOutput()
	{
		// find first mux we can (should not be more than 1 anyway):
		for (int i = 0; i < getNumTracks(); ++i)
		{
			if (!isTrackEnabled(i))
				continue;
			final MuxNode muxNode = (MuxNode) FilterGraphProcessor.getTail(root.getDestLink(i).getDestNode()); 
			
			return muxNode.getMultiplexer().getDataOutput();
		}
		throw new RuntimeException("No mux node found");
	}
	
	
	public RendererNode getRendererNode(int trackIndex)
	{
		if (root.getDestLink(trackIndex) == null)
			return null;
		
		final Node tail = FilterGraphProcessor.getTail(root.getDestLink(trackIndex).getDestNode());
		if (tail instanceof RendererNode)
			return (RendererNode) tail;
		else
			return null;
	}	
	
	public void printToLog()
	{
		FilterGraphPrinter.print(root, 1);
	}
	
	public Node getBeforeTail(int trackNumber)
	{
		return FilterGraphProcessor.getBeforeTail(root.getDestLink(trackNumber).getDestNode());
	}

	
}
