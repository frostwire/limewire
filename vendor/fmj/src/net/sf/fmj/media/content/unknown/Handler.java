package net.sf.fmj.media.content.unknown;

import java.awt.Component;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.BadHeaderException;
import javax.media.Buffer;
import javax.media.Clock;
import javax.media.ClockStoppedException;
import javax.media.Codec;
import javax.media.Control;
import javax.media.Demultiplexer;
import javax.media.Duration;
import javax.media.Format;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.InternalErrorEvent;
import javax.media.Multiplexer;
import javax.media.NotConfiguredError;
import javax.media.NotRealizedError;
import javax.media.PlugIn;
import javax.media.Renderer;
import javax.media.ResourceUnavailableException;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.Track;
import javax.media.UnsupportedPlugInException;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.renderer.VideoRenderer;

import net.sf.fmj.filtergraph.FilterGraph;
import net.sf.fmj.filtergraph.FilterGraphBuilder;
import net.sf.fmj.filtergraph.MuxNode;
import net.sf.fmj.filtergraph.Node;
import net.sf.fmj.filtergraph.CodecNode;
import net.sf.fmj.filtergraph.RendererNode;
import net.sf.fmj.gui.controlpanelfactory.ControlPanelFactorySingleton;
import net.sf.fmj.media.AbstractProcessor;
import net.sf.fmj.utility.LoggerSingleton;

import com.lti.utils.synchronization.CloseableThread;

/**
 * The main handler for media.  Builds a playback filter graph and starts threads to process it.
 * Note that while this is actually a Player, it is also a Processor.  The unknown processor simply
 * extends this class and puts it in a different mode (PROCESSOR).
 * 
 * @author Ken Larson
 *
 */
public class Handler extends AbstractProcessor
{
	// TODO: after starting the demux in order to get tracks, etc, we should stop it again.
	// done in doPlayerRealize, and getTrackControls, and in TrackControl methods.
	// TODO: does JMF start the demux for this purpose?
	
	// we can use this as a player or processor
	protected static final int PLAYER = 1;
	protected static final int PROCESSOR = 2;
	
	private final int mode;
	
	// TODO: AbstractPlayer handles multiple controllers, so what we need to do is create a Controller for each track.
	
	private static final Logger logger = LoggerSingleton.logger;


	
	private boolean prefetchNeeded = true;
	private Time duration;

	private Component visualComponent;
	private TrackThread[] trackThreads;
	
	private static final boolean TRACE = true;
	
	private FilterGraph filterGraph;
	private Demultiplexer demux;
	private int numTracks;
	private Track[] tracks;
	private Multiplexer mux;	// only for processor
	private Format[] muxInputFormats;	// only for processor, indexed by mux track number, not demux track number
	private int[] muxInputTrackNumbers; // only for processor - array indices are demux track numbers, values are mux input track numbers.
	private Time demuxDuration = DURATION_UNKNOWN;
	
	
	public Handler()
	{	this(PLAYER);
	}
	
    public Handler(final int mode)
	{
		super();
		this.mode = mode;
	}

	@Override
	public void setSource(DataSource source) throws IncompatibleSourceException
	{

    	// setSource and getDuration on a demux at this stage.
    	if (TRACE) logger.fine("DataSource: " + source);

    	// TODO: The original FMJ code would build the entire filter graph here.
    	// this is not what JMF does, JMF builds the filter graph in realize.
    	// The advantage of the old way is that if there were multiple demuxes that 
    	// matched, it would try them all.  Now, it finds the first demux that it can 
    	// successfully set the source on, and uses that.  The graph is then built in
    	// realize, and if that fails, the architecture will not try any other demux's for 
    	// this handler.
    	
    	// This causes problems where there are demultiplexers that fail to realize.
    	// an example is com.ibm.media.parser.video.MpegParser.  The immediate solution
    	// was to de-register that parser for mpeg audio.
    	
    	demux = FilterGraphBuilder.getSourceCompatibleDemultiplexer(source);
    	if (demux == null)
    		throw new IncompatibleSourceException("Unable to build filter graph for: " + source);
    	

  
    	demuxDuration = demux.getDuration();	// JMF calls this at this stage, so we might as well too, and use it in getDuration().
    	
		
	    super.setSource(source);
	    
	}
	
	/** can only be called after demux is open and started. */
	private boolean getDemuxTracks()
	{
		if (tracks != null)
			return true;
    	try
		{
    		if (!openAndStartDemux())
    			return false;
    		tracks = demux.getTracks();
			numTracks = tracks.length;
		} catch (BadHeaderException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			return false;
		} catch (IOException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			return false;
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "" + e, e);
			return false;
		}
		return true;
	}
	
	private boolean demuxOpenedAndStarted;
	
	private boolean openAndStartDemux()
	{
		if (demuxOpenedAndStarted)
			return true;
		try
		{
	    	// we need to open the demux before we can get the tracks.
	    	// we only need the tracks if we are a processor, so perhaps this should be deferred.
	    	// this could be done in buildMux, and in doRealize (only needs to be done once)
			demux.open();  // TODO: should this happen here or in realize?  For some demultiplexers, getTracks returns null if it is not open.  For example, com.sun.media.parser.RawPullBufferParser (project jipcam)
	
			demux.start();
			
			demuxOpenedAndStarted = true;
			return true;
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, "" + e, e);
			
			// TODO: not sure if JMF closes the demux in this case.
			// is it the demux's responsibility to clean up in the event of an exception?  or ours?
			try
			{
				closeDemux();
			}
			catch (Throwable t)
			{
				logger.log(Level.WARNING, "" + t, t);
			}
			return false;
		}
	}
	
	private void closeDemux()
	{
		if (demux != null)
			demux.close();
		demuxOpenedAndStarted = false;
	}
    
	//@Override
	@Override
	public void doPlayerClose()
	{
        try
        {
            // mgodehardt: this will call close on the filter graph objects
            if ( null != filterGraph )
            {
                filterGraph.closeExcludeDemux();
            }
        }
        catch (ResourceUnavailableException e)
        {
            logger.log(Level.WARNING, "" + e, e);
            postControllerErrorEvent("" + e);
        }
        
		closeDemux();
		// TODO
		logger.info("Handler.doPlayerClose");
	}

	//@Override
	@Override
	public boolean doPlayerDeallocate()
	{
		logger.info("Handler.doPlayerDeallocate");
		return true;
	}

	//@Override
	@Override
	public boolean doPlayerPrefetch()
	{
		if( ! prefetchNeeded ) return true;
		 
		
		
		prefetchNeeded = false;
		
		return true;
	}
	
	private void addCodecChainControls(Node node)
	{
		for (int i=0; i<node.getNumDestLinks(); i++)
		{
			Node dest = node.getDestLink(i).getDestNode();
			if ( dest instanceof CodecNode )
			{
				Codec c = ((CodecNode)dest).getCodec();
				final Object[] controls = c.getControls();
				if (controls != null)
				{
					for (Object o : controls)
					{
						addControl((Control) o);
					}
				}
				
				if ( null != dest )
				{
					addCodecChainControls(dest);
				}
			}
			else if ( dest instanceof MuxNode )
			{
				Multiplexer m = ((MuxNode)dest).getMultiplexer();
				final Object[] controls = m.getControls();
				if (controls != null)
				{
					for (Object o : controls)
					{
						addControl((Control) o);
					}
				}
			}
		}
	}
	
	@Override
	public boolean doPlayerRealize()
	{
		// TODO: in the event of errors, post more specific error events, such as ResourceUnavailableEvent.
		try
	    {
			if (!openAndStartDemux())
			{	postControllerErrorEvent("Failed to openAndStartDemux");	// TODO: we could get a more specific error, like resource not available, from the function.
				return false;
			}
			if (mode == PLAYER)
			{	filterGraph = FilterGraphBuilder.buildGraphToRenderer(new ContentDescriptor(getSource().getContentType()), demux);
			}
			else
			{	buildMux();
				filterGraph = FilterGraphBuilder.buildGraphToMux(new ContentDescriptor(getSource().getContentType()), demux, mux, muxInputFormats, muxInputTrackNumbers);
			}
	    }
	    catch (Exception e)
	    {	logger.log(Level.WARNING, "" + e, e);
	    	closeDemux();
	    	postControllerErrorEvent("" + e);
	    	return false;
	    }
		    
		if (filterGraph == null)
		{	logger.fine("unable to find a filter graph to connect from demux to renderer/mux");	// TODO: give details
			closeDemux();
			// TODO: base class says we should post these events.  TODO: do this everywhere in FMJ in a controller
			// where we fail to realize.
			postControllerErrorEvent("unable to find a filter graph to connect from demux to renderer/mux");
			return false;
		}	
		
		
		
		if (TRACE)
		{
			logger.fine("Filter graph:");
			filterGraph.printToLog();
		}

		final int videoTrackIndex = filterGraph.getVideoTrackIndex();
		
		if (mode == PLAYER && videoTrackIndex >= 0)	// if it has a video track
		{
		
			final RendererNode rendererNode = filterGraph.getRendererNode(videoTrackIndex);
			
			if (rendererNode != null) {
				final VideoRenderer videoRenderer = (VideoRenderer) rendererNode.getRenderer();
				final VideoFormat videoRendererInputFormat = (VideoFormat) rendererNode.getInputFormat();
				// TODO: we need to start the demux
				visualComponent = videoRenderer.getComponent();
				visualComponent.setSize(videoRendererInputFormat.getSize());
				//logger.fine("Video size: " + videoRendererInputFormat.getSize());
				videoRenderer.setBounds(new Rectangle(videoRendererInputFormat.getSize()));
			}
		}
		
		// TODO:
		// Sun's AudioRenderer implements Prefetchable, Drainable, Clock
		// This causes their handler to call some extra methods during initialization.
		// here we have a somewhat hard-coded attempt to recreate this.
		// For one, if it is Prefetchable, then we keep passing it buffers while isPrefetching is true,
		// then call syncStart (which is a Clock method)
		// TODO: determine which of these is to be called in our prefetch, realize, start, etc.
		final int audioTrackIndex = filterGraph.getAudioTrackIndex();
		
		if (mode == PLAYER && audioTrackIndex >= 0)	// if it has a audio track
		{
			final RendererNode rendererNode = filterGraph.getRendererNode(audioTrackIndex);
			if (rendererNode != null) {
				
				final Renderer renderer = rendererNode.getRenderer();
				if (renderer instanceof Clock)
				{
					final Clock rendererAsClock = (Clock) renderer;
					try
					{
						TimeBase timeBase = rendererAsClock.getTimeBase();
						
						// With JMF, this ends up as a com.sun.media.renderer.audio.AudioRenderer$AudioTimeBase@49bdc9d8
						// TODO: what do we do in between getting and setting?
						// probably what we need to do is somehow use this clock as our clock.
						// TODO: this is starting to make sense to me.  An audio renderer differs from a video renderer in that
						// the audio renderer has to determine time, therefore it is the master clock.  The video has to be synched with
						// the audio, not the other way around.
						
						rendererAsClock.setTimeBase(timeBase);	// this seems unnecessary, but it does cause the audio renderer to set its master clock.
					} catch (IncompatibleTimeBaseException e)
					{
						logger.log(Level.WARNING, "" + e, e);
						postControllerErrorEvent("" + e);
						return false;
					}
				}
			}	
		}
		
		try
		{
			// root was already opened in setDataSource.
			// TODO: JMF calls open on the parser during realize
			filterGraph.openExcludeDemux();
		}
		catch (ResourceUnavailableException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			postControllerErrorEvent("" + e);
			return false;
		}
		
		// now that nodes are open, add controls:
		
		// mgodehardt: query processor output datasource for controls
		// kenlars99: JMF does not include DataSource controls in its own list of controls.
		/*{
			final Object[] controls = getSource().getControls();
			if (controls != null)
			{
				for (Object o : controls)
					addControl((Control) o);
			}
		}*/
		
		// add controls from demux
		{
			final Object[] controls = filterGraph.getRoot().getDemux().getControls();
			if (controls != null)
			{
				for (Object o : controls)
				{
					addControl((Control) o);
				}
			}
		}
		
		// add all controls from the codec chain ( inclusive mux )
		addCodecChainControls(filterGraph.getRoot());
		
		// add all controls from renderers
		if ( mode == PLAYER )
		{
			for (int trackIndex = 0; trackIndex < filterGraph.getNumTracks(); ++trackIndex)
			{
				final RendererNode rendererNode = filterGraph.getRendererNode(trackIndex);
				if (rendererNode != null) {
					final Renderer renderer = rendererNode.getRenderer();
					// add any controls from renderer
					final Object[] controls = renderer.getControls();
					if (controls != null)
					{
						for (Object o : controls)
						{	addControl((Control) o);
						
							if (o instanceof GainControl)
								setGainControl((GainControl) o);
						}
						
					}
				}	
				
			}
		}
		
		// TODO: when to call open and when to call start?
		if (TRACE) logger.fine("Starting filter graph(s)");
		
		try
		{
			filterGraph.start();
		} catch (IOException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			postControllerErrorEvent("" + e);
			return false;
		}
		
		// TODO: the track threads need to coordinate better.  Perhaps each track thread
		// should really be its own controller, and this controller is a multi-controller (See EJMF example).
		trackThreads = new TrackThread[filterGraph.getNumTracks()];
		
	
		for (int i = 0; i < filterGraph.getNumTracks(); ++i)
		{
			if (filterGraph.isTrackEnabled(i))
			{	
			    final FilterGraph filterGraphCopy = filterGraph.duplicate();
				trackThreads[i] = new TrackThread(filterGraphCopy, i, videoTrackIndex == i ? FilterGraph.SUPPRESS_TRACK_READ : FilterGraph.PROCESS_DEFAULT); // use a copy of the graph for each thread, so that they don't interfere with each other.
				
				// mgodehardt: may block the blocking realize forever, if input stream is closed before data is read
				/*if (videoTrackIndex == i)	// if it has a video track - read the first video frame
				{	final int result = filterGraphCopy.process(null, videoTrackIndex, -1, FilterGraph.PROCESS_DEFAULT); // doesn't work because we have to use the actual filter graph clone that is used for the track.
					if ((result & PlugIn.BUFFER_PROCESSED_FAILED) != 0)
					{
						logger.log(Level.WARNING, "BUFFER_PROCESSED_FAILED");
						postControllerErrorEvent("BUFFER_PROCESSED_FAILED");
						return false;
					}
				}*/
			}
		}
		
//		if (videoTrackIndex >= 0)	// if it has a video track - read the first video frame
//		{	FilterGraph.process(root, null, videoTrackIndex, FilterGraph.PROCESS_DEFAULT); // doesn't work because we have to use the actual filter graph clone that is used for the track.
//		}
//				
		// TODO: we need the first frame (video only) to size the window right.
		//readAndProcessNextFrame();
		
		// we have to wait for the component to be visible to start really playing, and do this in another thread
	    
		
		return true;
	}
	
	
    @Override
	public Component getControlPanelComponent() {
        Component c = super.getControlPanelComponent();

        if( c == null ) {
            c = ControlPanelFactorySingleton.getInstance().getControlPanelComponent(this);
            setControlPanelComponent(c);
        }

        return c;
    }



	private class TrackThread extends CloseableThread
	{
		
		private final int trackNumber;
		private final FilterGraph filterGraph;
		private final int firstFlags; 
		private long nanoseconds;	// TODO: set this properly
		private boolean eom = false;
		   		
		public TrackThread(FilterGraph filterGraph, int trackNumber, int firstFlags)
		{
			super();
			setName("TrackThread for track " + trackNumber);
			setDaemon(true);
			this.filterGraph = filterGraph;
			this.trackNumber = trackNumber;
			this.firstFlags = firstFlags;
		}
		
		// mgodehardt: must be interrupted otherwise deadlock
		
		/*@Override
		public void close()
		{
			// don't call super.close(), which interrupts.
			// it seems risky to be interrupting the thread, which could be in any
			// possible state.  We would basically lose frames this way.
			// so the downside of not interrupting is that it may take longer than
			// expected to fully stop.
			
			// Interrupting would leave the media in an ungraceful state.
			// we may have buffers that are read, that will never be rendered, etc.
			// we may want to find a way to do this without interruption.
			
			// TODO: what we really want to do is keep any buffers around, and simply 
			// continue processing them if we resume.
			// really, we want to basically suspend the thread and resume it.
			
			setClosing();
		}*/

		// returns false on EOM.
		private Buffer readAndProcessNextFrame(int flags)
		{
//			 TODO: we need to honor isEnabled when processing tracks
			final int result = filterGraph.process(null, trackNumber, -1, flags);
			if ((result & PlugIn.BUFFER_PROCESSED_FAILED) != 0)
			{	throw new RuntimeException("BUFFER_PROCESSED_FAILED");	// TODO: what is the right thing to do?
			}
			
			final Buffer b = filterGraph.getRoot().getOutputBuffer(trackNumber);
			if (b == null)
				throw new NullPointerException();
			return b;
			
		}
		
		@Override
		public void run()
		{
			try
			{
				
				final float rate = getRate(); // what if this changes during playback?	It won't because we'll get stopped and started in that case.
	
				try
				{
					
					
					if (!filterGraph.isTrackEnabled(trackNumber))
						return;	 // this track is disabled, nothing to do.
					final Node penultimateNode = filterGraph.getBeforeTail(trackNumber);
					
					boolean first = true;
					boolean inputEOM = false;	// set to true when the input buffer reaches EOM, which may be before the last buffer in the chain reaches EOM.
					while (true)
					{
						if (isClosing())
							return;
						final int flags;
						if (first)
							flags = firstFlags;	// if firstFlags == FilterGraph.SUPPRESS_TRACK_READ, first frame has already been read, only needs redisplay
						else if (inputEOM)
							flags = FilterGraph.SUPPRESS_TRACK_READ;
						else
							flags = FilterGraph.PROCESS_DEFAULT;
						Buffer b = readAndProcessNextFrame(flags); 
						if (isClosing())
							return;
						if (first)
						{	first = false;
						}
						if (b.isEOM())
							inputEOM = true;	// we continue processing, however, until the final buffer in the chain gets EOM.
												// this is needed to handle the case where there is residual data in the codecs.
												//TODO: how close is this to what JMF does?  How does JMF handle this?
												// if we don't do this, the effect is that codecs that are not 1:1 in terms of buffers will have any
												// data buffered up in the codecs cut off.  
						
						if (b.isDiscard())
							continue;
						

						
						if (!(penultimateNode instanceof RendererNode) && !(penultimateNode instanceof MuxNode))
						{
							// we need to sleep based on the time in the processed data, not the input data.  
							// if the graph goes demux->renderer, then the output buffer of the demux,
							// which is set to b above, is what we use to check.
							b = penultimateNode.getOutputBuffer(0);
							// TODO: this can cause a NPE below if b is null (not sure how it could be null)
	
							if (b == null)
								continue;	// this can be null if it has not been set yet, if codecs earlier in the chain have been 
											// returning INPUT_BUFFER_NOT_CONSUMED or OUTPUT_BUFFER_NOT_FILLED.
						}
						
						if (b.isEOM())	// TODO: is there data in an eom buffer?
							break;
						if (b.isDiscard())
							continue;
						
			            //  Update the video time
			            nanoseconds = b.getTimeStamp();
					}
					
					eom = true;
					checkAllTracksEOM();
				}
				catch (Exception e)
				{
					logger.log(Level.WARNING, "" + e, e);
					postControllerErrorEvent("" + e);
				}
				

	        
			}
			finally
			{
				setClosed();
			}
		}

		public boolean isEOM()
		{
			return eom;
		}
		
		
	}
	
	/**
	 * If all tracks have reached eom, then we post an end of media event.
	 *
	 */
	private void checkAllTracksEOM()
	{
		// TODO: synchronize on something?
		for (int i = 0; i < trackThreads.length; ++i)
		{
			if (trackThreads[i] == null)
				continue;
			if (!trackThreads[i].isEOM())
				return;
		}
		
//		 TODO: not once for each track
        try {
            endOfMedia();
        } catch(ClockStoppedException e) {
            postEvent(
                new InternalErrorEvent(Handler.this,
                    "Controller not in Started state at EOM") );
        }
		
		
	}

	//@Override
	@Override
	public void doPlayerSetMediaTime(Time t)
	{
		logger.info("Handler.doPlayerSetMediaTime " + t.getSeconds());
		
		
		
		// TODO: bounds checking
		filterGraph.getRoot().getDemux().setPosition(t, 0);	// TODO: rounding?
		
        // TODO: 
//		long nanoseconds = t.getNanoseconds();
//		
//        //  Enforce bounds
//        if( nanoseconds > duration.getNanoseconds() ) {
//            nanoseconds = duration.getNanoseconds();
//        } else
//
//        if( nanoseconds < 0 ) {
//            nanoseconds = 0;
//        }
//
//        //  If the video is currently playing and hasn't reached
//        //  the end, then stop it, reset the nanosecond offset,
//        //  and restart it.  Otherwise, just set the nanosecond
//        //  offset.
//
//        if( playthread != null &&
//            playthread.isAlive() &&
//            ! endOfMedia() )
//        {
//            stop();
//            this.nanoseconds = nanoseconds;
//            start();
//        } else {
//            this.nanoseconds = nanoseconds;
//        }
//        calcInitFrame();
        
	}

	
	//@Override
	@Override
	public float doPlayerSetRate(float rate)
	{
		logger.fine("Handler.doPlayerSetRate " + rate);
        //  Zero is the only invalid rate
        if( rate == 0 ) return getRate();	// TODO: neg. not supported
        if( rate < 0 ) return getRate();	
        
        // TODO: 
        

//        //  If the video is currently playing and hasn't reached
//        //  the end, then stop it, reset the rate, and restart it.
//        //  Otherwise, just set the nanosecond offset.
//
//        if( playthread != null &&
//            playthread.isAlive() &&
//            ! endOfMedia() )
//        {
//            stop();
//            this.rate = rate;
//            start();
//        } else {
//            this.rate = rate;
//        }
        //calcInitFrame();
        return rate;
	}

	//@Override
	@Override
	public boolean doPlayerStop()
	{
		logger.info("Handler.doPlayerStop");
		
		if (trackThreads == null)
			return true;
		final List<TrackThread> waitUntilClosed = new ArrayList<TrackThread>();
		
		for (int i = 0; i < trackThreads.length; ++i)
		{
			final TrackThread t = trackThreads[i];
			if (t != null && t.isAlive())
			{	t.close(); // shut down gracefully
				waitUntilClosed.add(t);

			}
		}
		
//		// TODO: is it legal to call stop (for example Renderer.stop), potentially while 
//		// Renderer.process is being called?  This seems like this could cause
//		// more problems than it solves.  Perhaps we should wait for the tracks
//		// threads to complete before doing this.  This requires that the
//		// nodes themselves will successfully respond to an interruption.
//		try
//		{
//			FilterGraph.stop(root);
//		} catch (IOException e)
//		{
//			logger.log(Level.WARNING, "" + e, e);
//			postControllerErrorEvent("" + e);
//			return false;
//		}
		
		for (TrackThread t : waitUntilClosed)
		{
			// make sure threads are really stopped.  this can cause a thread deadlock.
			try
			{
				t.waitUntilClosed();
			} catch (InterruptedException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return false;
			}
		}
		
		// stop after the threads have completed, avoiding any threadin problems with
		// process on the filter graph.
		try
		{
			filterGraph.stop();
		} catch (IOException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			postControllerErrorEvent("" + e);
			return false;
		}
		
		return true;
	}

	private boolean firstStart = true;
	
	//@Override
	@Override
	public boolean doPlayerSyncStart(Time time)
	{
		logger.info("Handler.doPlayerSyncStart " + time.getSeconds() + " " + getState());
		
		// the first time we start, the threads are usually in place.  we only need to 
		// create them here if we've been stopped.
		if (firstStart)
		{	firstStart = false;
		}
		else
		{
			// TODO: this code is duplicated in doRealize.
			if (TRACE) logger.fine("Starting filter graph(s)");
			
			try
			{
				filterGraph.start();
			} catch (IOException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				postControllerErrorEvent("" + e);
				return false;
			}			
			
			// TODO: we need to go to the correct time!
			trackThreads = new TrackThread[filterGraph.getNumTracks()];
			
			for (int i = 0; i < filterGraph.getNumTracks(); ++i)
			{
				if (filterGraph.isTrackEnabled(i))
				{	final FilterGraph filterGraphCopy = filterGraph.duplicate();
					trackThreads[i] = new TrackThread(filterGraphCopy, i, FilterGraph.PROCESS_DEFAULT); // use a copy of the graph for each thread, so that they don't interfere with each other.
				}
			}
		}
		
		for (int i = 0; i < trackThreads.length; ++i)
		{
			final TrackThread t = trackThreads[i];
			if (t != null)
				t.start(); 
		}


		return true;
	}

	private static final String toString(Time d)
	{
		if (d == null)
			return "null";	// should never happen
		
		if (d.getNanoseconds() == Time.TIME_UNKNOWN.getNanoseconds())
			return "[Unknown]";
		
		if (d.getNanoseconds() == Duration.DURATION_UNBOUNDED.getNanoseconds())
			return "[Unbounded]";
		
		return "" + d.getNanoseconds();
	}
	
	//@Override
	@Override
	public Time getPlayerDuration()
	{
		// tricky because this gets called by the parent class. during realize, so we won't be in the realized state
		// although we already have the tracks.  
        if( getState() < Realizing || filterGraph == null ) {
      		logger.fine("getPlayerDuration: returning demuxDuration");
      	  
        	return demuxDuration;
        } else
        {
 
  //      if( getState() < Prefetched ) {
        	
        	// code borrowed from AbstractPlayer.  TODO: once each track has its own controller, there will be no need for this to be calculated here.
        	Time duration = null;
        	//Time d = getSource().getDuration();	
    		//if (duration != null && duration.getNanoseconds() != Duration.DURATION_UNKNOWN.getNanoseconds()
        	
    		final Track[] tracks = filterGraph.getTracks();
    		for (int i = 0; i < tracks.length; ++i)
    		{
    			try
    			{
    				if (!tracks[i].isEnabled())
    					continue;
    			 Time d = tracks[i].getDuration();
    			 logger.fine("Track " + i + " has duration of " + toString(d));

    			 if (duration == null)
    			 {
    				 duration = d;
    				 continue;
    			 }
                 if (d == DURATION_UNKNOWN ) {
                     duration = d;
                     break;
                 }

                 if( duration != DURATION_UNBOUNDED &&
                     ( d == DURATION_UNBOUNDED ||
                       d.getNanoseconds() >
                       duration.getNanoseconds() ) )
                 {
                     duration = d;
                 }
    			}
    			catch (Exception e)
    			{	logger.log(Level.WARNING, "" + e, e);
    				continue;
    				// TODO: we get an NPE in at com.sun.media.parser.RawBufferParser$FrameTrack.getDuration(RawBufferParser.java:227)
    				// when using that class for RTP reception.  Not sure why this is occurring, if it is a bug in JMF or FMJ.
    				// for now, this is just a workaround so that we can continue.
    			}
    					
    		}
    		
    		if (duration == null)
    		{	logger.fine("getPlayerDuration: returning DURATION_UNKNOWN (2)");
    			return DURATION_UNKNOWN;
    		}
    		
    		logger.fine("getPlayerDuration: returning " + toString(duration));
            return duration;
        }
        
        // TODO: 
            
//        } else
//        
//        	return DURATION_UNKNOWN;	// TODO

// TODO: 
	}

	//@Override
	@Override
	public Time getPlayerStartLatency()
	{
		return new Time(0);
	}

	//@Override
	@Override
	public Component getVisualComponent()
	{
		return visualComponent;
	}

	// Processor:
	
	@Override
	public boolean doConfigure()
	{
		if (mode == PLAYER)
			throw new UnsupportedOperationException();
		
		// TODO: what to do here?
		return true;
	}
	
	public DataSource getDataOutput() throws NotRealizedError
	{
		if (mode == PLAYER)
			throw new UnsupportedOperationException();
		
		if (getState() < Realized)
			throw new NotRealizedError("Cannot call getDataOutput on an unrealized Processor.");

		
		return filterGraph.getDataOutput();
		
	}

	private TrackControl[] trackControls;
	
	@Override
	public TrackControl[] getTrackControls() throws NotConfiguredError
	{
		if (mode == PLAYER)
			throw new UnsupportedOperationException();
		
		if (getState() < Configured)
			throw new NotConfiguredError("Cannot call getTrackControls on an unconfigured Processor.");

		
		if (trackControls == null)
		{	
			try
			{
				if (!getDemuxTracks())
					throw new RuntimeException("Unable to get demux tracks");
				
				
				final Track[] tracks = demux.getTracks();
				
				trackControls = new TrackControl[tracks.length];
			
				for (int trackNum = 0; trackNum < tracks.length; ++trackNum)
				{
					trackControls[trackNum] = new MyTrackControl(trackNum);
				}
			
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			} catch (BadHeaderException e)
			{
				throw new RuntimeException(e);
			}
		}
		return trackControls;
		
		
	}

	@Override
	public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError
	{
		if (mode == PLAYER)
			throw new UnsupportedOperationException();
		
		if (getState() < Configured)
			throw new NotConfiguredError("Cannot call getSupportedContentDescriptors on an unconfigured Processor.");

		
		final List<Multiplexer> muxs = FilterGraphBuilder.findMuxs();	// TODO: only find ones that are reachable given the input
		final List<ContentDescriptor> result = new ArrayList<ContentDescriptor>();
		for (int i = 0; i < muxs.size(); ++i)
		{
			final Multiplexer mux = (Multiplexer) muxs.get(i);
			final Format[] f = mux.getSupportedOutputContentDescriptors(null);
			for (int j = 0; j < f.length; ++j)
				result.add((ContentDescriptor) f[j]);
		}
		
		final ContentDescriptor[] arrayResult = new ContentDescriptor[result.size()];
		for (int i = 0; i < result.size(); ++i)
			arrayResult[i] = (ContentDescriptor) result.get(i);
		return arrayResult;

		
	}
	
	/** builds mux muxInputFormats, only if not already built */
	private void buildMux()
	{
		if (mux != null)
			return;
		
		if (!getDemuxTracks())
			throw new RuntimeException("Unable to get demux tracks");
		
		mux = FilterGraphBuilder.findMux(outputContentDescriptor);	// TODO: this works for RAW, but not RAW_RTP
		if (mux == null)
			throw new RuntimeException("Unable to find mux for " + outputContentDescriptor);
		final int numMuxInputTracks = mux.setNumTracks(numTracks);	// TODO: this doesn't take into account disabled tracks.
																	// TODO: we have to figure out which tracks correspond to which mux tracks.
		if (numMuxInputTracks != numTracks)
			logger.warning("Multiplexer only supports " + numMuxInputTracks + " tracks, not " + numTracks);
		
		mux.setContentDescriptor(outputContentDescriptor);
		muxInputFormats = new Format[numMuxInputTracks];
		muxInputTrackNumbers = new int[numTracks];
		// by default, input formats to the mux will be the same as the output formats
		// of the demux.  If the caller uses a TrackControl, they can change this.
		int nextMuxInputTrackNumber = 0;
		for (int i = 0; i < numTracks; ++i)
		{
			// TODO: we need to have a consistent way to keep track of enabled and disabled tracks.
			// currently, we create a track for each track in the demux, and even if it is disabled
			// it stays there.  So the demux track numbers and mux track numbers might not match up.
			// For now, we are going to just try to figure out the correspondence
			// by checking compatibility with the mux's supported input formats.
			// TODO: this is a big hack, it could all change once the track enabling/disabling is set,
			// and/or track formats are set.
			muxInputTrackNumbers[i] = -1; // no correspondence
			if ((tracks[i].getFormat() instanceof VideoFormat && anyFormatInstanceOf(mux.getSupportedInputFormats(), VideoFormat.class)) ||
			    (tracks[i].getFormat() instanceof AudioFormat && anyFormatInstanceOf(mux.getSupportedInputFormats(), AudioFormat.class)))
			{	if (nextMuxInputTrackNumber < numMuxInputTracks)
					muxInputTrackNumbers[i] = nextMuxInputTrackNumber++;
			}
			
			final int muxInputTrackNumber = muxInputTrackNumbers[i];
			
			if (muxInputTrackNumber < 0)
				continue; // no corresponding mux input track
			
			// TODO: setting the mux input format to the track output format really only makes sense
			// for a raw mux.
			muxInputFormats[muxInputTrackNumber] = mux.setInputFormat(tracks[i].getFormat(), muxInputTrackNumber);
			if (muxInputFormats[muxInputTrackNumber] == null)
			{	logger.warning("Multiplexer " + mux + " did not accept input format " + tracks[i].getFormat());
				// set to an input format that it will accept.
				// TODO: really this should be negotiated, because it may support multiple input formats, and we
				// want to negotiate this.
				// AND, the format may be unspecified in some aspects.
				muxInputFormats[muxInputTrackNumber] = mux.setInputFormat(mux.getSupportedInputFormats()[0], muxInputTrackNumbers[i]);
			}
		}
		
	}
	
	private static boolean anyFormatInstanceOf(Format[] formats, Class<? extends Format> target)
	{
		for (Format f : formats)
		{	if (target.isAssignableFrom(f.getClass()))
				return true;
		}
		return false;
	}
	
	// TODO: implement.
	// this appears to be able to control how the filter graph is built.
	
	private class MyTrackControl implements TrackControl
	{
		private final int trackNum;

		public MyTrackControl(final int trackNum)
		{
			super();
			this.trackNum = trackNum;
		}

		public void setCodecChain(Codec[] codecs) throws UnsupportedPlugInException, NotConfiguredError
		{
			// TODO
			logger.warning("Not yet supported: setCodecChain");
		}

		public void setRenderer(Renderer renderer) throws UnsupportedPlugInException, NotConfiguredError
		{
//			 TODO
			logger.warning("Not yet supported: setRenderer");
		}

		public Object getControl(String controlType)
		{
			return null;
		}

		public Object[] getControls()
		{
			return new Object[0];
		}

		public Format getFormat()
		{
			if (!getDemuxTracks())
				throw new RuntimeException("Unable to get demux tracks");
			
			if (muxInputFormats == null)
				return tracks[trackNum].getFormat();	// TODO: this appears to be what JMF returns, if the format is not set.
			return muxInputFormats[muxInputTrackNumbers[trackNum]];
		}

		public Format[] getSupportedFormats()
		{
			buildMux();	// Not sure when JMF builds the MUX.

			// TODO: FMJ returns this, but JMF returns a bunch of specific formats.
			// we need to build FilterGraph.buildGraphToMux for this track, for all
			// possible codec chains from the demux to the mux.
			// if the mux supports multiple input formats, we should probably build all
			// possible codec chains to all possible muxs.
			// in FMJ, this is a relatively slow operation, by the way.
			// TODO: the nonspecific formats in FMJ can include null encodings, which causes problems
			// with SIP-Communicator, for example.
			return mux.getSupportedInputFormats();
		}

		private boolean enabled = true;
		
		public boolean isEnabled()
		{
			if (!getDemuxTracks())
				throw new RuntimeException("Unable to get demux tracks");

			// return enabled;
			return tracks[trackNum].isEnabled();
		}

		public void setEnabled(boolean enabled)
		{
			if (!getDemuxTracks())
				throw new RuntimeException("Unable to get demux tracks");
			//this.enabled = enabled;
			tracks[trackNum].setEnabled(enabled);
//			 TODO: how to actually make this take effect, if it is false?
		}

		public Format setFormat(Format format)
		{
			buildMux();		// Not sure when JMF builds the MUX.
			
			// supposed to set the format of the input to this track of the MUX.
			// this means we must already have created the mux?
			// or maybe what this means, is we have to build a chain of configured codecs to 
			// get to the mux?
			// I think what this means is that we build the mux, and set the input format for this
			// track on the mux.  when it comes time to build the graph, the input format on the mux
			// is already fixed, so we don't negotiate it.
			// TODO: we can't accept a format without building a filter graph.
			// example: track format is video, mux is an audio mux.  The mux may be
			// happy with the format, but that is useless unless we can get there from
			// here, that is, unless a graph can be built for that track which
			// produces that format.
			// for now, we'll work around this by just doing some really basic compatibility testing.:
			if (tracks[trackNum].getFormat() instanceof VideoFormat && !(format instanceof VideoFormat))
			{	logger.fine("Rejecting MUX input format " + format + " because a filter graph will not be able to be built from " + tracks[trackNum].getFormat());
				return null;
			}
			if (tracks[trackNum].getFormat() instanceof AudioFormat && !(format instanceof AudioFormat))
			{	logger.fine("Rejecting MUX input format " + format + " because a filter graph will not be able to be built from " + tracks[trackNum].getFormat());
				return null;
			}
			
			if (muxInputTrackNumbers[trackNum] < 0)
			{	logger.fine("Rejecting MUX input format " + format + " for track " + trackNum + " because there is no corresponding mux input track");
				return null;
			}
			
			final Format result = mux.setInputFormat(format, muxInputTrackNumbers[trackNum]);
			if (result == null)
			{	logger.fine("Multiplexer rejected input format for track " + trackNum + ": " + format);
				return null;
			}
			muxInputFormats[muxInputTrackNumbers[trackNum]] = result; 
			return muxInputFormats[muxInputTrackNumbers[trackNum]];
		}

		public Component getControlComponent()
		{
			return null;
		}
		
	}
	
	


	

}
