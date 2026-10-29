package net.sf.fmj.apps.play;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;
import javax.swing.SwingUtilities;

import net.sf.fmj.ejmf.toolkit.util.PlayerDriver;
import net.sf.fmj.ejmf.toolkit.util.PlayerPanel;
import net.sf.fmj.utility.FmjStartup;

/**
 * A simple FMJ player application.  Does not contain the large number of features that FMJ studio has.
 * Based on EJMF GenericPlayer.
 * @author Ken Larson
 *
 */
public class FmjPlay extends PlayerDriver implements ControllerListener
{

    private PlayerPanel playerpanel;
    private Player player;

    public static void main(String args[]) {
      	
    	FmjStartup.init();
    	
    	main(new FmjPlay(), args);
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
