package net.sf.fmj.apps.applet;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;
import javax.swing.SwingUtilities;

import net.sf.fmj.ejmf.toolkit.util.PlayerDriver;
import net.sf.fmj.ejmf.toolkit.util.PlayerPanel;
import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.utility.ClasspathChecker;

/**
 * Media playback applet.
 * Based on EJMF GenericPlayer.
 * @author Ken Larson
 *
 */
public class FmjApplet extends PlayerDriver implements ControllerListener
{

    private PlayerPanel playerpanel;
    private Player player;

    public static void main(String args[]) {
      	
    	main(new FmjApplet(), args);
    }

    @Override
	public void init()
	{
    	RegistryDefaults.setDefaultFlags(RegistryDefaults.FMJ);
    	
    	if (!ClasspathChecker.checkManagerImplementation())
    	{	// JMF manager is in charge, we need to wipe out anything in its registry, and register ours.
    		// let's get rid of any JMF entries in registry.
    		RegistryDefaults.unRegisterAll(RegistryDefaults.JMF | RegistryDefaults.FMJ_NATIVE | RegistryDefaults.THIRD_PARTY);
    		RegistryDefaults.registerAll(RegistryDefaults.getDefaultFlags());
    	}
    	
		super.init();
	}

	@Override
	public void begin() {
        playerpanel = getPlayerPanel();
        player = playerpanel.getPlayer();

        // Add ourselves as a listener to the player's events
        player.addControllerListener(this);

        // Start Player
        player.start();
    }

    /**
     * This controllerUpdate function must be defined in order to
     * implement a ControllerListener interface. This 
     * function will be called whenever there is a media event.
     *
     * @param          event
     *                 the media event
     */
    public synchronized void controllerUpdate(ControllerEvent event) {

        if( event instanceof RealizeCompleteEvent ) {

            Runnable r = new Runnable() {
                public void run() {
                    // Add Control Panel Component
                    playerpanel.addControlComponent();

                    // Add Visual Component
                    playerpanel.addVisualComponent();
                }
            };

            SwingUtilities.invokeLater(r);
        } else

        if (event instanceof EndOfMediaEvent) {
            // End of the media -- rewind
            player.setMediaTime(new Time(0));
        }
    }
}   
