package net.sf.fmj.qt.media.content.unknown;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.protocol.DataSource;

import net.sf.fmj.ejmf.toolkit.media.AbstractPlayer;
import net.sf.fmj.gui.controlpanelfactory.ControlPanelFactorySingleton;
import net.sf.fmj.utility.LoggerSingleton;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.view.MoviePlayer;
import quicktime.app.view.QTFactory;
import quicktime.app.view.QTJComponent;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTException;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.movies.media.AudioMediaHandler;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.MediaHandler;
import quicktime.std.movies.media.VisualMediaHandler;

/**
 * 
 * Handler for Quicktime for Java, which bypasses most of JMF (parsers, codecs).
 * @author Ken Larson
 *
 */
public class QuickTimeForJavaHandler extends AbstractPlayer
{
	private static final Logger logger = LoggerSingleton.logger;

	private boolean prefetchNeeded = true;
	
	private static final boolean TRACE = true;
	
	private Movie movie;
	private QTJComponent movieComponent = null;
        
	// This will resize the window to the size of the new movie
	public void createNewMovieFromURL (String theURL) throws QTException
	{
		logger.fine(theURL);

		if (theURL.toLowerCase().endsWith("avi")) {
            throw new QTException("AVI files are not supported by LimeWire's built in video player on OS X.");
        }
		
		String path = theURL.replace("file:///", "");
		QTFile file = new QTFile(path);
		OpenMovieFile movieFile = OpenMovieFile.asRead(file);
		movie = Movie.fromFile(movieFile);
			
		// determine whether we have audio, video, or both:
		boolean hasAudio = false;
		boolean hasVideo = false;
		
		for (int i = 1; i <= movie.getTrackCount(); ++i)
		{
			final Track track = movie.getTrack(i);
			final Media media = track.getMedia();
			final MediaHandler mediaHandler = media.getHandler();
			// MediaHandler can be instanceof both AudioMediaHandler and VisualMediaHandler, for example MPEGMediaHandler.
			if (mediaHandler instanceof AudioMediaHandler)
			{
				hasAudio = true;
			}
			if (mediaHandler instanceof VisualMediaHandler)
			{	
				hasVideo = true;
			}
		}
		
		if (hasAudio)
			setGainControl(new QTGainControl(movie));
		
		if (!hasVideo)
		{
			// if the file does not have a readable video part, then let's throw an exception
			// that will cause the file to be opened in a native video player...
			throw new QTException("The video file does not have a readable video part!!!");
		}		
		else
		{
            if (movieComponent == null) {
                movieComponent = QTFactory.makeQTJComponent(new MoviePlayer(movie));
            } else {
                movieComponent.setMoviePlayer(new MoviePlayer(movie));
            }
		}

		movie.start();
	}
	
	private void registerStopTriggerNotification() throws QTException
	{
		// This used to register a QuickTime ExtremesCallBack that would be called when
		// the movie stopped, but since we weren't getting any reliable events on Windows
		// when the player stops, we created a timer elsewhere to check the players state
		// and deactivated the callback here on OS X as well.
	}

	
    public void setSource(DataSource source) throws IncompatibleSourceException
	{
		if (TRACE) logger.fine("DataSource: " + source);
		
		try {
			QTSession.open();
			
			createNewMovieFromURL(source.getLocator().toExternalForm());
		} catch (QTException e) {
			logger.log(Level.WARNING, "" + e, e);
			throw new IncompatibleSourceException();
		}
		catch (Throwable e) {
			logger.log(Level.WARNING, "" + e, e);
			throw new IncompatibleSourceException();
		}
		
	    super.setSource(source);
	}
    
	//@Override
	public void doPlayerClose()
	{
		// TODO
		logger.info("Handler.doPlayerClose");

		try {
			movie.stop();
		} catch (StdQTException e) {
		}

		QTSession.exitMovies();

		try
		{
			QTSession.close();
		} 
		catch (Exception e)
		{
			// a NullPointerException has occasionally been occurring when closing the QTSession,
			// b/c close() tries to stop a movie TaskThread that no longer exists.
			// let's catch and swallow the exception...
		}
	}

	//@Override
	public boolean doPlayerDeallocate()
	{
		logger.info("Handler.doPlayerDeallocate");
		
		if (movieComponent != null) {
			super.setVisualComponent(null);
			movieComponent = null;
		}

		if (movie != null) {
			movie = null;
		}
			
		return true;
	}

	//@Override
	public boolean doPlayerPrefetch()
	{
		if( ! prefetchNeeded ) return true;
		 
		prefetchNeeded = false;
 
		return true;
	}

	//@Override
	public boolean doPlayerRealize()
	{	
		return true;
	}

	//@Override
	public void doPlayerSetMediaTime(Time t)
	{
		logger.info("Handler.doPlayerSetMediaTime (sec): " + t.getSeconds());
		
		try
		{
			movie.setTime(new TimeRecord(movie.getTimeScale(), (long) (t.getSeconds() * movie.getTimeScale())));
		} catch (StdQTException e)
		{
			// TODO handle
			logger.log(Level.WARNING, "" + e, e);
		} catch (QTException e)
		{
			// TODO handle
			logger.log(Level.WARNING, "" + e, e);
		}
	}

	//@Override
	public float doPlayerSetRate(float rate)
	{
		logger.fine("Handler.doPlayerSetRate " + rate);
		try
		{
			if (false)
				movie.setRate(rate);	// TODO: setting this to 1.0 starts the movie
			return rate;
		} catch (StdQTException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			return getRate();
		}
	}

	//@Override
	public boolean doPlayerStop()
	{
		logger.info("Handler.doPlayerStop");
		try {
			if (movie != null)
				movie.stop();
		} catch (QTException err) {
			logger.log(Level.WARNING, "" + err, err);
			return false;
		}
		return true;
	}

	//@Override
	public boolean doPlayerSyncStart(Time t)
	{
		logger.info("Handler.doPlayerSyncStart (sec): " + t.getSeconds());
		try {
			registerStopTriggerNotification();
			movie.start();
		} catch (QTException e) {
			logger.log(Level.WARNING, "" + e, e);
			return false;
		}

		return true;
	}

	//@Override
	public Time getPlayerDuration()
	{
        if (getState() < Realized)
		{
			return DURATION_UNKNOWN;
		} 
        else if (movie != null)
		{
			try
			{
				final double totalSeconds = (double) movie.getDuration() / (double) movie.getTimeScale();
				return new Time(totalSeconds);

			} catch (StdQTException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return DURATION_UNKNOWN;
			}

		} 
        else
        {
			return DURATION_UNKNOWN;	
        }
	}

	//@Override
	public synchronized Time getMediaTime()
	{
		if (getState() < Realized)
		{
			return super.getMediaTime();
		} 
		else if (movie != null)
		{
			try
			{
				final double totalSeconds = (double) movie.getTime() / (double) movie.getTimeScale();
				return new Time(totalSeconds);

			} catch (StdQTException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return super.getMediaTime();
			}

		} 
		else
		{
			return super.getMediaTime();
		}
	}



	// @Override
	public Time getPlayerStartLatency()
	{
		return new Time(0);
	}

	//@Override
	public Component getVisualComponent()
	{
		assert movieComponent != null;

		//TODO maintain aspect ratio
		//return this directly so it can scale
		return movieComponent.asJComponent();

	}
	
//	//@Override
//	public Component getVisualComponent()
//	{
//		if (qtcMovie == null)
//			return null;
//		// by putting it in a panel, we prevent it from scaling if put into a different sized container.
//		final JPanel result = new JPanel();
//
//		final JComponent jComponent = qtcMovie.asJComponent();
//		result.add(jComponent);
//		result.setPreferredSize(jComponent.getPreferredSize());
//		
//		return result;
//
//	}



//	@Override
//	public Component getControlPanelComponent()
//	{
//		if (qtcMovieController == null)
//			return null;
//		return qtcMovieController.asComponent();
//	}
	
	// until we figure out how to isolate QT's control panel component (get it without the movie panel),
	// we'll use FMJ's control panel.
    public Component getControlPanelComponent() {
        Component c = super.getControlPanelComponent();

        if( c == null ) {
            c = ControlPanelFactorySingleton.getInstance().getControlPanelComponent(this);
            setControlPanelComponent(c);
        }

        return c;
    }

}
