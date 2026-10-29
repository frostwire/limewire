package net.sf.fmj.filtergraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.BadHeaderException;
import javax.media.Codec;
import javax.media.Demultiplexer;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Multiplexer;
import javax.media.PlugInManager;
import javax.media.Renderer;
import javax.media.Track;
import javax.media.control.BufferControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.ObjUtils;

/**
 * TODO: improvements:
 * need to build graph nodes before recursive call, so that 
 * we can instantiate a relaxed match, then negotiate formats when the full
 * path is found.  FilterGraphLink should include the source track info, and not 
 * use the array index.
 * @author Ken Larson
 *
 */
public class FilterGraphBuilder
{
	private static final Logger logger = LoggerSingleton.logger;
	private static final boolean TRACE = true;

	// TODO: this should be set to true, but false is useful for testing (allows playback of less than all tracks if filter graph cannot be built for some tracks).
	private static final boolean REQUIRE_GRAPH_FOR_ALL_TRACKS = false;
	private static final int MAX_FORMAT_NEGOTIATION_ITERATIONS = 100;
	private static final boolean SKIP_NON_FORMAT_CHANGING_CODECS = true; // TODO: this should be false.
	private static final boolean SET_RENDERER_BUFFER_LENGTH = true; // TODO: this should be false.
	private static final boolean FIND_NONSPECIFIC_GRAPH_FIRST = true;	// true=better, but slower
	private static final boolean PRE_LINK = FIND_NONSPECIFIC_GRAPH_FIRST || true;

	private static final int MAX_GRAPH_DEPTH = 10;	// prevent searches from taking too long.
	private static final int TYPICAL_GRAPH_DEPTH = FIND_NONSPECIFIC_GRAPH_FIRST ? MAX_GRAPH_DEPTH : 0;		// disable this feature, too slow.
		// Most graphs have 4 or less nodes: demux, codec, renderer is typical, but we'll add an extra in case there are 2 codecs.
		// for a mux, this  might be demux, codec, encoder, packetizer, mux
	private static final int BEST_FIRST_SEARCH_DEPTH = FIND_NONSPECIFIC_GRAPH_FIRST ? 0 : 8;	// 0 will disable (but this is slow)
	private static final int BEST_FIRST_SEARCH_BREADTH = 5;

	
	public static FilterGraph buildGraphToRenderer(ContentDescriptor contentDescriptor, Demultiplexer demux)
	{
		return buildGraphTo(PlugInManager.RENDERER, contentDescriptor, demux, null, null, null);
	}
	
	public static FilterGraph buildGraphToMux(ContentDescriptor contentDescriptor, Demultiplexer demux, final Multiplexer mux, final Format[] muxInputFormats, final int[] muxInputTrackNumbers)
	{
		return buildGraphTo(PlugInManager.MULTIPLEXER, contentDescriptor, demux, mux, muxInputFormats, muxInputTrackNumbers);
	}
	
	// TODO: throw exception instead of returning null;
	/** demux should be opened and started before calling */
	private static FilterGraph buildGraphTo(int pluginType, ContentDescriptor contentDescriptor, Demultiplexer demux, final Multiplexer mux, final Format[] muxInputFormats, final int[] muxInputTrackNumbers)
	{

			final Track[] tracks;
			try
			{
				tracks = demux.getTracks();
			} catch (BadHeaderException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return null;
			} catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return null;
			} 

	    	if (tracks == null)
	    	{
	    		logger.warning("demux " + demux + ": " + "no tracks");
	    		return null;
	    	}
	    	
	    	final DemuxNode demuxNode = new DemuxNode(contentDescriptor, demux, tracks);


	    	int numEnabledTracks = 0;
			for (int trackIndex = 0; trackIndex < demuxNode.getTracks().length; ++trackIndex)
			{
				final Track t = demuxNode.getTracks()[trackIndex];
				
				if (!t.isEnabled())
					continue;
				++numEnabledTracks;
			}
			
			if (TRACE) logger.fine("Number of tracks: " + demuxNode.getTracks().length + " enabled: " + numEnabledTracks);
			
			int tracksComplete = 0;
			for (int trackIndex = 0; trackIndex < demuxNode.getTracks().length; ++trackIndex)
			{
				final Track t = demuxNode.getTracks()[trackIndex];
				
				if (!t.isEnabled())
				{
					demuxNode.addDestLink(null);
					continue;
				}
				
				
				if (TRACE) logger.fine("Track format for track " + trackIndex + ": " + t.getFormat());
				
				final int muxDestTrack;
				final Format muxInputFormat;
				final MuxNode muxNode;
				final InputPin muxInputPin;
				if (muxInputTrackNumbers == null)
				{	
					// not building to a mux
					muxDestTrack = Pin.NO_TRACK;
					muxInputFormat = null;
					muxNode = null;
					muxInputPin = null;
				}
				else
				{
					// TODO: don't build a mux node for each mux track, should be same node
					muxDestTrack = muxInputTrackNumbers[trackIndex];
					muxInputFormat = muxInputFormats[muxDestTrack];
					muxNode = new MuxNode(mux, null, muxDestTrack);	// mux input format on node will be set later, during negotiation
					muxInputPin = muxNode.getInputPinByTrack(muxDestTrack);
				}
				
				
				// now we have to find a path to the renderer or mux:
				final Node n = findCodecPathTo(demuxNode.getOutputPinByTrack(trackIndex), pluginType, t.getFormat(), demuxNode.getOutputPinByTrack(trackIndex), muxInputPin, muxInputFormat, 0);	
				if (n == null)
				{	demuxNode.addDestLink(null);
					continue;
				}
				
				
				if (!PRE_LINK)
					link(demuxNode.getOutputPinByTrack(trackIndex), (n instanceof MuxNode) ? n.getInputPinByTrack(muxDestTrack) : n.getInputPin(0));
				++tracksComplete;
				
				
			}
			
			if (tracksComplete > 0)
			{	

				if (tracksComplete != numEnabledTracks)
				{	
					if (REQUIRE_GRAPH_FOR_ALL_TRACKS)
					{	logger.warning("Filter graphs could only be built for " + tracksComplete + " of " + numEnabledTracks + " tracks, not proceeding.");
					}
					else
					{
						logger.warning("Filter graphs could only be built for " + tracksComplete + " of " + numEnabledTracks + " tracks, proceeding with " + tracksComplete + " tracks");
					}
				}
				return new FilterGraph(demuxNode);	
			}
	    	
	    
	    return null;
	}


	/** First tries a best-first search.
	 * Then does Breadth-first search by doing depth-first searches of different depths. 
	 * Generally, we want the best-first search to succeed, because it is fast.
	 * Breadth-first can be unacceptably slow. 
	 * 
	 * Standard Best-first can find non-shortest graphs, for example:
	 * 
	 *	net.sf.fmj.media.parser.JavaSoundParser
	 *		[Track 0 of] AudioFormat [LINEAR, 22050.0 Hz, 8-bit, Mono, Unsigned, 22050.0 frame rate, FrameSize=8 bits]
	 *		[Track 0 of] net.sf.fmj.media.codec.audio.RateConverter
	 *			[Track 0 of] AudioFormat [LINEAR, 8000.0 Hz, 8-bit, Mono, Signed, 8000.0 frame rate, FrameSize=8 bits]
	 *			[Track 0 of] net.sf.fmj.media.codec.JavaSoundCodec
	 *				[Track 0 of] AudioFormat [LINEAR, 8000.0 Hz, 8-bit, Mono, Unsigned, 8000.0 frame rate, FrameSize=8 bits]
	 *				[Track 0 of] net.sf.fmj.media.codec.audio.RateConverter
	 *					[Track 0 of] AudioFormat [LINEAR, 8000.0 Hz, 16-bit, Mono, BigEndian, Signed, 8000.0 frame rate, FrameSize=16 bits]
	 *					[Track 0 of] net.sf.fmj.media.codec.audio.ulaw.Encoder
	 *						[Track 0 of] AudioFormat [ULAW, 8000.0 Hz, 8-bit, Mono, Signed, 8000.0 frame rate, FrameSize=8 bits]
	 *						[Track 0 of] com.sun.media.codec.audio.ulaw.Packetizer
	 *							[Track 0 of] AudioFormat [ULAW/rtp, 8000.0 Hz, 8-bit, Mono]
	 * this is because there is no search from the destination format backwards.
	 * 
	 * TODO: this can be unacceptably slow, need to find other ways to improve.
	 * @param f the source format.
	 */
	private static Node findCodecPathTo(final OutputPin rootDemuxOutputPin, int destPlugInType, final Format f, final OutputPin from, final InputPin muxInputPin, final Format muxInputFormat, final int depth)
	{
		
		
		// first, increasing depths of breadth-first search, up to a shorter depth.  
		for (int cutoffDepth = 0; cutoffDepth < TYPICAL_GRAPH_DEPTH; ++cutoffDepth)
		{	//System.out.println("Trying depth " + cutoffDepth);
			Node n = findCodecPathTo(rootDemuxOutputPin, destPlugInType, f, from, new HashSet(), muxInputPin, muxInputFormat, depth, cutoffDepth, -1);
			if (n != null)
				return n;
		}
		
		if (BEST_FIRST_SEARCH_DEPTH > 0)
		{
			// now, increasing depths of best-first search.
			// the reason for not doing a straight best-first search is that suboptimal graphs like the above (in comments) can be found.
			for (int cutoffDepth = 0; cutoffDepth < BEST_FIRST_SEARCH_DEPTH; ++cutoffDepth)
			{
				// first, do best-first search
				Node n = findCodecPathTo(rootDemuxOutputPin, destPlugInType, f, from, new HashSet(), muxInputPin, muxInputFormat, depth, cutoffDepth, BEST_FIRST_SEARCH_BREADTH);
				if (n != null)
				{	
					logger.fine("Graph found with best-first search.");
					return n;
				}
			}
			logger.fine("Graph not found with best-first search, trying incrementally deeper breadth-first searches");
		}
		
		// now, increasing depths of breadth-first search.
		for (int cutoffDepth = TYPICAL_GRAPH_DEPTH; cutoffDepth < MAX_GRAPH_DEPTH; ++cutoffDepth)
		{	//System.out.println("Trying depth " + cutoffDepth);
			Node n = findCodecPathTo(rootDemuxOutputPin, destPlugInType, f, from, new HashSet(), muxInputPin, muxInputFormat, depth, cutoffDepth, -1);
			if (n != null)
				return n;
		}
		
		logger.warning("Filter graph search depth at " + MAX_GRAPH_DEPTH + ", abandoning deeper search");	// just to prevent infinite search.  Not sure this can actually happen, this is a safeguard.
		return null;
	}
	
	
	

	// TODO: format between one node's output and another node's input must be negotiated!
	// A->B: f = a.getOutputFormat(), f = b.setInputFormat(f), if f differs, f = f.setOutputFormat(f), etc!
	// excludeFormatAtDepths is used to incrementally add plug ins which are a dead end - no path to renderer, to avoid retracing our steps.
	// maxBestCodecs is used for best-first search.  Only applies when the muxInputFormat is known.  If not -1, only the top x codecs will be tried.
	// this limits the breadth of the search tree and allows us to go deeper.
	private static Node findCodecPathTo(final OutputPin rootDemuxOutputPin, int destPlugInType, final Format f, final OutputPin from, final Set excludeFormatAtDepths, final InputPin muxInputPin, final Format muxInputFormat, final int depth, final int cutoffDepth, final int maxBestCodecs)
	{
		if (f == null)
			throw new NullPointerException();
  	
		if (depth >= cutoffDepth)	// for our search, we have reached the depth limit.
		{	return null;
		}
		
    	if (excludeFormatAtDepths.contains(new FormatAtDepth(f, depth)))	// already tried this format at this depth, no luck.
    		return null;

			
		if (destPlugInType == PlugInManager.RENDERER)
		{
			final Renderer renderer = findRenderer(f);
			if (renderer != null)
			{	
				final RendererNode rendererNode = new RendererNode(renderer, null); // format will be set in negotiation
				if (PRE_LINK)
					link(from, rendererNode.getInputPin(0));

				final Format fAccepted;
				if (FIND_NONSPECIFIC_GRAPH_FIRST)
				{
					fAccepted = f;
					if (fAccepted != null)
						rendererNode.getInputPin(0).setFormat(fAccepted);
				}
				else
				{
					fAccepted = negotiate(f, rendererNode.getInputPin(0), null, from);
				}
				if (fAccepted != null)
				{	
					if (SET_RENDERER_BUFFER_LENGTH)
					{
						BufferControl bufferControl = (BufferControl) renderer.getControl("javax.media.control.BufferControl");
						if (bufferControl != null)
						{
							bufferControl.setBufferLength(2000);	// TODO: hard-coded for testing
						}
					}
					
					if (FIND_NONSPECIFIC_GRAPH_FIRST)
					{
						// now that we've found a graph with non-negotiated (partially specified) formats,
						// see if we can negotiate all the formats:
						if (!negotiateAllFormats(rootDemuxOutputPin, rendererNode))
							return null;
					}
					
					return rendererNode;
				}
			}
		}
		else if (destPlugInType == PlugInManager.MULTIPLEXER)
		{
			if (PRE_LINK)
				link(from, muxInputPin);

			final Format fAccepted;
			if (FIND_NONSPECIFIC_GRAPH_FIRST)
			{
				fAccepted = f.matches(muxInputFormat) ? f : null;
				if (fAccepted != null)
					muxInputPin.setFormat(fAccepted);
			}
			else
			{
				fAccepted = negotiate(f, muxInputPin, muxInputFormat, from);
			}
			if (fAccepted != null)
			{	
				if (FIND_NONSPECIFIC_GRAPH_FIRST)
				{
					// now that we've found a graph with non-negotiated (partially specified) formats,
					// see if we can negotiate all the formats:
					if (!negotiateAllFormats(rootDemuxOutputPin, muxInputPin.getOwnerNode()))
						return null;
				}
				
				return muxInputPin.getOwnerNode();
			}
			
		}
		else
			throw new IllegalArgumentException();
		
		// if we call again recursively, we will hit the depth limit, so just stop here.  We've already checked
		// whether we can make a terminal node with a renderer/mux.  If we get here, it would have to add
		// a codec below to the chain, before a renderer/mux, which would be 2 nodes.
		if (depth >= cutoffDepth - 1)	// for our search, we have reached the depth limit.
		{	return null;
		}
		
		final List<CodecFormatPair> codecFormatPairs = getCodecFormatPairs(f, muxInputFormat, depth, maxBestCodecs);


	    for (int j = 0; j < codecFormatPairs.size(); ++j)
	    {
	    	final CodecFormatPair codecFormatPair = codecFormatPairs.get(j);
	    	final Codec codec = codecFormatPair.getCodec();
	    	final Format codecOutputFormat = codecFormatPair.getFormat();
	    	
	    	if (TRACE) logger.finer(indent(depth) + "Trying " + codec.getClass().getName() + " with output format: " + codecOutputFormat);
	    	{
	    		
	    		final CodecNode codecNode = new CodecNode(codec, null);	// format will be set in negotiation
				if (PRE_LINK)
					link(from, codecNode.getInputPin(0));
				final Format fAccepted;
    			if (FIND_NONSPECIFIC_GRAPH_FIRST)
    			{
    				fAccepted = f;
    				if (fAccepted != null)
    					codecNode.getInputPin(0).setFormat(fAccepted);
    			}
    			else
    			{
    				fAccepted = negotiate(f, codecNode.getInputPin(0), null, from);
    			}
	    		if (fAccepted == null)
	    	    {	logger.warning("Codec " + codec + " rejected input format (or negotiation failed) " + f);
		    		continue;
		    	}
	    		
	    		// TODO: the output format may be partially unspecified, so it has to be negotiated.
	    		// also, if we are connecting to something with a more specific format (say, a mux), then
	    		// we need to be able to constrain this output format using that.
	    		final Format codecOutputFormatAccepted;
	    		if (FIND_NONSPECIFIC_GRAPH_FIRST)
	    			codecOutputFormatAccepted = codecOutputFormat;
	    		else
	    			codecOutputFormatAccepted = codec.setOutputFormat(codecOutputFormat);	// TODO: this should be set only in negotiation
	    		if (codecOutputFormatAccepted == null)
	    	    {	logger.warning("Codec " + codec + " rejected output format " + codecOutputFormat);
	    			continue;
	    	    }
	    		
	    		if (false && codecOutputFormatAccepted.equals(f))
	    		{	if (TRACE) logger.finer(indent(depth) + (SKIP_NON_FORMAT_CHANGING_CODECS ? "" : "NOT ") + "Skipping accepted Codec output format, same as input format: " + codecOutputFormatAccepted);
		    		if (SKIP_NON_FORMAT_CHANGING_CODECS)
		    			continue;	// no need to have a codec that does not change formats.  this can happen in the case of 
		    						// something like com.sun.media.codec.audio.rc.RateCvrt, which will offer an output format
		    						// the same as an input format.
	    		}
	    		
	    		if (TRACE) logger.finer(indent(depth) + "ACCEPT " + codecOutputFormatAccepted);
	    		
	    		// for any depth between this one and the cutoff depth, we no longer need to search.
	    		for (int i = depth ; i <= cutoffDepth; ++i)
	    		{	excludeFormatAtDepths.add(new FormatAtDepth(f, i));
	    		}
	    		final Node tail = findCodecPathTo(rootDemuxOutputPin, destPlugInType, codecOutputFormatAccepted, codecNode.getOutputPin(0), excludeFormatAtDepths, muxInputPin, muxInputFormat, depth + 1, cutoffDepth, maxBestCodecs);
	    		if (tail != null)
	    		{	
	    			if (!PRE_LINK)
	    				link(codecNode.getOutputPin(0), tail instanceof MuxNode ? muxInputPin : tail.getInputPin(0));
	    			return codecNode;
	    		}
	    		
	    	}
	    }
	    
	    return null;
		
	}
	
	private static void link(OutputPin outputPin, InputPin inputPin)
	{
		// pre-existing link - if there, remove
		final Link existingLink = outputPin.getOwnerNode().getDestLink(outputPin);
		if (existingLink != null)
			outputPin.getOwnerNode().removeDestLink(existingLink);
		
		final Link link = new Link(outputPin, inputPin);
		outputPin.getOwnerNode().addDestLink(link);
		// TODO: add source link on inputPin.getOwnerNode
	}

	/**
	 * Attempts to negotiate all partially specified formats in the graph.
	 * @return true if negotiation successful, false otherwise.
	 */
	private static boolean negotiateAllFormats(OutputPin rootDemuxOutputPin, Node terminalNode)
	{
		Link link = rootDemuxOutputPin.getOwnerNode().getDestLink(rootDemuxOutputPin);
		
		while (link != null)
		{
			
			final Format formatMustMatch;
			if (link.getDestNode() instanceof MuxNode)
				formatMustMatch = link.getDestPin().getFormat();
			else
				formatMustMatch = null;
			if (negotiate(link.getDestPin().getFormat(), link.getDestPin(), formatMustMatch, link.getSourcePin()) == null)
				return false;
			
			if (link.getDestNode().getNumDestLinks() == 0)
				break;
			link = link.getDestNode().getDestLink(0);
		}
		
		return true;
	}
	
	/** @param formatMustMatch if not null, the format must match this.  Used for mux input pins only.
	 * returns null if negotiation failed.  Sets format on muxInputPin if successful. */
	private static Format negotiate(final Format initialFormat, InputPin inputPin, Format formatMustMatch, OutputPin outputPin)
	{
		Format f = initialFormat; // f will be the curent format we are attempting to use
		
		if (formatMustMatch != null)
		{	if (!formatMustMatch.matches(f))
			{	return null;	// TODO: do this in the loop as well?
			}
		}
		
		int iterations = 0;
		while (true)
		{
			if (f == null)
				return null;
			if (iterations >= MAX_FORMAT_NEGOTIATION_ITERATIONS)
			{	// just a sanity check, not sure it can or will ever happen.
				logger.warning("negotiate iterated " + MAX_FORMAT_NEGOTIATION_ITERATIONS + " times, probably stuck in infinite loop - abandoning format negotiation");
				logger.warning("src=" + outputPin.getOwnerNode().getPlugIn());
				logger.warning("dest=" + inputPin.getOwnerNode().getPlugIn());
				logger.warning("f=" + f);
				return null;
			}
			++iterations;
			
			final Format f2 = inputPin.setPlugInInputFormat(f);
			if (f2 == null)	
			{	logger.warning("Input format rejected by " + inputPin.getOwnerNode().getPlugIn() + ": " + f);	// this shouldn't happen, since we chose which nodes to try based on the input formats they offered.
				return null;
			}
			final Format f3;
			if (outputPin.getOwnerNode().getPlugIn() instanceof Codec)
			{
				f3 = outputPin.setPlugInOutputFormat(f2);
			}
			else
			{	return f2;	// no other plug ins besides codecs can have their output formats set, negotiation successful
			}
			
			if (f2.equals(f3))
			{	
				return f3;	// negotiation successful
			}
			
			f = f3; // try again with f3.

		}

	}

	
	// does not check any deeper than demux node.
	public static Demultiplexer getSourceCompatibleDemultiplexer(DataSource source)
	{
		if (TRACE) logger.fine("Content type: " + source.getContentType());
	    
	    // loop through demuxs for content type:
		final ContentDescriptor contentDescriptor = new ContentDescriptor(source.getContentType());
	    final Vector demuxs = PlugInManager.getPlugInList(contentDescriptor, null, PlugInManager.DEMULTIPLEXER);
	    if (TRACE) logger.fine("Num demux: " + demuxs.size());
	    for (int i = 0; i < demuxs.size(); ++i)
	    {
	    	
	    	final String demuxClassName = (String) demuxs.get(i);
	    	if (TRACE) logger.fine("Demux class name found: " + demuxClassName);

	    	final Demultiplexer demux = (Demultiplexer) instantiate(demuxClassName);
	    	if (demux == null)
	    		continue;
	    	
	    	
    		try
			{
				demux.setSource(source);
			} catch (IncompatibleSourceException e)
			{
				logger.warning("Skipping demux " + demuxClassName + ": " + e);
				continue;
			} catch (IOException e)
			{
				logger.warning("Skipping demux " + demuxClassName + ": " + e);
				continue;
			}
			
			return demux;
	    	
	    }
	    
	    return null;
	}
	
	// does not set renderer's input format or open
	private static Renderer findRenderer(final Format f)
	{
		final Vector renderers = PlugInManager.getPlugInList(f, null, PlugInManager.RENDERER);
		
		if (renderers.size() == 0)
			if (TRACE) logger.fine("No renderers found for: " + f);
		
		for (int k = 0; k < renderers.size(); ++k)
	    {
	    	final String rendererClassName = (String) renderers.get(k);
	    	if (TRACE) logger.fine("Found renderer for " + f + ": " + rendererClassName);
	    }
		
		for (int k = 0; k < renderers.size(); ++k)
	    {
	    	final String rendererClassName = (String) renderers.get(k);
	    	if (TRACE) logger.fine("Trying renderer for " + f + ": " + rendererClassName);
	    	
	    	// TODO: instantiate renderer, take first successful one for each track.
	    	// Or, how do we pick the "best" one?  // how do we pick the "best" format?
	    	
	    	final Renderer renderer = (Renderer) instantiate(rendererClassName);
	    	if (renderer == null)
	    		continue;
	    	
			return renderer;
	    	
	    	
	    }
		
		return null;
	}
	
	// does not set mux's input format or open
	public static Multiplexer findMux(final Format destFormat)
	{
		// TODO: normally we would pass in f as the first parameter.  But all the mux's are registered with
		// an empty input format array.  Not sure whether getPlugInList should deal with this.
		final Vector muxs = PlugInManager.getPlugInList(null, destFormat, PlugInManager.MULTIPLEXER);
		
		if (muxs.size() == 0)
		{	if (TRACE) logger.fine("No muxs found for: " + destFormat);
			return null;
		}
		
		if (TRACE)
		{
			for (int k = 0; k < muxs.size(); ++k)
		    {
		    	final String muxClassName = (String) muxs.get(k);
		    	logger.fine("Found mux for " + destFormat + ": " + muxClassName);
		    }
		}
		
		for (int k = 0; k < muxs.size(); ++k)
	    {
	    	final String muxClassName = (String) muxs.get(k);
	    	if (TRACE) logger.fine("Trying mux for " + destFormat + ": " + muxClassName);
	    	
	    	// TODO: instantiate mux, take first successful one for each track.
	    	// Or, how do we pick the "best" one?  // how do we pick the "best" format?
	    	
	    	final Multiplexer mux = (Multiplexer) instantiate(muxClassName);
	    	if (mux == null)
	    		continue;

			return mux;
	    }
		
		return null;
	}

	/**
	 * Get all multiplexers 
	 */
	public static List<Multiplexer> findMuxs()
	{
		// TODO: normally we would pass in f as the first parameter.  But all the mux's are registered with
		// an empty input format array.  Not sure whether getPlugInList should deal with this.
		final Vector muxs = PlugInManager.getPlugInList(null, null, PlugInManager.MULTIPLEXER);
		
		if (TRACE)
		{
			if (muxs.size() == 0)
			{	logger.fine("No muxs found");
				return new ArrayList<Multiplexer>();
			}
		}
		
		for (int k = 0; k < muxs.size(); ++k)
	    {
	    	final String muxClassName = (String) muxs.get(k);
	    	if (TRACE) logger.fine("Found mux: " + muxClassName);
	    }
		
		final List<Multiplexer> result = new ArrayList<Multiplexer>();
		
		for (int k = 0; k < muxs.size(); ++k)
	    {
	    	final String muxClassName = (String) muxs.get(k);
	    	if (TRACE) logger.fine("Trying mux: " + muxClassName);
	    	
	    	final Multiplexer mux = (Multiplexer) instantiate(muxClassName);
	    	if (mux == null)
	    		continue;

	    	result.add(mux);
	    }
		
		return result;
	}

	
	private static final List<CodecFormatPair> getCodecFormatPairs(Format f, Format muxInputFormat, int depth, int maxBestCodecs)
	{
	    final Vector codecs = PlugInManager.getPlugInList(f, null, PlugInManager.CODEC);
	    
	    for (int j = 0; j < codecs.size(); ++j)
	    {
	    	final String codecClassName = (String) codecs.get(j);
	    	if (TRACE) logger.finest(indent(depth) + "Found codec for " + f + ": " + codecClassName);
	    }
	    
	    // first, put all the possible codecs and output formats as pairs in a list of CodecFormatPair, which we
	    // will then sort so that we can try the best ones first:
	    final List<CodecFormatPair> codecFormatPairs = new ArrayList<CodecFormatPair>();	
	    
	    for (int j = 0; j < codecs.size(); ++j)
	    {
	    	final String codecClassName = (String) codecs.get(j);
	    	
	    	if (TRACE) logger.finer(indent(depth) + "Trying " + codecClassName);
	    	
	    	final Codec codec = (Codec) instantiate(codecClassName);
	    	if (codec == null)
	    		continue;
	    		    	
	    	final Format[] codecOutputFormats = codec.getSupportedOutputFormats(f);
	    	
	    	if (TRACE && logger.isLoggable(Level.FINEST))
	    	{
		    	for (int codecOutputFormatIndex = 0; codecOutputFormatIndex < codecOutputFormats.length; ++codecOutputFormatIndex)
		    	{
		    		final Format codecOutputFormat = codecOutputFormats[codecOutputFormatIndex];
		    		logger.finest(indent(depth) + "Found Codec output format: " + codecOutputFormat);
		    	}
	    	}
	    	
	    	for (int codecOutputFormatIndex = 0; codecOutputFormatIndex < codecOutputFormats.length; ++codecOutputFormatIndex)
	    	{
	    		final Format codecOutputFormat = codecOutputFormats[codecOutputFormatIndex];
	    		if (codecOutputFormat == null)
	    		{	logger.finer(indent(depth) + "Skipping null Codec (" + codec.getClass() + ") output format, input format: " + f);
	    			continue;
	    		}
	    		if (codecOutputFormat.equals(f))
	    		{	if (TRACE) logger.finest(indent(depth) + (SKIP_NON_FORMAT_CHANGING_CODECS ? "YES " : "NOT ") + "Skipping Codec output format, same as input format: " + codecOutputFormat);
	    			if (!FIND_NONSPECIFIC_GRAPH_FIRST && SKIP_NON_FORMAT_CHANGING_CODECS)
	    				continue;	// no need to have a codec that does not change formats.  this can happen in the case of 
		    						// something like com.sun.media.codec.audio.rc.RateCvrt, which will offer an output format
		    						// the same as an input format.
	    		}
	    		
	    		// instantiate a new copy of the codec for each pair.
	    		codecFormatPairs.add(new CodecFormatPair((Codec) instantiate(codecClassName), codecOutputFormat));
	    	}
	    	
	    }
	    
	    // now that we have all of the codec/format pairs, sort them (if we know what format we are trying to reach)
	    if (muxInputFormat != null)
	    {	Collections.sort(codecFormatPairs, new CodecFormatPairProximityComparator(muxInputFormat));
	    
//		    for (int j = 0; j < codecFormatPairs.size(); ++j)
//		    {
//		    	final CodecFormatPair codecFormatPair = (CodecFormatPair) codecFormatPairs.get(j);
//		    	final Codec codec =codecFormatPair.getCodec();
//		    	final Format codecOutputFormat = codecFormatPair.getFormat();
//		    	
//		    	if (TRACE) logger.fine(indent(depth) + j + ". Will try " + codec.getClass().getName() + " with output format: " + codecOutputFormat);
//	
//		    }
		    
	    	// best-first enabled.
	    	if (maxBestCodecs > 1)
		    {	while (codecFormatPairs.size() > maxBestCodecs)
		    		codecFormatPairs.remove(maxBestCodecs);
		    }
	    	
	    	if (TRACE && logger.isLoggable(Level.FINER))
	    	{
			    for (int j = 0; j < codecFormatPairs.size(); ++j)
			    {
			    	final CodecFormatPair codecFormatPair = codecFormatPairs.get(j);
			    	final Codec codec = codecFormatPair.getCodec();
			    	final Format codecOutputFormat = codecFormatPair.getFormat();
			    	
			    	logger.finer(indent(depth) + j + ". Will try " + codec.getClass().getName() + " with output format: " + codecOutputFormat);
		
			    }
	    	}
	    }
	    return codecFormatPairs;
	}   
	

	/**
	 * For pretty-printing.
	 */
	private static String indent(int depth)
	{
		final StringBuffer b = new StringBuffer();
		for (int i = 0; i < depth; ++i)
			b.append(' ');
		return b.toString();
	}
	

	
	private static Object instantiate(String className)
	{
		final Class clazz;
		try
		{
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e)
		{
			logger.warning("Unable to instantiate " + className + ": " + e.getMessage());
			return null;
		}
    	try
		{
			return clazz.newInstance();
		} catch (InstantiationException e)
		{
			logger.warning("Unable to instantiate " + className + ": " + e.getMessage());
			return null;
		} catch (IllegalAccessException e)
		{
			logger.warning("Unable to instantiate " + className + ": " + e.getMessage());
			return null;
		}
		catch (Throwable e)
		{
			logger.log(Level.SEVERE, "Unable to instantiate " + className + ": " + e.getMessage(), e);
			return null;
		}
		
	}


	/** A combination of Format and depth, used to keep track of where we have already searched. */
	private static class FormatAtDepth
	{
		private final Format f;
		private final int depth;
		public FormatAtDepth(final Format f, final int depth)
		{
			super();
			this.f = f;
			this.depth = depth;
		}
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof FormatAtDepth))
				return false;
			
			FormatAtDepth oCast = (FormatAtDepth) obj;
			return 	oCast.depth == this.depth && 
					ObjUtils.equal(oCast.f, this.f);

		}
		@Override
		public int hashCode()
		{
			int result = depth;
			if (f != null)
				result += f.toString().hashCode();	// Format.hashCode is not implemented in JMF, so let's not call it.
			return result;
		}
		
		
	}

}
