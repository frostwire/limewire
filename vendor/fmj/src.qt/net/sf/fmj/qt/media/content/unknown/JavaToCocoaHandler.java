package net.sf.fmj.qt.media.content.unknown;

import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.protocol.DataSource;

import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSArray;
import org.rococoa.cocoa.qtkit.MovieComponent;
import org.rococoa.cocoa.qtkit.QTKit;
import org.rococoa.cocoa.qtkit.QTMedia;
import org.rococoa.cocoa.qtkit.QTMovie;
import org.rococoa.cocoa.qtkit.QTMovieView;
import org.rococoa.cocoa.qtkit.QTTime;
import org.rococoa.cocoa.qtkit.QTTrack;

import net.sf.fmj.concurrent.ExecutorServiceManager;
import net.sf.fmj.ejmf.toolkit.media.AbstractPlayer;
import net.sf.fmj.gui.controlpanelfactory.ControlPanelFactorySingleton;
import net.sf.fmj.utility.ExceptionUtils;
import net.sf.fmj.utility.LoggerSingleton;

/**
 * RococoaHandler which wraps the Cocoa QuickTime library QTKit.
 */
public class JavaToCocoaHandler extends AbstractPlayer
{
    private static final Logger logger = LoggerSingleton.logger;
    
    private static final ExecutorService executorService = ExecutorServiceManager.getExecutorService();

    private boolean prefetchNeeded = true;
    
    private static final boolean TRACE = false;

    private QTMovie movie;
    private MovieComponent movieComponent;
    private QTMovieView movieView;
    private QTTime readPositionInMedia;
    
    static {
        // load library
        try {
            QTKit instance = QTKit.instance;
            QTMovieView movieView = QTMovieView.CLASS.create();
        }
        catch (java.lang.UnsatisfiedLinkError error) {
            throw new ExceptionInInitializerError("librococoa.dylib could not be loaded!");
        }
        
    }
        
    // This will resize the window to the size of the new movie
    public void createNewMovieFromURL (String theURL) throws IncompatibleSourceException
    {
        logger.fine(theURL);

        String path = theURL.replace("file:///", "");

        movieView = QTMovieView.CLASS.create();
        movieView.setControllerVisible(false);
        movieView.setPreservesAspectRatio(true);

        movieComponent = new MovieComponent(movieView);

        // Passing an error reference to QTMovie sometimes causes the entire application to 
        // crash b/c a null pointer occurs in Cocoa. So, let's not do that, but instead we'll
        // just check if the movie object is null.
        ObjCObjectByReference errorReference = new ObjCObjectByReference();
        movie = QTMovie.movieWithFile_error(path, errorReference);

        if (movie == null) {
            throw new IncompatibleSourceException("The movie " + path + " could not be opened in the Cocoa QuickTime Player.");
        }
        
        movieView.setMovie(movie);                
        movie.gotoBeginning();
        
        NSArray audioTracks = movie.tracksOfMediaType(QTMedia.QTMediaTypeSound);
        if (audioTracks != null && audioTracks.count() > 0) {
            QTTrack track = Rococoa.cast(movie.tracksOfMediaType(QTMedia.QTMediaTypeSound).objectAtIndex(0), QTTrack.class);
            setGainControl(new RococoaGainControl(track));
        }

        movie.play();
    }
    
    private void registerStopTriggerNotification()
    {
        // This used to register a QuickTime ExtremesCallBack that would be called when
        // the movie stopped, but since we weren't getting any reliable events on Windows
        // when the player stops, we created a timer elsewhere to check the players state
        // and deactivated the callback here on OS X as well.
    }

    
    public void setSource(final DataSource source) throws IncompatibleSourceException
    {
        if (TRACE) logger.fine("DataSource: " + source);

        Future future = executorService.submit(new Callable<Void>() {
            public Void call() throws IncompatibleSourceException {
                if (movie != null) {
                    movie.stop();
                }

                createNewMovieFromURL(source.getLocator().toExternalForm());
                JavaToCocoaHandler.super.setSource(source);
                
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }
    
    //@Override
    public void doPlayerClose()
    {
        // TODO
        logger.info("RococoaHandler.doPlayerClose");
        
        Future future = executorService.submit(new Runnable(){
            public void run() {
                if (movie != null) {
                    movie.stop();
                }
            }
        });

        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        } 
    }

    //@Override
    public boolean doPlayerDeallocate()
    {
        logger.info("RococoaHandler.doPlayerDeallocate");
        
        if (movieComponent != null) {
            movieComponent = null;
        }
        
        if (movie != null) {
            movie = null;
        }
        
        if (movieView != null) {
            movieView = null;
        }

        if (readPositionInMedia != null) {
            readPositionInMedia = null;
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

    private int timeScale = -1;
    
    //@Override
    public synchronized void doPlayerSetMediaTime(final Time t)
    {
        if (TRACE) logger.info("RococoaHandler.doPlayerSetMediaTime (sec): " + t.getSeconds());

        readPositionInMedia = new QTTime( (long) (t.getSeconds() * timeScale), timeScale);

        Future future = executorService.submit(new Runnable(){
            public void run() {
                if (timeScale == -1) {
                    QTTime currentTime = movie.currentTime();
                    timeScale = currentTime.timeScale.intValue();
                }
                movie.setCurrentTime( new QTTime( (long) (t.getSeconds() * timeScale), timeScale) );
            }
        });

        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        }
    }

    //@Override
    public float doPlayerSetRate(float rate)
    {
        if (TRACE) logger.fine("RococoaHandler.doPlayerSetRate " + rate);
        
        // for some reason the fmj people had this function disabled.
        //movie.setRate(rate);
        
        return rate;
    }

    //@Override
    public boolean doPlayerStop()
    {
        logger.info("RococoaHandler.doPlayerStop");

        // since we're stopping the movie on a separate thread, let's make a local copy of the reference
        // to the current movie object in case another movie is loaded before this thread runs.
        final QTMovie movieCopyOfReference = movie;
        
        Future future = executorService.submit(new Runnable(){
            public void run() {
                if (movieCopyOfReference != null)
                    movieCopyOfReference.stop();
            }
        });

        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        }
        
        return true;
    }

    //@Override
    public boolean doPlayerSyncStart(Time t)
    {
        if (TRACE) logger.info("RococoaHandler.doPlayerSyncStart (sec): " + t.getSeconds());

        registerStopTriggerNotification();
        
        Future future = executorService.submit(new Runnable(){
            public void run() {
                if (movie != null)
                    movie.play();
            }
        });

        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
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
            Future<Time> future = executorService.submit(new Callable<Time>() {                 
                public Time call() {
                    QTTime duration = movie.duration();
                    final double totalSeconds = (double) duration.timeValue / duration.timeScale.doubleValue();
                    return new Time(totalSeconds);
                }
            });
            
            try {
                return future.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "" + e, e);
                ExceptionUtils.reportOrReturn(e);
                return DURATION_UNKNOWN;
            } catch (ExecutionException e) {
                logger.log(Level.WARNING, "" + e, e);
                ExceptionUtils.reportOrReturn(e.getCause());
                return DURATION_UNKNOWN;
            } catch (TimeoutException e) {
                return DURATION_UNKNOWN;
            }
        } 
        else
        {
            return DURATION_UNKNOWN;    
        }
    }

    public synchronized void setMediaTime(QTTime qtTime) {
        readPositionInMedia = qtTime;
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
            // For some odd reason if movie.currentTime() is called while LW is being resized,
            // the thread is given over to the Cocoa main thread and never comes back again. So, here we make the call on 
            // a thread that we'll kill after 100 msecs if it doesn't return.
            Future future = executorService.submit(new Runnable() {
                public void run() {
                    if (movie != null) {
                        QTTime currentTime = movie.currentTime();
                        setMediaTime(currentTime);
                    }
                }
            });
            
            try {
                future.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "" + e, e);
                ExceptionUtils.reportOrReturn(e);
                return DURATION_UNKNOWN;
            } catch (ExecutionException e) {
                logger.log(Level.WARNING, "" + e, e);
                ExceptionUtils.reportOrReturn(e.getCause());
                return DURATION_UNKNOWN;
            } catch (TimeoutException e) {
            }

            if (readPositionInMedia != null) {
                final double totalSeconds = (double) readPositionInMedia.timeValue / readPositionInMedia.timeScale.doubleValue();
                return new Time(totalSeconds);
            } else {
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

        return movieComponent;
    }

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
