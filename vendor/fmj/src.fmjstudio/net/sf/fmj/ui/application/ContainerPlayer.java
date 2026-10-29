package net.sf.fmj.ui.application;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.StartEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.sf.fmj.gui.controlpanel.SwingLookControlPanel;
import net.sf.fmj.utility.ClasspathChecker;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.URLUtils;

/**
 * ContainerPlayer.
 * 
 * Slider update code adapted from EJMF StandardProgressControl.
 * 
 * @author Warren Bloomer
 */
public class ContainerPlayer {

	private static final Logger logger = LoggerSingleton.logger;

	/** the visual contain to place this player in */
	private Container container;

	/** the JMF player */
	private volatile Player player;

	/** the visual component of the player */
	private Component visualComponent;

	/** whether the player should start upon being "realized" */
	private boolean shouldStartOnRealize = false;
	/** whether we should loop upon EOM */
	private volatile boolean autoLoop = false;
	
	/** listener for mouse events from visual component */
	private MouseListener mouseListener;

	/** the location of the media. A URI string */
	private String mediaLocation;
	

 
 	
	/** controller listener to listen to controller events from the player */
	private ControllerListener controllerListener = new ControllerListener() {
		public void controllerUpdate(ControllerEvent event) {

			logger.fine("Got controller event: " + event);
			
			Player player = (Player) event.getSourceController();

			if (player != ContainerPlayer.this.player)
				return;		// ignore messages from old players.
			
			// TODO: handle RestartingEvent
			if (event instanceof RealizeCompleteEvent) {
				notifyStatusListener(ContainerPlayerStatusListener.REALIZE_COMPLETE);
				// controller realized
				try {
					setVisualComponent(player.getVisualComponent());
					if (shouldStartOnRealize) {
						start();
						
						
					}
				}
				catch (Exception e) {
					notifyStatusListener(ContainerPlayerStatusListener.ERROR_SHOWING_PLAYER);
					logger.log(Level.WARNING, "" + e, e);
					JLabel label = new JLabel("");
					setVisualComponent(label);
				}
			}
			else if (event instanceof ResourceUnavailableEvent) {
				notifyStatusListener(ContainerPlayerStatusListener.RESOURCE_UNAVAILABLE);
				JLabel label = new JLabel("");
				setVisualComponent(label);
			}
			else if (event instanceof StopEvent) {
				notifyStatusListener(ContainerPlayerStatusListener.STOPPED);
			}
			else if (event instanceof StartEvent)
			{
				notifyStatusListener(ContainerPlayerStatusListener.STARTED);
			}
			else if (event instanceof ControllerErrorEvent)
			{
				notifyStatusListener(ContainerPlayerStatusListener.ERROR_PREFIX + ((ControllerErrorEvent) event).getMessage());
			}
			else if (event instanceof ControllerClosedEvent) 
			{
			}
			
			// Borrowed from EJMF GenericPlayer:
	        if (event instanceof EndOfMediaEvent) {
				notifyStatusListener(ContainerPlayerStatusListener.END_OF_MEDIA);
	            // End of the media -- rewind
	            player.setMediaTime(new Time(0));  // TODO: this causes some demuxes to reconnect to the datasource.
	            if (autoLoop)
	            	start();

	        }
		}
	};
	
	private void notifyStatusListener(String status)
	{
		if (statusListener != null) 
			statusListener.onStatusChange(status);
		
	}
	

	/**
	 * Constructor.
	 * 
	 * @param container
	 */
	public ContainerPlayer(Container container) {
		setContainer(container);
	}
	

	
	public void setMediaLocation(String mediaLocation, boolean startAutomatically) throws NoDataSourceException, NoPlayerException, IOException {
		
		logger.fine("setMediaLocation: " + mediaLocation + " startAutomatically=" + startAutomatically);
		
		try
		{
			MediaLocator locator = new MediaLocator(mediaLocation);
			stop();
			if (player != null) {
				player.close();
				player.deallocate();
				player = null;
			}
			notifyStatusListener(ContainerPlayerStatusListener.LOADING);	
			if (startAutomatically)
				shouldStartOnRealize = true;
			createNewPlayer(locator);
		}
//		catch (NoDataSourceException e)
//		{
//			setMediaLocationFailed = true;
//			notifyStatusListener(ContainerPlayerStatusListener.CREATE_PLAYER_FAILED);
//			throw e;
//		}
		catch (NoPlayerException e)
		{
			notifyStatusListener(ContainerPlayerStatusListener.CREATE_PLAYER_FAILED);
			throw e;
		}
		catch (IOException e)
		{
			notifyStatusListener(ContainerPlayerStatusListener.CREATE_PLAYER_FAILED);
			throw e;
		}
		catch (RuntimeException e)
		{
			notifyStatusListener(ContainerPlayerStatusListener.CREATE_PLAYER_FAILED); 
			throw e;
		}
	}

	
	/** Used in the case of things like RTP wizard, which create a player/processor explicitly. */
	public void setRealizedStartedProcessor(Processor p)
	{
		stop();
		if (player != null) {
			player.close();
			player.deallocate();
			player = null;
		}
		notifyStatusListener(ContainerPlayerStatusListener.PROCESSING);	
		
		useExistingRealizedStartedPlayer(p);
	}

	/**
	 * 
	 */
	public void start() {
		if (player != null) {
			shouldStartOnRealize = false;
			player.start();
//			// copied from EJMF StandardStartControl
//		    final int state = player.getState();
//
//		    if (state == Controller.Started) 
//		    	return;
//
//		    if (state < Controller.Prefetched) {
//				StateWaiter w = new StateWaiter(player);
//				w.blockingPrefetch();
//		    }
//
//		    final TimeBase tb = player.getTimeBase();
//		    player.syncStart(tb.getTime());
		}
		else
		{
			shouldStartOnRealize = true;
		}
	}

	/**
	 * 
	 */
	public void stop() {
		shouldStartOnRealize = false;
		if (player != null) {
			player.stop();
		}
	}

	
	/**
	 * Deallocate all resources used by the player.
	 */
	public void deallocate() {
		shouldStartOnRealize = false;
		if (player != null) {
			player.deallocate();
			player = null;
		}
	}

	/**
	 * Deallocate resources and close the player.
	 */
	public void close() {
		shouldStartOnRealize = false;
		if (player != null) {
			player.close();
		}
	}


	/* --------------- private methods ------------------- */


	
	private void createNewPlayer(MediaLocator source) throws NoPlayerException, IOException {
		if (!ClasspathChecker.check())
		{
			// workaround because JMF does not like relative file URLs.
			if (source.getProtocol().equals("file"))
			{	final String newUrl = URLUtils.createAbsoluteFileUrl(source.toExternalForm());
				if (newUrl != null)
				{
					final MediaLocator newSource = new MediaLocator(newUrl);
					if (!source.toExternalForm().equals(newSource.toExternalForm()))
					{
						logger.warning("Changing file URL to absolute for JMF, from " + source.toExternalForm() + " to " + newSource);
						source = newSource;
					}
				}
			}
		}
		
		player = javax.media.Manager.createPlayer(source);
		


	    	
		player.addControllerListener(controllerListener);
		getTransportControlPanel().setPlayer(player);
		player.realize();
	}
	
	private void useExistingRealizedStartedPlayer(Player p)  {
		player = p;
	
		player.addControllerListener(controllerListener);
		getTransportControlPanel().setPlayer(player);
	}

	/**
	 * Set the visdual component.
	 * 
	 * @param newVisualComponent
	 */
	private void setVisualComponent(final Component newVisualComponent) {
		if (this.visualComponent == newVisualComponent) {
			return;
		}

		if (getMouseListener() != null) {
			newVisualComponent.addMouseListener(getMouseListener());
		}

		Runnable runnable = new Runnable() {
			public void run() {
				if (ContainerPlayer.this.visualComponent != null)
					getContainer().remove(ContainerPlayer.this.visualComponent);
				ContainerPlayer.this.visualComponent = newVisualComponent;
				if (newVisualComponent != null)
					getContainer().add(newVisualComponent);
				getContainer().validate();
			}
		};
		SwingUtilities.invokeLater(runnable);




	}

	private void setContainer(Container container) {
		this.container = container;
	}

	private Container getContainer() {
		return container;
	}

	public void setMouseListener(MouseListener mouseListener) {
		this.mouseListener = mouseListener;
	}

	private MouseListener getMouseListener() {
		return mouseListener;
	}

	private ContainerPlayerStatusListener statusListener;
	public void setContainerPlayerStatusListener(ContainerPlayerStatusListener listener)
	{
		statusListener = listener;
	}
	
	

    

    
    public void setGain(float value)
    {
    	if (player != null && player.getGainControl() != null)
    	{
    		player.getGainControl().setLevel(value);
    	}
    }

	public void setMute(boolean value)
	{
    	if (player != null && player.getGainControl() != null)
    	{
    		player.getGainControl().setMute(value);
    	}
	}


	public boolean isAutoLoop()
	{
		return autoLoop;
	}


	public void setAutoLoop(boolean autoLoop)
	{
		this.autoLoop = autoLoop;
	}
	
	private SwingLookControlPanel transportControlPanel;
	
	public SwingLookControlPanel getTransportControlPanel()
	{
		if (transportControlPanel == null)
		{	transportControlPanel = new SwingLookControlPanel();
		}
		return transportControlPanel;
	}
    
    

}