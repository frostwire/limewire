package net.sf.fmj.qt.media.content.unknown;

import org.rococoa.cocoa.qtkit.QTTrack;

import net.sf.fmj.media.AbstractGainControl;

/**
 * 
 * @author Greg Kellum
 *
 */
public class RococoaGainControl extends AbstractGainControl
{
    private final QTTrack track;

    public RococoaGainControl(QTTrack track)
    {
        super();
        this.track = track;
    }


    public float getLevel()
    {
        if (getMute())
            return getSavedLevelDuringMute();

        final float result = track.volume();
        return result;
    }



    public float setLevel(float level)
    {
        track.setVolume(level);
        
        return getLevel();
    }


}
